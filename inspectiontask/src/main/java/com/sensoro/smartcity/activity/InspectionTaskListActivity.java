package com.sensoro.smartcity.activity;
/**
 * @Author: jack
 * 时  间: 2019-09-09
 * 包  名: com.sensoro.smartcity.activity
 * 类  名: InspectionTaskListActivity
 * 简  述: <巡检任务首页,未完成，已完成列表，上拉刷新，下拉加载更多，日历筛选>
 */
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.callback.RecycleViewItemClickListener;
import com.sensoro.common.constant.ARouterConstants;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.server.bean.InspectionIndexTaskInfo;
import com.sensoro.common.utils.DateUtil;
import com.sensoro.common.widgets.CustomDivider;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.inspectiontask.R;
import com.sensoro.inspectiontask.R2;
import com.sensoro.smartcity.adapter.InspectionTaskAdapter;
import com.sensoro.smartcity.imainviews.IInspectionTaskListActivityView;
import com.sensoro.common.model.CalendarDateModel;
import com.sensoro.smartcity.presenter.InspectionTaskListActivityPresenter;
import com.sensoro.common.widgets.CalendarPopUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


@Route(path = ARouterConstants.ACTIVITY_INSPECTIONTASK_List)
public class InspectionTaskListActivity extends BaseActivity<IInspectionTaskListActivityView, InspectionTaskListActivityPresenter>
        implements IInspectionTaskListActivityView, CalendarPopUtils.OnCalendarPopupCallbackListener, Constants {
    @BindView(R2.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R2.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R2.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R2.id.include_text_title_cl_root)
    ConstraintLayout includeTextTitleClRoot;
    @BindView(R2.id.ac_inspection_task_list_rb_current)
    RadioButton acInspectionTaskListRbCurrent;
    @BindView(R2.id.ac_inspection_task_list_rb_history)
    RadioButton acInspectionTaskListRbHistory;
    @BindView(R2.id.ac_inspection_task_list_rg)
    RadioGroup acInspectionTaskListRg;
    @BindView(R2.id.ac_inspection_task_list_imv_calendar)
    ImageView acInspectionTaskListImvCalendar;
    @BindView(R2.id.ac_inspection_task_list_rc_content)
    RecyclerView acInspectionTaskListRcContent;
    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R2.id.ac_inspection_task_list_tv_date_edit)
    TextView acInspectionTaskListTvDateEdit;
    @BindView(R2.id.ac_inspection_task_list_imv_date_close)
    ImageView acInspectionTaskListImvDateClose;
    @BindView(R2.id.ac_inspection_task_list_rl_date_edit)
    RelativeLayout acInspectionTaskListRlDateEdit;
    View icNoContent;
    private InspectionTaskAdapter mTaskAdapter;
    private CalendarPopUtils mCalendarPopUtils;

    private ProgressUtils mProgressUtils;
    private boolean isShowProgressDialog = true;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_inspection_task_list);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
    }


    private void initView() {
        icNoContent = LayoutInflater.from(this).inflate(R.layout.no_content, null);

        includeTextTitleTvTitle.setText(R.string.inspection_task);
        includeTextTitleTvSubtitle.setVisibility(View.GONE);
        acInspectionTaskListImvCalendar.setVisibility(View.GONE);

        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());
        initRcContent();
        initCalendarPop();
    }

    private void initCalendarPop() {
        mCalendarPopUtils = new CalendarPopUtils(mActivity);
        mCalendarPopUtils
                .setMonthStatus(1)
                .setOnCalendarPopupCallbackListener(this);
    }

    private void initRcContent() {
        mTaskAdapter = new InspectionTaskAdapter(mActivity);
        final LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        acInspectionTaskListRcContent.setLayoutManager(manager);
        acInspectionTaskListRcContent.addItemDecoration(new CustomDivider(mActivity, DividerItemDecoration.VERTICAL));
        acInspectionTaskListRcContent.setAdapter(mTaskAdapter);

        mTaskAdapter.setOnRecycleViewItemClickListener(new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPresenter.doItemClick(mTaskAdapter.getItem(position));

            }
        });


        refreshLayout.setEnableAutoLoadMore(true);//开启自动加载功能（非必须）
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                isShowProgressDialog = false;
                mPresenter.refreshData(Constants.DIRECTION_DOWN);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                isShowProgressDialog = false;
                mPresenter.refreshData(Constants.DIRECTION_UP);
            }
        });


    }

    @Override
    protected InspectionTaskListActivityPresenter createPresenter() {
        return new InspectionTaskListActivityPresenter();
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
        if (isShowProgressDialog) {
            mProgressUtils.showProgress();
        }
        isShowProgressDialog = true;
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


    @OnClick({R2.id.include_text_title_imv_arrows_left, R2.id.ac_inspection_task_list_rb_current,
            R2.id.ac_inspection_task_list_rb_history, R2.id.ac_inspection_task_list_imv_calendar,
            R2.id.ac_inspection_task_list_imv_date_close})
    public void onViewClicked(View view) {

        int viewID = view.getId();
        if (viewID == R.id.include_text_title_imv_arrows_left) {
            finishAc();
        } else if (viewID == R.id.ac_inspection_task_list_rb_current) {
            refreshLayout.setNoMoreData(false);
            acInspectionTaskListImvCalendar.setVisibility(View.GONE);
            if (getRlDateEditIsVisible()) {
                acInspectionTaskListRlDateEdit.setVisibility(View.GONE);
            }
            mPresenter.doUndone();
        } else if (viewID == R.id.ac_inspection_task_list_rb_history) {
            refreshLayout.setNoMoreData(false);
            if (getRlDateEditIsVisible()) {
                acInspectionTaskListRlDateEdit.setVisibility(View.GONE);
            }
            acInspectionTaskListImvCalendar.setVisibility(View.VISIBLE);
            mPresenter.doDone();
        } else if (viewID == R.id.ac_inspection_task_list_imv_calendar) {
            doCalendar();
        } else if (viewID == R.id.ac_inspection_task_list_imv_date_close) {
            acInspectionTaskListRlDateEdit.setVisibility(View.GONE);
            mPresenter.doDone();
        }

    }

    private void doCalendar() {
        long temp_startTime = -1;
        long temp_endTime = -1;
        if (getRlDateEditIsVisible()) {
            temp_startTime = mPresenter.getStartTime();
            temp_endTime = mPresenter.getEndTime();
        }

        mCalendarPopUtils.show(includeTextTitleClRoot, temp_startTime, temp_endTime);
    }

    @Override
    public void onCalendarPopupCallback(CalendarDateModel calendarDateModel) {
        setRlDateEditVisible(true);
        mPresenter.setStartTime(DateUtil.strToDate(calendarDateModel.startDate).getTime());
        mPresenter.setEndTime(DateUtil.strToDate(calendarDateModel.endDate).getTime());
        setSelectedDateSearchText(DateUtil.getCalendarYearMothDayFormatDate(mPresenter.getStartTime()) + " ~ " + DateUtil
                .getCalendarYearMothDayFormatDate(mPresenter.getEndTime()));
        mPresenter.updateEndTime();
        mPresenter.requestDataByDate(mPresenter.getStartTime(), mPresenter.getEndTime());
    }


    @Override
    public void setRlDateEditVisible(boolean isVisible) {
        acInspectionTaskListRlDateEdit.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean getRlDateEditIsVisible() {
        return acInspectionTaskListRlDateEdit.getVisibility() == View.VISIBLE;
    }

    @Override
    public void setSelectedDateSearchText(String time) {
        acInspectionTaskListTvDateEdit.setText(time);
    }

    @Override
    public void updateRcContent(List<InspectionIndexTaskInfo> tasks) {
        if (tasks != null && tasks.size() > 0) {
            mTaskAdapter.updateTaskList(tasks);
        }
        setNoContentVisible(tasks == null || tasks.size() < 1);

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setNoContentVisible(boolean isVisible) {
        RefreshHeader refreshHeader = refreshLayout.getRefreshHeader();
        if (refreshHeader != null) {
            if (isVisible) {
                refreshHeader.setPrimaryColors(getResources().getColor(R.color.c_f4f4f4));
            } else {
                refreshHeader.setPrimaryColors(getResources().getColor(R.color.white));
            }
        }
        if (isVisible) {
            refreshLayout.setRefreshContent(icNoContent);
        } else {
            refreshLayout.setRefreshContent(acInspectionTaskListRcContent);
        }


    }

    @Override
    public void onPullRefreshCompleted() {
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
    }

    @Override
    public void rcSmoothScrollToTop() {
        acInspectionTaskListRcContent.smoothScrollToPosition(0);
    }

    @Override
    public void closeRefreshHeaderOrFooter() {
        refreshLayout.closeHeaderOrFooter();
    }
}
