package com.example.android.test;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;



public class MainActivity extends WearableActivity implements View.OnClickListener{

    private Button startButton;
    private Handler handler;
    private Intent service;
    private Button stopButton;
    private TextView xText, yText, zText;
    private TextView gXtext, gYtext, gZtext;
    MyService.MyBinder binder;


    public MainActivity() {
        // Create the handler to handle the messages.
        handler = new Handler() {
            public void handleMessage(Message msg) {
                // Handle messages from the services here. You can use these to update the UI etc.
                Bundle received = msg.getData();

                // Update values for accelerometer
                xText.setText("X: "+ received.getFloat("X"));
                yText.setText("Y: "+ received.getFloat("Y"));
                zText.setText("Z: "+ received.getFloat("Z"));


                // Update values for gyroscope
                gXtext.setText("GX: " + received.getFloat("GX"));
                gYtext.setText("GY: " + received.getFloat("GY"));
                gZtext.setText("GZ: " + received.getFloat("GZ"));

            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();


        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);

        gXtext = (TextView)findViewById(R.id.gXtext);
        gYtext = (TextView)findViewById(R.id.gYtext);
        gZtext = (TextView)findViewById(R.id.gZtext);

        startButton = (Button)findViewById(R.id.start);
        startButton.setOnClickListener(this);
        stopButton = (Button)findViewById(R.id.stop);
        stopButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.start:
                startMonitoring();
                break;

            case R.id.stop:
                stopMonitoring();
                break;

            default:
                break;
        }
    }



    // Start monitoring
    private void startMonitoring() {

        startButton.setEnabled(false);
        service = new Intent(this, MyService.class);
        startService(service);  // Start the service.
        bindService(service, serviceConnection, BIND_IMPORTANT);



    }

    // Stop monitoring
    private void stopMonitoring() {
        if(service != null) {
            // Unbind from the service and then stop it manually.
            // Unregister all listeners and write to file.
            if(binder != null)
                binder.unregister();
            unbindService(serviceConnection);
            stopService(service);
            binder = null;
            service = null;
            startButton.setEnabled(true);

        }
    }
    // Connection object for binding.
    public ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MyService.MyBinder)service;
            //Set the actual message handler if it is not null.
            if(handler != null) {
                binder.setHandler(handler);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // If you want to do any house keeping once the service has been unbounded.


        }
    };//


}
