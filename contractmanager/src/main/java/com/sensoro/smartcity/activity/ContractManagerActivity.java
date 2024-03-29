package com.sensoro.smartcity.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sensoro.common.adapter.SearchHistoryAdapter;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.callback.RecycleViewItemClickListener;
import com.sensoro.common.constant.ARouterConstants;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.manger.SensoroLinearLayoutManager;
import com.sensoro.common.model.StatusCountModel;
import com.sensoro.common.server.bean.ContractListInfo;
import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.common.widgets.SpacesItemDecoration;
import com.sensoro.common.widgets.StatePopUtils;
import com.sensoro.common.widgets.TipOperationDialogUtils;
import com.sensoro.contractmanager.R;
import com.sensoro.contractmanager.R2;
import com.sensoro.smartcity.adapter.ContractListAdapter;
import com.sensoro.smartcity.imainviews.IContractManagerActivityView;
import com.sensoro.smartcity.presenter.ContractManagerActivityPresenter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sensoro.common.constant.Constants.DIRECTION_DOWN;
import static com.sensoro.common.constant.Constants.DIRECTION_UP;

@Route(path = ARouterConstants.ACTIVITY_CONTRACT_MANAGER)
public class ContractManagerActivity extends BaseActivity<IContractManagerActivityView, ContractManagerActivityPresenter> implements IContractManagerActivityView, View.OnClickListener, TipOperationDialogUtils.TipDialogUtilsClickListener {
    @BindView(R2.id.contract_iv_menu_list)
    ImageView contractIvMenuList;
    @BindView(R2.id.contract_manger_root)
    RelativeLayout contractMangerRoot;
    @BindView(R2.id.ac_contract_manger_add)
    RelativeLayout acContractMangerAdd;
    @BindView(R2.id.contract_ptr_list)
    RecyclerView contractPtrList;
    @BindView(R2.id.contract_return_top)
    ImageView contractReturnTop;
    @BindView(R2.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    View icNoContent;
    @BindView(R2.id.contract_tv_select_type)
    TextView tvSelectType;
    @BindView(R2.id.contract_tv_select_status)
    TextView tvSelectStatus;
    @BindView(R2.id.contract_cl_select_root)
    ConstraintLayout clSelectRoot;
    @BindView(R2.id.fg_main_top_search_title_root)
    LinearLayout fgMainWarnTitleRoot;
    @BindView(R2.id.ac_contract_record_frame_search)
    RelativeLayout fgMainWarnFrameSearch;
    @BindView(R2.id.ac_contract_record_et_search)
    EditText fgMainWarnEtSearch;
    @BindView(R2.id.ac_contract_record_imv_calendar)
    ImageView fgMainWarnImvCalendar;
    @BindView(R2.id.tv_contract_search_cancel)
    TextView tvWarnAlarmSearchCancel;
    @BindView(R2.id.rv_search_history)
    RecyclerView rvSearchHistory;
    @BindView(R2.id.btn_search_clear)
    ImageView btnSearchClear;
    @BindView(R2.id.ll_search_history)
    LinearLayout llSearchHistory;
    @BindView(R2.id.ac_contract_record_search_imv_clear)
    ImageView fgMainWarnFragmentImvClear;
    @BindView(R2.id.fg_main_top_search_tv_date_edit)
    TextView fgMainWarnTvDateEdit;
    @BindView(R2.id.fg_main_warn_top_search_date_close)
    ImageView fgMainWarnImvDateClose;
    @BindView(R2.id.fg_main_top_search_rl_date_edit)
    RelativeLayout fgMainWarnRlDateEdit;

    private ProgressUtils mProgressUtils;
    private boolean isShowDialog = true;
    private ContractListAdapter mContractListAdapter;
    private StatePopUtils mSelectStatusPop;
    private Drawable blackTriangle;
    private Drawable grayTriangle;
    private StatePopUtils mSelectTypePop;
    private SearchHistoryAdapter mSearchHistoryAdapter;
    private TipOperationDialogUtils historyClearDialog;
    private Animation returnTopAnimation;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_contract_manager);
        ButterKnife.bind(mActivity);
        initView();
        mPresenter.initData(mActivity);
    }

    private void initView() {

        icNoContent = LayoutInflater.from(this).inflate(R.layout.no_content, null);

        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());

        fgMainWarnEtSearch.setHint(R.string.legal_representative_name);
        refreshLayout.setEnableAutoLoadMore(true);//开启自动加载功能（非必须）
        refreshLayout.setNoMoreData(false);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
////                mPresenter.requestTopData();
                isShowDialog = false;
                String text = fgMainWarnEtSearch.getText().toString();
                mPresenter.requestSearchData(Constants.DIRECTION_DOWN, text);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                isShowDialog = false;
                String text = fgMainWarnEtSearch.getText().toString();
                mPresenter.requestSearchData(Constants.DIRECTION_UP, text);
            }
        });
        mContractListAdapter = new ContractListAdapter(mActivity);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        contractPtrList.setLayoutManager(linearLayoutManager);
        contractPtrList.setAdapter(mContractListAdapter);
        mContractListAdapter.setOnClickListener(new ContractListAdapter.OnClickListener() {
            @Override
            public void onClick(View v, int position) {
                mPresenter.clickItem(position);
            }
        });
        returnTopAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.return_top_in_anim);
        contractPtrList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                if (xLinearLayoutManager.findFirstVisibleItemPosition() == 0 && newState == SCROLL_STATE_IDLE &&
//                        toolbarDirection == DIRECTION_DOWN) {
////                    mListRecyclerView.setre
//                }
                if (linearLayoutManager.findFirstVisibleItemPosition() > 4) {
                    if (newState == 0) {
                        contractReturnTop.setVisibility(View.VISIBLE);
                        contractReturnTop.setAnimation(returnTopAnimation);
                        if (returnTopAnimation != null && returnTopAnimation.hasEnded()) {
                            contractReturnTop.startAnimation(returnTopAnimation);
                        }
                    } else {
                        contractReturnTop.setVisibility(View.GONE);
                    }
                } else {
                    contractReturnTop.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });
        contractIvMenuList.setOnClickListener(this);
        contractIvMenuList.setOnClickListener(this);
        acContractMangerAdd.setOnClickListener(this);
        contractReturnTop.setOnClickListener(this);
        tvSelectType.setOnClickListener(this);
        tvSelectStatus.setOnClickListener(this);
        fgMainWarnFrameSearch.setOnClickListener(this);
        fgMainWarnEtSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fgMainWarnEtSearch.requestFocus();
                fgMainWarnEtSearch.setCursorVisible(true);
                setSearchHistoryVisible(true);
                AppUtils.openInputMethodManager(mActivity, fgMainWarnEtSearch);
//                forceOpenSoftKeyboard();
                return false;
            }
        });
        tvWarnAlarmSearchCancel.setOnClickListener(this);
        fgMainWarnFragmentImvClear.setOnClickListener(this);
        fgMainWarnImvCalendar.setOnClickListener(this);
        fgMainWarnImvDateClose.setOnClickListener(this);
        btnSearchClear.setOnClickListener(this);

        blackTriangle = mActivity.getResources().getDrawable(R.drawable.main_small_triangle);
        blackTriangle.setBounds(0, 0, blackTriangle.getMinimumWidth(), blackTriangle.getMinimumHeight());
        grayTriangle = mActivity.getResources().getDrawable(R.drawable.main_small_triangle_gray);
        grayTriangle.setBounds(0, 0, blackTriangle.getMinimumWidth(), blackTriangle.getMinimumHeight());
        initSelectTypePop();
        initSelectStatusPop();
        fgMainWarnEtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // 当按了搜索之后关闭软键盘
                    String text = fgMainWarnEtSearch.getText().toString();
                    mPresenter.save(text);
                    fgMainWarnEtSearch.clearFocus();
                    mPresenter.requestSearchData(Constants.DIRECTION_DOWN, text);
                    AppUtils.dismissInputMethodManager(mActivity, fgMainWarnEtSearch);
                    setSearchHistoryVisible(false);
                    return true;
                }
                return false;
            }
        });
        fgMainWarnEtSearch.addTextChangedListener(new TextWatcher() {
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
        AppUtils.getInputSoftStatus(contractMangerRoot, new AppUtils.InputSoftStatusListener() {
            @Override
            public void onKeyBoardClose() {
                fgMainWarnEtSearch.setCursorVisible(false);
            }

            @Override
            public void onKeyBoardOpen() {
                fgMainWarnEtSearch.setCursorVisible(true);
            }
        });

        //搜索历史的recyclerview
        initRcSearchHistory();
        initClearHistoryDialog();
    }

    private void initClearHistoryDialog() {
        historyClearDialog = new TipOperationDialogUtils(mActivity, true);
        historyClearDialog.setTipTitleText(getString(R.string.history_clear_all));
        historyClearDialog.setTipMessageText(getString(R.string.confirm_clear_history_record), R.color.c_a6a6a6);
        historyClearDialog.setTipCancelText(getString(R.string.cancel), getResources().getColor(R.color.c_1dbb99));
        historyClearDialog.setTipConfirmText(getString(R.string.clear), getResources().getColor(R.color.c_a6a6a6));
        historyClearDialog.setTipDialogUtilsClickListener(this);
    }

    private void initRcSearchHistory() {
        SensoroLinearLayoutManager layoutManager = new SensoroLinearLayoutManager(mActivity) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvSearchHistory.setLayoutManager(layoutManager);
        rvSearchHistory.addItemDecoration(new SpacesItemDecoration(false, AppUtils.dp2px(mActivity, 6)));
        mSearchHistoryAdapter = new SearchHistoryAdapter(mActivity, new
                RecycleViewItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String text = mSearchHistoryAdapter.getSearchHistoryList().get(position);
                        if (!TextUtils.isEmpty(text)) {
                            fgMainWarnEtSearch.setText(text);
                            fgMainWarnEtSearch.setSelection(fgMainWarnEtSearch.getText().toString().length());
                        }
                        fgMainWarnFragmentImvClear.setVisibility(View.VISIBLE);
                        fgMainWarnEtSearch.clearFocus();
                        AppUtils.dismissInputMethodManager(mActivity, fgMainWarnEtSearch);
                        setSearchHistoryVisible(false);
                        mPresenter.save(text);
                        mPresenter.requestSearchData(Constants.DIRECTION_DOWN, text);
                    }
                });
        rvSearchHistory.setAdapter(mSearchHistoryAdapter);
    }

    @Override
    public void setSearchClearImvVisible(boolean isVisible) {
        fgMainWarnFragmentImvClear.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void initSelectTypePop() {
        mSelectTypePop = new StatePopUtils(mActivity);
        mSelectTypePop.setSelectDeviceTypeItemClickListener(new StatePopUtils.SelectDeviceTypeItemClickListener() {
            @Override
            public void onSelectDeviceTypeItemClick(View view, int position) {
                //选择类型的pop点击事件
                StatusCountModel item = mSelectTypePop.getItem(position);
                mPresenter.doSelectTypeDevice(item);
                Resources resources = mActivity.getResources();
                if (position == 0) {
                    tvSelectType.setText(R.string.all_contracts);
                    tvSelectType.setTextColor(resources.getColor(R.color.c_a6a6a6));
                    tvSelectType.setCompoundDrawables(null, null, grayTriangle, null);
                } else {
                    tvSelectType.setTextColor(resources.getColor(R.color.c_252525));
                    tvSelectType.setCompoundDrawables(null, null, blackTriangle, null);
                    tvSelectType.setText(item.statusTitle);
                }
                mSelectTypePop.dismiss();

            }
        });
    }

    private void initSelectStatusPop() {
        mSelectStatusPop = new StatePopUtils(mActivity);
        mSelectStatusPop.setSelectDeviceTypeItemClickListener(new StatePopUtils.SelectDeviceTypeItemClickListener() {
            @Override
            public void onSelectDeviceTypeItemClick(View view, int position) {
                //选择类型的pop点击事件
                StatusCountModel item = mSelectStatusPop.getItem(position);
                mPresenter.doSelectStatusDevice(item);
                Resources resources = mActivity.getResources();
                if (position == 0) {
                    tvSelectStatus.setTextColor(resources.getColor(R.color.c_a6a6a6));
                    tvSelectStatus.setCompoundDrawables(null, null, grayTriangle, null);
                    tvSelectStatus.setText(R.string.all_states);
                } else {
                    tvSelectStatus.setTextColor(resources.getColor(R.color.c_252525));
                    tvSelectStatus.setCompoundDrawables(null, null, blackTriangle, null);
                    tvSelectStatus.setText(item.statusTitle);
                }
                mSelectStatusPop.dismiss();

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mSelectTypePop.isShowing()) {
            mSelectTypePop.dismiss();
        } else if (mSelectStatusPop.isShowing()) {
            mSelectStatusPop.dismiss();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onPause() {
        AppUtils.dismissInputMethodManager(mActivity, fgMainWarnEtSearch);
        super.onPause();
    }

    @Override
    public void onPullRefreshComplete() {
        refreshLayout.finishLoadMore();
        refreshLayout.finishRefresh();
//        contractPtrList.onRefreshComplete();
    }

//    @Override
//    public PullToRefreshBase.State getPullRefreshState() {
//        return contractPtrList.getState();
//    }

    @Override
    public void requestDataByDirection(int direction, boolean isFirst) {
        mPresenter.requestDataByDirection(direction, isFirst);
    }

    @Override
    public void updateContractList(List<ContractListInfo> data) {
        if (data != null && data.size() > 0) {
            mContractListAdapter.setData(data);
            mContractListAdapter.notifyDataSetChanged();
        }
        setNoContentVisible(data == null || data.size() < 1);
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
            refreshLayout.setRefreshContent(contractPtrList);
        }


    }

    @Override
    public void UpdateSelectStatusPopList(List<StatusCountModel> list) {
        if (mSelectStatusPop != null) {
            mSelectStatusPop.updateSelectDeviceStatusList(list);
        }
    }

    @Override
    public void showSelectStatusPop() {
        if (mSelectStatusPop != null) {
            mSelectStatusPop.showAsDropDown(clSelectRoot);
        }
    }

    @Override
    public void UpdateSelectTypePopList(List<StatusCountModel> list) {
        if (mSelectTypePop != null) {
            mSelectTypePop.updateSelectDeviceStatusList(list);
        }
    }

    @Override
    public void showSelectStTypePop() {
        if (mSelectTypePop != null) {
            mSelectTypePop.showAsDropDown(clSelectRoot);
        }
    }

    @Override
    public void smoothScrollToPosition(int position) {
//        contractPtrList.setSmoothScrollbarEnabled(true);
        contractPtrList.smoothScrollToPosition(position);

    }

    @Override
    public void closeRefreshHeaderOrFooter() {
        refreshLayout.closeHeaderOrFooter();
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
        if (isShowDialog) {
            mProgressUtils.showProgress();
        }
        isShowDialog = true;
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

    @Override
    public void onClick(View view) {
        int viewID = view.getId();
        if (viewID == R.id.ac_contract_manger_add) {
            mPresenter.startToAdd();
        } else if (viewID == R.id.contract_iv_menu_list) {
            finishAc();
        } else if (viewID == R.id.contract_return_top) {
            contractPtrList.smoothScrollToPosition(0);
            closeRefreshHeaderOrFooter();
        } else if (viewID == R.id.contract_tv_select_type) {
            if (mSelectStatusPop != null && mSelectStatusPop.isShowing()) {
                mSelectStatusPop.dismiss();
            }
            AppUtils.dismissInputMethodManager(mActivity, fgMainWarnEtSearch);
            mPresenter.doSelectTypePop();
        } else if (viewID == R.id.btn_search_clear) {
            showHistoryClearDialog();
        } else if (viewID == R.id.contract_tv_select_status) {
            AppUtils.dismissInputMethodManager(mActivity, fgMainWarnEtSearch);
            mPresenter.doSelectStatusPop();
        } else if (viewID == R.id.ac_contract_record_imv_calendar) {
            mPresenter.doCalendar(fgMainWarnTitleRoot);
            AppUtils.dismissInputMethodManager(mActivity, fgMainWarnEtSearch);
        } else if (viewID == R.id.fg_main_warn_top_search_date_close) {
            fgMainWarnRlDateEdit.setVisibility(View.GONE);
            String text = fgMainWarnEtSearch.getText().toString();
            setSearchHistoryVisible(false);
            AppUtils.dismissInputMethodManager(mActivity, fgMainWarnEtSearch);
            mPresenter.requestSearchData(Constants.DIRECTION_DOWN, text);
        } else if (viewID == R.id.tv_contract_search_cancel) {
            doCancelSearch();
            setSearchHistoryVisible(false);
            AppUtils.dismissInputMethodManager(mActivity, fgMainWarnEtSearch);
        } else if (viewID == R.id.ac_contract_record_search_imv_clear) {
            fgMainWarnEtSearch.getText().clear();
            fgMainWarnEtSearch.requestFocus();
            AppUtils.openInputMethodManager(mActivity, fgMainWarnEtSearch);
        }
    }

    @Override
    public void setSearchHistoryVisible(boolean isVisible) {
        llSearchHistory.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        refreshLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        setSearchButtonTextVisible(isVisible);
    }

    @Override
    public void UpdateSearchHistoryList(List<String> data) {
        btnSearchClear.setVisibility(data.size() > 0 ? View.VISIBLE : View.GONE);
        mSearchHistoryAdapter.updateSearchHistoryAdapter(data);
    }

    @Override
    public boolean isSelectedDateLayoutVisible() {
        return fgMainWarnRlDateEdit.getVisibility() == View.VISIBLE;
    }

    @Override
    public void setSelectedDateLayoutVisible(boolean isVisible) {
        fgMainWarnRlDateEdit.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSelectedDateSearchText(String content) {
        fgMainWarnTvDateEdit.setText(content);
    }

    @Override
    public void showHistoryClearDialog() {
        if (historyClearDialog != null) {
            historyClearDialog.show();
        }
    }

    @Override
    public void setContractMangerAddVisible(boolean isVisible) {
        acContractMangerAdd.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void doCancelSearch() {
        if (getSearchTextVisible()) {
            fgMainWarnEtSearch.getText().clear();
        }
        mPresenter.doCancelSearch();
    }

    @Override
    public boolean getSearchTextVisible() {
        return tvWarnAlarmSearchCancel.getVisibility() == View.VISIBLE;
    }

    @Override
    public void setSearchButtonTextVisible(boolean isVisible) {
        if (isVisible) {
            tvWarnAlarmSearchCancel.setVisibility(View.VISIBLE);
//            setEditTextState(false);
            AppUtils.dismissInputMethodManager(mActivity, fgMainWarnEtSearch);
        } else {
            tvWarnAlarmSearchCancel.setVisibility(View.GONE);
//            setEditTextState(true);
        }

    }

    @Override
    protected void onDestroy() {
        if (returnTopAnimation != null) {
            returnTopAnimation.cancel();
        }
        if (mProgressUtils != null) {
            mProgressUtils.destroyProgress();
            mProgressUtils = null;
        }

        if (historyClearDialog != null) {
            historyClearDialog.destroy();
            historyClearDialog = null;
        }
        super.onDestroy();
    }


    @Override
    protected ContractManagerActivityPresenter createPresenter() {
        return new ContractManagerActivityPresenter();
    }

    @Override
    public void onCancelClick() {
        if (historyClearDialog != null) {
            historyClearDialog.dismiss();
        }
    }

    @Override
    public void onConfirmClick(String content, String diameter) {
        mPresenter.clearSearchHistory();
        if (historyClearDialog != null) {
            historyClearDialog.dismiss();
        }
    }
}
