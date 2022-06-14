package com.thesoftwarecompany.facerecognizer;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Activity mActivity = this;
        ApplicationClass applicationClass = (ApplicationClass)getApplication();
        applicationClass.setActivity(mActivity);
        NavController navController = Navigation.findNavController(mActivity, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController);
    }
}