package com.sensoro.common.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.baidu.mobstat.StatService;
import com.sensoro.common.iwidget.IOnFragmentStart;
import com.sensoro.common.utils.LogUtils;
import com.sensoro.common.widgets.SensoroToast;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by JL-DDONG on 2017/7/12 0012.
 */

public abstract class BaseFragment<V, P extends BasePresenter<V>> extends Fragment implements IOnFragmentStart {
    protected P mPresenter;
    protected View mRootView;
    protected Unbinder unbinder;
    protected BaseFragment mRootFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        mPresenter = createPresenter();
        mPresenter.attachView((V) this);
        V view = mPresenter.getView();
        if (view instanceof BaseFragment) {
            mRootFragment = (BaseFragment) view;
        } else {
            try {
                LogUtils.loge(this, "当前View转换异常！");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            mRootFragment = this;
        }
        if (mRootView == null) {
            mRootView = inflater.inflate(initRootViewId(), container, false);
        } else {
            ViewGroup viewGroup = (ViewGroup) mRootView.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(mRootView);
            }
        }
        unbinder = ButterKnife.bind(mPresenter.getView(), mRootView);

        return mRootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 页面埋点
        StatService.onPageStart(getActivity(), this.getClass().getSimpleName());
    }


    /**
     * fragment onStart
     */
    @Override
    public void onStart() {
        try {
            if (mPresenter != null && getUserVisibleHint()) {
                onFragmentStart();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }

    /**
     * fragment onStop
     */
    @Override
    public void onStop() {
        try {
            if (mPresenter != null && getUserVisibleHint()) {
                onFragmentStop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    /**
     * fragment 选中
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        try {
            if (mPresenter != null) {
                if (isVisibleToUser) {
                    onFragmentStart();
                } else {
                    onFragmentStop();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }


    @Override
    public void onPause() {
        super.onPause();
        // 页面埋点
        StatService.onPageEnd(getActivity(), this.getClass().getSimpleName());
    }

    protected abstract void initData(Context activity);


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            LogUtils.logd("onActivityCreated");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        if (mRootFragment != null) {
            initData(mRootFragment.getActivity());
        }

    }

    protected abstract int initRootViewId();

    protected abstract P createPresenter();


    @Override
    public void onDestroyView() {
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        if (mPresenter != null) {
            mPresenter.onDestroy();
            mPresenter.detachView();
            mPresenter = null;
        }
        if (mRootFragment != null) {
            mRootFragment = null;
        }
        SensoroToast.getInstance().cancelToast();
        super.onDestroyView();
    }

}
