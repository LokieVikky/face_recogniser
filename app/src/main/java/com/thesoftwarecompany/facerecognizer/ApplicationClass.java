package com.thesoftwarecompany.facerecognizer;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.navigation.Navigation;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ApplicationClass extends Application {
    private static final String TAG = "ApplicationClass";
    public ApplicationClass instance;
    Activity mActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void navigateTo(int fragmentIDinNavigation) {
        Navigation.findNavController(mActivity, R.id.nav_host_fragment).navigate(fragmentIDinNavigation);
    }

    public void navigateTo(int fragmentIDinNavigation, Bundle b) {
        Navigation.findNavController(mActivity, R.id.nav_host_fragment).navigate(fragmentIDinNavigation, b);
    }

    public void goBack() {
        Navigation.findNavController(mActivity, R.id.nav_host_fragment).popBackStack();
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        } catch (UnknownHostException e) {
            // Log error
        }
        return false;
    }

}
