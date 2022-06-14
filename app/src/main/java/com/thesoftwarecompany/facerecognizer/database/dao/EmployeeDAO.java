package com.thesoftwarecompany.facerecognizer.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.thesoftwarecompany.facerecognizer.database.entities.EmployeeEntity;

import java.util.List;

@Dao
public interface EmployeeDAO {

    @Query("select * from employeeentity")
    List<EmployeeEntity> getAllEmployees();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEmployees(EmployeeEntity... employeeEntities);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateEmployee(EmployeeEntity employeeEntity);

    @Query("delete from employeeentity where EmpID IN  (:uiDs)")
    void deleteEmployees(String[] uiDs);

    @Query("select count(*) from employeeentity")
    int getTotalUsers();

    @Query("select * from employeeentity where EmpID = :UID")
    EmployeeEntity getUser(String UID);

}
