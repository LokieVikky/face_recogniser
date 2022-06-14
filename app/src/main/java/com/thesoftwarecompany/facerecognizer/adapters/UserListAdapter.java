package com.thesoftwarecompany.facerecognizer.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thesoftwarecompany.facerecognizer.R;
import com.thesoftwarecompany.facerecognizer.callbacks.EditClickListener;
import com.thesoftwarecompany.facerecognizer.callbacks.RegisterFaceListener;
import com.thesoftwarecompany.facerecognizer.database.entities.EmployeeEntity;

import java.util.ArrayList;
import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserItemViewHolder> {

    public List<EmployeeEntity> employees;
    public EditClickListener editClickListener;
    public RegisterFaceListener registerFaceListener;


    public UserListAdapter(EditClickListener editClickListener,RegisterFaceListener registerFaceListener) {
        employees = new ArrayList<>();
        this.editClickListener = editClickListener;
        this.registerFaceListener = registerFaceListener;
    }

    public void setData(List<EmployeeEntity> employees) {
        this.employees.clear();
        this.employees.addAll(employees);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserItemViewHolder holder, int position) {
        EmployeeEntity employeeEntity = employees.get(position);
        holder.txtEmployeeID.setText(employeeEntity.empID);
        holder.txtEmployeeName.setText(employeeEntity.empName);
        holder.txtEmployeeDesignation.setText(employeeEntity.department);
        if(employeeEntity.facePath!=null){
            if (employeeEntity.facePath.equals("")) {
                holder.txtFaceNotRegistered.setVisibility(View.VISIBLE);
            } else {
                holder.txtFaceNotRegistered.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }

    public class UserItemViewHolder extends RecyclerView.ViewHolder {

        TextView txtEmployeeID,txtEmployeeName, txtEmployeeDesignation, txtFaceNotRegistered;
        ImageView btnEdit;

        public UserItemViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEmployeeID = itemView.findViewById(R.id.txtEmpID);
            txtEmployeeName = itemView.findViewById(R.id.txtName);
            txtEmployeeDesignation = itemView.findViewById(R.id.txtxDesignation);
            txtFaceNotRegistered = itemView.findViewById(R.id.txtFaceNotRegistered);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnEdit.setOnClickListener(v -> editClickListener.OnEditClick(employees.get(getAdapterPosition()).empID));
            txtFaceNotRegistered.setOnClickListener(v -> registerFaceListener.OnClickHereToRegisterClick(employees.get(getAdapterPosition()).empID));
        }
    }
}
