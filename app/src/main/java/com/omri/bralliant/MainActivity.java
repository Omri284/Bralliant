package com.omri.bralliant;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.security.Permission;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    public final static int DEVICE_FOUND = 2;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_SCANNING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int DATA_RECEIVED = 3;

    public static final int NONE = 0;
    public static final int LEFT_ACTIVE = 1;
    public static final int RIGHT_ACTIVE = 2;
    public static final int BOTH_ACTIVE = 3;

    public static final int TIME = 1;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String STATE = "state";
    public static final String TIME_PASSED = "time_passed";

    LinearLayout mBTwarning;
    Button mBTbtn;
    Button mDebugBtn;
    //AddItem mAddItem;
    Bluetooth mBluetooth;

    TextView mTime0;
    TextView mTime1;
    TextView mTime2;
    TextView mTime3;

    TextView mDifference1;
    TextView mDifference2;

    ImageView mR0;
    ImageView mL0;
    ImageView mR1;
    ImageView mL1;
    ImageView mR2;
    ImageView mL2;
    ImageView mR3;
    ImageView mL3;

    Calendar calendar;

    Date startTime1;
    Date startTime2;
    Date startTime3;

    float oldTimeDifference = 165;
    String startTimeString;
    Date testTime;
    DateFormat df;
    final String dateString = "16-08-2015 16:15:16";

    private int timePassed;
    int FIFOState = 0;
    int oldState;
    int stateInt;
    int timePassedMin;
    int lastState = NONE;
    private Handler mScanHandler;
    boolean activeDataFlag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBTbtn = findViewById(R.id.BTbtn);

        mTime0 = findViewById(R.id.time0);
        mTime1 = findViewById(R.id.time1);
        mTime2 = findViewById(R.id.time2);
        mTime3 = findViewById(R.id.time3);

        mDifference1 = findViewById(R.id.difference1);
        mDifference2 = findViewById(R.id.difference2);

        mR0 = findViewById(R.id.R0);
        mL0 = findViewById(R.id.L0);
        mR1 = findViewById(R.id.R1);
        mL1 = findViewById(R.id.L1);
        mR2 = findViewById(R.id.R2);
        mL2 = findViewById(R.id.L2);
        mR3 = findViewById(R.id.R3);
        mL3 = findViewById(R.id.L3);

        mDebugBtn = findViewById(R.id.debugBtn);
        mBTwarning = findViewById(R.id.BTwarning);
        mScanHandler = new Handler();

        df = new SimpleDateFormat("h:mm a");

        mBTbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBTwarning.setVisibility(View.GONE);
                mBluetooth.turnOnBTAdapter();
            }
        });

        mDebugBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetooth.connect();
            }
        });
        mBluetooth = new Bluetooth(this, this);

        initTimes();

    }

    public void updatesStatus(final int status, final String state){

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (status == DEVICE_FOUND)
                {
                    mDebugBtn.setText("Device found");
                    NoDataHandler();
                }
                else if (status == STATE_CONNECTED)
                {
                    mDebugBtn.setText("Connected");
                }
                else if (status == DATA_RECEIVED)
                {
                    activeDataFlag = true;
                    stateInt = Integer.parseInt(state);
                    saveData(stateInt);
                    stateMachine(stateInt);
                }
                else if (status == STATE_DISCONNECTED)
                {
                    activeDataFlag = false;
                    mDebugBtn.setText("Disconnected");
                    mScanHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetooth.searchDevice();
                    }
                }, 2000);
                }
                else if (status == STATE_SCANNING)
                {
                    mDebugBtn.setText("Scanning");
                    NoDataHandler();
                }
            }
        });
    }

    public void stateMachine(int state) {
        if (state == NONE) {
            mBluetooth.disconnect();
            if (lastState == LEFT_ACTIVE) {

            }
            else if (lastState == RIGHT_ACTIVE) {

            }
            else if (lastState == NONE) {

            }
        }
        else if (state == RIGHT_ACTIVE) {
            if (lastState == NONE) {
                setStartTime();
                openBFActivity(state);
            }
        }
        else if (state == LEFT_ACTIVE) {
            if (lastState == NONE) {
                setStartTime();
                openBFActivity(state);
            }
        }
        lastState = state;
    }

    void setTimesOnGui(int Side){
        int hours;
        float minutes;
        int seconds;
        float timeDifference;
        //Third row
        mR3.setVisibility(mR2.getVisibility());
        mL3.setVisibility(mL2.getVisibility());
        mTime3.setText(mTime2.getText());
        //Second row
        mR2.setVisibility(mR1.getVisibility());
        mL2.setVisibility(mL1.getVisibility());
        mTime2.setText(mTime1.getText());
        mDifference2.setText(mDifference1.getText());
        //First row

        mTime1.setText("" + startTimeString + ", " + timePassedMin + " min");
        if (Side == LEFT_ACTIVE) {
            mL1.setVisibility(View.VISIBLE);
            mR1.setVisibility(View.INVISIBLE);
        }
        else if (Side == RIGHT_ACTIVE) {
            mR1.setVisibility(View.VISIBLE);
            mL1.setVisibility(View.INVISIBLE);
        }
        else {
            Log.e("ADebugTag", "setTimesOnGui error");
        }
        long diff = startTime1.getTime() - startTime2.getTime();
        if (diff < 0) {
            hours = (int) (((diff / (1000 * 60 * 60)) % 24) + 23);
            minutes = (int) (((diff / (1000 * 60)) % 60) + 59);
            //seconds = (int) (diff / 1000 ) % 60;
        }
        else {
            hours = (int) (diff / (1000 * 60 * 60)) % 24;
            minutes = (int) (diff / (1000 * 60)) % 60;
            //seconds = (int) (diff / 1000 ) % 60;
        }
        timeDifference = (hours * 60 + minutes);
        mDifference1.setText("" + hours + "h " + (int)minutes + "min ");
        startTime2 = startTime1;
        double nextFeedingTimeMin = (( timeDifference + oldTimeDifference ) / 2.0) * 0.2 + 180 * 0.8;
        oldTimeDifference = timeDifference;
        Date nextFeedingTime = new Date();
        nextFeedingTime.setTime(TimeUnit.MINUTES.toMillis((long)nextFeedingTimeMin) + startTime1.getTime());
        mTime0.setText(df.format(nextFeedingTime));
        mR0.setVisibility(mL1.getVisibility());
        mL0.setVisibility(mR1.getVisibility());


    }

    private void setStartTime(){
        startTimeString = df.format(Calendar.getInstance().getTime());
        startTime1 = Calendar.getInstance().getTime();
    }

    private void initTimes() {
        calendar = Calendar.getInstance();
        calendar.set(2020,6,24,21,20);
        startTime2 = (calendar.getTime());
    }

    private void NoDataHandler() {
        mScanHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!activeDataFlag) {
                    updatesStatus(STATE_DISCONNECTED,"");
                }
            }
        }, 4000);
    }

    public void openBFActivity(int state) {
        Intent intent = new Intent(this,BreastfeedActivity.class);
        intent.putExtra("state", state);
        startActivityForResult(intent,TIME);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TIME) {
            if(resultCode == RESULT_OK){
                timePassed = data.getIntExtra("timePassed",50);
                if (timePassed != 0) { //Time reached destination
                    oldState = data.getIntExtra("state",60);
                    timePassedMin = ( timePassed / 60 ) % 60;
                    setTimesOnGui(oldState);
                }
                else { //Time didn't reach destination
                    Log.i("ADebugTag", "Time didn't reach distination");
                }
            }
            if (resultCode == RESULT_CANCELED) {
                mTime2.setText("Error RESULT CANCELED");
            }
        }
    }//onActivityResult

    public void setBTwarning(boolean warningOn){
        if (warningOn) {
            mBTwarning.setVisibility(View.VISIBLE);
        }
        else {
            mBTwarning.setVisibility(View.GONE);
        }
    }

    public void saveData(int state) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STATE,state);
        editor.apply();
        //Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show();
    }


}
