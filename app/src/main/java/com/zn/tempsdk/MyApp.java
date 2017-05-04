package com.zn.tempsdk;

import android.Manifest;
import android.app.Application;
import android.util.Log;

import com.use.tempsdk.CommonUtil;

/**
 * Created by zhengnan on 2016/11/30.
 */
public class MyApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(getPackageName() + "权限？", "" + CommonUtil.checkPermission(this, Manifest.permission.SEND_SMS));
        Log.e("权限？", "" + CommonUtil.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION));
        Log.e("权限？", "" + CommonUtil.checkPermission(this, Manifest.permission.READ_PHONE_STATE));
        Log.e("权限？", "" + CommonUtil.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }
}