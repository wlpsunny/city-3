package com.sensoro.common.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.sensoro.common.R;
import com.sensoro.common.manger.ThreadPoolManager;
import com.sensoro.common.server.bean.AlarmInfo;
import com.sensoro.common.widgets.SelectDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AppUtils {
    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        //获取所有已安装程序的包信息
        List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);
        //存储所有已安装程序的包名
        List<String> pName = new ArrayList<>();
        //从info中将报名字逐一取出
        if (pInfo != null) {
            for (int i = 0; i < pInfo.size(); i++) {
                String pn = pInfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }

    private static volatile int statusBarHeight;

    public static boolean isActivityTop(Context context, Class<?> activityClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String name = null;
        if (manager != null) {
            name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        }
        return name != null && name.equals(activityClass.getName());
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight == 0) {
            synchronized (AppUtils.class) {
                try {
                    Class<?> c = Class.forName("com.android.internal.R$dimen");
                    Object obj = c.newInstance();
                    Field field = c.getField("status_bar_height");
                    int x = Integer.parseInt(field.get(obj).toString());
                    statusBarHeight = context.getResources().getDimensionPixelSize(x);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return statusBarHeight;
    }

    /**
     * 拨打电话（跳转到拨号界面，用户手动点击拨打）
     *
     * @param phoneNum 电话号码
     */
    public static void diallPhone(String phoneNum, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        activity.startActivity(intent);
    }


    public static boolean doNavigation(Activity activity, double[] destPosition, String text) {

        if (destPosition == null || destPosition.length != 2 || destPosition[0] == 0 || destPosition[1] == 0) {
//            SensoroToast.INSTANCE.makeText("位置坐标信息错误", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (isAppInstalled(activity, "com.google.android.apps.maps")) {
            goNaviByGoogleMap(activity, destPosition[0], destPosition[1], null);
        } else {
//            Toast.makeText(activity, "您尚未安装谷歌地图", Toast.LENGTH_LONG)
//                    .show();
//            Uri uri = Uri
//                    .parse("market://details?id=com.google.android.apps.maps");
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            activity.startActivity(intent);
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
//            parameters
            try {
                LogUtils.loge("doNavigation = " + destPosition[1] + "," + destPosition[0]);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            String url = "https://www.google.com/maps/search/?api=1&query=" + destPosition[0] + "," + destPosition[1];
//            String url = "https://www.google.com/maps/search/?api=1&query=" + destPosition[1] + "," + destPosition[0];
//            String url = "http://uri.amap.com/navigation?from=" + startPosition.longitude + "," + startPosition.latitude
//                    + ",当前位置" +
//                    "&to=" + destPosition.longitude + "," + destPosition.latitude + "," +
//                    "设备部署位置&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=0";
            try {
                LogUtils.loge("doNavigation = " + url);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            activity.startActivity(intent);
        }
        return true;
    }

    private static void goNaviByGoogleMap(Activity activity, double lat, double lon, String address) {
        final String GOOGLE_MAP_NAVI_URI = "google.navigation:q=";
        Uri uri;
        if (TextUtils.isEmpty(address)) {
            uri = Uri.parse(GOOGLE_MAP_NAVI_URI + lat + "," + lon);
        } else {
            uri = Uri.parse(GOOGLE_MAP_NAVI_URI + lat + "," + lon + "," + address);
        }
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");
        activity.startActivity(mapIntent);
        //        Intent intent = new Intent(Intent.ACTION_VIEW,
//                Uri.parse("http://maps.google.com/maps?"
//                        + "saddr="+ mCurrentLatLng.latitude+ "," + mCurrentLatLng.longitude
//                        + "&daddr=" + mEndLat+ "," + mEndLng
//                        +"&avoid=highway"
//                        +"&language=zh-CN")
//        );
//
//        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
//        startActivity(intent);
    }


    public static void openNetPage(Activity activity, String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        activity.startActivity(intent);
    }

    //版本名称
    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    //版本号
    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;
        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pi;
    }

    public static void getInputSoftStatus(final View view, final InputSoftStatusListener listener) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                view.getWindowVisibleDisplayFrame(rect);
                int height = view.getRootView().getHeight();
                int i = height - rect.bottom;
                if (i < 200) {
                    listener.onKeyBoardClose();
                } else {
                    listener.onKeyBoardOpen();
                }

            }
        });
    }


    public interface InputSoftStatusListener {
        void onKeyBoardClose();

        void onKeyBoardOpen();
    }
    // 计算两点距离

    private static final double EARTH_RADIUS = 6378137.0;

    public static double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {

        double radLat1 = (lat_a * Math.PI / 180.0);

        double radLat2 = (lat_b * Math.PI / 180.0);

        double a = radLat1 - radLat2;

        double b = (lng_a - lng_b) * Math.PI / 180.0;

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)

                + Math.cos(radLat1) * Math.cos(radLat2)

                * Math.pow(Math.sin(b / 2), 2)));

        s = s * EARTH_RADIUS;

        s = Math.round(s * 10000) / 10000;

        return s;
    }

    /**
     * @return 手机型号
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    /**
     * @return 手机厂商
     */
    public static String getSystemBrand() {
        return Build.BRAND;
    }

    /**
     * 系统版本
     *
     * @return
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        return line;
    }

    /**
     * 根据手机分辨率从DP转成PX
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 根据手机的分辨率PX(像素)转成DP
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue
     * @return
     */

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

//    public static int dp2px(Context context, int dpValue) {
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
//    }
//
//    public static int sp2px(Context context, int spValue) {
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
//
//    }

    public static void dismissInputMethodManager(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);//从控件所在的窗口中隐藏
        editText.setCursorVisible(false);
    }

    public static void dismissInputMethodManager(Context context, EditText editText, boolean cursorVisible) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);//从控件所在的窗口中隐藏
        editText.setCursorVisible(cursorVisible);
    }

    public static void openInputMethodManager(Context context, EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void dismissInputMethodManager(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive()) {
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    public static int getAndroiodScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(dm);
            return dm.heightPixels; // 屏幕高度（像素）
        }

        return -1;
    }

    public static int getAndroiodScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(dm);
            return dm.widthPixels;// 屏幕宽度（像素）
        }

        return -1;
    }

    public static boolean isChineseLanguage() {
        String language = getLanguageEnv();

        return language != null && (language.trim().equals("zh-CN") || language.trim().equals("zh-TW"));
    }

    private static String getLanguageEnv() {
        Locale l = Locale.getDefault();
        String language = l.getLanguage();
        String country = l.getCountry().toLowerCase();
        if ("zh".equals(language)) {
            if ("cn".equals(country)) {
                language = "zh-CN";
            } else if ("tw".equals(country)) {
                language = "zh-TW";
            }
        } else if ("pt".equals(language)) {
            if ("br".equals(country)) {
                language = "pt-BR";
            } else if ("pt".equals(country)) {
                language = "pt-PT";
            }
        }
        return language;
    }

    public static String getContactPhone(List<AlarmInfo.RecordInfo> list) {
        String[] contract = new String[3];
        String tempNumber = null;
        outer:
        for (AlarmInfo.RecordInfo recordInfo : list) {
            String type = recordInfo.getType();
            if ("sendVoice".equals(type)) {
                AlarmInfo.RecordInfo.Event[] phoneList = recordInfo.getPhoneList();
                for (AlarmInfo.RecordInfo.Event event : phoneList) {
                    String source = event.getSource();
                    String number = event.getNumber();
                    if (!TextUtils.isEmpty(number)) {
                        if ("attach".equals(source)) {
                            try {
                                LogUtils.loge("单独联系人：" + number);
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                            if (TextUtils.isEmpty(contract[0])) {
                                contract[0] = number;
                            }
                            break outer;

                        } else if ("group".equals(source)) {
                            try {
                                LogUtils.loge("分组联系人：" + number);
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                            if (TextUtils.isEmpty(contract[0])) {
                                contract[1] = number;
                            }
                            break;
                        } else if ("notification".equals(source)) {
                            try {
                                LogUtils.loge("账户联系人：" + number);
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                            if (TextUtils.isEmpty(contract[0])) {
                                contract[2] = number;
                            }
                            break;
                        }

                    }

                }
            }
        }
        for (String contractStr : contract) {
            if (!TextUtils.isEmpty(contractStr)) {
                tempNumber = contractStr;
                break;
            }
        }
        return tempNumber;
    }

    public static String getTextFromClip(Context context) {
        try {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            //判断剪切版时候有内容
            if (!clipboardManager.hasPrimaryClip())
                return null;
            ClipData clipData = clipboardManager.getPrimaryClip();
            //获取 ClipDescription
//            ClipDescription clipDescription = clipboardManager.getPrimaryClipDescription();
//            //获取 lable
//            String lable = clipDescription.getLabel().toString();
            //获取 text
            return clipData.getItemAt(0).getText().toString();
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 不重复添加联系人
     *
     * @param context
     * @param names
     * @param numbers
     */
    public static void addToPhoneContact(final Context context, final List<String> names, final List<String> numbers) {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                //TODO 暂时去掉删除原有联系人
//                final ArrayList<Long> idList = new ArrayList<>();
//                Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
//                if (cursor != null) {
//                    while (cursor.moveToNext()) {
//                        long contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID));
//                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//                        String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        //
//                        LogUtils.loge("addToPhoneContact-->>name = " + name + ",number = " + number);
//                        if ("升哲安全服务".equals(name)) {
//                            idList.add(contactId);
//                        }
//                    }
//                    if (cursor != null) {
//                        cursor.close();
//                    }
//                    batchDelContact(context, idList);
//                }
                //
                ContentValues values = new ContentValues();
                //TODO 默认设置不允许重复添加
//                values.put(ContactsContract.RawContacts.AGGREGATION_MODE,ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED);
                Uri rawContactUri = context.getContentResolver().insert(
                        ContactsContract.RawContacts.CONTENT_URI, values);
                long rawContactId = ContentUris.parseId(rawContactUri);
                //插入姓名和号码
                final ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                for (String name : names) {
                    ContentProviderOperation build = ContentProviderOperation
                            .newInsert(
                                    ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                            .withValue(ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name).build();
                    ops.add(build);
                }
                for (String number : numbers) {
                    ContentProviderOperation build = ContentProviderOperation
                            .newInsert(
                                    ContactsContract.Data.CONTENT_URI)
                            .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.LABEL,
                                    ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE))
                            .build();
                    ops.add(build);
                }
                try {
                    ContentProviderResult[] results = context.getContentResolver()
                            .applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 删除指定id联系人
     *
     * @param context
     * @param list
     */
    private static void batchDelContact(Context context, final List<Long> list) {
        final ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            long id = list.get(i);
            ops.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, id))
                    .withYieldAllowed(true)
                    .build());
        }
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * 合同选择场地性质dialog
     */
    public static SelectDialog showDialog(Activity activity, SelectDialog.SelectDialogListener listener, List<String> items, String title) {
        SelectDialog dialog = new SelectDialog(activity, R.style
                .select_dialog_bg,
                listener, items, title);
        if (!activity.isFinishing()) {
            dialog.show();
        }
        return dialog;
    }

    /**
     * 判断是否是主进程
     *
     * @return
     */
    public static boolean isAppMainProcess(@NonNull Context context, @NonNull String appName) {
        try {
            int pid = android.os.Process.myPid();
            String process = getProcessNameByPID(context, pid);
//            try {
//                LogUtils.loge("currentNearby---->> process = " + process);
//            } catch (Throwable throwable) {
//                throwable.printStackTrace();
//            }
            return TextUtils.isEmpty(process) || appName.equalsIgnoreCase(process);
//            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 根据 pid 获取进程名
     *
     * @param context
     * @param pid
     * @return
     */
    private static String getProcessNameByPID(Context context, int pid) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return "";
        }
        for (android.app.ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo == null) {
                continue;
            }
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return "";
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
