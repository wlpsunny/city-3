package com.sensoro.smartcity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sensoro.common.adapter.TagAdapter;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.constant.MonitorPointOperationCode;
import com.sensoro.common.manger.SensoroLinearLayoutManager;
import com.sensoro.common.model.Elect3DetailModel;
import com.sensoro.common.model.MonitoringPointRcContentAdapterModel;
import com.sensoro.common.server.bean.ScenesData;
import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.common.widgets.SpacesItemDecoration;
import com.sensoro.common.widgets.TipOperationDialogUtils;
import com.sensoro.common.widgets.TouchRecycleView;
import com.sensoro.common.widgets.dialog.TipBleDialogUtils;
import com.sensoro.common.widgets.toast.SensoroSuccessToast;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.MonitorDeployDetailPhotoAdapter;
import com.sensoro.smartcity.adapter.MonitorDetailOperationAdapter;
import com.sensoro.smartcity.adapter.MonitoringPointRcContentAdapter;
import com.sensoro.smartcity.adapter.MonitoringPointRcMalfunctionContentAdapter;
import com.sensoro.smartcity.adapter.model.EarlyWarningthresholdDialogUtilsAdapterModel;
import com.sensoro.smartcity.imainviews.IMonitorPointDetailActivityView;
import com.sensoro.smartcity.model.TaskOptionModel;
import com.sensoro.smartcity.presenter.MonitorPointDetailActivityPresenter;
import com.sensoro.smartcity.widget.dialog.EarlyWarningThresholdDialogUtils;
import com.sensoro.smartcity.widget.dialog.MonitorPointDemoDialogUtils;
import com.sensoro.smartcity.widget.dialog.MonitorPointOperatingDialogUtil;
import com.sensoro.smartcity.widget.dialog.TipDeviceUpdateDialogUtils;
import com.sensoro.smartcity.widget.divider.BottomNoDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MonitorPointDetailActivity extends BaseActivity<IMonitorPointDetailActivityView,
        MonitorPointDetailActivityPresenter> implements IMonitorPointDetailActivityView, TipOperationDialogUtils.TipDialogUtilsClickListener, MonitorDeployDetailPhotoAdapter.OnRecyclerViewItemClickListener, EarlyWarningThresholdDialogUtils.DialogUtilsChangeClickListener {
    @BindView(R.id.include_text_title_imv_arrows_left)
    ImageView includeImvTitleImvArrowsLeft;
    @BindView(R.id.include_text_title_tv_title)
    TextView includeImvTitleTvTitle;
    @BindView(R.id.include_text_title_divider)
    View includeTextTitleDivider;
    @BindView(R.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R.id.ac_monitoring_point_tv_name)
    TextView acMonitoringPointTvName;
    @BindView(R.id.ac_monitoring_point_imv_detail)
    ImageView acMonitoringPointImvDetail;
    @BindView(R.id.ac_monitoring_point_tv_type_time)
    TextView acMonitoringPointTvTypeTime;
    @BindView(R.id.ac_monitoring_point_rc_content)
    RecyclerView acMonitoringPointRcContent;
    @BindView(R.id.ac_monitoring_point_tv_alert_contact)
    TextView acMonitoringPointTvAlertContact;
    @BindView(R.id.ac_monitoring_point_tv_alert_contact_name)
    TextView acMonitoringPointTvAlertContactName;
    @BindView(R.id.ac_monitoring_point_tv_alert_contact_phone)
    TextView acMonitoringPointTvAlertContactPhone;
    @BindView(R.id.ac_monitoring_point_imv_phone_arrow)
    ImageView acMonitoringPointImvPhoneArrow;
    @BindView(R.id.ac_monitoring_point_tv_phonecount)
    TextView acMonitoringPointTvPhoneCount;
    @BindView(R.id.ac_monitoring_point_tv_location_navigation)
    TextView acMonitoringPointTvLocationNavigation;
    @BindView(R.id.ac_monitoring_point_tv_location)
    TextView acMonitoringPointTvLocation;
    @BindView(R.id.ac_monitoring_point_imv_location)
    ImageView acMonitoringPointImvLocation;
    @BindView(R.id.ac_monitoring_point_tv_operation)
    TextView acMonitoringPointTvOperation;
    @BindView(R.id.ac_monitoring_point_cl_alert_contact)
    ConstraintLayout acMonitoringPointClAlertContact;
    @BindView(R.id.ac_monitoring_point_cl_location_navigation)
    ConstraintLayout acMonitoringPointClLocationNavigation;
    @BindView(R.id.monitor_detail_tv_sn)
    TextView monitorDetailTvSn;
    @BindView(R.id.monitor_detail_rc_tag)
    TouchRecycleView monitorDetailRcTag;
    @BindView(R.id.monitor_detail_tv_battery)
    TextView monitorDetailTvBattery;
    @BindView(R.id.monitor_detail_tv_interval)
    TextView monitorDetailTvInterval;
    @BindView(R.id.ac_monitoring_point_tv_status)
    TextView acMonitoringPointTvStatus;
    @BindView(R.id.ac_monitoring_point_view)
    View acMonitoringPointView;
    @BindView(R.id.ac_monitoring_point_tv_device_type)
    TextView acMonitoringPointTvDeviceType;
    @BindView(R.id.ac_monitoring_point_ll_operation)
    LinearLayout acMonitoringPointLlOperation;
    @BindView(R.id.ac_monitor_deploy_photo)
    TouchRecycleView acMonitorDeployPhoto;
    MonitorDeployDetailPhotoAdapter mAdapter;
    @BindView(R.id.tr_elect_top)
    TableRow trElectTop;
    @BindView(R.id.tr_elect_a)
    TableRow trElectA;
    @BindView(R.id.tr_elect_v)
    TableRow trElectV;
    @BindView(R.id.tr_elect_t)
    TableRow trElectT;
    @BindView(R.id.elect_info)
    ImageView electInfo;
    @BindView(R.id.ll_all_info)
    LinearLayout llAllInfo;
    @BindView(R.id.tv_elect_main)
    TextView tvElectMain;
    @BindView(R.id.elect_main_type)
    TextView electMainType;
    @BindView(R.id.tv_shrink)
    TextView tvShrink;
    @BindView(R.id.iv_arrow_elect)
    ImageView ivArrowElect;
    @BindView(R.id.elect_3_detail)
    TableLayout elect3Detail;
    @BindView(R.id.monitor_detail_tv_ble_pwd)
    TextView monitorDetailTvBlePwd;
    @BindView(R.id.ll_all_elect_detail)
    LinearLayout llAllElectDetail;
    @BindView(R.id.iv_alarm_status)
    ImageView ivAlarmStatus;
    @BindView(R.id.ac_monitoring_point_content_malfunction)
    RecyclerView acMonitoringPointContentMalfunction;
    @BindView(R.id.ll_elect_top)
    LinearLayout llElectTop;
    @BindView(R.id.ac_monitoring_elect_point_line)
    View acMonitoringElectPointLine;
    @BindView(R.id.monitor_detail_tv_category)
    TextView monitorDetailTvCategory;
    @BindView(R.id.ac_monitoring_point_rc_operation)
    RecyclerView acMonitoringPointRcOperation;
    //部署时间
    @BindView(R.id.ll_monitor_deploy_time)
    LinearLayout llMonitorDeployTime;
    @BindView(R.id.monitor_detail_tv_deploy_time)
    TextView monitorDetailTvDeployTime;
    //固件版本
    @BindView(R.id.rl_monitor_device_update)
    RelativeLayout rlMonitorDeviceUpdate;
    @BindView(R.id.monitor_detail_tv_device_vision)
    TextView monitorDetailTvDeviceVision;
    @BindView(R.id.iv_has_new_version)
    ImageView ivHasNewVersion;
    //信号
    @BindView(R.id.iv_monitor_signal)
    ImageView ivMonitorSignal;
    @BindView(R.id.tv_monitor_signal)
    TextView tvMonitorSignal;
    @BindView(R.id.iv_monitor_device_demo)
    ImageView ivMonitorDeviceDemo;
    @BindView(R.id.iv_line_device_demo)
    ImageView ivLineDeviceDemo;
    @BindView(R.id.rl_monitor_device_demo)
    RelativeLayout rlMonitorDeviceDemo;
    @BindView(R.id.rl_monitor_demo_mode_tip)
    RelativeLayout rlMonitorDemoModeTip;
    @BindView(R.id.iv_demo_mode_tip)
    ImageView ivDemoModeTip;
    @BindView(R.id.v_top_margin)
    View vTopMargin;
    @BindView(R.id.ll_monitor_deploy_cameras)
    LinearLayout llMonitorDeployCameras;
    @BindView(R.id.monitor_detail_tv_deploy_cameras)
    TextView monitorDetailTvDeployCameras;
    @BindView(R.id.line_monitor_deploy_cameras)
    View lineMonitorDeployCameras;
    @BindView(R.id.iv_line_device_config)
    ImageView ivLineDeviceConfig;
    @BindView(R.id.iv_monitor_device_config)
    ImageView ivMonitorDeviceConfig;
    @BindView(R.id.tv_device_detail_config_actual)
    TextView tvDeviceDetailConfigActual;
    @BindView(R.id.tv_device_detail_config_trans)
    TextView tvDeviceDetailConfigTrans;
    @BindView(R.id.monitor_detail_ll_device_config)
    LinearLayout monitorDetailLlDeviceConfig;
    @BindView(R.id.rl_monitor_device_config)
    RelativeLayout rlMonitorDeviceConfig;
    @BindView(R.id.tv_monitor_detail_imei)
    TextView tvMonitorDetailImei;
    @BindView(R.id.tv_monitor_detail_imsi)
    TextView tvMonitorDetailImsi;
    @BindView(R.id.tv_monitor_detail_iccid)
    TextView tvMonitorDetailIccid;
    @BindView(R.id.tv_monitor_detail_title_iccid)
    TextView tvMonitorDetailTitleIccid;
    @BindView(R.id.ll_2g_device_config)
    LinearLayout ll2gDeviceConfig;

    private boolean showDetail = false;
    private MonitoringPointRcContentAdapter mContentAdapter;
    private MonitoringPointRcMalfunctionContentAdapter mContentMalfunctionAdapter;
    private TagAdapter mTagAdapter;
    private ProgressUtils mProgressUtils;
    private TipOperationDialogUtils mTipUtils;
    private MonitorPointOperatingDialogUtil mOperatingUtil;
    private int mTipDialogType;
    private EarlyWarningThresholdDialogUtils earlyWarningThresholdDialogUtils;
    private MonitorDetailOperationAdapter monitorDetailOperationAdapter;
    private TipDeviceUpdateDialogUtils tipDeviceUpdateDialogUtils;
    private TipBleDialogUtils tipBleDialogUtils;
    private MonitorPointDemoDialogUtils mCloseDemoDialogUtils;
    private MonitorPointDemoDialogUtils mOpenDemoDialogUtils;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_monitoring_point_electric_detail);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    private void initView() {
        ivDemoModeTip.setColorFilter(mActivity.getResources().getColor(R.color.c_197358));
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());
        includeImvTitleTvTitle.setText(R.string.monitoring_point_details);
        includeTextTitleTvSubtitle.setText(R.string.historical_log);
        //
        mTagAdapter = new TagAdapter(mActivity, R.color.c_252525, R.color.c_dfdfdf);
        SensoroLinearLayoutManager layoutManager = new SensoroLinearLayoutManager(mActivity, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        int spacingInPixels = AppUtils.dp2px(mActivity, 8);
        monitorDetailRcTag.setIntercept(true);
        monitorDetailRcTag.setLayoutManager(layoutManager);
        monitorDetailRcTag.addItemDecoration(new SpacesItemDecoration(false, spacingInPixels, false, false));
        monitorDetailRcTag.setAdapter(mTagAdapter);
        //
        mContentAdapter = new MonitoringPointRcContentAdapter(mActivity);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        BottomNoDividerItemDecoration dividerItemDecoration = new BottomNoDividerItemDecoration(mActivity, BottomNoDividerItemDecoration.VERTICAL);
        acMonitoringPointRcContent.setLayoutManager(manager);
        acMonitoringPointRcContent.addItemDecoration(dividerItemDecoration);
        acMonitoringPointRcContent.setAdapter(mContentAdapter);
        //dialog
        initTipDialog();
        initEditDialog();
        initOperatingDialog();
        initMonitorPhoto();
        initMalfunctionData();
        initMonitorOperation();
        tipBleDialogUtils = new TipBleDialogUtils(mActivity);
        initEarlyWarningThresholdDialog();
        initTipDeviceUpdateDialog();
        initOpenDemoDialog();
        initCloseDemoDialog();

    }

    private void initCloseDemoDialog() {
        mCloseDemoDialogUtils = new MonitorPointDemoDialogUtils(mActivity);
        mCloseDemoDialogUtils.setDemoTitleText(getString(R.string.close_demo_mode));
        mCloseDemoDialogUtils.setDemoContent(getString(R.string.close_demo_mode_content));
        mCloseDemoDialogUtils.setDemoBtnText(getString(R.string.confirm_close));
        mCloseDemoDialogUtils.setMonitorPointDemoClickListener(new MonitorPointDemoDialogUtils.MonitorPointDemolickListener() {
            @Override
            public void onConfirmClick() {
                mPresenter.doDemoConfigSwitch(0);
            }
        });
    }

    private void initOpenDemoDialog() {
        mOpenDemoDialogUtils = new MonitorPointDemoDialogUtils(mActivity);
        mOpenDemoDialogUtils.setDemoTitleText(getString(R.string.open_demo_mode));
        mOpenDemoDialogUtils.setDemoContent(getString(R.string.open_demo_mode_content));
        mOpenDemoDialogUtils.setDemoDescription(getString(R.string.open_demo_mode_description));
        mOpenDemoDialogUtils.setDemoBtnText(getString(R.string.confirm_open));
        mOpenDemoDialogUtils.setMonitorPointDemoClickListener(new MonitorPointDemoDialogUtils.MonitorPointDemolickListener() {
            @Override
            public void onConfirmClick() {
                mPresenter.doDemoConfigSwitch(1);
            }
        });
    }

    private void initTipDeviceUpdateDialog() {
        tipDeviceUpdateDialogUtils = new TipDeviceUpdateDialogUtils(mActivity);
        tipDeviceUpdateDialogUtils.setTipDialogUtilsClickListener(mPresenter);
    }

    private void initEarlyWarningThresholdDialog() {
        earlyWarningThresholdDialogUtils = new EarlyWarningThresholdDialogUtils(mActivity);
        earlyWarningThresholdDialogUtils.setDialogUtilsChangeClickListener(this);
    }

    private void initMalfunctionData() {
        mContentMalfunctionAdapter = new MonitoringPointRcMalfunctionContentAdapter(mActivity);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        acMonitoringPointContentMalfunction.setLayoutManager(manager);
        acMonitoringPointContentMalfunction.setAdapter(mContentMalfunctionAdapter);
    }

    private void initMonitorPhoto() {
        //
        acMonitorDeployPhoto.setIntercept(false);
        SensoroLinearLayoutManager layoutManager = new SensoroLinearLayoutManager(mActivity, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.setReverseLayout(true);
        acMonitorDeployPhoto.addItemDecoration(new SpacesItemDecoration(false, AppUtils.dp2px(this, 8), false));
        acMonitorDeployPhoto.setLayoutManager(layoutManager);
        mAdapter = new MonitorDeployDetailPhotoAdapter(mActivity);
        acMonitorDeployPhoto.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
    }

    private void initMonitorOperation() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 4);
        acMonitoringPointRcOperation.setLayoutManager(gridLayoutManager);
        monitorDetailOperationAdapter = new MonitorDetailOperationAdapter(mActivity);
        acMonitoringPointRcOperation.setAdapter(monitorDetailOperationAdapter);
        monitorDetailOperationAdapter.setOnMonitorDetailOperationAdatperListener(mPresenter);

    }

    private void initEditDialog() {

    }

    private void initOperatingDialog() {
        mOperatingUtil = new MonitorPointOperatingDialogUtil(mActivity, false);
    }

    private void initTipDialog() {
        mTipUtils = new TipOperationDialogUtils(mActivity, false);
        mTipUtils.setTipDialogUtilsClickListener(this);
    }

    @Override
    protected MonitorPointDetailActivityPresenter createPresenter() {
        return new MonitorPointDetailActivityPresenter();
    }

    @Override
    public void startAC(Intent intent) {
        mActivity.startActivity(intent);
    }

    @Override
    public void finishAc() {
        mActivity.finish();
    }

    @Override
    public void startACForResult(Intent intent, int requestCode) {
        mActivity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void setIntentResult(int resultCode) {

    }

    @Override
    public void setIntentResult(int resultCode, Intent data) {

    }

    @Override
    public void showProgressDialog() {
        mProgressUtils.showProgress();
    }

    @Override
    public void dismissProgressDialog() {
        mProgressUtils.dismissProgress();
    }

    @Override
    public void toastShort(String msg) {
        SensoroToast.getInstance().makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {

    }

    @Override
    public void setTitleNameTextView(String name) {
        acMonitoringPointTvName.setText(name);
    }

    @Override
    public void setUpdateTime(String time) {
        acMonitoringPointTvTypeTime.setText(time);
    }

    @Override
    public void setAlarmStateColor(int color) {
        acMonitoringPointTvName.setTextColor(color);
        acMonitoringPointTvTypeTime.setTextColor(color);
    }

    @Override
    public void setContractName(String contractName) {
        acMonitoringPointTvAlertContactName.setText(contractName);
    }

    @Override
    public void setContractPhone(String contractPhone) {
        acMonitoringPointTvAlertContactPhone.setText(contractPhone);
    }

    @Override
    public void setContractCount(int count) {

        if (count > 1) {
            acMonitoringPointTvAlertContactPhone.setTextColor(getResources().getColor(R.color.c_252525));
            setContactPhoneIconVisible(true);
            acMonitoringPointTvPhoneCount.setText(getResources().getString(R.string.total) + count + getResources().getString(R.string.person));
        } else {
            acMonitoringPointTvAlertContactPhone.setTextColor(getResources().getColor(R.color.c_1dbb99));
            acMonitoringPointTvPhoneCount.setText("");
            setContactPhoneIconVisible(false);
        }
    }

    @Override
    public void setDeviceLocation(String location, boolean isArrowsRight) {
        acMonitoringPointTvLocation.setText(location);
        acMonitoringPointImvLocation.setVisibility(isArrowsRight ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateDeviceInfoAdapter(final List<MonitoringPointRcContentAdapterModel> data) {
        if (acMonitoringPointRcContent.isComputingLayout()) {
            acMonitoringPointRcContent.post(new Runnable() {
                @Override
                public void run() {
                    mContentAdapter.updateAdapter(data);
                }
            });
            return;
        }
        mContentAdapter.updateAdapter(data);
    }

    @Override
    public void updateDeviceMalfunctionInfoAdapter(final List<MonitoringPointRcContentAdapterModel> data) {
        if (data != null && data.size() > 0) {
            acMonitoringPointContentMalfunction.setVisibility(View.VISIBLE);
            if (acMonitoringPointContentMalfunction.isComputingLayout()) {
                acMonitoringPointContentMalfunction.post(new Runnable() {
                    @Override
                    public void run() {
                        mContentMalfunctionAdapter.updateAdapter(data);
                    }
                });
                return;
            }
            mContentMalfunctionAdapter.updateAdapter(data);
        } else {
            acMonitoringPointContentMalfunction.setVisibility(View.GONE);
        }
    }

    @Override
    public void setSNText(String sn) {
        monitorDetailTvSn.setText(sn);
    }

    @Override
    protected void onStart() {
        mPresenter.onStart();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.onStop();
    }

    @Override
    protected void onDestroy() {
//        if (mAlarmPopupView != null) {
//            mAlarmPopupView.onDestroyPop();
//        }
        if (mProgressUtils != null) {
            mProgressUtils.destroyProgress();
            mProgressUtils = null;
        }
        if (mTipUtils != null) {
            mTipUtils.destroy();
        }
        if (mOperatingUtil != null) {
            mOperatingUtil.destroy();
        }
        if (tipDeviceUpdateDialogUtils != null) {
            tipDeviceUpdateDialogUtils.destory();
        }
        if (mOpenDemoDialogUtils != null) {
            mOpenDemoDialogUtils.destroy();
        }
        if (mCloseDemoDialogUtils != null) {
            mCloseDemoDialogUtils.destroy();
        }
        SensoroSuccessToast.getInstance().cancelToast();
        super.onDestroy();
    }

    @Override
    public void updateTags(final List<String> list) {
        if (monitorDetailRcTag.isComputingLayout()) {
            monitorDetailRcTag.post(new Runnable() {
                @Override
                public void run() {
                    mTagAdapter.updateTags(list);
                }
            });
            return;
        }
        mTagAdapter.updateTags(list);
    }

    @Override
    public void updateMonitorPhotos(final List<ScenesData> data) {
        if (acMonitorDeployPhoto.isComputingLayout()) {
            acMonitorDeployPhoto.post(new Runnable() {
                @Override
                public void run() {
                    mAdapter.updateImages(data);
                }
            });
            return;
        }
        mAdapter.updateImages(data);
    }

    @Override
    public void setBatteryInfo(String battery) {
        monitorDetailTvBattery.setText(battery);
    }

    @Override
    public void setInterval(String interval) {
        monitorDetailTvInterval.setText(interval);
    }

    @Override
    public void setStatusInfo(String statusInfo, int textColor) {
        acMonitoringPointTvStatus.setText(statusInfo);
        acMonitoringPointTvStatus.setTextColor(textColor);
    }

    @Override
    public void setContactPhoneIconVisible(boolean isVisible) {
        acMonitoringPointImvPhoneArrow.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setNoContact() {
        acMonitoringPointTvAlertContactName.setText(R.string.no_contact_added);
        acMonitoringPointTvAlertContactName.setTextColor(mActivity.getResources().getColor(R.color.c_a6a6a6));
        acMonitoringPointView.setVisibility(View.GONE);
        acMonitoringPointTvAlertContactPhone.setVisibility(View.GONE);
        acMonitoringPointImvPhoneArrow.setVisibility(View.GONE);

    }

    @Override
    public void setDeviceLocationTextColor(int color) {
        acMonitoringPointTvLocation.setTextColor(mActivity.getResources().getColor(color));
    }

    @Override
    public void setDeviceTypeName(String typeName) {
        acMonitoringPointTvDeviceType.setText(typeName);
    }

    @Override
    public void setDeviceOperationVisible(boolean isVisible) {
        acMonitoringPointLlOperation.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }


    @Override
    public void showOperationSuccessToast() {
        SensoroSuccessToast.getInstance().showToast(mActivity, Toast.LENGTH_SHORT);
    }

    @Override
    public void showOperationSuccessToast(String text) {
        SensoroSuccessToast.getInstance().showToast(mActivity, Toast.LENGTH_LONG, text);
    }

    @Override
    public void showErrorTipDialog(String errorMsg) {
        if (mTipUtils.isShowing()) {
            mTipUtils.setTipMessageText(errorMsg);
            return;
        }
        mTipUtils.setTipEtRootVisible(false);
        mTipUtils.setDiameterVisible(false);
        mTipUtils.setTipTitleText(mActivity.getString(R.string.request_failed));
        mTipUtils.setTipMessageText(errorMsg);
        mTipUtils.setTipCancelText(mActivity.getString(R.string.back), mActivity.getResources().getColor(R.color.c_252525));
        mTipUtils.setTipConfirmVisible(false);
        mTipUtils.show();
    }

    @Override
    public void showOperationTipLoadingDialog() {
        if (mOperatingUtil != null) {
            switch (mTipDialogType) {
                case MonitorPointOperationCode.ERASURE:
                    mOperatingUtil.setTipText(mActivity.getString(R.string.erasuring));
                    break;
                case MonitorPointOperationCode.ERASURE_LONG:
                    mOperatingUtil.setTipText(mActivity.getString(R.string.erasuring));
                    break;
                case MonitorPointOperationCode.RESET:
                    mOperatingUtil.setTipText(mActivity.getString(R.string.reseting));
                    break;
                case MonitorPointOperationCode.PSD:
                    mOperatingUtil.setTipText(mActivity.getString(R.string.psd_modifing));
                    break;
                case MonitorPointOperationCode.QUERY:
                    mOperatingUtil.setTipText(mActivity.getString(R.string.quering));
                    break;
                case MonitorPointOperationCode.SELF_CHECK:
                    mOperatingUtil.setTipText(mActivity.getString(R.string.self_checking));
                    break;
                case MonitorPointOperationCode.AIR_SWITCH_CONFIG:
                    mOperatingUtil.setTipText(mActivity.getString(R.string.configuring));
                    break;
                case MonitorPointOperationCode.AIR_SWITCH_POWER_OFF:
                    mOperatingUtil.setTipText(mActivity.getString(R.string.configuring));
                    break;
                case MonitorPointOperationCode.AIR_SWITCH_POWER_ON:
                    mOperatingUtil.setTipText(mActivity.getString(R.string.configuring));
                    break;
                case MonitorPointOperationCode.ERASURE_TIME:
                    mOperatingUtil.setTipText(mActivity.getString(R.string.erasuring));
                    break;

            }
            if (!mOperatingUtil.isShowing()) {
                mOperatingUtil.show();
            }

        }
    }

    @Override
    public void showOperationTipLoadingDialog(String text) {
        if (mOperatingUtil != null) {
            mOperatingUtil.setTipText(text);
            if (!mOperatingUtil.isShowing()) {
                mOperatingUtil.show();
            }

        }
    }

    @Override
    public void setDeviceCamerasText(String text) {
        llMonitorDeployCameras.setVisibility(View.VISIBLE);
        lineMonitorDeployCameras.setVisibility(View.VISIBLE);
        monitorDetailTvDeployCameras.setText(text);
    }

    @Override
    public void setDeviceDetailConfigInfo(String actual, String trans) {
        rlMonitorDeviceConfig.setVisibility(View.VISIBLE);
        ivLineDeviceConfig.setVisibility(View.VISIBLE);
        tvDeviceDetailConfigActual.setText(actual);
        if (TextUtils.isEmpty(trans)) {
            tvDeviceDetailConfigTrans.setVisibility(View.GONE);
        } else {
            tvDeviceDetailConfigTrans.setVisibility(View.VISIBLE);
            tvDeviceDetailConfigTrans.setText(trans);
        }

    }

    @Override
    public void set2GDeviceConfigVisible(boolean visible) {
        ll2gDeviceConfig.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void set2GDeviceConfigInfo(String imei, String imsi, String iccid) {
        if (!TextUtils.isEmpty(imei)) {
            tvMonitorDetailImei.setText(imei);
        }
        if (!TextUtils.isEmpty(imsi)) {
            tvMonitorDetailImsi.setText(imsi);
        }
        if (!TextUtils.isEmpty(iccid)) {
            tvMonitorDetailIccid.setText(iccid);
        }
    }

    @Override
    public void setMonitorDetailTitleIccid(String title) {
        tvMonitorDetailTitleIccid.setText(title);
    }

    @Override
    public void dismissTipDialog() {
        if (mTipUtils != null) {
            mTipUtils.dismiss();
        }
    }

    @Override
    public void dismissOperatingLoadingDialog() {
        if (mOperatingUtil != null) {
            mOperatingUtil.dismiss();
        }
    }

    @Override
    public void setBlePwd(String pwd) {
        monitorDetailTvBlePwd.setText(pwd);
    }

    @Override
    public void setElectDetailVisible(boolean isVisible) {
        if (isVisible) {
            tvShrink.setText(R.string.collapse);
            ivArrowElect.setImageResource(R.drawable.arrow_up_elect);
        } else {
            tvShrink.setText(R.string.more_data);
            ivArrowElect.setImageResource(R.drawable.arrow_down_elect);
        }
        setLlAllElectDetailVisible(isVisible);
    }

    @Override
    public void setIvAlarmStatusVisible(boolean isVisible) {
        ivAlarmStatus.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setElect3DetailVisible(boolean isVisible) {
        elect3Detail.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void set3ElectADetail(Elect3DetailModel detailModel) {
        View virtualChildAt = trElectA.getVirtualChildAt(detailModel.index);
        if (virtualChildAt instanceof TextView) {
            ((TextView) virtualChildAt).setText(detailModel.text);
            if (detailModel.index != 0) {
                ((TextView) virtualChildAt).setBackgroundColor(mActivity.getResources().getColor(detailModel.backgroundColor));
                ((TextView) virtualChildAt).setTextColor(mActivity.getResources().getColor(detailModel.textColor));
            }
        }
    }

    @Override
    public void set3ElectTopDetail(Elect3DetailModel detailModel) {
        View virtualChildAt = trElectTop.getVirtualChildAt(detailModel.index);
        if (virtualChildAt instanceof TextView) {
            ((TextView) virtualChildAt).setText(detailModel.text);
        }

    }

    @Override
    public void set3ElectVDetail(Elect3DetailModel detailModel) {
        View virtualChildAt = trElectV.getVirtualChildAt(detailModel.index);
        if (virtualChildAt instanceof TextView) {
            ((TextView) virtualChildAt).setText(detailModel.text);
            if (detailModel.index != 0) {
                ((TextView) virtualChildAt).setBackgroundColor(mActivity.getResources().getColor(detailModel.backgroundColor));
                ((TextView) virtualChildAt).setTextColor(mActivity.getResources().getColor(detailModel.textColor));
            }
        }
    }

    @Override
    public void set3ElectTDetail(Elect3DetailModel detailModel) {
        View virtualChildAt = trElectT.getVirtualChildAt(detailModel.index);
        if (virtualChildAt instanceof TextView) {
            ((TextView) virtualChildAt).setText(detailModel.text);
            if (detailModel.index != 0) {
                ((TextView) virtualChildAt).setBackgroundColor(mActivity.getResources().getColor(detailModel.backgroundColor));
                ((TextView) virtualChildAt).setTextColor(mActivity.getResources().getColor(detailModel.textColor));
            }
        }
    }

    @Override
    public void updateEarlyWarningThresholdAdapterDialogUtils(List<EarlyWarningthresholdDialogUtilsAdapterModel> data) {
        if (earlyWarningThresholdDialogUtils != null) {
            earlyWarningThresholdDialogUtils.show(data);
        }
    }

    @Override
    public void dismissEarlyWarningThresholdAdapterDialogUtils() {
        if (earlyWarningThresholdDialogUtils != null) {
            earlyWarningThresholdDialogUtils.dismiss();
        }
    }

    @Override
    public void showUpdateDialogUtils(String title, String version, String date, boolean hasNewVersion) {
        if (tipDeviceUpdateDialogUtils != null) {
            tipDeviceUpdateDialogUtils.setTipTitleText(title);
            tipDeviceUpdateDialogUtils.setTipNewVersionText(version);
            tipDeviceUpdateDialogUtils.setTipVersionDateText(date);
            tipDeviceUpdateDialogUtils.setTipButtonVisible(hasNewVersion);
            tipDeviceUpdateDialogUtils.setCancelable(true);
            tipDeviceUpdateDialogUtils.setCanceledOnTouchOutside(true);
            tipDeviceUpdateDialogUtils.show();
        }
    }

    @Override
    public void dismissUpdateDialogUtils() {
        if (tipDeviceUpdateDialogUtils != null) {
            tipDeviceUpdateDialogUtils.dismiss();
        }
    }

    @Override
    public void updateDialogProgress(String msg, int progress, int status) {
        if (tipDeviceUpdateDialogUtils != null) {
            tipDeviceUpdateDialogUtils.setCancelable(false);
            tipDeviceUpdateDialogUtils.setCanceledOnTouchOutside(false);
            tipDeviceUpdateDialogUtils.updateDialog(msg, progress, status);
        }
    }

    @Override
    public void setIvHasNewVersionViewVisible(boolean isVisible) {
        ivHasNewVersion.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSignalStatus(int drawable, String text) {
        ivMonitorSignal.setImageResource(drawable);
        tvMonitorSignal.setText(text);
    }

    @Override
    public void setLlElectTopVisible(boolean isVisible) {
        llElectTop.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setTopElectData(String value, int color, String typeName) {
        tvElectMain.setText(value);
        tvElectMain.setTextColor(mActivity.getResources().getColor(color));
        electMainType.setText(typeName);
    }

    @Override
    public void setAcMonitoringElectPointLineVisible(boolean isVisible) {
        acMonitoringElectPointLine.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setLlAllElectDetailVisible(boolean isVisible) {
        llAllElectDetail.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setElectInfoTipVisible(boolean isVisible) {
        electInfo.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setMonitorDetailTvCategory(String category) {
        monitorDetailTvCategory.setText(category);
    }


    @OnClick({R.id.include_text_title_tv_subtitle, R.id.ac_monitoring_point_cl_alert_contact, R.id.ac_monitoring_point_imv_location, R.id.ac_monitoring_point_cl_location_navigation,
            R.id.ac_monitoring_point_imv_detail, R.id.include_text_title_imv_arrows_left, R.id.ll_elect_more, R.id.ll_all_info, R.id.rl_monitor_device_update, R.id.iv_monitor_device_demo, R.id.rl_monitor_demo_mode_tip, R.id.ll_monitor_deploy_cameras, R.id.rl_monitor_device_config})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.include_text_title_tv_subtitle:
                mPresenter.doMonitorHistory();
                break;
            case R.id.ac_monitoring_point_cl_alert_contact:
                mPresenter.doContact();
                break;
            case R.id.ac_monitoring_point_imv_location:
            case R.id.ac_monitoring_point_cl_location_navigation:
                mPresenter.doNavigation();
                break;
            case R.id.ac_monitoring_point_imv_detail:
                //已删除
                break;
            case R.id.include_text_title_imv_arrows_left:
                finishAc();
                break;
            case R.id.ll_elect_more:
                showDetail = !showDetail;
                setElectDetailVisible(showDetail);
                break;
            case R.id.ll_all_info:
                if (electInfo.getVisibility() == View.VISIBLE) {
                    mPresenter.showEarlyWarningThresholdDialogUtils();
                }
                break;
            case R.id.rl_monitor_device_update:
                //固件升级
//                toastShort("固件升级");
                mPresenter.doDeviceUpdate();
                break;
            case R.id.iv_monitor_device_demo:
            case R.id.rl_monitor_demo_mode_tip:
                mPresenter.showDemoModeDialog();
                break;
            case R.id.ll_monitor_deploy_cameras:
                mPresenter.doDeviceGroupCameras();
                break;
            case R.id.rl_monitor_device_config:
                mPresenter.goConfigDetailInfo();
                break;
        }
    }

    //TODO 包含慢炖空开配置
    @Override
    public void showTipDialog(boolean isEdit, String deviceType, CharSequence title, CharSequence message, @ColorRes int messageColor, CharSequence confirm, @ColorRes int confirmColor, int type) {
        if (mTipUtils.isShowing()) {
            mTipUtils.dismiss();
        }
        //控制线径显示
        mTipUtils.setDiameterVisible(false);
        mTipUtils.setTipEtRootVisible(isEdit);
        mTipUtils.setTipTitleText(title);
        mTipUtils.setTipMessageText(message, messageColor);
        mTipUtils.setTipConfirmVisible(true);
        mTipUtils.setTipCancelText(mActivity.getString(R.string.back), mActivity.getResources().getColor(R.color.c_252525));
        mTipUtils.setTipConfirmText(confirm, mActivity.getResources().getColor(confirmColor));
        mTipDialogType = type;
        mTipUtils.show();
    }

    @Override
    public void updateTaskOptionModelAdapter(final ArrayList<TaskOptionModel> optionModels) {
        if (acMonitoringPointRcOperation.isComputingLayout()) {
            acMonitoringPointRcOperation.post(new Runnable() {
                @Override
                public void run() {
                    monitorDetailOperationAdapter.updateMonitorDetailOperations(optionModels);
                }
            });
            return;
        }
        monitorDetailOperationAdapter.updateMonitorDetailOperations(optionModels);
    }

    @Override
    public void setMonitorDeployTime(String time) {
        monitorDetailTvDeployTime.setVisibility(View.VISIBLE);
        monitorDetailTvDeployTime.setText(time);
    }

    @Override
    public void setDeviceVision(String text) {
        monitorDetailTvDeviceVision.setText(text);
    }


    //tip dialog 点击事件
    @Override
    public void onCancelClick() {
        if (mTipUtils != null) {
            mTipUtils.dismiss();
        }
    }

    @Override
    public void onConfirmClick(String content, String diameter) {
        mPresenter.doOperation(mTipDialogType, content);
    }

    @Override
    public void onItemClick(View view, int position) {
        List<ScenesData> images = mAdapter.getImages();
        mPresenter.toPhotoDetail(position, images);
    }

    @Override
    public void onChangeInfoClick() {
        //TODO 前往修改阈值
    }

    @Override
    public void showBleTips() {
        if (tipBleDialogUtils != null && !tipBleDialogUtils.isShowing()) {
            tipBleDialogUtils.show();
        }
    }

    @Override
    public void hideBleTips() {
        if (tipBleDialogUtils != null && tipBleDialogUtils.isShowing()) {
            tipBleDialogUtils.dismiss();
        }
    }

    @Override
    public void setDeviceDemoModeViewStatus(int status) {
        switch (status) {
            case Constants.DEVICE_DEMO_MODE_NOT_SUPPORT:
                //不显示条目
                ivLineDeviceDemo.setVisibility(View.GONE);
                rlMonitorDeviceDemo.setVisibility(View.GONE);
                rlMonitorDemoModeTip.setVisibility(View.GONE);
                vTopMargin.setVisibility(View.VISIBLE);
                includeTextTitleDivider.setVisibility(View.VISIBLE);
                break;
            case Constants.DEVICE_DEMO_MODE_NO_PERMISSION:
                //不可点击
                ivLineDeviceDemo.setVisibility(View.VISIBLE);
                rlMonitorDeviceDemo.setVisibility(View.VISIBLE);
                ivMonitorDeviceDemo.setImageResource(R.drawable.ic_switch_none);
                rlMonitorDemoModeTip.setVisibility(View.GONE);
                vTopMargin.setVisibility(View.VISIBLE);
                includeTextTitleDivider.setVisibility(View.VISIBLE);
                break;
            case Constants.DEVICE_DEMO_MODE_OPEN:
                //演示状态
                ivLineDeviceDemo.setVisibility(View.VISIBLE);
                rlMonitorDeviceDemo.setVisibility(View.VISIBLE);
                ivMonitorDeviceDemo.setImageResource(R.drawable.ic_switch_open);
                rlMonitorDemoModeTip.setVisibility(View.VISIBLE);
                vTopMargin.setVisibility(View.GONE);
                includeTextTitleDivider.setVisibility(View.GONE);
                break;
            case Constants.DEVICE_DEMO_MODE_CLOSE:
                //非演示状态
                ivLineDeviceDemo.setVisibility(View.VISIBLE);
                rlMonitorDeviceDemo.setVisibility(View.VISIBLE);
                ivMonitorDeviceDemo.setImageResource(R.drawable.ic_switch_close);
                rlMonitorDemoModeTip.setVisibility(View.GONE);
                vTopMargin.setVisibility(View.VISIBLE);
                includeTextTitleDivider.setVisibility(View.VISIBLE);
                break;
            default:
                ivLineDeviceDemo.setVisibility(View.GONE);
                rlMonitorDeviceDemo.setVisibility(View.GONE);
                rlMonitorDemoModeTip.setVisibility(View.GONE);
                vTopMargin.setVisibility(View.VISIBLE);
                includeTextTitleDivider.setVisibility(View.VISIBLE);
                break;

        }
    }

    @Override
    public void showOpenDemoDialog() {
        if (mOpenDemoDialogUtils != null) {
            mOpenDemoDialogUtils.show();
        }

    }

    @Override
    public void dismissOpenDemoDialog() {
        if (mOpenDemoDialogUtils != null) {
            mOpenDemoDialogUtils.dismiss();
        }
    }

    @Override
    public void showCloseDemoDialog() {
        if (mCloseDemoDialogUtils != null) {
            mCloseDemoDialogUtils.show();
        }
    }

    public void dismissCloseDemoDialog() {
        if (mCloseDemoDialogUtils != null) {
            mCloseDemoDialogUtils.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (tipBleDialogUtils != null) {
            tipBleDialogUtils.onActivityResult(requestCode, resultCode, data);
        }
    }

}

