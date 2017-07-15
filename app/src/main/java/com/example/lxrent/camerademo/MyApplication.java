package com.example.lxrent.camerademo;

import android.app.Application;

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
    }

    public static MyApplication getInstance() {
        return mApplication;
    }
}
