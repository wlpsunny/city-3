package com.sensoro.smartcity.activity;
/**
 * @Author: jack
 * 时  间: 2019-09-09
 * 包  名: com.sensoro.smartcity.activity
 * 类  名: InspectionExceptionDetailActivity
 * 简  述: <巡检异常详情页>
 */
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.sensoro.common.adapter.TagAdapter;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.constant.ARouterConstants;
import com.sensoro.common.server.bean.ScenesData;
import com.sensoro.common.utils.WidgetUtil;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.inspectiontask.R;
import com.sensoro.inspectiontask.R2;
import com.sensoro.smartcity.adapter.InspectionExceptionThumbnailAdapter;
import com.sensoro.smartcity.imainviews.IInspectionExceptionDetailActivityView;
import com.sensoro.smartcity.presenter.InspectionExceptionDetailActivityPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@Route(path = ARouterConstants.ACTIVITY_INSPECTION_EXCEPTION_DETAIL)
public class InspectionExceptionDetailActivity extends BaseActivity<IInspectionExceptionDetailActivityView,
        InspectionExceptionDetailActivityPresenter> implements IInspectionExceptionDetailActivityView {
    @BindView(R2.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R2.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R2.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R2.id.ac_inspection_exception_detail_tv_name)
    TextView acInspectionExceptionDetailTvName;
    @BindView(R2.id.ac_inspection_exception_detail_tv_sn)
    TextView acInspectionExceptionDetailTvSn;
    @BindView(R2.id.ac_inspection_exception_detail_rc_tag)
    RecyclerView acInspectionExceptionDetailRcTag;
    @BindView(R2.id.ac_inspection_exception_detail_tv_state)
    TextView acInspectionExceptionDetailTvState;
    @BindView(R2.id.ac_inspection_exception_detail_rc_exception_tag)
    RecyclerView acInspectionExceptionDetailRcExceptionTag;
    @BindView(R2.id.ac_inspection_exception_detail_tv_remark)
    TextView acInspectionExceptionDetailTvRemark;
    @BindView(R2.id.ac_inspection_exception_detail_rc_photo)
    RecyclerView acInspectionExceptionDetailRcPhoto;
    @BindView(R2.id.ac_inspection_exception_detail_rc_camera)
    RecyclerView acInspectionExceptionDetailRcCamera;

    private TagAdapter mTagAdapter;
    private TagAdapter mExceptionTagAdapter;
    private InspectionExceptionThumbnailAdapter mPhotoAdapter;
    private InspectionExceptionThumbnailAdapter mCameraAdapter;
    private ProgressUtils mProgressUtils;
    private GridLayoutManager mPhotoGridLayoutManager;
    private GridLayoutManager mVideoGridLayoutManager;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_inspection_exception_detail);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
    }

    private void initView() {
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());
        includeTextTitleTvTitle.setText(R.string.monitoring_point_exception_details);
        includeTextTitleTvSubtitle.setVisibility(View.GONE);
        mPhotoGridLayoutManager = new GridLayoutManager(mActivity, 3) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mVideoGridLayoutManager = new GridLayoutManager(mActivity, 3) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        initRcTag();

        initRcExceptionTag();

        initRcPhoto();

        initRcCamera();
    }

    private void initRcCamera() {
        mCameraAdapter = new InspectionExceptionThumbnailAdapter(mActivity);
        mCameraAdapter.setPhotoType(true);
        acInspectionExceptionDetailRcCamera.setLayoutManager(mVideoGridLayoutManager);
        acInspectionExceptionDetailRcCamera.setAdapter(mCameraAdapter);
        acInspectionExceptionDetailRcCamera.setHasFixedSize(true);
        acInspectionExceptionDetailRcCamera.setNestedScrollingEnabled(false);
        mCameraAdapter.setOnExceptionThumbnailItemClickListener(new InspectionExceptionThumbnailAdapter.ExceptionThumbnailItemClickListener() {
            @Override
            public void onExceptionThumbnailItemClickListener(int position) {
                mPresenter.doPreviewCamera(mCameraAdapter.getItem(position));
            }
        });
    }

    private void initRcPhoto() {
        mPhotoAdapter = new InspectionExceptionThumbnailAdapter(mActivity);
        acInspectionExceptionDetailRcPhoto.setLayoutManager(mPhotoGridLayoutManager);
        acInspectionExceptionDetailRcPhoto.setAdapter(mPhotoAdapter);
        acInspectionExceptionDetailRcPhoto.setHasFixedSize(true);
        acInspectionExceptionDetailRcPhoto.setNestedScrollingEnabled(false);
        mPhotoAdapter.setOnExceptionThumbnailItemClickListener(new InspectionExceptionThumbnailAdapter.ExceptionThumbnailItemClickListener() {
            @Override
            public void onExceptionThumbnailItemClickListener(int position) {
                mPresenter.doPreviewPhoto(mPhotoAdapter.getDataList(), position);
            }
        });

    }

    private void initRcExceptionTag() {
        mExceptionTagAdapter = new TagAdapter(mActivity, R.color.c_ff8d34, R.color.c_ff8d34);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        acInspectionExceptionDetailRcExceptionTag.setLayoutManager(manager);
        acInspectionExceptionDetailRcExceptionTag.setAdapter(mExceptionTagAdapter);
    }

    private void initRcTag() {
        mTagAdapter = new TagAdapter(mActivity, R.color.c_252525, R.color.c_dfdfdf);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        acInspectionExceptionDetailRcTag.setLayoutManager(manager);
        acInspectionExceptionDetailRcTag.setAdapter(mTagAdapter);
    }

    @Override
    protected InspectionExceptionDetailActivityPresenter createPresenter() {
        return new InspectionExceptionDetailActivityPresenter();
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
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_LONG).show();
    }


    @OnClick({R2.id.include_text_title_imv_arrows_left})
    public void onViewClicked(View view) {
        int viewID = view.getId();
        if (viewID == R.id.include_text_title_imv_arrows_left) {
            finishAc();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressUtils != null) {
            mProgressUtils.destroyProgress();
        }
        mProgressUtils = null;
    }

    @Override
    public void updateTagsData(List<String> list) {
        mTagAdapter.updateTags(list);
    }

    @Override
    public void updateExceptionTagsData(List<String> list) {
        mExceptionTagAdapter.updateTags(list);
    }

    @Override
    public void setTvName(String name) {
        acInspectionExceptionDetailTvName.setText(name);
    }

    @Override
    public void setTvSn(String sn) {
        acInspectionExceptionDetailTvSn.setText(sn);
    }

    @Override
    public void setTvStatus(int colorRes, String text) {
        WidgetUtil.changeTvState(mActivity, acInspectionExceptionDetailTvState, colorRes, text);
    }

    @Override
    public void setTvRemark(String remark) {
        acInspectionExceptionDetailTvRemark.setText(remark);
    }

    @Override
    public void updateRcPhotoAdapter(List<ScenesData> imageUrls) {
        int size = imageUrls.size();
        if (size != 0 && size < 3) {
            mPhotoGridLayoutManager.setSpanCount(size);
        }
        mPhotoAdapter.updateDataList(imageUrls);
    }

    @Override
    public void updateRcCameraAdapter(List<ScenesData> videoThumbUrls) {
        int size = videoThumbUrls.size();
        if (size != 0 && size < 3) {
            mVideoGridLayoutManager.setSpanCount(size);
        }
        mCameraAdapter.updateDataList(videoThumbUrls);
    }
}
