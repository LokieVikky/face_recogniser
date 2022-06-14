package com.thesoftwarecompany.facerecognizer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.thesoftwarecompany.facerecognizer.adapters.UserListAdapter;
import com.thesoftwarecompany.facerecognizer.database.FaceRecognizerDB;
import com.thesoftwarecompany.facerecognizer.database.entities.EmployeeEntity;

import java.util.List;

public class UserListFragment extends Fragment {

    ExtendedFloatingActionButton fabAddUser;
    RecyclerView rvUserList;
    ApplicationClass applicationClass;
    UserListAdapter userListAdapter;
    Context mContext;
    public static String EMP_ID = "EmployeeID";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getContext();
        applicationClass = (ApplicationClass) getActivity().getApplication();
        fabAddUser = view.findViewById(R.id.fabAddUser);
        rvUserList = view.findViewById(R.id.rvUserList);
        rvUserList.setLayoutManager(new LinearLayoutManager(mContext));
        fabAddUser.setOnClickListener(v -> applicationClass.navigateTo(R.id.action_userListFragment_to_addUser));
        FaceRecognizerDB db = FaceRecognizerDB.getDatabase(applicationClass);
        List<EmployeeEntity> employeeList = db.employeeDAO().getAllEmployees();
        userListAdapter = new UserListAdapter(employeeID -> {
            Bundle b = new Bundle();
            b.putString(EMP_ID, employeeID);
            applicationClass.navigateTo(R.id.action_userListFragment_to_addUser, b);
        }, employeeID -> {
            Bundle b = new Bundle();
            b.putString(EMP_ID, employeeID);
            applicationClass.navigateTo(R.id.action_userListFragment_to_faceRegisterFragment, b);
        });
        rvUserList.setAdapter(userListAdapter);
        userListAdapter.setData(employeeList);

    }
}
