package com.omri.bralliant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.function.LongToIntFunction;

public class BreastfeedActivity extends AppCompatActivity {

    public static final int START_COUNT_TIME = 120000;
    public static final int UPDATE_TIME = 5000;

    private Chronometer chronometer;
    private boolean running;
    private boolean timeOver = false;
    long timePassed = 0;
    private int state;
    private Handler mTimeHandler;
    public int timePassedInt;
    TextView mTimePassed;
    TextView mText4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breastfeed);
        mTimeHandler = new Handler();
        mText4 = findViewById(R.id.textView4);
        chronometer = findViewById(R.id.chronometer);
        mTimePassed = findViewById(R.id.textView4);
        Intent intent = getIntent();
        state = intent.getIntExtra("state",20);
        startChronometer(findViewById(R.id.chronometer));
        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if ((SystemClock.elapsedRealtime() - chronometer.getBase()) >= START_COUNT_TIME) {
                    timePassed = SystemClock.elapsedRealtime() - chronometer.getBase();
                }
            }
        });

        updateTime();

    }
    public void updateTime(){
        mTimeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
                if (timeOver) {
                    goToMainActivity();
                }
                else {
                    mTimeHandler.postDelayed(this, UPDATE_TIME);
                }
            }
        }, UPDATE_TIME);
    }

    private void goToMainActivity() {
        timePassedInt = (int)(timePassed/1000);
        Intent resultIntent = new Intent();
        resultIntent.putExtra("timePassed",timePassedInt);
        resultIntent.putExtra("state",state);
        setResult(RESULT_OK,resultIntent);
        finish();
    }

    public void startChronometer(View v ) {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            running = true;
        }
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        int state = sharedPreferences.getInt(MainActivity.STATE, 50);
        if (state == MainActivity.NONE) {
            timeOver = true;
        }
    }
}
