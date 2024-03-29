package com.sensoro.smartcity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sensoro.common.adapter.TagAdapter;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.manger.SensoroLinearLayoutManager;
import com.sensoro.common.server.bean.ScenesData;
import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.common.widgets.SpacesItemDecoration;
import com.sensoro.common.widgets.TouchRecycleView;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.MonitorDeployDetailPhotoAdapter;
import com.sensoro.smartcity.imainviews.IOfflineDeployTaskDetailActivityView;
import com.sensoro.smartcity.presenter.OfflineDeployTaskDetailActivityPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OfflineDeployTaskDetailActivity extends BaseActivity<IOfflineDeployTaskDetailActivityView, OfflineDeployTaskDetailActivityPresenter>
        implements IOfflineDeployTaskDetailActivityView, MonitorDeployDetailPhotoAdapter.OnRecyclerViewItemClickListener {
    @BindView(R.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R.id.include_text_title_cl_root)
    ConstraintLayout includeTextTitleClRoot;
    @BindView(R.id.ac_deploy_record_detail_tv_name)
    TextView acDeployRecordDetailTvName;
    @BindView(R.id.ac_deploy_record_detail_rc_tag)
    RecyclerView acDeployRecordDetailRcTag;
    @BindView(R.id.ac_deploy_record_detail_tv_time)
    TextView acDeployRecordDetailTvTime;
    @BindView(R.id.ll_contacts)
    LinearLayout llContacts;
    @BindView(R.id.tv_first_contact)
    TextView tvFirstContact;
    @BindView(R.id.tv_total_contact)
    TextView tvTotalContact;
    @BindView(R.id.iv_contact_arrow)
    ImageView ivContactArrow;
    @BindView(R.id.ac_deploy_record_detail_tv_we_chat)
    TextView acDeployRecordDetailTvWeChat;
    @BindView(R.id.ac_deploy_record_detail_tv_fixed_point_state)
    TextView acDeployRecordDetailTvFixedPointState;
    @BindView(R.id.ac_deploy_record_detail_ll_fixed_point)
    LinearLayout acDeployRecordDetailLlFixedPoint;
    @BindView(R.id.ac_deploy_record_detail_ll_deploy_pic)
    LinearLayout acDeployRecordDetailLlDeployPic;
    @BindView(R.id.ac_deploy_device_record_detail_tv_device_type)
    TextView acDeployDeviceRecordDetailTvDeviceType;
    @BindView(R.id.ac_deploy_device_record_detail_tv_device_sn)
    TextView acDeployDeviceRecordDetailTvDeviceSn;
    @BindView(R.id.ll_deploy_record_detail_we_chat)
    LinearLayout llDeployRecordDetailWeChat;
    @BindView(R.id.line_we_chat)
    View lineWeChat;
    @BindView(R.id.include_text_title_divider)
    View includeTextTitleDivider;
    @BindView(R.id.ac_deploy_record_detail_rc_deploy_pic)
    TouchRecycleView acDeployRecordDetailRcDeployPic;
    @BindView(R.id.ac_deploy_record_detail_tv_deploy_staff)
    TextView acDeployRecordDetailTvDeployStaff;
    @BindView(R.id.ac_deploy_device_detail_deploy_record_location_line)
    View acDeployDeviceDetailDeployRecordLocationLine;
    @BindView(R.id.tv_ac_deploy_device_record_detail_force_deploy_reson)
    TextView tvAcDeployDeviceRecordDetailForceDeployReason;
    @BindView(R.id.iv_deploy_record_config)
    ImageView ivDeployRecordConfig;
    @BindView(R.id.tv_deploy_record_config_actual)
    TextView tvDeployRecordConfigActual;
    @BindView(R.id.tv_deploy_record_config_trans)
    TextView tvDeployRecordConfigTrans;
    @BindView(R.id.ll_deploy_record_config)
    LinearLayout llDeployRecordConfig;
    @BindView(R.id.rl_deploy_record_config)
    RelativeLayout rlDeployRecordConfig;
    @BindView(R.id.line_deploy_record_config)
    View lineDeployRecordConfig;
    private TagAdapter mTagAdapter;
    private MonitorDeployDetailPhotoAdapter mDeployPicAdapter;


    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.actvity_offline_deploy_task_detail);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
    }

    private void initView() {
        includeTextTitleTvSubtitle.setVisibility(View.GONE);
        includeTextTitleTvTitle.setVisibility(View.VISIBLE);
        includeTextTitleTvTitle.setText(R.string.offline_deploy_task_detail_title);
        initRcTag();
        if (!AppUtils.isChineseLanguage()) {
            lineWeChat.setVisibility(View.GONE);
            llDeployRecordDetailWeChat.setVisibility(View.GONE);
        }
        initRcContact();
        initRcDeployPic();
    }

    private void initRcDeployPic() {
        acDeployRecordDetailRcDeployPic.setIntercept(false);
        SensoroLinearLayoutManager layoutManager = new SensoroLinearLayoutManager(mActivity, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.setReverseLayout(true);
        acDeployRecordDetailRcDeployPic.addItemDecoration(new SpacesItemDecoration(false, AppUtils.dp2px(this, 8), false));
        acDeployRecordDetailRcDeployPic.setLayoutManager(layoutManager);
        mDeployPicAdapter = new MonitorDeployDetailPhotoAdapter(mActivity);
        acDeployRecordDetailRcDeployPic.setAdapter(mDeployPicAdapter);
        mDeployPicAdapter.setOnItemClickListener(this);
    }

    private void initRcContact() {
    }

    private void initRcTag() {
        mTagAdapter = new TagAdapter(mActivity, R.color.c_252525, R.color.c_dfdfdf);
        SensoroLinearLayoutManager layoutManager = new SensoroLinearLayoutManager(mActivity);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        acDeployRecordDetailRcTag.setLayoutManager(layoutManager);
        int spacingInPixels = mActivity.getResources().getDimensionPixelSize(R.dimen.x10);
        acDeployRecordDetailRcTag.addItemDecoration(new SpacesItemDecoration(false, spacingInPixels));
        acDeployRecordDetailRcTag.setAdapter(mTagAdapter);

    }

    @Override
    protected OfflineDeployTaskDetailActivityPresenter createPresenter() {
        return new OfflineDeployTaskDetailActivityPresenter();
    }

    @Override
    public void startAC(Intent intent) {
        mActivity.startActivity(intent);
    }

    @Override
    public void finishAc() {
        finish();
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

    }

    @Override
    public void dismissProgressDialog() {

    }

    @Override
    public void toastShort(String msg) {
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_LONG).show();
    }


    @OnClick({R.id.include_text_title_imv_arrows_left, R.id.ac_deploy_record_detail_ll_fixed_point, R.id.include_text_title_tv_subtitle, R.id.rl_deploy_record_config, R.id.ll_contacts})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.include_text_title_imv_arrows_left:
                finishAc();
                break;
            case R.id.ac_deploy_record_detail_ll_fixed_point:
                mPresenter.doFixedPoint();
                break;
            case R.id.rl_deploy_record_config:
                mPresenter.goConfigDetail();
                break;
            case R.id.include_text_title_tv_subtitle:
                finishAc();
                break;
            case R.id.ll_contacts:
                //
                mPresenter.showContactDialog();
                break;
        }
    }

    @Override
    public void setSNTitle(String sn) {
        acDeployDeviceRecordDetailTvDeviceSn.setText(sn);
    }

    @Override
    public void setDeviceName(String deviceName) {
        acDeployRecordDetailTvName.setText(deviceName);
    }

    @Override
    public void updateTagList(List<String> tags) {
        mTagAdapter.updateTags(tags);
    }

    @Override
    public void setDeployTime(String time) {
        acDeployRecordDetailTvTime.setText(time);
    }

    @Override
    public void seDeployWeChat(String text) {
        acDeployRecordDetailTvWeChat.setText(text);
    }

    @Override
    public void updateDeployPic(ArrayList<ScenesData> data) {
        mDeployPicAdapter.updateImages(data);
    }

    @Override
    public void setPositionStatus(int status) {
        switch (status) {
            case 0:
                acDeployRecordDetailTvFixedPointState.setText(mActivity.getString(R.string.not_positioned));
                acDeployRecordDetailTvFixedPointState.setTextColor(mActivity.getResources().getColor(R.color.c_a6a6a6));
                break;
            case 1:
                acDeployRecordDetailTvFixedPointState.setText(mActivity.getString(R.string.positioned));
                acDeployRecordDetailTvFixedPointState.setTextColor(mActivity.getResources().getColor(R.color.c_252525));
                break;

        }
    }

    @Override
    public void refreshSingle(String signalQuality) {
//        String signal_text = null;
//        if (signalQuality != null) {
//            switch (signalQuality) {
//                case "good":
//                    signal_text = mActivity.getString(R.string.signal_excellent);
//                    acDeployRecordDetailTvFixedPointSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_good));
//                    break;
//                case "normal":
//                    signal_text = mActivity.getString(R.string.signal_good);
//                    acDeployRecordDetailTvFixedPointSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_normal));
//                    break;
//                case "bad":
//                    signal_text = mActivity.getString(R.string.signal_weak);
//                    acDeployRecordDetailTvFixedPointSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_bad));
//                    break;
//                default:
//                    signal_text = mActivity.getString(R.string.no_signal);
//                    acDeployRecordDetailTvFixedPointSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_none));
//                    break;
//            }
//        } else {
//            signal_text = mActivity.getString(R.string.no_signal);
//            acDeployRecordDetailTvFixedPointSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_none));
//        }
//        acDeployRecordDetailTvFixedPointSignal.setText(signal_text);
    }

    @Override
    public void setDeployDeviceRecordDeviceType(String text) {
        acDeployDeviceRecordDetailTvDeviceType.setText(text);
    }

    @Override
    public void setDeployDetailDeploySettingVisible(boolean isVisible) {
        rlDeployRecordConfig.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        lineDeployRecordConfig.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setForceDeployReason(String reason) {
        if (TextUtils.isEmpty(reason)) {
            tvAcDeployDeviceRecordDetailForceDeployReason.setVisibility(View.GONE);
        } else {
            tvAcDeployDeviceRecordDetailForceDeployReason.setVisibility(View.VISIBLE);
            tvAcDeployDeviceRecordDetailForceDeployReason.setText(reason);
        }
    }

    @Override
    public void setDeployRecordDetailDeployStaff(String text) {
        acDeployRecordDetailTvDeployStaff.setText(text);
    }

    @Override
    public void setDeployDetailConfigInfo(String actual, String trans) {
        lineDeployRecordConfig.setVisibility(View.VISIBLE);
        rlDeployRecordConfig.setVisibility(View.VISIBLE);
        tvDeployRecordConfigActual.setText(actual);
        if (TextUtils.isEmpty(trans)) {
            tvDeployRecordConfigTrans.setVisibility(View.GONE);
        } else {
            tvDeployRecordConfigTrans.setVisibility(View.VISIBLE);
            tvDeployRecordConfigTrans.setText(trans);
        }
    }

    @Override
    public void setFirstContact(String contact) {
        if (!TextUtils.isEmpty(contact)) {
            tvFirstContact.setText(contact);
        }
    }

    @Override
    public void setTotalContact(int total) {
        if (total > 1) {
            tvTotalContact.setText(getString(R.string.total) + total + getString(R.string.person));
            tvTotalContact.setVisibility(View.VISIBLE);
            ivContactArrow.setVisibility(View.VISIBLE);
        } else {
            tvTotalContact.setVisibility(View.GONE);
            ivContactArrow.setVisibility(View.GONE);
        }
    }


    @Override
    public void onItemClick(View view, int position) {
        List<ScenesData> images = mDeployPicAdapter.getImages();
        mPresenter.toPhotoDetail(position, images);
    }

}
