package com.omri.bralliant;

import androidx.appcompat.app.AppCompatActivity;
import gr.net.maroulis.library.EasySplashScreen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import java.net.MalformedURLException;

public class SplashScreenActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(homeIntent);
                finish();
            }
        },SPLASH_TIME_OUT);
    }
}

//
//    EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
//            .withFullScreen()
//            .withTargetActivity(MainActivity.class)
//            .withSplashTimeOut(5000)
//            .withBackgroundColor(Color.parseColor("#FFFFFF"))
//            .withAfterLogoText("Breastfeeding with peace of mind")
//            .withLogo(R.drawable.bralliantlogo);
//
//        config.getAfterLogoTextView().setTextColor(Color.parseColor("#FFC0CB"));
//
//                View easySplashScreen = config.create();
//                setContentView(easySplashScreen);