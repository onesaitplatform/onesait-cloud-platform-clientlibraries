package com.minsait.onesait.platform.android.onesaitplatformandroidclient;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
//import com.indracompany.sofia2.android.s4candroidapi.protocols.implementations.RESTClient;
import com.minsait.onesait.platform.android.onesaitplatformandroidclientapi.RestClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;

/**
 * Created by mbriceno on 30/08/2016.
 */
public class TrackingService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SensorEventListener {
    private volatile HandlerThread mHandlerThread;
    private ServiceHandler mServiceHandler;

    private LocalBroadcastManager mLocalBroadcastManager;
    public static final String ACTION_LOC = "TrackingService-Loc";
    public static final String ACTION_SENSOR = "TrackingService-Sens";
    public static final String ACTION_STATUS = "TrackingService-Sens";
    public static final String ACTION_FRAME_SEND = "TrackingService-FSend";
    public static final String ACTION_FRAME_GEN = "TrackingService-FGen";
    public static final String TAG = "TrackingService";


    private final static String TOKEN = "cd94211df9634bcdbd3a4fcfed312dc0";
    private final static String ONTOLOGY_NAME = "androidIoTFrame";
    private final static String CLIENT_PLATFORM = "androidIoTClient";

    private final static String SERVICE_URL ="http://development.onesaitplatform.com/iot-broker/rest/";

    private final int OFFLINE_MODE = 0;
    private final int ONLINE_MODE = 1;
    private final int COMBINED_MODE = 2;

    String actualLogFile;
    private File dir;
    TelephonyManager mngr;
    String IMEI = "";
    String temperatureValue = "0";
    String luxometerValue = "0";
    String humidityValue = "0";
    String accXValue = "0";
    String accYValue = "0";
    String accZValue = "0";
    String gyrXValue = "0";
    String gyrYValue = "0";
    String gyrZValue = "0";
    private int maxAccuracy = 500;
    private int PORT = 1880;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    SharedPreferences preferences;

    private String pref_sensor_id;
    private String pref_email;
    private String pref_token;
    private String pref_thinkp;
    private String pref_ontology;

    private ArrayList<Location> locationsQ = new ArrayList<Location>();

    private LinkedList<String> bufferFrames = new LinkedList<String>();

    RestClient IoTBrokerProxyRest = null;

    int bufferFramesLength = 20;

    int framesSend =0;
    int framesGen =0;

    private final String beaconMacAddr = "01:17:C5:9B:BB:5F";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    protected Boolean mRequestingLocationUpdates = true;
    protected String mLastUpdateTime;
    private Location mBestLocation;

    Handler bleScanHandle = new Handler();

    private boolean SCAN_FLAG = true;
    private boolean FOUND_FLAG =false;
    private String mDeviceAddress;

    private BluetoothAdapter mBluetoothAdapter;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    float gravity[] = new float[3];
    long curTime;
    long lastUpdate;

    boolean fistSight = false;


    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            if(SCAN_FLAG){
                loadPreferences();
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                bleScanHandle.postDelayed(runnableCode, 20000);
                SCAN_FLAG = false;
                //bleConnHandle.post(connect);

            }
            else{
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                bleScanHandle.postDelayed(runnableCode, 5000);
                SCAN_FLAG = true;
            }
        }
    };


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    //Log.i(TAG,device.getAddress());
                            if(device.getAddress().equals(pref_sensor_id) && !FOUND_FLAG && (rssi > -90)){
                                Log.i(TAG,"Found!");
                                FOUND_FLAG = true;
                                pushBeaconData();
                            }
                }
            };


    private void pushBeaconData() {
        Intent mIntent = new Intent(ACTION_SENSOR);
        mIntent.putExtra("sens-update-beaconEvent", FOUND_FLAG);
        mLocalBroadcastManager.sendBroadcast(mIntent);
        treatFrame(composeFrame(mBestLocation,true),COMBINED_MODE);
    }

    private void pushSensorData() {
        Intent mIntent = new Intent(ACTION_SENSOR);
        mIntent.putExtra("sens-update-accX", accXValue);
        mIntent.putExtra("sens-update-accY", accYValue);
        mIntent.putExtra("sens-update-accZ", accZValue);
        mLocalBroadcastManager.sendBroadcast(mIntent);
        treatFrame(composeFrame(mBestLocation,false),COMBINED_MODE);
    }



    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        locationsQ.add(mCurrentLocation);

        Log.i(TAG,"Loc-updated: "+mCurrentLocation.getLatitude()+", "+mCurrentLocation.getLongitude());
        if(locationsQ.size() == 3){
            mBestLocation = getBestLoc();
            Intent intent = new Intent(ACTION_LOC);
            intent.putExtra("loc-update-lat", Double.toString(mBestLocation.getLatitude()));
            intent.putExtra("loc-update-lon", Double.toString(mBestLocation.getLongitude()));
            intent.putExtra("loc-update-acc", Float.toString(mBestLocation.getAccuracy()));
            mLocalBroadcastManager.sendBroadcast(intent);
            Log.i(TAG,"Loc-Best: "+mBestLocation.getLatitude()+", "+mBestLocation.getLongitude()+"->ACC: "+mBestLocation.getAccuracy());
            locationsQ.clear();
        }
    }

    private Location getBestLoc(){
        Location bestLoc = locationsQ.get(0);
        for(Location loc: locationsQ){
            Log.i(TAG,"Loc-Q: "+loc.getLatitude()+", "+loc.getLongitude()+"->ACC: "+loc.getAccuracy());
            if(loc.getAccuracy() < bestLoc.getAccuracy()){
                bestLoc = loc;
            }
        }
        return bestLoc;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");

        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        final float alpha = (float) 0.8;

        if(sensorEvent.sensor.getType()== Sensor.TYPE_ACCELEROMETER){
            gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];

            curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 4000) { //2000 subido por dashboard lento
                lastUpdate = curTime;

                accXValue = Float.toString(gravity[0]);
                accYValue = Float.toString(gravity[1]);
                accZValue = Float.toString(gravity[2]);
                Log.d(TAG,"pushSensorData");
                pushSensorData();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
        }
    }

    public void onCreate() {
        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadPreferences();

        try {
            IoTBrokerProxyRest =  new RestClient(SERVICE_URL,pref_token,pref_thinkp,"Device_01");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        /*mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = mngr.getDeviceId();*/
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        mHandlerThread = new HandlerThread("TrackingService.HandlerThread");
        mHandlerThread.start();

        mServiceHandler = new ServiceHandler(mHandlerThread.getLooper());
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        bleScanHandle.postDelayed(runnableCode, 1000);

        buildGoogleApiClient();
        mGoogleApiClient.connect();


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Toast.makeText(getApplicationContext(),"Accelerometer detected :)", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"No acceleromter detected :(", Toast.LENGTH_SHORT).show();
        }
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        curTime = System.currentTimeMillis();
        lastUpdate = curTime;

        Intent mIntent = new Intent(ACTION_STATUS);
        mIntent.putExtra("status-update", "ON");
        mLocalBroadcastManager.sendBroadcast(mIntent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        mSensorManager.unregisterListener(this);

        if(!SCAN_FLAG){
            Log.i(TAG,"Destroyed while blescanning, stop scan");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        bleScanHandle.removeCallbacks(runnableCode);
        mGoogleApiClient.disconnect();
        mHandlerThread.quit();
        Intent mIntent = new Intent(ACTION_STATUS);
        mIntent.putExtra("status-update", "OFF");
        mLocalBroadcastManager.sendBroadcast(mIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void treatFrame(String frame, int action){
        if(frame!="" && mRequestingLocationUpdates){
            switch (action){
                case OFFLINE_MODE:
                    if(actualLogFile!=null){
                        writeToFile(createFileFromTS(),frame);
                    }
                    break;
                case ONLINE_MODE:
                    insertFrameOnesaitPlatform(frame);
                    break;
                case COMBINED_MODE:
                    if(actualLogFile!=null){
                        writeToFile(createFileFromTS(),frame);
                    }
                    insertFrameOnesaitPlatform(frame);
                    break;
                default:
                    if(actualLogFile!=null){
                        writeToFile(createFileFromTS(),frame);
                    }
                    break;
            }
        }
    }

    private String composeFrame(Location location, boolean event){
        if(location!=null){
            return "{ \""+pref_ontology+"\": { \"geometry\": { \"coordinates\": [ "+
                    location.getLongitude()+", "+location.getLatitude()+" ], \"type\": \"Point\" }, \"" +
                    "email\": \""+pref_email+"\", \"" +
                    "event\": "+event+", \"" +
                    "accelX\": "+accXValue+", \"" +
                    "accelY\": "+accYValue+", \"" +
                    "accelZ\": "+accZValue+"}}";
        }
        else{
            return "{ \""+pref_ontology+"\": { \"geometry\": { \"coordinates\": [ "+
                    0.0+", "+0.0+" ], \"type\": \"Point\" }, \"" +
                    "email\": \""+pref_email+"\", \"" +
                    "event\": "+event+", \"" +
                    "accelX\": "+accXValue+", \"" +
                    "accelY\": "+accYValue+", \"" +
                    "accelZ\": "+accZValue+"}}";
        }

    }



    public File getStorageDir(String albumName){
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), albumName);
        if (!file.mkdirs()) {
            Log.e("TAG", "Directory not created");
        }
        Log.d("TAG", "Everything OK: "+file.getAbsolutePath());

        return file;
    }

    private File createFileFromTS(){
        File file = new File(dir,actualLogFile+".txt");
        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.toString()}, null, null);
        return file;
    }

    private void writeToFile(File file, String data){
        try {
            FileWriter writer = new FileWriter(file,true);
            writer.append(data+"\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertFrameOnesaitPlatform(final String frame){
        final Context context = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent mIntent = new Intent(ACTION_FRAME_GEN);
                    framesGen++;
                    mIntent.putExtra("frame_generated", String.valueOf(framesGen));
                    mLocalBroadcastManager.sendBroadcast(mIntent);

                    if(IoTBrokerProxyRest.join() == HttpURLConnection.HTTP_OK){
                        IoTBrokerProxyRest.insert(pref_ontology,frame);
                    }
                }
                catch( Exception ex ) {
                    Log.i(TAG, "sending ERROR\n" + ex.getMessage() );
                }
            }
        } ).start();
    }

    private void loadPreferences(){

        maxAccuracy = 200;

        pref_sensor_id = preferences.getString(getString(R.string.pref_sensor_id),
                beaconMacAddr);
        pref_email = preferences.getString(getString(R.string.pref_email),"mbriceno@minsait.com");
        pref_ontology = preferences.getString(getString(R.string.pref_ontology),ONTOLOGY_NAME);
        pref_thinkp = preferences.getString(getString(R.string.pref_thinkp),CLIENT_PLATFORM);
        pref_token = preferences.getString(getString(R.string.pref_token),TOKEN);

    }

}
