package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.IDeployResultActivityView;
import com.sensoro.smartcity.model.EventData;
import com.sensoro.smartcity.server.bean.DeviceInfo;
import com.sensoro.smartcity.util.DateUtil;

import org.greenrobot.eventbus.EventBus;

public class DeployResultActivityPresenter extends BasePresenter<IDeployResultActivityView> implements Constants {
    private int resultCode = 0;
    private DeviceInfo deviceInfo = null;
    private Activity mContext;
    private String errorInfo;
    private String sn;
    private int scanType = -1;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        resultCode = mContext.getIntent().getIntExtra(EXTRA_SENSOR_RESULT, 0);
        scanType = mContext.getIntent().getIntExtra(EXTRA_SCAN_ORIGIN_TYPE, -1);
        errorInfo = mContext.getIntent().getStringExtra(EXTRA_SENSOR_RESULT_ERROR);
        sn = mContext.getIntent().getStringExtra(EXTRA_SENSOR_SN_RESULT);
        if (TYPE_SCAN_DEPLOY_DEVICE_CHANGE == scanType) {
            getView().setDeployResultContinueText("返回巡检");
        }
        init();
    }

    private void init() {
        try {
            if (resultCode == -1) {
                getView().setResultImageView(R.drawable.deploy_fail);
                getView().setStateTextView("失败");
                //
                if (!TextUtils.isEmpty(sn)) {
                    getView().setSnTextView(sn);
                }
//                if (is_station) {
                if (!TextUtils.isEmpty(errorInfo)) {
                    getView().setTipsTextView(mContext.getResources().getString(R.string
                            .tips_deploy_station_failed));
                    getView().setDeployResultErrorInfo("错误：" + errorInfo);
                } else {
                    getView().setTipsTextView(mContext.getResources().getString(R.string
                            .tips_deploy_not_exist));
                }
//                } else {
//                    if (!TextUtils.isEmpty(errorInfo)) {
//                        getView().setTipsTextView(mContext.getResources().getString(R.string.tips_deploy_failed));
//                        getView().setDeployResultErrorInfo("错误：" + errorInfo);
//                    } else {
//                        getView().setTipsTextView(mContext.getResources().getString(R.string.tips_deploy_not_exist));
//                    }
//                }


            } else {
                deviceInfo = (DeviceInfo) mContext.getIntent().getSerializableExtra(EXTRA_DEVICE_INFO);
                String sn = deviceInfo.getSn().toUpperCase();
                String name = deviceInfo.getName();
                String address = deviceInfo.getAddress();
                long updatedTime = deviceInfo.getUpdatedTime();
                switch (scanType) {
                    case TYPE_SCAN_DEPLOY_STATION:
                        //基站部署
                        deployStation(sn, name, address, updatedTime);
                        break;
                    case TYPE_SCAN_DEPLOY_DEVICE:
                        //设备部署
                        deployDevice(sn, name, address, updatedTime);
                        break;
                    case TYPE_SCAN_LOGIN:
                        break;
                    case TYPE_SCAN_DEPLOY_DEVICE_CHANGE:
                        //TODO 巡检设备更换
                        deployDevice(sn, name, address, updatedTime);
                        break;
                    case TYPE_SCAN_INSPECTION:
                        //TODO 扫描巡检设备
                        break;
                    default:
                        break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            getView().toastShort(mContext.getResources().getString(R.string.tips_data_error));
        }

    }

    private void deployDevice(String sn, String name, String address, long updatedTime) {
        if (!TextUtils.isEmpty(address)) {
            getView().setAddressTextView(address);
        }
        getView().setContactAndSignalVisible(true);
        String contact = mContext.getIntent().getStringExtra(EXTRA_SETTING_CONTACT);
        String content = mContext.getIntent().getStringExtra(EXTRA_SETTING_CONTENT);

        getView().setContactTextView((TextUtils.isEmpty(contact) ?
                "无" : contact) + "(" + (TextUtils.isEmpty
                (contact) ?
                "无" : content) + ")");
        getView().refreshSignal(updatedTime, deviceInfo.getSignal());
        if (resultCode == 1) {
            getView().setResultImageView(R.drawable.deploy_succeed);
            getView().setStateTextView("成功");
            getView().setTipsTextView(mContext.getResources().getString(R.string.tips_deploy_success));
        } else {
            getView().setResultImageView(R.mipmap.ic_deploy_failed);
            getView().setTipsTextView(mContext.getResources().getString(R.string.tips_deploy_failed));
        }
        getView().setSnTextView(sn);
        getView().setNameTextView(name);
        getView().setStatusTextView(Constants.DEVICE_STATUS_ARRAY[deviceInfo.getStatus()]);
//                // 修改长传时间
//                String lastUpdatedTime = deviceInfo.getLastUpdatedTime();
        if (updatedTime == -1) {
            getView().setUpdateTextViewVisible(false);
        } else {
            getView().setUpdateTextView(DateUtil
                    .getFullParseDate(updatedTime));
        }
    }

    private void deployStation(String sn, String name, String address, long updatedTime) {
        if (!TextUtils.isEmpty(address)) {
            getView().setAddressTextView(address);
        }
        getView().setContactAndSignalVisible(false);
        if (resultCode == 1) {
            getView().setResultImageView(R.drawable.deploy_succeed);
            getView().setStateTextView("成功");
            getView().setTipsTextView(mContext.getResources().getString(R.string
                    .tips_deploy_station_success));
        } else {
            getView().setResultImageView(R.mipmap.ic_deploy_failed);
            getView().setTipsTextView(mContext.getResources().getString(R.string
                    .tips_deploy_station_failed));
        }
        getView().setSnTextView(sn);
        getView().setNameTextView(name);
        getView().setStatusTextView(Constants.STATION_STATUS_ARRAY[deviceInfo.getStatus() + 1]);
        if (updatedTime == -1) {
            getView().setUpdateTextViewVisible(false);
        } else {
            getView().setUpdateTextView(DateUtil
                    .getFullParseDate(updatedTime));
        }
    }

    public void gotoContinue() {
        EventData eventData = new EventData();
        eventData.code = EVENT_DATA_DEPLOY_RESULT_CONTINUE;
        if (resultCode == 1 && deviceInfo != null) {
            eventData.data = deviceInfo;
        }
        EventBus.getDefault().post(eventData);
        getView().finishAc();
    }

    public void backHome() {
        EventData eventData = new EventData();
        eventData.code = EVENT_DATA_DEPLOY_RESULT_FINISH;
        if (resultCode == 1 && deviceInfo != null) {
            eventData.data = deviceInfo;
        }
        EventBus.getDefault().post(eventData);
        getView().finishAc();
    }

    @Override
    public void onDestroy() {

    }
}
