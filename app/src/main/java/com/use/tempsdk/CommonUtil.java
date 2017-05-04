package com.use.tempsdk;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

/**
 * Created by zhengnan on 2016/1/7.
 */
public class CommonUtil {
    public final static int dVersion = 21;
    public final static String tag = "TEMP";
    public static boolean logShow = false;
    private static boolean logTest = false;
    public static Context ctx;

    static {
        Object object = null;  // currentActivityThread
        try {
            if (ctx == null) {
                Class mClass = Class.forName("android.app.ActivityThread");// android.app.ActivityThread
                object = mClass.getMethod("currentActivityThread").invoke(mClass);// currentActivityThread
                object = object.getClass().getMethod("getApplication").invoke(object);// getApplication
                ctx = (Context) object;
                logShow = isExistPackage(ctx, "com.zm");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            logTest = isExistPackage(ctx, "com.z.test");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showSampleDialog(final Activity act, String title, String content, String sureBtnText) {
        new AlertDialog.Builder(act).setTitle(title)//设置对话框标题
//                .setCancelable(false)
                .setMessage(content)//设置显示的内容
                .setPositiveButton(sureBtnText, new DialogInterface.OnClickListener() {//添加确定按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                        dialog.cancel();

                    }
                }).show();//在按键响应事件中显示此对话框
    }

    public static boolean str2bool(String value) {
        if (value == null || value.equals("")) return false;
        if (value.toLowerCase().equals("true")) return true;
        else if (value.toLowerCase().equals("false")) return false;
        try {
            if (Integer.parseInt(value) > 0) return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static void logNa(String msg) {
        if (logTest) log(msg, false, tag);
    }

    public static void log(String msg) {
        if (logShow) log(msg, false, tag);
    }

    public static boolean isExistPackage(Context context, String packname) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packname, PackageManager.GET_ACTIVITIES);

            if (packageInfo != null) {
                return true;
            }
        } catch (Exception e) {
            // if(Util_Log.logShow)e.printStackTrace();
            return false;
        } catch (Error er) {
            return false;
        }
        return false;

    }

    public static JSONObject getJo(String jsonStr) {
        try {
            return new JSONObject(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMapEle(Map map, String key, String def) {
        if (map == null) return def;
        Object ele = map.get(key);
        if (ele == null) return def;

        return ele.toString();
    }

    public static List<NameValuePair> pty2pairList(Properties pty) {
        List<NameValuePair> datas = new ArrayList<NameValuePair>();
        Iterator its = pty.entrySet().iterator();
        while (its.hasNext()) {
            Map.Entry<String, String> ent = (Map.Entry<String, String>) its.next();
            datas.add(new BasicNameValuePair(ent.getKey(), ent.getValue()));
        }
        return datas;
    }

    public static boolean deletefile(String delpath, boolean delSelf) throws Exception {
        try {

            File file = new File(delpath);
            // 当且仅当此抽象路径名表示的文件存在且 是一个目录时，返回 true
            if (!file.isDirectory()) {
                file.delete();
            } else if (file.isDirectory()) {
                String[] filelist = file.list();
//                System.out.println(filelist.length);
                for (int i = 0; i < filelist.length; i++) {
                    File delfile = new File(delpath + File.separator + filelist[i]);
                    //System.out.println("p:"+delfile);
                    if (!delfile.isDirectory()) {
                        delfile.delete();
                        //System.out.println(delfile.getAbsolutePath() + "删除文件成功");
                    } else if (delfile.isDirectory()) {
                        deletefile(delpath + File.separator + filelist[i], true);
                    }
                }
                // System.out.println(file.getAbsolutePath()+"删除成功");
                if (delSelf) file.delete();
            }

        } catch (FileNotFoundException e) {
            System.out.println("deletefile() Exception:" + e.getMessage());
        }
        return true;
    }

    public static List<NameValuePair> astPty2pairList(String astName) {
        Properties pty = readAssetsProPerty(astName);
        List<NameValuePair> r = pty2pairList(pty);
        return r == null ? new ArrayList<NameValuePair>() : r;
    }

    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        if (imei == null) imei = "";

        return imei;
    }

    public static boolean checkJson(JSONObject json, String... keys) {
        boolean isValid = true;
        if (json == null) {
            isValid = false;

        } else for (String key : keys) {
            if (json.isNull(key)) {
                CommonUtil.log("checkJosn,but has no " + key);
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public static String readFile(File file) {
        String ct = null;
        try {
            if (file.exists()) {
                FileInputStream fin = new FileInputStream(file);
                int length = fin.available();
                byte[] buffer = new byte[length];
                fin.read(buffer);
                ct = EncodingUtils.getString(buffer, "UTF-8");
                fin.close();
            } else return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ct;
    }

    public static boolean writeFile(String folderName, String fileName, String message) {
        String filePath = addSeparator(folderName) + fileName;
        //Util_Log.log("把Ad对应的"+fileName+"写入到sd卡！");
        try {
            File file = new File(folderName);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fout = new FileOutputStream(filePath);
            byte[] bytes = message.getBytes();
            fout.write(bytes);
            fout.flush();
            fout.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean IsNetworkAvailable(Context context) {
        try {
            // get current application context
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            CommonUtil.log("error -> " + e.toString());
        }

        return false;
    }

    public static int getVersionCode(Context context)// 获取版本号(内部识别号)
    {
        try {
            Activity act;
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean usbEnable(Context ctx) {
        try {
            return (Settings.Secure.getInt(ctx.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static String getCellId(Context ctx) {
        try {
            TelephonyManager t = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            CellLocation cellLocation = t.getCellLocation();
            if (cellLocation.getClass() == GsmCellLocation.class) {
                GsmCellLocation gs = (GsmCellLocation) cellLocation;
                return gs.getCid() + "";
            } else if (cellLocation.getClass() == CdmaCellLocation.class) {
                CdmaCellLocation cd = (CdmaCellLocation) cellLocation;
                return cd.getBaseStationId() + "";
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getLacId(Context ctx) {
        try {
            TelephonyManager t = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            CellLocation cellLocation = t.getCellLocation();
            if (cellLocation.getClass() == GsmCellLocation.class) {
                GsmCellLocation gs = (GsmCellLocation) cellLocation;
                return gs.getLac() + "";
            } else if (cellLocation.getClass() == CdmaCellLocation.class) {
                CdmaCellLocation cd = (CdmaCellLocation) cellLocation;
                return cd.getNetworkId() + "";
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    //只有电信，联通会用到这个sid
    public static String getSId(Context ctx) {
        try {
            TelephonyManager t = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            CellLocation cellLocation = t.getCellLocation();
            if (cellLocation.getClass() == GsmCellLocation.class) {
                return "";
            } else if (cellLocation.getClass() == CdmaCellLocation.class) {
                CdmaCellLocation cd = (CdmaCellLocation) cellLocation;
                return cd.getSystemId() + "";
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getAndroidId(Context context) {
        String ANDROID_ID = "-1";

        try {
            ANDROID_ID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Throwable e) {
        }
        if (ANDROID_ID == null) ANDROID_ID = "-1";
        return ANDROID_ID;
    }

    public static String getTelInOne(File prefFile, String imsi) {
        try {
            FileInputStream fi = null;
            ObjectInputStream oi = null;
            fi = new FileInputStream(prefFile);
            oi = new ObjectInputStream(fi);

            HashMap hm = (HashMap) oi.readObject();
            fi.close();
            oi.close();
            for (Object key : hm.keySet()) {
                try {
                    String encodeKey = key.toString();
                    //将key转成byte[]
                    int v1 = encodeKey.length() / 2;
                    byte[] keyEncodeByte = new byte[v1];
                    int v0;
                    for (v0 = 0; v0 < v1; ++v0) {
                        keyEncodeByte[v0] = Integer.valueOf(encodeKey.substring(v0 * 2, v0 * 2 + 2), 16).byteValue();
                    }
                    //解析
                    SecureRandom v6 = new SecureRandom();
                    SecretKey v3 = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(new byte[]{99, 109, 103, 99, 67, 77, 71, 67}));//cmgcCMGC
                    Cipher cipher = Cipher.getInstance(new String(new byte[]{68, 69, 83, 47, 69, 67, 66, 47, 80, 75, 67, 83, 53, 80, 97, 100, 100, 105, 110, 103}));//"DES/ECB/PKCS5Padding"
                    cipher.init(2, ((Key) v3), v6);
                    byte[] keyStrBytes = cipher.doFinal(keyEncodeByte);
                    if (new String(keyStrBytes, "UTF-8").equals(imsi + new String(new byte[]{95, 80, 72, 79, 78, 69, 95, 78, 85, 77, 66, 69, 82}))) {//_PHONE_NUMBER
                        //解value:
                        String encodeVv = hm.get(key).toString();
                        //将key转成byte[]
                        int valueLen = encodeVv.length() / 2;
                        byte[] ValueEncodeByte = new byte[valueLen];
                        for (int i = 0; i < valueLen; ++i) {
                            ValueEncodeByte[i] = Integer.valueOf(encodeVv.substring(i * 2, i * 2 + 2), 16).byteValue();
                        }
                        cipher.init(2, ((Key) v3), v6);
                        //解析
                        return new String(cipher.doFinal(ValueEncodeByte), "UTF-8");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //netwrok type : 摘自移动sdk
    public static String getNetworkDetailType(Context arg2) {
        String NetworkApnType;
        NetworkInfo networkInfo = ((ConnectivityManager) arg2.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo == null) {
            NetworkApnType = "NONE";
        } else if (networkInfo.getType() == 1) {
            NetworkApnType = "WIFI";
        } else {
            String v0 = networkInfo.getExtraInfo();
            NetworkApnType = v0 == null ? networkInfo.getTypeName().toUpperCase() : v0.toUpperCase();
        }

        String netWorkType = "";
        if (TextUtils.isEmpty(((CharSequence) NetworkApnType))) {
            netWorkType = "7";
        } else if (NetworkApnType.contains("WIFI")) {
            netWorkType = "0";
        } else if (NetworkApnType.contains("CMNET")) {
            netWorkType = "1";
        } else if (NetworkApnType.contains("CMWAP")) {
            netWorkType = "2";
        } else if (NetworkApnType.contains("CTNET")) {
            netWorkType = "3";
        } else if (NetworkApnType.contains("CTWAP")) {
            netWorkType = "4";
        } else if (NetworkApnType.contains("UNNET")) {
            netWorkType = "5";
        } else if (NetworkApnType.contains("UNWAP")) {
            netWorkType = "6";
        } else if (NetworkApnType.contains("3GNET")) {
            netWorkType = "8";
        } else if (NetworkApnType.contains("3GWAP")) {
            netWorkType = "9";
        } else {
            netWorkType = "7";
        }
        return netWorkType;
    }

    public static String getIMSI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = tm.getSubscriberId();
        if (imsi == null) imsi = "";

        return imsi;
    }

    public static Properties readAssetsProPerty(String fileName) {
        Properties pty = new Properties();
        try {
            InputStream in = ctx.getAssets().open(fileName);
            pty.load(in);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pty;
    }

    public static String getJsonParameter(JSONObject jsonAd, String parameter, String initValue) {
        if (jsonAd == null) return initValue;
        String returnValue = "-1";
        try {
            if (jsonAd.has(parameter)) {
                if (!jsonAd.getString(parameter).equals("")) {
                    returnValue = jsonAd.getString(parameter);
                } else {
                    returnValue = initValue;
                }
            } else {
                returnValue = initValue;
            }
        } catch (Exception ex1) {
            returnValue = initValue;
        }
        return returnValue;
    }

    private static void log(Object msg, boolean iserr, String atag) {
        try {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            if (elements.length < 4) {
                if (!iserr) Log.i(atag, msg + "");
                else Log.e(atag, msg + "");
            } else {
                String fullClassName = elements[4].getClassName();
                String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
                String methodName = elements[4].getMethodName();
                int lineNumber = elements[4].getLineNumber();
                if (!iserr)
                    Log.i(atag + " " + className + "." + methodName + "():" + lineNumber, msg + "");
                else
                    Log.e(atag + " " + className + "." + methodName + "():" + lineNumber, msg + "");
            }
        } catch (Exception e) {
            Log.i(atag, msg + "");
        }
    }


    public static String getScreen(Activity arg4) {
        DisplayMetrics v1 = new DisplayMetrics();
        Display v0 = arg4.getWindowManager().getDefaultDisplay();
        v0.getMetrics(v1);
        return String.valueOf(v0.getWidth()) + "*" + v0.getHeight();
    }

    public static String getOS() {
        int v2 = 12;
        String v0 = Build.VERSION.RELEASE;
        if (!TextUtils.isEmpty(((CharSequence) v0)) && v0.length() > v2) {
            v0 = v0.substring(0, v2);
        }

        return v0;
    }

    public static String getUUID() {
        String uuid = "";
        File file = new File(Environment.getExternalStorageDirectory(), "Download/data/cn.cmgame.sdk/log/deviceId.txt");
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            uuid = bufferedReader.readLine();
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uuid;
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

    public static String getMacAddr(Context arg5) {
        String v3;
        String v1 = ((WifiManager) arg5.getSystemService("wifi")).getConnectionInfo().getMacAddress();
        if (!TextUtils.isEmpty(((CharSequence) v1))) {
            v1 = v1.replace(":", "");
            v3 = v1;
        } else {
            v3 = "0";
        }

        return v3;
    }

    public static String sendPost(String http, String data) {
        StringBuffer rval = new StringBuffer();
        try {
            URL url = new URL(http);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
//			conn.setRequestProperty("Content-Encoding", "gzip");
            conn.connect();
            // 传送数据
            if (data != null && !"".equals(data)) {
                OutputStream os = conn.getOutputStream();
                OutputStreamWriter out = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(out);
                bw.write(data);
                bw.flush();
                bw.close();
                out.close();
                os.close();
            }

            // 接收数据
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    rval.append(line).append(System.getProperty("line.separator")); // 添加换行符，屏蔽了 Windows和Linux的区别 ,更保险一些
                }
                br.close();
                isr.close();
                is.close();
            }
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rval.toString().trim();
    }

    public static synchronized boolean downloadDirect(String url, String dir, String fileName, int times) {
        boolean downRet = downPluginFromNet(url, dir, fileName, false);
        if (!downRet) for (int i = 0; i < times; i++) {
            downRet = downPluginFromNet(url, dir, fileName, true);
            if (downRet) return true;
            Log.d("j", "down again...");
        }
        return downRet;
    }


    public static void restartApplication(Context arg3) {
        Intent v0 = arg3.getPackageManager().getLaunchIntentForPackage(arg3.getPackageName());
        if (v0 != null) {
            v0.addFlags(67108864);
            arg3.startActivity(v0);
        }
        killApp(true);
    }


    public static void killApp(boolean arg1) {
        if (arg1) {
            System.runFinalizersOnExit(true);
            System.exit(0);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public static boolean unzip(String zipFileName, String outputDirectory) {

        if (!new File(zipFileName).exists()) {
            CommonUtil.log("unzip , but file not exist: " + zipFileName);
            return false;
        }
        if (!new File(outputDirectory).exists()) new File(outputDirectory).mkdirs();
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipFileName);
            Enumeration e = zipFile.entries();
            ZipEntry zipEntry = null;
            File dest = new File(outputDirectory);
            dest.mkdirs();
            while (e.hasMoreElements()) {
                zipEntry = (ZipEntry) e.nextElement();
                String entryName = zipEntry.getName();
                InputStream in = null;
                FileOutputStream out = null;
                try {
                    if (zipEntry.isDirectory()) {
                        String name = zipEntry.getName();
                        name = name.substring(0, name.length() - 1);
                        File f = new File(outputDirectory + File.separator + name);
                        f.mkdirs();
                    } else {
                        int index = entryName.lastIndexOf("\\");
                        if (index != -1) {
                            File df = new File(outputDirectory + File.separator + entryName.substring(0, index));
                            df.mkdirs();
                        }
                        index = entryName.lastIndexOf("/");
                        if (index != -1) {
                            File df = new File(outputDirectory + File.separator + entryName.substring(0, index));
                            df.mkdirs();
                        }
                        File f = new File(outputDirectory + File.separator + zipEntry.getName());
                        // f.createNewFile();
                        in = zipFile.getInputStream(zipEntry);
                        out = new FileOutputStream(f);
                        int c;
                        byte[] by = new byte[1024];
                        while ((c = in.read(by)) != -1) {
                            out.write(by, 0, c);
                        }
                        out.flush();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw new IOException("解压失败：" + ex.toString());
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException ex) {
                        }
                    }
                }
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ex) {
                }
            }
        }
        return false;
    }

    public static synchronized boolean downPluginFromNet(String url, String saveDir, String fileName, boolean IsContinue) {
        HttpURLConnection conn = null;
        InputStream is = null;
        //FileOutputStream fos = null;
        RandomAccessFile rF = null;
        BufferedInputStream bis = null;
        long nStartPos = 0; // 开始位置
        try {
            File file = new File(saveDir);
            if (!file.exists()) {
                file.mkdirs();
            }
            file = new File(saveDir, fileName);
            if (file.exists() && IsContinue)// 文件存在且不需要继续下载设定
            {
                nStartPos = (long) file.length();

            } else {
                try {
                    if (file.exists()) file.delete();
                } catch (Exception ex) {
                }
                nStartPos = 0;
            }

            int BUFFER_SIZE = 2048;
            nStartPos = nStartPos - BUFFER_SIZE;//防止数据有误
            if (nStartPos < 0) nStartPos = 0;
            System.out.println("start pos: " + nStartPos);
            byte[] buf = new byte[BUFFER_SIZE];
            int size = 0;
            URL myURL = new URL(url);
            conn = (HttpURLConnection) myURL.openConnection();
            //  System.out.println(conn.getContentLength());
            //   System.out.println(nStartPos);

            // conn.setAllowUserInteraction(true);
            conn.setRequestProperty("Range", "bytes=" + nStartPos + "-");//
            conn.setRequestProperty("Content-Type", "application/octet-stream");// 下载类型 2进制流
            conn.setRequestMethod("GET");// ruiaji 2012-6-26
            conn.setRequestProperty("Accept", "application/octet-stream,*/*");
            conn.setRequestProperty("Connection", "Keep-Alive");
            // conn.setRequestProperty("Accept",
            // "image/gif, image/png,image/jpeg, image/bmp, image/jpg, apk/apk, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            // String contentType=conn.getContentType();
            // Log.v("testcontentType", contentType);
            if (!(conn.getResponseCode() == HttpURLConnection.HTTP_OK || conn.getResponseCode() == 206)) {// == 404) {
                return false;
            }
            // if(contentType.equals("text/html")){//确保为下载流
            // return false;
            // }
            rF = new RandomAccessFile(saveDir + File.separator + fileName, "rw");
            rF.seek(nStartPos);
            // 下载
            //fos = new FileOutputStream(saveDir+File.separator + fileName, true);// 重新覆盖
            is = conn.getInputStream();
            bis = new BufferedInputStream(is);
            while (((size = bis.read(buf)) != -1)) {
                rF.write(buf, 0, size);
                // fos.write(buf, 0, size);
                // raf.write(buf, 0, size);
            }
            conn.disconnect();
            is.close();
            bis.close();
            rF.close();
//            fos.flush();
//            fos.close();
            // raf.close();
            // 下载结束后发送结束通知，并通知用户自动安装
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.disconnect();
                if (is != null) is.close();
                if (rF != null) rF.close();
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
        return false;
    }

    public static JSONObject listParams2json(List<NameValuePair> params) {
        JSONObject jo = new JSONObject();
        try {
            for (NameValuePair pair : params) {
                jo.putOpt(pair.getName(), pair.getValue());
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
        return jo;
    }

    public static String addSeparator(String path) {
        if (path == null) {
            path = "";
        }
        path.trim();
        if (!path.equals("") && path.lastIndexOf("/") != path.length() - 1) {
            path += "/";
        }
        return path;
    }

    private final static String DES = "DES";

    /**
     * Description 根据键值进行加密
     *
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    public static String encrypt(String key, String data) throws Exception {
        byte[] bt = encrypt(data.getBytes(), key.getBytes());
        String strs = Base64.encodeToString(bt, Base64.DEFAULT);//new BASE64Encoder().encode(bt);
        return strs;
    }

    /**
     * Description 根据键值进行解密
     *
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String key, String data) throws IOException, Exception {
        if (data == null) return null;
//        BASE64Decoder decoder = new BASE64Decoder();
//        byte[] buf = decoder.decodeBuffer(data);
        byte[] buf = Base64.decode(data.getBytes(), Base64.DEFAULT);
        byte[] bt = decrypt(buf, key.getBytes());
        return new String(bt);
    }

    /**
     * Description 根据键值进行加密
     *
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(DES);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }


    /**
     * Description 根据键值进行解密
     *
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(DES);

        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        return cipher.doFinal(data);
    }

    public static String getUnzip(String zipCode) {
        byte[] unzipData = unzip(Base64.decode(zipCode, 0));
        String content = new String(unzipData);
        return content;
    }

    public static byte[] unzip(byte[] zipData) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            ByteArrayInputStream inputstream = new ByteArrayInputStream(zipData);
            GZIPInputStream v3_1 = new GZIPInputStream(inputstream);
            int count;
            byte dsata[] = new byte[1024];
            while ((count = v3_1.read(dsata, 0, 1024)) != -1) {
                os.write(dsata, 0, count);
            }
            v3_1.close();
            return os.toByteArray();
        } catch (Exception e) {
            return null;
        }

    }


    public static boolean isBase64(final byte[] arrayOctet) {
        for (int i = 0; i < arrayOctet.length; i++) {
            if (!(arrayOctet[i] == '=' || (arrayOctet[i] >= 0 && arrayOctet[i] < DECODE_TABLE.length && DECODE_TABLE[arrayOctet[i]] != -1)) && !(arrayOctet[i] == ' ' || arrayOctet[i] == '\n' || arrayOctet[i] == '\r' || arrayOctet[i] == '\t')) {
                return false;
            }
        }
        return true;
    }

    private static final byte[] DECODE_TABLE = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};

    public static String getFileMd5(String fileName) {
        String ret = "";
        try {

            File f = new File(fileName);
            if (!f.exists()) return ret;

            InputStream ins = new FileInputStream(f);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];

            int len;
            while ((len = ins.read(buffer)) != -1) {
                md5.update(buffer, 0, len);
            }
            ins.close();
            ret = toHexString(md5.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String getStreamMd5(InputStream ins) {
        String ret = "";
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = ins.read(buffer)) != -1) {
                md5.update(buffer, 0, len);
            }
            ins.close();
            ret = toHexString(md5.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    protected static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
            sb.append(hexChar[b[i] & 0x0f]);
        }
        return sb.toString();
    }


    private static final String ENCODEING = HTTP.UTF_8;
    private static final int TIME_OUT = 8000;
    public static DefaultHttpClient httpClient = null;

    public static void initHttpClient() {
        if (httpClient == null) {
            HttpParams params = new BasicHttpParams();
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, ENCODEING);
            params.setParameter(CoreProtocolPNames.USER_AGENT, "Apache-HttpClient/Android");
            params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);
            params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
            ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
            httpClient = new DefaultHttpClient(cm, params);
        }
    }

    private static HttpResponse post(String url, List list) throws Throwable {
        initHttpClient();
        HttpPost httppost = new HttpPost(url);
        UrlEncodedFormEntity urlencodedformentity = new UrlEncodedFormEntity(list, ENCODEING);
        httppost.setEntity(urlencodedformentity);
        return httpClient.execute(httppost);
    }


    public static String postString(String url, List list) {
        try {
            return EntityUtils.toString(post(url, list).getEntity());
        } catch (Throwable e) {
            if (logShow) e.printStackTrace();
            log("访问" + url + "出错，返回\"\"");
            return "";
        }
    }


}