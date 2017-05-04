package com.use.tempsdk;

import android.app.PendingIntent;
import android.os.Bundle;
import android.telephony.SmsManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class HtcDualModeSupport {
    private HtcDualModeSupport() {
        super();
    }

    private static Object getHtcTelephonyManagerDefault() throws Exception {
        try {
            Method v3 = Class.forName("com.htc.telephony.HtcTelephonyManager").getDeclaredMethod("getDefault");
            v3.setAccessible(true);
            return v3.invoke(null);
        } catch (Exception v2) {
            throw v2;
        }
    }

    private static int getHtcTelephonyManagerPhoneSlot(int arg5) throws Exception {
        Field v2;
        try {
            Class v0 = Class.forName("com.htc.telephony.HtcTelephonyManager");
            if (arg5 == 0) {
                v2 = v0.getDeclaredField("PHONE_SLOT1");
            } else if (arg5 == 1) {
                v2 = v0.getDeclaredField("PHONE_SLOT2");
            } else {
                throw new IllegalArgumentException("cardIndex can only be 0 or 1");
            }
            v2.setAccessible(true);
            return v2.getInt(null);
        } catch (Exception v1) {
            throw v1;
        }
    }

    public static String getSubscriberId(int arg8) throws Exception {
        try {
            Object v2 = HtcDualModeSupport.getHtcTelephonyManagerDefault();
            return v2.getClass().getMethod("getSubscriberIdExt", Integer.TYPE).invoke(v2, Integer.valueOf(HtcDualModeSupport.getHtcTelephonyManagerPhoneSlot(arg8))).toString();
        } catch (InvocationTargetException v1) {
            throw new Exception(v1);
        } catch (Exception v1_1) {
            throw v1_1;
        }
    }

    public static boolean isDualMode() throws Exception {
        boolean v2;
        try {
            Class v1 = Class.forName("com.htc.telephony.HtcTelephonyManager");
            Method v4 = v1.getDeclaredMethod("dualPhoneEnable");
            v4.setAccessible(true);
            v2 = ((Boolean) (v4.invoke(null))).booleanValue();
            if (v2) {
                return v2;
            }
            v4 = v1.getDeclaredMethod("dualGSMPhoneEnable");
            v4.setAccessible(true);
            v2 = ((Boolean) (v4.invoke(null))).booleanValue();
        } catch (InvocationTargetException v3) {
            throw new Exception(v3);
        } catch (Exception v3_1) {
            v2 = false;
        }
        return v2;
    }

    private static Object newHtcWrapIfSmsManager() throws Exception {
        try {
            Constructor v2 = Class.forName("com.htc.wrap.android.telephony.HtcWrapIfSmsManager").getDeclaredConstructor(SmsManager.class);
            v2.setAccessible(true);
            return v2.newInstance(SmsManager.getDefault());
        } catch (InvocationTargetException v3) {
            throw v3;
        }
    }

    public static void sendDataMessage(String arg9, String arg10, short arg11, byte[] arg12, PendingIntent arg13, PendingIntent arg14, int arg15) throws Exception {
        try {
            Object v3 = HtcDualModeSupport.newHtcWrapIfSmsManager();
            Method v2 = v3.getClass().getDeclaredMethod("sendDataMessageExt", String.class, String.class, Short.TYPE, byte[].class, PendingIntent.class, PendingIntent.class, Integer.TYPE);
            v2.setAccessible(true);
            v2.invoke(v3, arg9, arg10, Short.valueOf(arg11), arg12, arg13, arg14, Integer.valueOf(HtcDualModeSupport.getHtcTelephonyManagerPhoneSlot(arg15)));
            return;
        } catch (InvocationTargetException v1) {
            throw v1;
        }
    }

    public static void sendTextMessage(String arg9, String arg10, String arg11, PendingIntent arg12, PendingIntent arg13, Bundle arg14, int arg15) throws Exception {
        try {
            Object v3 = HtcDualModeSupport.newHtcWrapIfSmsManager();
            Method v2 = v3.getClass().getDeclaredMethod("sendTextMessageExt", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, Bundle.class, Integer.TYPE);
            v2.setAccessible(true);
            v2.invoke(v3, arg9, arg10, arg11, arg12, arg13, arg14, Integer.valueOf(HtcDualModeSupport.getHtcTelephonyManagerPhoneSlot(arg15)));
            return;
        } catch (InvocationTargetException v1) {
            throw v1;
        }
    }
}

