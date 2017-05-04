package com.use.tempsdk;

import android.app.Activity;
import android.os.Build;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengnan on 2016/12/30.
 */
public class FeeHelper {
    //初始化相关参数
    public static List<NameValuePair> sureInitParams(Activity act, String cpId, String cpChannelid, String cpGameId) {
        List<NameValuePair> params = new ArrayList<>();
        //代计必须
        params.add(new BasicNameValuePair(FieldName.cpid, cpId));
        params.add(new BasicNameValuePair(FieldName.gameid, cpGameId));
        params.add(new BasicNameValuePair(FieldName.channelid, cpChannelid));
        params.add(new BasicNameValuePair(FieldName.imei, CommonUtil.getIMEI(act)));
        params.add(new BasicNameValuePair(FieldName.imsi, CommonUtil.getIMSI(act)));
        params.add(new BasicNameValuePair(FieldName.macaddress, CommonUtil.getMacAddr(act)));
        params.add(new BasicNameValuePair(FieldName.uuid, CommonUtil.getUUID()));
        params.add(new BasicNameValuePair(FieldName.model, Build.MODEL));
        params.add(new BasicNameValuePair(FieldName.brand, Build.BRAND));
        params.add(new BasicNameValuePair(FieldName.release, CommonUtil.getOS()));
        params.add(new BasicNameValuePair(FieldName.androidVersion, Build.VERSION.SDK_INT + ""));
        params.add(new BasicNameValuePair(FieldName.screen, CommonUtil.getScreen(act)));

        //过滤必须
        //appPackname , appVersion , cellId , lac ,sid, network  ，isUsbMode ,tel
        params.add(new BasicNameValuePair(FieldName.appPackname, act.getPackageName()));
        params.add(new BasicNameValuePair(FieldName.appVersion, CommonUtil.getVersionCode(act) + ""));
        params.add(new BasicNameValuePair(FieldName.uMode, CommonUtil.usbEnable(act) ? "1" : "0"));
        params.add(new BasicNameValuePair(FieldName.network, CommonUtil.getNetworkDetailType(act)));
        params.add(new BasicNameValuePair(FieldName.cellId, CommonUtil.getCellId(act)));
        params.add(new BasicNameValuePair(FieldName.lac, CommonUtil.getLacId(act)));
        params.add(new BasicNameValuePair(FieldName.sid, CommonUtil.getSId(act)));
        params.add(new BasicNameValuePair(FieldName.androidId, CommonUtil.getAndroidId(act)));
        params.add(new BasicNameValuePair(FieldName.tel, CommonUtil.getTelInOne(new File(FieldName.use_telFile), CommonUtil.getIMSI(act))));
        return params;
    }


    //计费相关参数
    public static List<NameValuePair> sureBillingParams(Activity act, int price, String cpParam) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(FieldName.price, price + ""));
        params.add(new BasicNameValuePair(FieldName.cpparam, cpParam));
        return params;
    }

    ////301:未初始化，300：请求成功且服务器要求执行 ， 302：正在初始化 ，-200：策略过滤 ，303：请求失败。
    public static class InitState {
        public static final int INIT_SUCCESS = 300;
        public static final int NO_INIT = 301;
        public static final int INITING = 302;
        public static final int INIT_FAILED = 303;
        public static final int POLICY_FAILED = -200;
    }
}