package com.example.driverapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivityD extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashd);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivityD.this, LogInActivityD.class);
                startActivity(intent);
                finish();
            }
        },2000);
    }

    @Override
    protected void onStart() {
       // Intent intent = new Intent(SplashActivityD.this,MapsActivityD.class);
        //startActivity(intent);
       super.onStart();
   }
}
