package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.activity.MonitorPointDetailActivity;
import com.sensoro.smartcity.activity.SearchMonitorActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.ISearchMonitorActivityView;
import com.sensoro.smartcity.iwidget.IOnStart;
import com.sensoro.smartcity.model.AlarmPopModel;
import com.sensoro.smartcity.model.EventData;
import com.sensoro.smartcity.model.PushData;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.DeviceAlarmLogInfo;
import com.sensoro.smartcity.server.bean.DeviceInfo;
import com.sensoro.smartcity.server.response.DeviceAlarmLogRsp;
import com.sensoro.smartcity.server.response.DeviceInfoListRsp;
import com.sensoro.smartcity.util.LogUtils;
import com.sensoro.smartcity.widget.popup.AlarmLogPopUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.lzy.imagepicker.ImagePicker.EXTRA_RESULT_BY_TAKE_PHOTO;

public class SearchMonitorActivityPresenter extends BasePresenter<ISearchMonitorActivityView> implements Constants,
        IOnStart {
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private String history;


    private final List<String> mHistoryKeywords = new ArrayList<>();
    private int mTypeSelectedIndex = 0;
    private int mStatusSelectedIndex = 0;

    private int page = 1;
    private final List<DeviceInfo> mDataList = new ArrayList<>();
    private final List<DeviceInfo> originHistoryList = new ArrayList<>();
    private final List<DeviceInfo> currentList = new ArrayList<>();

    private final List<String> searchStrList = new ArrayList<>();
    private Activity mContext;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        originHistoryList.addAll(SensoroCityApplication.getInstance().getData());
        currentList.addAll(originHistoryList);
        mPref = mContext.getSharedPreferences(PREFERENCE_DEVICE_HISTORY, Activity.MODE_PRIVATE);
        mEditor = mPref.edit();
        history = mPref.getString(PREFERENCE_KEY_DEVICE, "");
        if (!TextUtils.isEmpty(history)) {
            mHistoryKeywords.clear();
            mHistoryKeywords.addAll(Arrays.asList(history.split(",")));
        }
        if (mHistoryKeywords.size() > 0) {
            getView().setSearchHistoryLayoutVisible(true);
            getView().updateSearchHistoryData(mHistoryKeywords);
        } else {
            getView().setSearchHistoryLayoutVisible(false);
        }

    }


    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        getView().hideSoftInput();
    }

//    private void hideSoftInput() {
//        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (imm != null) {
//            boolean active = imm.isActive();
//            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//        }
//
//    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(PushData data) {
        if (data != null) {
            List<DeviceInfo> deviceInfoList = data.getDeviceInfoList();
            for (int i = 0; i < mDataList.size(); i++) {
                DeviceInfo deviceInfo = mDataList.get(i);
                for (DeviceInfo in : deviceInfoList) {
                    if (in.getSn().equals(deviceInfo.getSn())) {
                        mDataList.set(i, in);
                    }
                }
            }
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isActivityTop() && getView().getSearchDataListVisible()) {
                        getView().refreshData(mDataList);
                    }
                }
            });


        }
    }


    private boolean isActivityTop() {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        return name.equals(SearchMonitorActivity.class.getName());
    }

    public void clickRelationItem(int position) {
        String s = searchStrList.get(position);
        save(s);
        requestWithDirection(DIRECTION_DOWN, s);
    }


    private void refreshCacheData() {
        this.mDataList.clear();
//        this.mDataList.clear();
//        this.mRelationLayout.setVisibility(View.GONE);
//        this.mIndexListLayout.setVisibility(VISIBLE);
        for (int i = 0; i < currentList.size(); i++) {
            DeviceInfo deviceInfo = currentList.get(i);
            switch (deviceInfo.getStatus()) {
                case SENSOR_STATUS_ALARM:
                    deviceInfo.setSort(1);
                    break;
                case SENSOR_STATUS_NORMAL:
                    deviceInfo.setSort(2);
                    break;
                case SENSOR_STATUS_LOST:
                    deviceInfo.setSort(3);
                    break;
                case SENSOR_STATUS_INACTIVE:
                    deviceInfo.setSort(4);
                    break;
                default:
                    break;
            }
            if (isMatcher(deviceInfo)) {
                mDataList.add(deviceInfo);
            }
        }
//        getView().refreshData(mDataList);
    }

    private boolean isMatcher(DeviceInfo deviceInfo) {
        if (mTypeSelectedIndex == 0 && mStatusSelectedIndex == 0) {
            return true;
        } else {
            boolean isMatcherType = false;
            boolean isMatcherStatus = false;
            String unionType = deviceInfo.getUnionType();
            if (unionType != null) {
                String[] unionTypeArray = unionType.split("\\|");
                List<String> unionTypeList = Arrays.asList(unionTypeArray);
                String[] menuTypeArray = SELECT_TYPE_VALUES[mTypeSelectedIndex].split("\\|");
                if (mTypeSelectedIndex == 0) {
                    isMatcherType = true;
                } else {
                    for (String menuType : menuTypeArray) {
                        if (unionTypeList.contains(menuType)) {
                            isMatcherType = true;
                            break;
                        }
                    }
                }
            }
            if (mStatusSelectedIndex != 0) {
                int status = INDEX_STATUS_VALUES[mStatusSelectedIndex - 1];
                if (deviceInfo.getStatus() == status) {
                    isMatcherStatus = true;
                }
            } else {
                isMatcherStatus = true;
            }
            return isMatcherStatus && isMatcherType;
        }
    }

    public void filterDeviceInfo(String filter) {
        List<DeviceInfo> originDeviceInfoList = new ArrayList<>(originHistoryList);
        ArrayList<DeviceInfo> deleteDeviceInfoList = new ArrayList<>();
        for (DeviceInfo deviceInfo : originDeviceInfoList) {
            String name = deviceInfo.getName();
            if (!TextUtils.isEmpty(name)) {
                if (!(name.contains(filter.toUpperCase()))) {
                    deleteDeviceInfoList.add(deviceInfo);
                }
            } else {
                deleteDeviceInfoList.add(deviceInfo);
            }

        }
        originDeviceInfoList.removeAll(deleteDeviceInfoList);
        List<String> tempList = new ArrayList<>();
        for (DeviceInfo deviceInfo : originDeviceInfoList) {
            String name = deviceInfo.getName();
            if (!TextUtils.isEmpty(name)) {
                tempList.add(name);
            }
        }
        searchStrList.clear();
        searchStrList.addAll(tempList);
        getView().updateRelationData(tempList);
        originDeviceInfoList.clear();
        tempList.clear();
        deleteDeviceInfoList.clear();


    }

    public void save(String text) {
        String oldText = mPref.getString(PREFERENCE_KEY_DEVICE, "");
        //
        List<String> oldHistoryList = new ArrayList<String>();
        if (!TextUtils.isEmpty(oldText)) {
            oldHistoryList.addAll(Arrays.asList(oldText.split(",")));
        }
        if (!oldHistoryList.contains(text)) {
            oldHistoryList.add(0, text);
        }
        ArrayList<String> tempList = new ArrayList<>();
        for (String str : oldHistoryList) {
            if (tempList.size() < 20) {
                tempList.add(str);
            }
        }
        mHistoryKeywords.clear();
        mHistoryKeywords.addAll(tempList);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < tempList.size(); i++) {
            if (i == (tempList.size() - 1)) {
                stringBuilder.append(tempList.get(i));
            } else {
                stringBuilder.append(tempList.get(i)).append(",");
            }
        }
        mEditor.putString(PREFERENCE_KEY_DEVICE, stringBuilder.toString());
        mEditor.commit();
        //
//        if (TextUtils.isEmpty(oldText)) {
//            mEditor.putString(PREFERENCE_KEY_DEVICE, text);
//        } else {
//            if (!TextUtils.isEmpty(text)) {
//                if (mHistoryKeywords.contains(text)) {
//                    List<String> list = new ArrayList<String>();
//                    for (String o : oldText.split(",")) {
//                        if (!o.equalsIgnoreCase(text)) {
//                            list.add(o);
//                        }
//                    }
//                    list.add(0, text);
//
//                    ArrayList<String> tempList = new ArrayList<>();
//                    for (String str : list) {
//                        if (tempList.size() < 20) {
//                            tempList.add(str);
//                        }
//                    }
//                    mHistoryKeywords.clear();
//                    mHistoryKeywords.addAll(tempList);
//                    StringBuffer stringBuilder = new StringBuffer();
//                    for (int i = 0; i < tempList.size(); i++) {
//                        if (i == (tempList.size() - 1)) {
//                            stringBuilder.append(tempList.getInstance(i));
//                        } else {
//                            stringBuilder.append(tempList.getInstance(i) + ",");
//                        }
//                    }
//                    mEditor.putString(PREFERENCE_KEY_DEVICE, stringBuilder.toString());
//                    mEditor.commit();
//                } else {
//                    mHistoryKeywords.add(0, text);
//                    ArrayList<String> tempList = new ArrayList<>();
//                    for (String str : mHistoryKeywords) {
//                        if (tempList.size() < 20) {
//                            tempList.add(str);
//                        }
//                    }
//                    mHistoryKeywords.clear();
//                    mHistoryKeywords.addAll(tempList);
//
//                    StringBuffer stringBuilder = new StringBuffer();
//                    for (int i = 0; i < tempList.size(); i++) {
//                        if (i == (tempList.size() - 1)) {
//                            stringBuilder.append(tempList.getInstance(i));
//                        } else {
//                            stringBuilder.append(tempList.getInstance(i) + ",");
//                        }
//                    }
//                    mEditor.putString(PREFERENCE_KEY_DEVICE, stringBuilder.toString());
//                    mEditor.commit();
//                }
//            }
//        }

    }

    public void cleanHistory() {
        mEditor.clear();
        mHistoryKeywords.clear();
        mEditor.commit();
        getView().updateSearchHistoryData(mHistoryKeywords);
        getView().setSearchHistoryLayoutVisible(false);
    }

    public void requestWithDirection(int direction, String searchText) {
        getView().setSearchHistoryLayoutVisible(false);
        getView().setRelationLayoutVisible(false);
        getView().setIndexListLayoutVisible(true);
        String type = mTypeSelectedIndex == 0 ? null : SELECT_TYPE_VALUES[mTypeSelectedIndex];
        Integer status = mStatusSelectedIndex == 0 ? null : INDEX_STATUS_VALUES[mStatusSelectedIndex - 1];
        getView().showProgressDialog();
        if (direction == DIRECTION_DOWN) {
            page = 1;
            RetrofitServiceHelper.INSTANCE.getDeviceBriefInfoList(page, type, null, status, searchText).subscribeOn
                    (Schedulers.io()).doOnNext(new Action1<DeviceInfoListRsp>() {
                @Override
                public void call(DeviceInfoListRsp deviceInfoListRsp) {
                    if (deviceInfoListRsp.getData().size() == 0) {
                        mDataList.clear();
                    } else {
                        currentList.clear();
                        currentList.addAll(deviceInfoListRsp.getData());
                        refreshCacheData();
                    }
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceInfoListRsp>(this) {


                @Override
                public void onCompleted(DeviceInfoListRsp deviceInfoListRsp) {
                    getView().dismissProgressDialog();
                    try {
                        getView().refreshData(mDataList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getView().recycleViewRefreshComplete();
                }

                @Override
                public void onErrorMsg(int errorCode, String errorMsg) {
                    getView().dismissProgressDialog();
                    getView().recycleViewRefreshComplete();
                    getView().toastShort(errorMsg);
                }
            });
        } else {
            page++;
            RetrofitServiceHelper.INSTANCE.getDeviceBriefInfoList(page, type, null, status, searchText).subscribeOn
                    (Schedulers.io()).doOnNext(new Action1<DeviceInfoListRsp>() {
                @Override
                public void call(DeviceInfoListRsp deviceInfoListRsp) {
                    try {
                        if (deviceInfoListRsp.getData().size() == 0) {
                            page--;
                        } else {
                            currentList.addAll(deviceInfoListRsp.getData());
                            refreshCacheData();
                        }
                    } catch (Exception e) {
                        page--;
                        e.printStackTrace();
                    }
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceInfoListRsp>(this) {


                @Override
                public void onCompleted(DeviceInfoListRsp deviceInfoListRsp) {
                    if (deviceInfoListRsp.getData().size() == 0) {
                        getView().toastShort("没有更多数据了");
                    } else {
                        getView().refreshData(mDataList);
                    }
                    getView().dismissProgressDialog();
                    getView().recycleViewRefreshComplete();
                }

                @Override
                public void onErrorMsg(int errorCode, String errorMsg) {
                    page--;
                    getView().dismissProgressDialog();
                    getView().recycleViewRefreshComplete();
                    getView().toastShort(errorMsg);
                }
            });
        }
    }

    public void clickItem(int position) {
        if (position >= 0) {
            DeviceInfo deviceInfo = mDataList.get(position);
            Intent intent = new Intent(mContext, MonitorPointDetailActivity.class);
            intent.putExtra(EXTRA_DEVICE_INFO, deviceInfo);
            intent.putExtra(EXTRA_SENSOR_NAME, deviceInfo.getName());
            intent.putExtra(EXTRA_SENSOR_TYPES, deviceInfo.getSensorTypes());
            intent.putExtra(EXTRA_SENSOR_STATUS, deviceInfo.getStatus());
            intent.putExtra(EXTRA_SENSOR_TIME, deviceInfo.getUpdatedTime());
            intent.putExtra(EXTRA_SENSOR_LOCATION, deviceInfo.getLonlat());
            getView().startAC(intent);
        }
    }


    @Override
    public void onDestroy() {
        mDataList.clear();
        mHistoryKeywords.clear();
        originHistoryList.clear();
        currentList.clear();
        searchStrList.clear();
    }

    public void filterByTypeWithRequest(int position, String text) {
        String statusText = SELECT_TYPE_VALUES[position];
        getView().setTypeView(statusText);
        this.mTypeSelectedIndex = position;
        requestWithDirection(DIRECTION_DOWN, text);
    }

    public void filterByStatusWithRequest(int position, String text) {
        String statusText = INDEX_STATUS_ARRAY[position];
        this.mStatusSelectedIndex = position;
        requestWithDirection(DIRECTION_DOWN, text);
    }

    public void clickAlarmInfo(int position) {
        DeviceInfo deviceInfo = mDataList.get(position);
        requestAlarmInfo(deviceInfo);
    }

    private void requestAlarmInfo(DeviceInfo deviceInfo) {
        //
        getView().showProgressDialog();
        RetrofitServiceHelper.INSTANCE.getDeviceAlarmLogList(1, deviceInfo.getSn(), null, null, null, null, null, null)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DeviceAlarmLogRsp>(this) {

            @Override
            public void onCompleted(DeviceAlarmLogRsp deviceAlarmLogRsp) {
                getView().dismissProgressDialog();
                if (deviceAlarmLogRsp.getData().size() == 0) {
                    getView().toastShort("未获取到预警日志信息");
                } else {
                    DeviceAlarmLogInfo deviceAlarmLogInfo = deviceAlarmLogRsp.getData().get(0);
                    enterAlarmLogPop(deviceAlarmLogInfo);
                }
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);
            }
        });
    }

    private void enterAlarmLogPop(DeviceAlarmLogInfo deviceAlarmLogInfo) {
        //TODO 弹起预警记录的dialog
        AlarmLogPopUtils mAlarmLogPop = new AlarmLogPopUtils(mContext);
        mAlarmLogPop.refreshData(deviceAlarmLogInfo);
        mAlarmLogPop.show();

    }

    public void handlerActivityResult(int requestCode, int resultCode, Intent data) {
        //TODO 对照片信息统一处理
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            //添加图片返回
            if (data != null && requestCode == REQUEST_CODE_SELECT) {
                ArrayList<ImageItem> tempImages = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                if (tempImages != null) {
                    boolean fromTakePhoto = data.getBooleanExtra(EXTRA_RESULT_BY_TAKE_PHOTO, false);
                    EventData eventData = new EventData();
                    eventData.code = EVENT_DATA_ALARM_POP_IMAGES;
                    AlarmPopModel alarmPopModel = new AlarmPopModel();
                    alarmPopModel.requestCode = requestCode;
                    alarmPopModel.resultCode = resultCode;
                    alarmPopModel.fromTakePhoto = fromTakePhoto;
                    alarmPopModel.imageItems = tempImages;
                    eventData.data = alarmPopModel;
                    EventBus.getDefault().post(eventData);
                }
            }
        } else if (resultCode == ImagePicker.RESULT_CODE_BACK) {
            //预览图片返回
            if (requestCode == REQUEST_CODE_PREVIEW && data != null) {
                ArrayList<ImageItem> tempImages = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS);
                if (tempImages != null) {
                    EventData eventData = new EventData();
                    eventData.code = EVENT_DATA_ALARM_POP_IMAGES;
                    AlarmPopModel alarmPopModel = new AlarmPopModel();
                    alarmPopModel.requestCode = requestCode;
                    alarmPopModel.resultCode = resultCode;
                    alarmPopModel.imageItems = tempImages;
                    eventData.data = alarmPopModel;
                    EventBus.getDefault().post(eventData);
                }
            }
        } else if (resultCode == RESULT_CODE_RECORD) {
            //拍视频
            if (data != null && requestCode == REQUEST_CODE_RECORD) {
                ImageItem imageItem = (ImageItem) data.getSerializableExtra("path_record");
                if (imageItem != null) {
                    LogUtils.loge("--- 从视频返回  path = " + imageItem.path);
                    ArrayList<ImageItem> tempImages = new ArrayList<>();
                    tempImages.add(imageItem);
                    EventData eventData = new EventData();
                    eventData.code = EVENT_DATA_ALARM_POP_IMAGES;
                    AlarmPopModel alarmPopModel = new AlarmPopModel();
                    alarmPopModel.requestCode = requestCode;
                    alarmPopModel.resultCode = resultCode;
                    alarmPopModel.imageItems = tempImages;
                    eventData.data = alarmPopModel;
                    EventBus.getDefault().post(eventData);
                }
            } else if (requestCode == REQUEST_CODE_PLAY_RECORD) {
                EventData eventData = new EventData();
                eventData.code = EVENT_DATA_ALARM_POP_IMAGES;
                AlarmPopModel alarmPopModel = new AlarmPopModel();
                alarmPopModel.requestCode = requestCode;
                alarmPopModel.resultCode = resultCode;
                eventData.data = alarmPopModel;
                EventBus.getDefault().post(eventData);
            }

        }
    }

    public void updateSearchHistoryData() {
        getView().updateSearchHistoryData(mHistoryKeywords);
    }
}
