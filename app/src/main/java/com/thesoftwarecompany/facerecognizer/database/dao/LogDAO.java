package com.thesoftwarecompany.facerecognizer.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.thesoftwarecompany.facerecognizer.database.entities.LogEntity;

import java.util.List;

@Dao
public interface LogDAO {

    @Query("select * from logentity order by 1 desc")
    List<LogEntity> getAll();

    @Insert
    void insertAll(LogEntity... logs);

    @Query("delete from logentity")
    void delete();


}
