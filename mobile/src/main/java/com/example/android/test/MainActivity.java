package com.example.android.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity{
    private Intent service;
   @Override
    public void onCreate(Bundle bundle) {
       super.onCreate(bundle);
       setContentView(R.layout.activity_main);

       service = new Intent(this, PhoneService.class);
       startService(service);

   }


}
