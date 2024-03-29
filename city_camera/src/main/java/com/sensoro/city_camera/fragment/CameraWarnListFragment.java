package com.sensoro.city_camera.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sensoro.city_camera.IMainViews.ICameraWarnListFragmentView;
import com.sensoro.city_camera.R;
import com.sensoro.city_camera.R2;
import com.sensoro.city_camera.adapter.CameraWarnFragRcContentAdapter;
import com.sensoro.city_camera.dialog.SecurityWarnConfirmDialog;
import com.sensoro.city_camera.model.FilterModel;
import com.sensoro.city_camera.presenter.CameraWarnListFragmentPresenter;
import com.sensoro.city_camera.widget.FilterPopUtils;
import com.sensoro.common.adapter.SearchHistoryAdapter;
import com.sensoro.common.base.BaseFragment;
import com.sensoro.common.callback.RecycleViewItemClickListener;
import com.sensoro.common.constant.ARouterConstants;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.manger.SensoroLinearLayoutManager;
import com.sensoro.common.server.security.bean.SecurityAlarmInfo;
import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.utils.LogUtils;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.common.widgets.SpacesItemDecoration;
import com.sensoro.common.widgets.TipOperationDialogUtils;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * @author wangqinghao
 */
@Route(path = ARouterConstants.FRAGMENT_CAMERA_WARN_LIST)
public class CameraWarnListFragment extends BaseFragment<ICameraWarnListFragmentView, CameraWarnListFragmentPresenter>
        implements ICameraWarnListFragmentView, CameraWarnFragRcContentAdapter.CameraWarnConfirmStatusClickListener, TipOperationDialogUtils.TipDialogUtilsClickListener {

    @BindView(R2.id.fg_camera_warns_top_search_title_root)
    RelativeLayout fgMainWarnTitleRoot;
    @BindView(R2.id.fg_camera_warns_top_search_et_search)
    EditText edFilterContent;
    @BindView(R2.id.fg_camera_warns_top_search_imv_clear)
    ImageView ivFilterContentClear;
    @BindView(R2.id.tv_top_search_alarm_search_cancel)
    TextView tvFilterCancel;
    @BindView(R2.id.fg_camera_warns_top_filter_rl)
    RelativeLayout layoutFilterContent;
    @BindView(R2.id.layout_filter_capture_time)
    RelativeLayout layoutCaptureTime;
    @BindView(R2.id.tv_search_camera_warns_time)
    TextView tvFilterCaptureTime;
    @BindView(R2.id.iv_search_camera_warns_time)
    ImageView ivFilterCaptureTime;

    @BindView(R2.id.layout_filter_process_status)
    RelativeLayout layoutProcessStatus;
    @BindView(R2.id.tv_search_camera_warns_status)
    TextView tvFilterProcessStatus;
    @BindView(R2.id.iv_search_camera_warns_status)
    ImageView ivFilterProcessStatus;

    @BindView(R2.id.fg_camera_warns_rc_content)
    RecyclerView rvCameraWarnsContent;
    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R2.id.ll_search_history)
    LinearLayout llSearchHistory;
    @BindView(R2.id.btn_search_clear)
    ImageView btnSearchClear;
    @BindView(R2.id.rv_search_history)
    RecyclerView rvSearchHistory;
    @BindView(R2.id.alarm_return_top)
    ImageView mReturnTopImageView;
    View icNoContent;

    private CameraWarnFragRcContentAdapter mRcContentAdapter;
    private boolean isShowDialog = true;
    private ProgressUtils mProgressUtils;
    private Animation returnTopAnimation;
    private SearchHistoryAdapter mSearchHistoryAdapter;
    //删除历史记录确认
    private TipOperationDialogUtils mHistoryClearDialog;
    //抓拍时间 处理状态 筛选PopuWindow
    private FilterPopUtils mCaptureTimeFilterPopUtils;
    private FilterPopUtils mProcessStatusFilterPopUtils;

    public static final int WARN_FILTER_TIME = 0;
    public static final int WARN_FILTER_STATUS = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARouter.getInstance().inject(this);
    }

    @Override
    protected void initData(Context activity) {
        initView();
        mPresenter.initData(activity);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        icNoContent = LayoutInflater.from(getActivity()).inflate(R.layout.no_content, null);

        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mRootFragment.getActivity()).build());
        mCaptureTimeFilterPopUtils = new FilterPopUtils(getActivity());
        mProcessStatusFilterPopUtils = new FilterPopUtils(getActivity());

        //返回顶部
        returnTopAnimation = AnimationUtils.loadAnimation(mRootFragment.getContext(), R.anim.return_top_in_anim);
        mReturnTopImageView.setAnimation(returnTopAnimation);
        mReturnTopImageView.setVisibility(View.GONE);
        //搜索数据========
        edFilterContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // 当按了搜索之后关闭软键盘
                    String text = edFilterContent.getText().toString().trim();
                    if (!TextUtils.isEmpty(text)) {
                        mPresenter.setFilterText(text);
                        edFilterContent.clearFocus();
                        AppUtils.dismissInputMethodManager(mRootFragment.getActivity(), edFilterContent);
                        setSearchHistoryVisible(false);
                    }
                    return true;
                }
                return false;
            }
        });
        edFilterContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setSearchClearImvVisible(s.length() > 0);
            }
        });
        edFilterContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edFilterContent.requestFocus();
                edFilterContent.setCursorVisible(true);
                setSearchHistoryVisible(true);
                return false;
            }
        });
        AppUtils.getInputSoftStatus(mRootView, new AppUtils.InputSoftStatusListener() {
            @Override
            public void onKeyBoardClose() {
                edFilterContent.setCursorVisible(false);
            }

            @Override
            public void onKeyBoardOpen() {
                edFilterContent.setCursorVisible(true);
            }
        });
        //抓拍时间筛选-选择回调处理
        mCaptureTimeFilterPopUtils.setSelectDeviceTypeItemClickListener(new FilterPopUtils.SelectFilterTypeItemClickListener() {
            @Override
            public void onSelectFilterTypeItemClick(View view, int position) {
                //隐藏搜索历史弹窗 显示取消按钮
                if (llSearchHistory.getVisibility() == View.VISIBLE) {
                    llSearchHistory.setVisibility(View.GONE);
                    refreshLayout.setVisibility(View.VISIBLE);
                    tvFilterCancel.setVisibility(View.VISIBLE);
                }
                //自定义时间
                if (position == 4) {
                    mPresenter.doCalendar(fgMainWarnTitleRoot);
                } else {
                    mPresenter.setFilterCapturetime(position);
                }
                setWarnFilterContent(WARN_FILTER_TIME);
            }

            @Override
            public void onBottomClickDismissPop() {
                layoutCaptureTime.performClick();
            }

            @Override
            public void onOutsideDismissPop() {
                //隐藏 时间选择弹窗
                ivFilterCaptureTime.setImageResource(R.drawable.ic_arrow_down);
                mPresenter.setFilterCapturetime(-1);

            }
        });
        //处理状态筛选-选择回调处理
        mProcessStatusFilterPopUtils.setSelectDeviceTypeItemClickListener(new FilterPopUtils.SelectFilterTypeItemClickListener() {
            @Override
            public void onSelectFilterTypeItemClick(View view, int position) {
                //隐藏搜索历史弹窗 显示取消按钮
                if (llSearchHistory.getVisibility() == View.VISIBLE) {
                    llSearchHistory.setVisibility(View.GONE);
                    refreshLayout.setVisibility(View.VISIBLE);
                    tvFilterCancel.setVisibility(View.VISIBLE);
                }
                //处理状态类型
                mPresenter.setFilterProcessStatus(position);
                setWarnFilterContent(WARN_FILTER_STATUS);
            }

            @Override
            public void onBottomClickDismissPop() {
                layoutProcessStatus.performClick();
            }

            @Override
            public void onOutsideDismissPop() {
                //隐藏 状态选择弹窗
                ivFilterProcessStatus.setImageResource(R.drawable.ic_arrow_down);
                mPresenter.setFilterProcessStatus(-1);

            }
        });


        initRcContent();
        initRcSearchHistory();
        initClearHistoryDialog();
    }


    private void initRcContent() {
        mRcContentAdapter = new CameraWarnFragRcContentAdapter(mRootFragment.getActivity());
        mRcContentAdapter.setAlarmConfirmStatusClickListener(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mRootFragment.getActivity());
        rvCameraWarnsContent.setLayoutManager(linearLayoutManager);
        rvCameraWarnsContent.setAdapter(mRcContentAdapter);
        //
        //新控件
        refreshLayout.setEnableAutoLoadMore(false);//开启自动加载功能（非必须）
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                isShowDialog = false;
                String text = edFilterContent.getText().toString();
                mPresenter.requestSearchData(Constants.DIRECTION_DOWN);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                isShowDialog = false;
                String text = edFilterContent.getText().toString();
                mPresenter.requestSearchData(Constants.DIRECTION_UP);
            }
        });
        rvCameraWarnsContent.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (linearLayoutManager.findFirstVisibleItemPosition() > 4) {
                    if (newState == 0) {
                        mReturnTopImageView.setVisibility(View.VISIBLE);
                        if (returnTopAnimation != null && returnTopAnimation.hasEnded()) {
                            mReturnTopImageView.startAnimation(returnTopAnimation);
                        }
                    } else {
                        mReturnTopImageView.setVisibility(View.GONE);
                    }
                } else {
                    mReturnTopImageView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });
    }

    private void initRcSearchHistory() {
        SensoroLinearLayoutManager layoutManager = new SensoroLinearLayoutManager(mRootFragment.getActivity(), true) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rvSearchHistory.setLayoutManager(layoutManager);
        rvSearchHistory.addItemDecoration(new SpacesItemDecoration(false, AppUtils.dp2px(mRootFragment.getActivity(), 4)));
        mSearchHistoryAdapter = new SearchHistoryAdapter(mRootFragment.getActivity(), new
                RecycleViewItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String text = mSearchHistoryAdapter.getSearchHistoryList().get(position);
                        if (!TextUtils.isEmpty(text)) {
                            edFilterContent.setText(text);
                            edFilterContent.setSelection(edFilterContent.getText().toString().length());
                        }
                        ivFilterContentClear.setVisibility(View.VISIBLE);
                        mPresenter.setFilterText(text);//搜索关键字
                        edFilterContent.clearFocus();
                        AppUtils.dismissInputMethodManager(mRootFragment.getActivity(), edFilterContent);
                        setSearchHistoryVisible(false);
                        mPresenter.save(text);
                        mPresenter.requestSearchData(Constants.DIRECTION_DOWN);
                    }
                });
        rvSearchHistory.setAdapter(mSearchHistoryAdapter);
    }


    private void initClearHistoryDialog() {
        mHistoryClearDialog = new TipOperationDialogUtils(mRootFragment.getActivity(), true);
        mHistoryClearDialog.setTipTitleText(getString(R.string.history_clear_all));
        mHistoryClearDialog.setTipMessageText(getString(R.string.confirm_clear_history_record), R.color.c_a6a6a6);
        mHistoryClearDialog.setTipCancelText(getString(R.string.cancel), getResources().getColor(R.color.c_1dbb99));
        mHistoryClearDialog.setTipConfirmText(getString(R.string.clear), getResources().getColor(R.color.c_a6a6a6));
        mHistoryClearDialog.setTipDialogUtilsClickListener(this);
    }


    @Override
    protected int initRootViewId() {
        return R.layout.fragment_camera_warn_list;
    }

    @Override
    protected CameraWarnListFragmentPresenter createPresenter() {
        return new CameraWarnListFragmentPresenter();
    }


    @Override
    public void onFragmentStart() {

    }

    @Override
    public void onFragmentStop() {
        dismissInput();
    }

    /**
     * 取消搜索数据
     */
    @Override
    public void cancelSearchData() {
        //取消搜索
        if (getSearchTextCancelVisible()) {
            edFilterContent.getText().clear();
        }
        mPresenter.doCancelSearch();
        setSearchHistoryVisible(false);
        AppUtils.dismissInputMethodManager(mRootFragment.getActivity(), edFilterContent);
    }


    @Override
    public void updateCameraWarnsListAdapter(List<SecurityAlarmInfo> securityAlarmInfoList) {
        if (null != securityAlarmInfoList) {
            mRcContentAdapter.setData(securityAlarmInfoList);
            mRcContentAdapter.notifyDataSetChanged();
        }
        try {
            LogUtils.loge("updateAlarmListAdapter-->> 刷新 " + mRcContentAdapter.getData().size());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        setNoContentVisible(securityAlarmInfoList.size() < 1);

    }

    @Override
    public void SmoothToTopList() {
        rvCameraWarnsContent.smoothScrollToPosition(0);
        //refreshLayout.resetNoMoreData();
        mReturnTopImageView.setVisibility(View.GONE);

    }

    @Override
    public void onPullRefreshComplete() {
        refreshLayout.finishRefresh();
        refreshLayout.finishLoadMore();
    }

    @Override
    public void onPullRefreshCompleteNoMoreData() {
        refreshLayout.finishLoadMoreWithNoMoreData();
    }

    @Override
    public void setSearchButtonTextCancelVisible(boolean isVisible) {
        if (isVisible) {
            tvFilterCancel.setVisibility(View.VISIBLE);
        } else if (TextUtils.isEmpty(edFilterContent.getText().toString())) {
            tvFilterCancel.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean getSearchTextCancelVisible() {
        return tvFilterCancel.getVisibility() == View.VISIBLE;

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
            refreshLayout.setRefreshContent(rvCameraWarnsContent);
        }
    }

    @Override
    public void setSearchClearImvVisible(boolean isVisible) {
        ivFilterContentClear.setVisibility(isVisible ? View.VISIBLE : View.GONE);

    }

    @Override
    public void updateSearchHistoryList(List<String> data) {
        btnSearchClear.setVisibility(data.size() > 0 ? View.VISIBLE : View.GONE);
        mSearchHistoryAdapter.updateSearchHistoryAdapter(data);

    }

    @Override
    public void setSearchHistoryVisible(boolean isVisible) {
        llSearchHistory.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        refreshLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        setSearchButtonTextCancelVisible(isVisible);
    }

    @Override
    public void showHistoryClearDialog() {
        if (mHistoryClearDialog != null) {
            mHistoryClearDialog.show();
        }
    }


    @Override
    public void updateFilterProcessStatusList(List<FilterModel> processStatusList) {
        mProcessStatusFilterPopUtils.updateSelectDeviceStatusList(processStatusList);

    }

    @Override
    public void updateFilterCaptureTimeList(List<FilterModel> captureTimeList) {
        mCaptureTimeFilterPopUtils.updateSelectDeviceStatusList(captureTimeList);

    }

    @Override
    public void setFilterCaptureTimeView(FilterModel captureTimeModel) {
        if (captureTimeModel.isSpecialShow) {
            tvFilterCaptureTime.setTextColor(getResources().getColor(R.color.c_a6a6a6));
            tvFilterCaptureTime.setText(R.string.capture_time);
        } else {
            tvFilterCaptureTime.setTextColor(getResources().getColor(R.color.c_252525));
            tvFilterCaptureTime.setText(captureTimeModel.statusTitle);
        }

    }

    @Override
    public void setFilterProcessStatusView(FilterModel processStatusModel) {
        if (processStatusModel.isSpecialShow) {
            tvFilterProcessStatus.setTextColor(getResources().getColor(R.color.c_a6a6a6));
            tvFilterProcessStatus.setText(R.string.process_status);
        } else {
            tvFilterProcessStatus.setTextColor(getResources().getColor(R.color.c_252525));
            tvFilterProcessStatus.setText(processStatusModel.statusTitle);
        }

    }

    @Override
    public void showConfirmDialog(SecurityAlarmInfo securityAlarmInfo) {
        if (securityAlarmInfo != null) {
            SecurityWarnConfirmDialog securityWarnConfirmDialog = new SecurityWarnConfirmDialog();
            securityWarnConfirmDialog.setSecurityConfirmCallback(mPresenter);
            Bundle bundle = new Bundle();
            bundle.putString(SecurityWarnConfirmDialog.EXTRA_KEY_SECURITY_ID, securityAlarmInfo.getId());
            bundle.putString(SecurityWarnConfirmDialog.EXTRA_KEY_SECURITY_TITLE, securityAlarmInfo.getTaskName());
            bundle.putString(SecurityWarnConfirmDialog.EXTRA_KEY_SECURITY_TIME, String.valueOf(securityAlarmInfo.getAlarmTime()));
            bundle.putInt(SecurityWarnConfirmDialog.EXTRA_KEY_SECURITY_TYPE, securityAlarmInfo.getAlarmType());
            securityWarnConfirmDialog.setArguments(bundle);
            securityWarnConfirmDialog.show(getChildFragmentManager());
        }
    }

    @Override
    public void dismissInput() {
        AppUtils.dismissInputMethodManager(mRootFragment.getActivity(), edFilterContent, false);
    }


    @Override
    public void startAC(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void finishAc() {
        Objects.requireNonNull(mRootFragment.getActivity()).finish();
    }

    @Override
    public void startACForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void setIntentResult(int resultCode) {

    }

    @Override
    public void setIntentResult(int resultCode, Intent data) {

    }

    @Override
    public void showProgressDialog() {
        if (isShowDialog) {
            if (mProgressUtils != null) {
                mProgressUtils.showProgress();
            }
        }
        isShowDialog = true;

    }

    @Override
    public void dismissProgressDialog() {
        if (mProgressUtils != null) {
            mProgressUtils.dismissProgress();
        }
    }

    @Override
    public void toastShort(String msg) {
        SensoroToast.getInstance().makeText(mRootFragment.getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {
        SensoroToast.getInstance().makeText(mRootFragment.getActivity(), msg, Toast.LENGTH_LONG).show();

    }

    /*item 预警确认*/
    @Override
    public void onConfirmStatusClick(View view, int position) {
        try {
            SecurityAlarmInfo securityAlarmInfo = mRcContentAdapter.getData().get(position);
            mPresenter.clickItemByConfirmStatus(securityAlarmInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        try {
            SecurityAlarmInfo securityAlarmInfo = mRcContentAdapter.getData().get(position);
            mPresenter.clickItem(securityAlarmInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 历史记录删除弹框 确认按键监听 取消点击
     */

    @Override
    public void onCancelClick() {
        mHistoryClearDialog.dismiss();

    }

    /**
     * 历史记录删除弹框 确认按键监听
     */
    @Override
    public void onConfirmClick(String content, String diameter) {
        mPresenter.clearSearchHistory();
        mHistoryClearDialog.dismiss();
    }

    @Override
    public void onDestroyView() {
        if (mRootView != null) {
            ((ViewGroup) mRootView.getParent()).removeView(mRootView);
        }

        if (mProgressUtils != null) {
            mProgressUtils.destroyProgress();
            mProgressUtils = null;
        }
        if (mHistoryClearDialog != null) {
            mHistoryClearDialog.destroy();
            mHistoryClearDialog = null;
        }
        super.onDestroyView();
    }

    @OnClick({R2.id.fg_camera_warns_top_search_imv_clear, R2.id.btn_search_clear,
            R2.id.tv_top_search_alarm_search_cancel, R2.id.alarm_return_top,
            R2.id.iv_search_camera_warns_status, R2.id.tv_search_camera_warns_status,
            R2.id.tv_search_camera_warns_time, R2.id.iv_search_camera_warns_time,
            R2.id.layout_filter_process_status, R2.id.layout_filter_capture_time
    })
    public void onViewClicked(View view) {
        int i = view.getId();
        if (i == R.id.fg_camera_warns_top_search_imv_clear) {
            edFilterContent.getText().clear();
            edFilterContent.requestFocus();
            AppUtils.openInputMethodManager(mRootFragment.getActivity(), edFilterContent);
            setSearchHistoryVisible(true);
        } else if (i == R.id.btn_search_clear) {
            showHistoryClearDialog();
        } else if (i == R.id.tv_top_search_alarm_search_cancel) {
            cancelSearchData();
        } else if (i == R.id.alarm_return_top) {
            rvCameraWarnsContent.smoothScrollToPosition(0);
            mReturnTopImageView.setVisibility(View.GONE);
        } else if (i == R.id.layout_filter_capture_time || i == R.id.tv_search_camera_warns_time || i == R.id.iv_search_camera_warns_time) {
            setWarnFilterContent(WARN_FILTER_TIME);
        } else if (i == R.id.layout_filter_process_status || i == R.id.iv_search_camera_warns_status || i == R.id.tv_search_camera_warns_status) {
            setWarnFilterContent(WARN_FILTER_STATUS);
        }
    }


    /**
     * 设置 抓拍时间 处理状态
     *
     * @param filterType 0 时间 1 处理状态
     */
    private void setWarnFilterContent(int filterType) {
        if (WARN_FILTER_TIME == filterType) {
            //隐藏 状态选择弹窗
            if (mProcessStatusFilterPopUtils.isShowing()) {
                mProcessStatusFilterPopUtils.dismiss();
                ivFilterProcessStatus.setImageResource(R.drawable.ic_arrow_down);
                mPresenter.setFilterProcessStatus(-1);
            }
            //显示/隐藏 时间选择弹窗
            if (mCaptureTimeFilterPopUtils.isShowing()) {
                mCaptureTimeFilterPopUtils.dismiss();
                //向下箭头
                ivFilterCaptureTime.setImageResource(R.drawable.ic_arrow_down);
                mPresenter.setFilterCapturetime(-1);
            } else {
                mCaptureTimeFilterPopUtils.showAsDropDown(layoutFilterContent);
                //标题绿色 向上箭头
                ivFilterCaptureTime.setImageResource(R.drawable.ic_arrow_up);
                //tvFilterCaptureTime.setText(R.string.capture_time);
                tvFilterCaptureTime.getPaint().setFakeBoldText(true);
                tvFilterCaptureTime.setTextColor(getResources().getColor(R.color.c_1dbb99));
            }

        } else if (WARN_FILTER_STATUS == filterType) {
            //隐藏 时间选择弹窗
            if (mCaptureTimeFilterPopUtils.isShowing()) {
                mCaptureTimeFilterPopUtils.dismiss();
                ivFilterCaptureTime.setImageResource(R.drawable.ic_arrow_down);
                mPresenter.setFilterCapturetime(-1);
            }
            //显示/隐藏 状态选择弹窗
            if (mProcessStatusFilterPopUtils.isShowing()) {
                mProcessStatusFilterPopUtils.dismiss();
                //向下箭头
                ivFilterProcessStatus.setImageResource(R.drawable.ic_arrow_down);
                mPresenter.setFilterProcessStatus(-1);
            } else {
                mProcessStatusFilterPopUtils.showAsDropDown(layoutFilterContent);
                //标题绿色 向上箭头
                ivFilterProcessStatus.setImageResource(R.drawable.ic_arrow_up);
                //tvFilterProcessStatus.setText(R.string.process_status);
                tvFilterProcessStatus.getPaint().setFakeBoldText(true);
                tvFilterProcessStatus.setTextColor(getResources().getColor(R.color.c_1dbb99));

            }

        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraWarnListFragmentPresenter.REQUEST_CODE_DETAIL && resultCode == Activity.RESULT_OK) {
            mPresenter.requestSearchData(Constants.DIRECTION_DOWN);
        }
    }
}
