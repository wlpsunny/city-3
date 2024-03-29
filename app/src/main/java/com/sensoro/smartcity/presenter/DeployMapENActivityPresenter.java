package com.sensoro.smartcity.presenter;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.maps.widgets.MyLocationViewSettings;
import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.helper.PreferencesHelper;
import com.sensoro.common.iwidget.IOnCreate;
import com.sensoro.common.model.DeployAnalyzerModel;
import com.sensoro.common.model.EventData;
import com.sensoro.common.server.CityObserver;
import com.sensoro.common.server.RetrofitServiceHelper;
import com.sensoro.common.server.bean.DeviceInfo;
import com.sensoro.common.server.response.ResponseResult;
import com.sensoro.common.utils.GPSUtil;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.imainviews.IDeployMapENActivityView;
import com.sensoro.common.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class DeployMapENActivityPresenter extends BasePresenter<IDeployMapENActivityView> implements IOnCreate, OnMapReadyCallback, MapboxMap.InfoWindowAdapter, MapboxMap.OnCameraChangeListener {
    private MapboxMap aMap;
    private Activity mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private DeployAnalyzerModel deployAnalyzerModel;
    private Marker markerView;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        onCreate();
        deployAnalyzerModel = (DeployAnalyzerModel) mContext.getIntent().getSerializableExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL);
        switch (deployAnalyzerModel.deployType) {
            case Constants.TYPE_SCAN_DEPLOY_STATION:
                //基站部署
                getView().setSignalVisible(false);
                break;
            case Constants.TYPE_SCAN_DEPLOY_INSPECTION_DEVICE_CHANGE:
                //巡检设备更换
            case Constants.TYPE_SCAN_DEPLOY_DEVICE:
                //设备部署
                getView().setSignalVisible(false);
                getView().refreshSignal(deployAnalyzerModel.updatedTime, deployAnalyzerModel.signal);
                break;
            case Constants.TYPE_SCAN_LOGIN:
                break;
            case Constants.TYPE_SCAN_INSPECTION:
                //扫描巡检设备
                break;
            case Constants.TYPE_SCAN_DEPLOY_POINT_DISPLAY:
                //回显地图数据
                getView().setSaveVisible(false);
                getView().refreshSignal(deployAnalyzerModel.signal);
                getView().setSubtitleVisible(false);
                break;
            default:
                break;
        }

    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacksAndMessages(null);
    }


    public void doSaveLocation() {
        if (deployAnalyzerModel.latLng.size() == 2) {
            switch (deployAnalyzerModel.mapSourceType) {
                case Constants.DEPLOY_MAP_SOURCE_TYPE_DEPLOY_MONITOR_DETAIL:
                    if (PreferencesHelper.getInstance().getUserData().hasSignalConfig && deployAnalyzerModel.deployType != Constants.TYPE_SCAN_DEPLOY_STATION) {
                        getView().showProgressDialog();
                        RetrofitServiceHelper.getInstance().getDeployDeviceDetail(deployAnalyzerModel.sn, deployAnalyzerModel.latLng.get(0), deployAnalyzerModel.latLng.get(1))
                                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(this) {
                            @Override
                            public void onCompleted(ResponseResult<DeviceInfo> deployDeviceDetailRsp) {
                                deployAnalyzerModel.blePassword = deployDeviceDetailRsp.getData().getBlePassword();
                                List<Integer> channelMask = deployDeviceDetailRsp.getData().getChannelMask();
                                if (channelMask != null && channelMask.size() > 0) {
                                    deployAnalyzerModel.channelMask.clear();
                                    deployAnalyzerModel.channelMask.addAll(channelMask);
                                }
                                getView().dismissProgressDialog();
                                handlerResult();
                            }

                            @Override
                            public void onErrorMsg(int errorCode, String errorMsg) {
                                getView().dismissProgressDialog();
                                //TODO 可以添加是否需要处理channelmask字段
                                handlerResult();
                            }
                        });
                    } else {
                        handlerResult();
                    }
                    break;
                case Constants.DEPLOY_MAP_SOURCE_TYPE_DEPLOY_RECORD:
                    break;
                case Constants.DEPLOY_MAP_SOURCE_TYPE_MONITOR_MAP_CONFIRM:
                    getView().showProgressDialog();
                    RetrofitServiceHelper.getInstance().doDevicePositionCalibration(deployAnalyzerModel.sn, deployAnalyzerModel.latLng.get(0), deployAnalyzerModel.latLng.get(1)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(this) {
                        @Override
                        public void onCompleted(ResponseResult<DeviceInfo> deviceDeployRsp) {
                            getView().dismissProgressDialog();
                            DeviceInfo data = deviceDeployRsp.getData();
                            EventData eventData = new EventData();
                            eventData.code = Constants.EVENT_DATA_DEVICE_POSITION_CALIBRATION;
                            eventData.data = data;
                            EventBus.getDefault().post(eventData);
                            getView().finishAc();
                        }

                        @Override
                        public void onErrorMsg(int errorCode, String errorMsg) {
                            getView().dismissProgressDialog();
                            getView().toastShort(errorMsg);
                        }
                    });
                    break;
            }
        }
    }

    private void handlerResult() {
        EventData eventData = new EventData();
        eventData.code = Constants.EVENT_DATA_DEPLOY_MAP;
        eventData.data = deployAnalyzerModel;
        EventBus.getDefault().post(eventData);
        getView().finishAc();
    }

    public void refreshSignal() {
        switch (deployAnalyzerModel.mapSourceType) {
            case Constants.DEPLOY_MAP_SOURCE_TYPE_DEPLOY_MONITOR_DETAIL:
                getView().showProgressDialog();
                RetrofitServiceHelper.getInstance().getDeviceDetailInfoList(deployAnalyzerModel.sn, null, 1).subscribeOn(Schedulers.io()).observeOn
                        (AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<List<DeviceInfo>>>(this) {


                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                    }

                    @Override
                    public void onCompleted(ResponseResult<List<DeviceInfo>> deviceInfoListRsp) {
                        if (deviceInfoListRsp.getData().size() > 0) {
                            DeviceInfo deviceInfo = deviceInfoListRsp.getData().get(0);
                            String signal = deviceInfo.getSignal();
                            getView().refreshSignal(deviceInfo.getUpdatedTime(), signal);
                        }
                        getView().dismissProgressDialog();
                    }
                });
                break;
            case Constants.DEPLOY_MAP_SOURCE_TYPE_DEPLOY_RECORD:
                break;
            case Constants.DEPLOY_MAP_SOURCE_TYPE_MONITOR_MAP_CONFIRM:
                break;
        }

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (cameraPosition != null) {
            LatLng target = cameraPosition.target;
            switch (deployAnalyzerModel.mapSourceType) {
                case Constants.DEPLOY_MAP_SOURCE_TYPE_DEPLOY_RECORD:
                    break;
                case Constants.DEPLOY_MAP_SOURCE_TYPE_DEPLOY_MONITOR_DETAIL:
                case Constants.DEPLOY_MAP_SOURCE_TYPE_MONITOR_MAP_CONFIRM:
                    double latitude = target.getLatitude();
                    double longitude = target.getLongitude();
                    double[] doubles = GPSUtil.gps84_To_Gcj02(latitude, longitude);
                    deployAnalyzerModel.latLng.clear();
                    deployAnalyzerModel.latLng.add(doubles[1]);
                    deployAnalyzerModel.latLng.add(doubles[0]);
                    if (markerView != null) {
                        ValueAnimator markerAnimator = ObjectAnimator.ofObject(markerView, "position",
                                new LatLngEvaluator(), markerView.getPosition(), target);
                        markerAnimator.setDuration(0);
                        markerAnimator.start();
                    }
                    break;
            }
        }
    }


    public void backToCurrentLocation() {
        switch (deployAnalyzerModel.mapSourceType) {
            case Constants.DEPLOY_MAP_SOURCE_TYPE_DEPLOY_RECORD:
                if (deployAnalyzerModel.latLng.size() == 2) {
                    freshMap();
                }
                break;
            case Constants.DEPLOY_MAP_SOURCE_TYPE_DEPLOY_MONITOR_DETAIL:
            case Constants.DEPLOY_MAP_SOURCE_TYPE_MONITOR_MAP_CONFIRM:
                if (aMap != null) {
                    aMap.clear();
                    Location lastLocation = aMap.getMyLocation();
                    if (lastLocation != null) {
                        LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        IconFactory iconFactory = IconFactory.getInstance(mContext);
                        Icon icon = iconFactory.fromResource(R.drawable.deploy_map_cur);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.icon(icon).title(mContext.getString(R.string.unknown_street))
                                .position(latLng);
                        markerView = aMap.addMarker(markerOptions);
                        markerView.setPosition(latLng);
                        CameraPosition position = new CameraPosition.Builder()
                                .target(latLng)
                                .zoom(16)
                                .tilt(20)
                                .build();
                        try {
                            LogUtils.loge("---markerView null");
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
                    }
                }
                break;
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeviceInfo deviceInfo) {
        //上报异常结果成功
        String sn = deviceInfo.getSn();
        try {
            if (deployAnalyzerModel.sn.equalsIgnoreCase(sn)) {
                deployAnalyzerModel.updatedTime = deviceInfo.getUpdatedTime();
                deployAnalyzerModel.signal = deviceInfo.getSignal();
                try {
                    LogUtils.loge(this, "地图也刷新信号 -->> deployMapModel.updatedTime = " + deployAnalyzerModel.updatedTime + ",deployMapModel.signal = " + deployAnalyzerModel.signal);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                getView().refreshSignal(deployAnalyzerModel.updatedTime, deployAnalyzerModel.signal);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onMapReady(final MapboxMap mapboxMap) {
        this.aMap = mapboxMap;
        //
        UiSettings uiSettings = aMap.getUiSettings();
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setCompassEnabled(false);//隐藏指南针
        uiSettings.setLogoEnabled(false);//隐藏logo
        uiSettings.setTiltGesturesEnabled(true);//设置是否可以调整地图倾斜角
        uiSettings.setRotateGesturesEnabled(true);//设置是否可以旋转地图
        uiSettings.setAttributionEnabled(false);//设置是否显示那个提示按钮
        aMap.setMyLocationEnabled(true);
        aMap.setInfoWindowAdapter(this);
        MyLocationViewSettings locationSettings = aMap.getMyLocationViewSettings();
        locationSettings.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.deploy_map_location), new int[]{20, 20, 20, 20});
        locationSettings.setForegroundTintColor(ContextCompat.getColor(mContext, R.color.transparent));
        locationSettings.setAccuracyTintColor(ContextCompat.getColor(mContext, R.color.transparent));
        locationSettings.setAccuracyAlpha(50);
        locationSettings.setTilt(30);
        //
        aMap.setOnCameraChangeListener(this);
        freshMap();
    }

    private void freshMap() {
        if (aMap != null) {
            aMap.clear();
            if (deployAnalyzerModel.latLng.size() == 2) {
//可视化区域，将指定位置指定到屏幕中心位置
                double[] currentLonlat = GPSUtil.gcj02_To_Gps84(deployAnalyzerModel.latLng.get(1), deployAnalyzerModel.latLng.get(0));
                LatLng latLng = new LatLng(currentLonlat[0], currentLonlat[1]);
                IconFactory iconFactory = IconFactory.getInstance(mContext);
                Icon icon = iconFactory.fromResource(R.drawable.deploy_map_cur);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.icon(icon).title(mContext.getString(R.string.unknown_street))
                        .position(latLng);
                markerView = aMap.addMarker(markerOptions);
                markerView.setPosition(latLng);
                CameraPosition position = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(16)
                        .tilt(20)
                        .build();
                aMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
            } else {
                backToCurrentLocation();
            }
        }

    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        View view = mContext.getLayoutInflater().inflate(R.layout.layout_marker, null);
        TextView info = (TextView) view.findViewById(R.id.marker_info);
        info.setText(marker.getTitle());
        return view;
    }

    private final class LatLngEvaluator implements TypeEvaluator<LatLng> {

        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }

}
