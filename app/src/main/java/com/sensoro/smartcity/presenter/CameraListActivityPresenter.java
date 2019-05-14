package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.activity.CameraDetailActivity;
import com.sensoro.smartcity.analyzer.PreferencesSaveAnalyzer;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.constant.SearchHistoryTypeConstants;
import com.sensoro.smartcity.imainviews.ICameraListActivityView;
import com.sensoro.smartcity.model.CameraFilterModel;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.DeviceCameraDetailInfo;
import com.sensoro.smartcity.server.bean.DeviceCameraInfo;
import com.sensoro.smartcity.server.response.CameraFilterRsp;
import com.sensoro.smartcity.server.response.DeviceCameraDetailRsp;
import com.sensoro.smartcity.server.response.DeviceCameraListRsp;
import com.sensoro.smartcity.util.PreferencesHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CameraListActivityPresenter extends BasePresenter<ICameraListActivityView> implements Constants {
    private Activity mContext;
    private volatile int cur_page = 1;


    private final List<DeviceCameraInfo> deviceCameraInfos = new ArrayList<>();
    private final List<String> mSearchHistoryList = new ArrayList<>();

    public List<CameraFilterModel> getCameraFilterModelList() {
        return cameraFilterModelList;
    }

    private final List<CameraFilterModel> cameraFilterModelList = new ArrayList<>();


    public final HashMap selectedHashMap = new HashMap();

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        Serializable serializableExtra = mContext.getIntent().getSerializableExtra(EXTRA_DEVICE_CAMERA_DETAIL_INFO_LIST);
        if (serializableExtra instanceof ArrayList) {
            getView().setSmartRefreshEnable(false);
            deviceCameraInfos.clear();
            List<DeviceCameraInfo> data = (List<DeviceCameraInfo>) serializableExtra;
            deviceCameraInfos.addAll(data);
            getView().updateDeviceCameraAdapter(deviceCameraInfos);
            getView().onPullRefreshComplete();
            getView().dismissProgressDialog();
            getView().setTopTitleState();
        } else {
            requestDataByFilter(DIRECTION_DOWN, null);
        }
        List<String> list = PreferencesHelper.getInstance().getSearchHistoryData(SearchHistoryTypeConstants.TYPE_SEARCH_CAMERALIST);
        if (list != null) {
            mSearchHistoryList.addAll(list);
            getView().updateSearchHistoryList(mSearchHistoryList);
        }
    }

    public void save(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        List<String> warnList = PreferencesSaveAnalyzer.handleDeployRecord(SearchHistoryTypeConstants.TYPE_SEARCH_CAMERALIST, text);
        mSearchHistoryList.clear();
        mSearchHistoryList.addAll(warnList);
        getView().updateSearchHistoryList(mSearchHistoryList);

    }

    public void clearSearchHistory() {
        PreferencesSaveAnalyzer.clearAllData(SearchHistoryTypeConstants.TYPE_SEARCH_CAMERALIST);
        mSearchHistoryList.clear();
        getView().updateSearchHistoryList(mSearchHistoryList);
    }

    @Override
    public void onDestroy() {
        selectedHashMap.clear();
    }

    public void getFilterPopData() {
        getView().showProgressDialog();
        RetrofitServiceHelper.getInstance().getCameraFilter().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<CameraFilterRsp>(this) {
            @Override
            public void onCompleted(CameraFilterRsp cameraFilterRsp) {
                List<CameraFilterModel> data = cameraFilterRsp.getData();
                if (data != null) {
                    cameraFilterModelList.addAll(data);
                    getView().updateFilterPop(data);
                }
                getView().dismissProgressDialog();
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);
            }
        });

    }

    public void onClickDeviceCamera(final DeviceCameraInfo deviceCameraInfo) {
        final String sn = deviceCameraInfo.getSn();
        final String cid = deviceCameraInfo.getCid();
        getView().showProgressDialog();
        RetrofitServiceHelper.getInstance().getDeviceCamera(sn).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceCameraDetailRsp>(this) {
            @Override
            public void onCompleted(DeviceCameraDetailRsp deviceCameraDetailRsp) {
                DeviceCameraDetailInfo data = deviceCameraDetailRsp.getData();
                if (data != null) {
                    String hls = data.getHls();
                    DeviceCameraDetailInfo.CameraBean camera = data.getCamera();
                    String lastCover = data.getLastCover();
                    Intent intent = new Intent();
                    intent.setClass(mContext, CameraDetailActivity.class);
                    intent.putExtra("cid", cid);
                    intent.putExtra("hls", hls);
                    intent.putExtra("sn", sn);
                    if (camera != null) {
                        String name = camera.getName();
                        intent.putExtra("cameraName", name);
                    }
                    intent.putExtra("lastCover", lastCover);
                    intent.putExtra("deviceStatus", data.getDeviceStatus());
                    getView().startAC(intent);
                } else {
                    getView().toastShort(mContext.getString(R.string.camera_info_get_failed));

                }
                getView().dismissProgressDialog();

            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);
            }
        });

    }


    public void requestDataByFilter(final int direction, String search) {
        switch (direction) {
            case DIRECTION_DOWN:
                cur_page = 1;
                if (isAttachedView()) {
                    getView().showProgressDialog();
                }
                RetrofitServiceHelper.getInstance().getDeviceCameraListByFilter(20, cur_page, search, selectedHashMap).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceCameraListRsp>(this) {
                    @Override
                    public void onCompleted(DeviceCameraListRsp deviceCameraListRsp) {

                        List<DeviceCameraInfo> data = deviceCameraListRsp.getData();
                        deviceCameraInfos.clear();
                        if (data != null && data.size() > 0) {
                            deviceCameraInfos.addAll(data);
                        }
                        getView().updateDeviceCameraAdapter(deviceCameraInfos);
                        getView().onPullRefreshComplete();
                        getView().dismissProgressDialog();
                    }

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                        getView().onPullRefreshComplete();

                    }
                });
                break;
            case DIRECTION_UP:
                cur_page++;
                if (isAttachedView()) {
                    getView().showProgressDialog();
                }
                RetrofitServiceHelper.getInstance().getDeviceCameraListByFilter(20, cur_page, search, selectedHashMap).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceCameraListRsp>(this) {
                    @Override
                    public void onCompleted(DeviceCameraListRsp deviceCameraListRsp) {

                        List<DeviceCameraInfo> data = deviceCameraListRsp.getData();
                        if (data != null && data.size() > 0) {
                            deviceCameraInfos.addAll(data);
                            getView().updateDeviceCameraAdapter(deviceCameraInfos);
                        } else {
                            getView().toastShort(mContext.getString(R.string.no_more_data));
                        }
                        getView().onPullRefreshComplete();
                        getView().dismissProgressDialog();
                    }

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                        getView().onPullRefreshComplete();

                    }
                });
                break;
            default:
                break;

        }

    }
}