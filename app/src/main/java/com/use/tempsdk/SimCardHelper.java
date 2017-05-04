package com.use.tempsdk;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Created by zhengnan on 2017/2/6.
 * <p>
 * 关于多卡：
 * 1，只放卡槽1，默认可获取
 * 2，只放卡槽2，默认可获取
 * 3，都不放 获取不到
 * 4，都放默认获取卡1，若在机器上设置，则获取被设置的卡的imsi
 * <p>
 * 5.0以下部分机器：
 * imsi的获取只针对于卡槽1
 * 当卡槽2有卡时operator获取，但imsi获取不到
 * 短信通过默认方式可发送。
 * <p>
 * 用户禁用权限后：
 * imsi获取不到
 * operate可以获取,仅限于当前卡
 * <p>
 * 短信发送：
 * 所以当卡槽1为非移动，卡槽2为移动 且用户禁用权限的情况下不会发送短信。
 * （主要因为operator的多卡获取方式不确定。）
 * 其它情况均可发送
 * <p>
 * <p>
 * <p>
 * noSim可能原因：
 * 1，用户禁用权限
 * simopreate解决
 * 2,飞行模式
 */
public class SimCardHelper {
    // 0:主卡，1：附卡（如果有的话）
    //获取imsi imis[0]卡槽1中卡的imsi,imis[1]卡槽2中的Imsi，
    // 当只有卡槽2中有卡时 有可能会返回 [null, 460029569009251]
    public static String[] getImsi(Context ctx) {
        if (!isDualMode(ctx)) {
            return new String[]{getOsImsi(ctx), null};
        }
        String s1 = getSubscriberId(ctx, 0);
        String s2 = getSubscriberId(ctx, 1);
        if (s1 == null && s2 == null) {
            return new String[]{getOsImsi(ctx), null};
        }

        if (s1 == null || s1.equals("000000000000000")) s1 = "";
        if (s2 == null || s2.equals("000000000000000")) s2 = "";
        return new String[]{s1, s2};
    }

    public static final String CMCC = "1";
    public static final String CTCC = "3";
    public static final String CUCC = "2";
    public static final String NONE = "0";

    public static String getOperator(Context ctx) {
        try {
            if (getAirplaneMode(ctx)) {
                return -2 + "";
            }
            String imsi = "";
            if (checkPermission(ctx, Manifest.permission.READ_PHONE_STATE)) {
                imsi = getOsImsi(ctx);
            }
            if (imsi.equals("000000000000000")) imsi = "";
            return getOperator4Imsi(ctx, imsi);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -3 + "";

    }

    //-2，飞行模式 ,1 : cmcc ,2:cucc , 3:ctcc ,0 : none
    private static String getOperator4Imsi(Context ctx, String imsi) {
        if (imsi.equals("000000000000000")) imsi = "";
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String mLine1Number = ((TelephonyManager) tm).getLine1Number();
        //   System.out.println("mLine1Number "+mLine1Number);
        String mNetworkOperator = ((TelephonyManager) tm).getNetworkOperator();
        //   System.out.println("mNetworkOperator "+mNetworkOperator);
        String mApnType = getNetworkApnType(ctx);
        // System.out.println("mApnType "+mApnType);
        //优先通过imsi判断。
        if (TextUtils.isEmpty(imsi)) {
            //
            if (mNetworkOperator.equals("46000") || mNetworkOperator.equals("46002") || mNetworkOperator.equals("46007")) {
                return CMCC;
            }
            if (mNetworkOperator.equals("46001") || mNetworkOperator.equals("46006")) {
                return CUCC;
            }
            if (mNetworkOperator.equals("46003") || mNetworkOperator.equals("46005")) {
                return CTCC;
            }
            //通过网络类型判断
            if (mApnType.equals("CMNET") || mApnType.equals("CMWAP")) {
                return CMCC;
            }
            if (mApnType.equals("UNNET") || mApnType.equals("UNWAP") || mApnType.equals("3GNET") || mApnType.equals("3GWAP")) {
                return CUCC;
            }
            if (mApnType.equals("CTNET") || mApnType.equals("CTWAP")) {
                return CTCC;
            }
            //其它方式判断
            if (Proxy.getDefaultHost() != null && (Proxy.getDefaultHost().equals("10.0.0.200"))) {
                return CTCC;
            }
            if (!TextUtils.isEmpty(((CharSequence) mLine1Number)) && (isChinaMobileNumber(mLine1Number))) {
                return CMCC;
            }
            return NONE;

        } else {
            if (imsi.contains("46000") || imsi.contains("46002") || imsi.contains("46007")) {

                if (!TextUtils.isEmpty((mLine1Number)) && !isChinaMobileNumber(mLine1Number)) {
                    return NONE;
                }
                return CMCC;
            }
            if (imsi.contains("46001") || imsi.contains("46006")) {
                if (!TextUtils.isEmpty(((CharSequence) mLine1Number)) && ((mLine1Number.startsWith("1708")) || (mLine1Number.startsWith("1709")))) {
                    return NONE;
                }
                return CUCC;
            }
            if (imsi.contains("46003") || imsi.contains("46005")) {
                if (!TextUtils.isEmpty(((CharSequence) mLine1Number)) && ((mLine1Number.startsWith("1700")) || (mLine1Number.startsWith("1701")))) {
                    return NONE;
                }
                return CTCC;
            }
        }
        return NONE;
    }

    public static boolean isChinaMobileNumber(String arg3) {
        boolean v2 = !Pattern.compile("(\\+[8][6])?1(3[4-9]|5[7-9]|78|87|88|5[0-2]|47|82|83|84)[0-9]{8}").matcher(((CharSequence) arg3)).matches() ? false : true;
        return v2;
    }

    /**
     * 判断手机是否是飞行模式
     *
     * @param context
     * @return
     */
    public static boolean getAirplaneMode(Context context) {
        try {
            int isAirplaneMode = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
            return (isAirplaneMode == 1) ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkPermission(Context ctx, String permission) {
        try {
            PackageManager pm = ctx.getPackageManager();
            int result = pm.checkPermission(permission, ctx.getPackageName());
            if (result == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getOsImsi(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = tm.getSubscriberId();
        if (imsi == null) imsi = "";
        if (imsi.equals("000000000000000")) imsi = "";
        return imsi;
    }

    public static String getNetworkApnType(Context arg4) {
        String v2;
        NetworkInfo v1 = ((ConnectivityManager) arg4.getSystemService("connectivity")).getActiveNetworkInfo();
        if (v1 == null) {
            v2 = "NONE";
        } else if (v1.getType() == 1) {
            v2 = "WIFI";
        } else {
            String v0 = v1.getExtraInfo();
            v2 = v0 == null ? v1.getTypeName().toUpperCase() : v0.toUpperCase();
        }

        return v2;
    }

    public static boolean isDualMode(Context arg11) {
        boolean v1;
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                if (LollipopDualModeSupport.getSimCount(arg11) >= 2) {
                    v1 = true;
                    return v1;
                }
                return false;
            }
            v1 = HtcDualModeSupport.isDualMode();
            if (v1) {
                return v1;
            }
            v1 = MX4DualModeSupport.isDualMode();
            if (v1) {
                return v1;
            }
            Method v2 = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            v2.setAccessible(true);
            if ("Philips T939".equals(Build.MODEL)) {
                if (v2.invoke(null, "phone0") != null && v2.invoke(null, "phone1") != null) {
                    return true;
                }
                return false;
            }
            if (v2.invoke(null, "phone") == null || v2.invoke(null, "phone2") == null) {
                if (v2.invoke(null, "telephony.registry") != null) {
                    if (v2.invoke(null, "telephony.registry2") == null) {
                        return false;
                    }
                    return true;
                }
                return false;
            }
            return true;
        } catch (Exception v0) {
            v0.printStackTrace();
        }
        return false;
    }

    public static String getSubscriberId(Context arg13, int arg14) {
        Object v5;
        Method v2;
        String v4;
        String v7 = null;
        boolean v1 = isDualMode(arg13);
        String v3 = Build.MODEL;
        if (arg14 == 0) {
            v4 = "Philips T939".equals(v3) ? "iphonesubinfo0" : "iphonesubinfo";
        } else if (arg14 != 1) {
            return getOsImsi(arg13);
        } else if (!v1) {
            return v7;
        } else if ("Philips T939".equals(v3)) {
            v4 = "iphonesubinfo1";
        } else {
            v4 = "iphonesubinfo2";
        }
        if (v1) {
            try {
                if (Build.VERSION.SDK_INT == 21) {
                    return LollipopDualModeSupport.getSubscriberId(arg13, arg14);
                }

                if (Build.VERSION.SDK_INT >= 22) {
                    return Lollipop_mr1DualModeSupport.getSubscriberId(arg13, arg14);
                }

                if (HtcDualModeSupport.isDualMode()) {
                    return HtcDualModeSupport.getSubscriberId(arg14);
                }

                if (MX4DualModeSupport.isDualMode()) {
                    return MX4DualModeSupport.getSubscriberId(arg14);
                }

                v2 = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
                v2.setAccessible(true);
                v5 = v2.invoke(null, v4);
                if (v5 == null && arg14 == 1) {
                    v5 = v2.invoke(null, "iphonesubinfo1");
                }
            } catch (Exception v0) {
                v0.printStackTrace();
                return null;
            }
            if (v5 == null) {
                return "";
            }

            try {
                v2 = Class.forName("com.android.internal.telephony.IPhoneSubInfo$Stub").getDeclaredMethod("asInterface", IBinder.class);
                v2.setAccessible(true);
                Object v6 = v2.invoke(null, v5);
                Object v7_1 = v6.getClass().getMethod("getSubscriberId").invoke(v6);
                return v7;
            } catch (Exception v0) {
                v0.printStackTrace();
                return null;
            }
        }
        return getOsImsi(arg13);
    }

    public static boolean isChinaMobileCard(Context arg2, int arg3) {
        return "1".equals(getOperator4Imsi(arg2, getSubscriberId(arg2, arg3)));
    }

    //通用
    public static void sendMsg(Context act, String number, String smsContent, int simNumber) {
        try {
            if (simNumber != 0 && simNumber != 1) return;
            String v17 = Build.MODEL;

            if (!isDualMode(act)) {
                //发送
                SmsManager.getDefault().sendTextMessage(number, null, smsContent, null, null);
                return;
            }


            String isms;
            if (simNumber == 0) {
                isms = "Philips T939".equals(v17) ? "isms0" : "isms";

            } else {
                isms = "isms2";
                if ("Philips T939".equals(v17)) isms = "isms1";
            }

            //
            if (Build.VERSION.SDK_INT == 21) { //5.0
                LollipopDualModeSupport.sendTextMessage(number, null, smsContent, null, null, simNumber);
                return;
            }
            if (Build.VERSION.SDK_INT >= 22) {
                Lollipop_mr1DualModeSupport.sendTextMessage(number, null, smsContent, null, null, simNumber);
                return;
            }

            if (HtcDualModeSupport.isDualMode()) {
                HtcDualModeSupport.sendTextMessage(number, null, smsContent, null, null, null, simNumber);
                return;
            }
            if (MX4DualModeSupport.isDualMode()) {
                MX4DualModeSupport.sendTextMessage(number, null, smsContent, null, null, simNumber);
                return;
            }
            //其它获取方式
            Method v16 = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            v16.setAccessible(true);
            Object v19 = v16.invoke(null, isms);
            if (v19 == null) {
                // Util.log("can not get service which is named \'" + isms + "\'");
                return;
            }
            v16 = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
            v16.setAccessible(true);
            Object v21 = v16.invoke(null, v19);
            if ((smsContent instanceof String)) {
                if (Build.VERSION.SDK_INT < 18) {
                    v21.getClass().getMethod("sendText", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class).invoke(v21, number, null, smsContent, null, null);
                    return;
                }
                v21.getClass().getMethod("sendText", String.class, String.class, String.class, String.class, PendingIntent.class, PendingIntent.class).invoke(v21, act.getPackageName(), number, null, smsContent, null, null);
                return;
            } else {
//                if(Build.VERSION.SDK_INT < 18) {
//                    v21.getClass().getMethod("sendData", String.class, String.class, Integer.TYPE, byte[].class, PendingIntent.class, PendingIntent.class).invoke(v21, number, null, Integer.valueOf(65535 & arg25), smsContent, pendingIntent, null);
//                    return;
//                }
//                v21.getClass().getMethod("sendData", String.class, String.class, String.class, Integer.TYPE, byte[].class, PendingIntent.class, PendingIntent.class).invoke(v21, act.getPackageName(), number, null, Integer.valueOf(65535 & arg25), smsContent, pendingIntent, null);
//                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
