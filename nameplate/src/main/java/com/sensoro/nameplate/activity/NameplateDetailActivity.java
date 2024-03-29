package com.sensoro.nameplate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sensoro.common.adapter.TagAdapter;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.constant.ARouterConstants;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.manger.SensoroLinearLayoutManager;
import com.sensoro.common.server.bean.NamePlateInfo;
import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SelectDialog;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.common.widgets.SpacesItemDecoration;
import com.sensoro.common.widgets.TouchRecycleView;
import com.sensoro.common.widgets.dialog.TipDialogUtils;
import com.sensoro.nameplate.IMainViews.INameplateDetailActivityView;
import com.sensoro.nameplate.R;
import com.sensoro.nameplate.R2;
import com.sensoro.nameplate.adapter.AddedSensorAdapter;
import com.sensoro.nameplate.presenter.NameplateDetailActivityPresenter;
import com.sensoro.nameplate.widget.CustomDrawableDivider;
import com.sensoro.nameplate.widget.QrCodeDialogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sensoro.common.constant.Constants.DIRECTION_DOWN;

@Route(path = ARouterConstants.ACTIVITY_NAMEPLATE_DETAIL)
public class NameplateDetailActivity extends BaseActivity<INameplateDetailActivityView, NameplateDetailActivityPresenter>
        implements INameplateDetailActivityView {
    @BindView(R2.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R2.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R2.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R2.id.include_text_title_divider)
    View includeTextTitleDivider;
    @BindView(R2.id.include_text_title_cl_root)
    ConstraintLayout includeTextTitleClRoot;
    @BindView(R2.id.tv_nameplate_detail_sn)
    TextView tvNameplateDetailSn;
    @BindView(R2.id.tv_nameplate_qrcode)
    TextView tvNameplateQrcode;
    @BindView(R2.id.tv_nameplate_edit)
    TextView tvNameplateEdit;
    @BindView(R2.id.tv_nameplate_name)
    TextView tvNameplateName;
    @BindView(R2.id.trv_nameplate_tag)
    TouchRecycleView trvNameplateTag;
    @BindView(R2.id.tv_nameplate_associated_sensor)
    TextView tvNameplateAssociatedSensor;
    @BindView(R2.id.rv_list_include)
    RecyclerView rvNameplateAssociatedSensor;

    @BindView(R2.id.refreshLayout_include)
    SmartRefreshLayout refreshLayout;
    @BindView(R2.id.return_top_include)
    ImageView returnTopInclude;
    @BindView(R2.id.view_divider_ac_nameplate_associated_sensor)
    View viewDividerNameplateAssociatedNewSensor;

    private AddedSensorAdapter mAddedSensorAdapter;
    private TagAdapter tagAdapter;
    private final List<String> options = new ArrayList<>();
    private TipDialogUtils mDeleteDialog;
    private QrCodeDialogUtils dialogUtils;
    private ProgressUtils mProgressUtils;
    private Animation returnTopAnimation;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_nameplate_detail);
        ButterKnife.bind(this);
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());

        includeTextTitleTvTitle.setText(R.string.nameplate_manager_detail);
        includeTextTitleTvSubtitle.setVisibility(View.GONE);
        dialogUtils = new QrCodeDialogUtils(mActivity);
        options.add(getResources().getString(R.string.sweep_code_associated));
        options.add(getResources().getString(R.string.sensor_list));

        returnTopAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.return_top_in_anim);
        returnTopInclude.setAnimation(returnTopAnimation);
        returnTopInclude.setVisibility(GONE);

        initNormalDialog();
        initTag();
        initRvAddedSensorList();
        mPresenter.initData(mActivity);

        mPresenter.requestData(DIRECTION_DOWN);

        refreshLayout.setEnableAutoLoadMore(false);//开启自动加载功能（非必须）
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                mPresenter.requestData(DIRECTION_DOWN);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                mPresenter.requestData(Constants.DIRECTION_UP);
            }
        });
    }

    private void initNormalDialog() {
        mDeleteDialog = new TipDialogUtils(mActivity);
        mDeleteDialog.setTipMessageText(mActivity.getString(R.string.delete_associate_sensor));
        mDeleteDialog.setTipCacnleText(mActivity.getString(R.string.cancel), mActivity.getResources().getColor(R.color.c_252525));
        mDeleteDialog.setTipConfirmText(mActivity.getString(R.string.delete), mActivity.getResources().getColor(R.color.c_f35a58));
    }

    private void initTag() {
        tagAdapter = new TagAdapter(mActivity, R.color.c_252525, R.color.c_dfdfdf);
        int spacingInPixels = mActivity.getResources().getDimensionPixelSize(R.dimen.x10);
        trvNameplateTag.addItemDecoration(new SpacesItemDecoration(false, spacingInPixels));
        trvNameplateTag.setIntercept(true);
        SensoroLinearLayoutManager layoutManager = new SensoroLinearLayoutManager(mActivity, false);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        trvNameplateTag.setLayoutManager(layoutManager);
//        int spacingInPixels = mContext.getResources().getDimensionPixelSize(R.dimen.x10);
//        holder.rvItemAdapterNameplateTag.addItemDecoration(new SpacesItemDecoration(false, spacingInPixels));

    }

    private void initRvAddedSensorList() {
        mAddedSensorAdapter = new AddedSensorAdapter(mActivity);
        LinearLayoutManager manager = new LinearLayoutManager(mActivity, RecyclerView.VERTICAL, false);
        CustomDrawableDivider bottomNoDividerItemDecoration =
                new CustomDrawableDivider(mActivity, CustomDrawableDivider.VERTICAL);
        rvNameplateAssociatedSensor.addItemDecoration(bottomNoDividerItemDecoration);
        rvNameplateAssociatedSensor.setLayoutManager(manager);
        rvNameplateAssociatedSensor.setAdapter(mAddedSensorAdapter);

        rvNameplateAssociatedSensor.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (manager.findFirstVisibleItemPosition() > 4) {
                    if (newState == 0) {
                        returnTopInclude.setVisibility(VISIBLE);
                        if (returnTopAnimation != null && returnTopAnimation.hasEnded()) {
                            returnTopInclude.startAnimation(returnTopAnimation);
                        }
                    } else {
                        returnTopInclude.setVisibility(GONE);
                    }
                } else {
                    returnTopInclude.setVisibility(GONE);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (manager.findFirstVisibleItemPosition() == 0 && rvNameplateAssociatedSensor.getChildAt(0).getTop() == 0) {
                    viewDividerNameplateAssociatedNewSensor.setVisibility(GONE);
                } else {
                    viewDividerNameplateAssociatedNewSensor.setVisibility(VISIBLE);
                }
            }
        });

        mAddedSensorAdapter.setOnDeleteClickListener(new AddedSensorAdapter.onDeleteClickListenre() {
            @Override
            public void onDeleteClick(int position) {
//                toastShort("点击了");
                if (mDeleteDialog != null) {
                    mDeleteDialog.show();
                    mDeleteDialog.setTipDialogUtilsClickListener(new TipDialogUtils.TipDialogUtilsClickListener() {
                        @Override
                        public void onCancelClick() {
                            mDeleteDialog.dismiss();
                        }

                        @Override
                        public void onConfirmClick() {
                            mDeleteDialog.dismiss();
                            mPresenter.unbindNameplateDevice(position);


                        }
                    });


                }
            }
        });

    }

    @Override
    protected NameplateDetailActivityPresenter createPresenter() {
        return new NameplateDetailActivityPresenter();
    }


    @OnClick({R2.id.return_top_include, R2.id.include_text_title_imv_arrows_left, R2.id.tv_nameplate_qrcode, R2.id.tv_nameplate_edit, R2.id.ll_association_sensor_ac_deploy_nameplate})
    public void onViewClicked(View view) {
        int id = view.getId();
        if (R.id.include_text_title_imv_arrows_left == id) {
            finish();
        } else if (R.id.tv_nameplate_qrcode == id) {

            dialogUtils.show();
        } else if (R.id.tv_nameplate_edit == id) {
            mPresenter.doEditNameplate();
        } else if (R.id.ll_association_sensor_ac_deploy_nameplate == id) {


            AppUtils.showDialog(mActivity, new SelectDialog.SelectDialogListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mPresenter.doNesSensor(position);
                }
            }, options, getResources().getString(R.string.association_sensort)).show();
        } else if (id == R.id.return_top_include) {
            rvNameplateAssociatedSensor.smoothScrollToPosition(0);
            returnTopInclude.setVisibility(GONE);
            refreshLayout.closeHeaderOrFooter();
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
    protected void onDestroy() {
        super.onDestroy();
        if (mDeleteDialog != null) {
            mDeleteDialog.destory();
        }
    }


    @Override
    public void showProgressDialog() {
        if (mProgressUtils != null) {
            mProgressUtils.showProgress();
        }
    }

    @Override
    public void dismissProgressDialog() {
        if (mProgressUtils != null) {
            mProgressUtils.dismissProgress();
        }
    }

    @Override
    public void toastShort(String msg) {
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void toastLong(String msg) {

    }


    @Override
    public void updateBindDeviceAdapter(List<NamePlateInfo> data) {

        if (data != null && data.size() > 0) {
            tvNameplateAssociatedSensor.setText(getResources().getString(R.string.association_sensor) + data.size());

            mAddedSensorAdapter.updateData(data);
        } else {
            tvNameplateAssociatedSensor.setText(getResources().getString(R.string.association_sensor) + "0");

        }
    }

    @Override
    public void onPullRefreshComplete() {
        refreshLayout.finishLoadMore();
        refreshLayout.finishRefresh();
    }


    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void updateTopDetail(NamePlateInfo namePlateInfo) {


        if (null != namePlateInfo) {


            if (null != namePlateInfo.getTags() && namePlateInfo.getTags().size() > 0) {
                trvNameplateTag.setAdapter(tagAdapter);
                tagAdapter.updateTags(namePlateInfo.getTags());
            } else {

                tagAdapter.getTags().clear();
                tagAdapter.notifyDataSetChanged();

            }

            if (!TextUtils.isEmpty(namePlateInfo.get_id())) {
                tvNameplateDetailSn.setText(namePlateInfo.get_id());
            }
            if (!TextUtils.isEmpty(namePlateInfo.getName())) {
                tvNameplateName.setText(namePlateInfo.getName());
            }
        }

    }

    @Override
    public void updateNamePlateStatus(int pos) {

        mAddedSensorAdapter.getmList().remove(pos);

        mAddedSensorAdapter.notifyDataSetChanged();
//        mAddedSensorAdapter.notifyItemRangeChanged(0, mAddedSensorAdapter.getmList().size());

        tvNameplateAssociatedSensor.setText(getResources().getString(R.string.association_sensor) + mAddedSensorAdapter.getmList().size());

    }

    @Override
    public void setQrCodeUrl(String qrCodeUrl) {
        dialogUtils.setImageUrl(qrCodeUrl);
    }
}
