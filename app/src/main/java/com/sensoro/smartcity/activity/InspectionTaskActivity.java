package com.sensoro.smartcity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.InspectionTaskRcContentAdapter;
import com.sensoro.smartcity.base.BaseActivity;
import com.sensoro.smartcity.imainviews.IInspectionTaskActivityView;
import com.sensoro.smartcity.presenter.InspectionTaskActivityPresenter;
import com.sensoro.smartcity.widget.RecycleViewItemClickListener;
import com.sensoro.smartcity.widget.SensoroToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InspectionTaskActivity extends BaseActivity<IInspectionTaskActivityView, InspectionTaskActivityPresenter>
        implements IInspectionTaskActivityView {
    @BindView(R.id.ac_inspection_task_imv_arrows_left)
    ImageView acInspectionTaskImvArrowsLeft;
    @BindView(R.id.ac_inspection_task_ll_search)
    LinearLayout acInspectionTaskLlSearch;
    @BindView(R.id.ac_inspection_task_fl_state)
    FrameLayout acInspectionTaskFlState;
    @BindView(R.id.ac_inspection_task_fl_type)
    FrameLayout acInspectionTaskFlType;
    @BindView(R.id.ac_inspection_task_rc_content)
    RecyclerView acInspectionTaskRcContent;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.ac_inspection_task_imv_scan)
    ImageView acInspectionTaskImvScan;
    @BindView(R.id.ac_inspection_task_imv_map)
    ImageView acInspectionTaskImvMode;
    @BindView(R.id.ac_inspection_task_tv_inspection_count)
    TextView acInspectionTaskTvInspectionCount;
    @BindView(R.id.ac_inspection_task_tv_not_inspection_count)
    TextView acInspectionTaskTvNotInspectionCount;
    private InspectionTaskRcContentAdapter mContentAdapter;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_inspection_task);
        ButterKnife.bind(this);
        initView();

        mPresenter.initData(mActivity);
    }

    private void initView() {
//地图模式切换，图片 map_list_mode map_mode,已经准备好了
        initRcContent();
    }

    private void initRcContent() {
        mContentAdapter = new InspectionTaskRcContentAdapter(mActivity);
        final LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        acInspectionTaskRcContent.setLayoutManager(manager);
        acInspectionTaskRcContent.addItemDecoration(new DividerItemDecoration(mActivity,DividerItemDecoration.VERTICAL));
        acInspectionTaskRcContent.setAdapter(mContentAdapter);

        mContentAdapter.setOnRecycleViewItemClickListener(new InspectionTaskRcContentAdapter.InspectionTaskRcItemClickListener() {
            @Override
            public void onInspectionTaskInspectionClick(int position, int state) {
                mPresenter.doItemClick(state);
            }

            @Override
            public void onInspectionTaskNavigationClick(int position) {

            }

        });

        acInspectionTaskRcContent.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                if (xLinearLayoutManager.findFirstVisibleItemPosition() == 0 && newState == SCROLL_STATE_IDLE &&
//                        toolbarDirection == DIRECTION_DOWN) {
////                    mListRecyclerView.setre
//                }
                if (manager.findFirstVisibleItemPosition() > 4) {
                    if (newState == 0) {
//                        mReturnTopImageView.setVisibility(VISIBLE);
//                        if (returnTopAnimation.hasEnded()) {
//                            mReturnTopImageView.startAnimation(returnTopAnimation);
//                        }
                    } else {
//                        mReturnTopImageView.setVisibility(View.GONE);
                    }
                } else {
//                    mReturnTopImageView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });

        refreshLayout.setEnableAutoLoadMore(true);//开启自动加载功能（非必须）
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {

            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {

            }
        });
    }

    @Override
    protected InspectionTaskActivityPresenter createPresenter() {
        return new InspectionTaskActivityPresenter();
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
        SensoroToast.INSTANCE.makeText(msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {
        SensoroToast.INSTANCE.makeText(msg, Toast.LENGTH_LONG).show();
    }


    @OnClick({R.id.ac_inspection_task_imv_arrows_left, R.id.ac_inspection_task_ll_search,
            R.id.ac_inspection_task_fl_state, R.id.ac_inspection_task_fl_type,R.id.ac_inspection_task_imv_scan
    ,R.id.ac_inspection_task_imv_map})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ac_inspection_task_imv_arrows_left:
                finishAc();
                break;
            case R.id.ac_inspection_task_ll_search:
                break;
            case R.id.ac_inspection_task_fl_state:
                break;
            case R.id.ac_inspection_task_fl_type:
                break;
            case R.id.ac_inspection_task_imv_scan:
                toastShort("扫描");
                break;
            case R.id.ac_inspection_task_imv_map:

                break;
        }
    }
}