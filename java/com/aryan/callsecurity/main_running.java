package com.aryan.callsecurity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class main_running extends AppCompatActivity {
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_running);
//userDevice admin
        try {
            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            mAdminName = new ComponentName(this, DeviceAdminDemo.class);
            if (!mDPM.isAdminActive(mAdminName)) {
                Toast.makeText(this, "on", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "click on activate button to secure your application.");
                startActivityForResult(intent, REQUEST_CODE);
            } else {
//                mDPM.lockNow();
//                Intent intent = new Intent(MainActivity.this,TrackDeviceService.class);
//                startService(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Button button = findViewById(R.id.stop_operation);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                operation_stop();
            }
        });


    }

    public void operation_stop() {

        Intent intent1 = new Intent(this, Recording_service.class);
        stopService(intent1);
        Toast.makeText(this, "fraud call detection service is stopped.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (REQUEST_CODE == requestCode) {
//            Toast.makeText(this, "starting the app....", Toast.LENGTH_SHORT).show();
//            Intent intent = new Intent(main_running.this, Recording_service.class);
//            startService(intent);
//        }
//    }
}