package com.omri.bralliant;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    public final static int DEVICE_FOUND = 2;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int DATA_RECEIVED = 3;

    LinearLayout mBTwarning;
    Button mBTbtn;
    Button mDebugBtn;
    //AddItem mAddItem;
    Bluetooth mBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBTbtn = (Button) findViewById(R.id.BTbtn);
        mDebugBtn = (Button) findViewById(R.id.debugBtn);
        mBTwarning = (LinearLayout) findViewById(R.id.BTwarning);

        mBTbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBTwarning.setVisibility(View.GONE);
                //mBluetooth.turnOnBTAdapter();
            }
        });

        mDebugBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetooth.connect();
            }
        });

        mBluetooth = new Bluetooth(this, this);
    }

    public void updatesStatus(final int status, final String state){

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (status == DEVICE_FOUND)
                {

                }
                else if (status == STATE_CONNECTED)
                {

                }
                else if (status == DATA_RECEIVED)
                {

                }
                else if (status == STATE_DISCONNECTED)
                {

                }
                else if (status == STATE_CONNECTING)
                {

                }
            }
        });
    }

    public void setBTwarning(){
        mBTwarning.setVisibility(View.VISIBLE);
    }

}
