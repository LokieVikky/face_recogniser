package com.thesoftwarecompany.facerecognizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thesoftwarecompany.facerecognizer.adapters.LogAdapter;
import com.thesoftwarecompany.facerecognizer.database.FaceRecognizerDB;
import com.thesoftwarecompany.facerecognizer.database.entities.LogEntity;

import java.util.List;

public class LogsFragment extends Fragment {

    RecyclerView rvLogs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.frament_logs,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvLogs = view.findViewById(R.id.rvLogs);
        rvLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        FaceRecognizerDB db = FaceRecognizerDB.getDatabase(getContext());
        List<LogEntity> logs = db.logDAO().getAll();
        LogAdapter logAdapter = new LogAdapter();
        rvLogs.setAdapter(logAdapter);
        logAdapter.setData(logs);
    }
}
