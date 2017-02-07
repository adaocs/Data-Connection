package com.example.android.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;

/**
 * Created by MrReRe on 2/3/17.
 */

public class PhoneService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks{

    private GoogleApiClient googleApiClient;
    private ArrayList<Float> X,Y,Z;

    @Override
            public void onCreate(){
            super.onCreate();
             googleApiClient=
                     new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(this).build();
            googleApiClient.connect();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents){
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
