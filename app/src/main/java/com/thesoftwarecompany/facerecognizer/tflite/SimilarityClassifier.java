/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.thesoftwarecompany.facerecognizer.tflite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 * Generic interface for interacting with different recognition engines.
 */
public interface SimilarityClassifier {

    String register(String name, Recognition recognition, Context context);

    List<Recognition> recognizeImage(Bitmap bitmap, boolean getExtra);

    void enableStatLogging(final boolean debug);

    String getStatString();

    void close();

    void setNumThreads(int num_threads);

    void setUseNNAPI(boolean isChecked);

    /**
     * An immutable result returned by a Classifier describing what was recognized.
     */
    public class Recognition {
        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private final String id;

        /**
         * Display name for the recognition.
         */
        private String title;

        /**
         * A sortable score for how good the recognition is relative to others. Lower should be better.
         */
        private final Float distance;
        float[][] embeddings;
        private Object extra;
        /**
         * Optional location within the source image for the location of the recognized object.
         */
        private RectF location;
        private Integer color;
        private Bitmap crop;


        public Recognition(
                final String id, final String title, final Float distance, final RectF location) {
            this.id = id;
            this.title = title;
            this.distance = distance;
            this.location = location;
            this.color = null;
            this.extra = null;
            this.crop = null;
        }

        public float[][] getExtraEmbed() {
            return embeddings;
        }

        public void setExtraEmbed(float[][] embeddings) {
            this.embeddings = embeddings;
        }

        public Object getExtra() {
            return this.extra;
        }

        public void setExtra(Object extra) {
            this.extra = extra;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Float getDistance() {
            return distance;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        @NonNull
        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += id + "#";
            }

            if (title != null) {
                resultString += title + "#";
            }

            if (distance != null) {
                // resultString += String.format("(%.1f%%) ", distance * 100.0f);
                //  resultString += (distance * 100.0f) + "_";
                resultString += distance + "#";
            }

            if (location != null) {
                resultString += location + "#";
            }

            if (extra != null) {
                resultString += Arrays.deepToString((float[][]) extra);
            }

//            if (embeddings != null) {
//                resultString += Arrays.deepToString(embeddings);
//            }

            return resultString.trim();
        }

    /*@Override
    public String toString() {
      String resultString = "";
      if (id != null) {
        resultString += "[" + id + "] ";
      }

      if (title != null) {
        resultString += title + " ";
      }

      if (distance != null) {
        resultString += String.format("(%.1f%%) ", distance * 100.0f);
      }

      if (location != null) {
        resultString += location + " ";
      }

      return resultString.trim();
    }*/

        public Integer getColor() {
            return this.color;
        }

        public void setColor(Integer color) {
            this.color = color;
        }

        public Bitmap getCrop() {
            return this.crop;
        }

        public void setCrop(Bitmap crop) {
            this.crop = crop;
        }
    }
}
