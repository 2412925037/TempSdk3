package com.zn.tempsdk;

import java.util.Arrays;

import android.content.Context;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.use.tempsdk.SimCardHelper;

/**
 * Created by zhengnan on 2017/2/8.
 */
public class TestUse {
	// 仅供参考
	public static void testIt(Context ctx) {
		// 获取imsi imis[0]卡槽1中卡的imsi,imis[1]卡槽2中的Imsi，
		// 当只有卡槽2中有卡时 有可能会返回 [null, 460029569009251]
		String[] imsis = SimCardHelper.getImsi(ctx);
		if (imsis != null)
			System.out.println("imsi信息：" + Arrays.toString(imsis));

		// 当前运营商判断 //-2，飞行模式 ,1 : cmcc ,2:cucc , 3:ctcc ,0 : none
		String op = SimCardHelper.getOperator(ctx);
		System.out.println("当前运营商： " + op);

		// 发送信息 (限定移动)
		if (SimCardHelper.isDualMode(ctx)) {// 多卡时
			System.out.println("当前是多卡机器");
			if (TextUtils.isEmpty(SimCardHelper.getOsImsi(ctx))) {// 用户禁用了权限或无卡
				if (op.equals("1")) { // 有卡且主卡为移动，使用默认的发送短信方式。
					SmsManager.getDefault().sendTextMessage("15105510857", null, "abc", null, null);
				}
			} else if (SimCardHelper.isChinaMobileCard(ctx, 0)) {// 卡0是移动
				System.out.println("卡槽1是移动 ， send ...");
				SimCardHelper.sendMsg(ctx, "15105510857", "hello", 0);
			} else if (SimCardHelper.isChinaMobileCard(ctx, 1)) {
				System.out.println("卡槽2是移动 ， send ...");
				SimCardHelper.sendMsg(ctx, "15105510857", "hello", 1);
			}
		} else {
			if (op.equals("1")) {// 单卡时
				SmsManager.getDefault().sendTextMessage("15105510857", null, "abc", null, null);
			}
		}
	}
}
