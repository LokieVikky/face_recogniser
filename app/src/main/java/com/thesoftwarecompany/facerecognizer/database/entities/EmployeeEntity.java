package com.thesoftwarecompany.facerecognizer.database.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class EmployeeEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "EmpID")
    public String empID;

    @ColumnInfo(name = "EmpName")
    public String empName;

    @ColumnInfo(name = "Department")
    public String department;

    @ColumnInfo(name = "FacePath")
    public String facePath;

    public EmployeeEntity(){

    }


    public EmployeeEntity(@NonNull String empID, String empName, String department) {
        this.empID = empID;
        this.empName = empName;
        this.department = department;
    }

    public EmployeeEntity(@NonNull String empID, String empName, String department, String facePath) {
        this.empID = empID;
        this.empName = empName;
        this.department = department;
        this.facePath = facePath;
    }

    public String getFacePath() {
        return facePath;
    }

    public void setFacePath(String facePath) {
        this.facePath = facePath;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public EmployeeEntity(String empID, String empName) {
        this.empID = empID;
        this.empName = empName;
    }

    public String getEmpID() {
        return empID;
    }

    public void setEmpID(String empID) {
        this.empID = empID;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }
}
