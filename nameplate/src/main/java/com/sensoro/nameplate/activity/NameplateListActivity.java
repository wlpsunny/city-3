package com.sensoro.nameplate.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sensoro.common.adapter.SearchHistoryAdapter;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.callback.RecycleViewItemClickListener;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.manger.SensoroLinearLayoutManager;
import com.sensoro.common.model.CameraFilterModel;
import com.sensoro.common.server.bean.DeviceCameraInfo;
import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.common.widgets.SpacesItemDecoration;
import com.sensoro.common.widgets.TipOperationDialogUtils;
import com.sensoro.nameplate.IMainViews.INameplateListActivityView;
import com.sensoro.nameplate.R;
import com.sensoro.nameplate.adapter.NameplateListAdapter;
import com.sensoro.nameplate.presenter.NameplateListActivityPresenter;

import java.util.ArrayList;
import java.util.List;

public class NameplateListActivity extends BaseActivity<INameplateListActivityView, NameplateListActivityPresenter>
        implements INameplateListActivityView, View.OnClickListener {


    ImageView ivNameplateListTopBack;
    EditText etNameplateListSearch;
    ImageView ivNameplateListSearchClear;
    LinearLayout llNameplateListSearchTop;
    TextView tvNameplateListSearchCancel;
    ImageView ivNameplateListFilter;
    ImageView ivNameplateListScan;
    LinearLayout llNameplateListTopSearch;
    ImageView noContent;
    TextView noContentTip;
    LinearLayout icNoContent;
    RecyclerView rvNameplateContent;
    SmartRefreshLayout refreshLayout;
    TextView tvSearchClear;
    ImageView btnSearchClear;
    RecyclerView rvSearchHistory;
    LinearLayout llSearchHistory;
    ImageView ivReturnTop;
    RelativeLayout llNameplateListRoot;
    private ProgressUtils mProgressUtils;
    private boolean isShowDialog = true;
    //    private DeviceCameraContentAdapter mDeviceCameraContentAdapter;
    private Animation returnTopAnimation;

    //    private CameraListFilterPopupWindowTest mCameraListFilterPopupWindow;
    private SearchHistoryAdapter mSearchHistoryAdapter;
    private TipOperationDialogUtils historyClearDialog;
    //
    private NameplateListAdapter nameplateListAdapter;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_nameplate_list);
        initView();
        mPresenter.initData(mActivity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        ivNameplateListTopBack = findViewById(R.id.iv_nameplate_list_top_back);
        etNameplateListSearch = findViewById(R.id.et_nameplate_list_search);
        ivNameplateListSearchClear = findViewById(R.id.iv_nameplate_list_search_clear);
        llNameplateListSearchTop = findViewById(R.id.ll_nameplate_list_search_top);
        tvNameplateListSearchCancel = findViewById(R.id.tv_nameplate_list_search_cancel);
        ivNameplateListFilter = findViewById(R.id.iv_nameplate_list_filter);
        ivNameplateListScan = findViewById(R.id.iv_nameplate_list_scan);
        llNameplateListTopSearch = findViewById(R.id.ll_nameplate_list_top_search);
        noContent = findViewById(R.id.no_content);
        noContentTip = findViewById(R.id.no_content_tip);
        icNoContent = findViewById(R.id.ic_no_content);
        rvNameplateContent = findViewById(R.id.rv_nameplate_content);
        refreshLayout = findViewById(R.id.refreshLayout);
        tvSearchClear = findViewById(R.id.tv_search_clear);
        btnSearchClear = findViewById(R.id.btn_search_clear);
        rvSearchHistory = findViewById(R.id.rv_search_history);
        llSearchHistory = findViewById(R.id.ll_search_history);
        ivReturnTop = findViewById(R.id.iv_return_top);
        llNameplateListRoot = findViewById(R.id.ll_nameplate_list_root);
        //
        ivNameplateListTopBack.setOnClickListener(this);
        etNameplateListSearch.setOnClickListener(this);
        ivNameplateListSearchClear.setOnClickListener(this);
        tvNameplateListSearchCancel.setOnClickListener(this);
        ivNameplateListFilter.setOnClickListener(this);
        ivNameplateListScan.setOnClickListener(this);
        btnSearchClear.setOnClickListener(this);
        ivReturnTop.setOnClickListener(this);

        //
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());
//        mDeviceCameraContentAdapter = new DeviceCameraContentAdapter(mActivity);
//        mDeviceCameraContentAdapter.setOnAlarmHistoryLogConfirmListener(this);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvNameplateContent.setLayoutManager(linearLayoutManager);
//        acHistoryLogRcContent.setAdapter(mDeviceCameraContentAdapter);
//        CustomDivider dividerItemDecoration = new CustomDivider(mActivity, DividerItemDecoration.VERTICAL);
//        acHistoryLogRcContent.addItemDecoration(dividerItemDecoration);
        //
        returnTopAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.return_top_in_anim);
        ivReturnTop.setAnimation(returnTopAnimation);
        ivReturnTop.setVisibility(View.GONE);
        //
        //新控件
        refreshLayout.setEnableAutoLoadMore(false);//开启自动加载功能（非必须）
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                isShowDialog = false;
                mPresenter.requestDataByFilter(Constants.DIRECTION_DOWN, getSearchText());
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull final RefreshLayout refreshLayout) {
                isShowDialog = false;
                mPresenter.requestDataByFilter(Constants.DIRECTION_UP, getSearchText());
            }
        });
        //
        nameplateListAdapter = new NameplateListAdapter(mActivity);
        final LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
//        CustomDivider dividerItemDecoration = new CustomDivider(mActivity, DividerItemDecoration.VERTICAL);
//        rvNameplateContent.addItemDecoration(dividerItemDecoration);
        rvNameplateContent.setLayoutManager(manager);
        rvNameplateContent.setAdapter(nameplateListAdapter);

        returnTopAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.return_top_in_anim);

        nameplateListAdapter.setOnClickListener(new NameplateListAdapter.OnNameplateListAdapterClickListener() {
            @Override
            public void onClick(View v, int position) {
                //点击
                mPresenter.doNameplateDetail(position);
            }

            @Override
            public void onDelete(int position) {
                //删除
            }
        });
        rvNameplateContent.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (linearLayoutManager.findFirstVisibleItemPosition() > 4) {
                    if (newState == 0) {
                        ivReturnTop.setVisibility(View.VISIBLE);
                        if (returnTopAnimation != null && returnTopAnimation.hasEnded()) {
                            ivReturnTop.startAnimation(returnTopAnimation);
                        }
                    } else {
                        ivReturnTop.setVisibility(View.GONE);
                    }
                } else {
                    ivReturnTop.setVisibility(View.GONE);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });
//        mCameraListFilterPopupWindow = new CameraListFilterPopupWindowTest(mActivity);
//        mCameraListFilterPopupWindow.setOnCameraListFilterPopupWindowListener(this);

        etNameplateListSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String text = getSearchText();
                    etNameplateListSearch.clearFocus();
                    if (!TextUtils.isEmpty(text)) {
                        mPresenter.save(text);
                    }
                    mPresenter.requestDataByFilter(Constants.DIRECTION_DOWN, text);
                    AppUtils.dismissInputMethodManager(NameplateListActivity.this, etNameplateListSearch);
                    setSearchHistoryVisible(false);

                    return true;
                }
                return false;
            }
        });
        etNameplateListSearch.addTextChangedListener(new TextWatcher() {
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

        AppUtils.getInputSoftStatus(llNameplateListRoot, new AppUtils.InputSoftStatusListener() {
            @Override
            public void onKeyBoardClose() {
                etNameplateListSearch.setCursorVisible(false);
            }

            @Override
            public void onKeyBoardOpen() {
                etNameplateListSearch.setCursorVisible(true);
            }
        });

        initRcSearchHistory();
        initClearHistoryDialog();

    }

    private void initClearHistoryDialog() {
        historyClearDialog = new TipOperationDialogUtils(NameplateListActivity.this, true);
        historyClearDialog.setTipTitleText(getString(R.string.history_clear_all));
        historyClearDialog.setTipMessageText(getString(R.string.confirm_clear_history_record), R.color.c_a6a6a6);
        historyClearDialog.setTipCancelText(getString(R.string.cancel), getResources().getColor(R.color.c_1dbb99));
        historyClearDialog.setTipConfirmText(getString(R.string.clear), getResources().getColor(R.color.c_a6a6a6));
        historyClearDialog.setTipDialogUtilsClickListener(new TipOperationDialogUtils.TipDialogUtilsClickListener() {
            @Override
            public void onCancelClick() {
                historyClearDialog.dismiss();

            }

            @Override
            public void onConfirmClick(String content, String diameter) {
                mPresenter.clearSearchHistory();
                historyClearDialog.dismiss();
            }
        });
    }

    private void initRcSearchHistory() {
        SensoroLinearLayoutManager layoutManager = new SensoroLinearLayoutManager(NameplateListActivity.this) {
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
        rvSearchHistory.addItemDecoration(new SpacesItemDecoration(false, AppUtils.dp2px(NameplateListActivity.this, 6)));
        mSearchHistoryAdapter = new SearchHistoryAdapter(NameplateListActivity.this, new
                RecycleViewItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String searchText = null;
                        String text = mSearchHistoryAdapter.getSearchHistoryList().get(position);
                        if (!TextUtils.isEmpty(text)) {
                            searchText = text;
                            etNameplateListSearch.setText(searchText);
                            etNameplateListSearch.setSelection(etNameplateListSearch.getText().toString().length());
                        }
                        ivNameplateListSearchClear.setVisibility(View.VISIBLE);
                        etNameplateListSearch.clearFocus();
                        AppUtils.dismissInputMethodManager(NameplateListActivity.this, etNameplateListSearch);
                        setSearchHistoryVisible(false);
                        mPresenter.save(searchText);
                        mPresenter.requestDataByFilter(Constants.DIRECTION_DOWN, searchText);

                    }
                });
        rvSearchHistory.setAdapter(mSearchHistoryAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (returnTopAnimation != null) {
            returnTopAnimation.cancel();
            returnTopAnimation = null;
        }
    }

    @Override
    public void setSearchClearImvVisible(boolean isVisible) {
        ivNameplateListSearchClear.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected NameplateListActivityPresenter createPresenter() {
        return new NameplateListActivityPresenter();
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

//    @Override
//    public void onItemClick(View v, int position) {
//        DeviceCameraInfo deviceCameraInfo = mDeviceCameraContentAdapter.getData().get(position);
//        mPresenter.onClickDeviceCamera(deviceCameraInfo);
//    }

    @Override
    public void updateDeviceCameraAdapter(List<DeviceCameraInfo> data) {
//        if (data != null && data.size() > 0) {
//            mDeviceCameraContentAdapter.updateAdapter(data);
//        }
        ArrayList<String> strings = new ArrayList<>();
        for (int i=0;i<10;i++){
            strings.add("名称是房间爱离开房间爱离 "+i);
        }

        nameplateListAdapter.updateData(strings);
        setNoContentVisible(data == null || data.size() < 1);
    }


    @Override
    public void setNoContentVisible(boolean isVisible) {
        icNoContent.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        rvNameplateContent.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setSmartRefreshEnable(boolean enable) {
        refreshLayout.setEnableLoadMore(enable);
        refreshLayout.setEnableRefresh(enable);
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
        setSearchButtonTextVisible(isVisible);
    }

    @Override
    public void setSearchButtonTextVisible(boolean isVisible) {
        if (isVisible) {
            tvNameplateListSearchCancel.setVisibility(View.VISIBLE);
//            setEditTextState(false);
//            AppUtils.dismissInputMethodManager(mRootFragment.getActivity(), fgMainWarnEtSearch);
        } else if (TextUtils.isEmpty(etNameplateListSearch.getText().toString())) {
            etNameplateListSearch.setVisibility(View.GONE);
//            setEditTextState(true);
        }

    }

    @Override
    public void showCameraListFilterPopupWindow(List<CameraFilterModel> data) {

    }

    @Override
    public void dismissCameraListFilterPopupWindow() {

    }

    @Override
    public void updateCameraListFilterPopupWindowStatusList(List<CameraFilterModel> list) {

    }

    @Override
    public void setCameraListFilterPopupWindowSelectState(boolean hasSelect) {
//        if (hasSelect) {
//            cameraListIvFilter.setImageResource(R.drawable.camera_filter_selected);
//        } else {
//            cameraListIvFilter.setImageResource(R.drawable.camera_filter_unselected);
//        }
    }

    @Override
    public void showHistoryClearDialog() {
        if (historyClearDialog != null) {
            historyClearDialog.show();
        }
    }

    @Override
    public void onPullRefreshComplete() {
        refreshLayout.finishLoadMore();
        refreshLayout.finishRefresh();
    }


    @Override
    public void onBackPressed() {
//        if (mCameraListFilterPopupWindow.isShowing()) {
//            mPresenter.onCameraListFilterPopupWindowDismiss();
//        } else {
//            super.onBackPressed();
//        }

    }

    private String getSearchText() {
        String text = etNameplateListSearch.getText().toString();
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        return text;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_nameplate_list_top_back) {
            finishAc();
            AppUtils.dismissInputMethodManager(mActivity, etNameplateListSearch);
        } else if (i == R.id.et_nameplate_list_search) {
            etNameplateListSearch.requestFocus();
            etNameplateListSearch.setCursorVisible(true);
            setSearchHistoryVisible(true);
        } else if (i == R.id.iv_nameplate_list_search_clear) {
            etNameplateListSearch.setText("");
            etNameplateListSearch.requestFocus();
            AppUtils.openInputMethodManager(NameplateListActivity.this, etNameplateListSearch);
            setSearchHistoryVisible(true);
        } else if (i == R.id.tv_nameplate_list_search_cancel) {
            if (tvNameplateListSearchCancel.getVisibility() == View.VISIBLE) {
                etNameplateListSearch.getText().clear();
            }
            mPresenter.requestDataByFilter(Constants.DIRECTION_DOWN, null);
            setSearchHistoryVisible(false);
            AppUtils.dismissInputMethodManager(NameplateListActivity.this, etNameplateListSearch);
        } else if (i == R.id.iv_nameplate_list_filter) {
        } else if (i == R.id.iv_nameplate_list_scan) {
        } else if (i == R.id.btn_search_clear) {//
        } else if (i == R.id.iv_return_top) {
            rvNameplateContent.smoothScrollToPosition(0);
            ivReturnTop.setVisibility(View.GONE);
            refreshLayout.closeHeaderOrFooter();
        }
    }


//    @Override
//    public void onSave(List<CameraFilterModel> list) {
//        mPresenter.onSaveCameraListFilterPopupWindowDismiss(list, getSearchText());
//    }
//
//    @Override
//    public void onDismiss() {
//        mPresenter.onCameraListFilterPopupWindowDismiss();
//    }
//
//    @Override
//    public void onReset() {
//        mPresenter.onResetCameraListFilterPopupWindowDismiss(getSearchText());
//    }
}