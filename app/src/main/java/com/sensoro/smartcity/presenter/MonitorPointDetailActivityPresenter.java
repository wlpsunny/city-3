package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.RegeocodeRoad;
import com.amap.api.services.geocoder.StreetNumber;
import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.constant.ARouterConstants;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.helper.PreferencesHelper;
import com.sensoro.common.iwidget.IOnCreate;
import com.sensoro.common.iwidget.IOnResume;
import com.sensoro.common.iwidget.IOnStart;
import com.sensoro.common.manger.ThreadPoolManager;
import com.sensoro.common.model.DeployAnalyzerModel;
import com.sensoro.common.model.DeviceNotificationBean;
import com.sensoro.common.model.EventData;
import com.sensoro.common.model.ImageItem;
import com.sensoro.common.server.CityObserver;
import com.sensoro.common.server.RetrofitServiceHelper;
import com.sensoro.common.server.bean.AlarmInfo;
import com.sensoro.common.server.bean.DeployControlSettingData;
import com.sensoro.common.server.bean.DeviceCameraInfo;
import com.sensoro.common.server.bean.DeviceInfo;
import com.sensoro.common.server.bean.DeviceTypeStyles;
import com.sensoro.common.server.bean.DeviceUpdateFirmwareData;
import com.sensoro.common.server.bean.DisplayOptionsBean;
import com.sensoro.common.server.bean.MalfunctionDataBean;
import com.sensoro.common.server.bean.MalfunctionTypeStyles;
import com.sensoro.common.server.bean.MergeTypeStyles;
import com.sensoro.common.server.bean.MonitorOptionsBean;
import com.sensoro.common.server.bean.MonitorPointOperationTaskResultInfo;
import com.sensoro.common.server.bean.OtherBean;
import com.sensoro.common.server.bean.ScenesData;
import com.sensoro.common.server.bean.SensorStruct;
import com.sensoro.common.server.bean.SensorTypeStyles;
import com.sensoro.common.server.response.MonitorPointOperationRequestRsp;
import com.sensoro.common.server.response.ResponseResult;
import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.utils.DateUtil;
import com.sensoro.common.widgets.dialog.WarningContactDialogUtil;
import com.sensoro.libbleserver.ble.callback.OnDeviceUpdateObserver;
import com.sensoro.libbleserver.ble.callback.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.callback.SensoroWriteCallback;
import com.sensoro.libbleserver.ble.connection.SensoroDeviceConnection;
import com.sensoro.libbleserver.ble.entity.BLEDevice;
import com.sensoro.libbleserver.ble.entity.SensoroDevice;
import com.sensoro.libbleserver.ble.scanner.BLEDeviceListener;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.activity.AlarmHistoryLogActivity;
import com.sensoro.smartcity.activity.CameraListActivity;
import com.sensoro.smartcity.activity.SingleMonitorConfigurationActivity;
import com.sensoro.smartcity.activity.MonitorPointMapActivity;
import com.sensoro.smartcity.activity.MonitorPointMapENActivity;
import com.sensoro.smartcity.adapter.MonitorDetailOperationAdapter;
import com.sensoro.smartcity.adapter.model.EarlyWarningthresholdDialogUtilsAdapterModel;
import com.sensoro.common.model.MonitoringPointRcContentAdapterModel;
import com.sensoro.smartcity.analyzer.OperationCmdAnalyzer;
import com.sensoro.common.callback.BleObserver;
import com.sensoro.common.callback.OnConfigInfoObserver;
import com.sensoro.common.constant.MonitorPointOperationCode;
import com.sensoro.smartcity.constant.CityConstants;
import com.sensoro.smartcity.factory.MonitorPointModelsFactory;
import com.sensoro.smartcity.imainviews.IMonitorPointDetailActivityView;
import com.sensoro.smartcity.model.BleUpdateModel;
import com.sensoro.common.model.Elect3DetailModel;
import com.sensoro.smartcity.model.TaskOptionModel;
import com.sensoro.common.utils.LogUtils;
import com.sensoro.common.utils.WidgetUtil;
import com.sensoro.smartcity.widget.dialog.TipDeviceUpdateDialogUtils;
import com.sensoro.common.imagepicker.ImagePicker;
import com.sensoro.common.imagepicker.ui.ImagePreviewDelActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MonitorPointDetailActivityPresenter extends BasePresenter<IMonitorPointDetailActivityView> implements IOnCreate, IOnResume,
        GeocodeSearch.OnGeocodeSearchListener, MonitorDetailOperationAdapter.OnMonitorDetailOperationAdapterListener, BLEDeviceListener<BLEDevice>
        , TipDeviceUpdateDialogUtils.TipDialogUpdateClickListener, IOnStart {
    private Activity mContext;
    private volatile DeviceInfo mDeviceInfo;
    private String mScheduleNo;
    private GeocodeSearch geocoderSearch;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private volatile HashMap<String, BLEDevice> bleDeviceMap = new HashMap<>();
    private final Runnable DeviceTaskOvertime = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(DeviceTaskOvertime);
            mScheduleNo = null;
            if (isAttachedView()) {
                getView().dismissOperatingLoadingDialog();
                //若超时 去除显示超时对话框逻辑
//                getView().showErrorTipDialog(mContext.getString(R.string.operation_request_time_out));
            }
        }
    };
    private final BleUpdateModel bleUpdateModel = new BleUpdateModel();
    private final Runnable bleRunnable = new Runnable() {
        @Override
        public void run() {
            boolean bleHasOpen = SensoroCityApplication.getInstance().bleDeviceManager.isBluetoothEnabled();
            if (bleHasOpen) {
                try {
                    bleHasOpen = SensoroCityApplication.getInstance().bleDeviceManager.startService();
                    LogUtils.loge("服务开启 ：" + bleHasOpen);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            mHandler.postDelayed(this, 1000);
        }
    };
    private final ArrayList<EarlyWarningthresholdDialogUtilsAdapterModel> mEarlyWarningThresholdDialogUtilsAdapterModels = new ArrayList<>();
    private SensoroDeviceConnection sensoroDeviceConnection;
    private String mOperationType;
    private volatile int deviceDemoMode = Constants.DEVICE_DEMO_MODE_NOT_SUPPORT;
    private ArrayList<DeviceCameraInfo> deviceCameras;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        onCreate();
        mDeviceInfo = (DeviceInfo) mContext.getIntent().getSerializableExtra(Constants.EXTRA_DEVICE_INFO);
        geocoderSearch = new GeocodeSearch(mContext);
        geocoderSearch.setOnGeocodeSearchListener(this);
        bleUpdateModel.sn = mDeviceInfo.getSn();
        bleUpdateModel.deviceType = mDeviceInfo.getDeviceType();
        bleUpdateModel.filePath = mContext.getExternalFilesDir(null) + File.separator + "bleDeviceUpdatePackage";
        BleObserver.getInstance().registerBleObserver(this);
        requestDeviceRecentLog();
    }

    private void freshTopData() {
        String sn = mDeviceInfo.getSn();
        getView().setSNText(sn);
        String typeName = mContext.getString(R.string.power_supply);
        String mergeType = mDeviceInfo.getMergeType();
        String deviceType = mDeviceInfo.getDeviceType();
        if (TextUtils.isEmpty(mergeType)) {
            mergeType = WidgetUtil.handleMergeType(deviceType);
        }
        String signal = mDeviceInfo.getSignal();
        DeviceTypeStyles configDeviceType = PreferencesHelper.getInstance().getConfigDeviceType(deviceType);
        if (configDeviceType != null) {
            String category = configDeviceType.getCategory();
            if (TextUtils.isEmpty(category)) {
                category = mContext.getString(R.string.unknown);
            }
            getView().setMonitorDetailTvCategory(category);
        }
        MergeTypeStyles mergeTypeStyles = PreferencesHelper.getInstance().getConfigMergeType(mergeType);
        if (mergeTypeStyles != null) {
            typeName = mergeTypeStyles.getName();
        }
        getView().setDeviceTypeName(typeName);

        List<String> deployPics = mDeviceInfo.getDeployPics();
        if (deployPics != null && deployPics.size() > 0) {
            ArrayList<ScenesData> list = new ArrayList<>();
            for (String url : deployPics) {
                ScenesData scenesData = new ScenesData();
                scenesData.url = url;
                list.add(scenesData);
            }
            getView().updateMonitorPhotos(list);
        }
        int status = mDeviceInfo.getStatus();
        int resId = R.drawable.signal_none;
        if (Constants.SENSOR_STATUS_LOST == status || Constants.SENSOR_STATUS_INACTIVE == status) {
            getView().setSignalStatus(resId, mContext.getString(R.string.s_none));
        } else {
            if (!TextUtils.isEmpty(signal)) {
                switch (signal) {
                    case "good":
                        resId = R.drawable.signal_good;
                        getView().setSignalStatus(resId, mContext.getString(R.string.s_good));
                        break;
                    case "normal":
                        resId = R.drawable.signal_normal;
                        getView().setSignalStatus(resId, mContext.getString(R.string.s_normal));
                        break;
                    case "bad":
                        resId = R.drawable.signal_bad;
                        getView().setSignalStatus(resId, mContext.getString(R.string.s_bad));
                        break;
                    default:
                        resId = R.drawable.signal_none;
                        getView().setSignalStatus(resId, mContext.getString(R.string.s_none));
                        break;
                }
            } else {
                getView().setSignalStatus(resId, mContext.getString(R.string.s_none));
            }
        }
        String statusText;
        int textColor;
        switch (status) {
            case Constants.SENSOR_STATUS_ALARM:
                textColor = mContext.getResources().getColor(R.color.c_f34a4a);
                statusText = mContext.getString(R.string.main_page_warn);
                break;
            case Constants.SENSOR_STATUS_NORMAL:
                textColor = mContext.getResources().getColor(R.color.c_1dbb99);
                statusText = mContext.getString(R.string.normal);
                break;
            case Constants.SENSOR_STATUS_LOST:
                textColor = mContext.getResources().getColor(R.color.c_5d5d5d);
                statusText = mContext.getString(R.string.status_lost);
                break;
            case Constants.SENSOR_STATUS_INACTIVE:
                textColor = mContext.getResources().getColor(R.color.c_b6b6b6);
                statusText = mContext.getString(R.string.status_inactive);
                break;
            case Constants.SENSOR_STATUS_MALFUNCTION:
                textColor = mContext.getResources().getColor(R.color.c_fdc83b);
                statusText = mContext.getString(R.string.status_malfunction);
                break;
            default:
                textColor = mContext.getResources().getColor(R.color.c_1dbb99);
                statusText = mContext.getString(R.string.normal);
                break;
        }
        String name = mDeviceInfo.getName();
        getView().setStatusInfo(statusText, textColor);
        getView().setTitleNameTextView(TextUtils.isEmpty(name) ? sn : name);


        AlarmInfo alarms = mDeviceInfo.getAlarms();
        if (alarms != null) {
            //设置共x人
            List<DeviceNotificationBean> notifications = WidgetUtil.handleDeviceNotifications(alarms.getNotifications());
            if (notifications.size() > 0) {
                DeviceNotificationBean deviceNotificationBean = notifications.get(0);
                String contact = deviceNotificationBean.getContact();
                if (!TextUtils.isEmpty(contact)) {
                    getView().setContractName(contact);
                }
                String content = deviceNotificationBean.getContent();
                if (!TextUtils.isEmpty(content)) {
                    getView().setContractPhone(content);
                }
                getView().setContractCount(notifications.size());
            } else {
                DeviceNotificationBean notification = alarms.getNotification();
                if (notification != null) {
                    String contact = notification.getContact();
                    if (!TextUtils.isEmpty(contact)) {
                        getView().setContractName(contact);
                    }
                    String content = notification.getContent();
                    if (!TextUtils.isEmpty(content)) {
                        getView().setContractPhone(content);
                        getView().setContractCount(1);
                    } else {
                        getView().setNoContact();
                    }

                } else {
                    getView().setNoContact();
                }
            }
        } else {
            getView().setNoContact();
        }


        long updatedTime = mDeviceInfo.getUpdatedTime();
        if (updatedTime == 0) {
            getView().setUpdateTime("-");
        } else {
            getView().setUpdateTime(DateUtil.getStrTimeTodayByDevice(mContext, updatedTime));
        }
        List<String> tags = mDeviceInfo.getTags();
        if (tags != null && tags.size() > 0) {
            getView().updateTags(tags);

        }
        Map<String, SensorStruct> sensoroDetails = mDeviceInfo.getSensoroDetails();
        if (sensoroDetails != null) {
            SensorStruct batteryStruct = sensoroDetails.get("battery");
            if (batteryStruct != null) {
                String battery = batteryStruct.getValue().toString();
                if (battery.equals("-1.0") || battery.equals("-1")) {
                    getView().setBatteryInfo(mContext.getString(R.string.power_supply));
                } else {
                    getView().setBatteryInfo(WidgetUtil.subZeroAndDot(battery) + "%");
                }
            }
        }
        Integer interval = mDeviceInfo.getInterval();
        if (interval != null) {
            getView().setInterval(DateUtil.secToTimeBefore(mContext, interval));
        }
        //TODO 特殊配置
        if (CityConstants.DEVICE_2G_CONFIG_DEVICE_TYPES.contains(deviceType)) {
            //针对特殊设备需要下行30s
            //带有2g的 配置参数
            getView().set2GDeviceConfigVisible(true);
            //TODO 动态设置叫iccid还是ccid
            if ("acrel300T_fires_2G".equals(deviceType) || "acrel300D_fires_2G".equals(deviceType)) {
                getView().setMonitorDetailTitleIccid(mContext.getString(R.string.device_iccid) + "（CCID）");
            } else {
                getView().setMonitorDetailTitleIccid(mContext.getString(R.string.device_iccid) + "（ICCID）");
            }
            OtherBean other = mDeviceInfo.getOther();
            if (other != null) {
                String imei = other.getImei();
                String imsi = other.getImsi();
                String iccid = other.getIccid();
                getView().set2GDeviceConfigInfo(imei, imsi, iccid);
            }
        }
    }

    private void refreshOperationStatus() {
        int status = mDeviceInfo.getStatus();
        HashMap<String, TaskOptionModel> taskOptionModelMap = MonitorPointModelsFactory.createTaskOptionModelMap(status);
        DeviceTypeStyles configDeviceType = PreferencesHelper.getInstance().getConfigDeviceType(mDeviceInfo.getDeviceType());
        if (configDeviceType != null) {
            List<String> taskOptions = configDeviceType.getTaskOptions();
            Map<String, String> taskFirmwareVersion = configDeviceType.getTaskFirmwareVersion();

            ArrayList<TaskOptionModel> optionModelList = getOptionModelList(taskOptions, taskFirmwareVersion, taskOptionModelMap);
            if (optionModelList != null) {
                getView().setDeviceOperationVisible(true);
                getView().updateTaskOptionModelAdapter(optionModelList);
            } else {
                getView().setDeviceOperationVisible(false);
            }
        }
        //正常状态下也开启蓝牙
        if (status == Constants.SENSOR_STATUS_ALARM || status == Constants.SENSOR_STATUS_MALFUNCTION
                || status == Constants.SENSOR_STATUS_NORMAL || PreferencesHelper.getInstance().getUserData().hasDeviceFirmwareUpdate
                || PreferencesHelper.getInstance().getUserData().hasDeviceDemoMode) {
            mHandler.removeCallbacks(bleRunnable);
            mHandler.post(bleRunnable);
        }
    }

    private ArrayList<TaskOptionModel> getOptionModelList(List<String> taskOptions, Map<String, String> taskFirmwareVersion,
                                                          HashMap<String, TaskOptionModel> taskOptionModelMap) {
        ArrayList<TaskOptionModel> taskOptionModelList = new ArrayList<>();
        if (taskOptions != null && taskOptions.size() > 0) {
            if (TextUtils.isEmpty(bleUpdateModel.currentFirmVersion)) {
                for (String string : taskOptions) {
                    TaskOptionModel taskOptionModel = taskOptionModelMap.get(string);
                    if (taskOptionModel != null) {
                        if (taskFirmwareVersion != null) {
                            if (!taskFirmwareVersion.containsKey(taskOptionModel.id)) {
                                taskOptionModelList.add(taskOptionModel);
                            }
                        } else {
                            taskOptionModelList.add(taskOptionModel);
                        }

                    }
                }
                return taskOptionModelList;

            } else {
                if (taskFirmwareVersion != null && taskFirmwareVersion.size() > 0) {
                    for (String string : taskOptions) {
                        TaskOptionModel taskOptionModel = taskOptionModelMap.get(string);
                        if (taskOptionModel != null) {
                            if (taskFirmwareVersion.containsKey(taskOptionModel.id)) {
                                String version = taskFirmwareVersion.get(taskOptionModel.id);
                                if (TextUtils.isEmpty(version) || TextUtils.isEmpty(version.trim())) {
                                    taskOptionModelList.add(taskOptionModel);
                                } else {
                                    String[] split = version.trim().split("~");
                                    if (split.length == 1 && WidgetUtil.isContainVersion(split[0], bleUpdateModel.currentFirmVersion)) {
                                        taskOptionModelList.add(taskOptionModel);
                                    } else if (split.length > 1 && WidgetUtil.isContainVersion(split[0], bleUpdateModel.currentFirmVersion)
                                            && WidgetUtil.isNewVersion(bleUpdateModel.currentFirmVersion, split[1])) {
                                        taskOptionModelList.add(taskOptionModel);
                                    }
                                }
                            } else {
                                taskOptionModelList.add(taskOptionModel);
                            }
                        }
                    }
                    return taskOptionModelList;
                } else {
                    //没有版本限制,所有的都加进去
                    for (String string : taskOptions) {
                        TaskOptionModel taskOptionModel = taskOptionModelMap.get(string);
                        if (taskOptionModel != null) {
                            taskOptionModelList.add(taskOptionModel);
                        }
                    }
                    return taskOptionModelList;
                }

            }
        }
        return null;

    }

    private void freshLocationDeviceInfo() {
        List<Double> lonlat = mDeviceInfo.getLonlat();
        try {
            double v = lonlat.get(1);
            double v1 = lonlat.get(0);
            if (v == 0 || v1 == 0) {
                getView().setDeviceLocation(mContext.getString(R.string.not_positioned), false);
                getView().setDeviceLocationTextColor(R.color.c_a6a6a6);
                return;
            }
            RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(v, v1), 200, GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);
        } catch (Exception e) {
            e.printStackTrace();
            getView().setDeviceLocationTextColor(R.color.c_a6a6a6);
            getView().setDeviceLocation(mContext.getString(R.string.not_positioned), false);
        }
    }

    private void requestDeviceRecentLog() {
        getView().showProgressDialog();
        requestBlePassword();
        //合并请求
//        RetrofitServiceHelper.getInstance().getDeviceDetailInfoList(sn, null, 1).subscribeOn(Schedulers.io()).observeOn
//                (AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceInfoListRsp>(this) {
//            @Override
//            public void onDeployCompleted(DeviceInfoListRsp deviceInfoListRsp) {
//                if (deviceInfoListRsp.getData().size() > 0) {
//                    mDeviceInfo = deviceInfoListRsp.getData().get(0);
//                }
//                requestBlePassword();
//                freshLocationDeviceInfo();
//                handleDeployInfo();
//                handleDeviceModeInfo();
//                freshTopData();
//                handleDeviceInfoAdapter();
//                getView().dismissProgressDialog();
//            }
//
//            @Override
//            public void onDeployErrorMsg(int errorCode, String errorMsg) {
//                requestBlePassword();
//                handleDeployInfo();
//                handleDeviceModeInfo();
//                getView().dismissProgressDialog();
//                getView().toastShort(errorMsg);
//            }
//        });
    }

    private void handleDeviceModeInfo() {
        if (mDeviceInfo != null) {
            DeviceTypeStyles configDeviceType = PreferencesHelper.getInstance().getConfigDeviceType(mDeviceInfo.getDeviceType());
            if (configDeviceType != null) {
                if (configDeviceType.isDemoSupported()) {
                    if ("fhsj_smoke".equals(mDeviceInfo.getDeviceType())) {
                        if (WidgetUtil.isContainVersion("2.1.1", bleUpdateModel.currentFirmVersion)) {
                            //只针对泛海三江烟感并且在2.1.1版本及以上
                            if (PreferencesHelper.getInstance().getUserData().hasDeviceDemoMode) {
                                Integer demoMode = mDeviceInfo.getDemoMode();
                                if (demoMode != null) {
                                    switch (demoMode) {
                                        case 0:
                                            //正常模式
                                            deviceDemoMode = Constants.DEVICE_DEMO_MODE_CLOSE;
                                            break;
                                        case 1:
                                            //演示模式
                                            deviceDemoMode = Constants.DEVICE_DEMO_MODE_OPEN;
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            } else {
                                deviceDemoMode = Constants.DEVICE_DEMO_MODE_NO_PERMISSION;
                            }
                        } else {
                            deviceDemoMode = Constants.DEVICE_DEMO_MODE_NOT_SUPPORT;
                        }

                    } else {
                        if (PreferencesHelper.getInstance().getUserData().hasDeviceDemoMode) {
                            Integer demoMode = mDeviceInfo.getDemoMode();
                            if (demoMode != null) {
                                switch (demoMode) {
                                    case 0:
                                        //正常模式
                                        deviceDemoMode = Constants.DEVICE_DEMO_MODE_CLOSE;
                                        break;
                                    case 1:
                                        //演示模式
                                        deviceDemoMode = Constants.DEVICE_DEMO_MODE_OPEN;
                                        break;
                                    default:
                                        break;
                                }
                            }
                        } else {
                            deviceDemoMode = Constants.DEVICE_DEMO_MODE_NO_PERMISSION;
                        }
                    }
                } else {
                    deviceDemoMode = Constants.DEVICE_DEMO_MODE_NOT_SUPPORT;
                }
            }
            //TODO delete
//            deviceDemoMode = DEVICE_DEMO_MODE_OPEN;
            getView().setDeviceDemoModeViewStatus(deviceDemoMode);
        }
    }

    private void handleDeployInfo() {
        if (mDeviceInfo != null) {
            //部署时间
            Long createdTime = mDeviceInfo.getDeployTime();
            if (createdTime != null) {
                getView().setMonitorDeployTime(DateUtil.getFullDate(createdTime));
            }
            //配置信息
            setMonitorConfigInfo(mDeviceInfo.getConfig());
        }
    }

    private void setMonitorConfigInfo(DeployControlSettingData deployControlSettingData) {
        if (CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(mDeviceInfo.getDeviceType())) {
            mDeviceInfo.setConfig(deployControlSettingData);
            final String[] values = {"-", "-"};
            if (deployControlSettingData != null) {
                Integer switchSpec = deployControlSettingData.getSwitchSpec();
                if (switchSpec != null) {
                    values[0] = switchSpec + "A";
                }
            }
            if (hasNesConfigInfo(deployControlSettingData)) {
                //新数据
                Integer transformer = deployControlSettingData.getTransformer();
                if (transformer != null) {
                    values[1] = transformer + "A";
                }
                getView().setDeviceDetailConfigInfo(mContext.getString(R.string.actual_overcurrent_threshold) + ":" + values[0], mContext.getString(R.string.device_detail_config_trans) + ":" + values[1]);
            } else {
                //传统数据
                getView().setDeviceDetailConfigInfo(mContext.getString(R.string.actual_overcurrent_threshold) + ":" + values[0], null);
            }
//            final String[] switchSpecStr = {"-", "-", "-"};
//            if (deployControlSettingData != null) {
//                Integer switchSpec = deployControlSettingData.getSwitchSpec();
//                if (switchSpec != null) {
//                    switchSpecStr[0] = switchSpec + "A";
//                }
//                Integer wireMaterial = deployControlSettingData.getWireMaterial();
//                if (wireMaterial != null) {
//                    switch (wireMaterial) {
//                        case 0:
//                            switchSpecStr[1] = mContext.getString(R.string.cu);
//                            break;
//                        case 1:
//                            switchSpecStr[1] = mContext.getString(R.string.al);
//                            break;
//                        default:
//                            break;
//                    }
//                }
//                Double wireDiameter = deployControlSettingData.getWireDiameter();
//                if (wireDiameter != null) {
//                    String formatDouble = WidgetUtil.getFormatDouble(wireDiameter, 2);
//                    switchSpecStr[2] = formatDouble + "m㎡";
//                }
//            }
//            mContext.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (isAttachedView()) {
//                        getView().setMonitorSwitchSpec(switchSpecStr[0]);
//                        getView().setMonitorWireMaterial(switchSpecStr[1]);
//                        getView().setMonitorWireDiameter(switchSpecStr[2]);
//                    }
//
//                }
//            });

        }
    }

    private boolean hasNesConfigInfo(DeployControlSettingData deployControlSettingData) {
        //三相电中 并且有新数据
        if (CityConstants.DEVICE_CONTROL_NEW_CONFIG_DEVICE_TYPES.contains(mDeviceInfo.getDeviceType())) {
            if (deployControlSettingData != null) {
                List<DeployControlSettingData.wireData> inputList = deployControlSettingData.getInput();
                List<DeployControlSettingData.wireData> outputList = deployControlSettingData.getOutput();
                return inputList != null && inputList.size() > 0 && outputList != null && outputList.size() > 0;
            }
        }
        return false;

    }

    private void requestBlePassword() {
        // 获取固件版本和下载固件的地址信息
        RetrofitServiceHelper.getInstance().getDeployDeviceDetail(mDeviceInfo.getSn(), null, null).subscribeOn
                (Schedulers.io()).flatMap(new Function<ResponseResult<DeviceInfo>, ObservableSource<ResponseResult<List<DeviceUpdateFirmwareData>>>>() {
            @Override
            public ObservableSource<ResponseResult<List<DeviceUpdateFirmwareData>>> apply(ResponseResult<DeviceInfo> deployDeviceDetailRsp) throws Exception {
                DeviceInfo data = deployDeviceDetailRsp.getData();
                if (data != null) {
                    mDeviceInfo = data;
                    bleUpdateModel.blePassword = data.getBlePassword();
                    bleUpdateModel.currentFirmVersion = data.getFirmwareVersion();
                    bleUpdateModel.band = data.getBand();
                    bleUpdateModel.hardwareVersion = data.getHardwareVersion();
                    try {
                        LogUtils.loge("升级--->> 版本信息 ： " + bleUpdateModel.currentFirmVersion);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
                return RetrofitServiceHelper.getInstance().getDeviceUpdateVision(bleUpdateModel.sn, bleUpdateModel.deviceType, bleUpdateModel.band, bleUpdateModel.currentFirmVersion, bleUpdateModel.hardwareVersion, 1, 100);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<List<DeviceUpdateFirmwareData>>>(this) {

            @Override
            public void onCompleted(ResponseResult<List<DeviceUpdateFirmwareData>> deviceUpdateFirmwareDataRsp) {
                List<DeviceUpdateFirmwareData> data = deviceUpdateFirmwareDataRsp.getData();
                if (data != null && data.size() > 0) {
                    DeviceUpdateFirmwareData deviceUpdateFirmwareData = data.get(0);
                    if (deviceUpdateFirmwareData != null) {
                        bleUpdateModel.serverFirmVersion = deviceUpdateFirmwareData.getVersion();
                        bleUpdateModel.firmUrl = deviceUpdateFirmwareData.getUrl();
                        bleUpdateModel.serverFirmCreateTime = deviceUpdateFirmwareData.getCreatedTime();
                    }
                    for (DeviceUpdateFirmwareData firmwareData : data) {
                        if (bleUpdateModel.currentFirmVersion != null) {
                            if (bleUpdateModel.currentFirmVersion.equals(firmwareData.getVersion())) {
                                bleUpdateModel.currentFirmCreateTime = firmwareData.getCreatedTime();
                                break;
                            }
                        }

                    }
                }
                //加入摄像头权限检查
                if (PreferencesHelper.getInstance().getUserData().hasDeviceCameraList && AppUtils.isChineseLanguage()) {
                    String id = mDeviceInfo.getDeviceGroup();
                    if (!TextUtils.isEmpty(id)) {
                        RetrofitServiceHelper.getInstance().getDeviceGroupCameraList(id, Constants.DEFAULT_PAGE_SIZE, 1, null).subscribeOn(Schedulers.io()).observeOn
                                (AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<List<DeviceCameraInfo>>>(MonitorPointDetailActivityPresenter.this) {
                            @Override
                            public void onCompleted(ResponseResult<List<DeviceCameraInfo>> deviceCameraListRsp) {
                                deviceCameras = (ArrayList<DeviceCameraInfo>) deviceCameraListRsp.getData();
                                if (deviceCameras != null && deviceCameras.size() > 0) {
                                    getView().setDeviceCamerasText(mContext.getString(R.string.device_detail_camera_has_camera) + deviceCameras.size() + mContext.getString(R.string.device_detail_camera_camera_count));
                                }
                            }

                            @Override
                            public void onErrorMsg(int errorCode, String errorMsg) {
                                getView().toastShort(errorMsg);
                            }
                        });
                    }
                }
                refreshOperationStatus();
                freshDeviceUpdateVersionInfo();
                freshLocationDeviceInfo();
                handleDeployInfo();
                handleDeviceModeInfo();
                freshTopData();
                handleDeviceInfoAdapter();
                getView().dismissProgressDialog();
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                refreshOperationStatus();
                freshDeviceUpdateVersionInfo();

                if (mDeviceInfo != null) {
                    freshLocationDeviceInfo();
                    handleDeployInfo();
                    handleDeviceModeInfo();
                    freshTopData();
                    handleDeviceInfoAdapter();
                    getView().dismissProgressDialog();
                }

            }
        });
    }

    private void freshDeviceUpdateVersionInfo() {
        boolean hasNewVersion = WidgetUtil.isNewVersion(bleUpdateModel.currentFirmVersion, bleUpdateModel.serverFirmVersion);
        if (!PreferencesHelper.getInstance().getUserData().hasDeviceFirmwareUpdate) {
            hasNewVersion = false;
        }
        getView().setIvHasNewVersionViewVisible(hasNewVersion);
        if (!TextUtils.isEmpty(bleUpdateModel.currentFirmVersion)) {
            getView().setDeviceVision("V " + bleUpdateModel.currentFirmVersion);
        }
    }

    private void handleDeviceInfoAdapter() {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (mDeviceInfo != null) {
                    final ArrayList<MonitoringPointRcContentAdapterModel> malfunctionBeanData = new ArrayList<>();
                    if (mDeviceInfo.getStatus() == Constants.SENSOR_STATUS_MALFUNCTION) {
                        Map<String, MalfunctionDataBean> malfunctionData = mDeviceInfo.getMalfunctionData();
                        //TODO 添加故障字段数组
                        if (malfunctionData != null) {
                            LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
                            Set<Map.Entry<String, MalfunctionDataBean>> entrySet = malfunctionData.entrySet();
                            if (entrySet != null) {
                                for (Map.Entry<String, MalfunctionDataBean> entry : entrySet) {
                                    MalfunctionDataBean entryValue = entry.getValue();
                                    if (entryValue != null) {
                                        Map<String, MalfunctionDataBean> details = entryValue.getDetails();
                                        if (details != null) {
                                            Set<String> keySet = details.keySet();
                                            if (keySet != null) {
                                                linkedHashSet.addAll(keySet);
                                            }
                                        }
                                    }
                                }
                            }
                            ArrayList<String> keyList = new ArrayList<>(linkedHashSet);
                            Collections.sort(keyList);
                            for (String key : keyList) {
                                MonitoringPointRcContentAdapterModel monitoringPointRcContentAdapterModel = new MonitoringPointRcContentAdapterModel();
                                monitoringPointRcContentAdapterModel.name = mContext.getString(R.string.malfunction_cause_detail);
                                monitoringPointRcContentAdapterModel.statusColorId = R.color.c_fdc83b;
                                MalfunctionTypeStyles configMalfunctionSubTypes = PreferencesHelper.getInstance().getConfigMalfunctionSubTypes(key);
                                if (configMalfunctionSubTypes != null) {
                                    monitoringPointRcContentAdapterModel.content = configMalfunctionSubTypes.getName();
                                }
                                if (TextUtils.isEmpty(monitoringPointRcContentAdapterModel.content)) {
                                    monitoringPointRcContentAdapterModel.content = mContext.getString(R.string.unknown);
                                }
                                malfunctionBeanData.add(monitoringPointRcContentAdapterModel);
                            }
                        }
                    }
                    //
                    String deviceType = mDeviceInfo.getDeviceType();
                    boolean needTop = false;
                    DeviceTypeStyles configDeviceType = PreferencesHelper.getInstance().getConfigDeviceType(deviceType);
                    Map<String, SensorStruct> sensoroDetails = mDeviceInfo.getSensoroDetails();
                    final ArrayList<MonitoringPointRcContentAdapterModel> dataBean = new ArrayList<>();
                    boolean hasAlarmStatus = false;
                    if (configDeviceType != null) {
                        //预警阈值信息处理
                        List<MonitorOptionsBean> monitorOptions = configDeviceType.getMonitorOptions();
                        handleEarlyWarningThresholdModel(monitorOptions);
                        //特殊头部展示
                        DisplayOptionsBean displayOptions = configDeviceType.getDisplayOptions();

                        if (displayOptions != null) {
                            List<String> majors = displayOptions.getMajors();
                            if (majors != null && majors.size() > 0) {
                                String sensoroType = majors.get(0);
                                if (!TextUtils.isEmpty(sensoroType)) {
                                    // 控制头部
                                    needTop = true;
                                    final MonitoringPointRcContentAdapterModel model = MonitorPointModelsFactory.createMonitoringPointRcContentAdapterModel(mContext, mDeviceInfo, sensoroDetails, sensoroType);
                                    if (model != null) {
                                        mContext.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (isAttachedView()) {
                                                    if (TextUtils.isEmpty(model.unit)) {
                                                        getView().setTopElectData(model.content, model.statusColorId, model.name);
                                                    } else {
                                                        getView().setTopElectData(model.content + model.unit, model.statusColorId, model.name);
                                                    }
                                                }
                                            }
                                        });

                                    }
                                }
                            }
                            List<String> minors = displayOptions.getMinors();
                            if (minors != null && minors.size() > 0) {
                                for (String type : minors) {
                                    MonitoringPointRcContentAdapterModel model = MonitorPointModelsFactory.createMonitoringPointRcContentAdapterModel(mContext, mDeviceInfo, sensoroDetails, type);
                                    if (model != null) {
                                        if (TextUtils.isEmpty(model.content)) {
                                            model.content = "-";
                                        }
                                        if (model.hasAlarmStatus()) {
                                            hasAlarmStatus = true;
                                        }
                                        dataBean.add(model);
                                    }
                                }
                                // 控制展开
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (isAttachedView()) {
                                            getView().updateDeviceInfoAdapter(dataBean);
                                        }
                                    }
                                });
                            }
                            // 控制九宫格显示
                            DisplayOptionsBean.SpecialBean special = displayOptions.getSpecial();
                            if (special != null) {
                                String type = special.getType();
                                if ("table".equals(type)) {
                                    final List<List<DisplayOptionsBean.SpecialBean.DataBean>> specialData = special.getData();
                                    if (specialData != null && specialData.size() >= 4) {
                                        List<DisplayOptionsBean.SpecialBean.DataBean> dataBeans0 = specialData.get(0);
                                        if (dataBeans0 != null && dataBeans0.size() >= 4) {
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean01 = dataBeans0.get(1);
                                            final Elect3DetailModel elect3TopModel1 = MonitorPointModelsFactory.createElect3NameModel(mContext, 1, dataBean01);
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean02 = dataBeans0.get(2);
                                            final Elect3DetailModel elect3TopModel2 = MonitorPointModelsFactory.createElect3NameModel(mContext, 2, dataBean02);
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean03 = dataBeans0.get(3);
                                            final Elect3DetailModel elect3TopModel3 = MonitorPointModelsFactory.createElect3NameModel(mContext, 3, dataBean03);
                                            mContext.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isAttachedView()) {
                                                        if (elect3TopModel1 != null) {
                                                            getView().set3ElectTopDetail(elect3TopModel1);
                                                        }
                                                        if (elect3TopModel2 != null) {
                                                            getView().set3ElectTopDetail(elect3TopModel2);
                                                        }
                                                        if (elect3TopModel3 != null) {
                                                            getView().set3ElectTopDetail(elect3TopModel3);
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        List<DisplayOptionsBean.SpecialBean.DataBean> dataBeans1 = specialData.get(1);
                                        if (dataBeans1 != null && dataBeans1.size() >= 4) {
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean10 = dataBeans1.get(0);
                                            final Elect3DetailModel elect3NameModel10 = MonitorPointModelsFactory.createElect3NameModel(mContext, 0, dataBean10);
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean11 = dataBeans1.get(1);
                                            final Elect3DetailModel elect3DetailModel1 = MonitorPointModelsFactory.createElect3DetailModel(mDeviceInfo, 1, dataBean11, sensoroDetails);
                                            if (elect3DetailModel1 != null) {
                                                if (elect3DetailModel1.hasAlarmStatus()) {
                                                    hasAlarmStatus = true;
                                                }
                                            }
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean12 = dataBeans1.get(2);
                                            final Elect3DetailModel elect3DetailModel2 = MonitorPointModelsFactory.createElect3DetailModel(mDeviceInfo, 2, dataBean12, sensoroDetails);
                                            if (elect3DetailModel2 != null) {
                                                if (elect3DetailModel2.hasAlarmStatus()) {
                                                    hasAlarmStatus = true;
                                                }
                                            }
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean13 = dataBeans1.get(3);
                                            final Elect3DetailModel elect3DetailModel3 = MonitorPointModelsFactory.createElect3DetailModel(mDeviceInfo, 3, dataBean13, sensoroDetails);
                                            if (elect3DetailModel3 != null) {
                                                if (elect3DetailModel3.hasAlarmStatus()) {
                                                    hasAlarmStatus = true;
                                                }
                                            }
                                            mContext.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isAttachedView()) {
                                                        if (elect3NameModel10 != null) {
                                                            getView().set3ElectVDetail(elect3NameModel10);
                                                        }
                                                        if (elect3DetailModel1 != null) {
                                                            getView().set3ElectVDetail(elect3DetailModel1);

                                                        }
                                                        if (elect3DetailModel2 != null) {
                                                            getView().set3ElectVDetail(elect3DetailModel2);
                                                        }
                                                        if (elect3DetailModel3 != null) {
                                                            getView().set3ElectVDetail(elect3DetailModel3);

                                                        }
                                                    }
                                                }
                                            });
                                        }
//
                                        List<DisplayOptionsBean.SpecialBean.DataBean> dataBeans2 = specialData.get(2);
                                        if (dataBeans2 != null && dataBeans2.size() >= 4) {
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean20 = dataBeans2.get(0);
                                            final Elect3DetailModel elect3NameModel20 = MonitorPointModelsFactory.createElect3NameModel(mContext, 0, dataBean20);
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean21 = dataBeans2.get(1);
                                            final Elect3DetailModel elect3DetailModel1 = MonitorPointModelsFactory.createElect3DetailModel(mDeviceInfo, 1, dataBean21, sensoroDetails);
                                            if (elect3DetailModel1 != null) {
                                                if (elect3DetailModel1.hasAlarmStatus()) {
                                                    hasAlarmStatus = true;
                                                }
                                            }
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean22 = dataBeans2.get(2);
                                            final Elect3DetailModel elect3DetailModel2 = MonitorPointModelsFactory.createElect3DetailModel(mDeviceInfo, 2, dataBean22, sensoroDetails);
                                            if (elect3DetailModel2 != null) {
                                                if (elect3DetailModel2.hasAlarmStatus()) {
                                                    hasAlarmStatus = true;
                                                }
                                            }
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean23 = dataBeans2.get(3);
                                            final Elect3DetailModel elect3DetailModel3 = MonitorPointModelsFactory.createElect3DetailModel(mDeviceInfo, 3, dataBean23, sensoroDetails);
                                            if (elect3DetailModel3 != null) {
                                                if (elect3DetailModel3.hasAlarmStatus()) {
                                                    hasAlarmStatus = true;
                                                }
                                            }
                                            mContext.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isAttachedView()) {
                                                        if (elect3NameModel20 != null) {
                                                            getView().set3ElectADetail(elect3NameModel20);
                                                        }
                                                        if (elect3DetailModel1 != null) {
                                                            getView().set3ElectADetail(elect3DetailModel1);
                                                        }
                                                        if (elect3DetailModel2 != null) {
                                                            getView().set3ElectADetail(elect3DetailModel2);

                                                        }
                                                        if (elect3DetailModel3 != null) {
                                                            getView().set3ElectADetail(elect3DetailModel3);
                                                        }
                                                    }
                                                }
                                            });
                                        }

                                        List<DisplayOptionsBean.SpecialBean.DataBean> dataBeans3 = specialData.get(3);
                                        if (dataBeans3 != null && dataBeans3.size() >= 3) {
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean30 = dataBeans3.get(0);
                                            final Elect3DetailModel elect3NameModel30 = MonitorPointModelsFactory.createElect3NameModel(mContext, 0, dataBean30);
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean31 = dataBeans3.get(1);
                                            final Elect3DetailModel elect3DetailModel1 = MonitorPointModelsFactory.createElect3DetailModel(mDeviceInfo, 1, dataBean31, sensoroDetails);
                                            if (elect3DetailModel1 != null) {
                                                if (elect3DetailModel1.hasAlarmStatus()) {
                                                    hasAlarmStatus = true;
                                                }
                                            }
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean32 = dataBeans3.get(2);
                                            final Elect3DetailModel elect3DetailModel2 = MonitorPointModelsFactory.createElect3DetailModel(mDeviceInfo, 2, dataBean32, sensoroDetails);
                                            if (elect3DetailModel2 != null) {
                                                if (elect3DetailModel2.hasAlarmStatus()) {
                                                    hasAlarmStatus = true;
                                                }
                                            }
                                            DisplayOptionsBean.SpecialBean.DataBean dataBean33 = dataBeans3.get(3);
                                            final Elect3DetailModel elect3DetailModel3 = MonitorPointModelsFactory.createElect3DetailModel(mDeviceInfo, 3, dataBean33, sensoroDetails);
                                            if (elect3DetailModel3 != null) {
                                                if (elect3DetailModel3.hasAlarmStatus()) {
                                                    hasAlarmStatus = true;
                                                }
                                            }
                                            mContext.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isAttachedView()) {
                                                        if (elect3NameModel30 != null) {
                                                            getView().set3ElectTDetail(elect3NameModel30);
                                                        }
                                                        if (elect3DetailModel1 != null) {
                                                            getView().set3ElectTDetail(elect3DetailModel1);
                                                        }
                                                        if (elect3DetailModel2 != null) {
                                                            getView().set3ElectTDetail(elect3DetailModel2);
                                                        }
                                                        if (elect3DetailModel3 != null) {
                                                            getView().set3ElectTDetail(elect3DetailModel3);
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        mContext.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (isAttachedView()) {
                                                    getView().setElect3DetailVisible(true);
                                                }
                                            }
                                        });
                                    }

                                }
                            }
                        }
                    }
                    final boolean finalHasAlarmStatus = hasAlarmStatus;
                    if (needTop) {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isAttachedView()) {
                                    getView().setIvAlarmStatusVisible(finalHasAlarmStatus);
                                    getView().updateDeviceMalfunctionInfoAdapter(malfunctionBeanData);
                                    getView().setAcMonitoringElectPointLineVisible(true);
                                    getView().setLlElectTopVisible(true);
                                }
                            }
                        });
                    } else {
                        //容错处理 若无任何添加，尝试查找全部
                        if (dataBean.size() == 0 && configDeviceType != null) {
                            List<String> sensorTypes = configDeviceType.getSensorTypes();
                            if (sensorTypes != null && sensorTypes.size() > 0 && sensoroDetails != null) {
                                for (String type : sensorTypes) {
                                    MonitoringPointRcContentAdapterModel model = MonitorPointModelsFactory.createMonitoringPointRcContentAdapterModel(mContext, mDeviceInfo, sensoroDetails, type);
                                    if (model != null) {
                                        if (TextUtils.isEmpty(model.content)) {
                                            model.content = "-";
                                        }
                                        dataBean.add(model);
                                    }
                                }
                            }
                        }
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isAttachedView()) {
                                    getView().setIvAlarmStatusVisible(finalHasAlarmStatus);
                                    getView().updateDeviceMalfunctionInfoAdapter(malfunctionBeanData);
                                    getView().setElectDetailVisible(true);
                                    getView().updateDeviceInfoAdapter(dataBean);
                                    getView().setLlElectTopVisible(false);
                                }
                            }
                        });
                    }
                }

            }
        });

    }

    /**
     * 处理预警阈值信息
     *
     * @param monitorOptions
     */
    private void handleEarlyWarningThresholdModel(List<MonitorOptionsBean> monitorOptions) {
        synchronized (mEarlyWarningThresholdDialogUtilsAdapterModels) {
            if (mDeviceInfo != null) {
                AlarmInfo alarms = mDeviceInfo.getAlarms();
                final HashMap<String, AlarmInfo.RuleInfo> ruleInfoHashMap = new HashMap<>();
                //先填充数据
                if (alarms != null) {
                    AlarmInfo.RuleInfo[] rules = alarms.getRules();
                    if (rules != null && rules.length > 0) {
                        for (AlarmInfo.RuleInfo ruleInfo : rules) {
                            String sensorTypeStr = ruleInfo.getSensorTypes();
                            if (!TextUtils.isEmpty(sensorTypeStr)) {
                                String conditionType = ruleInfo.getConditionType();
                                if (TextUtils.isEmpty(conditionType)) {
                                    ruleInfoHashMap.put(sensorTypeStr, ruleInfo);
                                } else {
                                    ruleInfoHashMap.put(sensorTypeStr + conditionType, ruleInfo);
                                }


                            }
                        }
                    }

                }
                mEarlyWarningThresholdDialogUtilsAdapterModels.clear();
                boolean hasMonitorOptions = false;
                if (monitorOptions != null && monitorOptions.size() > 0) {
                    for (MonitorOptionsBean monitorOptionsBean : monitorOptions) {
                        EarlyWarningthresholdDialogUtilsAdapterModel earlyWarningthresholdDialogUtilsAdapterModel = new EarlyWarningthresholdDialogUtilsAdapterModel();
                        String name = monitorOptionsBean.getName();
                        if (TextUtils.isEmpty(name)) {
                            name = "";
                        }
                        earlyWarningthresholdDialogUtilsAdapterModel.name = name;
                        List<MonitorOptionsBean.SensorTypesBean> sensorTypes = monitorOptionsBean.getSensorTypes();
                        StringBuilder stringBuilder = new StringBuilder();
                        for (MonitorOptionsBean.SensorTypesBean sensorTypeBean : sensorTypes) {
                            if (sensorTypeBean != null) {
                                hasMonitorOptions = true;
                                String key;
                                String id = sensorTypeBean.getId();
                                String conditionType = sensorTypeBean.getConditionType();
                                AlarmInfo.RuleInfo ruleInfo;
                                if (TextUtils.isEmpty(conditionType)) {
                                    key = id;
                                } else {
                                    key = id + conditionType;
                                }
                                ruleInfo = ruleInfoHashMap.get(key);
                                if (ruleInfo != null) {
                                    SensorTypeStyles configSensorType = PreferencesHelper.getInstance().getConfigSensorType(id);
                                    if (configSensorType != null) {
                                        id = configSensorType.getName();
                                        if (TextUtils.isEmpty(id)) {
                                            id = mContext.getString(R.string.unknown);
                                        }
                                        boolean bool = configSensorType.isBool();
                                        if (bool) {
                                            stringBuilder.append(configSensorType.getAlarm()).append(mContext.getString(R.string.is_alarm)).append("\n");
                                        } else {
                                            String unit = configSensorType.getUnit();
                                            float value = ruleInfo.getThresholds();
                                            Integer precision = configSensorType.getPrecision();
                                            String valueStr = String.valueOf(value);
                                            if (precision != null) {
                                                BigDecimal b = new BigDecimal(value);
                                                valueStr = b.setScale(precision, BigDecimal.ROUND_HALF_UP).toString();
                                            }
                                            if (!TextUtils.isEmpty(conditionType)) {
                                                String conditionTypeRule = ruleInfo.getConditionType();
                                                if (conditionType.equals(conditionTypeRule)) {
                                                    switch (conditionType) {
                                                        case "gt":
                                                            stringBuilder.append(id).append(" ").append(">=").append(" ").append(valueStr).append(unit);
                                                            break;
                                                        case "lt":
                                                            stringBuilder.append(id).append(" ").append("<=").append(" ").append(valueStr).append(unit);
                                                            break;
                                                    }
                                                    stringBuilder.append(" ").append(mContext.getString(R.string.is_alarm)).append("\n");
                                                }
                                            }

                                        }

                                    }

                                }
                            }

                        }
                        String content = stringBuilder.toString();
                        if (!TextUtils.isEmpty(content)) {
                            if (content.endsWith("\n")) {
                                content = content.substring(0, content.lastIndexOf("\n"));
                            }
                            earlyWarningthresholdDialogUtilsAdapterModel.content = content;
                            mEarlyWarningThresholdDialogUtilsAdapterModels.add(earlyWarningthresholdDialogUtilsAdapterModel);
                        }

                    }
                }
                final boolean finalHasMonitorOptions = hasMonitorOptions;
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isAttachedView()) {
                            getView().setElectInfoTipVisible(finalHasMonitorOptions);
                        }
                    }
                });
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(DeviceInfo deviceInfo) {
        if (deviceInfo.getSn().equalsIgnoreCase(mDeviceInfo.getSn())) {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAttachedView()) {
                        mDeviceInfo.cloneSocketData(deviceInfo);
                        bleUpdateModel.blePassword = mDeviceInfo.getBlePassword();
                        bleUpdateModel.currentFirmVersion = mDeviceInfo.getFirmwareVersion();
                        bleUpdateModel.band = mDeviceInfo.getBand();
                        bleUpdateModel.hardwareVersion = mDeviceInfo.getHardwareVersion();
                        // 单项数值设置
                        if (isAttachedView()) {
                            freshLocationDeviceInfo();
                            freshTopData();
                            handleDeviceInfoAdapter();
                            refreshOperationStatus();
                            //刷新是否是demo模式
                            handleDeviceModeInfo();
                        }

                    }
                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(MonitorPointOperationTaskResultInfo monitorPointOperationTaskResultInfo) {
        final String scheduleNo = monitorPointOperationTaskResultInfo.getScheduleNo();
        if (!TextUtils.isEmpty(scheduleNo) && monitorPointOperationTaskResultInfo.getTotal() == monitorPointOperationTaskResultInfo.getComplete()) {
            String[] split = scheduleNo.split(",");
            if (split.length > 0) {
                final String temp = split[0];
                if (!TextUtils.isEmpty(temp)) {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(mScheduleNo) && mScheduleNo.equals(temp)) {
                                mHandler.removeCallbacks(DeviceTaskOvertime);
                                if (isAttachedView()) {
                                    getView().dismissOperatingLoadingDialog();
                                    getView().showOperationSuccessToast();
                                }
                            }
                        }
                    });
                }
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(EventData eventData) {
        int code = eventData.code;
        Object data = eventData.data;
        switch (code) {
            case Constants.EVENT_DATA_DEPLOY_INIT_CONFIG_CODE:
                if (data instanceof DeployControlSettingData) {
                    DeployControlSettingData deployControlSettingData = (DeployControlSettingData) data;
                    setMonitorConfigInfo(deployControlSettingData);
                }
            case Constants.EVENT_DATA_DEVICE_POSITION_CALIBRATION:
                if (data instanceof DeviceInfo) {
                    final DeviceInfo pushDeviceInfo = (DeviceInfo) data;
                    if (pushDeviceInfo.getSn().equals(mDeviceInfo.getSn())) {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isAttachedView()) {
                                    mDeviceInfo.cloneSocketData(pushDeviceInfo);
                                    freshLocationDeviceInfo();
                                    freshTopData();
                                    handleDeviceInfoAdapter();
                                }
                            }
                        });
                    }
                }

                break;

        }
    }

    @Override
    public void onDestroy() {
        if (sensoroDeviceConnection != null) {
            sensoroDeviceConnection.disconnect();
        }
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
        SensoroCityApplication.getInstance().bleDeviceManager.stopService();
        BleObserver.getInstance().unregisterBleObserver(this);
        bleDeviceMap.clear();

    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
        String address;
        if (i == 1000) {
            if (AppUtils.isChineseLanguage()) {
//            address = regeocodeResult.getRegeocodeAddress().getFormatAddress();

//                改为自定义
                StringBuilder stringBuilder = new StringBuilder();
                //
                String province = regeocodeAddress.getProvince();
                //
                String district = regeocodeAddress.getDistrict();// 区或县或县级市
                //
                //
                String township = regeocodeAddress.getTownship();// 乡镇
                //
                String streetName = null;// 道路
                List<RegeocodeRoad> regeocodeRoads = regeocodeAddress.getRoads();// 道路列表
                if (regeocodeRoads != null && regeocodeRoads.size() > 0) {
                    RegeocodeRoad regeocodeRoad = regeocodeRoads.get(0);
                    if (regeocodeRoad != null) {
                        streetName = regeocodeRoad.getName();
                    }
                }
                //
                String streetNumber = null;// 门牌号
                StreetNumber number = regeocodeAddress.getStreetNumber();
                if (number != null) {
                    String street = number.getStreet();
                    if (street != null) {
                        streetNumber = street + number.getNumber();
                    } else {
                        streetNumber = number.getNumber();
                    }
                }
                //
                String building = regeocodeAddress.getBuilding();// 标志性建筑,当道路为null时显示
                //区县
                if (!TextUtils.isEmpty(province)) {
                    stringBuilder.append(province);
                }
                if (!TextUtils.isEmpty(district)) {
                    stringBuilder.append(district);
                }
                //乡镇
                if (!TextUtils.isEmpty(township)) {
                    stringBuilder.append(township);
                }
                //道路
                if (!TextUtils.isEmpty(streetName)) {
                    stringBuilder.append(streetName);
                }
                //标志性建筑
                if (!TextUtils.isEmpty(building)) {
                    stringBuilder.append(building);
                } else {
                    //门牌号
                    if (!TextUtils.isEmpty(streetNumber)) {
                        stringBuilder.append(streetNumber);
                    }
                }
                if (TextUtils.isEmpty(stringBuilder)) {
                    address = township;
                } else {
                    address = stringBuilder.append("附近").toString();
                }
                //
                try {
                    LogUtils.loge(this, "onRegeocodeSearched: " + "code = " + i + ",address = " + address);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                String subLoc = regeocodeAddress.getDistrict();// 区或县或县级市
                String ts = regeocodeAddress.getTownship();// 乡镇
                String thf = null;// 道路
                List<RegeocodeRoad> regeocodeRoads = regeocodeAddress.getRoads();// 道路列表
                if (regeocodeRoads != null && regeocodeRoads.size() > 0) {
                    RegeocodeRoad regeocodeRoad = regeocodeRoads.get(0);
                    if (regeocodeRoad != null) {
                        thf = regeocodeRoad.getName();
                    }
                }
                String subthf = null;// 门牌号
                StreetNumber streetNumber = regeocodeAddress.getStreetNumber();
                if (streetNumber != null) {
                    subthf = streetNumber.getNumber();
                }
                String fn = regeocodeAddress.getBuilding();// 标志性建筑,当道路为null时显示
                if (TextUtils.isEmpty(thf)) {
                    if (!TextUtils.isEmpty(fn)) {
                        stringBuilder.append(fn);
                    }
                }
                if (subLoc != null) {
                    stringBuilder.append(subLoc);
                }
                if (ts != null) {
                    stringBuilder.append(ts);
                }
                if (thf != null) {
                    stringBuilder.append(thf);
                }
                if (subthf != null) {
                    stringBuilder.append(subthf);
                }
                address = stringBuilder.toString();
                if (TextUtils.isEmpty(address)) {
                    address = ts;
                }
            }
        } else {
            address = mContext.getString(R.string.not_positioned);
        }
        if (TextUtils.isEmpty(address)) {
            address = mContext.getString(R.string.unknown_street);
        }
        mDeviceInfo.setAddress(address);
        if (isAttachedView()) {
            getView().setDeviceLocation(address, true);
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        try {
            LogUtils.loge(this, "onGeocodeSearched: " + "onGeocodeSearched");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void doNavigation() {
        List<Double> lonlat = mDeviceInfo.getLonlat();
        if (lonlat.size() == 2) {
            double v = lonlat.get(1);
            double v1 = lonlat.get(0);
            if (v == 0 || v1 == 0) {
                getView().toastShort(mContext.getString(R.string.location_information_not_set));
                return;
            }
        } else {
            getView().toastShort(mContext.getString(R.string.location_information_not_set));
            return;
        }

        Log.d("************", lonlat.get(0) + "**********" + lonlat.get(1) + "*******");
        Intent intent = new Intent();
        if (AppUtils.isChineseLanguage()) {
            intent.setClass(mContext, MonitorPointMapActivity.class);
        } else {
            intent.setClass(mContext, MonitorPointMapENActivity.class);
        }
        intent.putExtra(Constants.EXTRA_DEVICE_INFO, mDeviceInfo);
        getView().startAC(intent);
    }

    public void doContact() {

        AlarmInfo alarms = mDeviceInfo.getAlarms();
        if (alarms != null) {
            List<DeviceNotificationBean> notifications = WidgetUtil.handleDeviceNotifications(alarms.getNotifications());
            if (notifications.size() > 1) {
                WarningContactDialogUtil dialogUtil = new WarningContactDialogUtil(mContext);
                dialogUtil.show(notifications);
            } else if (notifications.size() == 1) {
                DeviceNotificationBean notificationBean = notifications.get(0);
                if (TextUtils.isEmpty(notificationBean.getContent()) || mContext.getString(R.string.not_set).equals(notificationBean.getContent())) {
                    getView().toastShort(mContext.getString(R.string.phone_contact_not_set));
                    return;
                }
                AppUtils.diallPhone(notificationBean.getContent(), mContext);
            } else {
                DeviceNotificationBean notification = alarms.getNotification();
                if (notification != null && !TextUtils.isEmpty(notification.getContent())) {
                    AppUtils.diallPhone(notification.getContent(), mContext);
                } else {
                    getView().toastShort(mContext.getString(R.string.phone_contact_not_set));
                }
            }
        } else {
            getView().toastShort(mContext.getString(R.string.phone_contact_not_set));
        }

    }

    public void doMonitorHistory() {
        String sn = mDeviceInfo.getSn();
        Intent intent = new Intent(mContext, AlarmHistoryLogActivity.class);
        intent.putExtra(Constants.EXTRA_SENSOR_SN, sn);
        getView().startAC(intent);
    }


    public void doOperation(int type, String beepMuteTime) {
        Integer beepMuteTimeInt = null;
        switch (type) {
            case MonitorPointOperationCode.ERASURE:
                mOperationType = MonitorPointOperationCode.ERASURE_STR;

                break;
            case MonitorPointOperationCode.ERASURE_LONG:
                mOperationType = MonitorPointOperationCode.ERASURE_LONG_STR;

                break;
            case MonitorPointOperationCode.RESET:
                mOperationType = MonitorPointOperationCode.RESET_STR;
                break;
            case MonitorPointOperationCode.PSD:
                mOperationType = MonitorPointOperationCode.PSD_STR;
                break;
            case MonitorPointOperationCode.QUERY:
                mOperationType = MonitorPointOperationCode.QUERY_STR;
                break;
            case MonitorPointOperationCode.SELF_CHECK:
                mOperationType = MonitorPointOperationCode.SELF_CHECK_STR;
                break;
            case MonitorPointOperationCode.AIR_SWITCH_POWER_OFF:
                mOperationType = MonitorPointOperationCode.AIR_SWITCH_POWER_OFF_STR;
                //断电
                break;
            case MonitorPointOperationCode.AIR_SWITCH_POWER_ON:
                mOperationType = MonitorPointOperationCode.AIR_SWITCH_POWER_ON_STR;
                //上电
                break;
            case MonitorPointOperationCode.ERASURE_TIME:
                if (TextUtils.isEmpty(beepMuteTime)) {
                    getView().toastShort(mContext.getString(com.sensoro.common.R.string.input_not_null));
                    return;
                }
                try {
                    beepMuteTimeInt = Integer.parseInt(beepMuteTime);
                } catch (Exception e) {
                    getView().toastShort(mContext.getString(com.sensoro.common.R.string.enter_the_correct_number_format));
                    return;
                }
                if (beepMuteTimeInt < 1 || beepMuteTimeInt > 30) {
                    getView().toastShort(mContext.getString(com.sensoro.common.R.string.monitor_point_operation_error_value_range) + "1~30");
                    return;
                }
                mOperationType = MonitorPointOperationCode.ERASURE_TIME_STR;
                break;
            default:
                break;
        }
        Integer finalBeepMuteTimeInt = beepMuteTimeInt;
        final OnConfigInfoObserver onConfigInfoObserver = new OnConfigInfoObserver() {
            @Override
            public void onStart(String msg) {
                if (isAttachedView()) {
                    getView().dismissTipDialog();
                    getView().showOperationTipLoadingDialog();
                }
            }

            @Override
            public void onSuccess(Object o) {
                if (isAttachedView()) {
                    //TODO 蓝牙成功后告诉服务器 只针对消音
                    switch (type) {
                        case MonitorPointOperationCode.ERASURE:
                        case MonitorPointOperationCode.ERASURE_LONG:
                        case MonitorPointOperationCode.ERASURE_TIME:
                            ArrayList<String> sns = new ArrayList<>();
                            sns.add(mDeviceInfo.getSn());
                            RetrofitServiceHelper.getInstance().doMonitorPointBLEUpdate(sns, mOperationType, null, null, null, null, null, null, finalBeepMuteTimeInt)
                                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<Object>>(MonitorPointDetailActivityPresenter.this) {
                                @Override
                                public void onCompleted(ResponseResult<Object> response) {
                                    getView().dismissOperatingLoadingDialog();
                                    getView().showOperationSuccessToast();
                                }

                                @Override
                                public void onErrorMsg(int errorCode, String errorMsg) {
                                    getView().dismissOperatingLoadingDialog();
                                    getView().toastShort(errorMsg);
                                }
                            });
                            break;
                        default:
                            getView().dismissOperatingLoadingDialog();
                            getView().showOperationSuccessToast();
                            break;
                    }

                }
            }

            @Override
            public void onFailed(String errorMsg) {
                if (isAttachedView()) {
                    bleRequestCmd(finalBeepMuteTimeInt);
                }
            }

            @Override
            public void onOverTime(String overTimeMsg) {
                if (isAttachedView()) {
                    bleRequestCmd(finalBeepMuteTimeInt);
                }
            }
        };
        //断电、上电不走蓝牙操作
        if (MonitorPointOperationCode.AIR_SWITCH_POWER_OFF == type || MonitorPointOperationCode.AIR_SWITCH_POWER_ON == type) {
            requestServerCmd(null);
        } else {
            //其他先进行本地蓝牙，失败则进行下行
            if (doBleMuteOperation(onConfigInfoObserver, finalBeepMuteTimeInt)) {
                //
                return;
            } else {
                requestServerCmd(finalBeepMuteTimeInt);
            }
        }


    }

    private boolean doBleMuteOperation(final OnConfigInfoObserver onConfigInfoObserver, Integer beepMuteTime) {
        String sn = mDeviceInfo.getSn();
        if (bleDeviceMap.containsKey(sn) && !TextUtils.isEmpty(bleUpdateModel.blePassword)) {
            BLEDevice bleDevice = bleDeviceMap.get(sn);
            if (bleDevice != null) {
                String macAddress = bleDevice.getMacAddress();
                if (!TextUtils.isEmpty(macAddress)) {
                    if (sensoroDeviceConnection != null) {
                        sensoroDeviceConnection.disconnect();
                    }
                    final Runnable configOvertime = new Runnable() {
                        @Override
                        public void run() {
                            if (isAttachedView()) {
                                if (sensoroDeviceConnection != null) {
                                    sensoroDeviceConnection.disconnect();
                                }
                                if (onConfigInfoObserver != null) {
                                    onConfigInfoObserver.onOverTime(mContext.getString(R.string.init_config_over_time));
                                }
                            }
                        }
                    };
                    try {
                        onConfigInfoObserver.onStart(null);
                        sensoroDeviceConnection = new SensoroDeviceConnection(mContext, macAddress);
                        final SensoroWriteCallback bleMuteOperationWriteCallback = new SensoroWriteCallback() {
                            @Override
                            public void onWriteSuccess(Object o, int cmd) {
                                if (isAttachedView()) {
                                    if (sensoroDeviceConnection != null) {
                                        sensoroDeviceConnection.disconnect();
                                    }
                                    mHandler.removeCallbacks(configOvertime);
                                    onConfigInfoObserver.onSuccess(null);
                                }

                            }

                            @Override
                            public void onWriteFailure(int errorCode, int cmd) {
                                if (isAttachedView()) {
                                    if (sensoroDeviceConnection != null) {
                                        sensoroDeviceConnection.disconnect();
                                    }
                                    mHandler.removeCallbacks(configOvertime);
                                    onConfigInfoObserver.onFailed("写入失败");
                                }

                            }
                        };
                        final SensoroConnectionCallback bleMuteOperationConnectionCallback = new SensoroConnectionCallback() {
                            @Override
                            public void onConnectedSuccess(BLEDevice bleDevice, int cmd) {
                                if (isAttachedView()) {
                                    OperationCmdAnalyzer.doOperation(mDeviceInfo.getDeviceType(), mOperationType, beepMuteTime, sensoroDeviceConnection, bleMuteOperationWriteCallback);
                                }

                            }

                            @Override
                            public void onConnectedFailure(int errorCode) {
                                if (isAttachedView()) {
                                    mHandler.removeCallbacks(configOvertime);
                                    onConfigInfoObserver.onFailed("连接失败");
                                }

                            }

                            @Override
                            public void onDisconnected() {

                            }
                        };
                        sensoroDeviceConnection.connect(bleUpdateModel.blePassword, bleMuteOperationConnectionCallback);
                        mHandler.postDelayed(configOvertime, 10 * 1000);
                        LogUtils.loge("--->>  蓝牙消音");
                    } catch (Throwable e) {
                        e.printStackTrace();
                        mHandler.removeCallbacks(configOvertime);
                        bleRequestCmd(beepMuteTime);
                    }
                    return true;
                }
            }

        }
        return false;
    }

    private void bleRequestCmd(Integer beepMuteTime) {
        if (sensoroDeviceConnection != null) {
            sensoroDeviceConnection.disconnect();
        }
        if (TextUtils.isEmpty(mOperationType)) {
            getView().dismissTipDialog();
            getView().toastShort(mContext.getString(R.string.unknown_error));
        } else {
            requestServerCmd(beepMuteTime);
        }
    }

    private void requestServerCmd(Integer beepMuteTime) {
        ArrayList<String> sns = new ArrayList<>();
        sns.add(mDeviceInfo.getSn());
        getView().dismissTipDialog();
        getView().showOperationTipLoadingDialog();
        mScheduleNo = null;
        //
        long deviceTaskOvertimeMillis;
        if (CityConstants.DEVICE_2G_CONFIG_DEVICE_TYPES.contains(mDeviceInfo.getDeviceType())) {
            deviceTaskOvertimeMillis = 30 * 1000;
        } else {
            deviceTaskOvertimeMillis = 15 * 1000;
        }
        RetrofitServiceHelper.getInstance().doMonitorPointOperation(sns, mOperationType, null, null, null, null, null, null, beepMuteTime)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<MonitorPointOperationRequestRsp>(this) {
            @Override
            public void onCompleted(MonitorPointOperationRequestRsp response) {
                clearOperationType();
                String scheduleNo = response.getScheduleNo();
                if (TextUtils.isEmpty(scheduleNo)) {
                    getView().dismissOperatingLoadingDialog();
                    getView().showErrorTipDialog(mContext.getString(R.string.monitor_point_operation_schedule_no_error));
                } else {
                    String[] split = scheduleNo.split(",");
                    if (split.length > 0) {
                        mScheduleNo = split[0];
                        mHandler.postDelayed(DeviceTaskOvertime, deviceTaskOvertimeMillis);
                    } else {
                        getView().dismissOperatingLoadingDialog();
                        getView().showErrorTipDialog(mContext.getString(R.string.monitor_point_operation_schedule_no_error));

                    }
                }
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                clearOperationType();
                getView().dismissOperatingLoadingDialog();
                getView().showErrorTipDialog(errorMsg);
            }
        });
    }

    private void clearOperationType() {
        mOperationType = null;
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }

    public void toPhotoDetail(int position, List<ScenesData> images) {
        if (images.size() > 0) {
            ArrayList<ImageItem> items = new ArrayList<>();
            for (ScenesData scenesData : images) {
                ImageItem imageItem = new ImageItem();
                imageItem.isRecord = false;
                imageItem.fromUrl = true;
                imageItem.path = scenesData.url;
                items.add(imageItem);
            }
            Intent intentPreview = new Intent(mContext, ImagePreviewDelActivity.class);
            intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, items);
            intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
            intentPreview.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
            intentPreview.putExtra(Constants.EXTRA_JUST_DISPLAY_PIC, true);
            getView().startACForResult(intentPreview, Constants.REQUEST_CODE_PREVIEW);
        } else {
            getView().toastShort(mContext.getString(R.string.no_photos_added));
        }
    }

    public void showEarlyWarningThresholdDialogUtils() {

        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isAttachedView()) {
                    getView().updateEarlyWarningThresholdAdapterDialogUtils(mEarlyWarningThresholdDialogUtilsAdapterModels);
                }
            }
        });
    }

    @Override
    public void onClickOperation(View view, int position, TaskOptionModel taskOptionModel) {
        switch (taskOptionModel.optionType) {
            case MonitorPointOperationCode.ERASURE:
                getView().showTipDialog(false, null, mContext.getString(R.string.is_device_erasure), mContext.getString(R.string.device_erasure_tip_message), R.color.c_a6a6a6, mContext.getString(R.string.erasure), R.color.c_f34a4a, MonitorPointOperationCode.ERASURE);
                break;
            case MonitorPointOperationCode.ERASURE_LONG:
                String allStr = mContext.getString(R.string.device_erasure_long_tip_message);
                try {
                    SpannableString spannableString = new SpannableString(allStr);
                    String redStr = mContext.getString(R.string.device_erasure_long_tip_red);
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")), allStr.indexOf(redStr), allStr.indexOf(redStr) + redStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    getView().showTipDialog(false, null, mContext.getString(R.string.is_device_erasure_long), spannableString, R.color.c_a6a6a6, mContext.getString(R.string.erasure_long), R.color.c_f34a4a, MonitorPointOperationCode.ERASURE_LONG);
                } catch (Exception e) {
                    e.printStackTrace();
                    getView().showTipDialog(false, null, mContext.getString(R.string.is_device_erasure_long), allStr, R.color.c_a6a6a6, mContext.getString(R.string.erasure_long), R.color.c_f34a4a, MonitorPointOperationCode.ERASURE_LONG);
                }
                break;
            case MonitorPointOperationCode.RESET:
                getView().showTipDialog(false, null, mContext.getString(R.string.is_device_reset), mContext.getString(R.string.device_reset_tip_message), R.color.c_a6a6a6, mContext.getString(R.string.reset), R.color.c_f34a4a, MonitorPointOperationCode.RESET);
                break;
            case MonitorPointOperationCode.PSD:
                getView().showTipDialog(false, null, mContext.getString(R.string.is_device_psd), mContext.getString(R.string.device_psd_tip_message), R.color.c_a6a6a6, mContext.getString(R.string.modify), R.color.c_f34a4a, MonitorPointOperationCode.PSD);
                break;
            case MonitorPointOperationCode.QUERY:
                getView().showTipDialog(false, null, mContext.getString(R.string.is_device_query), mContext.getString(R.string.device_query_tip_message), R.color.c_a6a6a6, mContext.getString(R.string.monitor_point_detail_query), R.color.c_1dbb99, MonitorPointOperationCode.QUERY);
                break;
            case MonitorPointOperationCode.SELF_CHECK:
                getView().showTipDialog(false, null, mContext.getString(R.string.is_device_self_check), mContext.getString(R.string.device_self_check_tip_message), R.color.c_a6a6a6, mContext.getString(R.string.self_check), R.color.c_1dbb99, MonitorPointOperationCode.SELF_CHECK);
                break;
            case MonitorPointOperationCode.AIR_SWITCH_CONFIG:
                //
                DeployAnalyzerModel deployAnalyzerModel = new DeployAnalyzerModel();
                deployAnalyzerModel.deviceType = mDeviceInfo.getDeviceType();
                deployAnalyzerModel.sn = mDeviceInfo.getSn();
                if (CityConstants.DEVICE_CONTROL_NEW_CONFIG_DEVICE_TYPES.contains(mDeviceInfo.getDeviceType())) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.EXTRA_DEPLOY_CONFIGURATION_ORIGIN_TYPE, Constants.DEPLOY_CONFIGURATION_SOURCE_TYPE_DEVICE_DETAIL);
                    bundle.putSerializable(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
                    DeployControlSettingData settingData = mDeviceInfo.getConfig();
                    if (settingData != null) {
                        bundle.putSerializable(Constants.EXTRA_DEPLOY_CONFIGURATION_SETTING_DATA, settingData);
                    }
                    startActivity(ARouterConstants.ACTIVITY_THREE_PHASE_ELECT_CONFIG_ACTIVITY, bundle, mContext);
                } else {
                    Intent intent = new Intent(mContext, SingleMonitorConfigurationActivity.class);
                    intent.putExtra(Constants.EXTRA_DEPLOY_CONFIGURATION_ORIGIN_TYPE, Constants.DEPLOY_CONFIGURATION_SOURCE_TYPE_DEVICE_DETAIL);
                    intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
                    getView().startAC(intent);
                }

                break;
            case MonitorPointOperationCode.AIR_SWITCH_POWER_OFF:
                //断电
                getView().showTipDialog(false, mDeviceInfo.getDeviceType(), mContext.getString(R.string.command_elec_disconnect_title), mContext.getString(R.string.command_elec_disconnect_desc), R.color.c_f34a4a, mContext.getString(R.string.command_elec_disconnect_btn_title), R.color.c_f34a4a, MonitorPointOperationCode.AIR_SWITCH_POWER_OFF);
                break;
            case MonitorPointOperationCode.AIR_SWITCH_POWER_ON:
                getView().showTipDialog(false, mDeviceInfo.getDeviceType(), mContext.getString(R.string.command_elec_connect_title), mContext.getString(R.string.command_elec_connect_desc), R.color.c_f34a4a, mContext.getString(R.string.command_elec_connect_btn_title), R.color.c_f34a4a, MonitorPointOperationCode.AIR_SWITCH_POWER_ON);
                //上电
                break;
            case MonitorPointOperationCode.ERASURE_TIME:
                getView().showTipDialog(true, mDeviceInfo.getDeviceType(), mContext.getString(R.string.command_smoke_mute_time_title), mContext.getString(R.string.command_smoke_mute_time_desc), R.color.c_a6a6a6, mContext.getString(R.string.erasure), R.color.c_f34a4a, MonitorPointOperationCode.ERASURE_TIME);
                break;

        }
    }

    @Override
    public void onNewDevice(BLEDevice bleDevice) {
        bleDeviceMap.put(bleDevice.getSn(), bleDevice);
    }

    @Override
    public void onGoneDevice(BLEDevice bleDevice) {
        try {
            bleDeviceMap.remove(bleDevice.getSn());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateDevices(ArrayList<BLEDevice> deviceList) {
        for (BLEDevice device : deviceList) {
            if (device != null) {
                bleDeviceMap.put(device.getSn(), device);
            }
        }
    }


    @Override
    public void onUpdateClick() {
        boolean bluetoothEnabled = SensoroCityApplication.getInstance().bleDeviceManager.isBluetoothEnabled();
        if (!bluetoothEnabled) {
            if (isAttachedView()) {
                getView().showBleTips();
            }
            return;
        }
        if (isAttachedView()) {
            getView().updateDialogProgress(mContext.getString(R.string.firmware_update_in_preparation), -1, 0);
        }
        RetrofitServiceHelper.getInstance().downloadDeviceFirmwareFile(bleUpdateModel.firmUrl, bleUpdateModel.filePath, new CityObserver<Boolean>(this) {
            @Override
            public void onCompleted(Boolean aBoolean) {
                if (aBoolean) {
                    if (bleDeviceMap.containsKey(mDeviceInfo.getSn())) {
                        if (sensoroDeviceConnection != null) {
                            sensoroDeviceConnection.disconnect();
                        }
                        BLEDevice bleDevice = bleDeviceMap.get(mDeviceInfo.getSn());
                        sensoroDeviceConnection = new SensoroDeviceConnection(mContext, (SensoroDevice) bleDevice);
                        onResume();
                        final OnDeviceUpdateObserver onDeviceUpdateObserver = new OnDeviceUpdateObserver() {
                            @Override
                            public void onEnteringDFU(String deviceMacAddress, String filePath, String msg) {
                                if (isAttachedView()) {
                                    getView().updateDialogProgress(mContext.getString(R.string.firmware_update_transferred) + "0%", 0, 1);
                                }
                            }

                            @Override
                            public void onUpdateCompleted(String filePath, String deviceMacAddress, String msg) {
                                if (isAttachedView()) {
                                    mHandler.postDelayed(checkUpdateTask, 1000);
                                    bleDeviceMap.remove(bleUpdateModel.sn);
                                    getView().updateDialogProgress(mContext.getString(R.string.checking_version_bluetooth), -1, 2);
                                }
                            }

                            @Override
                            public void onDFUTransfer(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal, String msg) {
                                if (isAttachedView()) {
                                    getView().updateDialogProgress(mContext.getString(R.string.firmware_update_transferred) + percent + "%", percent, 1);
                                }
                            }

                            @Override
                            public void onUpdateValidating(String deviceMacAddress, String msg) {
                                if (isAttachedView()) {
                                    getView().updateDialogProgress(mContext.getString(R.string.verifying_firmware_information), -1, 2);
                                }
                            }

                            @Override
                            public void onUpdateTimeout(int code, Object data, String msg) {

                            }

                            @Override
                            public void onDisconnecting() {
                                if (isAttachedView()) {
                                    getView().updateDialogProgress(mContext.getString(R.string.verifying_firmware_information), -1, 2);
                                }
                            }

                            @Override
                            public void onFailed(String deviceMacAddress, String errorMsg, Throwable e) {
                                if (isAttachedView()) {
                                    getView().dismissUpdateDialogUtils();
                                    getView().toastShort(mContext.getString(R.string.device_upgrade_failed));
                                }
                            }
                        };
                        if (CityConstants.DEVICE_UPDATE_FIRMWARE_CHIP_TYPES.contains(bleUpdateModel.deviceType)) {
                            sensoroDeviceConnection.startChipEUpdate(bleUpdateModel.filePath, bleUpdateModel.blePassword, onDeviceUpdateObserver);
                        } else {
                            sensoroDeviceConnection.startUpdate(bleUpdateModel.filePath, bleUpdateModel.blePassword, onDeviceUpdateObserver);
                        }
                    } else {
                        if (isAttachedView()) {
                            getView().toastShort(mContext.getString(R.string.device_is_not_nearby));
                            getView().dismissUpdateDialogUtils();
                        }

                    }
                } else {
                    if (isAttachedView()) {
                        getView().toastShort(mContext.getString(R.string.device_upgrade_failed));
                        getView().dismissUpdateDialogUtils();
                    }
                }
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().toastShort(errorMsg);
                getView().dismissUpdateDialogUtils();
            }
        });
    }

    /**
     * 检查升级状态
     */
    private final Runnable checkUpdateTask = new Runnable() {
        private volatile int checkUpdateCount = 1;

        @Override
        public void run() {
            if (checkUpdateCount < 10) {
                if (bleDeviceMap.containsKey(bleUpdateModel.sn)) {
                    BLEDevice bleDevice = bleDeviceMap.get(bleUpdateModel.sn);
                    if (bleDevice != null) {
                        if (bleUpdateModel.serverFirmVersion.contains(bleDevice.firmwareVersion)) {
                            mHandler.removeCallbacks(checkUpdateTask);
                            //升级成功
                            if (isAttachedView()) {
                                getView().updateDialogProgress(mContext.getString(R.string.sending_upgrade_version), -1, 2);
                                RetrofitServiceHelper.getInstance().upLoadDeviceUpdateVision(bleUpdateModel.sn, bleUpdateModel.serverFirmVersion)
                                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
                                        subscribe(new CityObserver<ResponseResult>(MonitorPointDetailActivityPresenter.this) {
                                            @Override
                                            public void onCompleted(ResponseResult responseBase) {
                                                try {
                                                    LogUtils.loge("升级--->> 成功！！次数=" + checkUpdateCount);
                                                } catch (Throwable throwable) {
                                                    throwable.printStackTrace();
                                                }
                                                bleUpdateModel.currentFirmVersion = bleUpdateModel.serverFirmVersion;
                                                bleUpdateModel.currentFirmCreateTime = bleUpdateModel.serverFirmCreateTime;
                                                getView().showOperationSuccessToast(mContext.getString(R.string.device_update_success));
                                                getView().dismissUpdateDialogUtils();
                                                getView().setIvHasNewVersionViewVisible(false);
                                                freshDeviceUpdateVersionInfo();
                                                //改变当前UI
                                            }

                                            @Override
                                            public void onErrorMsg(int errorCode, String errorMsg) {
                                                try {
                                                    LogUtils.loge("升级--->> 回传服务器失败！！");
                                                } catch (Throwable throwable) {
                                                    throwable.printStackTrace();
                                                }
                                                getView().toastShort(mContext.getString(R.string.device_upgrade_failed));
                                                getView().dismissUpdateDialogUtils();
                                                //告诉升级失败
                                            }
                                        });
                            }
                        } else {
                            try {
                                LogUtils.loge("升级--->> 广播不包含！！");
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                            if (isAttachedView()) {
                                getView().toastShort(mContext.getString(R.string.device_upgrade_failed));
                            }
                        }
                        return;
                    }
                }
                checkUpdateCount++;
                mHandler.postDelayed(checkUpdateTask, 1000);
            } else {
                mHandler.removeCallbacks(checkUpdateTask);
                try {
                    LogUtils.loge("升级--->> 超时！！次数=" + checkUpdateCount);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                checkUpdateCount = 1;
                //超时
                if (isAttachedView()) {
                    getView().toastShort(mContext.getString(R.string.device_upgrade_failed));
                    getView().dismissUpdateDialogUtils();
                }
            }
        }
    };

    @Override
    public void onStart() {
        SensoroCityApplication.getInstance().bleDeviceManager.startScan();
    }

    @Override
    public void onStop() {
        SensoroCityApplication.getInstance().bleDeviceManager.stopScan();
    }

    public void doDeviceUpdate() {
        String title;
        String desc;
        String timeStr = null;
        boolean hasNewVersion = WidgetUtil.isNewVersion(bleUpdateModel.currentFirmVersion, bleUpdateModel.serverFirmVersion);
        if (!PreferencesHelper.getInstance().getUserData().hasDeviceFirmwareUpdate) {
            hasNewVersion = false;
        }
        if (hasNewVersion) {
            title = mContext.getString(R.string.discover_new_version);
            desc = mContext.getString(R.string.latest_version) + "：V " + bleUpdateModel.serverFirmVersion;
            if (bleUpdateModel.serverFirmCreateTime != 0) {
                timeStr = mContext.getString(R.string.firmware_release_date) + "：" + DateUtil.getStrTime_ymd(bleUpdateModel.serverFirmCreateTime);
            }
        } else {
            title = mContext.getString(R.string.already_the_latest_version);
            if (TextUtils.isEmpty(bleUpdateModel.currentFirmVersion)) {
                desc = mContext.getString(R.string.current_version) + "：- ";
            } else {
                desc = mContext.getString(R.string.current_version) + "：V " + bleUpdateModel.currentFirmVersion;
            }
            if (bleUpdateModel.currentFirmCreateTime == 0) {
                timeStr = mContext.getString(R.string.firmware_release_date) + "：-";
            } else {
                timeStr = mContext.getString(R.string.firmware_release_date) + "：" + DateUtil.getStrTime_ymd(bleUpdateModel.currentFirmCreateTime);
            }
        }
        getView().showUpdateDialogUtils(title, desc, timeStr, hasNewVersion);
    }

    @Override
    public void onResume() {
        if (sensoroDeviceConnection != null) {
            sensoroDeviceConnection.onSessionResume();
        }
    }

    @Override
    public void onPause() {
        if (sensoroDeviceConnection != null) {
            sensoroDeviceConnection.onSessonPause();
        }
    }

    public void showDemoModeDialog() {
        switch (deviceDemoMode) {
            case Constants.DEVICE_DEMO_MODE_NOT_SUPPORT:
                //不显示条目
                break;
            case Constants.DEVICE_DEMO_MODE_NO_PERMISSION:
                //不可点击
                getView().toastShort(mContext.getString(R.string.no_permission_));
                break;
            case Constants.DEVICE_DEMO_MODE_OPEN:
                //演示状态
                getView().showCloseDemoDialog();
                break;
            case Constants.DEVICE_DEMO_MODE_CLOSE:
                //非演示状态
                getView().showOpenDemoDialog();
                break;
            default:
                break;

        }
    }

    public void doDemoConfigSwitch(final int mode) {
        boolean bluetoothEnabled = SensoroCityApplication.getInstance().bleDeviceManager.isBluetoothEnabled();
        if (!bluetoothEnabled) {
            if (isAttachedView()) {
                getView().showBleTips();
            }
            return;
        }
        getView().dismissCloseDemoDialog();
        getView().dismissOpenDemoDialog();
        if (bleDeviceMap.containsKey(mDeviceInfo.getSn())) {
            if (sensoroDeviceConnection != null) {
                sensoroDeviceConnection.disconnect();
            }
            getView().showOperationTipLoadingDialog(mContext.getString(R.string.mode_switch));
            sensoroDeviceConnection = new SensoroDeviceConnection(mContext, bleDeviceMap.get(mDeviceInfo.getSn()).getMacAddress());
            try {
                final SensoroWriteCallback bleDemoModeWriteCallback = new SensoroWriteCallback() {
                    @Override
                    public void onWriteSuccess(Object o, int cmd) {
                        String finalTipText = "";
                        switch (mode) {
                            case 0:
                                //演示模式关闭成功
                                finalTipText = mContext.getString(R.string.demo_mode_has_close);
                                deviceDemoMode = Constants.DEVICE_DEMO_MODE_CLOSE;
                                break;
                            case 1:
                                //演示模式开启成功
                                finalTipText = mContext.getString(R.string.demo_mode_has_open);
                                deviceDemoMode = Constants.DEVICE_DEMO_MODE_OPEN;
                                break;
                        }

                        if (sensoroDeviceConnection != null) {
                            sensoroDeviceConnection.disconnect();
                        }
                        if (isAttachedView()) {
                            getView().setDeviceDemoModeViewStatus(deviceDemoMode);
                            getView().showOperationSuccessToast(finalTipText);
                            getView().dismissOperatingLoadingDialog();
                        }
                    }

                    @Override
                    public void onWriteFailure(int errorCode, int cmd) {
                        if (isAttachedView()) {
                            getView().dismissOperatingLoadingDialog();
                            getView().toastShort(mContext.getString(R.string.ble_config_failed));
                        }
                    }
                };
                final SensoroConnectionCallback bleDemoModeConnectionCallback = new SensoroConnectionCallback() {
                    @Override
                    public void onConnectedSuccess(BLEDevice bleDevice, int cmd) {
                        if (isAttachedView()) {
                            sensoroDeviceConnection.writeDemoModeCmd(mode, bleDemoModeWriteCallback);
                        }
                    }

                    @Override
                    public void onConnectedFailure(int errorCode) {
                        if (isAttachedView()) {
                            getView().dismissOperatingLoadingDialog();
                            getView().toastShort(mContext.getString(R.string.ble_connect_failed));
                        }
                    }

                    @Override
                    public void onDisconnected() {

                    }
                };
                sensoroDeviceConnection.connect(bleUpdateModel.blePassword, bleDemoModeConnectionCallback);
            } catch (Exception e) {
                e.printStackTrace();
                getView().dismissOperatingLoadingDialog();
                getView().toastShort(mContext.getString(R.string.ble_connect_failed));
            }
        } else {
            if (isAttachedView()) {
                getView().toastShort(mContext.getString(R.string.device_is_not_nearby));
            }

        }
    }

    public void doDeviceGroupCameras() {
        if (deviceCameras != null && deviceCameras.size() > 0) {
            //TODO 去摄像头列表
            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_DEVICE_CAMERA_DETAIL_INFO_LIST, deviceCameras);
            intent.setClass(mContext, CameraListActivity.class);
            getView().startAC(intent);
        } else {
            getView().setDeviceCamerasText(mContext.getString(R.string.device_detail_camera_no_camera));
        }
    }

    public void goConfigDetailInfo() {
        DeployControlSettingData settingData = mDeviceInfo.getConfig();
        if (hasNesConfigInfo(settingData)) {
            //新界面
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.EXTRA_DEPLOY_CONFIGURATION_SETTING_DATA, settingData);
            startActivity(ARouterConstants.ACTIVITY_DEPLOY_RECORD_CONFIG_THREE_PHASE_ELECT_ACTIVITY, bundle, mContext);
        } else {
            Bundle bundle = new Bundle();
            if (settingData != null) {
                bundle.putSerializable(Constants.EXTRA_DEPLOY_CONFIGURATION_SETTING_DATA, settingData);
            }
            startActivity(ARouterConstants.ACTIVITY_DEPLOY_RECORD_CONFIG_COMMON_ELECT_ACTIVITY, bundle, mContext);
            //旧界面
        }
    }
}