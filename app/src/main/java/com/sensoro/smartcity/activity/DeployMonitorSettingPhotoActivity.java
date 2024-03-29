package com.sensoro.smartcity.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.model.ImageItem;
import com.sensoro.common.widgets.SelectDialog;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.smartcity.R;
import com.sensoro.common.adapter.ImagePickerAdapter;
import com.sensoro.smartcity.imainviews.IDeployMonitorSettingPhotoActivityView;
import com.sensoro.smartcity.presenter.DeployMonitorSettingPhotoActivityPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeployMonitorSettingPhotoActivity extends BaseActivity<IDeployMonitorSettingPhotoActivityView, DeployMonitorSettingPhotoActivityPresenter> implements
        IDeployMonitorSettingPhotoActivityView, ImagePickerAdapter.OnRecyclerViewItemClickListener {
    @BindView(R.id.rv_deploy_photo)
    RecyclerView rvDeployPhoto;
    @BindView(R.id.deploy_setting_photo_finish)
    TextView deploySettingPhotoFinish;
    @BindView(R.id.tv_deploy_photo_save)
    TextView tvDeployPhotoSave;
    private ImagePickerAdapter adapter;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_deploy_setting_photo);
        ButterKnife.bind(mActivity);
        initView();
        mPresenter.initData(mActivity);
    }

    private void initView() {
        adapter = new ImagePickerAdapter(mActivity, mPresenter.getSelImageList());
        adapter.setMaxImgCount(4);
        adapter.setOnItemClickListener(this);
        adapter.setAddTipText(mActivity.getString(R.string.live_photo));
        GridLayoutManager layoutManager = new GridLayoutManager(mActivity, 4);
        rvDeployPhoto.setLayoutManager(layoutManager);
        rvDeployPhoto.setHasFixedSize(true);
        rvDeployPhoto.setAdapter(adapter);
        //设置包裹不允许滑动，套一层父布局解决最后一项可能不显示的问题
        rvDeployPhoto.setNestedScrollingEnabled(false);
    }

    @Override
    public void showDialog(SelectDialog.SelectDialogListener listener, List<String> names, String title) {
        SelectDialog dialog = new SelectDialog(mActivity, R.style
                .transparentFrameWindowStyle,
                listener, names, title);
        if (!mActivity.isFinishing()) {
            dialog.show();
        }
    }

    @Override
    public void setJustDisplayPic(boolean isJustDisplay) {
        adapter.setJustDisplay(isJustDisplay);
    }

    @Override
    public void setSubtitleVisible(boolean isVisible) {
        tvDeployPhotoSave.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected DeployMonitorSettingPhotoActivityPresenter createPresenter() {
        return new DeployMonitorSettingPhotoActivityPresenter();
    }


    @OnClick({R.id.deploy_setting_photo_back, R.id.deploy_setting_photo_finish, R.id.tv_deploy_photo_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.deploy_setting_photo_back:
                finishAc();
                break;
            case R.id.deploy_setting_photo_finish:
            case R.id.tv_deploy_photo_save:
                mPresenter.doFinish();
                break;
        }
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
        mActivity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void setIntentResult(int resultCode) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setIntentResult(int resultCode, Intent data) {
    }

    @Override
    public void onItemClick(View view, int position) {
        int id = view.getId();
        List<ImageItem> images = adapter.getImages();
        mPresenter.clickItem(id, position, images);
    }

    @Override
    public void updateImageList(ArrayList<ImageItem> imageList) {
        adapter.setImages(imageList);
    }

    @Override
    public void toastShort(String msg) {
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {

    }
}
