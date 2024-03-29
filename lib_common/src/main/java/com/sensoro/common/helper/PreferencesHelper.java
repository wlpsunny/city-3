package com.sensoro.common.helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.sensoro.common.R;
import com.sensoro.common.base.ContextUtils;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.constant.SearchHistoryTypeConstants;
import com.sensoro.common.model.DeployAnalyzerModel;
import com.sensoro.common.model.EventLoginData;
import com.sensoro.common.model.IbeaconSettingData;
import com.sensoro.common.model.SecurityRisksTagModel;
import com.sensoro.common.server.RetrofitServiceHelper;
import com.sensoro.common.server.bean.AlarmPopupDataBean;
import com.sensoro.common.server.bean.DeployPicInfo;
import com.sensoro.common.server.bean.DeviceMergeTypesInfo;
import com.sensoro.common.server.bean.DeviceTypeStyles;
import com.sensoro.common.server.bean.MalfunctionTypeStyles;
import com.sensoro.common.server.bean.MergeTypeStyles;
import com.sensoro.common.server.bean.SensorTypeStyles;
import com.sensoro.common.utils.AESUtil;
import com.sensoro.common.utils.DateUtil;
import com.sensoro.common.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sensoro on 17/7/4.
 */

public final class PreferencesHelper implements Constants {

    private EventLoginData mEventLoginData;
    private DeviceMergeTypesInfo mDeviceMergeTypesInfo;
    private AlarmPopupDataBean mAlarmPopupDataBean;
    private String myBaseUrl;

    private PreferencesHelper() {
    }

    public static PreferencesHelper getInstance() {
        return PreferencesHelperHolder.instance;
    }


    private static class PreferencesHelperHolder {
        private static final PreferencesHelper instance = new PreferencesHelper();
    }


    public void saveUserData(EventLoginData eventLoginData) {
        mEventLoginData = eventLoginData;
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_SPLASH_LOGIN_DATA, Context
                .MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        //
        //String loginDataJson = RetrofitServiceHelper.INSTANCE.getGson().toJson(eventLoginData);
        //EventLoginData eventLoginData1 = RetrofitServiceHelper.INSTANCE.getGson().fromJson(loginDataJson, EventLoginData.class);
        if (!TextUtils.isEmpty(eventLoginData.userId)) {
            editor.putString(EXTRA_USER_ID, eventLoginData.userId);
        }
        if (!TextUtils.isEmpty(eventLoginData.userName)) {
            editor.putString(EXTRA_USER_NAME, eventLoginData.userName);
        }
        if (!TextUtils.isEmpty(eventLoginData.phone)) {
            editor.putString(EXTRA_PHONE, eventLoginData.phone);
        }
        if (!TextUtils.isEmpty(eventLoginData.phoneId)) {
            editor.putString(EXTRA_PHONE_ID, eventLoginData.phoneId);
        }

        //TODO character
        if (!TextUtils.isEmpty(eventLoginData.roles)) {
            editor.putString(EXTRA_USER_ROLES, eventLoginData.roles);
        }
        if (!TextUtils.isEmpty(eventLoginData.accountId)) {
            editor.putString(EXTRA_USER_ACCOUNT_ID, eventLoginData.accountId);
        }
        //
        editor.putBoolean(EXTRA_IS_SPECIFIC, eventLoginData.isSupperAccount);
        editor.putBoolean(EXTRA_GRANTS_HAS_STATION_DEPLOY, eventLoginData.hasStationDeploy);
        editor.putBoolean(EXTRA_GRANTS_HAS_STATION_LIST, eventLoginData.hasStationList);
        editor.putBoolean(EXTRA_GRANTS_HAS_CONTRACT, eventLoginData.hasContract);
        editor.putBoolean(EXTRA_GRANTS_HAS_CONTRACT_CREATE, eventLoginData.hasContractCreate);
        editor.putBoolean(EXTRA_GRANTS_HAS_CONTRACT_MODIFY, eventLoginData.hasContractModify);
        editor.putBoolean(EXTRA_GRANTS_HAS_SCAN_LOGIN, eventLoginData.hasScanLogin);
        editor.putBoolean(EXTRA_GRANTS_HAS_SUB_MERCHANT, eventLoginData.hasSubMerchant);
        editor.putBoolean(EXTRA_GRANTS_HAS_MERCHANT_CHANGE, eventLoginData.hasMerchantChange);
        editor.putBoolean(EXTRA_GRANTS_HAS_INSPECTION_TASK_LIST, eventLoginData.hasInspectionTaskList);
        editor.putBoolean(EXTRA_GRANTS_HAS_INSPECTION_TASK_MODIFY, eventLoginData.hasInspectionTaskModify);
        editor.putBoolean(EXTRA_GRANTS_HAS_INSPECTION_DEVICE_LIST, eventLoginData.hasInspectionDeviceList);
        editor.putBoolean(EXTRA_GRANTS_HAS_INSPECTION_DEVICE_MODIFY, eventLoginData.hasInspectionDeviceModify);
        editor.putBoolean(EXTRA_GRANTS_HAS_ALARM_LOG_INFO, eventLoginData.hasAlarmInfo);
        editor.putBoolean(EXTRA_GRANTS_HAS_MALFUNCTION_INFO, eventLoginData.hasMalfunction);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_BRIEF, eventLoginData.hasDeviceBrief);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_SIGNAL_CHECK, eventLoginData.hasSignalCheck);
//        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_SIGNAL_CONFIG, eventLoginData.hasSignalConfig);
        editor.putBoolean(EXTRA_GRANTS_HAS_BAD_SIGNAL_UPLOAD, eventLoginData.hasForceUpload);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_POSITION_CALIBRATION, eventLoginData.hasDevicePositionCalibration);
        //
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_MUTE_SHORT, eventLoginData.hasDeviceMuteShort);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_MUTE_LONG, eventLoginData.hasDeviceMuteLong);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_MUTE_TIME, eventLoginData.hasDeviceMuteTime);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_RESET, eventLoginData.hasDeviceControlReset);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_PASSWORD, eventLoginData.hasDeviceControlPassword);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_VIEW, eventLoginData.hasDeviceControlView);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_CHECK, eventLoginData.hasDeviceControlCheck);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_CONFIG, eventLoginData.hasDeviceControlConfig);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_OPEN, eventLoginData.hasDeviceControlOpen);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_CLOSE, eventLoginData.hasDeviceControlClose);
        //
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_FIRMWARE_UPDATE, eventLoginData.hasDeviceFirmwareUpdate);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_DEMO_MODE, eventLoginData.hasDeviceDemoMode);
        editor.putBoolean(EXTRA_GRANTS_HAS_CONTROLLER_AID, eventLoginData.hasControllerAid);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_CAMERA_LIST, eventLoginData.hasDeviceCameraList);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_FOREST_CAMERA_LIST, eventLoginData.hasDeviceForestCameraList);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_CAMERA_DEPLOY, eventLoginData.hasDeviceCameraDeploy);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEVICE_FOREST_CAMERA_DEPLOY, eventLoginData.hasDeviceForestCameraDeploy);
        editor.putBoolean(EXTRA_GRANTS_HAS_NAMEPLATE_LIST, eventLoginData.hasNameplateList);
        editor.putBoolean(EXTRA_GRANTS_HAS_NAMEPLATE_DEPLOY, eventLoginData.hasNameplateDeploy);
        editor.putBoolean(EXTRA_GRANTS_HAS_IBEACON_SEARCH_DEMO, eventLoginData.hasIBeaconSearchDemo);
        editor.putBoolean(EXTRA_GRANTS_HAS_MONITOR_TASK_LIST, eventLoginData.hasMonitorTaskList);
        editor.putBoolean(EXTRA_GRANTS_HAS_MONITOR_TASK_CONFIRM, eventLoginData.hasMonitorTaskConfirm);
        editor.putBoolean(EXTRA_GRANTS_HAS_DEPLOY_OFFLINE_TASK, eventLoginData.hasDeployOfflineTask);
        //
        editor.apply();
    }

    public EventLoginData getUserData() {
        if (mEventLoginData == null) {
            SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_SPLASH_LOGIN_DATA, Context
                    .MODE_PRIVATE);
            final EventLoginData eventLoginData = new EventLoginData();
            eventLoginData.phoneId = sp.getString(EXTRA_PHONE_ID, null);
            eventLoginData.userId = sp.getString(EXTRA_USER_ID, null);
            eventLoginData.userName = sp.getString(EXTRA_USER_NAME, null);
            eventLoginData.phone = sp.getString(EXTRA_PHONE, null);
            eventLoginData.roles = sp.getString(EXTRA_USER_ROLES, null);
            eventLoginData.accountId = sp.getString(EXTRA_USER_ACCOUNT_ID, null);
            try {
                LogUtils.loge(this, "phoneId = " + eventLoginData.phoneId + ",userId = " + eventLoginData.userId);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            //
            eventLoginData.isSupperAccount = sp.getBoolean(EXTRA_IS_SPECIFIC, false);
            eventLoginData.hasStationDeploy = sp.getBoolean(EXTRA_GRANTS_HAS_STATION_DEPLOY, false);
            eventLoginData.hasStationList = sp.getBoolean(EXTRA_GRANTS_HAS_STATION_LIST, false);
            eventLoginData.hasContract = sp.getBoolean(EXTRA_GRANTS_HAS_CONTRACT, false);
            eventLoginData.hasContractCreate = sp.getBoolean(EXTRA_GRANTS_HAS_CONTRACT_CREATE, false);
            eventLoginData.hasContractModify = sp.getBoolean(EXTRA_GRANTS_HAS_CONTRACT_MODIFY, false);
            eventLoginData.hasScanLogin = sp.getBoolean(EXTRA_GRANTS_HAS_SCAN_LOGIN, false);
            eventLoginData.hasSubMerchant = sp.getBoolean(EXTRA_GRANTS_HAS_SUB_MERCHANT, false);
            eventLoginData.hasMerchantChange = sp.getBoolean(EXTRA_GRANTS_HAS_MERCHANT_CHANGE, false);
            eventLoginData.hasInspectionTaskList = sp.getBoolean(EXTRA_GRANTS_HAS_INSPECTION_TASK_LIST, false);
            eventLoginData.hasInspectionTaskModify = sp.getBoolean(EXTRA_GRANTS_HAS_INSPECTION_TASK_MODIFY, false);
            eventLoginData.hasInspectionDeviceList = sp.getBoolean(EXTRA_GRANTS_HAS_INSPECTION_DEVICE_LIST, false);
            eventLoginData.hasInspectionDeviceModify = sp.getBoolean(EXTRA_GRANTS_HAS_INSPECTION_DEVICE_MODIFY, false);
            eventLoginData.hasAlarmInfo = sp.getBoolean(EXTRA_GRANTS_HAS_ALARM_LOG_INFO, false);
            eventLoginData.hasMalfunction = sp.getBoolean(EXTRA_GRANTS_HAS_MALFUNCTION_INFO, false);
            eventLoginData.hasDeviceBrief = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_BRIEF, false);
            eventLoginData.hasSignalCheck = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_SIGNAL_CHECK, false);
//            boolean hasDeviceSignalConfig = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_SIGNAL_CONFIG, false);
            //这里去掉具有信号配置权限
            eventLoginData.hasSignalConfig = false;
            eventLoginData.hasForceUpload = sp.getBoolean(EXTRA_GRANTS_HAS_BAD_SIGNAL_UPLOAD, false);

            eventLoginData.hasDevicePositionCalibration = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_POSITION_CALIBRATION, false);
            //设备控制权限
            eventLoginData.hasDeviceMuteShort = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_MUTE_SHORT, false);
            eventLoginData.hasDeviceMuteLong = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_MUTE_LONG, false);
            eventLoginData.hasDeviceMuteTime = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_MUTE_TIME, false);
            eventLoginData.hasDeviceControlReset = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_RESET, false);
            eventLoginData.hasDeviceControlPassword = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_PASSWORD, false);
            eventLoginData.hasDeviceControlView = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_VIEW, false);
            eventLoginData.hasDeviceControlCheck = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_CHECK, false);
            eventLoginData.hasDeviceControlConfig = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_CONFIG, false);
            eventLoginData.hasDeviceControlOpen = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_OPEN, false);
            eventLoginData.hasDeviceControlClose = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_CONTROL_CLOSE, false);
            //
            eventLoginData.hasDeviceFirmwareUpdate = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_FIRMWARE_UPDATE, false);
            eventLoginData.hasDeviceDemoMode = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_DEMO_MODE, false);
            eventLoginData.hasControllerAid = sp.getBoolean(EXTRA_GRANTS_HAS_CONTROLLER_AID, false);
            eventLoginData.hasDeviceCameraList = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_CAMERA_LIST, false);
            eventLoginData.hasDeviceForestCameraList = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_FOREST_CAMERA_LIST, false);
            eventLoginData.hasDeviceCameraDeploy = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_CAMERA_DEPLOY, false);
            eventLoginData.hasDeviceForestCameraDeploy = sp.getBoolean(EXTRA_GRANTS_HAS_DEVICE_FOREST_CAMERA_DEPLOY, false);
            eventLoginData.hasNameplateList = sp.getBoolean(EXTRA_GRANTS_HAS_NAMEPLATE_LIST, false);
            eventLoginData.hasNameplateDeploy = sp.getBoolean(EXTRA_GRANTS_HAS_NAMEPLATE_DEPLOY, false);
            eventLoginData.hasIBeaconSearchDemo = sp.getBoolean(EXTRA_GRANTS_HAS_IBEACON_SEARCH_DEMO, false);
            eventLoginData.hasMonitorTaskList = sp.getBoolean(EXTRA_GRANTS_HAS_MONITOR_TASK_LIST, false);
            eventLoginData.hasMonitorTaskConfirm = sp.getBoolean(EXTRA_GRANTS_HAS_MONITOR_TASK_CONFIRM, false);
            eventLoginData.hasDeployOfflineTask = sp.getBoolean(EXTRA_GRANTS_HAS_DEPLOY_OFFLINE_TASK, false);

            mEventLoginData = eventLoginData;
        }
        return mEventLoginData;
    }

    /**
     * 保存账户名称
     *
     * @param username
     * @param pwd
     */
    public boolean saveLoginNamePwd(String username, String pwd) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pwd)) {
            return false;
        }
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_LOGIN_NAME_PWD, Context
                .MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_NAME, username);
        String aes_pwd = AESUtil.encode(pwd);
        editor.putString(PREFERENCE_KEY_PASSWORD, aes_pwd);
        editor.apply();
        return true;
    }

    public Map<String, String> getLoginNamePwd() {
        HashMap<String, String> map = new HashMap<>();
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_LOGIN_NAME_PWD, Context
                .MODE_PRIVATE);
        String name = sp.getString(PREFERENCE_KEY_NAME, null);
        String pwd = sp.getString(PREFERENCE_KEY_PASSWORD, null);
        if (!TextUtils.isEmpty(name)) {
            map.put(PREFERENCE_KEY_NAME, name);
        }
        if (!TextUtils.isEmpty(pwd)) {
            String aes_pwd = AESUtil.decode(pwd);
            map.put(PREFERENCE_KEY_PASSWORD, aes_pwd);
        }
        return map;
    }

    public int getBaseUrlType() {
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_SCOPE, Context
                .MODE_PRIVATE);
        return sp.getInt(PREFERENCE_KEY_URL, 0);
    }

    public void saveBaseUrlType(int urlType) {
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_SCOPE, Context
                .MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREFERENCE_KEY_URL, urlType);
        editor.apply();
    }

    public String getMyBaseUrl() {
        if (!TextUtils.isEmpty(myBaseUrl)) {
            return myBaseUrl;
        }
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_SCOPE, Context
                .MODE_PRIVATE);
        myBaseUrl = sp.getString(PREFERENCE_KEY_MY_BASE_URL, null);
        return myBaseUrl;
    }

    public void saveMyBaseUrl(String url) {
        this.myBaseUrl = url;
        if (!TextUtils.isEmpty(myBaseUrl)) {
            SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_SCOPE, Context
                    .MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(PREFERENCE_KEY_MY_BASE_URL, url);
            editor.apply();
        }
    }

    public boolean saveSessionId(String sessionId, String token) {
        if (TextUtils.isEmpty(sessionId) && TextUtils.isEmpty(token)) {
            return false;
        }
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_LOGIN_ID, Context
                .MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (!TextUtils.isEmpty(sessionId)) {
            editor.putString(PREFERENCE_KEY_SESSION_ID, sessionId);
        }
        if (!TextUtils.isEmpty(token)) {
            editor.putString(PREFERENCE_KEY_USER_TOKEN, token);
        }
        editor.apply();
        return true;
    }

    public String getSessionToken() {
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_LOGIN_ID, Context
                .MODE_PRIVATE);
        return sp.getString(PREFERENCE_KEY_USER_TOKEN, null);

    }

    public String getSessionId() {
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_LOGIN_ID, Context
                .MODE_PRIVATE);
        return sp.getString(PREFERENCE_KEY_SESSION_ID, null);

    }

    public void clearLoginDataSessionId() {
        ContextUtils.getContext().getSharedPreferences(PREFERENCE_LOGIN_ID, Context
                .MODE_PRIVATE).edit().clear().apply();
        ContextUtils.getContext().getSharedPreferences(PREFERENCE_SPLASH_LOGIN_DATA, Context
                .MODE_PRIVATE).edit().clear().apply();
        this.mEventLoginData = null;
    }

    public boolean saveDeployNameAddressHistory(String history) {
        if (TextUtils.isEmpty(history)) {
            return false;
        }
        ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).edit().putString(PREFERENCE_KEY_DEPLOY_NAME_ADDRESS, history).apply();
        return true;
    }

    public boolean saveDeployWeChatRelationHistory(String history) {
        if (TextUtils.isEmpty(history)) {
            return false;
        }
        ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).edit().putString(PREFERENCE_KEY_DEPLOY_WE_CHAT_RELATION, history).apply();
        return true;
    }

    public boolean saveDeployForestCameraInstallPositionHistory(String history) {
        if (TextUtils.isEmpty(history)) {
            return false;
        }
        ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).edit().putString(PREFERENCE_KEY_DEPLOY_FOREST_CAMERA_INSTALL_POSITION, history).apply();
        return true;
    }

    public String getDeployNameAddressHistory() {
        return ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).getString(PREFERENCE_KEY_DEPLOY_NAME_ADDRESS, null);
    }

    public String getDeployWeChatRelationHistory() {
        return ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).getString(PREFERENCE_KEY_DEPLOY_WE_CHAT_RELATION, null);
    }

    public String getDeployForestCameraInstallPositionHistory() {
        return ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).getString(PREFERENCE_KEY_DEPLOY_FOREST_CAMERA_INSTALL_POSITION, null);
    }

    public boolean saveDeployTagsHistory(String history) {
        if (TextUtils.isEmpty(history)) {
            return false;
        }
        ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).edit().putString(PREFERENCE_KEY_DEPLOY_TAG, history).apply();
        return true;
    }

    public String getDeployTagsHistory() {
        return ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).getString(PREFERENCE_KEY_DEPLOY_TAG, null);
    }

    public DeviceMergeTypesInfo getLocalDevicesMergeTypes() {
        try {
            if (mDeviceMergeTypesInfo == null) {
                String json = ContextUtils.getContext().getSharedPreferences(PREFERENCE_LOCAL_DEVICES_MERGETYPES, Activity.MODE_PRIVATE).getString(PREFERENCE_KEY_LOCAL_DEVICES_MERGE_TYPES, null);
                LogUtils.loge("DeviceMergeTypesInfo json : " + json);
                if (!TextUtils.isEmpty(json)) {
                    mDeviceMergeTypesInfo = RetrofitServiceHelper.getInstance().getGson().fromJson(json, DeviceMergeTypesInfo.class);
                }
            }
//        if (mDeviceMergeTypesInfo != null) {
//            //加入全部的类型数据
//            DeviceMergeTypesInfo.DeviceMergeTypeConfig config = mDeviceMergeTypesInfo.getConfig();
//            Map<String, DeviceTypeStyles> deviceType = config.getDeviceType();
//            if (!deviceType.containsKey("all")) {
//                DeviceTypeStyles deviceTypeStyles = new DeviceTypeStyles();
//                deviceTypeStyles.setMergeType("all");
//                deviceType.put("all", deviceTypeStyles);
//            }
//            Map<String, MergeTypeStyles> mergeType = config.getMergeType();
//            if (!mergeType.containsKey("all")) {
//                MergeTypeStyles mergeTypeStyles = new MergeTypeStyles();
//                mergeTypeStyles.setName("全部");
//                mergeTypeStyles.setResId(R.drawable.type_all);
//                mergeType.put("all", mergeTypeStyles);
//            }
//            Map<String, SensorTypeStyles> sensorType = config.getSensorType();
//            if (!sensorType.containsKey("all")) {
//                sensorType.put("all", new SensorTypeStyles());
//            }
//        }
            return mDeviceMergeTypesInfo;
        } catch (Throwable t) {
            return null;
        }
    }

    public boolean saveLocalDevicesMergeTypes(DeviceMergeTypesInfo deviceMergeTypesInfo) {
        if (deviceMergeTypesInfo == null) {
            return false;
        }
        mDeviceMergeTypesInfo = deviceMergeTypesInfo;
        String json = RetrofitServiceHelper.getInstance().getGson().toJson(mDeviceMergeTypesInfo);
        if (!TextUtils.isEmpty(json)) {
            try {
                LogUtils.loge("saveLocalDevicesMergeTypes length = " + json.length());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            try {
                LogUtils.loge("saveLocalDevicesMergeTypes :" + json);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_LOCAL_DEVICES_MERGETYPES, Context
                .MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_LOCAL_DEVICES_MERGE_TYPES, json);
        editor.apply();
        return true;
    }

    public AlarmPopupDataBean getAlarmPopupDataBeanCache() {
        try {
            if (mAlarmPopupDataBean == null) {
                String json = ContextUtils.getContext().getSharedPreferences(PREFERENCE_LOCAL_ALARM_POPUP_DATA_BEAN, Activity.MODE_PRIVATE).getString(PREFERENCE_KEY_LOCAL_ALARM_POPUP_DATA_BEAN, null);
                LogUtils.loge("alarmPopupDataBeanCache json : " + json);
                if (!TextUtils.isEmpty(json)) {
                    mAlarmPopupDataBean = RetrofitServiceHelper.getInstance().getGson().fromJson(json, AlarmPopupDataBean.class);
                }
            }
            return mAlarmPopupDataBean;
        } catch (Throwable t) {
            return null;
        }
    }

    public boolean saveAlarmPopupDataBeanCache(AlarmPopupDataBean alarmPopupDataBean) {
        if (alarmPopupDataBean == null) {
            return false;
        }
        mAlarmPopupDataBean = alarmPopupDataBean;
        String json = RetrofitServiceHelper.getInstance().getGson().toJson(alarmPopupDataBean);
        if (!TextUtils.isEmpty(json)) {
            try {
                LogUtils.loge("saveAlarmPopupDataBeanCache length = " + json.length());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            try {
                LogUtils.loge("saveAlarmPopupDataBeanCache :" + json);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_LOCAL_ALARM_POPUP_DATA_BEAN, Context
                .MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_LOCAL_ALARM_POPUP_DATA_BEAN, json);
        editor.apply();
        return true;
    }

    public boolean saveSearchHistoryText(String text, int type) {
        String spFileName = getSearchHistoryFileName(type);
        if (TextUtils.isEmpty(spFileName) || TextUtils.isEmpty(text)) {
            return false;
        }
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(spFileName, Context
                .MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        String oldText = sp.getString(SearchHistoryTypeConstants.SEARCH_HISTORY_KEY, "");
        //
        List<String> oldHistoryList = new ArrayList<String>();
        if (!TextUtils.isEmpty(oldText)) {
            oldHistoryList.addAll(Arrays.asList(oldText.split(",")));
        }
        oldHistoryList.remove(text);
        oldHistoryList.add(0, text);
        ArrayList<String> tempList = new ArrayList<>();
        for (String str : oldHistoryList) {
            if (tempList.size() < 20) {
                tempList.add(str);
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < tempList.size(); i++) {
            if (i == (tempList.size() - 1)) {
                stringBuilder.append(tempList.get(i));
            } else {
                stringBuilder.append(tempList.get(i)).append(",");
            }
        }
        edit.putString(SearchHistoryTypeConstants.SEARCH_HISTORY_KEY, stringBuilder.toString());
        edit.apply();
        return true;

    }

    private String getSearchHistoryFileName(int type) {
        String spFileName = null;
        switch (type) {

            case SearchHistoryTypeConstants.TYPE_SEARCH_CAMERA_LIST:
                spFileName = SearchHistoryTypeConstants.SP_FILE_SEARCH_CAMERA_LIST;
                break;
            case SearchHistoryTypeConstants.TYPE_SEARCH_BASESTATION:
                spFileName = SearchHistoryTypeConstants.SP_FILE_BASESTATION;
                break;
            case SearchHistoryTypeConstants.TYPE_SEARCH_HISTORY_WARN:
                spFileName = SearchHistoryTypeConstants.SP_FILE_WARN;
                break;
            case SearchHistoryTypeConstants.TYPE_SEARCH_HISTORY_MALFUNCTION:
                spFileName = SearchHistoryTypeConstants.SP_FILE_MALFUNCTION;
                break;
            case SearchHistoryTypeConstants.TYPE_SEARCH_HISTORY_INSPECTION:
                spFileName = SearchHistoryTypeConstants.SP_FILE_INSPECTION;
                break;
            case SearchHistoryTypeConstants.TYPE_SEARCH_HISTORY_CONTRACT:
                spFileName = SearchHistoryTypeConstants.SP_FILE_CONTRACT;
                break;
            case SearchHistoryTypeConstants.TYPE_SEARCH_HISTORY_MERCHANT:
                spFileName = SearchHistoryTypeConstants.SP_FILE_MERCHANT;
                break;
            case SearchHistoryTypeConstants.TYPE_SEARCH_HISTORY_DEPLOY_RECORD:
                spFileName = SearchHistoryTypeConstants.SP_FILE_DEPLOY_RECORD;
                break;
            case SearchHistoryTypeConstants.TYPE_SEARCH_HISTORY_DEPLOY_NAMEPLATE_NAME:
                spFileName = SearchHistoryTypeConstants.SP_FILE_DEPLOY_NAMEPLATE_NAME;
                break;
            case SearchHistoryTypeConstants.TYPE_SEARCH_HISTORY_NAMEPLATE_ADD_FROM_LIST:
                spFileName = SearchHistoryTypeConstants.SP_FILE_NAMEPLATE_ADD_FROM_LIST;
                break;
            case SearchHistoryTypeConstants.TYPE_SEARCH_HISTORY_SECURITY_WARN:
                spFileName = SearchHistoryTypeConstants.SP_FILE_SECURITY_WARN;
                break;
            case SearchHistoryTypeConstants.TYPE_SEARCH_FOREST_FIRE_CAMERA_LIST:
                spFileName = SearchHistoryTypeConstants.SP_FILE_FOREST_FIRE_CAMERA_LIST;
                break;


        }
        return spFileName;
    }

    public boolean clearSearchHistory(int type) {
        String spFileName = getSearchHistoryFileName(type);
        if (TextUtils.isEmpty(spFileName)) {
            return false;
        }

        SharedPreferences.Editor editor = ContextUtils.getContext().getSharedPreferences(spFileName, Context
                .MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        return true;

    }

    public List<String> getSearchHistoryData(int type) {
        String spFileName = getSearchHistoryFileName(type);
        if (TextUtils.isEmpty(spFileName)) {
            return null;
        }

        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(spFileName, Context
                .MODE_PRIVATE);
        String oldText = sp.getString(SearchHistoryTypeConstants.SEARCH_HISTORY_KEY, "");
        //
        List<String> oldHistoryList = new ArrayList<String>();
        if (!TextUtils.isEmpty(oldText)) {
            oldHistoryList.addAll(Arrays.asList(oldText.split(",")));
        }
        return oldHistoryList;
    }

    /**
     * 获取配置字段 --故障主字段
     *
     * @param mainFunctionMainType
     * @return
     */
    public MalfunctionTypeStyles getConfigMalfunctionMainTypes(String mainFunctionMainType) {
        DeviceMergeTypesInfo localDevicesMergeTypes = getLocalDevicesMergeTypes();
        if (localDevicesMergeTypes != null) {
            DeviceMergeTypesInfo.DeviceMergeTypeConfig config = localDevicesMergeTypes.getConfig();
            if (config != null) {
                DeviceMergeTypesInfo.DeviceMergeTypeConfig.MalfunctionTypeBean malfunctionType = config.getMalfunctionType();
                if (malfunctionType != null) {
                    Map<String, MalfunctionTypeStyles> mainTypes = malfunctionType.getMainTypes();
                    if (mainTypes != null) {
                        return mainTypes.get(mainFunctionMainType);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取配置字段 --故障子字段
     *
     * @param mainFunctionSubType
     * @return
     */
    public MalfunctionTypeStyles getConfigMalfunctionSubTypes(String mainFunctionSubType) {
        DeviceMergeTypesInfo localDevicesMergeTypes = getLocalDevicesMergeTypes();
        if (localDevicesMergeTypes != null) {
            DeviceMergeTypesInfo.DeviceMergeTypeConfig config = localDevicesMergeTypes.getConfig();
            if (config != null) {
                DeviceMergeTypesInfo.DeviceMergeTypeConfig.MalfunctionTypeBean malfunctionType = config.getMalfunctionType();
                if (malfunctionType != null) {
                    Map<String, MalfunctionTypeStyles> subTypes = malfunctionType.getSubTypes();
                    if (subTypes != null) {
                        return subTypes.get(mainFunctionSubType);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取配置字段 --设备副类型
     *
     * @param deviceType
     * @return
     */
    public DeviceTypeStyles getConfigDeviceType(String deviceType) {
        DeviceMergeTypesInfo localDevicesMergeTypes = getLocalDevicesMergeTypes();
        if (localDevicesMergeTypes != null) {
            DeviceMergeTypesInfo.DeviceMergeTypeConfig config = localDevicesMergeTypes.getConfig();
            if (config != null) {
                Map<String, DeviceTypeStyles> deviceTypeStylesMap = config.getDeviceType();
                if (deviceTypeStylesMap != null) {
                    return deviceTypeStylesMap.get(deviceType);
                }
            }
        }
        return null;
    }

    /**
     * 获取部署照片中的配置字段
     *
     * @param deviceType
     * @return
     */
    public List<DeployPicInfo> getConfigDeviceDeployPic(String deviceType) {
        DeviceTypeStyles configDeviceType = getConfigDeviceType(deviceType);
        if (configDeviceType != null) {
            return configDeviceType.getDeployPicConfig();
        }
        return null;
    }

    /**
     * 获取配置字段 --设备主类型
     *
     * @param mergeType
     * @return
     */
    public MergeTypeStyles getConfigMergeType(String mergeType) {
        DeviceMergeTypesInfo localDevicesMergeTypes = getLocalDevicesMergeTypes();
        if (localDevicesMergeTypes != null) {
            DeviceMergeTypesInfo.DeviceMergeTypeConfig config = localDevicesMergeTypes.getConfig();
            if (config != null) {
                Map<String, MergeTypeStyles> mergeTypeStylesMap = config.getMergeType();
                if (mergeTypeStylesMap != null) {
                    return mergeTypeStylesMap.get(mergeType);
                }
            }
        }
        return null;
    }

    /**
     * 获取配置字段 --传感器类型
     *
     * @param sensorType
     * @return
     */
    public SensorTypeStyles getConfigSensorType(String sensorType) {
        DeviceMergeTypesInfo localDevicesMergeTypes = getLocalDevicesMergeTypes();
        if (localDevicesMergeTypes != null) {
            DeviceMergeTypesInfo.DeviceMergeTypeConfig config = localDevicesMergeTypes.getConfig();
            if (config != null) {
                Map<String, SensorTypeStyles> sensorTypeStylesMap = config.getSensorType();
                if (sensorTypeStylesMap != null) {
                    return sensorTypeStylesMap.get(sensorType);
                }
            }
        }
        return null;
    }

    /**
     * 存储示例照片今日不再提示的时间
     *
     * @param key
     */
    public void saveDeployExamplePicTimestamp(String key) {
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_EXAMPLE_PIC, Context
                .MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, DateUtil.getStrTime_yymmdd(System.currentTimeMillis()));
        editor.apply();
    }

    public String getDeployExamplePicTimestamp(String key) {
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_EXAMPLE_PIC, Context
                .MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public int getLocalDemoModeState(String sn) {
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEMO_MODE_JSON, Context
                .MODE_PRIVATE);
        String json = sp.getString(PREFERENCE_DEMO_MODE_JSON, null);
        if (!TextUtils.isEmpty(json)) {
            HashMap map = RetrofitServiceHelper.getInstance().getGson().fromJson(json, HashMap.class);
            Object value = map.get(sn);
            if (value instanceof Integer) {
                return (int) value;
            }
        }
        return 0;
    }

    public void saveLocalDemoModeState(String sn, int mode) {
        final HashMap<String, Integer> localDemoModeMap = new HashMap<>();
        localDemoModeMap.put(sn, mode);
        try {
            String json = RetrofitServiceHelper.getInstance().getGson().toJson(localDemoModeMap);
            SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEMO_MODE_JSON, Context
                    .MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(PREFERENCE_DEMO_MODE_JSON, json);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取存储的版本号
     *
     * @return
     */
    public int getSaveVersionCode() {
        try {
            SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_KEY_VERSION_CODE, Context
                    .MODE_PRIVATE);
            return sp.getInt(PREFERENCE_KEY_VERSION_CODE, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 保存当前的版本号
     *
     * @param code
     */
    public void saveCurrentVersionCode(int code) {
        SharedPreferences sp = ContextUtils.getContext().getSharedPreferences(PREFERENCE_KEY_VERSION_CODE, Context
                .MODE_PRIVATE);
        sp.edit().putInt(PREFERENCE_KEY_VERSION_CODE, code).apply();
    }

    public String getDeployAlarmContactNameHistory() {
        return ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).getString(PREFERENCE_KEY_DEPLOY_ALARM_CONTACT_NAME, null);
    }

    public boolean saveDeployAlarmContactNameHistory(String history) {
        if (TextUtils.isEmpty(history)) {
            return false;
        }
        ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).edit().putString(PREFERENCE_KEY_DEPLOY_ALARM_CONTACT_NAME, history).apply();
        return true;
    }

    public String getDeployAlarmContactPhoneHistory() {
        return ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).getString(PREFERENCE_KEY_DEPLOY_ALARM_CONTACT_PHONE, null);
    }

    public boolean saveDeployAlarmContactPhoneHistory(String history) {
        if (TextUtils.isEmpty(history)) {
            return false;
        }
        ContextUtils.getContext().getSharedPreferences(PREFERENCE_DEPLOY_HISTORY, Activity.MODE_PRIVATE).edit().putString(PREFERENCE_KEY_DEPLOY_ALARM_CONTACT_PHONE, history).apply();
        return true;
    }

    /**
     * 安全隐患，参考地点标签
     */
    public ArrayList<SecurityRisksTagModel> getSecurityRiskLocationTags(Context context) {
        String location = ContextUtils.getContext().getSharedPreferences(Constants.PREFERENCE_SECURITY_RISK_TAG, Context.MODE_PRIVATE)
                .getString(Constants.PREFERENCE_KEY_SECURITY_RISK_LOCATION, "");

        if (TextUtils.isEmpty(location)) {
            ArrayList<SecurityRisksTagModel> list = new ArrayList<>(4);
            SecurityRisksTagModel model1 = new SecurityRisksTagModel();
            model1.tag = context.getString(R.string.evacuation_walkway);
            list.add(model1);
            SecurityRisksTagModel model2 = new SecurityRisksTagModel();
            model2.tag = context.getString(R.string.fire_exits);
            list.add(model2);
            SecurityRisksTagModel model3 = new SecurityRisksTagModel();
            model3.tag = context.getString(R.string.safe_exit);
            list.add(model3);
            SecurityRisksTagModel model4 = new SecurityRisksTagModel();
            model4.tag = context.getString(R.string.indoor);
            list.add(model4);
            saveSecurityRiskLocationTag(list);
            return list;
        } else {
            ArrayList<SecurityRisksTagModel> list = new ArrayList<>();
            String[] split = location.split("#");
            for (String s : split) {
                SecurityRisksTagModel model = new SecurityRisksTagModel();
                model.tag = s;
                list.add(model);
            }
            return list;
        }
    }

    public void saveSecurityRiskLocationTag(ArrayList<SecurityRisksTagModel> list) {
        StringBuilder sb = new StringBuilder();
        if (list != null) {
            for (SecurityRisksTagModel model : list) {
                sb.append(model.tag);
                sb.append("#");
            }
        }
        ContextUtils.getContext().getSharedPreferences(Constants.PREFERENCE_SECURITY_RISK_TAG, Context.MODE_PRIVATE)
                .edit().putString(Constants.PREFERENCE_KEY_SECURITY_RISK_LOCATION, sb.toString()).apply();
    }

    /**
     * 安全隐患，参考行为标签
     *
     * @param context
     * @return
     */
    public ArrayList<SecurityRisksTagModel> getSecurityRiskBehaviorTags(Context context) {
        String location = ContextUtils.getContext().getSharedPreferences(Constants.PREFERENCE_SECURITY_RISK_TAG, Context.MODE_PRIVATE)
                .getString(Constants.PREFERENCE_KEY_SECURITY_RISK_BEHAVIOR, "");

        if (TextUtils.isEmpty(location)) {
            ArrayList<SecurityRisksTagModel> list = new ArrayList<>(4);
            SecurityRisksTagModel model1 = new SecurityRisksTagModel();
            model1.tag = context.getString(R.string.use_high_power_equipment);
            list.add(model1);
            SecurityRisksTagModel model2 = new SecurityRisksTagModel();
            model2.tag = context.getString(R.string.use_little_sun);
            list.add(model2);
            SecurityRisksTagModel model10 = new SecurityRisksTagModel();
            model10.tag = context.getString(R.string.use_electric_blanket);
            list.add(model10);

            SecurityRisksTagModel model3 = new SecurityRisksTagModel();
            model3.tag = context.getString(R.string.use_hot_fast);
            list.add(model3);
            SecurityRisksTagModel model4 = new SecurityRisksTagModel();
            model4.tag = context.getString(R.string.use_induction_cooker);
            list.add(model4);
            SecurityRisksTagModel model5 = new SecurityRisksTagModel();
            model5.tag = context.getString(R.string.use_coal_stove);
            list.add(model5);
            SecurityRisksTagModel model6 = new SecurityRisksTagModel();
            model6.tag = context.getString(R.string.use_lpg);
            list.add(model6);
            SecurityRisksTagModel model7 = new SecurityRisksTagModel();
            model7.tag = context.getString(R.string.electric_vehicle_charging);
            list.add(model7);
            SecurityRisksTagModel model8 = new SecurityRisksTagModel();
            model8.tag = context.getString(R.string.park_electric_car);
            list.add(model8);
            SecurityRisksTagModel model9 = new SecurityRisksTagModel();
            model9.tag = context.getString(R.string.private_pull_wire);
            list.add(model9);
            saveSecurityRiskBehaviorTag(list);
            return list;
        } else {
            ArrayList<SecurityRisksTagModel> list = new ArrayList<>();
            String[] split = location.split("#");
            for (String s : split) {
                SecurityRisksTagModel model = new SecurityRisksTagModel();
                model.tag = s;
                list.add(model);
            }
            return list;
        }
    }

    public void saveSecurityRiskBehaviorTag(ArrayList<SecurityRisksTagModel> list) {
        StringBuilder sb = new StringBuilder();
        if (list != null) {
            for (SecurityRisksTagModel model : list) {
                sb.append(model.tag);
                sb.append("#");
            }
        }
        ContextUtils.getContext().getSharedPreferences(Constants.PREFERENCE_SECURITY_RISK_TAG, Context.MODE_PRIVATE)
                .edit().putString(Constants.PREFERENCE_KEY_SECURITY_RISK_BEHAVIOR, sb.toString()).apply();
    }

    public void saveMyUUID(List<String> uuids) {
        if (uuids != null && !uuids.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < uuids.size(); i++) {
                sb.append(uuids.get(i));
                if (i != uuids.size() - 1) {
                    sb.append("#");
                }
            }
            ContextUtils.getContext().getSharedPreferences(Constants.PREFERENCE_UUID_SETTING_TAG, Context.MODE_PRIVATE)
                    .edit().putString(Constants.PREFERENCE_UUID_SETTING_MY_UUID_TAG, sb.toString()).apply();
        }
    }

    public List<String> getSaveMyUUID() {
        String data = ContextUtils.getContext().getSharedPreferences(Constants.PREFERENCE_UUID_SETTING_TAG, Context.MODE_PRIVATE)
                .getString(Constants.PREFERENCE_UUID_SETTING_MY_UUID_TAG, null);
        if (!TextUtils.isEmpty(data)) {
            String[] split = data.split("#");
            return Arrays.asList(split);
        }
        return null;
    }

    public boolean setIbeaconSettingData(IbeaconSettingData ibeaconSettingData) {
        if (ibeaconSettingData != null) {
            String json = RetrofitServiceHelper.getInstance().getGson().toJson(ibeaconSettingData);
            ContextUtils.getContext().getSharedPreferences(Constants.PREFERENCE_UUID_SETTING_TAG, Context.MODE_PRIVATE)
                    .edit().putString(Constants.PREFERENCE_UUID_SETTING_CURRENT_UUID_NO_SETTING_TAG, json).apply();
            return true;
        }
        return false;
    }

    public IbeaconSettingData getIbeaconSettingData() {
        String data = ContextUtils.getContext().getSharedPreferences(Constants.PREFERENCE_UUID_SETTING_TAG, Context.MODE_PRIVATE)
                .getString(Constants.PREFERENCE_UUID_SETTING_CURRENT_UUID_NO_SETTING_TAG, null);
        if (!TextUtils.isEmpty(data)) {
            return RetrofitServiceHelper.getInstance().getGson().fromJson(data, IbeaconSettingData.class);
        }
        return null;
    }

    public boolean setOfflineDeployData(LinkedHashMap<String, DeployAnalyzerModel> linkedHashMap) {
        EventLoginData userData = PreferencesHelper.getInstance().getUserData();
        if (userData != null && linkedHashMap != null) {
            String json = RetrofitServiceHelper.getInstance().getGson().toJson(linkedHashMap);
            ContextUtils.getContext().getSharedPreferences(Constants.OFFLINE_DEPLOYANALYZERMODEL_SP + userData.accountId, Context.MODE_PRIVATE)
                    .edit().putString(Constants.OFFLINE_DEPLOYANALYZERMODEL_KEY, json).apply();
            return true;
        }
        return false;
    }

    public LinkedHashMap<String, DeployAnalyzerModel> getOfflineDeployData() {
        EventLoginData userData = PreferencesHelper.getInstance().getUserData();
        if (userData != null) {
            String data = ContextUtils.getContext().getSharedPreferences(Constants.OFFLINE_DEPLOYANALYZERMODEL_SP + userData.accountId, Context.MODE_PRIVATE)
                    .getString(Constants.OFFLINE_DEPLOYANALYZERMODEL_KEY, null);
            if (!TextUtils.isEmpty(data)) {
                return RetrofitServiceHelper.getInstance().getGson().fromJson(data, new TypeToken<LinkedHashMap<String, DeployAnalyzerModel>>() {
                }.getType());
            }
        }
        return null;
    }


}
