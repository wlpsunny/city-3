package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.lzy.imagepicker.bean.ImageItem;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.activity.DeployMonitorAlarmContactActivity;
import com.sensoro.smartcity.activity.DeployDeviceTagActivity;
import com.sensoro.smartcity.activity.DeployMapActivity;
import com.sensoro.smartcity.activity.DeployMonitorNameAddressActivity;
import com.sensoro.smartcity.activity.DeployMonitorSettingPhotoActivity;
import com.sensoro.smartcity.activity.DeployResultActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.IDeployMonitorDetailActivityView;
import com.sensoro.smartcity.iwidget.IOnCreate;
import com.sensoro.smartcity.model.DeployContactModel;
import com.sensoro.smartcity.model.DeployMapModel;
import com.sensoro.smartcity.model.EventData;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.AlarmInfo;
import com.sensoro.smartcity.server.bean.DeviceInfo;
import com.sensoro.smartcity.server.bean.ScenesData;
import com.sensoro.smartcity.server.response.DeviceDeployRsp;
import com.sensoro.smartcity.server.response.DeviceInfoListRsp;
import com.sensoro.smartcity.server.response.ResponseBase;
import com.sensoro.smartcity.server.response.StationInfo;
import com.sensoro.smartcity.server.response.StationInfoRsp;
import com.sensoro.smartcity.util.LogUtils;
import com.sensoro.smartcity.util.RegexUtils;
import com.sensoro.smartcity.widget.popup.UpLoadPhotosUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DeployMonitorDetailActivityPresenter extends BasePresenter<IDeployMonitorDetailActivityView> implements IOnCreate, Constants, Runnable {
    private Activity mContext;
    private DeployMapModel deployMapModel = new DeployMapModel();
    private final List<String> tagList = new ArrayList<>();
    private final List<DeployContactModel> deployContactModelList = new ArrayList<>();
    private Handler mHandler;
    private DeviceInfo deviceInfo;
    private final ArrayList<ImageItem> images = new ArrayList<>();
    private UpLoadPhotosUtils upLoadPhotosUtils;
    private String mNameAndAddress;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        Intent intent = mContext.getIntent();
        deviceInfo = (DeviceInfo) intent.getSerializableExtra(EXTRA_DEVICE_INFO);
        deployMapModel.hasStation = intent.getBooleanExtra(EXTRA_IS_STATION_DEPLOY, false);
        if (intent.getBooleanExtra(EXTRA_IS_CHANGE_DEVICE,false)) {
            getView().updateUploadTvText("更换设备");
        }
        getView().setDeployContactRelativeLayoutVisible(!deployMapModel.hasStation);
        getView().setDeployDeviceRlSignalVisible(!deployMapModel.hasStation);
        getView().setDeployPhotoVisible(!deployMapModel.hasStation);
        init();
    }

    private void init() {
        if (deviceInfo == null) {
            Intent intent = new Intent();
            intent.setClass(mContext, DeployResultActivity.class);
            intent.putExtra(EXTRA_IS_STATION_DEPLOY, deployMapModel.hasStation);
            intent.putExtra(EXTRA_SENSOR_RESULT, -1);
            getView().startAC(intent);
        } else {
            deployMapModel.sn = deviceInfo.getSn();
            getView().setDeviceTitleName(deployMapModel.sn);
            mNameAndAddress = deviceInfo.getName();
            if (!TextUtils.isEmpty(mNameAndAddress)) {
//                不为空设置地址
                getView().setNameAddressText(mNameAndAddress);
//                name = mContext.getResources().getString(R.string.tips_hint_name_address_set);
            }
            if (!deployMapModel.hasStation) {
                if (deviceInfo.getAlarms() != null) {
                    AlarmInfo alarmInfo = deviceInfo.getAlarms();
                    AlarmInfo.NotificationInfo notification = alarmInfo.getNotification();
                    if (notification != null) {
                        //TODO 设置多个联系人
                        String contact = notification.getContact();
                        String content = notification.getContent();
                        if (TextUtils.isEmpty(contact) || TextUtils.isEmpty(content)) {
//                        getView().setContactEditText(mContext.getResources().getString(R.string.tips_hint_contact));
                        } else {
                            deployContactModelList.clear();
                            DeployContactModel deployContactModel = new DeployContactModel();
                            deployContactModel.name = contact;
                            deployContactModel.phone = content;
                            deployContactModelList.add(deployContactModel);
                            getView().updateContactData(deployContactModelList);
                        }

                    }
                }
                freshSignalInfo();
                deployMapModel.signal = deviceInfo.getSignal();
                deployMapModel.updatedTime = deviceInfo.getUpdatedTime();
                mHandler.post(this);
            }
            String tags[] = deviceInfo.getTags();
            if (tags != null) {
                for (String tag : tags) {
                    if (!TextUtils.isEmpty(tag)) {
                        tagList.add(tag);
                    }
                }
                getView().updateTagsData(tagList);
            }
            //
            getView().updateUploadState(true);
        }
    }

    //
    public void requestUpload() {
        final double lon = deployMapModel.latLng.longitude;
        final double lan = deployMapModel.latLng.latitude;
        if (deployMapModel.hasStation) {
            getView().showProgressDialog();
            RetrofitServiceHelper.INSTANCE.doStationDeploy(deployMapModel.sn, lon, lan, tagList, mNameAndAddress).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CityObserver<StationInfoRsp>(this) {

                        @Override
                        public void onErrorMsg(int errorCode, String errorMsg) {
                            getView().dismissProgressDialog();
                            getView().updateUploadState(true);
                            if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                                getView().toastShort(errorMsg);
                            } else if (errorCode == 4013101 || errorCode == 4000013) {
                                freshError(deployMapModel.sn, null);
                            } else {
                                freshError(deployMapModel.sn, errorMsg);
                            }
                        }

                        @Override
                        public void onCompleted(StationInfoRsp stationInfoRsp) {
                            freshStation(stationInfoRsp);
                            getView().dismissProgressDialog();
                            getView().finishAc();
                        }
                    });
        } else {
            if (images.size() > 0) {
                //TODO 图片提交
                final UpLoadPhotosUtils.UpLoadPhotoListener upLoadPhotoListener = new UpLoadPhotosUtils
                        .UpLoadPhotoListener() {

                    @Override
                    public void onStart() {
                        getView().showStartUploadProgressDialog();
                    }

                    @Override
                    public void onComplete(List<ScenesData> scenesDataList) {
                        ArrayList<String> strings = new ArrayList<>();
                        for (ScenesData scenesData : scenesDataList) {
                            scenesData.type = "image";
                            strings.add(scenesData.url);
                        }
                        getView().dismissUploadProgressDialog();
                        LogUtils.loge(this, "上传成功--- size = " + strings.size());
                        //TODO 上传结果
                        doDeployResult(lon, lan, strings);
                    }

                    @Override
                    public void onError(String errMsg) {
                        getView().updateUploadState(true);
                        getView().dismissUploadProgressDialog();
                        getView().toastShort(errMsg);
                    }

                    @Override
                    public void onProgress(int index, double percent) {
                        getView().showUploadProgressDialog(index, images.size(), percent);
                    }
                };
                upLoadPhotosUtils = new UpLoadPhotosUtils(mContext, upLoadPhotoListener);
                upLoadPhotosUtils.doUploadPhoto(images);
            } else {
                doDeployResult(lon, lan, null);
            }
        }

    }

    private void doDeployResult(double lon, double lan, List<String> imgUrls) {
        DeployContactModel deployContactModel = deployContactModelList.get(0);
        getView().showProgressDialog();
        RetrofitServiceHelper.INSTANCE.doDevicePointDeploy(deployMapModel.sn, lon, lan, tagList, mNameAndAddress,
                deployContactModel.name, deployContactModel.phone, imgUrls).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CityObserver<DeviceDeployRsp>(this) {


                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().updateUploadState(true);
                        if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                            getView().toastShort(errorMsg);
                        } else if (errorCode == 4013101 || errorCode == 4000013) {
                            freshError(deployMapModel.sn, null);
                        } else {
                            freshError(deployMapModel.sn, errorMsg);
                        }
                    }

                    @Override
                    public void onCompleted(DeviceDeployRsp deviceDeployRsp) {
                        freshPoint(deviceDeployRsp);
                        getView().dismissProgressDialog();
                        getView().finishAc();
                    }
                });
    }

    private void freshError(String scanSN, String errorInfo) {
        //
        Intent intent = new Intent();
        intent.setClass(mContext, DeployResultActivity.class);
        intent.putExtra(EXTRA_SENSOR_RESULT, -1);
        intent.putExtra(EXTRA_SENSOR_SN_RESULT, scanSN);
        intent.putExtra(EXTRA_IS_STATION_DEPLOY, deployMapModel.hasStation);
        if (!TextUtils.isEmpty(errorInfo)) {
            intent.putExtra(EXTRA_SENSOR_RESULT_ERROR, errorInfo);
        }
        getView().startAC(intent);
    }

    private void freshPoint(DeviceDeployRsp deviceDeployRsp) {
        int errCode = deviceDeployRsp.getErrcode();
        int resultCode = 1;
        if (errCode != ResponseBase.CODE_SUCCESS) {
            resultCode = errCode;
        }
        Intent intent = new Intent(mContext, DeployResultActivity.class);
        intent.putExtra(EXTRA_SENSOR_RESULT, resultCode);
//        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        DeviceInfo data = deviceDeployRsp.getData();
        data.setAddress(deployMapModel.address);
        intent.putExtra(EXTRA_DEVICE_INFO, data);
        //TODO 新版联系人

        if (deployContactModelList.size() > 0) {
            DeployContactModel deployContactModel = deployContactModelList.get(0);
            intent.putExtra(EXTRA_SETTING_CONTACT, deployContactModel.name);
            intent.putExtra(EXTRA_SETTING_CONTENT, deployContactModel.phone);
        }
        intent.putExtra(EXTRA_IS_STATION_DEPLOY, false);
        getView().startAC(intent);
    }

    private void freshStation(StationInfoRsp stationInfoRsp) {
        String s = stationInfoRsp.toString();
        LogUtils.loge(s);
        int errCode = stationInfoRsp.getErrcode();
        int resultCode = 1;
        if (errCode != ResponseBase.CODE_SUCCESS) {
            resultCode = errCode;
        }
        //
        StationInfo stationInfo = stationInfoRsp.getData();
        double[] lonLat = stationInfo.getLonlat();
        String name = stationInfo.getName();
        String sn = stationInfo.getSn();
        String[] tags = stationInfo.getTags();
        int normalStatus = stationInfo.getNormalStatus();
        long updatedTime = stationInfo.getUpdatedTime();
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setSn(sn);
        deviceInfo.setTags(tags);
        deviceInfo.setLonlat(lonLat);
        deviceInfo.setStatus(normalStatus);
        deviceInfo.setAddress(deployMapModel.address);
        deviceInfo.setUpdatedTime(updatedTime);
        if (!TextUtils.isEmpty(name)) {
            deviceInfo.setName(name);
        }
        Intent intent = new Intent(mContext, DeployResultActivity.class);
        intent.putExtra(EXTRA_SENSOR_RESULT, resultCode);
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        intent.putExtra(EXTRA_IS_STATION_DEPLOY, true);
        intent.putExtra(EXTRA_DEVICE_INFO, deviceInfo);
        getView().startAC(intent);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        tagList.clear();
        images.clear();
    }

    public void doNameAddress() {
        Intent intent = new Intent(mContext, DeployMonitorNameAddressActivity.class);
        if (!TextUtils.isEmpty(mNameAndAddress)) {
            intent.putExtra(EXTRA_SETTING_NAME_ADDRESS, mNameAndAddress);
        }
        getView().startAC(intent);
    }

    public void doAlarmContact() {
        Intent intent = new Intent(mContext, DeployMonitorAlarmContactActivity.class);
        if (deployContactModelList.size() > 0) {
            intent.putExtra(EXTRA_SETTING_DEPLOY_CONTACT, (ArrayList<DeployContactModel>) deployContactModelList);
        }
        getView().startAC(intent);
    }

    public void doTag() {
        Intent intent = new Intent(mContext, DeployDeviceTagActivity.class);
        if (tagList.size() > 0) {
            intent.putStringArrayListExtra(EXTRA_SETTING_TAG_LIST, (ArrayList<String>) tagList);
        }
        getView().startAC(intent);
    }

    public void doSettingPhoto() {
        Intent intent = new Intent(mContext, DeployMonitorSettingPhotoActivity.class);
        if (images.size() > 0) {
            intent.putExtra(EXTRA_DEPLOY_TO_PHOTO, images);
        }
        getView().startAC(intent);
    }

    public void doDeployMap() {
        Intent intent = new Intent();
        intent.setClass(mContext, DeployMapActivity.class);
        intent.putExtra(EXTRA_DEPLOY_TO_MAP, deployMapModel);
        getView().startAC(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData eventData) {
        //TODO 可以修改以此种方式传递，方便管理
        int code = eventData.code;
        Object data = eventData.data;
        if (code == EVENT_DATA_DEPLOY_RESULT_FINISH || code == EVENT_DATA_DEPLOY_RESULT_CONTINUE) {
            getView().finishAc();
        } else if (code == EVENT_DATA_DEPLOY_SETTING_NAME_ADDRESS) {
            if (data instanceof String) {
                mNameAndAddress = (String) data;
                getView().setNameAddressText(mNameAndAddress);
            }
        } else if (code == EVENT_DATA_DEPLOY_SETTING_TAG) {
            if (data instanceof List) {
                tagList.clear();
                tagList.addAll((List<String>) data);
                getView().updateTagsData(tagList);
            }
        } else if (code == EVENT_DATA_DEPLOY_SETTING_CONTACT) {
            if (data instanceof List) {
                //TODO 联系人
                deployContactModelList.clear();
                deployContactModelList.addAll((List<DeployContactModel>) data);
                getView().updateContactData(deployContactModelList);
            }
        } else if (code == EVENT_DATA_DEPLOY_SETTING_PHOTO) {
            if (data instanceof List) {
                images.clear();
                images.addAll((ArrayList<ImageItem>) data);
                if (images.size() > 0) {
                    getView().setDeployPhotoText("已选择" + images.size() + "张图片");
                } else {
                    getView().setDeployPhotoText("未添加");
                }
            }
        } else if (code == EVENT_DATA_DEPLOY_MAP) {
            if (data instanceof DeployMapModel) {
                deployMapModel = (DeployMapModel) data;
                freshSignalInfo();
            }
        }
//        LogUtils.loge(this, eventData.toString());
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void run() {
        RetrofitServiceHelper.INSTANCE.getDeviceDetailInfoList(deployMapModel.sn, null, 1).subscribeOn(Schedulers.io()).observeOn
                (AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceInfoListRsp>(this) {


            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
            }

            @Override
            public void onCompleted(DeviceInfoListRsp deviceInfoListRsp) {
                if (deviceInfoListRsp.getData().size() > 0) {
                    DeviceInfo deviceInfo = deviceInfoListRsp.getData().get(0);
                    deployMapModel.signal = deviceInfo.getSignal();
                    deployMapModel.updatedTime = deviceInfo.getUpdatedTime();
                    freshSignalInfo();
                }
            }
        });
        mHandler.postDelayed(this, 5 * 1000);
    }

    public void doConfirm() {
        //TODO 所有逻辑拦击
        //姓名地址校验
        //        例：大悦城20层走廊2号配电箱
        String name_default = mContext.getString(R.string.tips_hint_name_address);
        if (TextUtils.isEmpty(mNameAndAddress) || mNameAndAddress.equals(name_default)) {
            getView().toastShort(mContext.getResources().getString(R.string.tips_input_name));
            getView().updateUploadState(true);
            return;
        } else {
            byte[] bytes = new byte[0];
            try {
                bytes = mNameAndAddress.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (bytes.length > 48) {
                getView().toastShort("名称/地址最长不能超过16个汉字或48个字符");
                getView().updateUploadState(true);
                return;
            }
        }
        //经纬度校验
        if (deployMapModel.latLng == null) {
            getView().toastShort("请选择部署设备位置");
            getView().updateUploadState(true);
        } else {
            //TODO 背景选择器
            if (deployMapModel.hasStation) {
                requestUpload();
            } else {
                //TODO 联系人上传
                //联系人校验
                if (deployContactModelList.size() > 0) {
                    DeployContactModel deployContactModel = deployContactModelList.get(0);
                    if (TextUtils.isEmpty(deployContactModel.name) || TextUtils.isEmpty(deployContactModel.phone)) {
                        getView().toastShort("请输入联系人名称和电话号码");
                        getView().updateUploadState(true);
                        return;
                    }
                    if (!RegexUtils.checkPhone(deployContactModel.phone)) {
                        getView().toastShort(mContext.getResources().getString(R.string.tips_phone_empty));
                        getView().updateUploadState(true);
                        return;
                    }
                } else {
                    getView().toastShort("请输入联系人名称和电话号码");
                    getView().updateUploadState(true);
                    return;
                }
                if (needRefreshSignal()) {
                    getView().showWarnDialog();
                } else {
                    requestUpload();
                }
            }

        }

    }

    private void freshSignalInfo() {
        String signal_text = null;
        long time_diff = System.currentTimeMillis() - deployMapModel.updatedTime;
        int resId = 0;
        if (deployMapModel.signal != null && (time_diff < 300000)) {
            switch (deployMapModel.signal) {
                case "good":
                    signal_text = "信号：优";
                    resId = R.drawable.shape_signal_good;
                    break;
                case "normal":
                    signal_text = "信号：良";
                    resId = R.drawable.shape_signal_normal;
                    break;
                case "bad":
                    signal_text = "信号：差";
                    resId = R.drawable.shape_signal_bad;
                    break;
            }
        } else {
            signal_text = "无信号";
            resId = R.drawable.shape_signal_none;
        }
        if (deployMapModel.latLng == null) {
            getView().refreshSignal(deployMapModel.hasStation, signal_text, resId, "未定位");
        } else {
            getView().refreshSignal(deployMapModel.hasStation, signal_text, resId, "已定位");
        }

    }

    private boolean needRefreshSignal() {
        long time_diff = System.currentTimeMillis() - deployMapModel.updatedTime;
        if (deployMapModel.signal != null && (time_diff < 300000)) {
            switch (deployMapModel.signal) {
                case "good":
                case "normal":
                    return false;
            }
        }
        return true;
    }
}