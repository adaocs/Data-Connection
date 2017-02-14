package com.example.android.test;

import android.renderscript.Matrix2f;

import java.lang.reflect.Array;
import java.util.ArrayList;
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

    Algorithm(ArrayList<Float> accX, ArrayList<Long> time, float dthreshold, int cthreshold) {
        this.accX = accX;
        this.time = time;
        this.dthreshold = dthreshold;
        this.cthreshold = cthreshold;

        velocity = new ArrayList<>();
        distance = new ArrayList<>();

        // Preprocess the time to make it relative and in seconds.
        Long startTime = time.get(0);
        for(int a=0; a < time.size(); a++) {
            time.set(a, time.get(a) - startTime);
            time.set(a, time.get(a) / 1000);
        }
    }

    /* Private method which returns the velocity after integrating the acceleration. */
    private void calculateVelocity() {
        int cbuff = 0;

        velocity.add(0F);

        for(int a=0; a < accX.size()-1; a++) {
            float y1 = accX.get(a);
            float y2 = accX.get(a+1);
            float h = (y1 + y2) / 2;
            float w = time.get(a+1) - time.get(a);

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
            float w = time.get(a+1) - time.get(a);

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
}
