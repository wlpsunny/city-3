package com.sensoro.smartcity.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.contractmanager.R;
import com.sensoro.contractmanager.R2;
import com.sensoro.smartcity.adapter.ContractTemplateShowAdapter;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.smartcity.imainviews.IContractInfoActivityView;
import com.sensoro.smartcity.presenter.ContractInfoActivityPresenter;
import com.sensoro.common.server.bean.ContractsTemplateInfo;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SensoroToast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContractInfoActivity extends BaseActivity<IContractInfoActivityView, ContractInfoActivityPresenter>
        implements IContractInfoActivityView {
    @BindView(R2.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R2.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R2.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R2.id.tv_contract_info_line1)
    TextView tvContractInfoLine1;
    @BindView(R2.id.et_contract_info_line1)
    TextView etContractInfoLine1;
    //
    @BindView(R2.id.ll_contract_info_phone)
    LinearLayout llContractInfoPhone;
    @BindView(R2.id.et_contract_info_phone)
    TextView etContractInfoPhone;
    //
    @BindView(R2.id.tv_contract_info_line2)
    TextView tvContractInfoLine2;
    @BindView(R2.id.et_contract_info_line2)
    TextView etContractInfoLine2;
    @BindView(R2.id.tv_contract_info_line3)
    TextView tvContractInfoLine3;
    @BindView(R2.id.et_contract_info_line3)
    TextView etContractInfoLine3;
    @BindView(R2.id.ll_contract_info_line4)
    LinearLayout llContractInfoLine4;
    @BindView(R2.id.tv_contract_info_line4)
    TextView tvContractInfoLine4;
    @BindView(R2.id.et_contract_info_line4)
    TextView etContractInfoLine4;
    @BindView(R2.id.ll_contract_info_line5)
    LinearLayout llContractInfoLine5;
    @BindView(R2.id.ll_contract_info_line3)
    LinearLayout llContractInfoLine3;
    @BindView(R2.id.tv_contract_info_line5)
    TextView tvContractInfoLine5;
    @BindView(R2.id.et_contract_info_line5)
    TextView etContractInfoLine5;
    @BindView(R2.id.ll_contract_info_line6)
    LinearLayout llContractInfoLine6;
    @BindView(R2.id.tv_contract_info_line6)
    TextView tvContractInfoLine6;
    @BindView(R2.id.et_contract_info_line6)
    TextView etContractInfoLine6;
    //
    @BindView(R2.id.tv_contract_service_place_type)
    TextView tvContractServicePlace;
    @BindView(R2.id.tv_service_age)
    TextView tvServiceAge;
    @BindView(R2.id.tv_contract_age)
    TextView tvContractAge;
    @BindView(R2.id.tv_service_age_first)
    TextView tvServiceAgeFirst;
    @BindView(R2.id.tv_contract_age_first)
    TextView tvContractAgeFirst;
    @BindView(R2.id.tv_service_age_period)
    TextView tvServiceAgePeriod;
    @BindView(R2.id.tv_contract_age_period)
    TextView tvContractAgePeriod;
    @BindView(R2.id.rv_sensor_info_count)
    RecyclerView rvSensorInfoCount;
    @BindView(R2.id.rl_service_info_sign_time)
    RelativeLayout rlServiceInfoSignTime;
    @BindView(R2.id.tv_service_sign)
    TextView tvServiceSign;
    @BindView(R2.id.tv_contract_sign)
    TextView tvContractSign;
    @BindView(R2.id.bt_confirm)
    Button btConfirm;
    @BindView(R2.id.iv_line_phone)
    ImageView ivLinePhone;
    @BindView(R2.id.iv_line2)
    ImageView ivLine2;
    @BindView(R2.id.iv_line3)
    ImageView ivLine3;
    @BindView(R2.id.iv_line4)
    ImageView ivLine4;
    @BindView(R2.id.iv_line5)
    ImageView ivLine5;
    @BindView(R2.id.iv_line6)
    ImageView ivLine6;
    @BindView(R2.id.tv_contract_info_place_title)
    TextView tvContractInfoPlaceTitle;
    @BindView(R2.id.ll_contract_info_place)
    LinearLayout llContractInfoPlace;
    @BindView(R2.id.ll_contract_info_layout)
    LinearLayout llContractInfoLayout;
    @BindView(R2.id.tv_service_status)
    TextView tvServiceStatus;
    @BindView(R2.id.tv_contract_status)
    TextView tvContractStatus;
    @BindView(R2.id.rl_service_info_status)
    RelativeLayout rlServiceInfoStatus;
    @BindView(R2.id.iv_line8)
    ImageView ivLine8;
    @BindView(R2.id.tv_service_crate_time)
    TextView tvServiceCrateTime;
    @BindView(R2.id.tv_contract_crete_time)
    TextView tvContractCreteTime;
    @BindView(R2.id.rl_service_info_create_time)
    RelativeLayout rlServiceInfoCreateTime;
    @BindView(R2.id.iv_line7)
    ImageView ivLine7;
    private ProgressUtils mProgressUtils;
    private ContractTemplateShowAdapter contractTemplateShowAdapter;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_contract_info);
        ButterKnife.bind(mActivity);
        initView();
        mPresenter.initData(mActivity);
    }

    @Override
    protected ContractInfoActivityPresenter createPresenter() {
        return new ContractInfoActivityPresenter();
    }


    private void initView() {
        includeTextTitleTvTitle.setText(mActivity.getString(R.string.contract_info_title));
        includeTextTitleTvSubtitle.setText(mActivity.getString(R.string.title_edit));
        includeTextTitleTvSubtitle.setTextColor(mActivity.getResources().getColor(R.color.c_1dbb99));
        includeTextTitleTvSubtitle.setVisibility(View.GONE);
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());
        contractTemplateShowAdapter = new ContractTemplateShowAdapter(mActivity);
        rvSensorInfoCount.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, true));
        rvSensorInfoCount.setAdapter(contractTemplateShowAdapter);
        rvSensorInfoCount.setNestedScrollingEnabled(false);
    }


    @Override
    protected void onDestroy() {
        if (mProgressUtils != null) {
            mProgressUtils.destroyProgress();
            mProgressUtils = null;
        }
        super.onDestroy();
    }

    @OnClick({R2.id.include_text_title_imv_arrows_left, R2.id.bt_confirm, R2.id.include_text_title_tv_subtitle})
    public void onViewClicked(View view) {
        int viewID = view.getId();
        if (viewID == R.id.include_text_title_imv_arrows_left) {
            finishAc();
        } else if (viewID == R.id.bt_confirm) {
            String text = btConfirm.getText().toString();
            mPresenter.startToConfirm(text);
        } else if (viewID == R.id.include_text_title_tv_subtitle) {
            mPresenter.startToEdit();
        }
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
        mActivity.setResult(resultCode);
    }

    @Override
    public void setIntentResult(int resultCode, Intent data) {
        mActivity.setResult(resultCode, data);
    }

    @Override
    public void showContentText(int type, String line1, String phone, String line2, String line3, String line4,
                                String line5, String line6, String place, String serviceAge, String serviceAgeFirst, String serviceAgePeriod) {
        tvContractServicePlace.setText(place);
        switch (type) {
            case 1:
                etContractInfoLine1.setText(line1);
                //
                etContractInfoPhone.setText(phone);
                //
                etContractInfoLine2.setText(line2);
                //
                //暂时先用这样判断，社会信用号代码
                if ("无".equals(line3)) {
                    ivLine3.setVisibility(View.GONE);
                    llContractInfoLine3.setVisibility(View.GONE);
                } else {
                    ivLine3.setVisibility(View.VISIBLE);
                    llContractInfoLine3.setVisibility(View.VISIBLE);
                    etContractInfoLine3.setText(line3);
                }
                //注册码
                if ("无".equals(line4)) {
                    ivLine4.setVisibility(View.GONE);
                    llContractInfoLine4.setVisibility(View.GONE);
                } else {
                    ivLine4.setVisibility(View.VISIBLE);
                    llContractInfoLine4.setVisibility(View.VISIBLE);
                    etContractInfoLine4.setText(line4);
                }

                //
                etContractInfoLine5.setText(line5);
                //
                etContractInfoLine6.setText(line6);
                //
                break;
            case 2:
                tvContractInfoLine1.setText(R.string.name);
                etContractInfoLine1.setText(line1);
                //
                etContractInfoPhone.setText(phone);
                //
                tvContractInfoLine2.setText(R.string.sexs);
                etContractInfoLine2.setText(line2);
                //
                tvContractInfoLine3.setText(R.string.identification_number);
                etContractInfoLine3.setText(line3);
                //
                tvContractInfoLine4.setText(R.string.address);
                etContractInfoLine4.setText(line4);
                //
                ivLine5.setVisibility(View.GONE);
                llContractInfoLine5.setVisibility(View.GONE);
//                //
                ivLine6.setVisibility(View.GONE);
                llContractInfoLine6.setVisibility(View.GONE);
                //
                break;
            case 3:
                tvContractInfoLine1.setText(R.string.party_a_customer_name);
                etContractInfoLine1.setText(line1);
                //
                ivLinePhone.setVisibility(View.GONE);
                llContractInfoPhone.setVisibility(View.GONE);
                //
                tvContractInfoLine2.setText(R.string.owners_name);
                etContractInfoLine2.setText(line2);
                //
                tvContractInfoLine3.setText(R.string.phone_num);
                etContractInfoLine3.setText(line3);
                //
//                tvContractInfoLine4.setText(R.string.address);
//                etContractInfoLine4.setText(line4);
//                //
//                ivLine5.setVisibility(View.GONE);
//                llContractInfoLine5.setVisibility(View.GONE);

                tvContractInfoLine4.setText("身份证号");
                etContractInfoLine4.setText(line4);
                //
                tvContractInfoLine5.setText(R.string.address);
                etContractInfoLine5.setText(line5);

//                //
                ivLine6.setVisibility(View.GONE);
                llContractInfoLine6.setVisibility(View.GONE);
                //
                break;
            default:
                break;
        }
        String year = mActivity.getString(R.string.year);
        if (TextUtils.isEmpty(serviceAge)) {
            serviceAge = "0";
        }
        if (TextUtils.isEmpty(serviceAgeFirst)) {
            serviceAgeFirst = "0";
        }
        if (TextUtils.isEmpty(serviceAgePeriod)) {
            serviceAgePeriod = "0";
        }
        tvContractAge.setText(serviceAge + year);
        tvContractAgeFirst.setText(serviceAgeFirst + year);
        tvContractAgePeriod.setText(serviceAgePeriod + year);
    }

    @Override
    public void updateContractTemplateAdapterInfo(List<ContractsTemplateInfo> data) {
        contractTemplateShowAdapter.updateList(data);
    }

    @Override
    public void setSignTime(String time) {
        ivLine7.setVisibility(View.VISIBLE);
        rlServiceInfoSignTime.setVisibility(View.VISIBLE);
        tvContractSign.setText(time);
    }

    @Override
    public void setConfirmText(String text) {
        btConfirm.setText(text);
    }

    @Override
    public void updateFirmOrPersonal(int contract_type) {
        switch (contract_type) {
            case 1:
                tvContractInfoLine1.setText(R.string.company_name);
                tvContractInfoLine2.setText(R.string.social_credit_code);
                break;
            case 2:
                break;
        }
    }

    @Override
    public void setConfirmVisible(boolean isConfirmed) {
        btConfirm.setVisibility(!isConfirmed ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setConfirmStatus(boolean confirmed) {
        includeTextTitleTvSubtitle.setVisibility(confirmed ? View.GONE : View.VISIBLE);
        ivLine8.setVisibility(View.VISIBLE);
        rlServiceInfoStatus.setVisibility(View.VISIBLE);
        tvContractStatus.setText(confirmed ? R.string.signed : R.string.not_signed);
        tvContractStatus.setTextColor(confirmed ? getResources().getColor(R.color.c_1dbb99) :
                getResources().getColor(R.color.c_ff8d34));
        tvContractStatus.setBackgroundResource(confirmed ? R.drawable.shape_bg_contract_stroke_1_29c_full_corner :
                R.drawable.shape_bg_contract_stroke_1_ff8d_full_corner);
    }

    @Override
    public void setContractCreateTime(String createdAt) {
        rlServiceInfoCreateTime.setVisibility(View.VISIBLE);
        tvContractCreteTime.setText(createdAt);
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
}
