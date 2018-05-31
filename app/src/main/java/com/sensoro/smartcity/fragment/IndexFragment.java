package com.sensoro.smartcity.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.activity.MainActivity;
import com.sensoro.smartcity.activity.SearchDeviceActivity;
import com.sensoro.smartcity.adapter.IndexGridAdapter;
import com.sensoro.smartcity.adapter.IndexListAdapter;
import com.sensoro.smartcity.base.BaseFragment;
import com.sensoro.smartcity.imainviews.IIndexFragmentView;
import com.sensoro.smartcity.presenter.IndexFragmentPresenter;
import com.sensoro.smartcity.server.bean.Character;
import com.sensoro.smartcity.server.bean.DeviceInfo;
import com.sensoro.smartcity.server.response.DeviceInfoListRsp;
import com.sensoro.smartcity.widget.ProgressUtils;
import com.sensoro.smartcity.widget.RecycleViewItemClickListener;
import com.sensoro.smartcity.widget.SensoroShadowView;
import com.sensoro.smartcity.widget.SensoroXGridLayoutManager;
import com.sensoro.smartcity.widget.SensoroXLinearLayoutManager;
import com.sensoro.smartcity.widget.SpacesItemDecoration;
import com.sensoro.smartcity.widget.popup.SensoroPopupStatusView;
import com.sensoro.smartcity.widget.popup.SensoroPopupTypeView;
import com.sensoro.smartcity.widget.statusbar.StatusBarCompat;

import java.util.Collections;
import java.util.List;

import static android.view.View.VISIBLE;
import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
import static com.sensoro.smartcity.constant.Constants.DIRECTION_DOWN;
import static com.sensoro.smartcity.constant.Constants.DIRECTION_UP;
import static com.sensoro.smartcity.constant.Constants.EXTRA_FRAGMENT_INDEX;
import static com.sensoro.smartcity.constant.Constants.INDEX_STATUS_ARRAY;
import static com.sensoro.smartcity.constant.Constants.INDEX_TYPE_ARRAY;
import static com.sensoro.smartcity.constant.Constants.INPUT;
import static com.sensoro.smartcity.constant.Constants.REQUEST_CODE_SEARCH_DEVICE;
import static com.sensoro.smartcity.constant.Constants.TYPE_GRID;
import static com.sensoro.smartcity.constant.Constants.TYPE_LIST;

/**
 * Created by sensoro on 17/11/6.
 */

public class IndexFragment extends BaseFragment<IIndexFragmentView, IndexFragmentPresenter> implements
        IIndexFragmentView,
        View.OnClickListener,
        RecycleViewItemClickListener, ViewPager.OnPageChangeListener, AppBarLayout.OnOffsetChangedListener {

    private ImageView mSwitchImageView;
    private ImageView mSearchImageView;
    private ImageView mTypeImageView;
    private ImageView mStatusImageView;
    private ImageView mReturnTopImageView;
    private SensoroPopupTypeView mTypePopupView;
    private SensoroPopupStatusView mStatusPopupView;
    private IndexListAdapter mListAdapter;
    private IndexGridAdapter mGridAdapter;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    //    private Toolbar mToolbar;
    private View mToolbar1;
    private View mToolbar2;
    private TextView mTypeTextView;
    private TextView mStatusTextView;
    private TextView mTitleTextView;
    private TextView mHeadAlarmNumTextView;
    private TextView mHeadLostNumTextView;
    private TextView mHeadInactiveNumTextView;
    private TextView mHeadAlarmTitleTextView;
    private TextView mHeadLostTitleTextView;
    private TextView mHeadInactiveTitleTextView;
    private XRecyclerView mListRecyclerView;
    private XRecyclerView mGridRecyclerView;
    private SensoroXLinearLayoutManager xLinearLayoutManager;
    private SensoroXGridLayoutManager xGridLayoutManager;
    private LinearLayout alarmLayout;
    private LinearLayout mListLayout;
    private LinearLayout mGridLayout;
    private LinearLayout mHeadLayout;
    private SensoroShadowView mTypeShadowLayout;
    private SensoroShadowView mStatusShadowLayout;
    private Animation returnTopAnimation;
    private ProgressUtils mProgressUtils;

    private int toolbarDirection = DIRECTION_DOWN;
    private int switchType;

    public static IndexFragment newInstance(Character input) {
        IndexFragment indexFragment = new IndexFragment();
        Bundle args = new Bundle();
        args.putSerializable(INPUT, input);
        indexFragment.setArguments(args);
        return indexFragment;
    }

    @Override
    protected void initData(Context activity) {
        initView();
        mPrestener.initData(activity);
    }


    @Override
    public void onDestroyView() {
        if (returnTopAnimation != null) {
            returnTopAnimation.cancel();
            returnTopAnimation = null;
        }
        if (mRootView != null) {
            ((ViewGroup) mRootView.getParent()).removeView(mRootView);
        }

        if (mGridAdapter != null) {
            mGridAdapter.getData().clear();
        }
        if (mListAdapter != null) {
            mListAdapter.getData().clear();
        }
        if (mProgressUtils != null) {
            mProgressUtils.destroyProgress();
            mProgressUtils = null;
        }
        mPrestener.onDestroy();
        super.onDestroyView();
    }


    @Override
    protected int initRootViewId() {
        return R.layout.fragment_index;
    }

    @Override
    protected IndexFragmentPresenter createPresenter() {
        return new IndexFragmentPresenter();
    }

    @Override
    public void onStart() {
        super.onStart();
        //TODO 初始化刷新数据操作
        refreshCityInfo();
    }

    public void initView() {
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mRootFragment.getActivity()).build());
        collapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.toolbar_layout);
//        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        mAppBarLayout = (AppBarLayout) mRootView.findViewById(R.id.app_bar);
        mToolbar1 = mRootView.findViewById(R.id.index_toolbar1);
        mToolbar2 = mRootView.findViewById(R.id.index_toolbar2);
        mTitleTextView = (TextView) mRootView.findViewById(R.id.index_tv_title);
        mHeadLayout = (LinearLayout) mRootView.findViewById(R.id.index_layout_head1);
        alarmLayout = (LinearLayout) mRootView.findViewById(R.id.index_head_alarm_layout);
        mHeadAlarmNumTextView = (TextView) mRootView.findViewById(R.id.index_head_alarm_num);
        mHeadLostNumTextView = (TextView) mRootView.findViewById(R.id.index_head_lost_num);
        mHeadInactiveNumTextView = (TextView) mRootView.findViewById(R.id.index_head_inactive_num);
        mHeadAlarmTitleTextView = (TextView) mRootView.findViewById(R.id.index_head_alarm_num_title);
        mHeadLostTitleTextView = (TextView) mRootView.findViewById(R.id.index_head_lost_num_title);
        mHeadInactiveTitleTextView = (TextView) mRootView.findViewById(R.id.index_head_inactive_num_title);
        mSwitchImageView = (ImageView) mRootView.findViewById(R.id.index_iv_switch);
        mSearchImageView = (ImageView) mRootView.findViewById(R.id.index_iv_search);
        mTypeTextView = (TextView) mRootView.findViewById(R.id.index_tv_type);
        mStatusTextView = (TextView) mRootView.findViewById(R.id.index_tv_status);
        mTypeImageView = (ImageView) mRootView.findViewById(R.id.index_iv_type);
        mStatusImageView = (ImageView) mRootView.findViewById(R.id.index_iv_status);
        mReturnTopImageView = (ImageView) mRootView.findViewById(R.id.index_return_top);
        mTypeShadowLayout = (SensoroShadowView) mRootView.findViewById(R.id.index_type_shadow);
        mTypePopupView = (SensoroPopupTypeView) mRootView.findViewById(R.id.index_type_popup);
        mStatusShadowLayout = (SensoroShadowView) mRootView.findViewById(R.id.index_status_shadow);
        mStatusPopupView = (SensoroPopupStatusView) mRootView.findViewById(R.id.index_status_popup);
        mListLayout = (LinearLayout) mRootView.findViewById(R.id.layout_index_list);
        mGridLayout = (LinearLayout) mRootView.findViewById(R.id.layout_index_grid);
        mAppBarLayout.addOnOffsetChangedListener(this);
        mReturnTopImageView.setOnClickListener(this);
        mTypeTextView.setOnClickListener(this);
        mStatusTextView.setOnClickListener(this);
        mTypeImageView.setOnClickListener(this);
        mStatusImageView.setOnClickListener(this);
        mSwitchImageView.setOnClickListener(this);
        mSearchImageView.setOnClickListener(this);
        mHeadAlarmNumTextView.setOnClickListener(this);
        mHeadAlarmTitleTextView.setOnClickListener(this);
        mHeadInactiveNumTextView.setOnClickListener(this);
        mHeadInactiveTitleTextView.setOnClickListener(this);
        mHeadLostNumTextView.setOnClickListener(this);
        mHeadLostTitleTextView.setOnClickListener(this);
        mRootView.findViewById(R.id.index_iv_menu_list).setOnClickListener(this);
        mRootView.findViewById(R.id.index_iv_menu_list_reverse).setOnClickListener(this);
        mRootView.findViewById(R.id.index_iv_search_reverse).setOnClickListener(this);
        //
        mListRecyclerView = (XRecyclerView) mRootView.findViewById(R.id.index_rv_list);
        mGridRecyclerView = (XRecyclerView) mRootView.findViewById(R.id.index_rv_grid);
        //
        mSearchImageView.setColorFilter(Color.WHITE);
        returnTopAnimation = AnimationUtils.loadAnimation(mRootFragment.getContext(), R.anim.return_top_in_anim);
        mReturnTopImageView.setAnimation(returnTopAnimation);
        mReturnTopImageView.setVisibility(View.GONE);
        initListView();
        initGridView();
    }

    private void initListView() {
        xLinearLayoutManager = new SensoroXLinearLayoutManager(mRootFragment.getContext());
        mListAdapter = new IndexListAdapter(mRootFragment.getContext(), this);
        mListRecyclerView.setAdapter(mListAdapter);
        mListRecyclerView.setLayoutManager(xLinearLayoutManager);
        int spacingInPixels = mRootFragment.getResources().getDimensionPixelSize(R.dimen.x15);
        mListRecyclerView.addItemDecoration(new SpacesItemDecoration(false, spacingInPixels));
        mListRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                reFreshDataByDirection(DIRECTION_DOWN);
            }

            @Override
            public void onLoadMore() {
                reFreshDataByDirection(DIRECTION_UP);
            }
        });
        mListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (xLinearLayoutManager.findFirstVisibleItemPosition() == 0 && newState == SCROLL_STATE_IDLE &&
                        toolbarDirection == DIRECTION_DOWN) {
//                    mListRecyclerView.setre
                }
                if (xLinearLayoutManager.findFirstVisibleItemPosition() > 4) {
                    if (newState == 0) {
                        mReturnTopImageView.setVisibility(VISIBLE);
                        if (returnTopAnimation.hasEnded()) {
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


    private void initGridView() {
        xGridLayoutManager = new SensoroXGridLayoutManager(mRootFragment.getContext(), 3);
        mGridAdapter = new IndexGridAdapter(mRootFragment.getContext(), this);
        mGridRecyclerView.setAdapter(mGridAdapter);
        int spacingInPixels = mRootFragment.getResources().getDimensionPixelSize(R.dimen.x15);
        mGridRecyclerView.setLayoutManager(xGridLayoutManager);
        mGridRecyclerView.addItemDecoration(new SpacesItemDecoration(false, spacingInPixels));
        mGridRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                reFreshDataByDirection(DIRECTION_DOWN);
            }

            @Override
            public void onLoadMore() {
                reFreshDataByDirection(DIRECTION_UP);
            }
        });
        mGridRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (xGridLayoutManager.findFirstVisibleItemPosition() == 0
                        && newState == SCROLL_STATE_IDLE
                        && toolbarDirection == DIRECTION_DOWN) {
//                    mGridRecyclerView.setRefresh(true);
                }
                if (xGridLayoutManager.findFirstVisibleItemPosition() > 3) {
                    if (newState == 0) {
                        mReturnTopImageView.setVisibility(VISIBLE);
                        if (returnTopAnimation.hasEnded()) {
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


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.index_iv_type:
            case R.id.index_tv_type:
                showTypePopupView();
                break;
            case R.id.index_iv_status:
            case R.id.index_tv_status:
                showStatusPopupView();
                break;
            case R.id.index_return_top:
                returnTop();
                break;
            case R.id.index_iv_menu_list_reverse:
            case R.id.index_iv_menu_list:
                ((MainActivity) mRootFragment.getActivity()).getMenuDrawer().openMenu();
                break;
            case R.id.index_iv_search:
            case R.id.index_iv_search_reverse:
                Intent intent = new Intent(mRootFragment.getActivity(), SearchDeviceActivity.class);
                intent.putExtra(EXTRA_FRAGMENT_INDEX, 1);
//                int size = mDataList.size();
//                intent.putExtra("", value);
//                Bundle bundle = new Bundle();
                startACForResult(intent, REQUEST_CODE_SEARCH_DEVICE);
                break;
            case R.id.index_iv_switch:
                mPrestener.switchIndexGridOrList(switchType);
                break;
            case R.id.index_head_alarm_num:
            case R.id.index_head_alarm_num_title:
                break;
            case R.id.index_head_inactive_num:
            case R.id.index_head_inactive_num_title:
                mAppBarLayout.setExpanded(true, true);
                break;
            case R.id.index_head_lost_num:
            case R.id.index_head_lost_num_title:
                mAppBarLayout.setExpanded(true, true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        mPrestener.clickItem(position);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {//张开
            toolbarDirection = DIRECTION_DOWN;
            mToolbar1.setVisibility(View.VISIBLE);
            mToolbar2.setVisibility(View.GONE);
            mToolbar1.setAlpha(1.0f);
        } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {//收缩
            toolbarDirection = DIRECTION_UP;
            mToolbar1.setVisibility(View.GONE);
            mToolbar2.setVisibility(View.VISIBLE);
            mToolbar2.setAlpha(1.0f);

        }
        if (toolbarDirection == DIRECTION_DOWN) {
            if (Math.abs(verticalOffset) > 0) {
                float bar_alpha = Math.abs(verticalOffset) / (float) appBarLayout.getTotalScrollRange();
                mToolbar2.setAlpha(bar_alpha);
                mToolbar2.setVisibility(VISIBLE);
                mToolbar1.setVisibility(View.GONE);
            }
        } else {
            if (Math.abs(verticalOffset) < appBarLayout.getTotalScrollRange()) {
                float bar_alpha = Math.abs(verticalOffset) / (float) appBarLayout.getTotalScrollRange();
                mToolbar2.setAlpha(bar_alpha);
            }
        }
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
        Toast.makeText(mRootFragment.getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastShort(int resId) {
        Toast.makeText(mRootFragment.getActivity(), resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {
    }

    @Override
    public void toastLong(int resId) {
    }

    @Override
    public void refreshTop(boolean isFirstInit, int alarmCount, int lostCount, int inactiveCount) {
        mHeadAlarmNumTextView.setText(String.valueOf(alarmCount));
        mHeadLostNumTextView.setText(String.valueOf(lostCount));
        mHeadInactiveNumTextView.setText(String.valueOf(inactiveCount));
        if (alarmCount > 0) {
            if (isFirstInit) {
                mPrestener.playSound();
            }
            alarmLayout.setVisibility(View.VISIBLE);
            mHeadAlarmNumTextView.setVisibility(VISIBLE);
            mHeadLostNumTextView.setVisibility(VISIBLE);
            mHeadInactiveNumTextView.setVisibility(VISIBLE);
            mHeadLostTitleTextView.setVisibility(VISIBLE);
            mHeadInactiveTitleTextView.setVisibility(VISIBLE);
            mHeadAlarmTitleTextView.setText(R.string.today_alarm);
            mHeadAlarmNumTextView.setTextSize(mRootFragment.getResources().getDimensionPixelSize(R.dimen.x50));
            mHeadAlarmTitleTextView.setTextSize(mRootFragment.getResources().getDimensionPixelSize(R.dimen.x20));
            mHeadLayout.setBackgroundColor(mRootFragment.getResources().getColor(R.color.sensoro_alarm));
            mToolbar1.setBackgroundColor(mRootFragment.getResources().getColor(R.color.sensoro_alarm));
            collapsingToolbarLayout.setContentScrimColor(mRootFragment.getResources().getColor(R.color.sensoro_alarm));
            StatusBarCompat.setStatusBarColor(mRootFragment.getActivity(), mRootFragment.getResources().getColor(R
                    .color.sensoro_alarm));
        } else {
            alarmLayout.setVisibility(View.GONE);
            mHeadAlarmTitleTextView.setTextSize(mRootFragment.getResources().getDimensionPixelSize(R.dimen.x50));
            mHeadAlarmNumTextView.setTextSize(mRootFragment.getResources().getDimensionPixelSize(R.dimen.x20));
            mHeadAlarmNumTextView.setVisibility(View.INVISIBLE);
            mHeadLostNumTextView.setVisibility(View.INVISIBLE);
            mHeadInactiveNumTextView.setVisibility(View.INVISIBLE);
            mHeadLostTitleTextView.setVisibility(View.INVISIBLE);
            mHeadInactiveTitleTextView.setVisibility(View.INVISIBLE);
            mHeadAlarmTitleTextView.setText(R.string.tips_no_alarm);
            mHeadLayout.setBackgroundColor(mRootFragment.getResources().getColor(R.color.sensoro_normal));
            mToolbar1.setBackgroundColor(mRootFragment.getResources().getColor(R.color.sensoro_normal));
            collapsingToolbarLayout.setContentScrimColor(mRootFragment.getResources().getColor(R.color.sensoro_normal));
            StatusBarCompat.setStatusBarColor(mRootFragment.getActivity(), mRootFragment.getResources().getColor(R
                    .color.sensoro_normal));
        }
    }

    @Override
    public void switchToTypeList() {
        switchType = TYPE_LIST;
        mReturnTopImageView.setVisibility(View.GONE);
        mSwitchImageView.setImageResource(R.mipmap.ic_switch_grid);
        showListLayout();
    }

    @Override
    public void switchToTypeGrid() {
        switchType = TYPE_GRID;
        mReturnTopImageView.setVisibility(View.GONE);
        mSwitchImageView.setImageResource(R.mipmap.ic_switch_list);
        showGridLayout();
    }

    @Override
    public void returnTop() {
        if (switchType == TYPE_LIST) {
            mListRecyclerView.smoothScrollToPosition(0);
        } else {
            mGridRecyclerView.smoothScrollToPosition(0);
        }
        mReturnTopImageView.setVisibility(View.GONE);
    }

    @Override
    public void showListLayout() {
        final Animation inAnimation = AnimationUtils.loadAnimation(mRootFragment.getActivity(), R.anim.layout_in_anim);
        inAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mListLayout.setVisibility(View.VISIBLE);
                mGridLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mListLayout.setAnimation(inAnimation);
        mListLayout.startAnimation(inAnimation);
    }

    @Override
    public void showGridLayout() {
        Animation mInAnimation = AnimationUtils.loadAnimation(mRootFragment.getActivity(), R.anim.layout_in_anim);
        mInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mListLayout.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mGridLayout.setVisibility(View.VISIBLE);
        mGridLayout.setAnimation(mInAnimation);
        mGridLayout.startAnimation(mInAnimation);
    }

    @Override
    public void refreshData(List<DeviceInfo> dataList) {
        if (mRootFragment.isVisible() && mRootFragment.isResumed()) {
            Collections.sort(dataList);
            if (switchType == TYPE_LIST) {
                mListAdapter.setData(dataList);
                mListAdapter.notifyDataSetChanged();
                mListRecyclerView.refreshComplete();
            } else {
                mGridAdapter.setData(dataList);
                mGridAdapter.notifyDataSetChanged();
                mGridRecyclerView.refreshComplete();
            }
            if (dataList.size() < 5) {
                mReturnTopImageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void refreshCityInfo() {
        Character character = (Character) mRootFragment.getArguments().getSerializable(INPUT);
        if (character != null) {
            if (character.getShortName() != null) {
                mTitleTextView.setText(character.isApply() ? character.getShortName() : mRootFragment.getString(R
                        .string.city_name));
            }
        }
    }

    @Override
    public void showTypePopupView() {
        if (mTypePopupView.getVisibility() == VISIBLE) {
            mTypeTextView.setTextColor(mRootFragment.getResources().getColor(R.color.c_626262));
            mTypeImageView.setColorFilter(mRootFragment.getResources().getColor(R.color.c_626262));
            mTypeImageView.setRotation(0);
            mTypePopupView.dismiss();
        } else {
            mStatusTextView.setTextColor(mRootFragment.getResources().getColor(R.color.c_626262));
            mStatusImageView.setColorFilter(mRootFragment.getResources().getColor(R.color.c_626262));
            mStatusImageView.setRotation(0);
            mStatusPopupView.dismiss();

            mTypeTextView.setTextColor(mRootFragment.getResources().getColor(R.color.popup_selected_text_color));
            mTypeImageView.setColorFilter(mRootFragment.getResources().getColor(R.color.popup_selected_text_color));
            mTypeImageView.setRotation(180);
            mTypeShadowLayout.setVisibility(VISIBLE);
            mTypeShadowLayout.setAlpha(0.5f);
            mTypeShadowLayout.setBackgroundColor(mRootFragment.getResources().getColor(R.color.c_626262));
            mTypePopupView.show(mTypeShadowLayout, new SensoroPopupTypeView.OnTypePopupItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    filterByTypeWithRequest(position);
                }
            });
        }

    }

    @Override
    public void showStatusPopupView() {
        if (mStatusPopupView.getVisibility() == VISIBLE) {
            mStatusTextView.setTextColor(mRootFragment.getResources().getColor(R.color.c_626262));
            mStatusImageView.setColorFilter(mRootFragment.getResources().getColor(R.color.c_626262));
            mStatusImageView.setRotation(0);
            mStatusPopupView.dismiss();
        } else {
            mTypeTextView.setTextColor(mRootFragment.getResources().getColor(R.color.c_626262));
            mTypeImageView.setColorFilter(mRootFragment.getResources().getColor(R.color.c_626262));
            mTypeImageView.setRotation(0);
            mTypePopupView.dismiss();

            mStatusTextView.setTextColor(mRootFragment.getResources().getColor(R.color.popup_selected_text_color));
            mStatusImageView.setColorFilter(mRootFragment.getResources().getColor(R.color.popup_selected_text_color));
            mStatusImageView.setRotation(180);
            mStatusShadowLayout.setVisibility(VISIBLE);
            mStatusShadowLayout.setAlpha(0.5f);
            mStatusShadowLayout.setBackgroundColor(mRootFragment.getResources().getColor(R.color.c_626262));
            mStatusPopupView.show(mStatusShadowLayout, new SensoroPopupStatusView.OnStatusPopupItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    filterByStatusWithRequest(position);
                }
            });
        }

    }

    @Override
    public void filterByStatusWithRequest(int position) {
        mPrestener.setStatusSelectedIndex(position);
        mStatusTextView.setTextColor(mRootFragment.getResources().getColor(R.color.c_626262));
        mStatusImageView.setColorFilter(mRootFragment.getResources().getColor(R.color.c_626262));
        mStatusImageView.setRotation(0);
        String statusText = INDEX_STATUS_ARRAY[position];
        mStatusTextView.setText(statusText);
        reFreshDataByDirection(DIRECTION_DOWN);
    }

    @Override
    public void filterByTypeWithRequest(int position) {
        mPrestener.setTypeSelectedIndex(position);
        mTypeTextView.setTextColor(mRootFragment.getResources().getColor(R.color.c_626262));
        mTypeImageView.setColorFilter(mRootFragment.getResources().getColor(R.color.c_626262));
        mTypeImageView.setRotation(0);
        String typeText = INDEX_TYPE_ARRAY[position];
        mTypeTextView.setText(typeText);
        reFreshDataByDirection(DIRECTION_DOWN);
    }


    @Override
    public void handleSocketInfo(String data) {
        if (!TextUtils.isEmpty(data) && !mRootFragment.isHidden()) {
            mPrestener.organizeJsonData(data);
        }
    }

    @Override
    public void reFreshDataByDirection(int direction) {
        mPrestener.requestWithDirection(direction);
    }

    @Override
    public void refreshBySearch(DeviceInfoListRsp infoRspData) {
        mPrestener.refreshWithSearch(infoRspData);
    }

    @Override
    public void requestTopData(boolean isFirstInit) {
        if (mRootFragment.isVisible() && mRootFragment.isResumed()) {
            mPrestener.requestTopData(isFirstInit);
        }
    }

    @Override
    public void recycleViewRefreshComplete() {
        mListRecyclerView.refreshComplete();
        mGridRecyclerView.refreshComplete();
    }

    @Override
    public void startAC(Intent intent) {
        mRootFragment.getActivity().startActivity(intent);
    }

    @Override
    public void finishAc() {

    }

    @Override
    public void startACForResult(Intent intent, int requestCode) {
        mRootFragment.startActivityForResult(intent, requestCode);
    }

    @Override
    public void setIntentResult(int requestCode) {

    }

    @Override
    public void setIntentResult(int requestCode, Intent data) {

    }
}
