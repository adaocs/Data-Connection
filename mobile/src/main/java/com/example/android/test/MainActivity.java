package com.example.android.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.data.WorkoutExercises;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import junit.framework.Test;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;
import java.util.Date;

import static android.R.attr.data;
import static android.R.attr.x;
import static android.R.attr.y;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/*
    Authors: Caleb Short, Shravan Aras, Anh Dao
    Purpose: This class encapsulates both a timer and a walking algorithm
 */

public class MainActivity extends AppCompatActivity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // This class holds all of the data from a single run. It should be used once the run is complete
    private class SingleRunData{

        private ArrayList<Float>X, Y, Z;
        private ArrayList<Long> time;

        private float totalTime;

        private float timerTime;

        public SingleRunData(ArrayList<Float>X, ArrayList<Float>Y, ArrayList<Float>Z, ArrayList<Long> time){
            this.X = X;
            this.Y = Y;
            this.Z = Z;
            this.time = time;

            totalTime = time.get(time.size()-1)-time.get(0);
            totalTime /= 1000;
        }

        public float getTotalTime(){
            return totalTime;
        }

        public void addTimerTime(float timerTime){      // Currently unused
            this.timerTime = timerTime;
        }
    }

    private GoogleApiClient googleApiClient;
    private ArrayList<Float>X, Y, Z;                // The current x, y and z data
    private ArrayList<Long> time;                   // The current time data from the algorithm
    private ArrayList<SingleRunData> allData;       // The data for each completed run in a list

    Timer timer;
    int timerTime;                                  // The time from the timer
    ArrayList<Float> totalTimes;                    // The time for each completed run from the timer

    private boolean monitoringFlag;                 // Tells whether we are monitoring now or not

    private final String MONITORING_START = "/start_monitoring";
    private final String MONITORING_STOP = "/stop_monitoring";

    private TextView txtTime;                       // The time as taken from the algorithm
    private TextView txtDistance;                   // The distance as taken from the algorithm
    private TextView txt_lap_num;                   // The lap number dependent on the size of totalTimes
    private TextView txtAcceleration;               // The acceleration as taken from the algorithm
    private TextView txtActualTime;                 // The time as taken from the timer

    private RTAlgorithm rtAlgorithm;                // An instance of the algorithm

    private final float THRESHOLD = 0.8F;           // The threshold for the algorithm

    public static final int REQUEST_CODE = 1;       // The request code sent to the Activity FinishPopup

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
       time = new ArrayList<>();
       allData = new ArrayList<>();
       totalTimes = new ArrayList<>();

       monitoringFlag = false;

       Button buttStartMonitoring = (Button)findViewById(R.id.butt_startmonitoring);
       Button buttStopMonitoring = (Button)findViewById(R.id.butt_stopmonitoring);
       Button buttFinish = (Button) findViewById(R.id.finish);
       txtTime = (TextView)findViewById(R.id.time);
       txtDistance = (TextView)findViewById(R.id.txt_distance);
       txt_lap_num = (TextView)findViewById(R.id.lap_num);
       txtAcceleration = (TextView)findViewById(R.id.txt_acc);
       txtActualTime = (TextView)findViewById(R.id.accurate_time);

       // Init the processing algorithm.
       rtAlgorithm = new RTAlgorithm(1F, 5);


       buttStartMonitoring.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View view) {
               if(!monitoringFlag)
                   start();
               else
                   Toast.makeText(getApplicationContext(), "You are already monitoring!", Toast.LENGTH_SHORT).show();
           }
       });

       buttStopMonitoring.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View view) {
               if(monitoringFlag)
                   stop();
               else
                   Toast.makeText(getApplicationContext(), "You are not monitoring anything!", Toast.LENGTH_SHORT).show();
           }
       });

       buttFinish.setOnClickListener(new OnClickListener(){

           @Override
           public void onClick(View view) {
               if(monitoringFlag)
                   Toast.makeText(getApplicationContext(), "You can't finish mid-run!", Toast.LENGTH_SHORT).show();
               else if(totalTimes.size() == 0)
                   Toast.makeText(getApplicationContext(), "You must have at least 1 run to finish", Toast.LENGTH_SHORT).show();
               else
                   finishRun();
           }
       });

   }

    @Override
    public void onResume() {
        super.onResume();
        googleApiClient.connect();
        //stop();
    }

    @Override
    public void onPause() {
        super.onPause();
        stop();
        Wearable.DataApi.removeListener(googleApiClient, this);
        googleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
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

    // Method which sends a start monitoring message to the watch.
    private void sendStartMonitoring() {
        sendMessage(MONITORING_START);
    }

    // Method which sends a stop monitoring message to the watch.
    private void sendStopMonitoring() {
        sendMessage(MONITORING_STOP);
    }

    private void sendMessage(final String path) {
        // Get the connected nodes to this phone.
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

                for (Node node : nodesResult.getNodes()) {
                    // Send the message to all the connected nodes.
                    Wearable.MessageApi.sendMessage(googleApiClient,
                            node.getId(), path, "dummy message".getBytes())
                            .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                    if (sendMessageResult.getStatus().isSuccess()) {
                                        // The message was sent successfully. This does not mean the
                                        // monitoring started at the watch end. We need to implement
                                        // some sort of ACk policy to be able to determine when the monitoring has started. But for now this
                                        // is fine.

                                        // Update the UI thread accordingly to tell the user that the monitoring has started.
                                        if(path.equals(MONITORING_START)){
                                            Log.i("sendMessage", "Monitoring_start message sent successfully");
                                        }
                                        else if(path.equals(MONITORING_STOP)){
                                            Log.i("sendMessage", "Monitoring_stop message sent successfully");
                                        }
                                    }
                                    else{
                                        if(path.equals(MONITORING_START)){
                                            Log.i("sendMessage", "Monitoring_start message did not go through");
                                        }
                                        else if(path.equals(MONITORING_STOP)){
                                            Log.i("sendMessage", "Monitoring_stop message did not go through");
                                        }
                                    }
                                }
                            });
                }
            }
        }).start();

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for(DataEvent dataEvent: dataEvents){
            if(dataEvent.getType() == DataEvent.TYPE_CHANGED){
                final DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();

                if(!monitoringFlag)
                    return;

                if(path.equals("/Value")){
                    final float x = dataMap.getFloat("X");
                    final float y = dataMap.getFloat("Y");
                    final float z = dataMap.getFloat("Z");
                    final long t = dataMap.getLong("Time");

                    X.add(x);
                    Y.add(y);
                    Z.add(z);
                    time.add(t);



                    rtAlgorithm.addAcceleration(t, y);
                    rtAlgorithm.calculateVelocity();
                    rtAlgorithm.calculateDistance();
                    final float distance = rtAlgorithm.getDistance();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            float timeFormattedCorrectly = calculateCurrentTime();
                            txtTime.setText("Time: "+timeFormattedCorrectly);
                            txtAcceleration.setText("Acceleration: "+y);
                            txtDistance.setText("Distance: "+distance);
                            //addDataToFile(t, x, y, z);

                            Timer timer = new Timer();
                            timer.schedule(new MyTimerTask(t), 300);    // Run this after 0.3 seconds
                        }
                    });
                }
            }

        }
    }

    // A timer meant to determine when data is no longer being sent from the watch, so the data can now be saved
    private class MyTimerTask extends TimerTask{
        private long prevTime;

        public MyTimerTask(long prevTime){
            this.prevTime = prevTime;
        }

        @Override
        public void run() {
            if(time.size() != 0 && this.prevTime == time.get(time.size()-1)) {
                addDataFromLastRun();
            }
        }
    }

    // When the finish button is pressed, finish with this function (jump to activity FinishPopup)
    private void finishRun(){

        float total = 0;
        for(float aTime: totalTimes) {
            total += aTime;
            Log.i("aTime", "" + aTime);
        }
        float average = total/totalTimes.size();

        Intent myIntent = new Intent(MainActivity.this, FinishPopup.class);
        myIntent.putExtra("averageTime", average);

        startActivityForResult(myIntent, REQUEST_CODE);
    }

    // What to do when the activity FinishPopup has completed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE){
            if(resultCode == RESULT_OK){
                saveAllData();
                restart();
            }
            else if(resultCode == RESULT_CANCELED){
                saveAllData();
                finish();
            }
            else
                Log.i("onActivityResult", "The result code was not RESULT_OK or RESULT_CANCELED.");
        }
        Log.i("onActivityResult", "The request code was not REQUEST_CODE");
    }

    // A function used by OnDataChanged to calculate the current time
    private float calculateCurrentTime(){
        float timeFormattedCorrectly = time.get(time.size()-1)-time.get(0);
        timeFormattedCorrectly /= 1000;
        return timeFormattedCorrectly;
    }

    // Start everything when the start button is pressed
    private void start(){
        stop();         // stop everything before starting everything over

        // Clear all previous data if any.
        X.clear();
        Y.clear();
        Z.clear();
        time.clear();

        rtAlgorithm = new RTAlgorithm(1F, 5);
        //////
        txt_lap_num.setText("Lap #: " + (totalTimes.size()+1));
        //////
        monitoringFlag = true;
        sendStartMonitoring();
        startTimer();
    }

    // Start the timer
    private void startTimer(){
        timerTime = 0;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        float actualTime = ((float) timerTime) / 100;
                        txtActualTime.setText("Accurate Time: "+ actualTime);
                        timerTime++;
                    }
                });
            }
        }, 10, 10);     // Runs every 10 milliseconds
    }

    // Stop the timer
    private void stopTimer(){
        if(timer != null) {
            timer.cancel();
            timer = null;
            float actualTime = ((float) timerTime) / 100;
            totalTimes.add(actualTime);
            Log.i("actual Time", "" + actualTime);
        }
    }

    // Stop everything when the stop button is pressed
    private void stop(){
        monitoringFlag = false;
        sendStopMonitoring();
        stopTimer();
    }

    // A function called by OnDataChanged to updat the local data storage
    private void addDataFromLastRun(){
        allData.add(new SingleRunData(X, Y, Z, time));
    }

    // The function that will be called to send the data to the database
    private void saveAllData(){

    }

    // Restart everything and make the app start from the beginning
    private void restart(){
        X = new ArrayList<>();
        Y = new ArrayList<>();
        Z = new ArrayList<>();
        time = new ArrayList<>();
        allData = new ArrayList<>();
        totalTimes = new ArrayList<>();

        monitoringFlag = false;

        txtTime.setText("Time");
        txtAcceleration.setText("Acceleration");
        txtDistance.setText("Distance");
        txt_lap_num.setText("Lap #");
        txtActualTime.setText("Accurate Time");
    }

}
