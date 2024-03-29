package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.iwidget.IOnCreate;
import com.sensoro.common.model.DeployAnalyzerModel;
import com.sensoro.common.model.EventData;
import com.sensoro.common.server.CityObserver;
import com.sensoro.common.server.RetrofitServiceHelper;
import com.sensoro.common.server.bean.DeployControlSettingData;
import com.sensoro.common.server.bean.MonitorPointOperationTaskResultInfo;
import com.sensoro.common.server.response.MonitorPointOperationRequestRsp;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.model.EarlyWarningthresholdDialogUtilsAdapterModel;
import com.sensoro.smartcity.analyzer.DeployConfigurationAnalyzer;
import com.sensoro.smartcity.constant.CityConstants;
import com.sensoro.smartcity.imainviews.ISingleMonitorConfigurationView;
import com.sensoro.common.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.NumberFormat;
import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.sensoro.common.constant.Constants.DEPLOY_CONFIGURATION_SOURCE_TYPE_DEPLOY_DEVICE;
import static com.sensoro.common.constant.Constants.DEPLOY_CONFIGURATION_SOURCE_TYPE_DEVICE_DETAIL;
import static com.sensoro.smartcity.constant.CityConstants.MATERIAL_VALUE_MAP;

public class SingleMonitorConfigurationPresenter extends BasePresenter<ISingleMonitorConfigurationView> implements IOnCreate {
    private Activity mActivity;
    private DeployAnalyzerModel deployAnalyzerModel;
    private int[] mMinMaxValue;
    private ArrayList<EarlyWarningthresholdDialogUtilsAdapterModel> overCurrentDataList;
    private int configurationSource;
    private String mScheduleNo;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable DeviceTaskOvertime = new Runnable() {
        @Override
        public void run() {
            mHandler.removeCallbacks(DeviceTaskOvertime);
            mScheduleNo = null;
            getView().dismissOperatingLoadingDialog();
            //若超时 去除显示超时对话框逻辑
//            getView().showErrorTipDialog(mActivity.getString(R.string.operation_request_time_out));
            if (isAttachedView()) {
                getView().finishAc();
            }

        }
    };
    private final ArrayList<String> pickerStrings = new ArrayList<>();
    private DeployControlSettingData deployControlSettingData;

    @Override
    public void initData(Context context) {
        mActivity = (Activity) context;
        deployAnalyzerModel = (DeployAnalyzerModel) mActivity.getIntent().getSerializableExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL);
        configurationSource = mActivity.getIntent().getIntExtra(Constants.EXTRA_DEPLOY_CONFIGURATION_ORIGIN_TYPE, DEPLOY_CONFIGURATION_SOURCE_TYPE_DEVICE_DETAIL);
        switch (configurationSource) {
            case DEPLOY_CONFIGURATION_SOURCE_TYPE_DEPLOY_DEVICE:
                //部署
                getView().setAcDeployConfigurationTvConfigurationText(mActivity.getString(R.string.save));
                deployControlSettingData = (DeployControlSettingData) mActivity.getIntent().getSerializableExtra(Constants.EXTRA_DEPLOY_CONFIGURATION_SETTING_DATA);
                if (deployControlSettingData != null) {
                    Double diameterValue = deployControlSettingData.getWireDiameter();
                    if (diameterValue != null) {
                        NumberFormat nf = NumberFormat.getInstance();
                        String formatStr = nf.format(diameterValue);
                        getView().setInputDiameterValueText(formatStr);
                    }
                    Integer initValue = deployControlSettingData.getSwitchSpec();
                    if (initValue != null) {
                        getView().setInputCurrentText(String.valueOf(initValue));
                    }
                    Integer wireMaterial = deployControlSettingData.getWireMaterial();
                    if (0 == wireMaterial) {
                        getView().setInputWireMaterialText(mActivity.getString(R.string.cu));
                    } else if (1 == wireMaterial) {
                        getView().setInputWireMaterialText(mActivity.getString(R.string.al));
                    }
                }
                break;
            case DEPLOY_CONFIGURATION_SOURCE_TYPE_DEVICE_DETAIL:
                onCreate();
                getView().setAcDeployConfigurationTvConfigurationText(mActivity.getString(R.string.air_switch_config));
                //详情
                break;

        }
        getView().setLlAcDeployConfigurationDiameterVisible(needDiameter());
        init();
    }

    private void init() {
        mMinMaxValue = DeployConfigurationAnalyzer.analyzeDeviceType(deployAnalyzerModel.deviceType);
        if (mMinMaxValue == null) {
            getView().toastShort(mActivity.getString(R.string.deploy_configuration_analyze_failed));
        } else {
            getView().setTvEnterValueRange(mMinMaxValue[0], mMinMaxValue[1]);
        }
        initOverCurrentData();
        initPickerData();

    }

    private void initPickerData() {
        pickerStrings.addAll(MATERIAL_VALUE_MAP.keySet());
        getView().updatePvCustomOptions(pickerStrings);
    }

    private void initOverCurrentData() {
        overCurrentDataList = new ArrayList<>();
        EarlyWarningthresholdDialogUtilsAdapterModel model = new EarlyWarningthresholdDialogUtilsAdapterModel();
        model.content = mActivity.getString(R.string.over_current_description_one);
        overCurrentDataList.add(model);
        EarlyWarningthresholdDialogUtilsAdapterModel model1 = new EarlyWarningthresholdDialogUtilsAdapterModel();
        model1.content = mActivity.getString(R.string.over_current_description_two);
        overCurrentDataList.add(model1);

    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        mHandler.removeCallbacksAndMessages(null);
        pickerStrings.clear();
    }


    public void doConfiguration(String inputCurrent, String material, String diameter, String actualCurrent) {
        int materialValue = 0;
        double diameterValue = 0;
        Integer mEnterValue;
        Integer inputValue;
        try {
            if (TextUtils.isEmpty(inputCurrent)) {
                getView().toastShort(mActivity.getString(R.string.electric_current) + mActivity.getString(R.string.enter_the_correct_number_format));
                return;
            }
            if (mMinMaxValue == null) {
                getView().toastShort(mActivity.getString(R.string.deploy_configuration_analyze_failed));
                return;
            }
            try {
                inputValue = Integer.parseInt(inputCurrent);
                if (inputValue < mMinMaxValue[0] || inputValue > mMinMaxValue[1]) {
                    getView().toastShort(mActivity.getString(R.string.empty_open_rated_current_is_out_of_range));
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                getView().toastShort(mActivity.getString(R.string.electric_current) + mActivity.getString(R.string.enter_the_correct_number_format));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            getView().toastShort(mActivity.getString(R.string.electric_current) + mActivity.getString(R.string.enter_the_correct_number_format));
            return;
        }
        if (needDiameter()) {
            if (mActivity.getString(R.string.cu).equals(material)) {
                materialValue = 0;
            } else if (mActivity.getString(R.string.al).equals(material)) {
                materialValue = 1;
            }
            if (TextUtils.isEmpty(diameter)) {
                getView().toastShort(mActivity.getString(R.string.enter_wire_diameter_tip));
                return;
            }
            try {
                diameterValue = Double.parseDouble(diameter);
            } catch (Exception e) {
                e.printStackTrace();
                getView().toastShort(mActivity.getString(R.string.diameter) + mActivity.getString(R.string.enter_the_correct_number_format));
                return;
            }
        }

        try {
            if (!TextUtils.isEmpty(actualCurrent) && actualCurrent.endsWith("A")) {
                actualCurrent = actualCurrent.substring(0, actualCurrent.lastIndexOf("A"));
            } else {
                getView().toastShort(mActivity.getString(R.string.electric_current) + mActivity.getString(R.string.enter_the_correct_number_format));
                return;
            }
            if (mMinMaxValue == null) {
                getView().toastShort(mActivity.getString(R.string.deploy_configuration_analyze_failed));
                return;
            }
            try {
                mEnterValue = Integer.parseInt(actualCurrent);
                if (mEnterValue < mMinMaxValue[0] || mEnterValue > mMinMaxValue[1]) {
//                    getView().toastShort(mActivity.getString(R.string.electric_current) + mActivity.getString(R.string.monitor_point_operation_error_value_range) + mMinMaxValue[0] + "-" + mMinMaxValue[1]);
                    getView().toastShort(mActivity.getString(R.string.wire_current_carrying_capacity_is_not_within_the_open_range));
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                getView().toastShort(mActivity.getString(R.string.electric_current) + mActivity.getString(R.string.enter_the_correct_number_format));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            getView().toastShort(mActivity.getString(R.string.electric_current) + mActivity.getString(R.string.enter_the_correct_number_format));
            return;
        }
        switch (configurationSource) {
            case DEPLOY_CONFIGURATION_SOURCE_TYPE_DEPLOY_DEVICE:
                //部署
                EventData eventData = new EventData();
                eventData.code = Constants.EVENT_DATA_DEPLOY_INIT_CONFIG_CODE;
                deployControlSettingData = new DeployControlSettingData();
                deployControlSettingData.setSwitchSpec(mEnterValue);
                deployControlSettingData.setWireDiameter(diameterValue);
                deployControlSettingData.setWireMaterial(materialValue);
                deployControlSettingData.setInputValue(inputValue);
                eventData.data = deployControlSettingData;
                EventBus.getDefault().post(eventData);
                getView().finishAc();
                break;
            case DEPLOY_CONFIGURATION_SOURCE_TYPE_DEVICE_DETAIL:
                //详情
                requestCmd(inputValue, mEnterValue, materialValue, diameterValue);
                break;
        }

    }

    private void requestCmd(Integer inputValue, final Integer value, final int material, final Double diameter) {
        ArrayList<String> sns = new ArrayList<>();
        sns.add(deployAnalyzerModel.sn);
        getView().showOperationTipLoadingDialog();
        mScheduleNo = null;
        long deviceTaskOvertimeMillis;
        if (CityConstants.DEVICE_2G_CONFIG_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType)) {
            deviceTaskOvertimeMillis = 30 * 1000;
        } else {
            deviceTaskOvertimeMillis = 15 * 1000;
        }
        RetrofitServiceHelper.getInstance().doMonitorPointOperation(sns, "config", null, null, inputValue, value, material, diameter,null)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<MonitorPointOperationRequestRsp>(this) {
            @Override
            public void onCompleted(MonitorPointOperationRequestRsp response) {
                String scheduleNo = response.getScheduleNo();
                if (TextUtils.isEmpty(scheduleNo)) {
                    getView().dismissOperatingLoadingDialog();
                    getView().showErrorTipDialog(mActivity.getString(R.string.monitor_point_operation_schedule_no_error));
                } else {
                    String[] split = scheduleNo.split(",");
                    if (split.length > 0) {
                        mScheduleNo = split[0];
                        mHandler.removeCallbacks(DeviceTaskOvertime);
                        mHandler.postDelayed(DeviceTaskOvertime, deviceTaskOvertimeMillis);
                    } else {
                        getView().dismissOperatingLoadingDialog();
                        getView().showErrorTipDialog(mActivity.getString(R.string.monitor_point_operation_schedule_no_error));

                    }
                    deployControlSettingData = new DeployControlSettingData();
                    deployControlSettingData.setSwitchSpec(value);
                    deployControlSettingData.setWireDiameter(diameter);
                    deployControlSettingData.setWireMaterial(material);
                    deployControlSettingData.setInputValue(inputValue);
                }
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().dismissOperatingLoadingDialog();
                getView().showErrorTipDialog(errorMsg);
            }
        });

    }

    public boolean needDiameter() {
        return CityConstants.DEVICE_CONTROL_DEVICE_TYPES.contains(deployAnalyzerModel.deviceType);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessageEvent(MonitorPointOperationTaskResultInfo monitorPointOperationTaskResultInfo) {
        try {
            LogUtils.loge("EVENT_DATA_SOCKET_MONITOR_POINT_OPERATION_TASK_RESULT --->>");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        final String scheduleNo = monitorPointOperationTaskResultInfo.getScheduleNo();
        if (!TextUtils.isEmpty(scheduleNo) && monitorPointOperationTaskResultInfo.getTotal() == monitorPointOperationTaskResultInfo.getComplete()) {
            String[] split = scheduleNo.split(",");
            if (split.length > 0) {
                final String temp = split[0];
                if (!TextUtils.isEmpty(temp)) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(mScheduleNo) && mScheduleNo.equals(temp)) {
                                mHandler.removeCallbacks(DeviceTaskOvertime);
                                if (isAttachedView()) {
                                    getView().dismissOperatingLoadingDialog();
                                    getView().showOperationSuccessToast();
                                    //
                                    pushConfigResult();
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (isAttachedView()) {
                                                getView().finishAc();
                                            }
                                        }
                                    }, 1000);
                                }
                            }
                        }
                    });
                }
            }

        }
    }

    public void showOverCurrentDialog() {
//        if (isAttachedView()) {
//            getView().showOverCurrentDialog(overCurrentDataList);
//        }
    }

    private void pushConfigResult() {
        EventData eventData = new EventData();
        eventData.data = deployControlSettingData;
        eventData.code = Constants.EVENT_DATA_DEPLOY_INIT_CONFIG_CODE;
        EventBus.getDefault().post(eventData);
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }

    public void doCustomOptionPickerItemSelect(int position) {
        String tx = pickerStrings.get(position);
        if (!TextUtils.isEmpty(tx)) {
            getView().setInputDiameterValueText(tx);
        }
    }
}
