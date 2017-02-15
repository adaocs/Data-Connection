package com.example.android.test;

import android.renderscript.Matrix2f;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by Shravan Aras on 2/8/17.
 */

public class Algorithm {
    ArrayList<Float> accX;
    ArrayList<Long> time;
    ArrayList<Float> velocity;
    ArrayList<Float> distance;

    //Global vales which handle the working of the algorithm.
    float dthreshold;
    int cthreshold;

    private int window_size = 10;

    Algorithm(ArrayList<Float> accX, ArrayList<Long> time, float dthreshold, int cthreshold) {
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

    /* Private method which returns the velocity after integrating the acceleration. */
    private void calculateVelocity() {
        int cbuff = 0;

        velocity.add(0F);

        for(int a=0; a < accX.size()-1; a++) {
            float y1 = accX.get(a);
            float y2 = accX.get(a+1);
            float h = (y1 + y2) / 2;
            float w = (time.get(a+1) - time.get(a)) / 1000f;

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
    private void calculateDistance() {
        distance.add(0F);

        for(int a=0; a < velocity.size()-1; a++) {
            float y1 = Math.abs(velocity.get(a));
            float y2 = Math.abs(velocity.get(a+1));
            float h = (y1 + y2) / 2;
            float w = (time.get(a+1) - time.get(a)) / 1000f;

            float delta_area = h * w;

            distance.add(distance.get(distance.size()-1) + delta_area);
        }
    }

    /* Method which return the distance the person has walked. */
    public ArrayList<Float> getDistance() {
        calculateVelocity();
        calculateDistance();

        return distance;
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
