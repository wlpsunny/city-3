package com.sensoro.smartcity.widget.popup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.LatLng;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageAlarmPhotoDetailActivity;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.activity.VideoPlayActivity;
import com.sensoro.smartcity.adapter.AlertLogRcContentAdapter;
import com.sensoro.smartcity.adapter.TimerShaftAdapter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.AlarmInfo;
import com.sensoro.smartcity.server.bean.DeviceAlarmLogInfo;
import com.sensoro.smartcity.server.bean.ScenesData;
import com.sensoro.smartcity.server.response.DeviceAlarmItemRsp;
import com.sensoro.smartcity.server.response.ResponseBase;
import com.sensoro.smartcity.util.AppUtils;
import com.sensoro.smartcity.util.DateUtil;
import com.sensoro.smartcity.util.LogUtils;
import com.sensoro.smartcity.widget.ProgressUtils;
import com.sensoro.smartcity.widget.SensoroToast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.sensoro.smartcity.util.AppUtils.isAppInstalled;

public class AlarmLogPopUtils implements AlarmPopUtils.OnPopupCallbackListener,
        TimerShaftAdapter.OnPhotoClickListener, Constants {

    private final FixHeightBottomSheetDialog mAlarmLogDialog;
    private final Activity mActivity;
    @BindView(R.id.ac_alert_log_tv_name)
    TextView acAlertLogTvName;
    @BindView(R.id.ac_alert_tv_time)
    TextView acAlertTvTime;
    @BindView(R.id.ac_alert_imv_alert_icon)
    ImageView acAlertImvAlertIcon;
    @BindView(R.id.ac_alert_tv_alert_time)
    TextView acAlertTvAlertTime;
    @BindView(R.id.ac_alert_tv_alert_time_text)
    TextView acAlertTvAlertTimeText;
    @BindView(R.id.ac_alert_ll_alert_time)
    LinearLayout acAlertLlAlertTime;
    @BindView(R.id.ac_alert_imv_alert_count_icon)
    ImageView acAlertImvAlertCountIcon;
    @BindView(R.id.ac_alert_tv_alert_count)
    TextView acAlertTvAlertCount;
    @BindView(R.id.ac_alert_tv_alert_count_text)
    TextView acAlertTvAlertCountText;
    @BindView(R.id.ac_alert_ll_alert_count)
    LinearLayout acAlertLlAlertCount;
    @BindView(R.id.ac_alert_ll_card)
    LinearLayout acAlertLlCard;
    @BindView(R.id.ac_alert_rc_content)
    RecyclerView acAlertRcContent;
    @BindView(R.id.ac_alert_tv_contact_owner)
    TextView acAlertTvContactOwner;
    @BindView(R.id.ac_alert_tv_quick_navigation)
    TextView acAlertTvQuickNavigation;
    @BindView(R.id.ac_alert_tv_alert_confirm)
    TextView acAlertTvAlertConfirm;
    @BindView(R.id.ac_alert_ll_bottom)
    LinearLayout acAlertLlBottom;
    @BindView(R.id.alarm_log_close)
    ImageView alarmLogClose;
    private AlarmPopUtils mAlarmPopUtils;
    private List<AlarmInfo.RecordInfo> mList = new ArrayList<>();
    private AlertLogRcContentAdapter alertLogRcContentAdapter;
    private DeviceAlarmLogInfo mDeviceAlarmLogInfo;
    private LatLng destPosition;
    private boolean isReConfirm = false;
    private ProgressUtils mProgressUtils;

    public AlarmLogPopUtils(Activity activity) {
        mActivity = activity;
        mAlarmLogDialog = new FixHeightBottomSheetDialog(activity);
        View view = View.inflate(activity, R.layout.item_pop_alert_log, null);
        ButterKnife.bind(this, view);
        initRcContent();
        mAlarmLogDialog.setContentView(view);
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());
    }

    private void initRcContent() {
        alertLogRcContentAdapter = new AlertLogRcContentAdapter(mActivity);
        alertLogRcContentAdapter.setOnPhotoClickListener(this);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        acAlertRcContent.setLayoutManager(manager);
        acAlertRcContent.setAdapter(alertLogRcContentAdapter);
    }

    public void show() {
        if (mAlarmLogDialog != null) {
            mAlarmLogDialog.show();
        }
    }

    public void refreshData(DeviceAlarmLogInfo deviceAlarmLogInfo) {
        mDeviceAlarmLogInfo = deviceAlarmLogInfo;

        String name = mDeviceAlarmLogInfo.getDeviceName();
        acAlertLogTvName.setText(TextUtils.isEmpty(name) ? mDeviceAlarmLogInfo.getDeviceSN() : name);

        acAlertTvAlertTime.setText(DateUtil.getFullParseDate(mDeviceAlarmLogInfo.getUpdatedTime()));

        acAlertTvAlertCount.setText(mDeviceAlarmLogInfo.getDisplayStatus() + 10 + "");

        int displayStatus = mDeviceAlarmLogInfo.getDisplayStatus();
        if (displayStatus == DISPLAY_STATUS_CONFIRM) {
            isReConfirm = true;
            acAlertTvAlertConfirm.setText("预警确认");
        } else {
            isReConfirm = false;
            acAlertTvAlertConfirm.setText("再次确认");
        }

        initRcContentData();

    }


    private void initRcContentData() {
        AlarmInfo.RecordInfo[] recordInfoArray = mDeviceAlarmLogInfo.getRecords();
        if (recordInfoArray != null) {
            mList.clear();
            for (int i = recordInfoArray.length - 1; i >= 0; i--) {
                mList.add(recordInfoArray[i]);
            }
            for (AlarmInfo.RecordInfo recordInfo : recordInfoArray) {
                if (recordInfo.getType().equals("recovery")) {
//                    getView().setStatusInfo("于" + DateUtil.getFullParseDate(recordInfo.getUpdatedTime()) + "恢复正常", R
//                            .color.sensoro_normal, R.drawable.shape_status_normal);
//                    break;
                } else {
//                    getView().setStatusInfo(mContext.getResources().getString(R.string.alarming), R.color.sensoro_alarm,
//                            R.drawable.shape_status_alarm);
                }
            }
        }
        updateAlertLogContentAdapter(mList);
    }

    private void updateAlertLogContentAdapter(List<AlarmInfo.RecordInfo> recordInfoList) {
        alertLogRcContentAdapter.setData(recordInfoList);
        alertLogRcContentAdapter.notifyDataSetChanged();
    }

    @OnClick({R.id.alarm_log_close, R.id.ac_alert_tv_contact_owner, R.id.ac_alert_tv_quick_navigation, R.id.ac_alert_tv_alert_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.alarm_log_close:
                mAlarmLogDialog.dismiss();
                break;
            case R.id.ac_alert_tv_contact_owner:
                doContactOwner();
                break;
            case R.id.ac_alert_tv_quick_navigation:
                doNavigation();
                break;
            case R.id.ac_alert_tv_alert_confirm:
                doConfirm();
                break;
        }
    }

    public void doContactOwner() {
        String tempNumber = null;
        outer:
        for (AlarmInfo.RecordInfo recordInfo : mList) {
            String type = recordInfo.getType();
            if ("sendVoice".equals(type)) {
                AlarmInfo.RecordInfo.Event[] phoneList = recordInfo.getPhoneList();
                for (AlarmInfo.RecordInfo.Event event : phoneList) {
                    String source = event.getSource();
                    String number = event.getNumber();
                    if (!TextUtils.isEmpty(number)) {
                        if ("attach".equals(source)) {
                            LogUtils.loge("单独联系人：" + number);
                            tempNumber = number;
                            break outer;

                        } else if ("group".equals(source)) {
                            LogUtils.loge("分组联系人：" + number);
                            tempNumber = number;
                            break;
                        } else if ("notification".equals(source)) {
                            LogUtils.loge("账户联系人：" + number);
                            tempNumber = number;
                            break;
                        }

                    }

                }
            }
        }
        if (TextUtils.isEmpty(tempNumber)) {
            SensoroToast.INSTANCE.makeText("未找到电话联系人", Toast.LENGTH_SHORT).show();
        } else {
            AppUtils.diallPhone(tempNumber, mActivity);
        }
    }

    private void doNavigation() {
        double[] deviceLonlat = mDeviceAlarmLogInfo.getDeviceLonlat();
        if (deviceLonlat != null && deviceLonlat.length > 1) {
            destPosition = new LatLng(deviceLonlat[1], deviceLonlat[0]);
            AMapLocation lastKnownLocation = SensoroCityApplication.getInstance().mLocationClient.getLastKnownLocation();
            if (lastKnownLocation != null) {
                double lat = lastKnownLocation.getLatitude();//获取纬度
                double lon = lastKnownLocation.getLongitude();//获取经度
                LatLng startPosition = new LatLng(lat, lon);
                if (isAppInstalled(mActivity, "com.autonavi.minimap")) {
                    openGaoDeMap(startPosition);
                } else if (isAppInstalled(mActivity, "com.baidu.BaiduMap")) {
                    openBaiDuMap(startPosition);
                } else {
                    openOther(startPosition);
                }
                return;
            }
        }
        SensoroToast.INSTANCE.makeText("未获取到位置信息", Toast.LENGTH_SHORT).show();
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
        mActivity.startActivity(intent);
    }

    private void openBaiDuMap(LatLng startPosition) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("baidumap://map/direction?origin=name:当前位置|latlng:" + startPosition.latitude + "," +
                startPosition.longitude +
                "&destination=name:设备部署位置|latlng:" + destPosition.latitude + "," + destPosition.longitude +
                "&mode=driving&coord_type=gcj02"));
        mActivity.startActivity(intent);
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
        mActivity.startActivity(intent);
    }

    private void doConfirm() {
        mAlarmPopUtils = new AlarmPopUtils(mActivity);
        mAlarmPopUtils.setDialog(mActivity);
        mAlarmPopUtils.setOnPopupCallbackListener(this);
        mAlarmPopUtils.show();
    }

    @Override
    public void onPopupCallback(int statusResult, int statusType, int statusPlace, List<ScenesData> scenesDataList, String remark) {
        mAlarmPopUtils.setUpdateButtonClickable(false);
        mProgressUtils.showProgress();
        RetrofitServiceHelper.INSTANCE.doUpdatePhotosUrl(mDeviceAlarmLogInfo.get_id(), statusResult, statusType,
                statusPlace,
                remark, isReConfirm, scenesDataList).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe
                        (new CityObserver<DeviceAlarmItemRsp>() {


                            @Override
                            public void onCompleted(DeviceAlarmItemRsp deviceAlarmItemRsp) {
                                if (deviceAlarmItemRsp.getErrcode() == ResponseBase.CODE_SUCCESS) {
                                    SensoroToast.INSTANCE.makeText(mActivity.getResources().
                                            getString(R.string.tips_commit_success), Toast.LENGTH_SHORT).show();
                                    mDeviceAlarmLogInfo = deviceAlarmItemRsp.getData();
                                    refreshData(mDeviceAlarmLogInfo);
                                } else {
                                    SensoroToast.INSTANCE.makeText(mActivity.getResources().
                                            getString(R.string.tips_commit_failed), Toast.LENGTH_SHORT).show();
                                }
                                mProgressUtils.dismissProgress();
                                mAlarmPopUtils.dismiss();
                            }

                            @Override
                            public void onErrorMsg(int errorCode, String errorMsg) {
                                mProgressUtils.dismissProgress();
                                mAlarmPopUtils.dismiss();
                                mAlarmPopUtils.setUpdateButtonClickable(true);
                            }
                        });
    }

    @Override
    public void onPhotoItemClick(int position, List<ScenesData> scenesDataList) {
        ArrayList<ImageItem> items = new ArrayList<>();
        if (scenesDataList != null && scenesDataList.size() > 0) {
            for (ScenesData scenesData : scenesDataList) {
                ImageItem imageItem = new ImageItem();
                imageItem.fromUrl = true;
                if ("video".equals(scenesData.type)) {
                    imageItem.isRecord = true;
                    imageItem.recordPath = scenesData.url;
                    imageItem.path = scenesData.thumbUrl;
                } else {
                    imageItem.path = scenesData.url;
                    imageItem.isRecord = false;
                }
                items.add(imageItem);
            }
            ImageItem imageItem = items.get(position);
            if (imageItem.isRecord) {
                Intent intent = new Intent();
                intent.setClass(mActivity, VideoPlayActivity.class);
                intent.putExtra("path_record", (Serializable) imageItem);
                intent.putExtra("video_del", true);
                mActivity.startActivity(intent);
            } else {
                //
                Intent intentPreview = new Intent(mActivity, ImageAlarmPhotoDetailActivity.class);
                intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, items);
                intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
                intentPreview.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
                mActivity.startActivity(intentPreview);
            }

        }
    }
}