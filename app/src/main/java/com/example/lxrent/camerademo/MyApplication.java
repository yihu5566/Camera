package com.example.lxrent.camerademo;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.zero.smallvideorecord.DeviceUtils;
import com.zero.smallvideorecord.JianXiCamera;

import java.io.File;

/**
 * Created by 卢东方 on 2017/4/26 下午12:40.
 * <p>
 * God love people
 * <p>
 * description:
 */
public class MyApplication extends Application {
    private static MyApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        init();
    }


    private void init() {
        // 设置压缩视频缓存路径
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                JianXiCamera.setVideoCachePath(dcim + "/zero/");
            } else {
                JianXiCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/",
                        "/sdcard-ext/")
                        + "/zero/");
            }
        } else {
            JianXiCamera.setVideoCachePath(dcim + "/zero/");
        }

    }

    public static MyApplication getInstance() {
        return mApplication;
    }
}
