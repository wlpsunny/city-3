package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.activity.AlarmDetailActivity;
import com.sensoro.smartcity.activity.CalendarActivity;
import com.sensoro.smartcity.activity.SearchAlarmActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.IAlarmListFragmentView;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.AlarmInfo;
import com.sensoro.smartcity.server.bean.DeviceAlarmLogInfo;
import com.sensoro.smartcity.server.response.CityObserver;
import com.sensoro.smartcity.server.response.DeviceAlarmLogRsp;
import com.sensoro.smartcity.util.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AlarmListFragmentPresenter extends BasePresenter<IAlarmListFragmentView> implements Constants {
    private final List<DeviceAlarmLogInfo> mDeviceAlarmLogInfoList = new ArrayList<>();
    private volatile int cur_page = 1;
    private long startTime;
    private long endTime;
    private Activity mContext;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
    }

    public void freshUI(int direction, DeviceAlarmLogRsp deviceAlarmLogRsp, String searchText) {
        if (direction == DIRECTION_DOWN) {
            mDeviceAlarmLogInfoList.clear();
        }
        handleDeviceAlarmLogs(deviceAlarmLogRsp);
        if (!TextUtils.isEmpty(searchText)) {
            getView().setAlarmSearchText(searchText);
        }
        getView().updateAlarmListAdapter(mDeviceAlarmLogInfoList);
    }

    /**
     * 处理接收的数据
     *
     * @param deviceAlarmLogRsp
     */
    private void handleDeviceAlarmLogs(DeviceAlarmLogRsp deviceAlarmLogRsp) {
        List<DeviceAlarmLogInfo> deviceAlarmLogInfoList = deviceAlarmLogRsp.getData();
        for (int i = 0; i < deviceAlarmLogInfoList.size(); i++) {
            DeviceAlarmLogInfo deviceAlarmLogInfo = deviceAlarmLogInfoList.get(i);
            AlarmInfo.RecordInfo[] recordInfoArray = deviceAlarmLogInfo.getRecords();
            boolean isHaveRecovery = false;
            for (int j = 0; j < recordInfoArray.length; j++) {
                AlarmInfo.RecordInfo recordInfo = recordInfoArray[j];
                if (recordInfo.getType().equals("recovery")) {
                    deviceAlarmLogInfo.setSort(4);
                    isHaveRecovery = true;
                    break;
                } else {
                    deviceAlarmLogInfo.setSort(1);
                }
            }
            switch (deviceAlarmLogInfo.getDisplayStatus()) {
                case DISPLAY_STATUS_CONFIRM:
                    if (isHaveRecovery) {
                        deviceAlarmLogInfo.setSort(2);
                    } else {
                        deviceAlarmLogInfo.setSort(1);
                    }
                    break;
                case DISPLAY_STATUS_ALARM:
                    if (isHaveRecovery) {
                        deviceAlarmLogInfo.setSort(2);
                    } else {
                        deviceAlarmLogInfo.setSort(1);
                    }
                    break;
                case DISPLAY_STATUS_MISDESCRIPTION:
                    if (isHaveRecovery) {
                        deviceAlarmLogInfo.setSort(3);
                    } else {
                        deviceAlarmLogInfo.setSort(1);
                    }
                    break;
                case DISPLAY_STATUS_TEST:
                    if (isHaveRecovery) {
                        deviceAlarmLogInfo.setSort(4);
                    } else {
                        deviceAlarmLogInfo.setSort(1);
                    }
                    break;
                default:
                    break;
            }
            mDeviceAlarmLogInfoList.add(deviceAlarmLogInfo);
        }
        //            Collections.sort(mDeviceAlarmLogInfoList);
    }


    public void freshUI(String type) {
        try {
            List<DeviceAlarmLogInfo> tempList = new ArrayList<>();
            String typeArray[] = type.split(",");
            for (int i = 0; i < mDeviceAlarmLogInfoList.size(); i++) {
                DeviceAlarmLogInfo alarmLogInfo = mDeviceAlarmLogInfoList.get(i);
                String alarmType = alarmLogInfo.getSensorType();
                boolean isContains = Arrays.asList(typeArray).contains(alarmType);
                if (isContains) {
                    tempList.add(alarmLogInfo);
                }
            }
            getView().updateAlarmListAdapter(tempList);
        } catch (Exception e) {
            e.printStackTrace();
            getView().toastShort(mContext.getResources().getString(R.string.tips_data_error));
        }

    }

    private void requestDataBySearchDown(Long startTime, Long endTime, final String searchType) {
        switch (SensoroCityApplication.getInstance().saveSearchType) {
            case Constants.TYPE_DEVICE_NAME:
                getView().showProgressDialog();
                RetrofitServiceHelper.INSTANCE.getDeviceAlarmLogList(cur_page, null, searchType, null, startTime,
                        endTime,
                        null).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceAlarmLogRsp>() {


                    @Override
                    public void onCompleted() {
                        getView().dismissProgressDialog();
                        getView().onPullRefreshComplete();
                    }

                    @Override
                    public void onNext(DeviceAlarmLogRsp deviceAlarmLogRsp) {
                        freshUI(DIRECTION_DOWN, deviceAlarmLogRsp, null);
                    }

                    @Override
                    public void onErrorMsg(String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                    }
                });
                break;
            case Constants.TYPE_DEVICE_SN:
                getView().showProgressDialog();
                RetrofitServiceHelper.INSTANCE.getDeviceAlarmLogList(cur_page, searchType, null, null, startTime,
                        endTime,
                        null).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceAlarmLogRsp>() {


                    @Override
                    public void onCompleted() {
                        getView().dismissProgressDialog();
                        getView().onPullRefreshComplete();
                    }

                    @Override
                    public void onNext(DeviceAlarmLogRsp deviceAlarmLogRsp) {
                        freshUI(DIRECTION_DOWN, deviceAlarmLogRsp, null);
                    }

                    @Override
                    public void onErrorMsg(String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                    }
                });
                break;
            case Constants.TYPE_DEVICE_PHONE_NUM:
                getView().showProgressDialog();
                RetrofitServiceHelper.INSTANCE.getDeviceAlarmLogList(cur_page, null, null, searchType, startTime,
                        endTime,
                        null).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceAlarmLogRsp>() {


                    @Override
                    public void onCompleted() {
                        getView().dismissProgressDialog();
                        getView().onPullRefreshComplete();
                    }

                    @Override
                    public void onNext(DeviceAlarmLogRsp deviceAlarmLogRsp) {
                        freshUI(DIRECTION_DOWN, deviceAlarmLogRsp, null);
                    }

                    @Override
                    public void onErrorMsg(String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                    }
                });
                break;
            default:
                break;
        }

    }

    private void requestDataBySearchUp(Long startTime, Long endTime, final String searchType) {
        switch (SensoroCityApplication.getInstance().saveSearchType) {
            case Constants.TYPE_DEVICE_NAME:
                getView().showProgressDialog();
                RetrofitServiceHelper.INSTANCE.getDeviceAlarmLogList(cur_page, null, searchType, null, startTime,
                        endTime,
                        null).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceAlarmLogRsp>() {


                    @Override
                    public void onCompleted() {
                        getView().dismissProgressDialog();
                        getView().onPullRefreshComplete();
                    }

                    @Override
                    public void onNext(DeviceAlarmLogRsp deviceAlarmLogRsp) {
                        if (deviceAlarmLogRsp.getData().size() == 0) {
                            getView().toastShort("没有更多数据了");
                            cur_page--;
                        } else {
                            freshUI(DIRECTION_UP, deviceAlarmLogRsp, null);
                        }
                    }

                    @Override
                    public void onErrorMsg(String errorMsg) {
                        cur_page--;
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                    }
                });
                break;
            case Constants.TYPE_DEVICE_SN:
                getView().showProgressDialog();
                RetrofitServiceHelper.INSTANCE.getDeviceAlarmLogList(cur_page, searchType, null, null, startTime,
                        endTime,
                        null).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceAlarmLogRsp>() {


                    @Override
                    public void onCompleted() {
                        getView().dismissProgressDialog();
                        getView().onPullRefreshComplete();
                    }

                    @Override
                    public void onNext(DeviceAlarmLogRsp deviceAlarmLogRsp) {
                        if (deviceAlarmLogRsp.getData().size() == 0) {
                            getView().toastShort("没有更多数据了");
                            cur_page--;
                        } else {
                            freshUI(DIRECTION_UP, deviceAlarmLogRsp, null);
                        }
                    }

                    @Override
                    public void onErrorMsg(String errorMsg) {
                        cur_page--;
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                    }
                });
                break;
            case Constants.TYPE_DEVICE_PHONE_NUM:
                getView().showProgressDialog();
                RetrofitServiceHelper.INSTANCE.getDeviceAlarmLogList(cur_page, null, null, searchType, startTime,
                        endTime,
                        null).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceAlarmLogRsp>() {


                    @Override
                    public void onCompleted() {
                        getView().dismissProgressDialog();
                        getView().onPullRefreshComplete();
                    }

                    @Override
                    public void onNext(DeviceAlarmLogRsp deviceAlarmLogRsp) {
                        if (deviceAlarmLogRsp.getData().size() == 0) {
                            getView().toastShort("没有更多数据了");
                            cur_page--;
                        } else {
                            freshUI(DIRECTION_UP, deviceAlarmLogRsp, null);
                        }
                    }

                    @Override
                    public void onErrorMsg(String errorMsg) {
                        cur_page--;
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                    }
                });
                break;
            default:
                break;
        }

    }

    public void requestSearchData(int direction, boolean isForce, String searchText) {
        if (getView().getPullRefreshState() == PullToRefreshBase.State.RESET && !isForce || TextUtils.isEmpty
                (searchText)) {
            return;
        }
        Long temp_startTime = null;
        Long temp_endTime = null;
        if (getView().isSelectedDateLayoutVisible()) {
            temp_startTime = startTime;
            temp_endTime = endTime;
        }
        switch (direction) {
            case DIRECTION_DOWN:
                cur_page = 1;
                requestDataBySearchDown(temp_startTime, temp_endTime, searchText);
                break;
            case DIRECTION_UP:
                cur_page++;
                requestDataBySearchUp(temp_startTime, temp_endTime, searchText);
                break;
            default:
                break;
        }
    }

    public void requestDataAll(final int direction, boolean isForce) {
        if (getView().getPullRefreshState() == PullToRefreshBase.State.RESET && !isForce) {
            return;
        }
        Long temp_startTime = null;
        Long temp_endTime = null;
        if (getView().isSelectedDateLayoutVisible()) {
            temp_startTime = startTime;
            temp_endTime = endTime;
        }
        switch (direction) {
            case DIRECTION_DOWN:
                cur_page = 1;
                getView().showProgressDialog();
                RetrofitServiceHelper.INSTANCE.getDeviceAlarmLogList(cur_page, null, null, null, temp_startTime,
                        temp_endTime,
                        null).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceAlarmLogRsp>() {


                    @Override
                    public void onCompleted() {
                        getView().dismissProgressDialog();
                        getView().onPullRefreshComplete();
                    }

                    @Override
                    public void onNext(DeviceAlarmLogRsp deviceAlarmLogRsp) {
                        if (deviceAlarmLogRsp.getData().size() == 0) {
                            getView().toastShort("没有更多数据了");
                        } else {
                            freshUI(direction, deviceAlarmLogRsp, null);
                        }
                    }

                    @Override
                    public void onErrorMsg(String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                    }
                });
                break;
            case DIRECTION_UP:
                cur_page++;
                getView().showProgressDialog();
                RetrofitServiceHelper.INSTANCE.getDeviceAlarmLogList(cur_page, null, null, null, temp_startTime,
                        temp_endTime,
                        null).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceAlarmLogRsp>() {


                    @Override
                    public void onCompleted() {
                        getView().dismissProgressDialog();
                        getView().onPullRefreshComplete();
                    }

                    @Override
                    public void onNext(DeviceAlarmLogRsp deviceAlarmLogRsp) {
                        if (deviceAlarmLogRsp.getData().size() == 0) {
                            cur_page--;
                            getView().toastShort("没有更多数据了");
                        } else {
                            freshUI(direction, deviceAlarmLogRsp, null);
                        }
                    }

                    @Override
                    public void onErrorMsg(String errorMsg) {
                        cur_page--;
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                    }
                });
                break;
            default:
                break;
        }
    }

    /**
     * 通过回显日期搜索
     *
     * @param startDate
     * @param endDate
     */
    public void requestDataByDate(String startDate, String endDate) {
        getView().setSelectedDateLayoutVisible(true);
        startTime = DateUtil.strToDate(startDate).getTime();
        endTime = DateUtil.strToDate(endDate).getTime();
        getView().setSelectedDateSearchText(DateUtil.getMothDayFormatDate(startTime) + "-" + DateUtil
                .getMothDayFormatDate
                        (endTime));
        endTime += 1000 * 60 * 60 * 24;
        getView().showProgressDialog();
        RetrofitServiceHelper.INSTANCE.getDeviceAlarmLogList(1, null, null, null, startTime, endTime,
                null).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceAlarmLogRsp>() {


            @Override
            public void onCompleted() {
                getView().dismissProgressDialog();
                getView().onPullRefreshComplete();
            }

            @Override
            public void onNext(DeviceAlarmLogRsp deviceAlarmLogRsp) {
                if (deviceAlarmLogRsp.getData().size() == 0) {
                    getView().toastShort("没有更多数据了");
                }
                freshUI(DIRECTION_DOWN, deviceAlarmLogRsp, null);
            }

            @Override
            public void onErrorMsg(String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);
            }
        });
    }

    public void clickItem(int position) {
        Intent intent = new Intent(mContext, AlarmDetailActivity.class);
        intent.putExtra(EXTRA_ALARM_INFO, mDeviceAlarmLogInfoList.get(position - 1));
        getView().startACForResult(intent, REQUEST_CODE_ALARM);
    }

    public void clickItemByConfirmStatus(int position) {
        DeviceAlarmLogInfo deviceAlarmLogInfo = mDeviceAlarmLogInfoList.get(position);
        getView().showAlarmPopupView(deviceAlarmLogInfo);
    }

    public void popClickComplete(DeviceAlarmLogInfo deviceAlarmLogInfo) {
        for (int i = 0; i < mDeviceAlarmLogInfoList.size(); i++) {
            DeviceAlarmLogInfo tempLogInfo = mDeviceAlarmLogInfoList.get(i);
            if (tempLogInfo.get_id().equals(deviceAlarmLogInfo.get_id())) {
                AlarmInfo.RecordInfo[] recordInfoArray = deviceAlarmLogInfo.getRecords();
                deviceAlarmLogInfo.setSort(1);
                for (int j = 0; j < recordInfoArray.length; j++) {
                    AlarmInfo.RecordInfo recordInfo = recordInfoArray[j];
                    if (recordInfo.getType().equals("recovery")) {
                        deviceAlarmLogInfo.setSort(4);
                        break;
                    }
                }
                mDeviceAlarmLogInfoList.set(i, deviceAlarmLogInfo);
////                Collections.sort(mDeviceAlarmLogInfoList);
                break;
            }
        }
        ArrayList<DeviceAlarmLogInfo> tempList = new ArrayList<>(mDeviceAlarmLogInfoList);
        getView().updateAlarmListAdapter(tempList);
    }

    /**
     * 直接进入搜索界面
     */
    public void searchByImageView() {
        long temp_startTime = -1;
        long temp_endTime = -1;
        if (getView().isSelectedDateLayoutVisible()) {
            temp_startTime = startTime;
            temp_endTime = endTime;
        }
        Intent searchIntent = new Intent(mContext, SearchAlarmActivity.class);
        searchIntent.putExtra(PREFERENCE_KEY_START_TIME, temp_startTime);
        searchIntent.putExtra(PREFERENCE_KEY_END_TIME, temp_endTime);
        searchIntent.putExtra(EXTRA_FRAGMENT_INDEX, 2);
        getView().startACForResult(searchIntent, REQUEST_CODE_SEARCH_ALARM);

    }

    /**
     * 通过搜索内容搜索
     *
     * @param charSequence
     */
    public void searchByEditText(CharSequence charSequence) {
        long temp_startTime = -1;
        long temp_endTime = -1;
        if (getView().isSelectedDateLayoutVisible()) {
            temp_startTime = startTime;
            temp_endTime = endTime;
        }
        Intent searchIntent = new Intent(mContext, SearchAlarmActivity.class);
        if (!TextUtils.isEmpty(charSequence) && getView().isSearchLayoutVisible()) {
            searchIntent.putExtra(EXTRA_SEARCH_CONTENT, charSequence.toString().trim());
        } else {
            searchIntent.putExtra(EXTRA_SEARCH_CONTENT, "");
        }
        searchIntent.putExtra(PREFERENCE_KEY_START_TIME, temp_startTime);
        searchIntent.putExtra(PREFERENCE_KEY_END_TIME, temp_endTime);
        searchIntent.putExtra(EXTRA_FRAGMENT_INDEX, 2);
        getView().startACForResult(searchIntent, REQUEST_CODE_SEARCH_ALARM);
    }

    /**
     * 单独点击日期
     */
    public void clickByDate() {
        long temp_startTime = -1;
        long temp_endTime = -1;
        if (getView().isSelectedDateLayoutVisible()) {
            temp_startTime = startTime;
            temp_endTime = endTime;
        }
        Intent intent = new Intent(mContext, CalendarActivity.class);
        if (getView().isSelectedDateLayoutVisible()) {
            intent.putExtra(PREFERENCE_KEY_START_TIME, temp_startTime);
            intent.putExtra(PREFERENCE_KEY_END_TIME, temp_endTime);
        }
        getView().startACForResult(intent, REQUEST_CODE_CALENDAR);
    }
}
