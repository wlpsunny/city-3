package com.sensoro.smartcity.activity;


/**
 * @Author: jack
 * 时  间: 2019-09-09
 * 包  名: com.sensoro.smartcity.activity
 * 类  名: InspectionInstructionActivity
 * 简  述: <巡检内容页,要点：设备名称，巡检图片列表>
 */
import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sensoro.inspectiontask.R;
import com.sensoro.inspectiontask.R2;
import com.sensoro.smartcity.adapter.InspectionInstructionContentAdapter;
import com.sensoro.smartcity.adapter.InspectionInstructionTabAdapter;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.smartcity.imainviews.IInspectionInstructionActivityView;
import com.sensoro.smartcity.presenter.InspectionInstructionActivityPresenter;
import com.sensoro.common.server.bean.InspectionTaskInstructionModel;
import com.sensoro.common.server.bean.ScenesData;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.callback.RecycleViewItemClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InspectionInstructionActivity extends BaseActivity<IInspectionInstructionActivityView, InspectionInstructionActivityPresenter>
        implements IInspectionInstructionActivityView {
    @BindView(R2.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R2.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R2.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R2.id.include_text_title_cl_root)
    ConstraintLayout includeTextTitleClRoot;
    @BindView(R2.id.ac_inspection_instruction_rc_tab)
    RecyclerView acInspectionInstructionRcTab;
    @BindView(R2.id.ac_inspection_instruction_rc_content)
    RecyclerView acInspectionInstructionRcContent;
    private InspectionInstructionTabAdapter mTabAdapter;
    private InspectionInstructionContentAdapter mContentAdapter;

    private ProgressUtils mProgressUtils;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_inspection_instruction);
        ButterKnife.bind(this);
        iniView();
        mPresenter.initData(mActivity);
    }

    private void iniView() {
        includeTextTitleTvTitle.setText(R.string.inspection_content);
        includeTextTitleTvSubtitle.setVisibility(View.GONE);
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());

        initRcTab();

        initRcContent();
    }

    private void initRcContent() {
        mContentAdapter = new InspectionInstructionContentAdapter(mActivity);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        acInspectionInstructionRcContent.setLayoutManager(manager);
        acInspectionInstructionRcContent.setAdapter(mContentAdapter);

        mContentAdapter.setOnInspectionInstructionContentPicClickListenter(new InspectionInstructionContentAdapter.OnInspectionInstructionContentPicClickListenter() {
            @Override
            public void onInspectionInstructionContentPicClick(List<ScenesData> dataList, int position) {
                mPresenter.doPreviewPhoto(dataList, position);
            }
        });
    }

    private void initRcTab() {
        mTabAdapter = new InspectionInstructionTabAdapter(mActivity);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        acInspectionInstructionRcTab.setLayoutManager(manager);
        acInspectionInstructionRcTab.setAdapter(mTabAdapter);

        mTabAdapter.setRecycleViewItemClickListener(new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPresenter.doRequestTemplate(mTabAdapter.getItem(position));
            }
        });

    }

    @Override
    protected InspectionInstructionActivityPresenter createPresenter() {
        return new InspectionInstructionActivityPresenter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressUtils != null) {
            mProgressUtils.destroyProgress();
            mProgressUtils = null;
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

    }

    @Override
    public void toastLong(String msg) {

    }


    @OnClick(R2.id.include_text_title_imv_arrows_left)
    public void onViewClicked() {
        finishAc();
    }

    @Override
    public void updateRcContentData(List<InspectionTaskInstructionModel.DataBean> data) {
        mContentAdapter.updateDataList(data);
    }

    @Override
    public void updateRcTag(List<String> deviceTypes) {
        mTabAdapter.updateTagDataList(deviceTypes);
    }
}
