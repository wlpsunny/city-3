package com.sensoro.smartcity.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.NameAddressHistoryAdapter;
import com.sensoro.smartcity.base.BaseActivity;
import com.sensoro.smartcity.imainviews.IDeployMonitorNameAddressActivityView;
import com.sensoro.smartcity.presenter.DeployMonitorNameAddressActivityPresenter;
import com.sensoro.smartcity.widget.ProgressUtils;
import com.sensoro.smartcity.widget.RecycleViewItemClickListener;
import com.sensoro.smartcity.widget.SensoroLinearLayoutManager;
import com.sensoro.smartcity.widget.toast.SensoroToast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeployMonitorNameAddressActivity extends BaseActivity<IDeployMonitorNameAddressActivityView, DeployMonitorNameAddressActivityPresenter>
        implements IDeployMonitorNameAddressActivityView, RecycleViewItemClickListener {
    @BindView(R.id.ac_name_address_et)
    EditText acNameAddressEt;
    @BindView(R.id.ac_nam_address_ll)
    LinearLayout acNamAddressLl;
    @BindView(R.id.ac_nam_address_tv_history)
    TextView acNamAddressTvHistory;
    @BindView(R.id.ac_nam_address_rc_history)
    RecyclerView acNamAddressRcHistory;

    @BindView(R.id.include_text_title_tv_cancel)
    TextView includeTextTitleTvCancel;
    @BindView(R.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    private NameAddressHistoryAdapter mHistoryAdapter;
    private ProgressUtils mProgressUtils;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_name_address);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
    }

    private void initView() {
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());
        initTitle();
        initRcHistory();
    }

    private void initTitle() {
        includeTextTitleTvTitle.setText(R.string.name_address);
        includeTextTitleTvCancel.setVisibility(View.VISIBLE);
        includeTextTitleTvCancel.setTextColor(getResources().getColor(R.color.c_b6b6b6));
        includeTextTitleTvCancel.setText(R.string.cancel);
        includeTextTitleTvSubtitle.setVisibility(View.VISIBLE);
        includeTextTitleTvSubtitle.setText(getString(R.string.save));
        includeTextTitleTvSubtitle.setTextColor(getResources().getColor(R.color.c_29c093));
    }

    private void initRcHistory() {
        mHistoryAdapter = new NameAddressHistoryAdapter(mActivity);
        mHistoryAdapter.setRecycleViewItemClickListener(this);
        SensoroLinearLayoutManager manager = new SensoroLinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        acNamAddressRcHistory.setLayoutManager(manager);
        acNamAddressRcHistory.setAdapter(mHistoryAdapter);
        acNameAddressEt.requestFocus();
    }

    @Override
    protected DeployMonitorNameAddressActivityPresenter createPresenter() {
        return new DeployMonitorNameAddressActivityPresenter();
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

    private void dismissInputMethodManager(View view) {
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);//从控件所在的窗口中隐藏
        }
    }

    @Override
    public void toastShort(String msg) {
        SensoroToast.INSTANCE.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {

    }


    @OnClick({R.id.include_text_title_tv_subtitle, R.id.include_text_title_tv_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.include_text_title_tv_subtitle:
                String text = acNameAddressEt.getText().toString();
                mPresenter.doChoose(text);
                break;
            case R.id.include_text_title_tv_cancel:
                finishAc();
                break;
        }

    }

    @Override
    public void setEditText(String text) {
        acNameAddressEt.setText(text);
        acNameAddressEt.setSelection(text.length());
    }

    @Override
    public void updateSearchHistoryData(List<String> searchStr) {
        mHistoryAdapter.updateSearchHistoryAdapter(searchStr);
    }

    @Override
    public void updateTvTitle(String sn) {
        includeTextTitleTvTitle.setText(sn);
    }

    @Override
    public void onItemClick(View view, int position) {
        String text = mHistoryAdapter.getSearchHistoryList().get(position).trim();
        setEditText(text);
        acNameAddressEt.clearFocus();
        dismissInputMethodManager(view);
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
    protected void onDestroy() {
        mProgressUtils.destroyProgress();
        super.onDestroy();
    }
}
