package com.example.lxrent.camerademo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.lxrent.camerademo.util.CompressVideoUtils;
import com.example.lxrent.camerademo.util.FSScreen2;
import com.example.lxrent.camerademo.util.PathManager;
import com.example.lxrent.camerademo.view.LProgressBar;
import com.vincent.videocompressor.VideoCompress;

public class MainActivity extends Activity {

    private LProgressBar viewById;
    private static final String TAG = MainActivity.class.getSimpleName();
    int REQUEST_CODE_CONTACT = 101;
    String destPath = "/storage/emulated/0/aaaa/aa/1553131505244.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FSScreen2.init(this);
        viewById = findViewById(R.id.tv);
        viewById.setOnProgressTouchListener(new LProgressBar.OnProgressTouchListener() {
            @Override
            public void onClick(LProgressBar progressBar) {
                Log.i(TAG, "onClick" + destPath);
                Intent intent = new Intent(MainActivity.this, CameraCaptureActivity.class);
                startActivity(intent);
            }

            @Override
            public void onLongClick(LProgressBar progressBar) {
                Log.i(TAG, "onLongClick==");
            }

            @Override
            public void onLongClickUp(LProgressBar progressBar) {
                Log.i(TAG, "onLongClickUp");
            }
        });
//        viewById.setOnClickListener(this);

    }

    private void init() {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_CONTACT) {
            Toast.makeText(this, "权限申请成功", Toast.LENGTH_SHORT).show();
        }
    }
}
