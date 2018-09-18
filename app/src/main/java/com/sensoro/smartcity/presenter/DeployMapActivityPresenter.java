package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.RegeocodeRoad;
import com.amap.api.services.geocoder.StreetNumber;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.IDeployMapActivityView;
import com.sensoro.smartcity.model.DeployMapModel;
import com.sensoro.smartcity.model.EventData;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.DeviceInfo;
import com.sensoro.smartcity.server.response.DeviceInfoListRsp;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DeployMapActivityPresenter extends BasePresenter<IDeployMapActivityView> implements AMap.OnMapClickListener, AMap.OnCameraChangeListener, AMap.OnMarkerClickListener, AMap.OnMapLoadedListener, AMap.OnMapTouchListener, AMap.InfoWindowAdapter, GeocodeSearch.OnGeocodeSearchListener, Constants {
    private AMap aMap;
    private Marker smoothMoveMarker;
    private GeocodeSearch geocoderSearch;
    private Activity mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private DeployMapModel deployMapModel;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        deployMapModel = (DeployMapModel) mContext.getIntent().getParcelableExtra(EXTRA_DEPLOY_TO_MAP);
        getView().setSignalVisible(!deployMapModel.hasStation);
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private void getCurrentLocation() {
        System.out.println("====>onLocationChanged");
        AMapLocation lastKnownLocation = SensoroCityApplication.getInstance().mLocationClient.getLastKnownLocation();
        if (lastKnownLocation != null) {
            double lat = lastKnownLocation.getLatitude();//获取纬度
            double lon = lastKnownLocation.getLongitude();//获取经度
            deployMapModel.latLng = new LatLng(lat, lon);
            if (aMap != null) {
                //可视化区域，将指定位置指定到屏幕中心位置
                CameraUpdate update = CameraUpdateFactory
                        .newCameraPosition(new CameraPosition(deployMapModel.latLng, 15, 0, 30));
                aMap.moveCamera(update);
            }
            smoothMoveMarker.setPosition(deployMapModel.latLng);
        } else {
            //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
            Log.e("地图错误", "定位失败, 错误码:" + lastKnownLocation.getErrorCode() + ", 错误信息:"
                    + lastKnownLocation.getErrorInfo());
        }
    }


    private void setMarkerAddress(RegeocodeAddress regeocodeAddress) {
        StringBuffer stringBuffer = new StringBuffer();
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
//        if (subLoc != null) {
//            stringBuffer.append(subLoc);
//        }
//        if (ts != null) {
//            stringBuffer.append(ts);
//        }
        if (thf != null) {
            stringBuffer.append(thf);
        }
        if (subthf != null) {
            stringBuffer.append(subthf);
        }
        String address = stringBuffer.toString();
        if (TextUtils.isEmpty(address)) {
            address = ts;
        }
        deployMapModel.address = address;
        System.out.println(address);
        if (TextUtils.isEmpty(address)) {
            smoothMoveMarker.hideInfoWindow();
        } else {
            smoothMoveMarker.setTitle(address);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    smoothMoveMarker.showInfoWindow();
                }
            });
        }
    }

    private void setMapCustomStyleFile() {
        String styleName = "custom_config.data";
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        String filePath = null;
        try {
            inputStream = mContext.getAssets().open(styleName);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);

            filePath = mContext.getFilesDir().getAbsolutePath();
            File file = new File(filePath + "/" + styleName);
            if (!file.exists()) {
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file);
            outputStream.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        aMap.setCustomMapStylePath(filePath + "/" + styleName);

    }

    public void initMap(AMap map) {
        this.aMap = map;
        setMapCustomStyleFile();
        aMap.getUiSettings().setTiltGesturesEnabled(false);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        aMap.setMyLocationEnabled(true);
        aMap.setMapCustomEnable(true);
//            String styleName = "custom_config.data";
//            aMap.setCustomMapStylePath(mContext.getFilesDir().getAbsolutePath() + "/" + styleName);
        aMap.setOnMapClickListener(this);
        aMap.setOnCameraChangeListener(this);
        aMap.setOnMapLoadedListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setInfoWindowAdapter(this);
        aMap.setOnMapTouchListener(this);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.radiusFillColor(Color.argb(25, 73, 144, 226));
        myLocationStyle.strokeWidth(0);
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.deploy_map_location));
        myLocationStyle.showMyLocation(true);
        aMap.setMyLocationStyle(myLocationStyle);
    }

    public void doSaveLocation() {
        EventData eventData = new EventData();
        eventData.code = EVENT_DATA_DEPLOY_MAP;
        eventData.data = deployMapModel;
        EventBus.getDefault().post(eventData);
        getView().finishAc();
    }

    public void refreshSignal() {
        getView().showProgressDialog();
        RetrofitServiceHelper.INSTANCE.getDeviceDetailInfoList(deployMapModel.sn, null, 1).subscribeOn(Schedulers.io()).observeOn
                (AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceInfoListRsp>(this) {


            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);
            }

            @Override
            public void onCompleted(DeviceInfoListRsp deviceInfoListRsp) {
                if (deviceInfoListRsp.getData().size() > 0) {
                    DeviceInfo deviceInfo = deviceInfoListRsp.getData().get(0);
                    String signal = deviceInfo.getSignal();
                    getView().refreshSignal(deviceInfo.getUpdatedTime(), signal);
                }
                getView().dismissProgressDialog();
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (cameraPosition != null) {
            deployMapModel.latLng = cameraPosition.target;
            smoothMoveMarker.setPosition(deployMapModel.latLng);
            System.out.println("====>onCameraChange");
        }
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        deployMapModel.latLng = cameraPosition.target;
        smoothMoveMarker.setPosition(deployMapModel.latLng);
        LatLonPoint lp = new LatLonPoint(deployMapModel.latLng.latitude, deployMapModel.latLng.longitude);
        System.out.println("====>onCameraChangeFinish=>" + lp.getLatitude() + "&" + lp.getLongitude());
        RegeocodeQuery query = new RegeocodeQuery(lp, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapLoaded() {
        //TODO 先获原先取信号强度

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.deploy_map_cur);
        MarkerOptions markerOption = new MarkerOptions().icon(bitmapDescriptor)
                .anchor(0.5f, 0.5f)
                .draggable(true);
        smoothMoveMarker = aMap.addMarker(markerOption);
        geocoderSearch = new GeocodeSearch(mContext);
        geocoderSearch.setOnGeocodeSearchListener(this);
        getCurrentLocation();
        if (!deployMapModel.hasStation) {
            getView().refreshSignal(deployMapModel.updatedTime, deployMapModel.signal);
            refreshSignal();
        }
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        System.out.println("====>onRegeocodeSearched");
        try {
            RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
            setMarkerAddress(regeocodeAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onTouch(MotionEvent motionEvent) {

    }

    @Override
    public View getInfoWindow(Marker marker) {
        View view = mContext.getLayoutInflater().inflate(R.layout.layout_marker, null);
        TextView info = (TextView) view.findViewById(R.id.marker_info);
        info.setText(marker.getTitle());
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    public void backToCurrentLocation() {
        AMapLocation lastKnownLocation = SensoroCityApplication.getInstance().mLocationClient.getLastKnownLocation();
        if (lastKnownLocation != null) {
            double lat = lastKnownLocation.getLatitude();//获取纬度
            double lon = lastKnownLocation.getLongitude();//获取经度
            LatLng latLng = new LatLng(lat, lon);
            if (aMap != null) {
                //可视化区域，将指定位置指定到屏幕中心位置
                CameraUpdate update = CameraUpdateFactory
                        .newCameraPosition(new CameraPosition(latLng, 15, 0, 30));
                aMap.moveCamera(update);
            }
            smoothMoveMarker.setPosition(latLng);
        } else {
            //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
            Log.e("地图错误", "定位失败, 错误码:" + lastKnownLocation.getErrorCode() + ", 错误信息:"
                    + lastKnownLocation.getErrorInfo());
        }
    }
}