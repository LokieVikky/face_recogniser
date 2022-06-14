package com.thesoftwarecompany.facerecognizer.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.thesoftwarecompany.facerecognizer.database.dao.EmployeeDAO;
import com.thesoftwarecompany.facerecognizer.database.dao.LogDAO;
import com.thesoftwarecompany.facerecognizer.database.entities.EmployeeEntity;
import com.thesoftwarecompany.facerecognizer.database.entities.LogEntity;

@Database(entities = {LogEntity.class, EmployeeEntity.class}, version = 4)
@TypeConverters(Converters.class)
public abstract class FaceRecognizerDB extends RoomDatabase {
    public static volatile FaceRecognizerDB INSTANCE;

    public static FaceRecognizerDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FaceRecognizerDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            FaceRecognizerDB.class, "FaceRecognizerDB")
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }


    public abstract LogDAO logDAO();

    public abstract EmployeeDAO employeeDAO();
}
