package com.use.tempsdk;

import com.winter.mdm.CallBack;
import com.winter.mdm.MMLogManager;

import android.app.Activity;

/**
 * Created by zhengnan on 2016/11/30.
 */
public class TempSdkFace {

	private final static int TYPE = 3;

	private static Object logObj;

	/**
	 * 初始化
	 * 
	 * @param act
	 * @param cpId
	 * @param cpChannelid
	 * @param cpGameId
	 * @param price
	 * @param cpParam
	 * @param cb
	 */
	public static void init(final Activity act, final String cpId, final String cpChannelid, final String cpGameId,
			final TempSdkFace.InitCb cb) {
		try {
			logObj = MMLogManager.getLogObj(TYPE);
			if (logObj != null) {
				// final Activity act, final String cpId, final String
				// cpChannelid, final String cpGameId, final TempSdkFace.InitCb
				// cb
				Object classes = new Class[] { Activity.class, String.class, String.class, String.class,
						TempSdkFace.InitCb.class };
				Object[] params = new Object[] { act, cpId, cpChannelid, cpGameId, cb };
				MMLogManager.doLog(logObj, "com.use.tempsdk.TempSdkImpl", "init", null, classes, params);
			} else {
				MMLogManager.setLogCb(TYPE, new CallBack() {

					@Override
					public void onCallback(Object arg0, int type, int status) {
						if (type != TYPE) {
							cb.onResult(303);
							return;
						}
						if (arg0 != null && status == MMLogManager.MODULE_LOADED) {
							Object classes = new Class[] { Activity.class, String.class, String.class, String.class,
									TempSdkFace.InitCb.class };
							Object[] params = new Object[] { act, cpId, cpChannelid, cpGameId, cb };
							try {
								logObj = arg0;
								MMLogManager.doLog(logObj, "com.use.tempsdk.TempSdkImpl", "init", null, classes,
										params);
							} catch (Throwable e) {
								e.printStackTrace();
								cb.onResult(303);
							}
						} else {
							cb.onResult(303);
						}
					}
				});
			}

		} catch (Throwable e) {
			e.printStackTrace();
			cb.onResult(303);
		}

	}

	public static void doBilling(Activity act, int price, String cpParam, TempSdkFace.DoBillingCb cb) {
		if (logObj == null) {
			cb.onBilling(998);
		} else {
			// final Activity act, int price, String cpParam, final
			// TempSdkFace.DoBillingCb cb
			Object classes = new Class[] { Activity.class, int.class, String.class, TempSdkFace.DoBillingCb.class };
			Object[] params = new Object[] { act, price, cpParam, cb };
			try {
				MMLogManager.doLog(logObj, "com.use.tempsdk.TempSdkImpl", "doBilling", null, classes, params);
			} catch (Throwable e) {
				e.printStackTrace();
				cb.onBilling(998);
			}
		}
	}

	public interface DoBillingCb {
		// 100:成功，其它:失败
		void onBilling(int code);
	}

	public interface InitCb {
		// 300:成功，其它:失败
		void onResult(int code);
	}
}
