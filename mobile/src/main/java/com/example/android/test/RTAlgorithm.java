package com.example.android.test;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shravan Aras on 2/8/17.
 */

public class RTAlgorithm {
    ArrayList<Float> accX;
    ArrayList<Long> time;
    ArrayList<Float> velocity;
    ArrayList<Float> distance;

    //Global vales which handle the working of the algorithm.
    float dthreshold;
    int cthreshold;

    int cbuff = 0;

    private int window_size = 10;

    RTAlgorithm(float dthreshold, int cthreshold) {
        this.dthreshold = dthreshold;
        this.cthreshold = cthreshold;

        accX = new ArrayList<>();
        time = new ArrayList<>();
        velocity = new ArrayList<>();
        distance = new ArrayList<>();
        velocity.add(0F);
        distance.add(0F);
    }

    RTAlgorithm(ArrayList<Float> accX, ArrayList<Long> time, float dthreshold, int cthreshold) {
        this.accX = accX;
        this.time = time;
        this.dthreshold = dthreshold;
        this.cthreshold = cthreshold;

        velocity = new ArrayList<>(time.size());
        distance = new ArrayList<>(time.size());

        // Smooth the data using a moving average window filter.
        //this.accX = smoothData();
        Log.d("Algorithm", ""+this.accX.size());
        Log.d("Algorithm", ""+this.time.size());
    }

    /* Method to add to the acceleration buffer. */
    public void addAcceleration(long timestamp, float acceleration) {
        accX.add(acceleration);
        time.add(timestamp);
    }

    /* Public method which returns the velocity after integrating the acceleration. */
    public void calculateVelocity() {

        if(accX.size() >= 2) {
            int v2_index = accX.size()-1;
            int v1_index = accX.size()-2;

            float y1 = accX.get(v1_index);
            float y2 = accX.get(v2_index);
            float h = (y1 + y2) / 2;
            float w = (time.get(v2_index) - time.get(v1_index)) / 1000f;

            float delta_area = h * w;

            // Zero velocity correcion code.
            if(Math.abs(h) <= dthreshold) {
                cbuff ++;
                if(cbuff >= cthreshold) {
                    // Reset the velocity vector to 0.
                    velocity.add(0.0F);
                }
                else {
                    velocity.add(velocity.get(velocity.size()-1) + delta_area);
                }
            }
            else {
                cbuff = 0;
                velocity.add(velocity.get(velocity.size()-1) + delta_area);
            }
        }
    }

    /* Method which calculates the distance after integrating the velocity. */
    public void calculateDistance() {

        if(velocity.size() >= 2) {
            int v2_index = velocity.size()-1;
            int v1_index = velocity.size()-2;

            float y1 = Math.abs(velocity.get(v1_index));
            float y2 = Math.abs(velocity.get(v2_index));
            float h = (y1 + y2) / 2;
            float w = (time.get(v2_index) - time.get(v1_index)) / 1000f;

            float delta_area = h * w;

            Log.d("Distance", ""+delta_area);

            distance.add(distance.get(distance.size()-1) + delta_area);
        }
    }

    /* Method which return the distance the person has walked. */
    public ArrayList<Float> getDistanceList() {
        calculateVelocity();
        calculateDistance();

        return distance;
    }

    public float getDistance() {
        return distance.get(distance.size()-1);
    }

    // Method which smooths the data out.
    private ArrayList<Float> smoothData() {
        ArrayList<Float> filtered = new ArrayList<>(accX.size());
        for(int a=0; a < window_size && a < accX.size(); a++) {
            filtered.add(0.0f);
        }

        for(int a=window_size; a < accX.size(); a++) {
            float mean = getMean(accX.subList(a-window_size, a));
            filtered.add(mean);
        }

        return filtered;
    }

    // Method which returns a mean of the given list.
    private float getMean(List<Float> data) {
        float avg = 0.0f;
        for(int a=0; a < data.size(); a++) {
            avg += data.get(a);
        }

        return (avg / window_size);
    }
}
