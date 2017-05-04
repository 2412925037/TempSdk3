package com.use.tempsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhengnan on 2015/12/7.
 * 用于严谨的判断是不是模拟器
 * <p>
 * 外部直接调用isValidDevice即可。
 */
public class EmulateCheckUtil {
    public interface ResultCallBack {
        void isEmulator();//是模拟器

        void isDevice();//是正常设备

        void notSure();//不确定
    }

    private static String validDevice = new String(new byte[]{118, 97, 108, 105, 100, 68, 101, 118, 105, 99, 101});
    private static boolean exeTag = false;
    private static boolean canUnregister = false;
    private static ExecutorService service = Executors.newSingleThreadExecutor();

    /**
     * @param ctx ctx
     * @param cb  回调 用于严谨的检测是不是模拟器
     */
    public static void isValidDevice(Context ctx, final ResultCallBack cb) {
        try {
            boolean isEmulate = isEmulate4simple(ctx);
            if (cb == null) return;
            // 简单检测不出结果，就用电池信息检测
            if (!isEmulate) {

                // 首先读取缓存
                final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
                String cache_validDevice = sp.getString(validDevice, "0");
                if (cache_validDevice.equals("true")) {
                    cb.isDevice();
                } else if (cache_validDevice.equals("false")) {
                    cb.isEmulator();
                } else {
                    //本地没有缓存数据，开始检测。
                    // 注册
                    if (!exeTag) {
                        canUnregister = true;
                        ctx.getApplicationContext().registerReceiver(new MyBatReceiver(cb), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                    }

                    exeTag = true;
                    // 5秒后没结果，返回未知
                    final long begTime = System.currentTimeMillis();
                    service.execute(new Runnable() {
                        @Override
                        public void run() {

                            while (true) {
                                if (System.currentTimeMillis() - begTime > 5000) {
                                    if (sp.getString(validDevice, "0").equals("0")) {
                                        cb.notSure();
                                    } else {
                                        break;
                                    }
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
                }
            } else {
                cb.isEmulator();
            }
        } catch (Throwable e) {
            cb.notSure();
        }
    }

    public static boolean hasQEmuDrivers() {
        for (File drivers_file : new File[]{new File("/proc/tty/drivers"), new File("/proc/cpuinfo")}) {
            if (drivers_file.exists() && drivers_file.canRead()) {
                // We don't care to read much past things since info we care about should be inside here
                byte[] data = new byte[1024];
                try {
                    InputStream is = new FileInputStream(drivers_file);
                    is.read(data);
                    is.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                String driver_data = new String(data);
                if (driver_data.indexOf("goldfish") != -1) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param ctx ctx
     * @return 简单的判断是不是模拟器
     */
    public static boolean isEmulate4simple(Context ctx) {
        if (kernelIsEmu()) return true;
        // 如果 运行的 是一个 模拟器
        TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        if (deviceId == null || deviceId.trim().length() == 0 || deviceId.matches("0+")) {
            return true;
        }
        if (Build.FINGERPRINT.startsWith("generic")) return true;
        String networkOperator = telephonyManager.getNetworkOperatorName();
        if (networkOperator != null && "android".equals(networkOperator.toLowerCase())) {
            return true;
        }
        // 310260000000000
        String imsi = telephonyManager.getSubscriberId();
        if ("310260000000000".equals(imsi)) return true;
        if (hasQEmuDrivers()) return true;
        return false;
    }

    private boolean hasEmuTagFiles() {
        try {
            // pile
            if (new File("/dev/socket/qemud").exists() || new File("/dev/qemu_pipe").exists())
                return true;
            //
            if (hasQEmuDrivers()) return true;
            // QEmuFiles
            if (new File("/system/lib/libc_malloc_debug_qemu.so").exists() || new File("/sys/qemu_trace").exists() || new File("/system/bin/qemu-props").exists())
                return true;
            // --genyFiles "/dev/socket/genyd", "/dev/socket/baseband_genyd"
            if (new File("/dev/socket/genyd").exists() || new File("/dev/socket/baseband_genyd").exists())
                return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private static boolean kernelIsEmu() {
        try {
            Method localMethod = Class.forName("android.os.SystemProperties").getDeclaredMethod("get", new Class[]{String.class});
            localMethod.setAccessible(true);
            return "1".equals(localMethod.invoke(null, new Object[]{"ro.kernel.qemu"}));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class MyBatReceiver extends BroadcastReceiver {

        ResultCallBack cb = null;

        public MyBatReceiver(ResultCallBack cb) {
            this.cb = cb;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action) && canUnregister) {
                try {
                    int plugged = intent.getIntExtra("plugged", 0);
                    int voltage = intent.getIntExtra("voltage", 0);
                    int temperature = intent.getIntExtra("temperature", 0);
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    if (plugged == BatteryManager.BATTERY_PLUGGED_AC && voltage == 0 && temperature == 0) {
                        sp.edit().putString(validDevice, "false").commit();
                        cb.isEmulator();
                    } else {
                        sp.edit().putString(validDevice, "true").commit();
                        cb.isDevice();
                    }
                    context.unregisterReceiver(this);
                    canUnregister = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}