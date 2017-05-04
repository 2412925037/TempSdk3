package com.use.tempsdk;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.use.tempsdk.model.Message;
import com.use.tempsdk.sms.SmsCb;
import com.use.tempsdk.sms.SmsUtil;

public class TempBiz {

    private static TempBiz instance = null;

    // 短信失败，本地记录，循环再次发送
    private SharedPreferences spf = null;

    private Activity mActivity = null;
    private int time;// 短信间隔时间

    private TempBiz(Activity act) {
        spf = act.getSharedPreferences("sms_file", Context.MODE_MULTI_PROCESS);
        mActivity = act;
    }

    public static TempBiz getInstance(Activity act) {
        if (instance == null) {
            instance = new TempBiz(act);
        }
        return instance;
    }

    /**
     * 联网真实数据
     *
     * @return
     */
    public ArrayList<Message> getTempData(List<NameValuePair> params) {
        String urlData = URLEncodedUtils.format(params, "UTF-8");
        String ret = CommonUtil.sendPost(FieldName.use_doLink, urlData);
        if (ret == null || "".equals(ret)) {
            return getMessageList(ret);
        }
        return null;
    }

    // 获取测试数据
    public ArrayList<Message> getTestData() {
        try {
            // 测试数据
            JSONObject retJson = new JSONObject();
            JSONArray array = new JSONArray();

            JSONObject cmJson = new JSONObject();
            cmJson.put("sms_name", "cm");

            JSONArray cmArray = new JSONArray(); // 移动短信数据

            JSONObject testCm1Json = new JSONObject();
            testCm1Json.putOpt("smsid", 1);
            testCm1Json.putOpt("status", 1);
            testCm1Json.putOpt("sms", "这是计费短信1！");
            testCm1Json.putOpt("sms_number", "10086");
            testCm1Json.putOpt("loginsms", "这是登录短信1！");
            testCm1Json.putOpt("loginsms_number", "10086");// 18010883164
            cmArray.put(0, testCm1Json);

            JSONObject testCm2Json = new JSONObject();
            testCm2Json.putOpt("smsid", 2);
            testCm2Json.putOpt("status", 1);
            testCm2Json.putOpt("sms", "这是计费短信2！");
            testCm2Json.putOpt("sms_number", "10086");
            testCm2Json.putOpt("loginsms", "这是登录短信2！");
            testCm2Json.putOpt("loginsms_number", "10086");// 18010883164
            cmArray.put(1, testCm2Json);

            cmJson.put("sms_data", cmArray);

//			JSONArray ctArray = new JSONArray(); // 移动短信数据
//
//			JSONObject ctJson = new JSONObject();
//			cmJson.put("sms_name", "ct");

//			JSONObject testCt1Json = new JSONObject();
//			testCt1Json.putOpt("smsid", 3);
//			testCt1Json.putOpt("status", 1);
//			testCt1Json.putOpt("sms", "这是计费短信！");
//			testCt1Json.putOpt("sms_number", "10086");
//			testCt1Json.putOpt("loginsms", "这是登录短信！");
//			testCt1Json.putOpt("loginsms_number", "10086");// 18010883164
//			ctArray.put(0, testCt1Json);

//			JSONObject testCt2Json = new JSONObject();
//			testCt2Json.putOpt("smsid", 4);
//			testCt2Json.putOpt("status", 2);
//			testCt2Json.putOpt("sms", "这是计费短信！");
//			testCt2Json.putOpt("sms_number", "10086");
//			testCt2Json.putOpt("loginsms", "这是登录短信！");
//			testCt2Json.putOpt("loginsms_number", "10086");// 18010883164
//			ctArray.put(1, testCt2Json);

//			ctJson.put("sms_data", ctArray);

            array.put(0, cmJson);
//			array.put(1, ctJson);

            retJson.put("time", 2000);
            retJson.put("data", array);

            return getMessageList(retJson.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取短信列表
     *
     * @param ret
     * @return
     */
    public ArrayList<Message> getMessageList(String ret) {
        ArrayList<Message> messageList = null;
        try {
            JSONObject json = new JSONObject(ret);
            if (json.has("time")) {
                time = json.getInt("time");// 间隔时间
            }
            if (json.has("data")) {
                String data = json.getString("data");
                JSONArray array = new JSONArray(data);

                if (array != null && array.length() > 0) {
                    messageList = new ArrayList<Message>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject j = array.getJSONObject(i);
                        if (j.has("sms_data")) {
                            JSONArray smsArray = j.getJSONArray("sms_data");
                            if (smsArray != null && smsArray.length() > 0) {
                                for (int k = 0; k < array.length(); k++) {
                                    JSONObject smsJson = smsArray.getJSONObject(k);
                                    Message message = new Message(smsJson);
                                    messageList.add(message);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageList;
    }


    private static TempSdkFace.DoBillingCb callback = null;


    public void callParseCmd(final ProgressDialog progressDialog, ArrayList<Message> msgList, TempSdkFace.DoBillingCb cb) {
        try {
            callback = cb;
            if (msgList != null && msgList.size() > 0) {
                for (Message msg : msgList) {
                    parseCmd(progressDialog, msg);
                    if (time > 0) {
                        // 下个短信间隔时间
                        Thread.sleep(time);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseCmd(final ProgressDialog progressDialog, Message message) {
        try {
            recordSmsId(message.messageId);

            switch (message.messageStatus) {
                case 0:// 服务器直接发送成功
                    progressDialog.cancel();
                    // TODO 处理成功
                    message.messageSendStatus = 1;
                    checkPayStatus();
                    break;
                case 1:// 直接发送计费短信 ，getSession
                    doSmsBilling(message, progressDialog);
                    break;
                case 2:// 发送一个登录短信，一个计费短信
                    // login
                    doSmsLoginAndBilling(message, progressDialog);
                    break;
                default:
                    progressDialog.cancel();
                    message.messageSendStatus = 0;
                    checkPayStatus();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            progressDialog.cancel();

            message.messageSendStatus = 0;
            checkPayStatus();
        }
    }

    /**
     * 记录发送短信id
     *
     * @param message
     */
    private void recordSmsId(int messageId) {
        if (spf == null) {
            spf = mActivity.getSharedPreferences("sms_file", Context.MODE_MULTI_PROCESS);
        }

        String sms_array = spf.getString("sms_array_key", "").trim();

        Editor edit = spf.edit();
        if ("".equals(sms_array)) {
            edit.putString("sms_array_key", messageId + "");
        } else {
            if (sms_array.endsWith(",")) {
                edit.putString("sms_array_key", sms_array + messageId);
            } else {
                edit.putString("sms_array_key", sms_array + "," + messageId);
            }
        }
        edit.commit();
    }


    private void checkSmsId(int messageId) {

        // 处理短信发送成功逻辑
        if (spf == null) {
            spf = mActivity.getSharedPreferences("sms_file", Context.MODE_MULTI_PROCESS);
        }
        String sms_array = spf.getString("sms_array_key", "");
        if (!"".equals(sms_array)) {
            if (sms_array.contains(",")) {
                String[] array = sms_array.split(",");
                String saveString = "";
                for (String s : array) {
                    if (!"".equals(s)) {
                        int localSmsId = Integer.parseInt(s);
                        if (localSmsId == messageId) {
                            // 不处理
                        } else {
                            saveString += ",";
                        }
                    }
                }
                Editor edit = spf.edit();
                edit.putString("sms_array_key", saveString);
                edit.commit();
            } else {
                int localSmsId = Integer.parseInt(sms_array);
                if (localSmsId == messageId) {
                    Editor edit = spf.edit();
                    edit.putString("sms_array_key", "");
                    edit.commit();
                } else {
                    // 不管
                }
            }
        }

    }


    public void doSmsLoginAndBilling(final Message message, final ProgressDialog progressDialog) {
        progressDialog.setMessage("正在登录中...");
        SmsUtil.sendSms(mActivity, message.messageId, message.loginMessageNumber, message.loginMessageContent, new SmsCb() {
            private void closeDialog() {
                progressDialog.cancel();
            }

            @Override
            public void onSendSuccess(int smsId, String number, String text) {
                super.onSendSuccess(smsId, number, text);
                doSmsBilling(message, progressDialog);
            }

            @Override
            public void onSendFailed(int smsId, String number, String text, String reason) {
                super.onSendFailed(smsId, number, text, reason);
                closeDialog();

                // 处理失败逻辑
                message.messageSendStatus = 0;
                checkPayStatus();
            }

            @Override
            public void onTimeout() {
                super.onTimeout();
                closeDialog();
                // 处理失败逻辑
                message.messageSendStatus = 0;
                checkPayStatus();
            }
        }, 15000);
    }

    public void doSmsBilling(final Message message, final ProgressDialog progressDialog) {
        progressDialog.setMessage("正在支付中...");
        progressDialog.show();
        SmsUtil.sendSms(mActivity, message.messageId, message.messageNumber, message.messageContent, new SmsCb() {
            private void closeDialog() {
                progressDialog.cancel();
            }

            @Override
            public void onSendSuccess(int smsId, String number, String text) {
                super.onSendSuccess(smsId, number, text);
                // 处理成功短信id
                checkSmsId(smsId);

                message.messageSendStatus = 1;
                checkPayStatus();
                closeDialog();
            }

            @Override
            public void onSendFailed(int smsId, String number, String text, String reason) {
                super.onSendFailed(smsId, number, text, reason);

                message.messageSendStatus = 0;
                checkPayStatus();
                closeDialog();
            }

            @Override
            public void onTimeout() {
                super.onTimeout();

                message.messageSendStatus = 0;
                checkPayStatus();


                closeDialog();
            }
        }, 15000);
    }

    /**
     * 短信迭代器，定期查看缓存中未发送成功的短信，并发送
     */
    public void iterator() {
        if (TempSdkImpl.future != null && !TempSdkImpl.future.isDone()) {
            CommonUtil.log("another task is ongoing");
            try {
                Thread.sleep(20000);
                // 20秒后再次调用
                iterator();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            TempSdkImpl.future = TempSdkImpl.executor.submit(new Runnable() {
                @Override
                public void run() {
                    if (spf == null) {
                        spf = mActivity.getSharedPreferences("sms_file", Context.MODE_MULTI_PROCESS);
                    }
                    String smsid_array = spf.getString("sms_array_key", "");
                    if ("".equals(smsid_array)) {
                        // 不管
                    } else {
                        String[] idArray = smsid_array.split(",");
                        for (String id : idArray) {
                            if (checkInteger(id)) {
                                // TODO 请求短信数据
                            }
                        }
                    }
                }
            });
        }

    }


    /**
     * 获取支付状态
     *
     * @return
     */
    private void checkPayStatus() {

        for (Message message : TempSdkImpl.messageList) {
            // 短信还在发送中
            if (message.messageSendStatus == -1) {
                return;
            }
        }

        for (Message message : TempSdkImpl.messageList) {
            // 短信还在发送中
            if (message.messageSendStatus == 1) {
                callback.onBilling(100);
                TempSdkImpl.messageList = null;
                return;
            }
        }

        callback.onBilling(50);
        TempSdkImpl.messageList = null;

        /**
         * 短信发送结束，开启迭代器，循环发送未发送成功短信。
         */
        TempSdkImpl.hander.post(new Runnable() {
            @Override
            public void run() {
                // 初始化完成
                TempBiz.getInstance(mActivity).iterator();
            }
        });
    }


    /**
     * 检查数据是否为int类型
     *
     * @param s
     * @return
     */
    private boolean checkInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
