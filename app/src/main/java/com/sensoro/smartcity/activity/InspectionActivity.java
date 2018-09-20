package com.sensoro.smartcity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.TagAdapter;
import com.sensoro.smartcity.base.BaseActivity;
import com.sensoro.smartcity.imainviews.IInspectionActivityView;
import com.sensoro.smartcity.presenter.InspectionActivityPresenter;
import com.sensoro.smartcity.widget.TipDialogUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InspectionActivity extends BaseActivity<IInspectionActivityView, InspectionActivityPresenter>
        implements IInspectionActivityView ,TipDialogUtils.TipDialogUtilsClickListener{
    @BindView(R.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R.id.ac_inspection_tv_name)
    TextView acInspectionTvName;
    @BindView(R.id.ac_inspection_tv_sn)
    TextView acInspectionTvSn;
    @BindView(R.id.ac_inspection_rc_tag)
    RecyclerView acInspectionRcTag;
    @BindView(R.id.ac_inspection_tv_exception)
    TextView acInspectionTvException;
    @BindView(R.id.ac_inspection_tv_normal)
    TextView acInspectionTvNormal;
    private TagAdapter mTagAdapter;
    private TipDialogUtils mNormalDialog;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.actvity_inspection);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
    }

    private void initView() {
        includeTextTitleTvTitle.setText("巡检监测点");
        includeTextTitleTvSubtitle.setText("巡检内容");

        acInspectionTvName.setText("谁在乎我的心里有多苦谁在意我的明天去何处");
        acInspectionTvSn.setText("华安 123785");

        initRcTag();

        initNormalDialog();
    }

    private void initNormalDialog() {
        mNormalDialog = new TipDialogUtils(mActivity);
        mNormalDialog.setTipMessageText("确认监测点是否正常");
        mNormalDialog.setTipCacnleText("我再看看",mActivity.getResources().getColor(R.color.c_a6a6a6));
        mNormalDialog.setTipConfirmText("正常",mActivity.getResources().getColor(R.color.c_29c093));
        mNormalDialog.setTipDialogUtilsClickListener(this);
    }

    private void initRcTag() {
        mTagAdapter = new TagAdapter(mActivity);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        acInspectionRcTag.setLayoutManager(manager);
        acInspectionRcTag.setAdapter(mTagAdapter);
    }

    @Override
    protected InspectionActivityPresenter createPresenter() {
        return new InspectionActivityPresenter();
    }

    @Override
    public void startAC(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void finishAc() {
        finish();
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

    }

    @Override
    public void toastLong(String msg) {

    }


    @OnClick({R.id.include_text_title_imv_arrows_left, R.id.include_text_title_tv_subtitle, R.id.ac_inspection_tv_exception, R.id.ac_inspection_tv_normal})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.include_text_title_imv_arrows_left:
                finishAc();
                break;
            case R.id.include_text_title_tv_subtitle:
                mPresenter.doInspectionDetail();
                break;
            case R.id.ac_inspection_tv_exception:
                mPresenter.doUploadException();
                break;
            case R.id.ac_inspection_tv_normal:
                mPresenter.doNormal();
                break;
        }
    }

    @Override
    public void updateTagsData(List<String> tagList) {
        mTagAdapter.updateTags(tagList);
    }

    @Override
    public void showNormalDialog() {
        mNormalDialog.show();
    }

    @Override
    public void onCancelClick() {
        mNormalDialog.dismiss();
    }

    @Override
    public void onConfirmClick() {
        mNormalDialog.dismiss();
        //上传正常
    }
}