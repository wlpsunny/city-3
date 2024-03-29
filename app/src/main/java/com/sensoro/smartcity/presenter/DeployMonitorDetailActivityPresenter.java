package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.RegeocodeRoad;
import com.amap.api.services.geocoder.StreetNumber;
import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.callback.BleObserver;
import com.sensoro.common.callback.OnConfigInfoObserver;
import com.sensoro.common.constant.ARouterConstants;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.handler.HandlerDeployCheck;
import com.sensoro.common.helper.PreferencesHelper;
import com.sensoro.common.iwidget.IOnCreate;
import com.sensoro.common.iwidget.IOnStart;
import com.sensoro.common.model.DeployAnalyzerModel;
import com.sensoro.common.model.DeployContactModel;
import com.sensoro.common.model.DeployResultModel;
import com.sensoro.common.model.EventData;
import com.sensoro.common.model.ImageItem;
import com.sensoro.common.model.MonitoringPointRcContentAdapterModel;
import com.sensoro.common.server.CityObserver;
import com.sensoro.common.server.RetrofitServiceHelper;
import com.sensoro.common.server.RetryWithDelay;
import com.sensoro.common.server.bean.DeployControlSettingData;
import com.sensoro.common.server.bean.DeployPicInfo;
import com.sensoro.common.server.bean.DeployStationInfo;
import com.sensoro.common.server.bean.DeviceInfo;
import com.sensoro.common.server.bean.DeviceTypeStyles;
import com.sensoro.common.server.bean.MalfunctionDataBean;
import com.sensoro.common.server.bean.MalfunctionTypeStyles;
import com.sensoro.common.server.bean.MergeTypeStyles;
import com.sensoro.common.server.bean.MonitorPointOperationTaskResultInfo;
import com.sensoro.common.server.bean.ScenesData;
import com.sensoro.common.server.bean.SensorStruct;
import com.sensoro.common.server.bean.SensorTypeStyles;
import com.sensoro.common.server.response.MonitorPointOperationRequestRsp;
import com.sensoro.common.server.response.ResponseResult;
import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.utils.LogUtils;
import com.sensoro.common.utils.RegexUtils;
import com.sensoro.common.utils.WidgetUtil;
import com.sensoro.common.widgets.uploadPhotoUtil.UpLoadPhotosUtils;
import com.sensoro.libbleserver.ble.callback.SensoroConnectionCallback;
import com.sensoro.libbleserver.ble.callback.SensoroWriteCallback;
import com.sensoro.libbleserver.ble.connection.SensoroDeviceConnection;
import com.sensoro.libbleserver.ble.entity.BLEDevice;
import com.sensoro.libbleserver.ble.entity.SensoroDevice;
import com.sensoro.libbleserver.ble.scanner.BLEDeviceListener;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.activity.DeployMapActivity;
import com.sensoro.smartcity.activity.DeployMapENActivity;
import com.sensoro.smartcity.activity.DeployMonitorAlarmContactActivity;
import com.sensoro.smartcity.activity.DeployMonitorNameAddressActivity;
import com.sensoro.smartcity.activity.DeployMonitorWeChatRelationActivity;
import com.sensoro.smartcity.activity.DeployRepairInstructionActivity;
import com.sensoro.smartcity.activity.DeployResultActivity;
import com.sensoro.smartcity.activity.SingleMonitorConfigurationActivity;
import com.sensoro.smartcity.analyzer.DeployConfigurationAnalyzer;
import com.sensoro.smartcity.constant.CityConstants;
import com.sensoro.smartcity.constant.DeoloyCheckPointConstants;
import com.sensoro.smartcity.factory.MonitorPointModelsFactory;
import com.sensoro.smartcity.imainviews.IDeployMonitorDetailActivityView;
import com.sensoro.smartcity.model.DeployTask;
import com.sensoro.smartcity.util.DeployRetryUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeployMonitorDetailActivityPresenter extends BasePresenter<IDeployMonitorDetailActivityView> implements IOnCreate, IOnStart
        , BLEDeviceListener<BLEDevice>, Runnable {
    private Activity mContext;
    private SensoroDeviceConnection sensoroDeviceConnection;
    private Handler mHandler;
    private final HashMap<String, BLEDevice> bleDevice = new HashMap<>();
    private DeployAnalyzerModel deployAnalyzerModel;
    private DeployRetryUtil deployRetryUtil;

    private final HandlerDeployCheck checkHandler = new HandlerDeployCheck(Looper.getMainLooper());
    private String tempForceReason;
    private Integer tempStatus;
    private String mScheduleNo;
    //默认信号检测为2分钟
    private int signalTimeRule = 2 * 60 * 1000;
    private int signalCheckCount = 15;
    //不需要检查信号时间
    private final Runnable signalTask = new Runnable() {
        @Override
        public void run() {
            try {
                com.sensoro.common.utils.LogUtils.loge("信号更新--->>> 准备 freshSignalInfo updatedTime = " + deployAnalyzerModel.updatedTime + "，signal = " + deployAnalyzerModel.signal);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            if ("acrel300T_fires_2G".equals(deployAnalyzerModel.deviceType) || "acrel300D_fires_2G".equals(deployAnalyzerModel.deviceType)) {
                //针对此2g电气火灾设备特定修改信号时间
                signalTimeRule = 5 * 60 * 1000;
                signalCheckCount = 30;
            }
            freshSignalInfo();
            //加入部署接口轮询
            RetrofitServiceHelper.getInstance().getDeviceRealStatus(deployAnalyzerModel.sn).subscribeOn(Schedulers.io())
                    .retryWhen(new RetryWithDelay(2, 100))
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(DeployMonitorDetailActivityPresenter.this) {
                @Override
                public void onCompleted(final ResponseResult<DeviceInfo> data) {
                    DeviceInfo deviceInfo = data.getData();
                    if (deviceInfo != null) {
                        deployAnalyzerModel.updatedTime = deviceInfo.getUpdatedTime();
                        String signal = deviceInfo.getSignal();
                        if (TextUtils.isEmpty(signal)) {
                            signal = "none";
                        }
                        deployAnalyzerModel.signal = String.copyValueOf(signal.toCharArray());
                        mHandler.removeCallbacks(signalTask);
                        freshSignalInfo();
                        mHandler.postDelayed(signalTask, 2000);
                    }
                }

                @Override
                public void onErrorMsg(int errorCode, String errorMsg) {
                }
            });
            mHandler.postDelayed(signalTask, 2000);
        }
    };


    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        deployRetryUtil = DeployRetryUtil.getInstance();
        mHandler = new Handler(Looper.getMainLooper());
        onCreate();
        Intent intent = mContext.getIntent();
        deployAnalyzerModel = (DeployAnalyzerModel) intent.getSerializableExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL);
        getView().setNotOwnVisible(deployAnalyzerModel.notOwn);
        init();
        if (hasSignalConfig()) {
            mHandler.post(this);
        }
        BleObserver.getInstance().registerBleObserver(this);
        mHandler.post(signalTask);
        //默认显示已定位
        deployAnalyzerModel.address = mContext.getString(R.string.positioned);
        initGeocodeSearch();

    }

    private void init() {
        switch (deployAnalyzerModel.deployType) {
            case Constants.TYPE_SCAN_DEPLOY_STATION:
                //基站部署
                getView().setDeployContactRelativeLayoutVisible(false);
                getView().setDeployDeviceRlSignalVisible(false);
                getView().setDeployPhotoVisible(false);
                getView().setDeviceSn(mContext.getString(R.string.device_number) + deployAnalyzerModel.sn);
                getView().setDeployDeviceType(mContext.getString(R.string.station));
                if (!TextUtils.isEmpty(deployAnalyzerModel.nameAndAddress)) {
                    getView().setNameAddressText(deployAnalyzerModel.nameAndAddress);
                }
                getView().updateTagsData(deployAnalyzerModel.tagList);
                getView().setUploadBtnStatus(checkCanUpload());
                break;
            case Constants.TYPE_SCAN_DEPLOY_DEVICE:
                //设备部署
                getView().setDeployContactRelativeLayoutVisible(true);
                getView().setDeployDeviceRlSignalVisible(true);
                getView().setDeployPhotoVisible(true);
                getView().setDeployDeviceType(WidgetUtil.getDeviceMainTypeName(deployAnalyzerModel.deviceType));
                echoDeviceInfo();
                break;
            case Constants.TYPE_SCAN_DEPLOY_INSPECTION_DEVICE_CHANGE:
            case Constants.TYPE_SCAN_DEPLOY_MALFUNCTION_DEVICE_CHANGE:
                //巡检设备更换
                getView().setDeployContactRelativeLayoutVisible(true);
                getView().setDeployDeviceRlSignalVisible(true);
                getView().setDeployPhotoVisible(true);
                getView().updateUploadTvText(mContext.getString(R.string.replacement_equipment));
                getView().setDeployDeviceType(WidgetUtil.getDeviceMainTypeName(deployAnalyzerModel.deviceType));
                echoDeviceInfo();
                getView().setDeployDetailArrowWeChatVisible(false);
                break;
            case Constants.TYPE_SCAN_INSPECTION:
                //扫描巡检设备
                break;
            default:
                break;
        }
        //TODO 暂时只针对ancre的电器火灾并且排除掉泛海三江电气火灾
        boolean isFire = CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType);
        getView().setDeployDetailDeploySettingVisible(isFire);
        if (isFire) {
            //TODO 再次部署时暂时不回显电器火灾字段字段
            getView().setDeployDeviceDetailDeploySetting(null);
        }
        if (!AppUtils.isChineseLanguage()) {
            //TODO 英文版控制不显示小程序账号
            deployAnalyzerModel.weChatAccount = null;
        }
    }


    private void initGeocodeSearch() {
        GeocodeSearch geocoderSearch = new GeocodeSearch(mContext);
        geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                String address;

                if (i == 1000) {

                    RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();

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

                } else {
                    address = mContext.getString(R.string.not_positioned);

                }
                if (TextUtils.isEmpty(address)) {
                    address = mContext.getString(R.string.unknown_street);
                }
                deployAnalyzerModel.address = address;
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
        //查询一次地址信息
        if (deployAnalyzerModel.latLng.size() == 2) {
            LatLonPoint lp = new LatLonPoint(deployAnalyzerModel.latLng.get(1), deployAnalyzerModel.latLng.get(0));
            RegeocodeQuery query = new RegeocodeQuery(lp, 200, GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);
        }
    }

    //回显设备信息
    private void echoDeviceInfo() {
        getView().setDeviceSn(mContext.getString(R.string.device_number) + deployAnalyzerModel.sn);
        if (!TextUtils.isEmpty(deployAnalyzerModel.nameAndAddress)) {
            getView().setNameAddressText(deployAnalyzerModel.nameAndAddress);
        }
        if (deployAnalyzerModel.deployContactModelList.size() > 0) {
            DeployContactModel deployContactModel = deployAnalyzerModel.deployContactModelList.get(0);
            String contact = deployContactModel.name;
            String content = deployContactModel.phone;
            getView().setFirstContact(contact + "(" + content + ")");
        }
        getView().setTotalContact(deployAnalyzerModel.deployContactModelList.size());
//        getView().updateContactData(deployAnalyzerModel.deployContactModelList);
        getView().updateTagsData(deployAnalyzerModel.tagList);
        freshSignalInfo();
        getView().setUploadBtnStatus(checkCanUpload());
        getView().setDeployWeChatText(deployAnalyzerModel.weChatAccount);

    }

    public void updateCheckTipText(boolean isEnable) {
        if (isEnable) {
            if (Constants.TYPE_SCAN_DEPLOY_STATION == deployAnalyzerModel.deployType) {
                getView().setDeployLocalCheckTipText("");
            } else {
                if (CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType) || "mantun_fires".equals(deployAnalyzerModel.deviceType)) {
                    getView().setDeployLocalCheckTipText(mContext.getString(R.string.deploy_device_detail_check_tip_is_powered_on));
                } else {
                    DeviceTypeStyles configDeviceType = PreferencesHelper.getInstance().getConfigDeviceType(deployAnalyzerModel.deviceType);
                    if (configDeviceType != null) {
                        String mergeType = configDeviceType.getMergeType();
                        if ("smoke".equals(mergeType)) {
                            getView().setDeployLocalCheckTipText(mContext.getString(R.string.deploy_device_detail_check_tip_press_the_sensor));
                            return;
                        } else if ("fhsj_ch4".equals(deployAnalyzerModel.deviceType) || "baymax_ch4".equals(deployAnalyzerModel.deviceType)) {
                            getView().setDeployLocalCheckTipText(mContext.getString(R.string.deploy_device_detail_check_tip_press_the_sensor));
                            return;
                        }
                    }
                    getView().setDeployLocalCheckTipText("");
                }
            }
        } else {
            getView().setDeployLocalCheckTipText(mContext.getString(R.string.deploy_device_detail_add_all_required));
        }
    }

    //
    private void requestUpload() {


        DeployTask deployTask = new DeployTask();
        deployTask.deployType = deployAnalyzerModel.deployType;
        final double lon = deployAnalyzerModel.latLng.get(0);
        final double lan = deployAnalyzerModel.latLng.get(1);
        switch (deployAnalyzerModel.deployType) {
            case Constants.TYPE_SCAN_DEPLOY_STATION:
                //基站部署失败，单独处理
                getView().showProgressDialog();
                RetrofitServiceHelper.getInstance().doStationDeploy(deployAnalyzerModel.sn, lon, lan, deployAnalyzerModel.tagList, deployAnalyzerModel.nameAndAddress).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CityObserver<ResponseResult<DeployStationInfo>>(this) {

                            @Override
                            public void onErrorMsg(int errorCode, String errorMsg) {


                                getView().dismissProgressDialog();
                                getView().setUploadBtnStatus(true);
                                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                                    deployRetryUtil.addTask(deployAnalyzerModel);
                                    getView().showRetryDialog();
                                    getView().toastShort(errorMsg);
                                } else if (errorCode == 4013101 || errorCode == 4000013) {
                                    freshError(deployAnalyzerModel.sn, null, Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT);
                                } else {
                                    freshError(deployAnalyzerModel.sn, errorMsg, Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_FAILED);
                                }
                            }

                            @Override
                            public void onCompleted(ResponseResult<DeployStationInfo> deployStationInfoRsp) {

                                freshStation(deployStationInfoRsp);
                                getView().dismissProgressDialog();
                                getView().finishAc();
                            }
                        });
                break;
            case Constants.TYPE_SCAN_DEPLOY_DEVICE:
                //设备部署
            case Constants.TYPE_SCAN_DEPLOY_INSPECTION_DEVICE_CHANGE:
            case Constants.TYPE_SCAN_DEPLOY_MALFUNCTION_DEVICE_CHANGE:
                //巡检设备更换
                //TODO 暂时注释掉2g电气火灾的部署
                if ("acrel300T_fires_2G".equals(deployAnalyzerModel.deviceType) || "acrel300D_fires_2G".equals(deployAnalyzerModel.deviceType)) {
                    //2g电气火灾的配置
                    handle2GDeviceConfig();
                } else {
                    if (Constants.TYPE_SCAN_DEPLOY_WHITE_LIST == deployAnalyzerModel.whiteListDeployType) {
                        // 白名单设备
                        doUploadImages();
                    } else {
                        handleDeviceSignalStatusAndBleSetting();
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 单独处理2g的电气火灾类配置
     */
    private void handle2GDeviceConfig() {
        ArrayList<String> sns = new ArrayList<>();
        sns.add(deployAnalyzerModel.sn);
        getView().showBleConfigDialog();
        mScheduleNo = null;
        RetrofitServiceHelper.getInstance().doMonitorPointOperation2gDeploy(sns, "config", deployAnalyzerModel.settingData)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<MonitorPointOperationRequestRsp>(this) {
            @Override
            public void onCompleted(MonitorPointOperationRequestRsp response) {
                String scheduleNo = response.getScheduleNo();
                if (TextUtils.isEmpty(scheduleNo)) {
                    getView().dismissBleConfigDialog();
                    getView().toastShort(mContext.getString(R.string.deploy_device_detail_check_failed));
                } else {
                    String[] split = scheduleNo.split(",");
                    if (split.length > 0) {
                        mScheduleNo = split[0];
                        mHandler.removeCallbacks(DeviceTaskOvertime);
                        mHandler.postDelayed(DeviceTaskOvertime, 30 * 1000);
                    } else {
                        getView().dismissBleConfigDialog();
                        getView().toastShort(mContext.getString(R.string.deploy_device_detail_check_failed));

                    }
                }
                try {
                    LogUtils.loge(this, "socket-->>> DeviceTaskResultListener handle2GDeviceConfig  scheduleNo = " + scheduleNo);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().dismissBleConfigDialog();
                getView().toastShort(errorMsg);
                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                    deployRetryUtil.addTask(deployAnalyzerModel);
                    getView().showRetryDialog();
                }
            }
        });
    }

    private final Runnable DeviceTaskOvertime = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(DeviceTaskOvertime);
            mScheduleNo = null;
            getView().dismissBleConfigDialog();
            getView().showWarnDialog(PreferencesHelper.getInstance().getUserData().hasForceUpload, mContext.getString(R.string.deploy_device_detail_check_config_failed) + "，", mContext.getString(R.string.deploy_check_suggest_repair_instruction));

        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MonitorPointOperationTaskResultInfo monitorPointOperationTaskResultInfo) {
        final String scheduleNo = monitorPointOperationTaskResultInfo.getScheduleNo();
        try {
            LogUtils.loge(this, "socket-->>> DeviceTaskResultListener onMessageEvent  scheduleNo = " + scheduleNo);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
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
                                    //TODO 下行命令成功后开始安装检测
                                    checkDeviceSignalStatus();
//                                    final double lon = deployAnalyzerModel.latLng.get(0);
//                                    final double lan = deployAnalyzerModel.latLng.get(1);
//                                    getView().dismissBleConfigDialog();
//                                    doUploadImages(lon, lan);
                                }
                            }
                        }
                    });
                }
            }

        }
    }

    private void handleDeviceSignalStatusAndBleSetting() {

        final OnConfigInfoObserver<String> onConfigInfoObserver = new OnConfigInfoObserver<String>() {
            @Override
            public void onStart(String msg) {
                if (isAttachedView()) {
                    getView().showBleConfigDialog();
                }
            }

            @Override
            public void onSuccess(String s) {
                if (isAttachedView()) {
                    checkDeviceSignalStatus();
                }
            }

            @Override
            public void onFailed(String errorMsg) {
                tempForceReason = "config";
                getView().dismissBleConfigDialog();
                getView().showWarnDialog(PreferencesHelper.getInstance().getUserData().hasForceUpload, mContext.getString(R.string.deploy_device_detail_check_config_failed) + "，", mContext.getString(R.string.deploy_check_suggest_repair_instruction));
            }

            @Override
            public void onOverTime(String overTimeMsg) {
                tempForceReason = "config";
                getView().dismissBleConfigDialog();
                getView().showWarnDialog(PreferencesHelper.getInstance().getUserData().hasForceUpload, mContext.getString(R.string.deploy_device_detail_check_config_failed) + "，", mContext.getString(R.string.deploy_check_suggest_repair_instruction));
            }
        };
        if (PreferencesHelper.getInstance().getUserData().hasSignalConfig) {
            if (!TextUtils.isEmpty(deployAnalyzerModel.blePassword) && deployAnalyzerModel.channelMask.size() > 0) {
                //需要配置频点信息
                if (bleDevice.containsKey(deployAnalyzerModel.sn)) {
                    if (CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType)) {
                        if (deployAnalyzerModel.settingData != null) {
                            //配置频点信息和初始配置
                            connectDevice(onConfigInfoObserver);
                        } else {
                            getView().toastShort(mContext.getString(R.string.please_set_initial_configuration));
                        }
                    } else {
                        //直接配置频点信息
                        connectDevice(onConfigInfoObserver);
                    }

                } else {
                    //不在附近
                    tempForceReason = "lonlat";
                    getView().showWarnDialog(PreferencesHelper.getInstance().getUserData().hasForceUpload, mContext.getString(R.string.deploy_device_detail_check_not_nearby), "");
                }
            } else {
                getView().toastShort(mContext.getString(R.string.channel_mask_error_tip));
            }
            return;
        } else {
            if (CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType)) {
                //单独配置初始配置
                if (bleDevice.containsKey(deployAnalyzerModel.sn)) {
                    connectDevice(onConfigInfoObserver);
                } else {
                    //不在附近
                    tempForceReason = "lonlat";
                    getView().showWarnDialog(PreferencesHelper.getInstance().getUserData().hasForceUpload, mContext.getString(R.string.deploy_device_detail_check_not_nearby), "");
                }
                return;
            }
        }
//        doUploadImages(lon, lan);

        //统一进行信号和状态监测
        getView().showBleConfigDialog();
        checkDeviceSignalStatus();
    }

    /**
     * 信号和状态监测
     */
    private void checkDeviceSignalStatus() {
        HandlerDeployCheck.OnMessageDeal signalMsgDeal = new HandlerDeployCheck.OnMessageDeal() {
            @Override
            public void onNext() {
                int signalState = checkNeedSignal();
                switch (signalState) {
                    case DeoloyCheckPointConstants.DEPLOY_CHECK_DIALOG_SIGNAL_GOOD:
                        checkHandler.removeAllMessage();
                        deployAnalyzerModel.currentSignalQuality = "good";
                        getDeviceRealStatus();
                        break;
                    case DeoloyCheckPointConstants.DEPLOY_CHECK_DIALOG_SIGNAL_NORMAL:
                        deployAnalyzerModel.currentSignalQuality = "normal";
                        checkHandler.removeAllMessage();
                        getDeviceRealStatus();
                        break;
                }
            }

            @Override
            public void onFinish() {
                int state = checkNeedSignal();
                switch (state) {
                    case DeoloyCheckPointConstants.DEPLOY_CHECK_DIALOG_SIGNAL_NONE:
                        tempForceReason = "signalQuality";
                        deployAnalyzerModel.currentSignalQuality = "none";
                        getView().dismissBleConfigDialog();
                        getView().showWarnDialog(PreferencesHelper.getInstance().getUserData().hasForceUpload, mContext.getString(R.string.deploy_check_dialog_quality_bad_signal) + "，", mContext.getString(R.string.deploy_check_suggest_repair_instruction));
                        return;
                    case DeoloyCheckPointConstants.DEPLOY_CHECK_DIALOG_SIGNAL_BAD:
                        tempForceReason = "signalQuality";
                        deployAnalyzerModel.currentSignalQuality = "bad";
                        getView().dismissBleConfigDialog();
                        getView().showWarnDialog(PreferencesHelper.getInstance().getUserData().hasForceUpload, mContext.getString(R.string.deploy_check_dialog_quality_bad_signal) + "，", mContext.getString(R.string.deploy_check_suggest_repair_instruction));
                        return;
                }
                tempForceReason = "signalQuality";
                deployAnalyzerModel.currentSignalQuality = "none";
                getView().dismissBleConfigDialog();
                getView().showWarnDialog(PreferencesHelper.getInstance().getUserData().hasForceUpload, mContext.getString(R.string.deploy_check_dialog_quality_bad_signal) + "，", mContext.getString(R.string.deploy_check_suggest_repair_instruction));
            }
        };
        checkHandler.init(1000, signalCheckCount);
        checkHandler.dealMessage(3, signalMsgDeal);
    }


    private void doUploadImages() {
        if (getRealImageSize() > 0) {
            //TODO 图片提交
            final UpLoadPhotosUtils.UpLoadPhotoListener upLoadPhotoListener = new UpLoadPhotosUtils
                    .UpLoadPhotoListener() {

                @Override
                public void onStart() {
                    if (isAttachedView()) {
                        getView().showStartUploadProgressDialog();
                    }

                }

                @Override
                public void onComplete(List<ScenesData> scenesDataList) {
                    ArrayList<String> strings = new ArrayList<>();
                    for (ScenesData scenesData : scenesDataList) {
                        scenesData.type = "image";
                        strings.add(scenesData.url);
                    }
                    try {
                        LogUtils.loge(this, "上传成功--- size = " + strings.size());
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    if (isAttachedView()) {
                        getView().dismissUploadProgressDialog();
                        // 上传结果
                        doDeployResult(strings);
                        deployAnalyzerModel.imgUrls = strings;
                        deployAnalyzerModel.imageItems = null;

                    }


                }

                @Override
                public void onError(String errMsg) {
                    if (isAttachedView()) {
                        getView().setUploadBtnStatus(true);
                        getView().dismissUploadProgressDialog();
                        getView().toastShort(errMsg);
                        //失败，本地照片存储,重试
                        getView().showRetryDialog();
                        deployRetryUtil.addTask(deployAnalyzerModel);


                    }

                }

                @Override
                public void onProgress(String content, double percent) {
                    if (isAttachedView()) {
                        getView().showUploadProgressDialog(content, percent);
                    }

                }
            };
            UpLoadPhotosUtils upLoadPhotosUtils = new UpLoadPhotosUtils(mContext, upLoadPhotoListener);
            ArrayList<ImageItem> list = new ArrayList<>();
            for (DeployPicInfo deployPicInfo : deployAnalyzerModel.images) {
                if (deployPicInfo != null) {
                    if (deployPicInfo.photoItem != null) {
                        list.add(deployPicInfo.photoItem);
                    }

                }
            }
            deployAnalyzerModel.imageItems = list;


            //本地照片
            upLoadPhotosUtils.doUploadPhoto(list);
        } else {
            doDeployResult(null);
        }
    }

    //TODO 添加设备状态字段
    private void doDeployResult(List<String> imgUrls) {
//        DeployContactModel deployContactModel = deployAnalyzerModel.deployContactModelList.get(0);
        double lon = deployAnalyzerModel.latLng.get(0);
        double lan = deployAnalyzerModel.latLng.get(1);
        switch (deployAnalyzerModel.deployType) {
            case Constants.TYPE_SCAN_DEPLOY_DEVICE:
                //设备部署
                getView().showProgressDialog();
                boolean isFire = CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType);
                //暂时添加 后续可以删除
                DeployControlSettingData settingData = null;
                if (isFire) {
                    settingData = deployAnalyzerModel.settingData;
                }


                RetrofitServiceHelper.getInstance().doDevicePointDeploy(deployAnalyzerModel.sn, lon, lan, deployAnalyzerModel.tagList, deployAnalyzerModel.nameAndAddress,
                        deployAnalyzerModel.deployContactModelList, deployAnalyzerModel.weChatAccount, imgUrls, settingData, deployAnalyzerModel.forceReason, deployAnalyzerModel.status, deployAnalyzerModel.currentSignalQuality).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CityObserver<ResponseResult<DeviceInfo>>(this) {
                            @Override
                            public void onErrorMsg(int errorCode, String errorMsg) {
                                getView().dismissProgressDialog();
                                getView().setUploadBtnStatus(true);
                                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                                    getView().toastShort(errorMsg);
                                    deployRetryUtil.addTask(deployAnalyzerModel);
                                    getView().showRetryDialog();


                                } else if (errorCode == 4013101 || errorCode == 4000013) {
                                    freshError(deployAnalyzerModel.sn, null, Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT);
                                } else {
                                    freshError(deployAnalyzerModel.sn, errorMsg, Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_FAILED);
                                }
                            }

                            @Override
                            public void onCompleted(ResponseResult<DeviceInfo> deviceDeployRsp) {
                                freshPoint(deviceDeployRsp);
                                getView().dismissProgressDialog();
                                getView().finishAc();


                            }
                        });
                break;
            case Constants.TYPE_SCAN_DEPLOY_INSPECTION_DEVICE_CHANGE:
                getView().showProgressDialog();
                RetrofitServiceHelper.getInstance().doInspectionChangeDeviceDeploy(deployAnalyzerModel.mDeviceDetail.getSn(), deployAnalyzerModel.sn,
                        deployAnalyzerModel.mDeviceDetail.getTaskId(), 1, lon, lan, deployAnalyzerModel.tagList, deployAnalyzerModel.nameAndAddress,
                        deployAnalyzerModel.deployContactModelList, imgUrls, null, deployAnalyzerModel.forceReason, deployAnalyzerModel.status, deployAnalyzerModel.currentSignalQuality).
                        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(this) {
                    @Override
                    public void onCompleted(ResponseResult<DeviceInfo> deviceDeployRsp) {

                        freshPoint(deviceDeployRsp);
                        getView().dismissProgressDialog();
                        getView().finishAc();

                    }

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().setUploadBtnStatus(true);
                        if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                            getView().toastShort(errorMsg);
                            deployRetryUtil.addTask(deployAnalyzerModel);
                            getView().showRetryDialog();


                        } else if (errorCode == 4013101 || errorCode == 4000013) {
                            freshError(deployAnalyzerModel.sn, null, Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT);
                        } else {
                            freshError(deployAnalyzerModel.sn, errorMsg, Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_FAILED);
                        }
                    }
                });
                break;
            case Constants.TYPE_SCAN_DEPLOY_MALFUNCTION_DEVICE_CHANGE:
                getView().showProgressDialog();
                RetrofitServiceHelper.getInstance().doInspectionChangeDeviceDeploy(deployAnalyzerModel.mDeviceDetail.getSn(), deployAnalyzerModel.sn,
                        null, 2, lon, lan, deployAnalyzerModel.tagList, deployAnalyzerModel.nameAndAddress, deployAnalyzerModel.deployContactModelList, imgUrls, null, deployAnalyzerModel.forceReason, deployAnalyzerModel.status, deployAnalyzerModel.currentSignalQuality).
                        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(this) {
                    @Override
                    public void onCompleted(ResponseResult<DeviceInfo> deviceDeployRsp) {
                        //
                        freshPoint(deviceDeployRsp);
                        getView().dismissProgressDialog();
                        getView().finishAc();
                    }

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().setUploadBtnStatus(true);
                        if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                            getView().toastShort(errorMsg);
                            deployRetryUtil.addTask(deployAnalyzerModel);
                            getView().showRetryDialog();
                        } else if (errorCode == 4013101 || errorCode == 4000013) {
                            freshError(deployAnalyzerModel.sn, null, Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT);
                        } else {
                            freshError(deployAnalyzerModel.sn, errorMsg, Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_FAILED);
                        }
                    }
                });
                break;
            default:
                break;
        }

    }


    private void freshError(String scanSN, String errorInfo, int resultCode) {
        //
        Intent intent = new Intent();
        intent.setClass(mContext, DeployResultActivity.class);
        DeployResultModel deployResultModel = new DeployResultModel();
        deployResultModel.sn = scanSN;
        deployResultModel.deviceType = deployAnalyzerModel.deviceType;
        deployResultModel.resultCode = resultCode;
        deployResultModel.scanType = deployAnalyzerModel.deployType;
        deployResultModel.errorMsg = errorInfo;
        deployResultModel.wxPhone = deployAnalyzerModel.weChatAccount;
        deployResultModel.settingData = deployAnalyzerModel.settingData;
        if (deployAnalyzerModel.deployContactModelList.size() > 0) {
            deployResultModel.deployContactModelList.addAll(deployAnalyzerModel.deployContactModelList);
        }
        deployResultModel.address = deployAnalyzerModel.address;
        deployResultModel.updateTime = deployAnalyzerModel.updatedTime;
        deployResultModel.deviceStatus = deployAnalyzerModel.status;
        if (TextUtils.isEmpty(deployAnalyzerModel.currentSignalQuality)) {
            if (TextUtils.isEmpty(deployAnalyzerModel.signal)) {
                deployAnalyzerModel.signal = "none";
            }
            deployResultModel.signal = String.copyValueOf(deployAnalyzerModel.signal.toCharArray());
        } else {
            deployResultModel.signal = deployAnalyzerModel.currentSignalQuality;
        }
        deployResultModel.name = deployAnalyzerModel.nameAndAddress;
        intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
        getView().startAC(intent);
    }

    private void freshPoint(ResponseResult<DeviceInfo> deviceDeployRsp) {

        DeployResultModel deployResultModel = new DeployResultModel();
        DeviceInfo deviceInfo = deviceDeployRsp.getData();
        deployResultModel.deviceInfo = deviceInfo;
        Intent intent = new Intent(mContext, DeployResultActivity.class);
        //
        deployResultModel.sn = deviceInfo.getSn();
        deployResultModel.deviceType = deployAnalyzerModel.deviceType;
        deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_SUCCESS;
        deployResultModel.scanType = deployAnalyzerModel.deployType;
        deployResultModel.wxPhone = deployAnalyzerModel.weChatAccount;
        deployResultModel.settingData = deployAnalyzerModel.settingData;
        //TODO 新版联系人
        if (deployAnalyzerModel.deployContactModelList.size() > 0) {
            deployResultModel.deployContactModelList.addAll(deployAnalyzerModel.deployContactModelList);
        }
        deployResultModel.address = deployAnalyzerModel.address;
        deployResultModel.updateTime = deviceInfo.getUpdatedTime();
        deployResultModel.deployTime = deviceInfo.getDeployTime();
        deployResultModel.deviceStatus = deployAnalyzerModel.status;
        if (TextUtils.isEmpty(deployAnalyzerModel.currentSignalQuality)) {
            deployResultModel.signal = deviceInfo.getSignal();
        } else {
            deployResultModel.signal = deployAnalyzerModel.currentSignalQuality;
        }
        deployResultModel.name = deployAnalyzerModel.nameAndAddress;
        intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
        getView().startAC(intent);

    }

    private void freshStation(ResponseResult<DeployStationInfo> deployStationInfoRsp) {

        DeployResultModel deployResultModel = new DeployResultModel();
        //
        Intent intent = new Intent(mContext, DeployResultActivity.class);
        DeployStationInfo deployStationInfo = deployStationInfoRsp.getData();
        deployResultModel.name = deployStationInfo.getName();
        deployResultModel.sn = deployStationInfo.getSn();
        deployResultModel.deviceType = deployAnalyzerModel.deviceType;
        deployResultModel.stationStatus = deployStationInfo.getNormalStatus();
        deployResultModel.updateTime = deployStationInfo.getUpdatedTime();
        deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_SUCCESS;
        deployResultModel.scanType = deployAnalyzerModel.deployType;
        deployResultModel.address = deployAnalyzerModel.address;
        if (TextUtils.isEmpty(deployAnalyzerModel.signal)) {
            deployAnalyzerModel.signal = "none";
        }
        deployResultModel.signal = String.copyValueOf(deployAnalyzerModel.signal.toCharArray());
        intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
        getView().startAC(intent);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        deployAnalyzerModel.tagList.clear();
        deployAnalyzerModel.images.clear();
        mHandler.removeCallbacksAndMessages(null);
        checkHandler.removeAllMessage();
        SensoroCityApplication.getInstance().bleDeviceManager.stopService();
        BleObserver.getInstance().unregisterBleObserver(this);
        bleDevice.clear();

    }

    public void doNameAddress() {
        Intent intent = new Intent(mContext, DeployMonitorNameAddressActivity.class);
        if (!TextUtils.isEmpty(deployAnalyzerModel.nameAndAddress)) {
            intent.putExtra(Constants.EXTRA_SETTING_NAME_ADDRESS, deployAnalyzerModel.nameAndAddress);
        }
        getView().startAC(intent);
    }

    public void doAlarmContact() {
        Intent intent = new Intent(mContext, DeployMonitorAlarmContactActivity.class);
        if (deployAnalyzerModel.deployContactModelList.size() > 0) {
            intent.putExtra(Constants.EXTRA_SETTING_DEPLOY_CONTACT, (ArrayList<DeployContactModel>) deployAnalyzerModel.deployContactModelList);
        }
        getView().startAC(intent);
    }

    public void doTag() {
        Bundle bundle = new Bundle();
        if (deployAnalyzerModel.tagList.size() > 0) {
            bundle.putStringArrayList(Constants.EXTRA_SETTING_TAG_LIST, (ArrayList<String>) deployAnalyzerModel.tagList);
        }
        startActivity(ARouterConstants.ACTIVITY_DEPLOY_DEVICE_TAG, bundle, mContext);
    }

    public void doSettingPhoto() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.EXTRA_DEPLOY_TO_PHOTO, deployAnalyzerModel.images);
        bundle.putString(Constants.EXTRA_SETTING_DEPLOY_DEVICE_TYPE, deployAnalyzerModel.deviceType);
        startActivity(ARouterConstants.ACTIVITY_DEPLOY_DEVICE_PIC, bundle, mContext);

//        Intent intent = new Intent(mContext, DeployMonitorDeployPicActivity.class);
//        if (getRealImageSize() > 0) {
//            intent.putExtra(EXTRA_DEPLOY_TO_PHOTO, deployAnalyzerModel.images);
//        }
//        intent.putExtra(EXTRA_SETTING_DEPLOY_DEVICE_TYPE, deployAnalyzerModel.deviceType);
//        getView().startAC(intent);
    }

    public void doDeployMap() {
        Intent intent = new Intent();
        if (AppUtils.isChineseLanguage()) {
            intent.setClass(mContext, DeployMapActivity.class);
        } else {
            intent.setClass(mContext, DeployMapENActivity.class);
        }
        deployAnalyzerModel.mapSourceType = Constants.DEPLOY_MAP_SOURCE_TYPE_DEPLOY_MONITOR_DETAIL;
        intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
        getView().startAC(intent);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(DeviceInfo deviceInfo) {
        String sn = deviceInfo.getSn();
        try {
            if (deployAnalyzerModel.sn.equalsIgnoreCase(sn)) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isAttachedView()) {
                            deployAnalyzerModel.updatedTime = deviceInfo.getUpdatedTime();
                            String signal = deviceInfo.getSignal();
                            if (TextUtils.isEmpty(signal)) {
                                signal = "none";
                            }
                            deployAnalyzerModel.signal = String.copyValueOf(signal.toCharArray());
                            freshSignalInfo();
                        }
                    }
                });
//                            getView().toastLong("信号-->>time = " + deployAnalyzerModel.updatedTime + ",signal = " + deployAnalyzerModel.signal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData eventData) {
        int code = eventData.code;
        Object data = eventData.data;
        switch (code) {
            case Constants.EVENT_DATA_DEPLOY_RESULT_FINISH:
            case Constants.EVENT_DATA_DEPLOY_CHANGE_RESULT_CONTINUE:
            case Constants.EVENT_DATA_DEPLOY_RESULT_CONTINUE:
                getView().finishAc();
                break;
            case Constants.EVENT_DATA_DEPLOY_SETTING_NAME_ADDRESS:
                if (data instanceof String) {
                    deployAnalyzerModel.nameAndAddress = (String) data;
                    getView().setNameAddressText(deployAnalyzerModel.nameAndAddress);
                }
                getView().setUploadBtnStatus(checkCanUpload());
                break;
            case Constants.EVENT_DATA_DEPLOY_SETTING_TAG:
                if (data instanceof List) {
                    deployAnalyzerModel.tagList.clear();
                    deployAnalyzerModel.tagList.addAll((List<String>) data);
                    getView().updateTagsData(deployAnalyzerModel.tagList);
                }
                break;
            case Constants.EVENT_DATA_DEPLOY_SETTING_CONTACT:
                if (data instanceof List) {
                    //TODO 联系人
                    deployAnalyzerModel.deployContactModelList.clear();
                    deployAnalyzerModel.deployContactModelList.addAll((List<DeployContactModel>) data);
                    if (deployAnalyzerModel.deployContactModelList.size() > 0) {
                        DeployContactModel deployContactModel = deployAnalyzerModel.deployContactModelList.get(0);
                        String contact = deployContactModel.name;
                        String content = deployContactModel.phone;
                        getView().setFirstContact(contact + "(" + content + ")");
                    }
                    getView().setTotalContact(deployAnalyzerModel.deployContactModelList.size());
//                    getView().updateContactData(deployAnalyzerModel.deployContactModelList);
                }
                getView().setUploadBtnStatus(checkCanUpload());
                break;
            case Constants.EVENT_DATA_DEPLOY_SETTING_PHOTO:
                if (data instanceof List) {
                    deployAnalyzerModel.images.clear();

                    deployAnalyzerModel.images.addAll((ArrayList<DeployPicInfo>) data);

                    int realImageSize = getRealImageSize();
                    switch (realImageSize) {
                        case -1:
                            getView().setDeployPhotoText(mContext.getString(R.string.missing_required_photo));
                            break;
                        case 0:
                            getView().setDeployPhotoText(null);
                            break;
                        default:
                            getView().setDeployPhotoText(mContext.getString(R.string.added) + realImageSize + mContext.getString(R.string.images));
                            break;
                    }
                    getView().setUploadBtnStatus(checkCanUpload());
                }
                break;
            case Constants.EVENT_DATA_DEPLOY_MAP:
                if (data instanceof DeployAnalyzerModel) {
                    this.deployAnalyzerModel = (DeployAnalyzerModel) data;
                    freshSignalInfo();
                }
                getView().setUploadBtnStatus(checkCanUpload());
                break;
            case Constants.EVENT_DATA_DEPLOY_SETTING_WE_CHAT_RELATION:
                if (data instanceof String) {
                    deployAnalyzerModel.weChatAccount = (String) data;
                    getView().setDeployWeChatText(deployAnalyzerModel.weChatAccount);
                }
                break;
            case Constants.EVENT_DATA_DEPLOY_INIT_CONFIG_CODE:
                if (data instanceof DeployControlSettingData) {
                    deployAnalyzerModel.settingData = (DeployControlSettingData) data;
                    Integer initValue = deployAnalyzerModel.settingData.getSwitchSpec();
                    Integer transformer = deployAnalyzerModel.settingData.getTransformer();
                    if (CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType)) {
                        if (transformer != null) {
                            if (initValue != null) {
                                getView().setDeployDeviceDetailDeploySetting(mContext.getString(R.string.actual_overcurrent_threshold) + ":" + initValue + "A" + "\n" + mContext.getString(R.string.device_detail_config_trans) + ":" + transformer + "A");
                            }
                        } else {
                            if (initValue != null) {
                                getView().setDeployDeviceDetailDeploySetting(mContext.getString(R.string.actual_overcurrent_threshold) + ":" + initValue + "A");
                            }
                        }

                    }
                } else {
                    getView().setDeployDeviceDetailDeploySetting(null);
                }
                getView().setUploadBtnStatus(checkCanUpload());
            default:
                break;
        }
    }

    /**
     * -1为缺少必传照片
     *
     * @return
     */
    private int getRealImageSize() {
        int count = 0;
        boolean need = false;
        for (DeployPicInfo deployPicInfo : deployAnalyzerModel.images) {
            if (deployPicInfo != null) {
                if (deployPicInfo.isRequired != null && deployPicInfo.isRequired) {
                    if (deployPicInfo.photoItem == null) {
                        need = true;
                    }
                }
                if (deployPicInfo.photoItem != null) {
                    count++;
                }
            }
        }
        if (count == 0) {
            return count;
        } else {
            if (need) {
                return -1;
            }
        }
        return count;
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }

    //TODO 添加接口
    public void doConfirm() {
        //姓名地址校验
        switch (deployAnalyzerModel.deployType) {
            case Constants.TYPE_SCAN_DEPLOY_STATION:
                if (isNoPhoto()) return;
                //经纬度校验
                if (isNoLatLng()) return;
                requestUpload();
                break;
            case Constants.TYPE_SCAN_DEPLOY_DEVICE:
            case Constants.TYPE_SCAN_DEPLOY_INSPECTION_DEVICE_CHANGE:
            case Constants.TYPE_SCAN_DEPLOY_MALFUNCTION_DEVICE_CHANGE:
                //联系人校验
                if (isNoContact()) return;
                if (isNoPhoto()) return;
                //经纬度校验
                if (isNoLatLng()) return;
                boolean isFire = CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType);
                if (isFire) {
                    if (deployAnalyzerModel.settingData == null) {
                        getView().toastShort(mContext.getString(R.string.deploy_has_no_configuration_tip));
                        return;
                    }
                }
                requestUpload();
                break;
            default:
                break;
        }

    }

    private boolean checkCanUpload() {
        String name_default = mContext.getString(R.string.tips_hint_name_address);
        if (TextUtils.isEmpty(deployAnalyzerModel.nameAndAddress) || deployAnalyzerModel.nameAndAddress.equals(name_default)) {
            return false;
        } else {
            byte[] bytes = new byte[0];
            try {
                bytes = deployAnalyzerModel.nameAndAddress.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (bytes.length > 48) {
                return false;
            }
        }
        switch (deployAnalyzerModel.deployType) {
            case Constants.TYPE_SCAN_DEPLOY_STATION:
                //经纬度校验
                if (deployAnalyzerModel.latLng.size() != 2) {
                    return false;
                }
                break;
            case Constants.TYPE_SCAN_DEPLOY_DEVICE:
            case Constants.TYPE_SCAN_DEPLOY_INSPECTION_DEVICE_CHANGE:
            case Constants.TYPE_SCAN_DEPLOY_MALFUNCTION_DEVICE_CHANGE:
                //联系人校验
                if (deployAnalyzerModel.deployContactModelList.size() > 0) {
                    DeployContactModel deployContactModel = deployAnalyzerModel.deployContactModelList.get(0);
                    if (TextUtils.isEmpty(deployContactModel.name) || TextUtils.isEmpty(deployContactModel.phone)) {
                        return false;
                    }
                    if (!RegexUtils.checkPhone(deployContactModel.phone)) {
                        return false;
                    }
                } else {
                    return false;
                }
                //照片校验
                if (deployAnalyzerModel.deployType != Constants.TYPE_SCAN_DEPLOY_STATION) {
                    if (getRealImageSize() <= 0) {
                        return false;
                    }
                }

                //经纬度校验
                if (deployAnalyzerModel.latLng.size() != 2) {
                    return false;
                }
                boolean isFire = CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType);
                if (isFire) {
                    if (deployAnalyzerModel.settingData == null) {
                        return false;
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 检测是否填写过联系人
     *
     * @return
     */
    private boolean isNoContact() {
        if (deployAnalyzerModel.deployContactModelList.size() > 0) {
            DeployContactModel deployContactModel = deployAnalyzerModel.deployContactModelList.get(0);
            if (TextUtils.isEmpty(deployContactModel.name) || TextUtils.isEmpty(deployContactModel.phone)) {
                getView().toastShort(mContext.getString(R.string.please_enter_contact_phone));
                getView().setUploadBtnStatus(true);
                return true;
            }
            if (!RegexUtils.checkPhone(deployContactModel.phone)) {
                getView().toastShort(mContext.getResources().getString(R.string.tips_phone_empty));
                getView().setUploadBtnStatus(true);
                return true;
            }
        } else {
            getView().toastShort(mContext.getString(R.string.please_enter_contact_phone));
            getView().setUploadBtnStatus(true);
            return true;
        }
        return false;
    }

    /**
     * 检测是否有经纬度
     *
     * @return
     */
    private boolean isNoLatLng() {
        if (deployAnalyzerModel.latLng.size() != 2) {
            getView().toastShort(mContext.getString(R.string.please_specify_the_deployment_location));
            getView().setUploadBtnStatus(true);
            return true;
        }
        return false;
    }

    /**
     * 检测是否有图片
     *
     * @return
     */
    private boolean isNoPhoto() {
        if (deployAnalyzerModel.deployType != Constants.TYPE_SCAN_DEPLOY_STATION) {

            int realImageSize = getRealImageSize();
            switch (realImageSize) {
                case -1:
                    getView().toastShort(mContext.getString(R.string.missing_required_photo));
                    getView().setUploadBtnStatus(true);
                    return true;
                case 0:
                    getView().toastShort(mContext.getString(R.string.please_add_at_least_one_image));
                    getView().setUploadBtnStatus(true);
                    return true;
                default:
                    break;
            }
        }
        return false;


    }


    private void freshSignalInfo() {
        Resources resources = mContext.getResources();
        String signal_text = mContext.getString(R.string.s_none);
        long time_diff = System.currentTimeMillis() - deployAnalyzerModel.updatedTime;
        //
        Drawable drawable = resources.getDrawable(R.drawable.signal_none);
        if (deployAnalyzerModel.signal != null && (time_diff < signalTimeRule)) {
            switch (deployAnalyzerModel.signal) {
                case "good":
                    signal_text = mContext.getString(R.string.s_good);
                    drawable = resources.getDrawable(R.drawable.signal_good);
                    break;
                case "normal":
                    signal_text = mContext.getString(R.string.s_normal);
                    drawable = resources.getDrawable(R.drawable.signal_normal);
                    break;
                case "bad":
                    signal_text = mContext.getString(R.string.s_bad);
                    drawable = resources.getDrawable(R.drawable.signal_bad);
                    break;
                default:
                    signal_text = mContext.getString(R.string.s_none);
                    drawable = resources.getDrawable(R.drawable.signal_none);
                    break;
            }
        } else {
            deployAnalyzerModel.signal = "none";
        }
        //
        switch (deployAnalyzerModel.deployType) {
            case Constants.TYPE_SCAN_DEPLOY_STATION:
                if (deployAnalyzerModel.latLng.size() != 2) {
                    getView().refreshSignal(true, signal_text, drawable, mContext.getString(R.string.not_positioned));
                } else {
                    getView().refreshSignal(true, signal_text, drawable, mContext.getString(R.string.positioned));
                }
                break;
            case Constants.TYPE_SCAN_DEPLOY_DEVICE:
            case Constants.TYPE_SCAN_DEPLOY_INSPECTION_DEVICE_CHANGE:
            case Constants.TYPE_SCAN_DEPLOY_MALFUNCTION_DEVICE_CHANGE:
                if (deployAnalyzerModel.latLng.size() != 2) {
                    getView().refreshSignal(false, signal_text, drawable, mContext.getString(R.string.required));
                } else {
                    getView().refreshSignal(false, signal_text, drawable, mContext.getString(R.string.positioned));
                }
                break;
            case Constants.TYPE_SCAN_INSPECTION:
                //扫描巡检设备
                break;
            default:
                break;
        }


    }

    /**
     * 检查信号状态
     *
     * @return
     */
    private int checkNeedSignal() {
        long time_diff = System.currentTimeMillis() - deployAnalyzerModel.updatedTime;
        if (deployAnalyzerModel.signal != null && (time_diff < signalTimeRule)) {
            switch (deployAnalyzerModel.signal) {
                case "good":
                    return DeoloyCheckPointConstants.DEPLOY_CHECK_DIALOG_SIGNAL_GOOD;
                case "normal":
                    return DeoloyCheckPointConstants.DEPLOY_CHECK_DIALOG_SIGNAL_NORMAL;
                case "bad":
                    return DeoloyCheckPointConstants.DEPLOY_CHECK_DIALOG_SIGNAL_BAD;
            }
        }
        return DeoloyCheckPointConstants.DEPLOY_CHECK_DIALOG_SIGNAL_NONE;
    }

    @Override
    public void onNewDevice(final BLEDevice bleDevice) {
        this.bleDevice.put(bleDevice.getSn(), bleDevice);
    }

    @Override
    public void onGoneDevice(BLEDevice bleDevice) {
        try {
            this.bleDevice.remove(bleDevice.getSn());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateDevices(ArrayList<BLEDevice> deviceList) {
        for (BLEDevice device : deviceList) {
            if (device != null) {
                bleDevice.put(device.getSn(), device);
            }
        }
    }

    @Override
    public void run() {
        boolean bleHasOpen = SensoroCityApplication.getInstance().bleDeviceManager.isBluetoothEnabled();
        if (bleHasOpen) {
            try {
                bleHasOpen = SensoroCityApplication.getInstance().bleDeviceManager.startService();
            } catch (Exception e) {
                e.printStackTrace();
                getView().showBleTips();
            }
            if (bleHasOpen) {
                getView().hideBleTips();
            } else {
                getView().showBleTips();
            }
        } else {
            getView().showBleTips();
        }
        mHandler.postDelayed(this, 2000);
        getView().setDeployDeviceDetailFixedPointNearVisible(bleDevice.containsKey(deployAnalyzerModel.sn));

    }

    public void doWeChatRelation() {
        if (deployAnalyzerModel.deployType == Constants.TYPE_SCAN_DEPLOY_DEVICE) {
            Intent intent = new Intent(mContext, DeployMonitorWeChatRelationActivity.class);
            if (!TextUtils.isEmpty(deployAnalyzerModel.weChatAccount)) {
                intent.putExtra(Constants.EXTRA_SETTING_WE_CHAT_RELATION, deployAnalyzerModel.weChatAccount);
            }
            intent.putExtra(Constants.EXTRA_DEPLOY_TO_SN, deployAnalyzerModel.sn);
            getView().startAC(intent);
            getView().startACForResult(intent, Constants.REQUEST_CODE_INIT_CONFIG);
        }
    }

    public void doDeployBleSetting() {
        //TODO
        if (CityConstants.DEVICE_CONTROL_NEW_CONFIG_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType)) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.EXTRA_DEPLOY_CONFIGURATION_SETTING_DATA, deployAnalyzerModel.settingData);
            bundle.putInt(Constants.EXTRA_DEPLOY_CONFIGURATION_ORIGIN_TYPE, Constants.DEPLOY_CONFIGURATION_SOURCE_TYPE_DEPLOY_DEVICE);
            bundle.putSerializable(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
            startActivity(ARouterConstants.ACTIVITY_THREE_PHASE_ELECT_CONFIG_ACTIVITY, bundle, mContext);
        } else {
            Intent intent = new Intent(mContext, SingleMonitorConfigurationActivity.class);
            if (deployAnalyzerModel.settingData != null) {
                intent.putExtra(Constants.EXTRA_DEPLOY_CONFIGURATION_SETTING_DATA, deployAnalyzerModel.settingData);
            }
            intent.putExtra(Constants.EXTRA_DEPLOY_CONFIGURATION_ORIGIN_TYPE, Constants.DEPLOY_CONFIGURATION_SOURCE_TYPE_DEPLOY_DEVICE);
            intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
            getView().startAC(intent);

        }
    }

    @Override
    public void onStart() {
        SensoroCityApplication.getInstance().bleDeviceManager.startScan();
    }

    @Override
    public void onStop() {
        SensoroCityApplication.getInstance().bleDeviceManager.stopScan();
    }

    private void connectDevice(final OnConfigInfoObserver onConfigInfoObserver) {
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
            sensoroDeviceConnection = new SensoroDeviceConnection(mContext, bleDevice.get(deployAnalyzerModel.sn).getMacAddress());
            //蓝牙连接回调
            final SensoroConnectionCallback sensoroConnectionCallback = new SensoroConnectionCallback() {
                @Override
                public void onConnectedSuccess(final BLEDevice bleDevice, int cmd) {
                    if (isAttachedView()) {
                        //连接成功后写命令超时
                        if (PreferencesHelper.getInstance().getUserData().hasSignalConfig) {
                            //如果需要写频点信息 写入频点信息回调
                            final SensoroWriteCallback SignalWriteCallback = new SensoroWriteCallback() {
                                @Override
                                public void onWriteSuccess(Object o, int cmd) {
                                    if (isAttachedView()) {
                                        //需要写频点信息
                                        if (CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType)) {
                                            if (deployAnalyzerModel.settingData != null) {
                                                SensoroDevice sensoroDevice = DeployConfigurationAnalyzer.configurationData(deployAnalyzerModel.deviceType, (SensoroDevice) bleDevice, deployAnalyzerModel.settingData.getSwitchSpec(), deployAnalyzerModel.settingData.getTransformer());
                                                if (sensoroDevice != null) {
                                                    //频点信息写入状态回调
                                                    final SensoroWriteCallback configWriteCallback = new SensoroWriteCallback() {
                                                        @Override
                                                        public void onWriteSuccess(Object o, int cmd) {
                                                            if (isAttachedView()) {
                                                                sensoroDeviceConnection.disconnect();
                                                                mHandler.removeCallbacks(configOvertime);
                                                                onConfigInfoObserver.onSuccess(null);
                                                            }
                                                        }

                                                        @Override
                                                        public void onWriteFailure(int errorCode, int cmd) {
                                                            if (isAttachedView()) {
                                                                sensoroDeviceConnection.disconnect();
                                                                mHandler.removeCallbacks(configOvertime);
                                                                onConfigInfoObserver.onFailed(mContext.getString(R.string.ble_init_config_write_failure));
                                                            }
                                                        }
                                                    };
                                                    sensoroDeviceConnection.writeData05Configuration(sensoroDevice, configWriteCallback);
                                                } else {
                                                    sensoroDeviceConnection.disconnect();
                                                    mHandler.removeCallbacks(configOvertime);
                                                    onConfigInfoObserver.onFailed(mContext.getString(R.string.init_config_not_support_device));
                                                }

                                            } else {
                                                sensoroDeviceConnection.disconnect();
                                                mHandler.removeCallbacks(configOvertime);
                                                onConfigInfoObserver.onFailed(mContext.getString(R.string.init_config_info_error));
                                            }
                                        } else {
                                            //不需要写入信息直接成功
                                            sensoroDeviceConnection.disconnect();
                                            mHandler.removeCallbacks(configOvertime);
                                            onConfigInfoObserver.onSuccess(null);
                                        }
                                    }

                                }

                                @Override
                                public void onWriteFailure(int errorCode, int cmd) {
                                    if (isAttachedView()) {
                                        sensoroDeviceConnection.disconnect();
                                        mHandler.removeCallbacks(configOvertime);
                                        onConfigInfoObserver.onFailed(mContext.getString(R.string.frequency_config_write_failure));
                                    }

                                }

                            };
                            sensoroDeviceConnection.writeData05ChannelMask(deployAnalyzerModel.channelMask, SignalWriteCallback);
                        } else {
                            if (CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType)) {
                                //需要写入配置信息
                                if (deployAnalyzerModel.settingData != null) {
                                    SensoroDevice sensoroDevice = DeployConfigurationAnalyzer.configurationData(deployAnalyzerModel.deviceType, (SensoroDevice) bleDevice, deployAnalyzerModel.settingData.getSwitchSpec(), deployAnalyzerModel.settingData.getTransformer());
                                    if (sensoroDevice != null) {
                                        //配置信息写入回调
                                        SensoroWriteCallback configWriteCallback = new SensoroWriteCallback() {
                                            @Override
                                            public void onWriteSuccess(Object o, int cmd) {
                                                if (isAttachedView()) {
                                                    sensoroDeviceConnection.disconnect();
                                                    mHandler.removeCallbacks(configOvertime);
                                                    onConfigInfoObserver.onSuccess(null);
                                                }
                                            }

                                            @Override
                                            public void onWriteFailure(int errorCode, int cmd) {
                                                if (isAttachedView()) {
                                                    sensoroDeviceConnection.disconnect();
                                                    mHandler.removeCallbacks(configOvertime);
                                                    onConfigInfoObserver.onFailed(mContext.getString(R.string.ble_init_config_write_failure));
                                                }

                                            }
                                        };
                                        sensoroDeviceConnection.writeData05Configuration(sensoroDevice, configWriteCallback);
                                    } else {
                                        sensoroDeviceConnection.disconnect();
                                        mHandler.removeCallbacks(configOvertime);
                                        onConfigInfoObserver.onFailed(mContext.getString(R.string.init_config_not_support_device));
                                    }
                                } else {
                                    getView().toastShort(mContext.getString(R.string.please_set_initial_configuration));
                                    sensoroDeviceConnection.disconnect();
                                    mHandler.removeCallbacks(configOvertime);
                                    onConfigInfoObserver.onFailed(mContext.getString(R.string.init_config_info_error));
                                }
                            } else {
                                //不需要直接成功
                                sensoroDeviceConnection.disconnect();
                                mHandler.removeCallbacks(configOvertime);
                                onConfigInfoObserver.onSuccess(null);
                            }

                        }
                    }
                }

                @Override
                public void onConnectedFailure(int errorCode) {
                    if (isAttachedView()) {
                        mHandler.removeCallbacks(configOvertime);
                        onConfigInfoObserver.onFailed(mContext.getString(R.string.deploy_check_ble_connect_error));
                    }
                }

                @Override
                public void onDisconnected() {

                }
            };
            sensoroDeviceConnection.connect(deployAnalyzerModel.blePassword, sensoroConnectionCallback);
            mHandler.postDelayed(configOvertime, 15 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.removeCallbacks(configOvertime);
            onConfigInfoObserver.onFailed(mContext.getString(R.string.unknown_error));
        }
    }

    public String getRepairInstructionUrl() {
        String mergeType = WidgetUtil.handleMergeType(deployAnalyzerModel.deviceType);
        if (TextUtils.isEmpty(mergeType)) {
            return null;
        }
        MergeTypeStyles configMergeType = PreferencesHelper.getInstance().getConfigMergeType(mergeType);
        if (configMergeType == null) {
            return null;
        }
        return configMergeType.getFixSpecificationUrl();
    }

    /**
     * 跳转配置说明界面
     *
     * @param repairInstructionUrl
     */
    public void doInstruction(String repairInstructionUrl) {
        Intent intent = new Intent(mContext, DeployRepairInstructionActivity.class);
        intent.putExtra(Constants.EXTRA_DEPLOY_CHECK_REPAIR_INSTRUCTION_URL, repairInstructionUrl);
        getView().startAC(intent);
    }

    private void getDeviceRealStatus() {
        final long requestTime = System.currentTimeMillis();
        RetrofitServiceHelper.getInstance().getDeviceRealStatus(deployAnalyzerModel.sn).subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(2, 100))
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(this) {
            @Override
            public void onCompleted(final ResponseResult<DeviceInfo> data) {
                long diff = System.currentTimeMillis() - requestTime;
                if (diff >= 1000) {
                    updateDeviceStatusDialog(data);
                } else {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateDeviceStatusDialog(data);
                        }
                    }, 1000 - diff);
                }
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                tempForceReason = null;
                deployAnalyzerModel.currentSignalQuality = null;
                // 获取不到当前状态是否强制上传
                getView().toastShort(errorMsg);
                getView().dismissBleConfigDialog();
                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
//                    deployAnalyzerModel.isGetDeviceRealStatusFailure = true;
                    deployRetryUtil.addTask(deployAnalyzerModel);
                    getView().showRetryDialog();
                }
            }
        });
    }

    private void updateDeviceStatusDialog(ResponseResult<DeviceInfo> data) {
        if (data != null && data.getData() != null) {
            //只记录当前的信号和状态
            deployAnalyzerModel.status = data.getData().getStatus();
            switch (data.getData().getStatus()) {
                case Constants.SENSOR_STATUS_ALARM:
                    tempForceReason = "status";
                    tempStatus = data.getData().getStatus();
                    String alarmReason = handleAlarmReason(data.getData());
                    getView().dismissBleConfigDialog();
                    getView().showWarnDialog(PreferencesHelper.getInstance().getUserData().hasForceUpload, alarmReason, mContext.getString(R.string.deploy_check_suggest_repair_instruction));
                    break;
                case Constants.SENSOR_STATUS_MALFUNCTION:
                    tempForceReason = "status";
                    tempStatus = data.getData().getStatus();
                    String reason = handleMalfunctionReason(data.getData());
                    getView().dismissBleConfigDialog();
                    getView().showWarnDialog(PreferencesHelper.getInstance().getUserData().hasForceUpload, reason, mContext.getString(R.string.deploy_check_suggest_repair_instruction));
                    break;
                default:
                    tempForceReason = null;
                    //TODO 成功
                    getView().dismissBleConfigDialog();
                    doUploadImages();
                    break;
            }
        } else {
            //状态错误
            tempForceReason = null;
            deployAnalyzerModel.currentSignalQuality = null;
            String errMsg;
            if (AppUtils.isChineseLanguage()) {
                errMsg = "似乎已断开与互联网的连接。";
            } else {
                errMsg = "It seems to have disconnected from the internet.";
            }
            getView().toastShort(errMsg);
            getView().dismissBleConfigDialog();
        }
    }

    private String handleAlarmReason(DeviceInfo deviceInfo) {
        StringBuilder sb = new StringBuilder(mContext.getString(R.string.device_is_alarm));
        DeviceTypeStyles configDeviceType = PreferencesHelper.getInstance().getConfigDeviceType(deviceInfo.getDeviceType());
        if (configDeviceType == null) {
            return sb.toString();
        }
        Map<String, SensorStruct> sensoroDetails = deviceInfo.getSensoroDetails();
        if (sensoroDetails != null && sensoroDetails.size() > 0) {
            ArrayList<String> sensoroTypes = new ArrayList<>(sensoroDetails.keySet());
            Collections.sort(sensoroTypes);
            sb.append(mContext.getString(R.string.reason)).append("：");
            for (String sensoroType : sensoroTypes) {
                MonitoringPointRcContentAdapterModel model = MonitorPointModelsFactory.createMonitoringPointRcContentAdapterModel(mContext, deviceInfo, sensoroDetails, sensoroType);
                if (model != null && model.hasAlarmStatus()) {
                    SensorTypeStyles sensorTypeStyles = PreferencesHelper.getInstance().getConfigSensorType(sensoroType);
                    if (sensorTypeStyles != null && sensorTypeStyles.isBool()) {
                        sb.append(model.content);
                    } else {
                        sb.append(model.name).append(" ").append(model.content);
                    }
                    if (!TextUtils.isEmpty(model.unit)) {
                        sb.append(model.unit);
                    }
                    sb.append("、");
                }
            }
            String s = sb.toString();
            if (s.endsWith("、")) {
                s = s.substring(0, s.lastIndexOf("、"));
            }
            s += "，";
            return s;
        } else {
            return sb.toString();
        }
    }

    private String handleMalfunctionReason(DeviceInfo deviceInfo) {
        ArrayList<String> malfunctionBeanData = new ArrayList<>();
        Map<String, MalfunctionDataBean> malfunctionData = deviceInfo.getMalfunctionData();
        //TODO 添加故障字段数组
        if (malfunctionData != null) {
            LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
            Set<Map.Entry<String, MalfunctionDataBean>> entrySet = malfunctionData.entrySet();
            for (Map.Entry<String, MalfunctionDataBean> entry : entrySet) {
                MalfunctionDataBean entryValue = entry.getValue();
                if (entryValue != null) {
                    Map<String, MalfunctionDataBean> details = entryValue.getDetails();
                    if (details != null) {
                        Set<String> keySet = details.keySet();
                        linkedHashSet.addAll(keySet);
                    }
                }
            }
            ArrayList<String> keyList = new ArrayList<>(linkedHashSet);
            Collections.sort(keyList);
            for (String key : keyList) {
                MalfunctionTypeStyles configMalfunctionSubTypes = PreferencesHelper.getInstance().getConfigMalfunctionSubTypes(key);
                if (configMalfunctionSubTypes != null) {
                    malfunctionBeanData.add(configMalfunctionSubTypes.getName());
                }

            }
        }
        StringBuilder sb = new StringBuilder(mContext.getString(R.string.device_is_malfunction));
        if (malfunctionBeanData.size() > 0) {
            sb.append(mContext.getString(R.string.reason)).append("：");
            for (int i = 0; i < malfunctionBeanData.size(); i++) {
                if (i == malfunctionBeanData.size() - 1) {
                    sb.append(malfunctionBeanData.get(i)).append("，");
                } else {
                    sb.append(malfunctionBeanData.get(i)).append("、");
                }
            }
        }
        return sb.toString();
    }

    public void doForceUpload() {
        deployAnalyzerModel.forceReason = tempForceReason;
        deployAnalyzerModel.currentStatus = tempStatus;
        doUploadImages();

    }

    private boolean hasSignalConfig() {
        return (PreferencesHelper.getInstance().getUserData().hasSignalConfig && deployAnalyzerModel.deployType != Constants.TYPE_SCAN_DEPLOY_STATION && deployAnalyzerModel.whiteListDeployType != Constants.TYPE_SCAN_DEPLOY_WHITE_LIST)
                || CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType);
    }
}
