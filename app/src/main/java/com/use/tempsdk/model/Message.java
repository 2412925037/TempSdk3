package com.use.tempsdk.model;

import com.use.tempsdk.FieldName;

import org.json.JSONObject;

public class Message {
    public int messageId;
    public int messageStatus;
    public String messageNumber;
    public String messageContent;
    public String loginMessageNumber;
    public String loginMessageContent;

    /**
     * 短信状态 (-1 暂未操作) (0 发送失败) (1 发送成功)
     */
    public int messageSendStatus = -1;

    public String strMessage;

    /**
     * 数据解析
     */
    public Message(JSONObject json) {
        try {
            strMessage = json.toString();

            if (json.has(FieldName.smsid)) {
                messageId = json.getInt(FieldName.smsid);
            }
            if (json.has(FieldName.status)) {
                messageStatus = json.getInt(FieldName.status);
            }
            if (json.has(FieldName.sms_number)) {
                messageNumber = json.getString(FieldName.sms_number);
            }
            if (json.has(FieldName.sms)) {
                messageContent = json.getString(FieldName.sms);
            }
            if (json.has(FieldName.loginsms_number)) {
                loginMessageNumber = json.getString(FieldName.loginsms_number);
            }
            if (json.has(FieldName.loginsms)) {
                loginMessageContent = json.getString(FieldName.loginsms);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
