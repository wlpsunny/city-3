package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.activity.AlarmHistoryLogActivity;
import com.sensoro.smartcity.activity.MonitorMoreActivity;
import com.sensoro.smartcity.activity.MonitorPointDetailActivity;
import com.sensoro.smartcity.activity.MonitorPointMapActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.IMonitorPointDetailActivityView;
import com.sensoro.smartcity.iwidget.IOnStart;
import com.sensoro.smartcity.model.PushData;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.DeviceInfo;
import com.sensoro.smartcity.server.bean.DeviceMergeTypesInfo;
import com.sensoro.smartcity.server.bean.DeviceRecentInfo;
import com.sensoro.smartcity.server.bean.SensorStruct;
import com.sensoro.smartcity.server.response.DeviceInfoListRsp;
import com.sensoro.smartcity.server.response.DeviceRecentRsp;
import com.sensoro.smartcity.server.response.ResponseBase;
import com.sensoro.smartcity.util.AppUtils;
import com.sensoro.smartcity.util.DateUtil;
import com.sensoro.smartcity.util.LogUtils;
import com.sensoro.smartcity.util.PreferencesHelper;
import com.sensoro.smartcity.util.WidgetUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MonitorPointDetailActivityPresenter extends BasePresenter<IMonitorPointDetailActivityView> implements IOnStart, Constants, GeocodeSearch.OnGeocodeSearchListener {
    private Activity mContext;
    private DeviceInfo mDeviceInfo;

    private final List<DeviceRecentInfo> mRecentInfoList = new ArrayList<>();
    private int textColor;
    private String content;
    private boolean hasPhoneNumber;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        mDeviceInfo = (DeviceInfo) mContext.getIntent().getSerializableExtra(EXTRA_DEVICE_INFO);
        initCurrentDeviceInfo();
        requestDeviceRecentLog();
    }

    private void freshTopData() {
        switch (mDeviceInfo.getStatus()) {
            case SENSOR_STATUS_ALARM:
                textColor = mContext.getResources().getColor(R.color.c_f34a4a);
                break;
            case SENSOR_STATUS_NORMAL:
                textColor = mContext.getResources().getColor(R.color.c_29c093);
                break;
            case SENSOR_STATUS_LOST:
                textColor = mContext.getResources().getColor(R.color.c_5d5d5d);
                break;
            case SENSOR_STATUS_INACTIVE:
                textColor = mContext.getResources().getColor(R.color.c_b6b6b6);
                break;
            default:
                break;
        }
        String name = mDeviceInfo.getName();
        String sn = mDeviceInfo.getSn();
        //
        getView().setStatusInfo(MONITOR_STATUS_ARRAY[mDeviceInfo.getStatus()], textColor);
        //TODO 显示sn还是姓名等
        getView().setTitleNameTextView(TextUtils.isEmpty(name) ? sn : name);
        //
        String contact = null;
        String phone = null;
        try {
            contact = mDeviceInfo.getAlarms().getNotification().getContact();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            phone = mDeviceInfo.getAlarms().getNotification().getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(contact) && TextUtils.isEmpty(phone)) {
            getView().setNoContact();
            hasPhoneNumber = false;
        } else {
            if (TextUtils.isEmpty(contact)) {
                contact = "未设定";
            }
            hasPhoneNumber = !TextUtils.isEmpty(phone);
            getView().setContactPhoneIconVisible(hasPhoneNumber);
            if (hasPhoneNumber) {
                this.content = phone;
            } else {
                this.content = "未设定";
            }
            getView().setContractName(contact);
            getView().setContractPhone(content);
        }
        getView().setUpdateTime(DateUtil.getStrTimeToday(mDeviceInfo.getUpdatedTime(), 0));
        String tags[] = mDeviceInfo.getTags();
        if (tags != null && tags.length > 0) {
            List<String> list = Arrays.asList(tags);
            getView().updateTags(list);

        }
        SensorStruct batteryStruct = mDeviceInfo.getSensoroDetails().get("battery");
        if (batteryStruct != null) {
            String battery = batteryStruct.getValue().toString();
            if (battery.equals("-1.0") || battery.equals("-1")) {
                getView().setBatteryInfo("电源供电");
            } else {
                getView().setBatteryInfo(WidgetUtil.subZeroAndDot(battery) + "%");
            }
        }
        int interval = mDeviceInfo.getInterval();
        getView().setInterval(DateUtil.secToTimeBefore(interval));
    }

    private void initCurrentDeviceInfo() {
        getView().setSNText(mDeviceInfo.getSn());
        String typeName = "未知";
        try {
            DeviceMergeTypesInfo.DeviceMergeTypeConfig localDevicesMergeTypes = PreferencesHelper.getInstance().getLocalDevicesMergeTypes().getConfig();
            typeName = localDevicesMergeTypes.getMergeType().get(mDeviceInfo.getDeviceType()).getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getView().setDeviceTypeName(typeName);
        GeocodeSearch geocoderSearch = new GeocodeSearch(mContext);
        geocoderSearch.setOnGeocodeSearchListener(this);
        double[] lonlat = mDeviceInfo.getLonlat();
        try {
            double v = lonlat[1];
            double v1 = lonlat[0];
            if (v == 0 || v1 == 0) {
                getView().setDeviceLocation("未定位", false);
                getView().setDeviceLocationTextColor(R.color.c_a6a6a6);
                return;
            }
            RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(v, v1), 200, GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);
        } catch (Exception e) {
            e.printStackTrace();
            getView().setDeviceLocation("未定位", false);

        }

    }

    private void requestDeviceRecentLog() {
        long endTime = mDeviceInfo.getUpdatedTime();
        long startTime = endTime - 2 * 1000 * 60 * 60 * 24;
        String sn = mDeviceInfo.getSn();
        getView().showProgressDialog();
        //合并请求
        Observable<DeviceInfoListRsp> deviceDetailInfoList = RetrofitServiceHelper.INSTANCE.getDeviceDetailInfoList
                (sn, null, 1);
        Observable<DeviceRecentRsp> deviceHistoryList = RetrofitServiceHelper.INSTANCE.getDeviceHistoryList(sn,
                startTime, endTime);
        Observable.merge(deviceDetailInfoList, deviceHistoryList).subscribeOn(Schedulers.io()).doOnNext(new Action1<ResponseBase>() {


            @Override
            public void call(ResponseBase responseBase) {
                if (responseBase instanceof DeviceInfoListRsp) {
                    DeviceInfoListRsp response = (DeviceInfoListRsp) responseBase;
                    if (response.getData().size() > 0) {
                        DeviceInfo deviceInfo = response.getData().get(0);
                        String[] tags = deviceInfo.getTags();
                        mDeviceInfo.setTags(tags);
                    }
                } else if (responseBase instanceof DeviceRecentRsp) {
                    DeviceRecentRsp response = ((DeviceRecentRsp) responseBase);
                    String data = response.getData().toString();
                    try {
//                        String[] sensorTypes = response.getSensorTypes();
                        JSONObject jsonObject = new JSONObject(data);
//                        sensorTypesList = SortUtils.sortSensorTypes(sensorTypes);
                        Iterator<String> iterator = jsonObject.keys();
                        while (iterator.hasNext()) {
                            String keyStr = iterator.next();
                            if (!TextUtils.isEmpty(keyStr)) {
                                JSONObject object = jsonObject.getJSONObject(keyStr);
                                if (object != null) {
                                    DeviceRecentInfo recentInfo = RetrofitServiceHelper.INSTANCE.getGson().fromJson
                                            (object.toString(),
                                                    DeviceRecentInfo.class);
                                    recentInfo.setDate(keyStr);
                                    mRecentInfoList.add(recentInfo);
                                }

                            }
                        }
                        Collections.sort(mRecentInfoList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).observeOn
                (AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseBase>(this) {

//            @Override
//            public void onNext(ResponseBase responseBase) {
//                getView().setMapLayoutVisible(true);
//                getView().setMapViewVisible(true);
//                refreshBatteryLayout();
//                refreshKLayout();

//            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);
            }

            @Override
            public void onCompleted(ResponseBase responseBase) {
                freshTopData();
                getView().updateDeviceInfoAdapter(mDeviceInfo);
                getView().dismissProgressDialog();
            }
        });
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(PushData data) {
        if (data != null) {
            List<DeviceInfo> deviceInfoList = data.getDeviceInfoList();
            String sn = mDeviceInfo.getSn();
            for (DeviceInfo deviceInfo : deviceInfoList) {
                if (sn.equals(deviceInfo.getSn())) {
                    mDeviceInfo = deviceInfo;
                    break;
                }
            }
            if (mDeviceInfo != null && AppUtils.isActivityTop(mContext, MonitorPointDetailActivity.class)) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        freshTopData();
                        getView().updateDeviceInfoAdapter(mDeviceInfo);
//                        freshStructData();
//                        freshMarker();
                    }
                });


            }
        }
    }

    @Override
    public void onDestroy() {
        mRecentInfoList.clear();
//        if (tempUpBitmap != null) {
//            tempUpBitmap.recycle();
//            tempUpBitmap = null;
//        }
//        if (sensorTypesList != null) {
//            sensorTypesList.clear();
//            sensorTypesList = null;
//        }
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        String address = regeocodeResult.getRegeocodeAddress().getFormatAddress();
        LogUtils.loge(this, "onRegeocodeSearched: " + "code = " + i + ",address = " + address);
        if (TextUtils.isEmpty(address)) {
            address = "未知街道";
        }
        mDeviceInfo.setAddress(address);
        getView().setDeviceLocation(address, true);
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        LogUtils.loge(this, "onGeocodeSearched: " + "onGeocodeSearched");
        getView().setDeviceLocation("未知街道", true);
    }

    public void doMore() {
        Intent intent = new Intent(mContext, MonitorMoreActivity.class);
        intent.putExtra(EXTRA_SENSOR_SN, mDeviceInfo.getSn());
        getView().startAC(intent);
    }

    public void doNavigation() {
        double[] lonlat = mDeviceInfo.getLonlat();

        double v = lonlat[1];
        double v1 = lonlat[0];
        if (lonlat.length > 1 && v == 0 || v1 == 0) {
            getView().toastShort("未设置定位信息");
            return;
        }
        Intent intent = new Intent();
        intent.setClass(mContext, MonitorPointMapActivity.class);
        intent.putExtra(EXTRA_DEVICE_INFO, mDeviceInfo);
        getView().startAC(intent);
    }

    public void doContact() {
        if (hasPhoneNumber) {
            if (TextUtils.isEmpty(content) || "未设定".equals(content)) {
                getView().toastShort("未设置电话联系人");
                return;
            }
            AppUtils.diallPhone(content, mContext);
        }

    }

    public void doMonitorHistory() {
        String sn = mDeviceInfo.getSn();
        Intent intent = new Intent(mContext, AlarmHistoryLogActivity.class);
        intent.putExtra(EXTRA_SENSOR_SN, sn);
        getView().startAC(intent);
    }
}
