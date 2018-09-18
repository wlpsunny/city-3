package com.sensoro.smartcity.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.NameAddressHistoryAdapter;
import com.sensoro.smartcity.base.BaseActivity;
import com.sensoro.smartcity.imainviews.INameAddressActivityView;
import com.sensoro.smartcity.presenter.NameAddressActivityPresenter;
import com.sensoro.smartcity.widget.RecycleViewItemClickListener;
import com.sensoro.smartcity.widget.SensoroLinearLayoutManager;
import com.sensoro.smartcity.widget.SensoroToast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NameAddressActivity extends BaseActivity<INameAddressActivityView, NameAddressActivityPresenter>
        implements INameAddressActivityView, RecycleViewItemClickListener {
    @BindView(R.id.ac_name_address_et)
    EditText acNameAddressEt;
    @BindView(R.id.ac_nam_address_ll)
    LinearLayout acNamAddressLl;
    @BindView(R.id.ac_nam_address_tv_history)
    TextView acNamAddressTvHistory;
    @BindView(R.id.ac_nam_address_rc_history)
    RecyclerView acNamAddressRcHistory;
    @BindView(R.id.ac_nam_address_tv_save)
    TextView acNamAddressTvSave;
    private NameAddressHistoryAdapter mHistoryAdapter;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_name_address);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
    }

    private void initView() {
        initRcHistory();
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
    protected NameAddressActivityPresenter createPresenter() {
        return new NameAddressActivityPresenter();
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
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);//从控件所在的窗口中隐藏
    }

    @Override
    public void toastShort(String msg) {
        SensoroToast.INSTANCE.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {

    }


    @OnClick(R.id.ac_nam_address_tv_save)
    public void onViewClicked() {
        String text = acNameAddressEt.getText().toString();
        mPresenter.doChoose(text);
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
    public void onItemClick(View view, int position) {
        String text = mHistoryAdapter.getSearchHistoryList().get(position).trim();
        setEditText(text);
        acNameAddressEt.clearFocus();
        dismissInputMethodManager(view);
    }
}