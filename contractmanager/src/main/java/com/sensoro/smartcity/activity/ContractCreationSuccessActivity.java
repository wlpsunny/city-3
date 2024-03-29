package com.sensoro.smartcity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.contractmanager.R;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.contractmanager.R2;
import com.sensoro.smartcity.imainviews.IContractCreationSuccessView;
import com.sensoro.smartcity.presenter.ContractCreationSuccessPresenter;
import com.sensoro.common.widgets.SensoroToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContractCreationSuccessActivity extends BaseActivity<IContractCreationSuccessView, ContractCreationSuccessPresenter>
        implements IContractCreationSuccessView {
    @BindView(R2.id.ac_contract_creation_success_tv_finish)
    TextView acContractCreationSuccessTvFinish;
    @BindView(R2.id.ac_contract_creation_success_tv_contract_preview)
    TextView acContractCreationSuccessTvContractPreview;
    @BindView(R2.id.ac_contract_creation_success_tv_create_qr_code)
    TextView acContractCreationSuccessTvCreateQrCode;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_contract_creation_success);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
    }

    private void initView() {

    }

    @Override
    protected ContractCreationSuccessPresenter createPresenter() {
        return new ContractCreationSuccessPresenter();
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

    }


    @OnClick({R2.id.ac_contract_creation_success_tv_finish, R2.id.ac_contract_creation_success_tv_contract_preview, R2.id.ac_contract_creation_success_tv_create_qr_code})
    public void onViewClicked(View view) {
        int viewID = view.getId();
        if (viewID == R.id.ac_contract_creation_success_tv_finish) {
            mPresenter.finish();
        } else if (viewID == R.id.ac_contract_creation_success_tv_contract_preview) {
            mPresenter.doContractPreview();
        } else if (viewID == R.id.ac_contract_creation_success_tv_create_qr_code) {
            mPresenter.doShareCode();
        }

    }
}
