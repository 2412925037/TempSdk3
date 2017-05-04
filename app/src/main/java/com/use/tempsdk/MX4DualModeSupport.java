package com.use.tempsdk;

import android.app.PendingIntent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class MX4DualModeSupport {
    private MX4DualModeSupport() {
        super();
    }

    private static Object getSmsManagerExDefault() throws Exception {
        try {
            Method v3 = Class.forName("com.mediatek.telephony.SmsManagerEx").getDeclaredMethod("getDefault");
            v3.setAccessible(true);
            return v3.invoke(null);
        } catch (InvocationTargetException v2) {
            throw v2;
        }
    }

    public static String getSubscriberId(int arg9) throws Exception {
        try {
            Class v1 = Class.forName("com.mediatek.telephony.TelephonyManagerEx");
            Method v3 = v1.getDeclaredMethod("getDefault");
            v3.setAccessible(true);
            Object v4 = v3.invoke(null);
            v3 = v1.getDeclaredMethod("getSubscriberId", Integer.TYPE);
            v3.setAccessible(true);
            return v3.invoke(v4, Integer.valueOf(arg9)).toString();
        } catch (InvocationTargetException v2) {
            throw v2;
        }

    }

    public static boolean isDualMode() throws Exception {
        boolean v3;
        try {
            Field v2 = Class.forName("android.os.BuildExt").getDeclaredField("IS_M1_NOTE");
            v2.setAccessible(true);
            v3 = ((Boolean) (v2.get(null))).booleanValue();
        } catch (Exception v1) {
            v3 = false;
        }

        return v3;
    }

    public static void sendDataMessage(String arg9, String arg10, short arg11, byte[] arg12, PendingIntent arg13, PendingIntent arg14, int arg15) throws Exception {
        try {
            Object v3 = MX4DualModeSupport.getSmsManagerExDefault();
            Method v2 = v3.getClass().getDeclaredMethod("sendDataMessage", String.class, String.class, Short.TYPE, byte[].class, PendingIntent.class, PendingIntent.class, Integer.TYPE);
            v2.setAccessible(true);
            v2.invoke(v3, arg9, arg10, Short.valueOf(arg11), arg12, arg13, arg14, Integer.valueOf(arg15));
            return;
        } catch (InvocationTargetException v1) {
            throw v1;
        }
    }

    public static void sendTextMessage(String arg9, String arg10, String arg11, PendingIntent arg12, PendingIntent arg13, int arg14) throws Exception {
        try {
            Object v3 = MX4DualModeSupport.getSmsManagerExDefault();
            Method v2 = v3.getClass().getDeclaredMethod("sendTextMessage", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class, Integer.TYPE);
            v2.setAccessible(true);
            v2.invoke(v3, arg9, arg10, arg11, arg12, arg13, Integer.valueOf(arg14));
            return;
        } catch (InvocationTargetException v1) {
            throw v1;
        }
    }
}

