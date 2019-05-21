package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.RegeocodeRoad;
import com.amap.api.services.geocoder.StreetNumber;
import com.sensoro.common.iwidget.IOnCreate;
import com.sensoro.common.iwidget.IOnStart;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.activity.DeployDeviceTagActivity;
import com.sensoro.smartcity.activity.DeployMapActivity;
import com.sensoro.smartcity.activity.DeployMapENActivity;
import com.sensoro.smartcity.activity.DeployMonitorDeployPicActivity;
import com.sensoro.smartcity.activity.DeployMonitorNameAddressActivity;
import com.sensoro.smartcity.activity.DeployResultActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.IDeployCameraDetailActivityView;
import com.sensoro.smartcity.model.CameraFilterModel;
import com.sensoro.smartcity.model.DeployAnalyzerModel;
import com.sensoro.smartcity.model.DeployCameraConfigModel;
import com.sensoro.smartcity.model.DeployContactModel;
import com.sensoro.smartcity.model.DeployResultModel;
import com.sensoro.smartcity.model.EventData;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.DeployControlSettingData;
import com.sensoro.smartcity.server.bean.DeviceInfo;
import com.sensoro.smartcity.server.bean.ScenesData;
import com.sensoro.smartcity.server.response.CameraFilterRsp;
import com.sensoro.smartcity.server.response.DeviceDeployRsp;
import com.sensoro.smartcity.util.AppUtils;
import com.sensoro.smartcity.util.LogUtils;
import com.sensoro.smartcity.util.PreferencesHelper;
import com.sensoro.smartcity.util.WidgetUtil;
import com.sensoro.smartcity.widget.imagepicker.bean.ImageItem;
import com.sensoro.smartcity.widget.popup.SelectDialog;
import com.sensoro.smartcity.widget.popup.UpLoadPhotosUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeployCameraDetailActivityPresenter extends BasePresenter<IDeployCameraDetailActivityView> implements IOnCreate, IOnStart, Constants
        , Runnable {
    private Activity mContext;
    private Handler mHandler;
    private DeployAnalyzerModel deployAnalyzerModel;
    private final Runnable signalTask = new Runnable() {
        @Override
        public void run() {
            //TODO 轮询查看摄像头状态？
            mHandler.postDelayed(signalTask, 2000);
        }
    };
    private final List<DeployCameraConfigModel> deployMethods = new ArrayList<>();
    private final List<DeployCameraConfigModel> deployOrientations = new ArrayList<>();
    private DeployCameraConfigModel mOrientationConfig;
    private DeployCameraConfigModel mMethodConfig;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        mHandler = new Handler(Looper.getMainLooper());
        onCreate();
        Intent intent = mContext.getIntent();
        deployAnalyzerModel = (DeployAnalyzerModel) intent.getSerializableExtra(EXTRA_DEPLOY_ANALYZER_MODEL);
        getView().setNotOwnVisible(deployAnalyzerModel.notOwn);
        if (PreferencesHelper.getInstance().getUserData().hasSignalConfig && deployAnalyzerModel.deployType != TYPE_SCAN_DEPLOY_STATION || Constants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType)) {
            mHandler.post(this);
        }
        mHandler.post(signalTask);
        init();
        requestData();
    }

    private void init() {
        getView().setDeviceSn(mContext.getString(R.string.device_number) + deployAnalyzerModel.sn);
        if (!TextUtils.isEmpty(deployAnalyzerModel.nameAndAddress)) {
            getView().setNameAddressText(deployAnalyzerModel.nameAndAddress);
        }
        getView().setDeployPhotoVisible(true);
        getView().setDeployDeviceType("摄像机");
        getView().updateTagsData(deployAnalyzerModel.tagList);
        //默认显示已定位
        deployAnalyzerModel.address = mContext.getString(R.string.positioned);
        if (checkHasDeployPosition()) {
            GeocodeSearch geocoderSearch = new GeocodeSearch(mContext);
            geocoderSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
                @Override
                public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                    try {
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
                        String address;
                        if (TextUtils.isEmpty(stringBuilder)) {
                            address = township;
                        } else {
                            address = stringBuilder.append("附近").toString();
                        }
                        if (!TextUtils.isEmpty(address)) {
                            deployAnalyzerModel.address = address;
                        }
                        try {
                            LogUtils.loge("deployMapModel", "----" + deployAnalyzerModel.address);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        //
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getView().setDeployPosition(true, deployAnalyzerModel.address);
                }

                @Override
                public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

                }
            });
            //查询一次地址信息
            LatLonPoint lp = new LatLonPoint(deployAnalyzerModel.latLng.get(1), deployAnalyzerModel.latLng.get(0));
            RegeocodeQuery query = new RegeocodeQuery(lp, 200, GeocodeSearch.AMAP);
            geocoderSearch.getFromLocationAsyn(query);
        } else {
            getView().setDeployPosition(false, null);
        }
    }

    private void requestData() {
        getView().showProgressDialog();
        RetrofitServiceHelper.getInstance().getCameraFilter().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<CameraFilterRsp>(this) {
            @Override
            public void onCompleted(CameraFilterRsp cameraFilterRsp) {
                List<CameraFilterModel> data = cameraFilterRsp.getData();
                if (data != null) {
                    for (CameraFilterModel cameraFilterModel : data) {
                        String key = cameraFilterModel.getKey();
                        if ("orientation".equalsIgnoreCase(key)) {
                            List<CameraFilterModel.ListBean> list = cameraFilterModel.getList();
                            if (list != null) {
                                for (CameraFilterModel.ListBean listBean : list) {
                                    DeployCameraConfigModel deployCameraConfigModel = new DeployCameraConfigModel();
                                    deployCameraConfigModel.code = listBean.getCode();
                                    deployCameraConfigModel.name = listBean.getName();
                                    deployOrientations.add(deployCameraConfigModel);
                                    if (deployAnalyzerModel.orientation != null && deployAnalyzerModel.orientation.equals(deployCameraConfigModel.code)) {
                                        mOrientationConfig = deployCameraConfigModel;
                                    }
                                }
                            }
                            //安装朝向
                        } else if ("installationMode".equalsIgnoreCase(key)) {
                            //安装方式
                            List<CameraFilterModel.ListBean> list = cameraFilterModel.getList();
                            if (list != null) {
                                for (CameraFilterModel.ListBean listBean : list) {
                                    DeployCameraConfigModel deployCameraConfigModel = new DeployCameraConfigModel();
                                    deployCameraConfigModel.name = listBean.getName();
                                    deployCameraConfigModel.code = listBean.getCode();
                                    deployMethods.add(deployCameraConfigModel);
                                    if (deployAnalyzerModel.installationMode != null && deployAnalyzerModel.installationMode.equals(deployCameraConfigModel.code)) {
                                        mMethodConfig = deployCameraConfigModel;
                                    }
                                }
                            }
                        }
                    }
                }
                echoDeviceInfo();
                getView().dismissProgressDialog();
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);
            }
        });
    }

    //回显设备信息
    private void echoDeviceInfo() {
        //TODO 刷线摄像头状态
        getView().setUploadBtnStatus(checkCanUpload());
        //TODO 信息回查
        if (mOrientationConfig == null || TextUtils.isEmpty(deployAnalyzerModel.orientation)) {
            getView().setDeployOrientation(null);
        } else {
            getView().setDeployOrientation(mOrientationConfig.name);
        }
        if (mMethodConfig == null || TextUtils.isEmpty(deployAnalyzerModel.installationMode)) {
            getView().setDeployMethod(null);
        } else {
            getView().setDeployMethod(mMethodConfig.name);
        }
        getView().setDeployCameraStatus(deployAnalyzerModel.cameraStatus);
//        getView().updateUploadTvText(mContext.getString(R.string.replacement_equipment));
    }

    //
    public void requestUpload() {
        final double lon = deployAnalyzerModel.latLng.get(0);
        final double lan = deployAnalyzerModel.latLng.get(1);
        //TODO 直接上传
    }


    private void doUploadImages(final double lon, final double lan) {
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
                        doDeployResult(lon, lan, strings);
                    }


                }

                @Override
                public void onError(String errMsg) {
                    if (isAttachedView()) {
                        getView().updateUploadState(true);
                        getView().dismissUploadProgressDialog();
                        getView().toastShort(errMsg);
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
            for (ImageItem image : deployAnalyzerModel.images) {
                if (image != null) {
                    list.add(image);
                }
            }
            upLoadPhotosUtils.doUploadPhoto(list);
        } else {
            doDeployResult(lon, lan, null);
        }
    }

    private void doDeployResult(double lon, double lan, List<String> imgUrls) {
        //TODO 上传接口

        DeployContactModel deployContactModel = deployAnalyzerModel.deployContactModelList.get(0);
        switch (deployAnalyzerModel.deployType) {
            case TYPE_SCAN_DEPLOY_DEVICE:
                //设备部署
                getView().showProgressDialog();
                //TODO 暂时不支持添加wx电话
                //TODO 添加电气火灾配置支持
//                deployAnalyzerModel.weChatAccount = null;
                boolean isFire = DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType);
                //暂时添加 后续可以删除
                DeployControlSettingData settingData = null;
                if (isFire) {
                    settingData = deployAnalyzerModel.settingData;
                }
                RetrofitServiceHelper.getInstance().doDevicePointDeploy(deployAnalyzerModel.sn, lon, lan, deployAnalyzerModel.tagList, deployAnalyzerModel.nameAndAddress,
                        deployContactModel.name, deployContactModel.phone, deployAnalyzerModel.weChatAccount, imgUrls, settingData, deployAnalyzerModel.forceReason, deployAnalyzerModel.status, deployAnalyzerModel.currentSignalQuality).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CityObserver<DeviceDeployRsp>(this) {
                            @Override
                            public void onErrorMsg(int errorCode, String errorMsg) {
                                getView().dismissProgressDialog();
                                getView().updateUploadState(true);
                                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                                    getView().toastShort(errorMsg);
                                } else if (errorCode == 4013101 || errorCode == 4000013) {
                                    freshError(deployAnalyzerModel.sn, null, DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT);
                                } else {
                                    freshError(deployAnalyzerModel.sn, errorMsg, DEPLOY_RESULT_MODEL_CODE_DEPLOY_FAILED);
                                }
                            }

                            @Override
                            public void onCompleted(DeviceDeployRsp deviceDeployRsp) {
                                freshPoint(deviceDeployRsp);
                                getView().dismissProgressDialog();
                                getView().finishAc();
                            }
                        });
                break;
            case TYPE_SCAN_DEPLOY_INSPECTION_DEVICE_CHANGE:
                getView().showProgressDialog();
                RetrofitServiceHelper.getInstance().doInspectionChangeDeviceDeploy(deployAnalyzerModel.mDeviceDetail.getSn(), deployAnalyzerModel.sn,
                        deployAnalyzerModel.mDeviceDetail.getTaskId(), 1, lon, lan, deployAnalyzerModel.tagList, deployAnalyzerModel.nameAndAddress,
                        deployContactModel.name, deployContactModel.phone, imgUrls, null, deployAnalyzerModel.forceReason, deployAnalyzerModel.status, deployAnalyzerModel.currentSignalQuality).
                        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceDeployRsp>(this) {
                    @Override
                    public void onCompleted(DeviceDeployRsp deviceDeployRsp) {
                        freshPoint(deviceDeployRsp);
                        getView().dismissProgressDialog();
                        getView().finishAc();
                    }

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().updateUploadState(true);
                        if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                            getView().toastShort(errorMsg);
                        } else if (errorCode == 4013101 || errorCode == 4000013) {
                            freshError(deployAnalyzerModel.sn, null, DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT);
                        } else {
                            freshError(deployAnalyzerModel.sn, errorMsg, DEPLOY_RESULT_MODEL_CODE_DEPLOY_FAILED);
                        }
                    }
                });
                break;
            case TYPE_SCAN_DEPLOY_MALFUNCTION_DEVICE_CHANGE:
                getView().showProgressDialog();
                RetrofitServiceHelper.getInstance().doInspectionChangeDeviceDeploy(deployAnalyzerModel.mDeviceDetail.getSn(), deployAnalyzerModel.sn,
                        null, 2, lon, lan, deployAnalyzerModel.tagList, deployAnalyzerModel.nameAndAddress, deployContactModel.name,
                        deployContactModel.phone, imgUrls, null, deployAnalyzerModel.forceReason, deployAnalyzerModel.status, deployAnalyzerModel.currentSignalQuality).
                        subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceDeployRsp>(this) {
                    @Override
                    public void onCompleted(DeviceDeployRsp deviceDeployRsp) {
                        //
                        freshPoint(deviceDeployRsp);
                        getView().dismissProgressDialog();
                        getView().finishAc();
                    }

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().updateUploadState(true);
                        if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                            getView().toastShort(errorMsg);
                        } else if (errorCode == 4013101 || errorCode == 4000013) {
                            freshError(deployAnalyzerModel.sn, null, DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT);
                        } else {
                            freshError(deployAnalyzerModel.sn, errorMsg, DEPLOY_RESULT_MODEL_CODE_DEPLOY_FAILED);
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
            DeployContactModel deployContactModel = deployAnalyzerModel.deployContactModelList.get(0);
            deployResultModel.contact = deployContactModel.name;
            deployResultModel.phone = deployContactModel.phone;
        }
        deployResultModel.address = deployAnalyzerModel.address;
        deployResultModel.updateTime = deployAnalyzerModel.updatedTime;
        deployResultModel.deviceStatus = deployAnalyzerModel.status;
        deployResultModel.signal = deployAnalyzerModel.signal;
        deployResultModel.name = deployAnalyzerModel.nameAndAddress;
        intent.putExtra(EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
        getView().startAC(intent);
    }

    private void freshPoint(DeviceDeployRsp deviceDeployRsp) {
        DeployResultModel deployResultModel = new DeployResultModel();
        DeviceInfo deviceInfo = deviceDeployRsp.getData();
        deployResultModel.deviceInfo = deviceInfo;
        Intent intent = new Intent(mContext, DeployResultActivity.class);
        //
        deployResultModel.sn = deviceInfo.getSn();
        deployResultModel.deviceType = deployAnalyzerModel.deviceType;
        deployResultModel.resultCode = DEPLOY_RESULT_MODEL_CODE_DEPLOY_SUCCESS;
        deployResultModel.scanType = deployAnalyzerModel.deployType;
        deployResultModel.wxPhone = deployAnalyzerModel.weChatAccount;
        deployResultModel.settingData = deployAnalyzerModel.settingData;
        //TODO 新版联系人
        if (deployAnalyzerModel.deployContactModelList.size() > 0) {
            DeployContactModel deployContactModel = deployAnalyzerModel.deployContactModelList.get(0);
            deployResultModel.contact = deployContactModel.name;
            deployResultModel.phone = deployContactModel.phone;
        }
        deployResultModel.address = deployAnalyzerModel.address;
        deployResultModel.updateTime = deviceInfo.getUpdatedTime();
        deployResultModel.deviceStatus = deviceInfo.getStatus();
        deployResultModel.signal = deviceInfo.getSignal();
        deployResultModel.name = deployAnalyzerModel.nameAndAddress;
        intent.putExtra(EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
        getView().startAC(intent);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        deployAnalyzerModel.tagList.clear();
        deployAnalyzerModel.images.clear();
        mHandler.removeCallbacksAndMessages(null);

    }

    public void doNameAddress() {
        Intent intent = new Intent(mContext, DeployMonitorNameAddressActivity.class);
        if (!TextUtils.isEmpty(deployAnalyzerModel.nameAndAddress)) {
            intent.putExtra(EXTRA_SETTING_NAME_ADDRESS, deployAnalyzerModel.nameAndAddress);
        }
        intent.putExtra(EXTRA_DEPLOY_TO_SN, deployAnalyzerModel.sn);
        intent.putExtra(EXTRA_DEPLOY_TYPE, deployAnalyzerModel.deployType);
        if (!TextUtils.isEmpty(deployAnalyzerModel.nameAndAddress)) {
            intent.putExtra(EXTRA_DEPLOY_ORIGIN_NAME_ADDRESS, deployAnalyzerModel.nameAndAddress);
        }
        intent.putExtra(EXTRA_DEPLOY_TYPE, deployAnalyzerModel.deployType);
        getView().startAC(intent);
    }

    public void doTag() {
        Intent intent = new Intent(mContext, DeployDeviceTagActivity.class);
        if (deployAnalyzerModel.tagList.size() > 0) {
            intent.putStringArrayListExtra(EXTRA_SETTING_TAG_LIST, (ArrayList<String>) deployAnalyzerModel.tagList);
        }
        getView().startAC(intent);
    }

    public void doSettingPhoto() {
        Intent intent = new Intent(mContext, DeployMonitorDeployPicActivity.class);
        if (getRealImageSize() > 0) {
            intent.putExtra(EXTRA_DEPLOY_TO_PHOTO, deployAnalyzerModel.images);
        }
        intent.putExtra(EXTRA_SETTING_DEPLOY_DEVICE_TYPE, deployAnalyzerModel.deviceType);
        getView().startAC(intent);
    }

    public void doDeployMap() {
        Intent intent = new Intent();
        if (AppUtils.isChineseLanguage()) {
            intent.setClass(mContext, DeployMapActivity.class);
        } else {
            intent.setClass(mContext, DeployMapENActivity.class);
        }
        deployAnalyzerModel.mapSourceType = DEPLOY_MAP_SOURCE_TYPE_DEPLOY_MONITOR_DETAIL;
        intent.putExtra(EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
        getView().startAC(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData eventData) {
        int code = eventData.code;
        Object data = eventData.data;
        switch (code) {
            case EVENT_DATA_DEPLOY_RESULT_FINISH:
            case EVENT_DATA_DEPLOY_CHANGE_RESULT_CONTINUE:
            case EVENT_DATA_DEPLOY_RESULT_CONTINUE:
                getView().finishAc();
                break;
            case EVENT_DATA_DEPLOY_SETTING_NAME_ADDRESS:
                if (data instanceof String) {
                    deployAnalyzerModel.nameAndAddress = (String) data;
                    getView().setNameAddressText(deployAnalyzerModel.nameAndAddress);
                }
                getView().setUploadBtnStatus(checkCanUpload());
                break;
            case EVENT_DATA_DEPLOY_SETTING_TAG:
                if (data instanceof List) {
                    deployAnalyzerModel.tagList.clear();
                    deployAnalyzerModel.tagList.addAll((List<String>) data);
                    getView().updateTagsData(deployAnalyzerModel.tagList);
                }
                break;
            case EVENT_DATA_DEPLOY_SETTING_PHOTO:
                if (data instanceof List) {
                    deployAnalyzerModel.images.clear();

                    deployAnalyzerModel.images.addAll((ArrayList<ImageItem>) data);

                    if (getRealImageSize() > 0) {
                        getView().setDeployPhotoText(mContext.getString(R.string.added) + getRealImageSize() + mContext.getString(R.string.images));
                    } else {
                        getView().setDeployPhotoText(mContext.getString(R.string.not_added));
                    }
                    getView().setUploadBtnStatus(checkCanUpload());
                }
                break;
            case EVENT_DATA_DEPLOY_MAP:
                if (data instanceof DeployAnalyzerModel) {
                    this.deployAnalyzerModel = (DeployAnalyzerModel) data;
                    //TODO 刷新数据状态
                }
                getView().setDeployPosition(checkHasDeployPosition(), deployAnalyzerModel.address);
                getView().setUploadBtnStatus(checkCanUpload());
                break;
            default:
                break;
        }
    }

    private int getRealImageSize() {
        int count = 0;
        for (ImageItem image : deployAnalyzerModel.images) {
            if (image != null) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }

    public void doConfirm() {
        //姓名地址校验
//        switch (deployAnalyzerModel.deployType) {
////            case TYPE_SCAN_DEPLOY_STATION:
////                if (checkHasPhoto()) return;
////                //经纬度校验
////                if (checkHasNoLatLng()) return;
////                requestUpload();
////                break;
////            case TYPE_SCAN_DEPLOY_DEVICE:
////            case TYPE_SCAN_DEPLOY_INSPECTION_DEVICE_CHANGE:
////            case TYPE_SCAN_DEPLOY_MALFUNCTION_DEVICE_CHANGE:
////                if (checkHasPhoto()) return;
////                //经纬度校验
////                if (checkHasNoLatLng()) return;
////                boolean isFire = DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType);
////                if (isFire) {
////                    if (deployAnalyzerModel.settingData == null) {
////                        getView().toastShort(mContext.getString(R.string.deploy_has_no_configuration_tip));
////                        return;
////                    }
////                }
//////                if (checkNeedSignal()) {
//////                    checkHasForceUploadPermission();
//////                } else {
//////                    requestUpload();
//////                }
////
////                break;
////            default:
////                break;
////        }
        //TODO 强制上传
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
        //照片校验
        if (getRealImageSize() == 0) {
            return false;
        }
        //经纬度校验
        if (!checkHasDeployPosition()) {
            return false;
        }
        if (!checkHasDeployMethod()) {
            return false;
        }
        if (!checkHasDeployOrientation()) {
            return false;
        }
        return true;
    }

    /**
     * 检查是否能强制上传
     */
    private void checkHasForceUploadPermission() {
        String mergeType = WidgetUtil.handleMergeType(deployAnalyzerModel.deviceType);
        if (TextUtils.isEmpty(mergeType)) {
            getView().showWarnDialog(true);
        } else {
            if (Constants.DEPLOY_CAN_FOURCE_UPLOAD_PERMISSION_LIST.contains(mergeType)) {
                if (PreferencesHelper.getInstance().getUserData().hasBadSignalUpload) {
                    getView().showWarnDialog(true);
                } else {
                    getView().showWarnDialog(false);
                }
            } else {
                getView().showWarnDialog(true);
            }
        }
    }

    private boolean checkHasDeployMethod() {
        return mMethodConfig != null;
    }

    private boolean checkHasDeployOrientation() {
        return mOrientationConfig != null;
    }

    private boolean checkHasDeployPosition() {
        return deployAnalyzerModel.latLng.size() == 2;
    }


    @Override
    public void run() {
        //TODO
        mHandler.postDelayed(this, 2000);

    }


    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
    }

    public void doDeployMethod() {
        if (deployMethods.size() > 0) {
            handleMethod();
        } else {
            getView().showProgressDialog();
            RetrofitServiceHelper.getInstance().getCameraFilter().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<CameraFilterRsp>(this) {
                @Override
                public void onCompleted(CameraFilterRsp cameraFilterRsp) {
                    List<CameraFilterModel> data = cameraFilterRsp.getData();
                    if (data != null) {
                        for (CameraFilterModel cameraFilterModel : data) {
                            String key = cameraFilterModel.getKey();
                            if ("installationMode".equalsIgnoreCase(key)) {
                                //安装方式
                                List<CameraFilterModel.ListBean> list = cameraFilterModel.getList();
                                if (list != null) {
                                    for (CameraFilterModel.ListBean listBean : list) {
                                        DeployCameraConfigModel deployCameraConfigModel = new DeployCameraConfigModel();
                                        deployCameraConfigModel.name = listBean.getName();
                                        deployCameraConfigModel.code = listBean.getCode();
                                        deployMethods.add(deployCameraConfigModel);
                                    }
                                }
                            }
                        }
                    }
                    handleMethod();
                    getView().dismissProgressDialog();
                }

                @Override
                public void onErrorMsg(int errorCode, String errorMsg) {
                    getView().dismissProgressDialog();
                    getView().toastShort(errorMsg);
                }
            });
        }
    }

    public void doDeployOrientation() {
        if (deployOrientations.size() > 0) {
            handleOrientation();
        } else {
            getView().showProgressDialog();
            RetrofitServiceHelper.getInstance().getCameraFilter().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<CameraFilterRsp>(this) {
                @Override
                public void onCompleted(CameraFilterRsp cameraFilterRsp) {
                    List<CameraFilterModel> data = cameraFilterRsp.getData();
                    if (data != null) {
                        for (CameraFilterModel cameraFilterModel : data) {
                            String key = cameraFilterModel.getKey();
                            if ("orientation".equalsIgnoreCase(key)) {
                                List<CameraFilterModel.ListBean> list = cameraFilterModel.getList();
                                if (list != null) {
                                    for (CameraFilterModel.ListBean listBean : list) {
                                        DeployCameraConfigModel deployCameraConfigModel = new DeployCameraConfigModel();
                                        deployCameraConfigModel.code = listBean.getCode();
                                        deployCameraConfigModel.name = listBean.getName();
                                        deployOrientations.add(deployCameraConfigModel);
                                    }
                                }
                                //安装朝向
                            }
                        }
                    }
                    handleOrientation();
                    getView().dismissProgressDialog();
                }

                @Override
                public void onErrorMsg(int errorCode, String errorMsg) {
                    getView().dismissProgressDialog();
                    getView().toastShort(errorMsg);
                }
            });
        }

    }

    //处理安装朝向
    private void handleOrientation() {
        ArrayList<String> strings = new ArrayList<>();
        for (DeployCameraConfigModel deployMethod : deployMethods) {
            strings.add(deployMethod.name);
        }
        AppUtils.showDialog(mContext, new SelectDialog.SelectDialogListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mOrientationConfig = deployOrientations.get(position);
                String orientation = mOrientationConfig.name;
                getView().setDeployOrientation(orientation);
            }
        }, strings);
    }

    //处理安装方式
    private void handleMethod() {
        ArrayList<String> strings = new ArrayList<>();
        for (DeployCameraConfigModel deployMethod : deployMethods) {
            strings.add(deployMethod.name);
        }
        AppUtils.showDialog(mContext, new SelectDialog.SelectDialogListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mMethodConfig = deployMethods.get(position);
                String method = mMethodConfig.name;
                getView().setDeployMethod(method);
            }
        }, strings);
    }
}
