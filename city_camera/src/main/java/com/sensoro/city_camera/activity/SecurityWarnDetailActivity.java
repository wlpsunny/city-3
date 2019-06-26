package com.sensoro.city_camera.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sensoro.city_camera.IMainViews.ISecurityWarnDetailView;
import com.sensoro.city_camera.R;
import com.sensoro.city_camera.R2;
import com.sensoro.city_camera.dialog.SecurityControlPersonDetailsDialog;
import com.sensoro.city_camera.dialog.SecurityWarnConfirmDialog;
import com.sensoro.city_camera.presenter.SecurityWarnDetailPresenter;
import com.sensoro.city_camera.util.MapUtil;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.widgets.MaxHeightRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author : bin.tian
 * date   : 2019-06-24
 */
public class SecurityWarnDetailActivity extends BaseActivity<ISecurityWarnDetailView, SecurityWarnDetailPresenter>
        implements ISecurityWarnDetailView {

    @BindView(R2.id.include_text_title_imv_arrows_left)
    ImageView mBackIv;
    @BindView(R2.id.include_text_title_tv_title)
    TextView mTitleTv;
    @BindView(R2.id.include_text_title_tv_subtitle)
    TextView mSubtitle;
    @BindView(R2.id.security_warn_type_tv)
    TextView mSecurityWarnTypeTv;
    @BindView(R2.id.security_warn_title_tv)
    TextView mSecurityWarnTitleTv;
    @BindView(R2.id.security_warn_video_tv)
    TextView mSecurityWarnVideoTv;
    @BindView(R2.id.security_warn_camera_tv)
    TextView mSecurityWarnCameraNameTv;
    @BindView(R2.id.security_warn_deploy_tv)
    TextView mSecurityWarnDeployTv;
    @BindView(R2.id.security_warn_log_rv)
    MaxHeightRecyclerView mSecurityLogRv;
    @BindView(R2.id.security_warn_contact_owner_tv)
    TextView mSecurityWarnContactOwnerTv;
    @BindView(R2.id.security_warn_quick_navigation_tv)
    TextView mSecurityWarnNavigationTv;
    @BindView(R2.id.security_warn_alert_confirm_tv)
    TextView mSecurityWarnConfirmTv;

    private SecurityWarnConfirmDialog mSecurityWarnConfirmDialog;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.security_warn_detail_layout);
        ButterKnife.bind(this);

        mPresenter.initData(this);

        initView();

        MapUtil.startLocation(this);
    }

    @Override
    protected SecurityWarnDetailPresenter createPresenter() {
        return new SecurityWarnDetailPresenter();
    }

    private void initView() {
        mSubtitle.setVisibility(View.GONE);
        mTitleTv.setText(R.string.security_warn_detail_activity_title);


    }

    @OnClick({R2.id.security_warn_video_tv, R2.id.security_warn_camera_tv,
            R2.id.security_warn_deploy_tv, R2.id.security_warn_contact_owner_tv,
            R2.id.security_warn_quick_navigation_tv, R2.id.security_warn_alert_confirm_tv})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.security_warn_video_tv) {

        } else if (view.getId() == R.id.security_warn_camera_tv) {

        } else if (view.getId() == R.id.security_warn_deploy_tv) {

        } else if (view.getId() == R.id.security_warn_contact_owner_tv) {
//            SecurityCameraDetailsDialog cameraDetailsDialog = new SecurityCameraDetailsDialog();
//            cameraDetailsDialog.show(getSupportFragmentManager());
            SecurityControlPersonDetailsDialog securityControlPersonDetailsDialog = new SecurityControlPersonDetailsDialog();
            securityControlPersonDetailsDialog.show(getSupportFragmentManager());
//            SecurityWarnConfirmDialog warnConfirmDialog = new SecurityWarnConfirmDialog();
//            warnConfirmDialog.show(getSupportFragmentManager());

        } else if (view.getId() == R.id.security_warn_quick_navigation_tv) {
            mPresenter.doNavigation();
        } else if (view.getId() == R.id.security_warn_alert_confirm_tv) {
            mPresenter.doConfirm();
        }
    }

    @Override
    public void showConfirmDialog() {
        if (mSecurityWarnConfirmDialog == null) {
            mSecurityWarnConfirmDialog = new SecurityWarnConfirmDialog();
            mSecurityWarnConfirmDialog.setSecurityConfirmCallback(mPresenter);
        }
        Bundle bundle = new Bundle();
        bundle.putString(SecurityWarnConfirmDialog.EXTRA_KEY_SECURITY_ID, "");
        bundle.putString(SecurityWarnConfirmDialog.EXTRA_KEY_SECURITY_TITLE, "");
        bundle.putString(SecurityWarnConfirmDialog.EXTRA_KEY_SECURITY_TIME, "");
        bundle.putString(SecurityWarnConfirmDialog.EXTRA_KEY_SECURITY_TYPE, "");
        mSecurityWarnConfirmDialog.setArguments(bundle);
        mSecurityWarnConfirmDialog.show(getSupportFragmentManager());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MapUtil.stopLocation();
    }
}
