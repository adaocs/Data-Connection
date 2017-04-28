package com.example.android.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Caleb on 4/26/17.
 */

public class FinishPopup extends Activity {

    private TextView txtTime;
    private Button butt_restart;
    private Button butt_finish;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.finishwindow);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*.8), (int) (height*.5));

        txtTime = (TextView)findViewById(R.id.finish_avgTime);
        butt_restart = (Button)findViewById(R.id.finish_restart);
        butt_finish = (Button)findViewById(R.id.finish_finish);

        butt_restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }
        });

        butt_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            float averageTime = extras.getFloat("averageTime");
                    txtTime.setText("Average Time: " + averageTime);
        }
        else
            Log.i("FinishPopup", "Did not recieve the average time.");
    }
}
