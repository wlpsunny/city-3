package com.sensoro.smartcity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.DeployDeviceTagAddTagAdapter;
import com.sensoro.smartcity.adapter.DeployDeviceTagHistoryTagAdapter;
import com.sensoro.smartcity.base.BaseActivity;
import com.sensoro.smartcity.imainviews.IDeployDeviceTagActivityView;
import com.sensoro.smartcity.presenter.DeployDeviceTagActivityPresenter;
import com.sensoro.smartcity.widget.RecycleViewItemClickListener;
import com.sensoro.smartcity.widget.SensoroLinearLayoutManager;
import com.sensoro.smartcity.widget.SensoroToast;
import com.sensoro.smartcity.widget.TagDialogUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.sensoro.smartcity.widget.TagDialogUtils.DIALOG_TAG_ADD;
import static com.sensoro.smartcity.widget.TagDialogUtils.DIALOG_TAG_EDIT;

public class DeployDeviceTagActivity extends BaseActivity<IDeployDeviceTagActivityView, DeployDeviceTagActivityPresenter>
        implements IDeployDeviceTagActivityView, DeployDeviceTagAddTagAdapter.DeployDeviceTagAddTagItemClickListener, RecycleViewItemClickListener, TagDialogUtils.OnTagDialogListener {
    @BindView(R.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R.id.ac_deploy_device_tag_commit)
    TextView acDeployDeviceTagCommit;
    @BindView(R.id.ac_deploy_device_tag_rc_add_tag)
    RecyclerView acDeployDeviceTagRcAddTag;
    @BindView(R.id.ac_deploy_device_tag_rc_history_tag)
    RecyclerView acDeployDeviceTagRcHistoryTag;
    private DeployDeviceTagAddTagAdapter mAddTagAdapter;
    private DeployDeviceTagHistoryTagAdapter mHistoryTagAdapter;
    private TagDialogUtils tagDialogUtils;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_deploy_device_tag);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
    }

    private void initView() {
        tagDialogUtils = new TagDialogUtils(mActivity);
        tagDialogUtils.registerListener(this);
        includeTextTitleTvTitle.setText("标签");
        includeTextTitleTvSubtitle.setVisibility(View.GONE);

        initRcAddTag();

        initRcHistoryTag();

    }


    private void initRcHistoryTag() {
        mHistoryTagAdapter = new DeployDeviceTagHistoryTagAdapter(mActivity);
        mHistoryTagAdapter.setRecycleViewItemClickListener(this);
        SensoroLinearLayoutManager manager = new SensoroLinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        acDeployDeviceTagRcHistoryTag.setLayoutManager(manager);
        acDeployDeviceTagRcHistoryTag.setAdapter(mHistoryTagAdapter);
    }

    private void initRcAddTag() {
        mAddTagAdapter = new DeployDeviceTagAddTagAdapter(mActivity);
        SensoroLinearLayoutManager manager = new SensoroLinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        acDeployDeviceTagRcAddTag.setLayoutManager(manager);
        acDeployDeviceTagRcAddTag.setAdapter(mAddTagAdapter);
        mAddTagAdapter.setDeployDeviceTagAddTagItemClickListener(this);
    }

    @Override
    protected DeployDeviceTagActivityPresenter createPresenter() {
        return new DeployDeviceTagActivityPresenter();
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
        SensoroToast.INSTANCE.makeText(msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {

    }

    @Override
    protected void onDestroy() {
        tagDialogUtils.unregisterListener();
        super.onDestroy();
    }

    @OnClick(R.id.ac_deploy_device_tag_commit)
    public void onViewClicked() {
        mPresenter.doFinish();
    }

    @Override
    public void onAddClick() {
        tagDialogUtils.show();
    }

    @Override
    public void onDeleteClick(int position) {
        mPresenter.clickDeleteTag(position);
    }

    @Override
    public void onClickItem(View v, int position) {
        mPresenter.doEditTag(position);
    }

    @Override
    public void updateTags(List<String> tags) {
        mAddTagAdapter.setTags(tags);
        mAddTagAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateSearchHistory(List<String> strHistory) {
        mHistoryTagAdapter.updateSearchHistoryAdapter(strHistory);
    }

    @Override
    public void showDialogWithEdit(String text,int position) {
        tagDialogUtils.show(text,position);
    }

    @Override
    public void dismissDialog() {
        tagDialogUtils.dismissDialog();
    }

    @Override
    public void onItemClick(View view, int position) {
        mPresenter.addTags(position);
    }

    @Override
    public void onConfirm(int type, String text,int position) {
        switch (type) {
            case DIALOG_TAG_ADD:
                mPresenter.addTags(text);
                break;
            case DIALOG_TAG_EDIT:
                mPresenter.updateEditTag(position,text);
                break;
            default:
                break;
        }
    }
}