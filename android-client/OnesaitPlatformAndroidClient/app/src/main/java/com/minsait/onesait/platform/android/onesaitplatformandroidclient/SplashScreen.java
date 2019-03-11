package com.minsait.onesait.platform.android.onesaitplatformandroidclient;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbriceno on 10/03/2015.
 */
public class SplashScreen extends Activity {
    private final int SPLASH_DISPLAY_DURATION = 2000;
    boolean granted = false;

    String[] permissions= new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        PreferenceManager.setDefaultValues(this, R.xml.preferences,false);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(requestPermissions()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        createAndShowAlertDialog();
                    }

                }, SPLASH_DISPLAY_DURATION);
            }
        }
        else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    createAndShowAlertDialog();
                }

            }, SPLASH_DISPLAY_DURATION);
        }




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            createAndShowAlertDialog();
                        }

                    }, SPLASH_DISPLAY_DURATION);

                } else {
                    finish();
                }
                return;
            }
        }

    }


    private  boolean requestPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),1);
            return false;
        }
        return true;
    }

    private void createAndShowAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_title_loc);
        builder.setMessage(R.string.alert_title_msg);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent firstIntent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(firstIntent);
                finish();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
