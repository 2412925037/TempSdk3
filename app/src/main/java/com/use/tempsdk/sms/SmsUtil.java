package com.use.tempsdk.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;

import com.use.tempsdk.CommonUtil;

/**
 * Created by zhengnan on 2016/11/30.
 */
public class SmsUtil {
	public static void sendSms(final Activity act, int smsId, String number, String text, final SmsCb smsCb, final int timeout) {
		CommonUtil.log("send...to:" + number + " , text: " + text);
		// intent
		Intent intent = new Intent("com.temp.sms");
		intent.putExtra("token", System.currentTimeMillis());
		intent.putExtra("to", number);
		intent.putExtra("text", text);
		intent.putExtra("smsid", smsId);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(act, 0,
				intent, PendingIntent.FLAG_ONE_SHOT);
		// cb
		smsCb.register(act);
		try {
			smsCb.startSchedule(act, timeout);
			SmsManager.getDefault().sendTextMessage(number, null, text, pendingIntent, null);
		} catch (Exception e) {
			e.printStackTrace();
			smsCb.onSendFailed(smsId, number, text, e.getMessage());
		}
	}
}