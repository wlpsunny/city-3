package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.activity.MonitorPointMapActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.IMonitorPointMapActivityView;
import com.sensoro.smartcity.iwidget.IOnStart;
import com.sensoro.smartcity.model.PushData;
import com.sensoro.smartcity.server.bean.DeviceInfo;
import com.sensoro.smartcity.util.AppUtils;
import com.sensoro.smartcity.util.ImageFactory;
import com.sensoro.smartcity.util.LogUtils;
import com.sensoro.smartcity.util.WidgetUtil;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static com.amap.api.maps.AMap.MAP_TYPE_NORMAL;
import static com.sensoro.smartcity.util.AppUtils.isAppInstalled;

public class MonitorPointMapActivityPresenter extends BasePresenter<IMonitorPointMapActivityView> implements Constants, AMap.OnMapLoadedListener, IOnStart {

    private Activity mContext;
    private AMap aMap;
    private LatLng destPosition;
    private Bitmap tempUpBitmap;
    private DeviceInfo mDeviceInfo;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        mDeviceInfo = (DeviceInfo) mContext.getIntent().getSerializableExtra(EXTRA_DEVICE_INFO);
        initMap();
    }

    @Override
    public void onDestroy() {
        if (tempUpBitmap != null) {
            tempUpBitmap.recycle();
            tempUpBitmap = null;
        }
    }

    public void getMap(AMap map) {
        aMap = map;
    }

    private void initMap() {
        aMap.setMapCustomEnable(true);
        aMap.getUiSettings().setTiltGesturesEnabled(false);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        aMap.setOnMapLoadedListener(this);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
//        aMap.getUiSettings().setScaleControlsEnabled(true);
        aMap.setMapType(MAP_TYPE_NORMAL);
//        aMap.setOnMapTouchListener(this);
        String styleName = "custom_config.data";
        aMap.setCustomMapStylePath(mContext.getFilesDir().getAbsolutePath() + "/" + styleName);
    }

    @Override
    public void onMapLoaded() {
        refreshMap();
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
            if (mDeviceInfo != null && AppUtils.isActivityTop(mContext, MonitorPointMapActivity.class)) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        freshTopData();
//                        freshStructData();
                        freshMarker();
                    }
                });


            }
        }
    }

    private void refreshMap() {
        double[] lonlat = mDeviceInfo.getLonlat();
        if (aMap != null && mDeviceInfo.getSensorTypes().length > 0) {
            UiSettings uiSettings = aMap.getUiSettings();
            // 通过UISettings.setZoomControlsEnabled(boolean)来设置缩放按钮是否能显示
            uiSettings.setZoomControlsEnabled(false);
            destPosition = new LatLng(lonlat[1], lonlat[0]);
            if (lonlat[0] == 0 && lonlat[1] == 0) {
//                getView().setMapLayoutVisible(false);
//                getView().setNotDeployLayoutVisible(true);
//                mapLayout.setVisibility(View.GONE);
//                notDeployLayout.setVisibility(View.VISIBLE);
            } else {
//                getView().setMapLayoutVisible(true);
//                getView().setNotDeployLayoutVisible(false);
//                mapLayout.setVisibility(View.VISIBLE);
//                notDeployLayout.setVisibility(View.GONE);
                //可视化区域，将指定位置指定到屏幕中心位置
                final CameraUpdate mUpdata = CameraUpdateFactory
                        .newCameraPosition(new CameraPosition(destPosition, 16, 0, 30));
                aMap.moveCamera(mUpdata);

                freshMarker();
//                RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(mDeviceInfo.getLonlat()[1], mDeviceInfo
//                        .getLonlat()[0]), 200, GeocodeSearch.AMAP);
//                geocoderSearch.getFromLocationAsyn(query);

            }

        }
    }

    private void freshMarker() {
        int statusId = R.mipmap.ic_sensor_status_normal;
        switch (mDeviceInfo.getStatus()) {
            case SENSOR_STATUS_ALARM://alarm
                statusId = R.mipmap.ic_sensor_status_alarm;
                break;
            case SENSOR_STATUS_NORMAL://normal
                statusId = R.mipmap.ic_sensor_status_normal;
                break;
            case SENSOR_STATUS_INACTIVE://inactive
                statusId = R.mipmap.ic_sensor_status_inactive;
                break;
            case SENSOR_STATUS_LOST://lost
                statusId = R.mipmap.ic_sensor_status_lost;
                break;
            default:
                break;
        }
        BitmapDescriptor bitmapDescriptor = null;
        Bitmap srcBitmap = BitmapFactory.decodeResource(mContext.getResources(), statusId);
        if (WidgetUtil.judgeSensorType(mDeviceInfo.getSensorTypes()) != 0) {
            Bitmap targetBitmap = BitmapFactory.decodeResource(mContext.getResources(), WidgetUtil
                    .judgeSensorType(mDeviceInfo.getSensorTypes()));
            Bitmap filterTargetBitmap = WidgetUtil.tintBitmap(targetBitmap, mContext.getResources().getColor
                    (R.color
                            .white));
            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(WidgetUtil.createBitmapDrawable(mContext,
                    mDeviceInfo.getSensorTypes()[0], srcBitmap, filterTargetBitmap).getBitmap());
        } else {
            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(srcBitmap);
        }
        tempUpBitmap = bitmapDescriptor.getBitmap();
        aMap.clear();
        MarkerOptions markerOption = new MarkerOptions().icon(bitmapDescriptor)
                .position(destPosition)
                .draggable(true);
        Marker marker = aMap.addMarker(markerOption);
        marker.setDraggable(false);
        marker.showInfoWindow();
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

    public void doDetailShare() {
        boolean wxAppInstalled = SensoroCityApplication.getInstance().api.isWXAppInstalled();
        if (wxAppInstalled) {
//            boolean wxAppSupportAPI = SensoroCityApplication.getInstance().api.isWXAppSupportAPI();
//            if (wxAppSupportAPI) {
            toShareWeChat();
//            } else {
//                getView().toastShort("当前版的微信不支持分享功能");
//            }
        } else {
            getView().toastShort("当前手机未安装微信，请安装后重试");
        }
    }

    /**
     * 微信分享
     */
    private void toShareWeChat() {
        WXMiniProgramObject miniProgramObj = new WXMiniProgramObject();
        miniProgramObj.miniprogramType = WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE;
        miniProgramObj.webpageUrl = "https://www.sensoro.com"; // 兼容低版本的网页链接
        miniProgramObj.userName = "gh_6b7a86071f47";
        miniProgramObj.withShareTicket = false;
        String name = mDeviceInfo.getName();
        if (TextUtils.isEmpty(name)) {
            name = mDeviceInfo.getSn();
        }
        int status = mDeviceInfo.getStatus();
        String[] tags = mDeviceInfo.getTags();
        String tempTagStr = "";
        if (tags != null && tags.length > 0) {
            for (String tag : tags) {
                tempTagStr += tag + ",";
            }
            tempTagStr = tempTagStr.substring(0, tempTagStr.lastIndexOf(","));
        }
        long updatedTime = mDeviceInfo.getUpdatedTime();
        String tempAddress = mDeviceInfo.getAddress();
        if (TextUtils.isEmpty(tempAddress)) {
            tempAddress = "未知街道";
        }
        final String tempData = "/pages/index?lon=" + mDeviceInfo.getLonlat()[0] + "&lat=" + mDeviceInfo.getLonlat()
                [1] +
                "&name=" + name + "&address=" + tempAddress + "&status=" + status + "&tags=" + tempTagStr + "&uptime=" +
                updatedTime;
        miniProgramObj.path = tempData;            //小程序页面路径
        final WXMediaMessage msg = new WXMediaMessage(miniProgramObj);
        msg.title = "传感器位置";                    // 小程序消息title
        msg.description = "通过此工具，可以查看，以及导航到相应的传感器设备";
        aMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(Bitmap bitmap) {
//                int allocationByteCount = bitmap.getAllocationByteCount();
//                Bitmap ratio = ImageFactory.ratio(bitmap, 500, 400);
//                int allocationByteCount1 = ratio.getAllocationByteCount();
//                msg.thumbData = Util.bmpToByteArray(ratio, true);
                byte[] ratio = ImageFactory.ratio(bitmap);
//                int length = ratio.length;
                msg.thumbData = ratio;
                bitmap.recycle();
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = SystemClock.currentThreadTimeMillis() + "";
                req.scene = SendMessageToWX.Req.WXSceneSession;
                req.message = msg;
                boolean b = SensoroCityApplication.getInstance().api.sendReq(req);
                LogUtils.loge(this, "toShareWeChat: isSuc = " + b + ",bitmapLength = " + ratio);
            }

            @Override
            public void onMapScreenShot(Bitmap bitmap, int i) {
                LogUtils.loge(this, "onMapScreenShot: i = " + i);
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

}