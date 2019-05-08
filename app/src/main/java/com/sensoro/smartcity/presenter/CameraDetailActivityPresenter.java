package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.activity.CameraPersonAvatarHistoryActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.ICameraDetailActivityView;
import com.sensoro.smartcity.model.CalendarDateModel;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.DeviceCameraFacePic;
import com.sensoro.smartcity.server.bean.DeviceCameraHistoryBean;
import com.sensoro.smartcity.server.response.DeviceCameraFacePicListRsp;
import com.sensoro.smartcity.server.response.DeviceCameraHistoryRsp;
import com.sensoro.smartcity.util.DateUtil;
import com.sensoro.smartcity.widget.popup.CalendarPopUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CameraDetailActivityPresenter extends BasePresenter<ICameraDetailActivityView> implements
        CalendarPopUtils.OnCalendarPopupCallbackListener {
    private Activity mActivity;
    private String cid;
    private String minId = null;
    private String yearMonthDate;
    private String url;
    private long startDateTime;
    private long endDateTime;
    private CalendarPopUtils mCalendarPopUtils;
    private long time;
    private String mCameraName, lastCover;
    private String itemTitle;
    private String itemUrl;
    private ArrayList<DeviceCameraFacePic> mLists = new ArrayList<>();
    ;

    @Override
    public void initData(Context context) {
        mActivity = (Activity) context;
        Intent intent = mActivity.getIntent();


        url = Constants.LIVE_URL;
        if (intent != null) {
            cid = intent.getStringExtra("cid");
            url = intent.getStringExtra("hls");
            mCameraName = intent.getStringExtra("cameraName");
            lastCover = intent.getStringExtra("lastCover");
            getLastCoverImage(lastCover);


            String deviceStatus = intent.getStringExtra("deviceStatus");
            if (!TextUtils.isEmpty(deviceStatus) && "0".equals(deviceStatus)) {

                getView().offlineType(url);
            }

        }

        doLive();
        getView().showProgressDialog();
        requestData(cid, Constants.DIRECTION_DOWN);
        mCalendarPopUtils = new CalendarPopUtils(mActivity);
        mCalendarPopUtils.setMonthStatus(1)
                .setRangeStatus(1)
                .isDefaultSelectedCurDay(false);
        mCalendarPopUtils.setOnCalendarPopupCallbackListener(this);
    }

    private void getLastCoverImage(String lastCover) {
        Glide.with(mActivity).load(lastCover).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                BitmapDrawable bitmapDrawable = new BitmapDrawable(resource);
                getView().setImage(bitmapDrawable);
            }
        });
    }

    private void requestData(String cid, final int direction) {
        if (direction == Constants.DIRECTION_DOWN) {
            minId = null;
            getView().setLiveState(true);
            doLive();
            getView().clearClickPosition();
        }
        ArrayList<String> strings = new ArrayList<>();
        strings.add(cid);
        long currentTimeMillis = System.currentTimeMillis();
        String startTime;
        String endTime;
        if (startDateTime == 0 || endDateTime == 0) {
            startTime = String.valueOf(currentTimeMillis - 24 * 60 * 60 * 30 * 1000L);
            endTime = String.valueOf(currentTimeMillis);
        } else {
            startTime = String.valueOf(startDateTime);
            endTime = String.valueOf(endDateTime);
        }

        RetrofitServiceHelper.getInstance().getDeviceCameraFaceList(strings, null, 20, minId, startTime, endTime)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceCameraFacePicListRsp>(null) {
            @Override
            public void onCompleted(final DeviceCameraFacePicListRsp deviceCameraFacePicListRsp) {
                List<DeviceCameraFacePic> data = deviceCameraFacePicListRsp.getData();
                if (data != null) {
                    if (data.size() > 0) {
                        minId = data.get(data.size() - 1).getId();
                        if (direction == Constants.DIRECTION_DOWN) {
                            mLists.clear();
                            if (isAttachedView()) {
                                getView().onPullRefreshComplete();
                                getView().updateCameraList(data);
                            }
                        } else {
                            mLists.addAll(data);
                            if (isAttachedView()) {
                                getView().onPullRefreshComplete();
                                getView().updateCameraList(mLists);
                            }
                        }
                    } else if (direction == Constants.DIRECTION_UP) {
                        if (isAttachedView()) {
                            getView().toastShort(mActivity.getString(R.string.no_more_data));
                            getView().onPullRefreshCompleteNoMoreData();
                        }
                    }

                }
                if (isAttachedView()) {
                    getView().dismissProgressDialog();
                }


            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                if (isAttachedView()) {
                    getView().dismissProgressDialog();
                }

            }
        });
    }


    private boolean isYesterday(String currentDate, String ymd) {
        String[] currentSplit = currentDate.split("-");
        String[] ymdSplit = ymd.split("-");
        if (currentSplit.length == 3 && ymdSplit.length == 3) {
            try {
                int currentInt = Integer.parseInt(currentSplit[2]);
                int ymdInt = Integer.parseInt(ymdSplit[2]);

                return currentInt - ymdInt == 1 && currentSplit[1].equals(ymdSplit[1]) && currentSplit[0].equals(ymdSplit[0]);

            } catch (NumberFormatException e) {
                e.printStackTrace();
                return false;
            }

        }

        return false;
    }

    @Override
    public void onDestroy() {
    }

    private void setLastCover(DeviceCameraFacePic model) {
        Glide.with(mActivity).load(Constants.CAMERA_BASE_URL + model.getSceneUrl())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存全尺寸
                .into(getView().getImageView());

    }

    public void onCameraItemClick(final int index) {
        List<DeviceCameraFacePic> rvListData = getView().getRvListData();
        if (rvListData != null) {

            DeviceCameraFacePic model = rvListData.get(index);
            String captureTime1 = model.getCaptureTime();
            getLastCoverImage(Constants.CAMERA_BASE_URL + model.getSceneUrl());

            setLastCover(model);
            long time;
            try {
                time = Long.parseLong(captureTime1);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                getView().toastShort(mActivity.getString(R.string.time_parse_error));
                return;
            }

            //7天以为没有视频，所以显示没有视频，
            if (System.currentTimeMillis() - 24 * 3600 * 1000 * 7L > time) {
                getView().setGsyVideoNoVideo();
                return;
            }
            itemTitle = DateUtil.getStrTime_MM_dd_hms(time);
            time = time / 1000;

            String beginTime = String.valueOf(time - 15);
            String endTime = String.valueOf(time + 15);
            getView().showProgressDialog();
            RetrofitServiceHelper.getInstance().getDeviceCameraPlayHistoryAddress(cid, beginTime, endTime, null).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceCameraHistoryRsp>(null) {
                @Override
                public void onCompleted(DeviceCameraHistoryRsp deviceCameraHistoryRsp) {
                    List<DeviceCameraHistoryBean> data = deviceCameraHistoryRsp.getData();
                    if (data != null && data.size() > 0) {
                        DeviceCameraHistoryBean deviceCameraHistoryBean = data.get(0);
                        itemUrl = deviceCameraHistoryBean.getUrl();

                        if (isAttachedView()) {
                            getView().startPlayLogic(itemUrl, itemTitle);
                        }

                    }

                    if (isAttachedView()) {
                        getView().dismissProgressDialog();
                    }
                }

                @Override
                public void onErrorMsg(int errorCode, String errorMsg) {
                    if (isAttachedView()) {
                        getView().playError(index);
                        getView().dismissProgressDialog();
                    }
                }
            });


        }
    }

    public void doCalendar(LinearLayout root) {
        long temp_startTime = -1;
        long temp_endTime = -1;
        if (getView().isSelectedDateLayoutVisible()) {
            temp_startTime = startDateTime;
            temp_endTime = endDateTime;
        }
        mCalendarPopUtils.show(root, temp_startTime, temp_endTime);


    }


    public void doRefresh() {
        requestData(cid, Constants.DIRECTION_DOWN);
    }

    public void doLoadMore() {
        requestData(cid, Constants.DIRECTION_UP);
    }

    public void doLive() {
        getView().doPlayLive(url, TextUtils.isEmpty(mCameraName) ? "" : mCameraName, true);
        itemUrl = null;
        itemTitle = null;
    }

    @Override
    public void onCalendarPopupCallback(CalendarDateModel calendarDateModel) {
        getView().setSelectedDateLayoutVisible(true);
        startDateTime = DateUtil.strToDate(calendarDateModel.startDate).getTime();
        endDateTime = DateUtil.strToDate(calendarDateModel.endDate).getTime();
        getView().setSelectedDateSearchText(DateUtil.getCalendarYearMothDayFormatDate(startDateTime) + " ~ " + DateUtil
                .getCalendarYearMothDayFormatDate(endDateTime));
        endDateTime += 1000 * 60 * 60 * 24;

        getView().showProgressDialog();
        requestData(cid, Constants.DIRECTION_DOWN);
    }

    public void doRequestData() {
        startDateTime = 0;
        endDateTime = 0;
        getView().showProgressDialog();
        requestData(cid, Constants.DIRECTION_DOWN);
    }

    public void doPersonAvatarHistory(int position) {
        DeviceCameraFacePic model = getView().getItemData(position);
        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_PERSON_AVATAR_HISTORY_FACE_ID, model.getId());
        intent.putExtra(Constants.EXTRA_CAMERA_PERSON_AVATAR_HISTORY_FACE_URL, model.getFaceUrl());
        intent.setClass(mActivity, CameraPersonAvatarHistoryActivity.class);
        getView().startAC(intent);

    }

    public void doOnRestart() {
        if (itemUrl == null) {
            doLive();
        } else {
            getView().doPlayLive(itemUrl, TextUtils.isEmpty(itemTitle) ? "" : itemTitle, false);
        }
    }
}
