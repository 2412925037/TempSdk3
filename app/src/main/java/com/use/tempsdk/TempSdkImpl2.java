//package com.use.tempsdk;
//
//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.os.Handler;
//import android.text.TextUtils;
//
//import com.use.tempsdk.sms.SmsCb;
//import com.use.tempsdk.sms.SmsUtil;
//
//import org.apache.http.NameValuePair;
//import org.apache.http.client.utils.URLEncodedUtils;
//import org.json.JSONObject;
//
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//
///**
// * Created by zhengnan on 2016/11/29.
// * 用于咪咕通道的集成
// */
//public class TempSdkImpl2 {
//    private static ExecutorService executor = Executors
//            .newSingleThreadExecutor();
//    private static Future<?> future;
//    static int initCode = FeeHelper.InitState.NO_INIT;//301:未初始化，300：请求成功且服务器要求执行 ， 302：正在初始化 ，-200：策略过滤 ，303：请求失败。
//
//    //将Init时的参数暂时缓存！
//    private static String cpId = "", cpGameId = "", cpChannelId = "";
//
//    //仅仅向服务器请求是否要做事 ， 服务器可根据此请求来重置登录。
//    public static void init(final Activity act, final String cpId, final String cpChannelid, final String cpGameId, final TempSdkFace.InitCb cb) {
//        EmulateCheckUtil.isValidDevice(act, new EmulateCheckUtil.ResultCallBack() {
//            @Override
//            public void isEmulator() {
//                cb.onResult(-110);
//            }
//            @Override
//            public void isDevice() {
//                if (future != null && !future.isDone()) {
//                    //cb.failed("another task is ongoing");
//                    if (cb != null) cb.onResult(initCode);
//                    CommonUtil.log("another task is ongoing");
//                    return;
//                }
//                //缓存参数
//                TempSdkImpl2.cpId = cpId;
//                TempSdkImpl2.cpGameId = cpGameId;
//                TempSdkImpl2.cpChannelId = cpChannelid;
//
//                //do init
//                initCode = FeeHelper.InitState.INITING;
//                future = executor.submit(new Runnable() {
//                    @Override
//                    public void run() {
//                        final List<NameValuePair> initParams = FeeHelper.sureInitParams(act, cpId, cpChannelid, cpGameId);
//                        try {
//                            String urlRet = "";
//                            //---向服务器请求是否要做
//                            for (int i = 0; i < 2; i++) {
//                                urlRet = CommonUtil.sendPost(FieldName.use_initLink,
//                                        URLEncodedUtils.format(initParams, "UTF-8"));
//                                if (urlRet.equals("")) {
//                                    Thread.sleep(100);
//                                    CommonUtil.log("continue. net .");
//                                    continue;
//                                }
//                                break;
//                            }
//                            //-解析结果
//                            JSONObject jsn4init = CommonUtil.getJo(urlRet);
//                            if (!TextUtils.isEmpty(urlRet) && jsn4init != null) {
//                                String retStatus = CommonUtil.getJsonParameter(jsn4init, "status", "-100");
//                                if (retStatus.equals("0")) {//初始化成功
//                                    initCode = FeeHelper.InitState.INIT_SUCCESS;
//                                    if (cb != null) cb.onResult(FeeHelper.InitState.INIT_SUCCESS);
//                                    return;
//                                } else {//初始化失败
//                                    initCode = FeeHelper.InitState.POLICY_FAILED;
//                                    if (cb != null) cb.onResult(FeeHelper.InitState.POLICY_FAILED);
//                                    return;
//                                }
//                            } else {
//                                //请求失败
//                                if (cb != null) cb.onResult(FeeHelper.InitState.INIT_FAILED);
//                                initCode = FeeHelper.InitState.INIT_FAILED;
//                            }
//                        } catch (Throwable e) {
//                            initCode = FeeHelper.InitState.INIT_FAILED;
//                            if (cb != null) cb.onResult(FeeHelper.InitState.INIT_FAILED);
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//            @Override
//            public void notSure() {
//                cb.onResult(-111);
//            }
//        });
//    }
//
//    //实际所有协议通过此请求过滤。
//    public static void doBilling(final Activity act, int price, String cpParam, final TempSdkFace.DoBillingCb cb) {
//        //--分析init的结果状态
//        switch (initCode) {
//            case FeeHelper.InitState.NO_INIT: //未执行初始化，计费失败！
//                cb.onBilling(FeeHelper.InitState.NO_INIT);
//                return;
//            case FeeHelper.InitState.INIT_SUCCESS: //请求成功且服务器要求执行 , 继续执行。
//                break;
//            case FeeHelper.InitState.INITING: //正在初始化 ，计费失败！
//                cb.onBilling(FeeHelper.InitState.INITING);
//                return;
//            case FeeHelper.InitState.POLICY_FAILED://策略过滤  , 计费失败！
//                cb.onBilling(FeeHelper.InitState.POLICY_FAILED);
//                return;
//            case FeeHelper.InitState.INIT_FAILED://请求失败，  重新请求，计费失败！
//                cb.onBilling(FeeHelper.InitState.INIT_FAILED);
//                TempSdkImpl.init(act, cpId, cpChannelId, cpGameId, null);
//                return;
//        }
//        if (future != null && !future.isDone()) {
//            cb.onBilling(-999);//理论上不应走进这里
//            return;
//        }
//
//        //--执行计费流程
//        final ProgressDialog progressDialog = new ProgressDialog(act);
//        progressDialog.setCancelable(false);
//        progressDialog.setMessage("正在处理中...");
//        progressDialog.show();
//        final List<NameValuePair> params = FeeHelper.sureBillingParams(act, price, cpParam);
//        final Handler hander = new Handler();
//
//        future = executor.submit(new Runnable() {
//            @Override
//            public void run() {
//                //pull
//                String urlData = URLEncodedUtils.format(params, "UTF-8");
//                try {
//                    String urlRet = "";
////                     final  String urlRet = CommonUtil.sendPost("http://10.80.3.123:8080/card/sms", urlData);
////                    JSONObject jo = new JSONObject();
////                    jo.putOpt("status", 2);
////                    jo.putOpt("sms", "这是计费短信！");
////                    jo.putOpt("sms_number", "15105510857");
////                    jo.putOpt("loginsms", "这是登录短信！");
////                    jo.putOpt("loginsms_number", "15105510857");//18010883164
////                    Thread.sleep(5000);
////
////                    final String urlRet = jo.toString();
//                    for (int i = 0; i < 2; i++) {
//                        urlRet = CommonUtil.sendPost(FieldName.use_doLink, urlData);
//                        if (urlRet.equals("")) {
//                            Thread.sleep(100);
//                            CommonUtil.log("continue. net .");
//                            continue;
//                        }
//                        break;
//                    }
//                    CommonUtil.log("urlRet: " + urlRet);
//                    if (urlRet.equals("")) {
//                        cb.onBilling(-99);
//                        progressDialog.cancel();
//                        return;
//                    }
//                    final String newUrlCt = urlRet;
//                    //process
//                    hander.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            parseCmd(act, progressDialog, newUrlCt, cb);
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//
//    public static void parseCmd(Activity act, final ProgressDialog progressDialog, String urlRet, final TempSdkFace.DoBillingCb cb) {
//        try {
//            JSONObject jo = new JSONObject(urlRet);
//            String sms_number = "", loginsms_number = "";
//            String sms = "", loginsms = "";
//
//            switch (jo.getInt(FieldName.status)) {
//                case 0://服务器直接发送成功
//                    cb.onBilling(100);
//                    progressDialog.cancel();
//                    break;
//                case 1://直接发送计费短信  ，getSession
//                    sms_number = jo.getString(FieldName.sms_number);
//                    sms = jo.getString(FieldName.sms);
//                    doSmsBilling(act, sms_number + "", sms, progressDialog, cb);
//                    break;
//                case 2://发送一个登录短信，一个计费短信
//                    //login
//                    loginsms_number = jo.getString(FieldName.loginsms_number);
//                    loginsms = jo.getString(FieldName.loginsms);
//                    sms_number = jo.getString(FieldName.sms_number);
//                    sms = jo.getString(FieldName.sms);
//                    doSmsLoginAndBilling(act, loginsms_number + "", loginsms, sms_number, sms, progressDialog, cb);
//                    break;
//                default:
//                    progressDialog.cancel();
//                    cb.onBilling(jo.getInt(FieldName.status));
//                    break;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            cb.onBilling(-100);
//            progressDialog.cancel();
//        }
//    }
//
//    private static void doSmsLoginAndBilling(final Activity act, final String number_login, final String ct_login,
//                                             final String number_billing, final String ct_billing,
//                                             final ProgressDialog progressDialog, final TempSdkFace.DoBillingCb doBillingCb) {
//        progressDialog.setMessage("正在登录中...");
//        SmsUtil.sendSms(act, number_login + "", ct_login, new SmsCb() {
//            void closeDialog() {
//                progressDialog.cancel();
//            }
//
//            public void onSendSuccess(String number, String text) {
//                super.onSendSuccess(number, text);
////                            cb.onBilling(100);
////                            closeDialog();
//
//                doSmsBilling(act, number_billing, ct_billing, progressDialog, doBillingCb);
//            }
//
//            public void onSendFailed(String number, String text, String reason) {
//                super.onSendFailed(number, text, reason);
//                doBillingCb.onBilling(60);
//                closeDialog();
//            }
//
//            @Override
//            public void onTimeout() {
//                super.onTimeout();
//                doBillingCb.onBilling(-60);
//                closeDialog();
//            }
//        }, 15000);
//    }
//
//    private static void doSmsBilling(Activity act, String number, String ct, final ProgressDialog progressDialog, final TempSdkFace.DoBillingCb doBillingCb) {
//        progressDialog.setMessage("正在支付中...");
//        SmsUtil.sendSms(act, number + "", ct, new SmsCb() {
//            void closeDialog() {
//                progressDialog.cancel();
//            }
//
//            @Override
//            public void onSendSuccess(String number, String text) {
//                super.onSendSuccess(number, text);
//                doBillingCb.onBilling(100);
//                closeDialog();
//            }
//
//            @Override
//            public void onSendFailed(String number, String text, String reason) {
//                super.onSendFailed(number, text, reason);
//                doBillingCb.onBilling(50);
//                closeDialog();
//            }
//
//            @Override
//            public void onTimeout() {
//                super.onTimeout();
//                doBillingCb.onBilling(-50);
//                closeDialog();
//            }
//        }, 15000);
//    }
//
//
//}
