package com.thesoftwarecompany.facerecognizer;

import static com.thesoftwarecompany.facerecognizer.UserListFragment.EMP_ID;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.thesoftwarecompany.facerecognizer.database.FaceRecognizerDB;
import com.thesoftwarecompany.facerecognizer.database.entities.EmployeeEntity;

public class AddModifyUserFragment extends Fragment {
    TextInputEditText edtEmpID, edtName, edtDesignation, edtFace;
    TextInputLayout tilFace;
    ExtendedFloatingActionButton fabSave;
    ApplicationClass applicationClass;
    String empID = "";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.frament_add_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            empID = args.getString(EMP_ID, "");
        }
        applicationClass = (ApplicationClass) getActivity().getApplication();
        edtEmpID = view.findViewById(R.id.edtEmpID);
        edtName = view.findViewById(R.id.edtName);
        edtDesignation = view.findViewById(R.id.edtDesignation);
        edtFace = view.findViewById(R.id.edtFace);
        tilFace = view.findViewById(R.id.tilFace);
        fabSave = view.findViewById(R.id.fabSave);
        tilFace.setEndIconOnClickListener(v -> {
            if (empID != null) {
                if (!empID.equals("")) {
                    Bundle b = new Bundle();
                    b.putString(EMP_ID, empID);
                    applicationClass.navigateTo(R.id.action_addUser_to_faceRegisterFragment,b);
                }
            }
        });
        fabSave.setOnClickListener(v -> {
            if (edtEmpID.getText().toString().trim().equals("")) {
                edtEmpID.setError("Enter Employee ID");
                return;
            }
            if (edtName.getText().toString().trim().equals("")) {
                edtName.setError("Enter Name");
                return;
            }
            if (edtDesignation.getText().toString().trim().equals("")) {
                edtDesignation.setError("Enter Designation");
                return;
            }
            saveEmployee(edtEmpID.getText().toString(), edtName.getText().toString(), edtDesignation.getText().toString());
            Toast.makeText(applicationClass, "User Added", Toast.LENGTH_SHORT).show();
            applicationClass.goBack();
        });
        fillEmployeeDetails();
    }

    void saveEmployee(String ID, String name, String designation) {
        EmployeeEntity employeeEntity = new EmployeeEntity(ID.trim(), name.trim(), designation.trim());
        FaceRecognizerDB db = FaceRecognizerDB.getDatabase(applicationClass);
        db.employeeDAO().insertEmployees(employeeEntity);
    }

    void registerFace(String empID) {
        Bundle b = new Bundle();
        b.putString(EMP_ID, empID);
        applicationClass.navigateTo(R.id.action_addUser_to_faceRegisterFragment);
    }

    void fillEmployeeDetails() {
        if (!empID.equals("")) {
            tilFace.setVisibility(View.VISIBLE);
            edtEmpID.setEnabled(false);
            EmployeeEntity employeeEntity = FaceRecognizerDB.getDatabase(applicationClass).employeeDAO().getUser(empID);
            if (employeeEntity != null) {
                edtEmpID.setText(employeeEntity.getEmpID());
                edtName.setText(employeeEntity.getEmpName());
                edtDesignation.setText(employeeEntity.getDepartment());
                if (employeeEntity.getFacePath() != null) {
                    if (!employeeEntity.getFacePath().equals("")) {
                        edtFace.setText(R.string.face_registered);
                    } else {
                        edtFace.setText(R.string.face_not_registred);
                    }
                }
            }
        }
    }

}
