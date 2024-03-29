package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;

import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.server.bean.DeviceCameraFacePic;
import com.sensoro.common.widgets.CalendarPopUtils;
import com.sensoro.smartcity.imainviews.IMutilCameraView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class MutilCamerPresenter extends BasePresenter<IMutilCameraView> {
    private Activity mActivity;
    private String cid;
    private String minId = null;
    private String yearMonthDate;
    private String url;
    private String flv;
    private long startDateTime;
    private long endDateTime;
    private CalendarPopUtils mCalendarPopUtils;
    private long time;
    private String mCameraName, lastCover;
    private String itemTitle;
    private String itemUrl;
    /**
     * 摄像机是否在线
     */
    private String deviceStatus;
    private String sn;
    private ArrayList<DeviceCameraFacePic> mLists = new ArrayList<>();
    private ArrayList<String> urlList = new ArrayList<>();

    @Override
    public void initData(Context context) {
        EventBus.getDefault().register(this);

    }

    @Override
    public void onDestroy() {

        EventBus.getDefault().unregister(this);
    }


//    /**
//     * 网络改变状态
//     *
//     * @param eventData
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent(EventData eventData) {
//        int code = eventData.code;
//        if (code == Constants.NetworkInfo) {
//            int data = (int) eventData.data;
//            switch (data) {
//                case ConnectivityManager.TYPE_WIFI:
//                    setCityPlayState(-1);
//                    if (getCurrentState() == CURRENT_STATE_PAUSE) {
//                        clickCityStartIcon();
//                        CustomManager.onResumeAll();
//
//                    } else {
//                        if (!TextUtils.isEmpty(mUrl)) {
//                            setUp(mUrl, false, TextUtils.isEmpty(mTitle) ? "" : mTitle);
//                            startPlayLogic();
//                        }
//                    }
//                    break;
//                case ConnectivityManager.TYPE_MOBILE:
//                    setCityPlayState(2);
//                    getPlayAndRetryBtn().setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            setCityPlayState(-1);
//                            if (getCurrentState() == CURRENT_STATE_PAUSE) {
//                                clickCityStartIcon();
//
//                            }
//
//
//                        }
//                    });
//
//
//                    break;
//
//                default:
//                    CustomManager.onPauseAll();
//
//
//                    setCityPlayState(1);
//                    if (mOrientationUtils != null) {
//                        mOrientationUtils.backToProtVideo();
//                        mOrientationUtils.setEnable(false);
//                    }
//                    if (GSYVideoManager.backFromWindowFull(mContext)) {
//                        return;
//                    }
//                    break;
//
//
//            }
//        } else if (code == Constants.VIDEO_START) {
//            if (getCurrentState() == CURRENT_STATE_PAUSE) {
//                clickCityStartIcon();
//                GSYVideoManager.onResume(true);
//            } else {
//                if (!TextUtils.isEmpty(mUrl)) {
//                    setUp(mUrl, false, TextUtils.isEmpty(mTitle) ? "" : mTitle);
//                    startPlayLogic();
//                }
//            }
//        } else if (code == Constants.VIDEO_STOP) {
//            GSYVideoManager.onPause();
//
//            if (mOrientationUtils != null) {
//                mOrientationUtils.backToProtVideo();
//                mOrientationUtils.setEnable(false);
//            }
//            if (GSYVideoManager.backFromWindowFull(mContext)) {
//                return;
//            }
//
//        }
//    }

//    @Override
//    public void initData(Context context) {
//        mActivity = (Activity) context;
//        EventBus.getDefault().register(this);
//        Intent intent = mActivity.getIntent();
//        if (intent != null) {
//            cid = intent.getStringExtra("cid");
//            url = intent.getStringExtra("hls");
//            flv = intent.getStringExtra("flv");
//            urlList.add(flv);
//            urlList.add(url);
//            mCameraName = intent.getStringExtra("cameraName");
//            lastCover = intent.getStringExtra("lastCover");
//
//            getView().loadCoverImage(lastCover);
//            deviceStatus = intent.getStringExtra("deviceStatus");
//            sn = intent.getStringExtra("sn");
//
//        }
//
//        getView().showProgressDialog();
//        getView().setLiveState(true);
//        doLive();
//        requestData(cid, Constants.DIRECTION_DOWN);
//        mCalendarPopUtils = new CalendarPopUtils(mActivity);
//        mCalendarPopUtils.setMonthStatus(1)
//                .setRangeStatus(1)
//                .isDefaultSelectedCurDay(false);
//        mCalendarPopUtils.setOnCalendarPopupCallbackListener(this);
//    }
//
//
//    private void requestData(String cid, final int direction) {
//
//        if (direction == Constants.DIRECTION_DOWN) {
//            minId = null;
//            if (isAttachedView() && TextUtils.isEmpty(itemUrl)) {
//                getView().setLiveState(true);
//                doLive();
//                getView().clearClickPosition();
//            }
//
//        }
//        ArrayList<String> strings = new ArrayList<>();
//        strings.add(cid);
//        long currentTimeMillis = System.currentTimeMillis();
//        String startTime;
//        String endTime;
//        if (startDateTime == 0 || endDateTime == 0) {
//            //后台存的人脸图片，只保留30天，所以这里请求30天的
//            startTime = String.valueOf(currentTimeMillis - 24 * 60 * 60 * 30 * 1000L);
//            endTime = String.valueOf(currentTimeMillis);
//        } else {
//            startTime = String.valueOf(startDateTime);
//            endTime = String.valueOf(endDateTime);
//        }
//        //获取图片是根绝minID向后推limit个条目，服务器做的限定
//        RetrofitServiceHelper.getInstance().getDeviceCameraFaceList(strings, null, Constants.DEFAULT_PAGE_SIZE, minId, startTime, endTime)
//                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<List<DeviceCameraFacePic>>>(this) {
//            @Override
//            public void onCompleted(final ResponseResult<List<DeviceCameraFacePic>> deviceCameraFacePicListRsp) {
//                List<DeviceCameraFacePic> data = deviceCameraFacePicListRsp.getData();
//                if (data != null) {
//                    if (data.size() > 0) {
//                        minId = data.get(data.size() - 1).getId();
//                        if (direction == Constants.DIRECTION_DOWN) {
//                            mLists.clear();
//                            if (isAttachedView()) {
//                                getView().onPullRefreshComplete();
//                                getView().updateCameraList(data);
//                            }
//                        } else {
//                            mLists.addAll(data);
//                            if (isAttachedView()) {
//                                getView().onPullRefreshComplete();
//                                getView().updateCameraList(mLists);
//                            }
//                        }
//                    } else {
//
//                        if (direction == Constants.DIRECTION_UP) {
//                            if (isAttachedView()) {
//                                getView().onPullRefreshComplete();
//                                getView().toastShort(mActivity.getString(R.string.no_more_data));
//                            }
//                        } else {
//                            mLists.clear();
//                            if (isAttachedView()) {
//                                getView().onPullRefreshComplete();
//                                getView().updateCameraList(mLists);
//                            }
//                        }
//                    }
//
//
//                }
//                if (isAttachedView()) {
//                    getView().dismissProgressDialog();
//                }
//
//
//            }
//
//            @Override
//            public void onErrorMsg(int errorCode, String errorMsg) {
//                if (isAttachedView()) {
//                    getView().toastShort(errorMsg);
//                    getView().dismissProgressDialog();
//                    getView().onPullRefreshComplete();
//                }
//
//            }
//        });
//    }
//
//
//    @Override
//    public void onDestroy() {
//        EventBus.getDefault().unregister(this);
//    }
//
//
//    public void onCameraItemClick(final int index) {
//        if (isAttachedView()) {
//            List<DeviceCameraFacePic> rvListData = getView().getRvListData();
//            if (rvListData != null) {
//
//                DeviceCameraFacePic model = rvListData.get(index);
//                String captureTime1 = model.getCaptureTime();
//
//                getView().loadCoverImage(model.getSceneUrl());
//                long time;
//                try {
//                    time = Long.parseLong(captureTime1);
//                } catch (NumberFormatException e) {
//                    e.printStackTrace();
//                    getView().toastShort(mActivity.getString(R.string.time_parse_error));
//                    return;
//                }
//
//                //7天以外没有视频，所以显示没有视频，
//                if (System.currentTimeMillis() - 24 * 3600 * 1000 * 7L > time) {
//                    getView().setGsyVideoNoVideo();
//                    return;
//                }
//                itemTitle = DateUtil.getStrTime_MM_dd_hms(time);
//                time = time / 1000;
//
//                String beginTime = String.valueOf(time - 15);
//                String endTime = String.valueOf(time + 15);
//                getView().showProgressDialog();
//                RetrofitServiceHelper.getInstance().getDeviceCameraPlayHistoryAddress(cid, beginTime, endTime, null).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<List<DeviceCameraHistoryBean>>>(this) {
//                    @Override
//                    public void onCompleted(ResponseResult<List<DeviceCameraHistoryBean>> deviceCameraHistoryRsp) {
//                        if (index != getView().getCurrentClickPosition()) {
//                            return;
//                        }
//                        List<DeviceCameraHistoryBean> data = deviceCameraHistoryRsp.getData();
//                        if (data != null && data.size() > 0) {
//                            DeviceCameraHistoryBean deviceCameraHistoryBean = data.get(0);
//                            itemUrl = deviceCameraHistoryBean.getUrl();
//
//                            if (isAttachedView()) {
//                                getView().startPlayLogic(itemUrl, itemTitle);
//                            }
//
//                        } else {
//                            itemUrl = "";
//                            if (isAttachedView()) {
//                                getView().startPlayLogic(itemUrl, itemTitle);
//                            }
//                        }
//
//                        if (isAttachedView()) {
//                            getView().dismissProgressDialog();
//                        }
//                    }
//
//                    @Override
//                    public void onErrorMsg(int errorCode, String errorMsg) {
//                        if (isAttachedView()) {
//                            getView().playError(index);
//                            getView().dismissProgressDialog();
//                        }
//                    }
//                });
//
//
//            }
//        }
//    }
//
//    //无动画效果关闭日历弹框
//    public void doDissmissCalendar() {
//        mCalendarPopUtils.dismissNoAnimation();
//    }
//
//    public void doCalendar(LinearLayout root) {
//        long temp_startTime = -1;
//        long temp_endTime = -1;
//        if (getView().isSelectedDateLayoutVisible()) {
//            temp_startTime = startDateTime;
//            temp_endTime = endDateTime;
//        }
//        mCalendarPopUtils.showFalseClip(root, temp_startTime, temp_endTime);
//
//
//    }
//
//
//    public void doRefresh() {
//        requestData(cid, Constants.DIRECTION_DOWN);
//    }
//
//    public void doLoadMore() {
//        requestData(cid, Constants.DIRECTION_UP);
//    }
//
//    public void doLive() {
//        if (isAttachedView()) {
//
//
//            if (!TextUtils.isEmpty(deviceStatus) && "0".equals(deviceStatus)) {
//                getView().offlineType(mCameraName);
//            } else {
//                getView().doPlayLive(urlList, TextUtils.isEmpty(mCameraName) ? "" : mCameraName, true);
//
//                getView().loadCoverImage(lastCover);
//
//                itemUrl = null;
//                itemTitle = null;
//            }
//        }
//    }
//
//
//    @Override
//    public void onCalendarPopupCallback(CalendarDateModel calendarDateModel) {
//        getView().setSelectedDateLayoutVisible(true);
//        startDateTime = DateUtil.strToDate(calendarDateModel.startDate).getTime();
//        endDateTime = DateUtil.strToDate(calendarDateModel.endDate).getTime();
//        if (startDateTime == endDateTime) {
//            getView().setSelectedDateSearchText(DateUtil.getCalendarYearMothDayFormatDate(startDateTime));
//        } else {
//            getView().setSelectedDateSearchText(DateUtil.getCalendarYearMothDayFormatDate(startDateTime) + " ~ " + DateUtil
//                    .getCalendarYearMothDayFormatDate(endDateTime));
//        }
//
//        endDateTime += 1000 * 60 * 60 * 24;
//
//        getView().showProgressDialog();
//        requestData(cid, Constants.DIRECTION_DOWN);
//    }
//
//    public void doRequestData() {
//        startDateTime = 0;
//        endDateTime = 0;
//        getView().showProgressDialog();
//        requestData(cid, Constants.DIRECTION_DOWN);
//    }
//
//    public void doPersonAvatarHistory(int position) {
//        DeviceCameraFacePic model = getView().getItemData(position);
//        Intent intent = new Intent();
//        intent.putExtra(Constants.EXTRA_PERSON_AVATAR_HISTORY_FACE_ID, model.getId());
//        intent.putExtra(Constants.EXTRA_CAMERA_PERSON_AVATAR_HISTORY_FACE_URL, model.getFaceUrl());
//        intent.setClass(mActivity, CameraPersonAvatarHistoryActivity.class);
//        getView().startAC(intent);
//
//    }
//
//    public void doOnRestart() {
//        if (itemUrl == null) {
//            doLive();
//        } else {
//            if (getView().getPlayView().getCurrentState() == CURRENT_STATE_PAUSE) {
//                GSYVideoManager.onResume(false);
//            } else if (getView().getPlayView().getCurrentState() != CURRENT_STATE_AUTO_COMPLETE) {
//
//                getView().startPlayLogic(itemUrl, itemTitle);
//
//            }
//        }
//    }
//
//    /**
//     * 重新获取摄像头状态
//     */
//    public void regainGetCameraState() {
//        getView().showProgressDialog();
//        RetrofitServiceHelper.getInstance().getDeviceCamera(sn).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceCameraDetailInfo>>(this) {
//            @Override
//            public void onCompleted(ResponseResult<DeviceCameraDetailInfo> deviceCameraDetailRsp) {
//                DeviceCameraDetailInfo data = deviceCameraDetailRsp.getData();
//                if (data != null) {
//                    String hls = data.getHls();
//                    String lastCover = data.getLastCover();
//
//                    url = hls;
//
//                    getView().loadCoverImage(lastCover);
//                    deviceStatus = data.getDeviceStatus();
//                    if (!TextUtils.isEmpty(deviceStatus) && "0".equals(deviceStatus)) {
//                        if (!TextUtils.isEmpty(itemTitle)) {
//                            getView().offlineType(itemTitle);
//
//                        } else {
//                            getView().offlineType(mCameraName);
//
//                        }
//                    } else {
//                        doLive();
//                    }
//
//                } else {
//                    getView().toastShort(mActivity.getString(R.string.camera_info_get_failed));
//
//                }
//                getView().dismissProgressDialog();
//
//            }
//
//            @Override
//            public void onErrorMsg(int errorCode, String errorMsg) {
//                getView().dismissProgressDialog();
//                getView().toastShort(errorMsg);
//            }
//        });
//
//    }


}
