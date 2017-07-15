package com.example.lxrent.camerademo;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import com.example.lxrent.camerademo.util.FSScreen2;
import com.example.lxrent.camerademo.view.LProgressBar;

public class MainActivity extends Activity {

    private LProgressBar viewById;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FSScreen2.init(this);
        viewById = (LProgressBar) findViewById(R.id.tv);
        viewById.setOnProgressTouchListener(new LProgressBar.OnProgressTouchListener() {
            @Override
            public void onClick(LProgressBar progressBar) {
                Log.i(TAG, "onClick");
                Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
                startActivity(intent);
            }

            @Override
            public void onLongClick(LProgressBar progressBar) {
                Log.i(TAG, "onLongClick");
            }

            @Override
            public void onLongClickUp(LProgressBar progressBar) {
                Log.i(TAG, "onLongClickUp");
            }
        });
//        viewById.setOnClickListener(this);
    }





}
