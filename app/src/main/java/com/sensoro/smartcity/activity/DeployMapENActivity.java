package com.sensoro.smartcity.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.maps.MapView;
import com.sensoro.smartcity.R;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.smartcity.imainviews.IDeployMapENActivityView;
import com.sensoro.smartcity.presenter.DeployMapENActivityPresenter;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SensoroToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeployMapENActivity extends BaseActivity<IDeployMapENActivityView, DeployMapENActivityPresenter> implements IDeployMapENActivityView {
    @BindView(R.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R.id.include_text_title_cl_root)
    ConstraintLayout includeTextTitleClRoot;
    @BindView(R.id.tm_deploy_map_en)
    MapView tmDeployMap;
    @BindView(R.id.bt_deploy_map_signal)
    TextView btDeployMapSignal;
    @BindView(R.id.iv_deploy_map_location)
    ImageView ivDeployMapLocation;
    @BindView(R.id.tv_deploy_map_save)
    TextView tvDeployMapSave;
    private ProgressUtils mProgressUtils;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_deploy_map_en);
        ButterKnife.bind(this);
        tmDeployMap.onCreate(savedInstanceState);
        //
        iniView();
        mPresenter.initData(mActivity);
        tmDeployMap.getMapAsync(mPresenter);
    }

    private void iniView() {
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());
        includeTextTitleTvTitle.setText(R.string.deploy_device_detail_deploy_location);
        includeTextTitleTvSubtitle.setVisibility(View.GONE);
        mActivity.getWindow().getDecorView().postInvalidate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        tmDeployMap.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        tmDeployMap.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mProgressUtils != null) {
            mProgressUtils.destroyProgress();
            mProgressUtils = null;
        }
        super.onDestroy();
        tmDeployMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        tmDeployMap.onLowMemory();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onResume() {
        super.onResume();
        tmDeployMap.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onPause() {
        super.onPause();
        tmDeployMap.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        tmDeployMap.onSaveInstanceState(outState);
    }


    @Override
    public void refreshSignal(long updateTime, String signal) {
        String signal_text = null;
        long time_diff = System.currentTimeMillis() - updateTime;
        if (signal != null && (time_diff < 2 * 60 * 1000)) {
            switch (signal) {
                case "good":
                    signal_text = mActivity.getString(R.string.signal_excellent);
                    btDeployMapSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_good));
                    break;
                case "normal":
                    signal_text = mActivity.getString(R.string.signal_good);
                    btDeployMapSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_normal));
                    break;
                case "bad":
                    signal_text = mActivity.getString(R.string.signal_weak);
                    btDeployMapSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_bad));
                    break;
            }
        } else {
            signal_text = mActivity.getString(R.string.no_signal);
            btDeployMapSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_none));
        }
        btDeployMapSignal.setText(signal_text);
//        signalButton.setPadding(6, 10, 6, 10);
    }

    @Override
    public void refreshSignal(String signal) {
        String signal_text = null;
        if (signal != null) {
            switch (signal) {
                case "good":
                    signal_text = mActivity.getString(R.string.signal_excellent);
                    btDeployMapSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_good));
                    break;
                case "normal":
                    signal_text = mActivity.getString(R.string.signal_good);
                    btDeployMapSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_normal));
                    break;
                case "bad":
                    signal_text = mActivity.getString(R.string.signal_weak);
                    btDeployMapSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_bad));
                    break;
                default:
                    signal_text = mActivity.getString(R.string.no_signal);
                    btDeployMapSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_none));
                    break;
            }
        } else {
            signal_text = mActivity.getString(R.string.no_signal);
            btDeployMapSignal.setBackground(getResources().getDrawable(R.drawable.shape_signal_none));
        }
        btDeployMapSignal.setText(signal_text);
    }

    @Override
    public void setSubtitleVisible(boolean isVisible) {
        includeTextTitleTvSubtitle.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSignalVisible(boolean isVisible) {
        btDeployMapSignal.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSaveVisible(boolean isVisible) {
        tvDeployMapSave.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected DeployMapENActivityPresenter createPresenter() {
        return new DeployMapENActivityPresenter();
    }

    @Override
    public void startAC(Intent intent) {

    }

    @Override
    public void finishAc() {
        mActivity.finish();
    }

    @Override
    public void startACForResult(Intent intent, int requestCode) {

    }

    @Override
    public void setIntentResult(int resultCode) {

    }

    @Override
    public void setIntentResult(int resultCode, Intent data) {

    }

    @Override
    public void toastShort(String msg) {
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {

    }

    @OnClick({R.id.include_text_title_imv_arrows_left, R.id.bt_deploy_map_signal, R.id.tv_deploy_map_save, R.id.iv_deploy_map_location})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.include_text_title_imv_arrows_left:
                finishAc();
                break;
            case R.id.bt_deploy_map_signal:
//                mPresenter.refreshSignal();
                break;
            case R.id.tv_deploy_map_save:
                mPresenter.doSaveLocation();
                break;
            case R.id.iv_deploy_map_location:
                mPresenter.backToCurrentLocation();
                break;
        }
    }

    @Override
    public void showProgressDialog() {
        mProgressUtils.showProgress();
    }

    @Override
    public void dismissProgressDialog() {
        mProgressUtils.dismissProgress();
    }
}
