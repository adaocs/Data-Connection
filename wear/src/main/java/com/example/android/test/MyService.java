package com.example.android.test;

/**
 * Created by MrReRe on 1/13/17.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.support.wearable.activity.WearableActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


//Background service
public class MyService extends Service implements SensorEventListener, GoogleApiClient.ConnectionCallbacks{

    // private ArrayList<Accelerometer> acceleromenterList;
    //private ArrayList<Gyroscope> gyroscopeList;

    private Sensor linearAcc;
    private Sensor gyroscope;
    private GoogleApiClient mGoogleApiClient;
    private boolean connected;
    private Handler handler;

    private SensorManager SM;

    PowerManager.WakeLock partial;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Create our Sensor Manager
        SM = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Accelerometer Sensor
        gyroscope = SM.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        linearAcc = SM.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        // Register sensor listener
        SM.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(this, linearAcc, SensorManager.SENSOR_DELAY_NORMAL);


        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        partial = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "cpuon");
        partial.acquire();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
        return START_STICKY;


    }

    @Override
    public void onDestroy() {
        partial.release();
        super.onDestroy();
    }


    // Method from implementation of SensorListener
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        //  Getting sensors values and adding to respective array list
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            try{

                // Make an Accelerometer object and add to the array
                long timeStamp = System.currentTimeMillis();

                sendToPhone(timeStamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                sendValuesAccelerometer(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);

            } catch (IllegalStateException e){

            }


        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            try {

                sendValuesGyro(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);

            } catch (IllegalStateException e) {

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        connected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    public void sendToPhone(long time, float x, float y, float z){
        if(connected == true){
            PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/Value");
            dataMapRequest.getDataMap().putLong("Time", time);
            dataMapRequest.getDataMap().putFloat("X", x);
            dataMapRequest.getDataMap().putFloat("Y", y);
            dataMapRequest.getDataMap().putFloat("Z", z);

            PutDataRequest putDataRequest = dataMapRequest.asPutDataRequest();
            putDataRequest.setUrgent();
            Wearable.DataApi.putDataItem(mGoogleApiClient,putDataRequest).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                    if(dataItemResult.getStatus().isSuccess()){
                        Log.d("", "sent to phone successfully");
                    }
                    else {
                        Log.d("", "failed");
                    }
                }
            });
        }

    }





    public class MyBinder extends Binder {
        // Expose a function that sets the handler. These functions will be exported to the calling
        // entity.
        void setHandler(Handler tempHandler) {
            handler = tempHandler;
        }
    }
    // function that sends values of accelerometer to handler
    private void sendValuesAccelerometer(float x, float y, float z) {
        if (handler == null) {
            return;
        }

        Message message = Message.obtain();
        Bundle bundle = new Bundle(3);   // Create a bundle with capacity 3 to store x, y, z values
        bundle.putFloat("X", x);
        bundle.putFloat("Y", y);
        bundle.putFloat("Z", z);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    // function that sends values of gyro to handler
    private void sendValuesGyro(float gx, float gy, float gz) {
        if (handler == null) {
            return;
        }
        Message message = Message.obtain();
        Bundle bundle = new Bundle(3);
        bundle.putFloat("GX", gx);
        bundle.putFloat("GY", gy);
        bundle.putFloat("GZ", gz);
        message.setData(bundle);
        handler.sendMessage(message);
    }
}
