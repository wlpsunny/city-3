package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.amap.api.maps.model.LatLng;
import com.sensoro.common.iwidget.IOnCreate;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.activity.AlarmCameraLiveDetailActivity;
import com.sensoro.smartcity.activity.AlarmCameraVideoDetailActivity;
import com.sensoro.smartcity.activity.AlarmHistoryLogActivity;
import com.sensoro.smartcity.activity.VideoPlayActivity;
import com.sensoro.smartcity.analyzer.AlarmPopupConfigAnalyzer;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.IAlarmDetailLogActivityView;
import com.sensoro.smartcity.model.AlarmPopupModel;
import com.sensoro.smartcity.model.EventAlarmStatusModel;
import com.sensoro.smartcity.model.EventData;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.AlarmInfo;
import com.sensoro.smartcity.server.bean.DeviceAlarmLogInfo;
import com.sensoro.smartcity.server.bean.ScenesData;
import com.sensoro.smartcity.server.response.AlarmCloudVideoRsp;
import com.sensoro.smartcity.server.response.AlarmCountRsp;
import com.sensoro.smartcity.server.response.DeviceAlarmItemRsp;
import com.sensoro.smartcity.server.response.DevicesAlarmPopupConfigRsp;
import com.sensoro.smartcity.server.response.ResponseBase;
import com.sensoro.smartcity.util.AppUtils;
import com.sensoro.smartcity.util.DateUtil;
import com.sensoro.smartcity.util.PreferencesHelper;
import com.sensoro.smartcity.util.WidgetUtil;
import com.sensoro.smartcity.widget.imagepicker.ImagePicker;
import com.sensoro.smartcity.widget.imagepicker.bean.ImageItem;
import com.sensoro.smartcity.widget.imagepicker.ui.ImageAlarmPhotoDetailActivity;
import com.sensoro.smartcity.widget.popup.AlarmPopUtilsTest;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AlarmDetailLogActivityPresenter extends BasePresenter<IAlarmDetailLogActivityView> implements Constants, IOnCreate, AlarmPopUtilsTest.OnPopupCallbackListener {
    private final List<AlarmInfo.RecordInfo> mList = new ArrayList<>();
    private DeviceAlarmLogInfo deviceAlarmLogInfo;
    private boolean isReConfirm = false;
    private Activity mContext;
    private LatLng destPosition = null;
    private AlarmCloudVideoRsp.DataBean mVideoBean;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        onCreate();
        deviceAlarmLogInfo = (DeviceAlarmLogInfo) mContext.getIntent().getSerializableExtra(EXTRA_ALARM_INFO);

        getAlarmCount();

        getCloudVideo();

        refreshData(true);

    }

    private void getCloudVideo() {

        if (!PreferencesHelper.getInstance().getUserData().hasDeviceCameraList) {
            getView().setLlVideoSize(-1);
            return;
        }

        String[] eventIds = {deviceAlarmLogInfo.get_id()};
        RetrofitServiceHelper.getInstance().getCloudVideo(eventIds)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CityObserver<AlarmCloudVideoRsp>(this) {
                    @Override
                    public void onCompleted(AlarmCloudVideoRsp response) {
                        List<AlarmCloudVideoRsp.DataBean> data = response.getData();
                        if (data != null && data.size() > 0) {
                            mVideoBean = data.get(0);
                            List<AlarmCloudVideoRsp.DataBean.MediasBean> mMedias = mVideoBean.getMedias();
                            if (mMedias != null && mMedias.size() > 0) {
                                getView().setLlVideoSize(mMedias.size());
                            } else {
                                getView().setLlVideoSize(-1);
                            }
                        } else {
                            getView().setLlVideoSize(-1);
                        }
                    }

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().setLlVideoSize(-1);
                    }
                });
    }

    public void doBack() {
        EventData eventData = new EventData();
        eventData.code = EVENT_DATA_ALARM_DETAIL_RESULT;
        eventData.data = deviceAlarmLogInfo;
        EventBus.getDefault().post(eventData);
        if (isAttachedView()) {
            getView().finishAc();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData eventData) {
        int code = eventData.code;
        Object data = eventData.data;
        //
        switch (code) {
            case EVENT_DATA_ALARM_FRESH_ALARM_DATA:
                if (data instanceof DeviceAlarmLogInfo) {
                    if (this.deviceAlarmLogInfo.get_id().equals(((DeviceAlarmLogInfo) data).get_id())) {
                        this.deviceAlarmLogInfo = (DeviceAlarmLogInfo) data;
                        refreshData(false);
                    }

                }
                break;
            case EVENT_DATA_ALARM_SOCKET_DISPLAY_STATUS:
                if (data instanceof EventAlarmStatusModel) {
                    EventAlarmStatusModel tempEventAlarmStatusModel = (EventAlarmStatusModel) data;
                    if (deviceAlarmLogInfo.get_id().equals(tempEventAlarmStatusModel.deviceAlarmLogInfo.get_id())) {
                        switch (tempEventAlarmStatusModel.status) {
                            case MODEL_ALARM_STATUS_EVENT_CODE_RECOVERY:
                                // 做一些预警恢复的逻辑
                            case MODEL_ALARM_STATUS_EVENT_CODE_CONFIRM:
                                // 做一些预警被确认的逻辑
                            case MODEL_ALARM_STATUS_EVENT_CODE_RECONFIRM:
                                // 做一些预警被再次确认的逻辑
                                deviceAlarmLogInfo = tempEventAlarmStatusModel.deviceAlarmLogInfo;
                                refreshData(false);
                                break;
                            default:
                                // 未知逻辑 可以联系我确认 有可能是bug
                                break;
                        }
                    }
                }
                break;
        }
    }

    public void refreshData(boolean isInit) {
        //
        String deviceName = deviceAlarmLogInfo.getDeviceName();
        if (isAttachedView()) {
            getView().setDeviceNameTextView(TextUtils.isEmpty(deviceName) ? deviceAlarmLogInfo.getDeviceSN() : deviceName);
        }
        String deviceSN = deviceAlarmLogInfo.getDeviceSN();
        if (TextUtils.isEmpty(deviceSN)) {
            deviceSN = mContext.getString(R.string.device_number) + mContext.getString(R.string.unknown);
        } else {
            deviceSN = mContext.getString(R.string.device_number) + deviceSN;
        }
        if (isAttachedView()) {
            getView().setDeviceSn(deviceSN);
        }

        if (PreferencesHelper.getInstance().getUserData().hasDeviceCameraList) {
            getView().setCameraLiveCount(deviceAlarmLogInfo.getCameras());
        } else {
            getView().setCameraLiveCount(null);
        }

        long createdTime = deviceAlarmLogInfo.getCreatedTime();
        String alarmTime = DateUtil.getStrTimeToday(mContext, createdTime, 1);
        if (isInit) {
            if (isAttachedView()) {
                getView().showProgressDialog();
            }
        }
//        getView().setDisplayStatus(deviceAlarmLogInfo.getDisplayStatus());
//        getView().setSensoroIv(deviceAlarmLogInfo.getSensorType());
        AlarmInfo.RecordInfo[] recordInfoArray = deviceAlarmLogInfo.getRecords();
        if (recordInfoArray != null) {
            mList.clear();
            for (int i = recordInfoArray.length - 1; i >= 0; i--) {
                mList.add(recordInfoArray[i]);
            }
            if (isAttachedView()) {
                getView().updateAlertLogContentAdapter(mList);
            }
            //
            switch (deviceAlarmLogInfo.getDisplayStatus()) {
                case DISPLAY_STATUS_CONFIRM:
                    isReConfirm = false;
                    if (isAttachedView()) {
                        getView().setConfirmColor(mContext.getResources().getColor(R.color.white));
                        getView().setConfirmBg(R.drawable.shape_btn_corner_29c_bg_4dp);
                        getView().setConfirmText(mContext.getString(R.string.alarm_log_alarm_warn_confirm));
                    }
                    break;
                case DISPLAY_STATUS_ALARM:
                case DISPLAY_STATUS_MIS_DESCRIPTION:
                case DISPLAY_STATUS_TEST:
                case DISPLAY_STATUS_RISKS:
                    isReConfirm = true;
                    if (isAttachedView()) {
                        getView().setConfirmColor(mContext.getResources().getColor(R.color.c_252525));
                        getView().setConfirmBg(R.drawable.shape_bg_solid_fa_stroke_df_corner_4dp);
                        getView().setConfirmText(mContext.getString(R.string.confirming_again));
                    }
                    break;
            }
            for (AlarmInfo.RecordInfo recordInfo : recordInfoArray) {
                if (recordInfo.getType().equals("recovery")) {
                    if (isAttachedView()) {
                        getView().setCurrentAlarmState(0, alarmTime);
                    }
                    return;
                }
            }
            if (isAttachedView()) {
                getView().setCurrentAlarmState(1, alarmTime);
            }
        }


    }

    private void getAlarmCount() {
        long current = System.currentTimeMillis();
        RetrofitServiceHelper.getInstance().getAlarmCount(current - 3600 * 24 * 180 * 1000L, current,
                null, deviceAlarmLogInfo.getDeviceSN())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CityObserver<AlarmCountRsp>(this) {
                    @Override
                    public void onCompleted(AlarmCountRsp alarmCountRsp) {
                        int count = alarmCountRsp.getCount();
                        if (isAttachedView()) {
                            getView().setAlarmCount(count + "");
                            getView().dismissProgressDialog();
                        }
                    }

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        if (isAttachedView()) {
                            getView().dismissProgressDialog();
                            getView().toastShort(errorMsg);
                        }
                    }
                });
    }

    public void clickPhotoItem(int position, List<ScenesData> scenesDataList) {
        //
        ArrayList<ImageItem> items = new ArrayList<>();
        if (scenesDataList != null && scenesDataList.size() > 0) {
            for (ScenesData scenesData : scenesDataList) {
                ImageItem imageItem = new ImageItem();
                imageItem.fromUrl = true;
                if ("video".equals(scenesData.type)) {
                    imageItem.isRecord = true;
                    imageItem.thumbPath = scenesData.thumbUrl;
                    imageItem.path = scenesData.url;
                } else {
                    imageItem.path = scenesData.url;
                    imageItem.isRecord = false;
                }
                items.add(imageItem);
            }
            ImageItem imageItem = items.get(position);
            if (imageItem.isRecord) {
                Intent intent = new Intent();
                intent.setClass(mContext, VideoPlayActivity.class);
                intent.putExtra("path_record", (Serializable) imageItem);
                intent.putExtra("video_del", true);
                if (isAttachedView()) {
                    getView().startAC(intent);
                }
            } else {
                //
                Intent intentPreview = new Intent(mContext, ImageAlarmPhotoDetailActivity.class);
                intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, items);
                intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
                intentPreview.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
                if (isAttachedView()) {
                    getView().startAC(intentPreview);
                }
            }

        }

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        mList.clear();
    }

    public void doContactOwner() {
        String tempNumber = deviceAlarmLogInfo.getDeviceNotification().getContent();

        if (TextUtils.isEmpty(tempNumber)) {
            if (isAttachedView()) {
                getView().toastShort(mContext.getString(R.string.no_find_contact_phone_number));
            }
        } else {
            AppUtils.diallPhone(tempNumber, mContext);
        }
    }

    public void doNavigation() {
        double[] deviceLonlat = deviceAlarmLogInfo.getDeviceLonlat();
        if (deviceLonlat != null && deviceLonlat.length > 1) {
            destPosition = new LatLng(deviceLonlat[1], deviceLonlat[0]);
            if (AppUtils.doNavigation(mContext, destPosition)) {
                return;
            } else {
                if (isAttachedView()) {
                    getView().toastShort(mContext.getString(R.string.location_not_obtained));
                }
            }
        } else {
            if (isAttachedView()) {
                getView().toastShort(mContext.getString(R.string.location_not_obtained));
            }
        }

    }


    public void doAlarmHistory() {
        Intent intent = new Intent(mContext, AlarmHistoryLogActivity.class);
        intent.putExtra(EXTRA_SENSOR_SN, deviceAlarmLogInfo.getDeviceSN());
        if (isAttachedView()) {
            getView().startAC(intent);
        }
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPopupCallback(AlarmPopupModel alarmPopupModel, List<ScenesData> scenesDataList) {
        if (isAttachedView()) {
            getView().setUpdateButtonClickable(false);
            getView().showProgressDialog();
        }
        Map<String, Integer> alarmPopupServerData = AlarmPopupConfigAnalyzer.createAlarmPopupServerData(alarmPopupModel);
        RetrofitServiceHelper.getInstance().doUpdatePhotosUrl(deviceAlarmLogInfo.get_id(), alarmPopupServerData, alarmPopupModel.securityRisksList,
                alarmPopupModel.mRemark, isReConfirm, scenesDataList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CityObserver<DeviceAlarmItemRsp>(this) {

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        if (isAttachedView()) {
                            getView().dismissProgressDialog();
                            getView().toastShort(errorMsg);
                            getView().setUpdateButtonClickable(true);
                        }
                    }

                    @Override
                    public void onCompleted(DeviceAlarmItemRsp deviceAlarmItemRsp) {
                        if (deviceAlarmItemRsp.getErrcode() == ResponseBase.CODE_SUCCESS) {
                            if (isAttachedView()) {
                                getView().toastShort(mContext.getResources().getString(R.string
                                        .tips_commit_success));
                            }
                            deviceAlarmLogInfo = deviceAlarmItemRsp.getData();
                            refreshData(false);
                        } else {
                            if (isAttachedView()) {
                                getView().toastShort(mContext.getResources().getString(R.string
                                        .tips_commit_failed));
                            }
                        }
                        if (isAttachedView()) {
                            getView().dismissProgressDialog();
                            getView().dismissAlarmPopupView();
                        }
                    }
                });
    }

    public void showAlarmPopupView() {
        if (PreferencesHelper.getInstance().getAlarmPopupDataBeanCache() == null) {
            RetrofitServiceHelper.getInstance().getDevicesAlarmPopupConfig().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DevicesAlarmPopupConfigRsp>(this) {
                @Override
                public void onCompleted(DevicesAlarmPopupConfigRsp devicesAlarmPopupConfigRsp) {
                    PreferencesHelper.getInstance().saveAlarmPopupDataBeanCache(devicesAlarmPopupConfigRsp.getData());
                    final AlarmPopupModel alarmPopupModel = new AlarmPopupModel();
                    String deviceName = deviceAlarmLogInfo.getDeviceName();
                    if (TextUtils.isEmpty(deviceName)) {
                        alarmPopupModel.title = deviceAlarmLogInfo.getDeviceSN();
                    } else {
                        alarmPopupModel.title = deviceName;
                    }
                    alarmPopupModel.alarmStatus = deviceAlarmLogInfo.getAlarmStatus();
                    alarmPopupModel.updateTime = deviceAlarmLogInfo.getUpdatedTime();
                    alarmPopupModel.mergeType = WidgetUtil.handleMergeType(deviceAlarmLogInfo.getDeviceType());
                    alarmPopupModel.sensorType = deviceAlarmLogInfo.getSensorType();
                    //
                    AlarmPopupConfigAnalyzer.handleAlarmPopupModel(null, alarmPopupModel);
                    getView().showAlarmPopupView(alarmPopupModel);
                    getView().dismissProgressDialog();

                }

                @Override
                public void onErrorMsg(int errorCode, String errorMsg) {
                    getView().toastShort(errorMsg);
                    getView().dismissProgressDialog();
                }
            });
        } else {
            final AlarmPopupModel alarmPopupModel = new AlarmPopupModel();
            String deviceName = deviceAlarmLogInfo.getDeviceName();
            if (TextUtils.isEmpty(deviceName)) {
                alarmPopupModel.title = deviceAlarmLogInfo.getDeviceSN();
            } else {
                alarmPopupModel.title = deviceName;
            }
            alarmPopupModel.alarmStatus = deviceAlarmLogInfo.getAlarmStatus();
            alarmPopupModel.updateTime = deviceAlarmLogInfo.getUpdatedTime();
            alarmPopupModel.mergeType = WidgetUtil.handleMergeType(deviceAlarmLogInfo.getDeviceType());
            alarmPopupModel.sensorType = deviceAlarmLogInfo.getSensorType();
            //
            AlarmPopupConfigAnalyzer.handleAlarmPopupModel(null, alarmPopupModel);
            getView().showAlarmPopupView(alarmPopupModel);
        }
    }


    public void doCameraVideo() {
        Intent intent = new Intent(mContext, AlarmCameraVideoDetailActivity.class);
        intent.putExtra(Constants.EXTRA_ALARM_CAMERA_VIDEO, mVideoBean);
        getView().startAC(intent);
    }

    public void doCameraLive() {
        Intent intent = new Intent(mContext, AlarmCameraLiveDetailActivity.class);
        ArrayList<String> cameras = new ArrayList<>(deviceAlarmLogInfo.getCameras());
        intent.putExtra(Constants.EXTRA_ALARM_CAMERAS, cameras);
        getView().startAC(intent);
    }
}
