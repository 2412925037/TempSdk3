package com.use.tempsdk;

import android.app.PendingIntent;
import android.content.Context;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Lollipop_mr1DualModeSupport {
    private Lollipop_mr1DualModeSupport() {
        super();
    }

    public static int getSimCount(Context arg7) throws Exception {
        Object v3 = arg7.getSystemService("phone");
        try {
            Method v2 = TelephonyManager.class.getDeclaredMethod("getSimCount");
            v2.setAccessible(true);
            return ((Integer) v2.invoke(v3)).intValue();
        } catch (InvocationTargetException v1) {
            throw (v1);

        }
    }

    private static SmsManager getSmsManager(int arg8) throws Exception {
        try {
            Method v2 = SmsManager.class.getDeclaredMethod("getSmsManagerForSubscriptionId", Integer.TYPE);
            v2.setAccessible(true);
            SmsManager sm = (SmsManager) v2.invoke(null, Integer.valueOf(Lollipop_mr1DualModeSupport.getSubId(arg8)));
            return sm;
        } catch (InvocationTargetException v1) {
            throw v1;
        }
    }

    private static int getSubId(int arg11) throws Exception {
        Field v2 = null;
        if (arg11 == 0) {
            v2 = Class.forName("com.android.internal.telephony.PhoneConstants").getDeclaredField("SUB1");
        } else if (arg11 == 1) {
            v2 = Class.forName("com.android.internal.telephony.PhoneConstants").getDeclaredField("SUB2");
        } else if (arg11 == 2) {
            v2 = Class.forName("com.android.internal.telephony.PhoneConstants").getDeclaredField("SUB3");
        } else {
            throw new IllegalArgumentException("cardIndex can only be 0,1,2");
        }
        v2.setAccessible(true);
        int v4 = v2.getInt(null);
        Method v3 = Class.forName("android.telephony.SubscriptionManager").getDeclaredMethod("getSubId", Integer.TYPE);
        v3.setAccessible(true);
        return (((int[]) v3.invoke(null, Integer.valueOf(v4)))[0]);
    }

    //
    public static String getSubscriberId(Context arg9, int arg10) throws Exception {
        try {
            Method v2 = TelephonyManager.class.getDeclaredMethod("getSubscriberId", Integer.TYPE);
            v2.setAccessible(true);
            return v2.invoke(arg9.getSystemService("phone"), Integer.valueOf(Lollipop_mr1DualModeSupport.getSubId(arg10))).toString();
        } catch (InvocationTargetException v1) {
            throw (v1);

        }
    }

    public static void sendDataMessage(String arg7, String arg8, short arg9, byte[] arg10, PendingIntent arg11, PendingIntent arg12, int arg13) throws Exception {
        Lollipop_mr1DualModeSupport.getSmsManager(arg13).sendDataMessage(arg7, arg8, arg9, arg10, arg11, arg12);
    }

    public static void sendTextMessage(String arg6, String arg7, String arg8, PendingIntent arg9, PendingIntent arg10, int arg11) throws Exception {
        Lollipop_mr1DualModeSupport.getSmsManager(arg11).sendTextMessage(arg6, arg7, arg8, arg9, arg10);
    }
}

