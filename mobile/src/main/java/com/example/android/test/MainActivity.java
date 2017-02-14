package com.example.android.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.concurrent.RunnableFuture;


public class MainActivity extends AppCompatActivity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Intent service;
    private GoogleApiClient googleApiClient;
    private ArrayList<Float>X, Y, Z;
    private ArrayList<Long> time;

    private final String MONITORING_START = "/start_monitoring";
    private final String MONITORING_STOP = "/stop_monitoring";

    private TextView txtDistance;

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

       Button buttStartMonitoring = (Button)findViewById(R.id.butt_startmonitoring);
       Button buttStopMonitoring = (Button)findViewById(R.id.butt_stopmonitoring);
       txtDistance = (TextView)findViewById(R.id.txt_distance);

       buttStartMonitoring.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               // Clear all previous data if any.
               X.clear();
               Y.clear();
               Z.clear();
               time.clear();

               sendStartMonitoring();
           }
       });

       buttStopMonitoring.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               sendStopMonitoring();

               // Create a new execution thread and process the data using our algorithm.
               new Thread(new Runnable() {
                   @Override
                   public void run() {
                       Algorithm algorithm = new Algorithm(Y, time, 0.8F, 1);
                       final ArrayList<Float> distance = algorithm.getDistance();
                       Log.d("Phone Activity", "Calculated Distance "+distance.get(distance.size()-1));

                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               txtDistance.setText(""+distance.get(distance.size()-1));
                           }
                       });
                   }
               }).start();
           }
       });
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
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();

                if(path.equals("/Value")){
                    X.add(dataMap.getFloat("X"));
                    Y.add(dataMap.getFloat("Y"));
                    Z.add(dataMap.getFloat("Z"));
                    time.add(dataMap.getLong("Time"));
                }
            }

        }
    }
}
