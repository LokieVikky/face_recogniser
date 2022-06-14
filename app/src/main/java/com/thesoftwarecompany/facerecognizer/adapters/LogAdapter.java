package com.thesoftwarecompany.facerecognizer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thesoftwarecompany.facerecognizer.R;
import com.thesoftwarecompany.facerecognizer.database.entities.LogEntity;

import java.util.ArrayList;
import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogItemViewHolder> {

    public List<LogEntity> logs;


    public LogAdapter() {
        logs = new ArrayList<>();
    }

    public void setData(List<LogEntity> logs) {
        this.logs.clear();
        this.logs.addAll(logs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LogItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LogItemViewHolder holder, int position) {
        LogEntity logEntity = logs.get(position);
        holder.txtDateTime.setText(logEntity.date.toString());
        holder.txtEmployeeDetails.setText(logEntity.empID);
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public class LogItemViewHolder extends RecyclerView.ViewHolder {

        TextView txtEmployeeDetails,txtDateTime;

        public LogItemViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDateTime = itemView.findViewById(R.id.logTime);
            txtEmployeeDetails = itemView.findViewById(R.id.txtEmployeeDetails);
        }
    }
}
