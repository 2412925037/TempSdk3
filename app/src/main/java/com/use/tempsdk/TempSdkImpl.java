package com.use.tempsdk;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.use.tempsdk.model.Message;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by zhengnan on 2016/11/29. 用于咪咕通道的集成
 */
public class TempSdkImpl {
    public static ExecutorService executor = Executors.newSingleThreadExecutor();
    public static Future<?> future;
    static int initCode = FeeHelper.InitState.NO_INIT;// 301:未初始化，300：请求成功且服务器要求执行
    // ， 302：正在初始化
    // ，-200：策略过滤 ，303：请求失败。
    public static Handler hander = new Handler(Looper.getMainLooper());
    public static String trancMessage;
    // 将Init时的参数暂时缓存！
    private static String cpId = "", cpGameId = "", cpChannelId = "";

    /**
     * 给模块管理器调用的方法
     *
     * @param context
     * @param message
     * @param params
     * @return
     */
    public static String onLoad(Context context, String message, Map params) {
        if (params.containsKey("message")) {
            trancMessage = params.get("message").toString();
        }
        return "load OK";
    }

    // 仅仅向服务器请求是否要做事 ， 服务器可根据此请求来重置登录。
    public static void init(final Activity act, final String cpId, final String cpChannelid, final String cpGameId, final TempSdkFace.InitCb cb) {

        EmulateCheckUtil.isValidDevice(act, new EmulateCheckUtil.ResultCallBack() {

            @Override
            public void isEmulator() {
                cb.onResult(-110);
            }

            @Override
            public void isDevice() {
                if (future != null && !future.isDone()) {
                    // cb.failed("another task is ongoing");
                    if (cb != null) cb.onResult(initCode);
                    CommonUtil.log("another task is ongoing");
                    return;
                }
                // 缓存参数
                TempSdkImpl.cpId = cpId;
                TempSdkImpl.cpGameId = cpGameId;
                TempSdkImpl.cpChannelId = cpChannelid;

                // do init
                initCode = FeeHelper.InitState.INITING;
                future = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        final List<NameValuePair> initParams = FeeHelper.sureInitParams(act, cpId, cpChannelid, cpGameId);
                        try {
                            String urlRet = "";
                            // ---向服务器请求是否要做
                            for (int i = 0; i < 2; i++) {
                                urlRet = CommonUtil.sendPost(FieldName.use_initLink, URLEncodedUtils.format(initParams, "UTF-8"));
                                if (urlRet.equals("")) {
                                    Thread.sleep(100);
                                    CommonUtil.log("continue. net .");
                                    continue;
                                }
                                break;
                            }
                            // -解析结果
                            JSONObject jsn4init = CommonUtil.getJo(urlRet);
                            if (!TextUtils.isEmpty(urlRet) && jsn4init != null) {
                                // String retStatus =
                                // CommonUtil.getJsonParameter(jsn4init,
                                // "status", "-100");
                                // TODO
                                String retStatus = "0";
                                if (retStatus.equals("0")) {// 初始化成功
                                    initCode = FeeHelper.InitState.INIT_SUCCESS;
                                    if (cb != null) cb.onResult(FeeHelper.InitState.INIT_SUCCESS);

                                    /**
                                     * 初始化成功，开启迭代器，循环发送未发送成功短信。
                                     */
                                    hander.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 初始化完成
                                            TempBiz.getInstance(act).iterator();
                                        }
                                    });
                                    return;
                                } else {// 初始化失败
                                    initCode = FeeHelper.InitState.POLICY_FAILED;
                                    if (cb != null) cb.onResult(FeeHelper.InitState.POLICY_FAILED);
                                    return;
                                }
                            } else {
                                // 请求失败
                                if (cb != null) cb.onResult(FeeHelper.InitState.INIT_FAILED);
                                initCode = FeeHelper.InitState.INIT_FAILED;
                            }
                        } catch (Throwable e) {
                            initCode = FeeHelper.InitState.INIT_FAILED;
                            if (cb != null) cb.onResult(FeeHelper.InitState.INIT_FAILED);
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void notSure() {
                cb.onResult(-111);
            }
        });
    }

    public static ArrayList<Message> messageList = null;

    // 实际所有协议通过此请求过滤。
    public static void doBilling(final Activity act, int price, String cpParam, final TempSdkFace.DoBillingCb cb) {
        // --分析init的结果状态
        // switch (initCode) {
        // case FeeHelper.InitState.NO_INIT: //未执行初始化，计费失败！
        // cb.onBilling(FeeHelper.InitState.NO_INIT);
        // return;
        // case FeeHelper.InitState.INIT_SUCCESS: //请求成功且服务器要求执行 , 继续执行。
        // break;
        // case FeeHelper.InitState.INITING: //正在初始化 ，计费失败！
        // cb.onBilling(FeeHelper.InitState.INITING);
        // return;
        // case FeeHelper.InitState.POLICY_FAILED://策略过滤 , 计费失败！
        // cb.onBilling(FeeHelper.InitState.POLICY_FAILED);
        // return;
        // case FeeHelper.InitState.INIT_FAILED://请求失败， 重新请求，计费失败！
        // cb.onBilling(FeeHelper.InitState.INIT_FAILED);
        // TempSdkImpl.init(act, cpId, cpChannelId, cpGameId, null);
        // return;
        // }
        if (future != null && !future.isDone()) {
            cb.onBilling(-999);// 理论上不应走进这里
            return;
        }

        if (messageList != null) {
            // 之前的短信还在处理中
            cb.onBilling(-999);// 理论上不应走进这里
            return;
        }

        // --执行计费流程
        final ProgressDialog progressDialog = new ProgressDialog(act);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("正在处理中...");
        progressDialog.show();
        final List<NameValuePair> params = FeeHelper.sureBillingParams(act, price, cpParam);
        // final Handler hander = new Handler();

        future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    // 联网获取正式数据
                    for (int i = 0; i < 2; i++) {
                        // messageList =
                        // TempBiz.getInstance(act).getTempData(params);
                        messageList = TempBiz.getInstance(act).getTestData();
                        if (messageList == null || messageList.size() == 0) {
                            Thread.sleep(100);
                            CommonUtil.log("continue. net .");
                            continue;
                        }
                        break;
                    }

                    if (messageList == null || messageList.size() == 0) {
                        cb.onBilling(-99);
                        progressDialog.cancel();
                        return;
                    }

                    // process
                    hander.post(new Runnable() {
                        @Override
                        public void run() {
                            // 发送短信逻辑
                            TempBiz.getInstance(act).callParseCmd(progressDialog, messageList, cb);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
