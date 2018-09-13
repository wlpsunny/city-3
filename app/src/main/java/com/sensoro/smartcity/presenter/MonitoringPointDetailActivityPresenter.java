package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.activity.MonitoringPointDetailActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.IMonitoringPointDetailActivityView;
import com.sensoro.smartcity.iwidget.IOnStart;
import com.sensoro.smartcity.model.PushData;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.DeviceInfo;
import com.sensoro.smartcity.server.bean.DeviceRecentInfo;
import com.sensoro.smartcity.server.response.DeviceInfoListRsp;
import com.sensoro.smartcity.server.response.DeviceRecentRsp;
import com.sensoro.smartcity.server.response.ResponseBase;
import com.sensoro.smartcity.util.AppUtils;
import com.sensoro.smartcity.util.DateUtil;
import com.sensoro.smartcity.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.sensoro.smartcity.util.AppUtils.isAppInstalled;

public class MonitoringPointDetailActivityPresenter extends BasePresenter<IMonitoringPointDetailActivityView> implements IOnStart, Constants, GeocodeSearch.OnGeocodeSearchListener {
    private Activity mContext;
    private DeviceInfo mDeviceInfo;

    private LatLng destPosition = null;
    private final List<DeviceRecentInfo> mRecentInfoList = new ArrayList<>();
    private int textColor;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        mDeviceInfo = (DeviceInfo) mContext.getIntent().getSerializableExtra(EXTRA_DEVICE_INFO);
        freshDestPosition();
        requestDeviceRecentLog();
    }

    private void freshTopData() {
        switch (mDeviceInfo.getStatus()) {
            case SENSOR_STATUS_ALARM:
//                getView().setStatusImageView(R.drawable.shape_status_alarm);
                textColor = mContext.getResources().getColor(R.color.sensoro_alarm);
                break;
            case SENSOR_STATUS_INACTIVE:
//                getView().setStatusImageView(R.drawable.shape_status_inactive);
                textColor = mContext.getResources().getColor(R.color.sensoro_inactive);
                break;
            case SENSOR_STATUS_LOST:
//                getView().setStatusImageView(R.drawable.shape_status_lost);
                textColor = mContext.getResources().getColor(R.color.sensoro_lost);
                break;
            case SENSOR_STATUS_NORMAL:
//                getView().setStatusImageView(R.drawable.shape_status_normal);
                textColor = mContext.getResources().getColor(R.color.sensoro_normal);
                break;
            default:
                break;
        }
        getView().setAlarmStateColor(textColor);
        String name = mDeviceInfo.getName();
        String sn = mDeviceInfo.getSn();
        //TODO 显示sn还是姓名等
//        getView().setTitleNameTextView(TextUtils.isEmpty(name) ? mContext.getResources().getString(R
//                .string.unname) : name);
        getView().setTitleNameTextView(TextUtils.isEmpty(name) ? sn : name);
        //
        mDeviceInfo.getSensorTypes();
        String contact = mDeviceInfo.getContact();
//        String address = mDeviceInfo.getAddress();
        String content = mDeviceInfo.getContent();
        if (TextUtils.isEmpty(contact)) {
            contact = "未设定";
        }
        if (TextUtils.isEmpty(content)) {
            content = "未设定";
        }
        getView().setContractName(contact);
        getView().setContractPhone(content);
        getView().setUpdateTime(DateUtil.getFullParseDate(mDeviceInfo.getUpdatedTime()));
    }

    private void freshDestPosition() {
        GeocodeSearch geocoderSearch = new GeocodeSearch(mContext);
        geocoderSearch.setOnGeocodeSearchListener(this);
        double[] lonlat = mDeviceInfo.getLonlat();
        try {
            destPosition = new LatLng(lonlat[1], lonlat[0]);
            RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(mDeviceInfo.getLonlat()[1], mDeviceInfo
                    .getLonlat()[0]), 200, GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);
//        if (mDeviceInfo.getSensorTypes().length > 0) {
//
//        }
        } catch (Exception e) {
            e.printStackTrace();
            getView().setDeviceLocation("未知街道");
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
            if (mDeviceInfo != null && AppUtils.isActivityTop(mContext, MonitoringPointDetailActivity.class)) {
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


    public void doNavigation() {
        AMapLocation lastKnownLocation = SensoroCityApplication.getInstance().mLocationClient.getLastKnownLocation();
        if (lastKnownLocation != null) {
            double lat = lastKnownLocation.getLatitude();//获取纬度
            double lon = lastKnownLocation.getLongitude();//获取经度
            LatLng startPosition = new LatLng(lat, lon);
            if (isAppInstalled(mContext, "com.autonavi.minimap")) {
                openGaoDeMap(startPosition);
            } else if (isAppInstalled(mContext, "com.baidu.BaiduMap")) {
                openBaiDuMap(startPosition);
            } else {
                openOther(startPosition);
            }
            return;
        }
        getView().toastShort("定位失败，请重试");

    }

    private void openGaoDeMap(LatLng startPosition) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        Uri uri = Uri.parse("amapuri://route/plan/?sid=BGVIS1&slat=" + startPosition.latitude + "&slon=" +
                startPosition.longitude + "&sname=当前位置" + "&did=BGVIS2&dlat=" + destPosition.latitude + "&dlon=" +
                destPosition.longitude +
                "&dname=设备部署位置" + "&dev=0&t=0");
        intent.setData(uri);
        //启动该页面即可
        getView().startAC(intent);
    }

    private void openBaiDuMap(LatLng startPosition) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("baidumap://map/direction?origin=name:当前位置|latlng:" + startPosition.latitude + "," +
                startPosition.longitude +
                "&destination=name:设备部署位置|latlng:" + destPosition.latitude + "," + destPosition.longitude +
                "&mode=driving&coord_type=gcj02"));
        getView().startAC(intent);
    }

    private void openOther(LatLng startPosition) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        String url = "http://uri.amap.com/navigation?from=" + startPosition.longitude + "," + startPosition.latitude
                + ",当前位置" +
                "&to=" + destPosition.longitude + "," + destPosition.latitude + "," +
                "设备部署位置&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=0";
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        getView().startAC(intent);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        final String address = regeocodeResult.getRegeocodeAddress().getFormatAddress();
        LogUtils.loge(this, "onRegeocodeSearched: " + "code = " + i + ",address = " + address);
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getView().setDeviceLocation(address);
            }
        });

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        LogUtils.loge(this, "onGeocodeSearched: " + "onGeocodeSearched");
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getView().setDeviceLocation("未知街道");
            }
        });

    }
}
