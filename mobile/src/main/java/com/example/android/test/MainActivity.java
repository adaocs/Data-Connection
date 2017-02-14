package com.example.android.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.WorkoutExercises;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Intent service;
    private GoogleApiClient googleApiClient;
    private ArrayList<Float>X, Y, Z;

   @Override
    public void onCreate(Bundle bundle) {
       super.onCreate(bundle);
       setContentView(R.layout.activity_main);

       // Create a google api client instance.
       googleApiClient = new GoogleApiClient.Builder(this)
               .addApi(Wearable.API)
               .addConnectionCallbacks(this)
               .addOnConnectionFailedListener(this)
               .build();

       X = new ArrayList<>();
       Y = new ArrayList<>();
       Z = new ArrayList<>();
   }

    @Override
    public void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(googleApiClient, this);
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(googleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("Phone Activity", "Data received from watch.");
        for(DataEvent dataEvent: dataEvents){
            if(dataEvent.getType() == DataEvent.TYPE_CHANGED){
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();

                if(path.equals("/Value")){

                    X = new ArrayList<>();
                    X.add(dataMap.getFloat("X"));
                    Log.d("", Float.toString(dataMap.getFloat("X")));

                    Y = new ArrayList<>();
                    Y.add(dataMap.getFloat("Y"));
                    Log.d("", Float.toString(dataMap.getFloat("Y")));


                    Z = new ArrayList<>();
                    Z.add(dataMap.getFloat("Z"));
                    Log.d("", Float.toString(dataMap.getFloat("Z")));
                }
            }

        }
    }
}
