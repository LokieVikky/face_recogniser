package com.thesoftwarecompany.facerecognizer.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class LogEntity {

    @PrimaryKey(autoGenerate = true)
    public int ID;
    @ColumnInfo(name = "EmpID")
    public String empID;
    @ColumnInfo(name = "Date")
    public Date date;


    public LogEntity(Date date, String empID) {
        this.date = date;
        this.empID = empID;
    }

    public String getEmpID() {
        return empID;
    }

    public LogEntity() {

    }

    public void setEmpID(String empID) {
        this.empID = empID;
    }


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


}
