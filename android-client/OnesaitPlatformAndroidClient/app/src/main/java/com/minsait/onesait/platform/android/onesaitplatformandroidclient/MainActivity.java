/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.android.onesaitplatformandroidclient;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.skyfishjy.library.RippleBackground;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

//import com.skyfishjy.library.RippleBackground;

public class MainActivity extends AppCompatActivity {

    String TAG = "S4C MainActivity";

    private final int LOCATION_PERMIT = 1;
    private final int BLUETOOTH_PERMIT = 2;
    private final int BLUETOOTH_ADMIN_PERMIT = 3;
    private final int INTERNET_PERMIT = 4;
    private final int READ_PHONE_STATE_PERMIT = 5;
    private final int WRITE_EXTERNAL_STORAGE_PERMIT = 6;

    /* Layout views */
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView mAccXTextView;
    protected TextView mAccYTextView;
    protected TextView mAccZTextView;
    protected TextView mFramesGenView;
    protected TextView mAccuracyView;
    protected ImageView mBeaconImageView;

    private static final int REQUEST_ENABLE_BT = 1;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    String actualLogFile;
    int framesGen = 0;
    int framesSent = 0;

    /* Preferences related objects */
    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;
    private String pref_plate_id;
    private String pref_loc_acc_id;
    private String pref_email;

    private int maxAccuracy = 200;

    boolean playButtonState =false;

    TelephonyManager mngr;
    String IMEI = "";

    String mLatitude = "";
    String mLongitude= "";
    String mAccuracy = "";
    String mTemperature= "";
    String mAccelX = "";
    String mAccelY = "";
    String mAccelZ = "";
    Boolean mBeaconEvent = false;
    String mFrameGen="";

    RippleBackground rippleBackground;


    /***********************************************************************************/
    /**
     * Loads shared preference saved values into variables
     */
    private void loadPreferences(){
        pref_email = preferences.getString(getString(R.string.pref_email),"mbriceno@minsait.com");
        maxAccuracy = 200;
    }

    private void checkDeviceBleFeatures(){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,  R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**ACTIVITY LIFECYCLE**/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferencesEditor = preferences.edit();

        playButtonState = preferences.getBoolean("playButtonState",false);
        loadPreferences();
        checkDeviceBleFeatures();
        setContentView(R.layout.activity_main);

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = mngr.getDeviceId();

        /* Bind views */
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_value);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_value);
        mAccXTextView = (TextView)findViewById(R.id.accX_value) ;
        mAccYTextView = (TextView)findViewById(R.id.accY_value) ;
        mAccZTextView = (TextView)findViewById(R.id.accZ_value) ;
        mFramesGenView = (TextView)findViewById(R.id.frames_gen_value) ;
        mAccuracyView = (TextView) findViewById(R.id.accuracy_value);
        mBeaconImageView = (ImageView) findViewById(R.id.beacon_view);

        rippleBackground=(RippleBackground)findViewById(R.id.content);

        Log.i(TAG,"Former Play button state: "+preferences.getBoolean("playButtonState",false));
    }

    private void requestLocationEnabled(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);
        GoogleApiClient mGoogleApiClient= new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(
                                    MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == RESULT_OK){

            }
            else{
                stopTrackingService();
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        Log.w(TAG,"onStart");
        Log.i(TAG,"Is service running?: "+isServiceRunning());
        /* Checks whether the device has BLE capabilities, and enables them if that is the case */
        checkDeviceBleFeatures();


        if(playButtonState){
            requestLocationEnabled();
            if(!rippleBackground.isRippleAnimationRunning()){
                rippleBackground.startRippleAnimation();
            }
            if(!isServiceRunning()){
                launchTrackingService();
            }
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.w(TAG,"onResume");
        super.onResume();
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(TrackingService.ACTION_LOC);
        filter.addAction(TrackingService.ACTION_SENSOR);
        filter.addAction(TrackingService.ACTION_FRAME_GEN);
        filter.addAction(TrackingService.ACTION_FRAME_SEND);
        LocalBroadcastManager.getInstance(this).registerReceiver(testReceiver, filter);
        loadPreferences();
    }

    @Override
    protected void onPause() {
        Log.w(TAG,"onPause");
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(testReceiver);
    }

    @Override
    protected void onStop() {
        Log.w(TAG,"onStop");
        actualLogFile = null;
        framesGen = 0;
        framesSent = 0;
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent mSettingsIntent =  new Intent(getApplicationContext(),SettingsActivity.class);
            startActivity(mSettingsIntent);
            return true;
        }
        else if (id == R.id.action_play) {
            if(!playButtonState){

                mBeaconImageView.setVisibility(View.INVISIBLE);
                if(rippleBackground.isRippleAnimationRunning()){
                    rippleBackground.stopRippleAnimation();
                }
                rippleBackground.startRippleAnimation();

                //invalidateOptionsMenu();
                requestLocationEnabled();
                item.setTitle(R.string.stop_menu);
                playButtonState = true;
                preferencesEditor.putBoolean("playButtonState", true);
                preferencesEditor.commit();

                mFramesGenView.setText(Integer.toString(framesGen));

                if(!isServiceRunning()){
                    launchTrackingService();
                }

            }
            else{
                playButtonState = false;
                if(rippleBackground.isRippleAnimationRunning()){
                    rippleBackground.stopRippleAnimation();
                }
                //invalidateOptionsMenu();
                item.setTitle(R.string.play_menu);
                preferencesEditor.putBoolean("playButtonState", false);
                preferencesEditor.commit();

                actualLogFile = null;
                framesGen = 0;
                framesSent = 0;

                stopTrackingService();
                mBeaconImageView.setVisibility(View.INVISIBLE);
            }
            return true;
        }

        else if( id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(menu.findItem(R.id.action_play).getItemId() == R.id.action_play){
            if(!playButtonState){
                menu.findItem(R.id.action_play).setTitle(R.string.play_menu);
            }
            else{
                menu.findItem(R.id.action_play).setTitle(R.string.stop_menu);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if(playButtonState){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_title);
            builder.setMessage(R.string.dialog_message);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case BLUETOOTH_PERMIT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                return;
            }
            case BLUETOOTH_ADMIN_PERMIT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                return;
            }
            case INTERNET_PERMIT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                return;
            }
            case READ_PHONE_STATE_PERMIT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                return;
            }
            case WRITE_EXTERNAL_STORAGE_PERMIT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                return;
            }
            case LOCATION_PERMIT: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                return;
            }
        }
    }
    /**ACTIVITY LIFECYCLE**/


    /* Checks whether TrackingService service is running or not */
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.minsait.onesait.platform.android.onesaitplatformandroidclient.TrackingService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateAccuracy(){
        if(!mAccuracy.equals("")){
            if(Float.parseFloat(mAccuracy)<200){
                return true;
            }
        }
        return false;
    }

    private boolean evaluateConnectivity(){
        if(!mAccuracy.equals("")){
            if(Integer.parseInt(mFrameGen)>0){
                return true;
            }
        }
        return false;
    }

    private void updateUI() {
        mLatitudeTextView.setText(mLatitude);
        mLongitudeTextView.setText(mLongitude);
        mAccuracyView.setText(mAccuracy);
        if(!mAccuracy.equals("")){
            if(Float.parseFloat(mAccuracy)<maxAccuracy){
                mAccuracyView.setTextColor(Color.GREEN);
            }
            else{
                mAccuracyView.setTextColor(Color.RED);
            }
        }

        mAccXTextView.setText(mAccelX);
        mAccYTextView.setText(mAccelY);
        mAccZTextView.setText(mAccelZ);
        mFramesGenView.setText(mFrameGen);

        if(mBeaconEvent){
            mBeaconImageView.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(),"BEACON FOUND! :)", Toast.LENGTH_LONG).show();
            playButtonState = false;
            invalidateOptionsMenu();
            preferencesEditor.putBoolean("playButtonState", false);
            preferencesEditor.commit();
            if(rippleBackground.isRippleAnimationRunning()){
                rippleBackground.stopRippleAnimation();
            }

            actualLogFile = null;
            framesGen = 0;
            framesSent = 0;

            stopTrackingService();

        }
    }

    public void launchTrackingService() {
        Intent i = new Intent(this, TrackingService.class);
        startService(i);
    }

    public void stopTrackingService() {
        Intent i = new Intent(this, TrackingService.class);
        stopService(i);
    }


    /**
     * Broadcast receiver for TrackingService
     */
    private BroadcastReceiver testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(TrackingService.ACTION_LOC)){
                mLatitude = intent.getStringExtra("loc-update-lat");
                mLongitude = intent.getStringExtra("loc-update-lon");
                mAccuracy = intent.getStringExtra("loc-update-acc");
            }
            else if(intent.getAction().equals(TrackingService.ACTION_SENSOR)){
                mTemperature = intent.getStringExtra("sens-update-temp");
                mAccelX = intent.getStringExtra("sens-update-accX");
                mAccelY = intent.getStringExtra("sens-update-accY");
                mAccelZ = intent.getStringExtra("sens-update-accZ");
                mBeaconEvent = intent.getBooleanExtra("sens-update-beaconEvent",false);
            }
            else if(intent.getAction().equals(TrackingService.ACTION_FRAME_GEN)){
               mFrameGen = intent.getStringExtra("frame_generated");
            }
            updateUI();
        }
    };

}
