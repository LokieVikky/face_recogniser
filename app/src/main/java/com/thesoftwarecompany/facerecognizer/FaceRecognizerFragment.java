package com.thesoftwarecompany.facerecognizer;


import static com.thesoftwarecompany.facerecognizer.UserListFragment.EMP_ID;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.camera2.CameraCharacteristics;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.thesoftwarecompany.facerecognizer.customview.OverlayView;
import com.thesoftwarecompany.facerecognizer.database.FaceRecognizerDB;
import com.thesoftwarecompany.facerecognizer.database.entities.EmployeeEntity;
import com.thesoftwarecompany.facerecognizer.database.entities.LogEntity;
import com.thesoftwarecompany.facerecognizer.env.BorderedText;
import com.thesoftwarecompany.facerecognizer.env.ImageUtils;
import com.thesoftwarecompany.facerecognizer.env.Logger;
import com.thesoftwarecompany.facerecognizer.tflite.SimilarityClassifier;
import com.thesoftwarecompany.facerecognizer.tflite.TFLiteObjectDetectionAPIModel;
import com.thesoftwarecompany.facerecognizer.tracking.MultiBoxTracker;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class FaceRecognizerFragment extends CameraFragment implements ImageReader.OnImageAvailableListener {

    public static final long CAPTURE_TIMEOUT = 10000;// 10 Seconds
    private static final Logger LOGGER = new Logger();
    private static final String TAG = "DetectorActivity";
    public static final String REGISTER_MODE = "RegisterMode";
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final int TF_OD_API_INPUT_SIZE = 112;
    private static final boolean TF_OD_API_IS_QUANTIZED = false;
    private static final String TF_OD_FR_API_MODEL_FILE = "mobile_face_net.tflite";
    private static final String TF_OD_FR_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final float TEXT_SIZE_DIP = 10;
    private static DetectorMode MODE = DetectorMode.TF_OD_FACE_REG_API;
    ApplicationClass appClass;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;
    private SimilarityClassifier faceRecognizer;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private boolean computingDetection = false;
    private boolean welcomeDialogShown = false;
    private long timestamp = 0;
    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;
    private MultiBoxTracker tracker;
    // Face detector
    private FaceDetector faceDetector;
    // here the preview image is drawn in portrait way
    private Bitmap portraitBmp = null;
    // here the face is cropped and drawn
    private Bitmap faceBmp = null;
    private EmployeeEntity mSelectedUser;
    private boolean addingUser = false;
    private boolean addPending = false;
    private String empID = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Real-time contour detection of multiple faces
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();
        faceDetector = FaceDetection.getClient(options);
        appClass = (ApplicationClass) mActivity.getApplication();
        MODE = DetectorMode.TF_OD_FACE_REG_API;
        ExtendedFloatingActionButton registerFab = view.findViewById(R.id.fabRegister);
        registerFab.setOnClickListener(v -> {
            addPending = true;
        });
        Bundle b = getArguments();
        if (b != null) {
            empID = b.getString(EMP_ID);
            Log.d(TAG, "onViewCreated: "+empID);
            if (empID.equals("")) {
                setHasOptionsMenu(true);
                registerFab.setVisibility(View.GONE);
            } else {
                setHasOptionsMenu(false);
                registerFab.setVisibility(View.VISIBLE);
            }
        }else{
            setHasOptionsMenu(true);
        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.face_recognizer_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_users:
                appClass.navigateTo(R.id.action_faceRecognizerFragment_to_userListFragment);
                break;
            case R.id.action_view_logs:
                appClass.navigateTo(R.id.action_faceRecognizerFragment_to_logsFragment);
                break;
        }
        return true;
    }

    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
//        if (SAVE_PREVIEW_BITMAP) {
//            ImageUtils.saveBitmap(croppedBitmap);
//        }

        InputImage image = InputImage.fromBitmap(croppedBitmap, 0);
        faceDetector
                .process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        if (faces.size() == 0) {
                            updateResultsAfterFaceRecognition(currTimestamp, new LinkedList<>());
                            return;
                        }
                        runInBackground(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        onFacesDetected(currTimestamp, faces, addPending);
                                    }
                                });
                    }
                });
    }

    @Override
    protected void onPreviewSizeChosen(Size size, int rotation) {

        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, mContext.getResources().getDisplayMetrics());
        BorderedText borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(mContext);

        // Initializing Face Recognizer
        try {
            faceRecognizer = TFLiteObjectDetectionAPIModel.create(
                    mContext.getAssets(),
                    TF_OD_FR_API_MODEL_FILE,
                    TF_OD_FR_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED,
                    mContext
            );
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            mContext, "R.string.ClassifierCould", Toast.LENGTH_SHORT);
            toast.show();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        int screenOrientation = getScreenOrientation();
        sensorOrientation = rotation - screenOrientation;
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);

        //   sensorOrientation = 270;
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);
        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);


        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);


        int targetW, targetH;
        if (sensorOrientation == 90 || sensorOrientation == 270) {
            targetH = previewWidth;
            targetW = previewHeight;
        } else {
            targetW = previewWidth;
            targetH = previewHeight;
        }
        int cropW = (int) (targetW / 2.0);
        int cropH = (int) (targetH / 2.0);

        croppedBitmap = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888);

        portraitBmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888);
        faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropW, cropH,
                        sensorOrientation, MAINTAIN_ASPECT);

//    frameToCropTransform =
//            ImageUtils.getTransformationMatrix(
//                    previewWidth, previewHeight,
//                    previewWidth, previewHeight,
//                    sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        // View v = LayoutInflater.from(mContext).inflate(getLayoutId(),null,false);
        trackingOverlay = getView().findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                canvas -> {
                    tracker.draw(canvas);
                    if (isDebug()) {
                        tracker.drawDebug(canvas);
                    }
                });

        //   tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tfe_od_camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    protected void setNumThreads(int numThreads) {

    }

    @Override
    protected void setUseNNAPI(boolean isChecked) {

    }


    // Face Mask Processing
    private Matrix createTransform(
            final int srcWidth,
            final int srcHeight,
            final int dstWidth,
            final int dstHeight,
            final int applyRotation) {

        Matrix matrix = new Matrix();
        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                LOGGER.w("Rotation of %d % 90 != 0", applyRotation);
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

//        // Account for the already applied rotation, if any, and then determine how
//        // much scaling is needed for each axis.
//        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;
//
//        final int inWidth = transpose ? srcHeight : srcWidth;
//        final int inHeight = transpose ? srcWidth : srcHeight;

        if (applyRotation != 0) {

            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;

    }

    private void updateResultsAfterFaceRecognition(long currTimestamp, final List<SimilarityClassifier.Recognition> mappedRecognitions) {
        tracker.trackResults(mappedRecognitions, currTimestamp);
        trackingOverlay.postInvalidate();
        computingDetection = false;
        if (mappedRecognitions.size() > 0) {
            SimilarityClassifier.Recognition rec = mappedRecognitions.get(0);
            if(empID.equals("")){
                if(!rec.getTitle().equals("")){
                    if(!welcomeDialogShown){
                        welcomeDialogShown = true;
                        getActivity().runOnUiThread(() -> {
                            FaceRecognizerDB db = FaceRecognizerDB.getDatabase(getContext());
                            LogEntity logEntity = new LogEntity(Calendar.getInstance().getTime(),rec.getTitle());
                            db.logDAO().insertAll(logEntity);
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                            alertDialog.setTitle("Welcome");
                            alertDialog.setMessage(rec.getTitle());
                            AlertDialog welcomeDialog = alertDialog.create();
                            welcomeDialog.show();
                            CountDownTimer timer = new CountDownTimer(4000,1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }
                                @Override
                                public void onFinish() {
                                    welcomeDialogShown = false;
                                    welcomeDialog.dismiss();
                                }
                            };
                            timer.start();
                        });
                    }
                }
            }
            if (rec.getExtra() != null) {
                if (!addingUser) {
                    addingUser = true;
                    AddFace(rec);
                }
            }
        }
    }

    private void AddFace(SimilarityClassifier.Recognition rec) {
        if(empID.equals("")){
            addingUser = false;
            return;
        }
        FaceRecognizerDB db = FaceRecognizerDB.getDatabase(getContext());
        EmployeeEntity entity = db.employeeDAO().getUser(empID);
        rec.setTitle(entity.getEmpName() + "_" + entity.getEmpID());
        Map.Entry<String, String> faceData = new AbstractMap.SimpleEntry(entity.empID, rec.toString());
        entity.facePath = faceData.toString();
        Log.d(TAG, "AddFace: "+entity.facePath);
        db.employeeDAO().updateEmployee(entity);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, "Face Registered Successfully", Toast.LENGTH_SHORT).show();
                appClass.goBack();
            }
        });
    }

    private void onFacesDetected(long currTimestamp, List<Face> faces, boolean add) {
        Bitmap cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
        final Canvas canvas = new Canvas(cropCopyBitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.0f);
        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
        switch (MODE) {
            case TF_OD_FACE_REG_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;
        }

        final List<SimilarityClassifier.Recognition> mappedRecognitions =
                new LinkedList<SimilarityClassifier.Recognition>();


        //final List<Classifier.Recognition> results = new ArrayList<>();

        // Note this can be done only once
        int sourceW = rgbFrameBitmap.getWidth();
        int sourceH = rgbFrameBitmap.getHeight();
        int targetW = portraitBmp.getWidth();
        int targetH = portraitBmp.getHeight();
        Matrix transform = createTransform(
                sourceW,
                sourceH,
                targetW,
                targetH,
                sensorOrientation);
        final Canvas cv = new Canvas(portraitBmp);

        // draws the original image in portrait mode.
        cv.drawBitmap(rgbFrameBitmap, transform, null);

        final Canvas cvFace = new Canvas(faceBmp);

        boolean saved = false;

        for (Face face : faces) {

            LOGGER.i("FACE" + face.toString());
            LOGGER.i("Running detection on face " + currTimestamp);
            //results = detector.recognizeImage(croppedBitmap);

            final RectF boundingBox = new RectF(face.getBoundingBox());

            //final boolean goodConfidence = result.getConfidence() >= minimumConfidence;
            final boolean goodConfidence = true; //face.get;
            if (boundingBox != null && goodConfidence) {

                // maps crop coordinates to original
                cropToFrameTransform.mapRect(boundingBox);

                // maps original coordinates to portrait coordinates
                RectF faceBB = new RectF(boundingBox);
                transform.mapRect(faceBB);

                // translates portrait to origin and scales to fit input inference size
                //cv.drawRect(faceBB, paint);
                float sx = ((float) TF_OD_API_INPUT_SIZE) / faceBB.width();
                float sy = ((float) TF_OD_API_INPUT_SIZE) / faceBB.height();
                Matrix matrix = new Matrix();
                matrix.postTranslate(-faceBB.left, -faceBB.top);
                matrix.postScale(sx, sy);

                cvFace.drawBitmap(portraitBmp, matrix, null);

                //canvas.drawRect(faceBB, paint);

                String label = "";
                float confidence = -1f;
                Integer color = Color.BLUE;
                Object extra = null;
                Bitmap crop = null;

                if (add) {
                    crop = Bitmap.createBitmap(portraitBmp,
                            (int) faceBB.left,
                            (int) faceBB.top,
                            (int) faceBB.width(),
                            (int) faceBB.height());
                }

                final long startTime = SystemClock.uptimeMillis();
                final List<SimilarityClassifier.Recognition> resultsAux = faceRecognizer.recognizeImage(faceBmp, add);
                if (resultsAux.size() > 0) {
                    SimilarityClassifier.Recognition result = resultsAux.get(0);
                    extra = result.getExtra();
                    float conf = result.getDistance();
                    if (conf < 1.0f) {
                        confidence = conf;
                        label = result.getTitle();
                        if (result.getId().equals("0")) {
                            color = Color.GREEN;
                        } else {
                            color = Color.RED;
                        }
                    }

                }

                if (getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT) {

                    // camera is frontal so the image is flipped horizontally
                    // flips horizontally
                    Matrix flip = new Matrix();
                    if (sensorOrientation == 90 || sensorOrientation == 270) {
                        flip.postScale(1, -1, previewWidth / 2.0f, previewHeight / 2.0f);
                    } else {
                        flip.postScale(-1, 1, previewWidth / 2.0f, previewHeight / 2.0f);
                    }
                    //flip.postScale(1, -1, targetW / 2.0f, targetH / 2.0f);
                    flip.mapRect(boundingBox);

                }

                final SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                        "0", label, confidence, boundingBox);

                result.setColor(color);
                result.setLocation(boundingBox);
                result.setExtra(extra);
                result.setCrop(crop);
                mappedRecognitions.add(result);
            }
        }

        //    if (saved) {
//      lastSaved = System.currentTimeMillis();
//    }

        updateResultsAfterFaceRecognition(currTimestamp, mappedRecognitions);


    }


    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    enum DetectorMode {
        TF_OD_FACE_REG_API,
    }


}
