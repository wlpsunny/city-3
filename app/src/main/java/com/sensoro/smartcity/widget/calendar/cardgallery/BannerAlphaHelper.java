package com.sensoro.smartcity.widget.calendar.cardgallery;

import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.utils.LogUtils;

import java.util.Objects;

/**
 * Created by jameson on 8/30/16.
 * changed by 二精-霁雪清虹 on 2017/11/19
 */
public class BannerAlphaHelper implements ViewTreeObserver.OnGlobalLayoutListener {
    private BannerRecyclerView mRecyclerView;
    private Context mContext;

    private float mAlpha = 0f; // 两边视图scale
    private int mPagePadding = BannerAdapterHelper.sPagePadding; // 卡片的padding, 卡片间的距离等于2倍的mPagePadding
    private int mShowLeftCardWidth = BannerAdapterHelper.sShowLeftCardWidth;   // 左边卡片显示大小

    private int mCardWidth; // 卡片宽度
    private int mOnePageWidth; // 滑动一页的距离
    private int mCardGalleryWidth;

    private int mFirstItemPos;
    private int mCurrentItemOffset;

    private CardLinearSnapHelper mLinearSnapHelper = new CardLinearSnapHelper();
    private int mLastPos;

    public void attachToRecyclerView(final BannerRecyclerView mRecyclerView) {
        if (mRecyclerView == null) {
            return;
        }
        this.mRecyclerView = mRecyclerView;
        mContext = mRecyclerView.getContext();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        int currentItem = getCurrentItem();
                        try {
                            LogUtils.loge(this, "onScrollStateChanged -->> currentItem = " + currentItem);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        mLinearSnapHelper.mNoNeedToScroll = currentItem == 0 || currentItem == mRecyclerView.getAdapter().getItemCount() - 1;
                        if (mLinearSnapHelper.finalSnapDistance[0] == 0
                                && mLinearSnapHelper.finalSnapDistance[1] == 0) {
                            mCurrentItemOffset = 0;
                            mLastPos = currentItem;
                            //认为是一次滑动停止 这里可以写滑动停止回调
//                        mRecyclerView.dispatchOnPageSelected(mLastPos);
                            //Log.e("TAG", "滑动停止后最终位置为" + getCurrentItem());
                        }
                        mRecyclerView.dispatchOnPageSelected(currentItem);
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        break;
                    default:
                        mLinearSnapHelper.mNoNeedToScroll = false;
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // dx>0则表示右滑, dx<0表示左滑, dy<0表示上滑, dy>0表示下滑
                mCurrentItemOffset += dx;
                onScrolledChangedCallback();
            }
        });
        initWidthData();
        mLinearSnapHelper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * 初始化卡片宽度
     */
    public void initWidthData() {
        //防止出现首页缩放不显示
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    public void setCurrentItem(int item) {
        setCurrentItem(item, false);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        if (mRecyclerView == null) {
            return;
        }
        if (smoothScroll) {
            mRecyclerView.smoothScrollToPosition(item);
        } else {
            scrollToPosition(item);
        }
    }

    public void scrollToPosition(int pos) {
        if (mRecyclerView == null) {
            return;
        }
        //mRecyclerView.getLayoutManager()).scrollToPositionWithOffset 方法不会回调  RecyclerView.OnScrollListener 的onScrollStateChanged方法,是瞬间跳到指定位置
        //mRecyclerView.smoothScrollToPosition 方法会回调  RecyclerView.OnScrollListener 的onScrollStateChanged方法 并且是自动居中，有滚动过程的滑动到指定位置
        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).
                scrollToPositionWithOffset(pos,
                        AppUtils.dp2px(mContext, mPagePadding + mShowLeftCardWidth));
        mCurrentItemOffset = 0;
        mLastPos = pos;
        //认为是一次滑动停止 这里可以写滑动停止回调
        mRecyclerView.dispatchOnPageSelected(mLastPos);
        //onScrolledChangedCallback();
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                onScrolledChangedCallback();
            }
        });
    }

    public void scrollToPositionAlpha(final int pos) {
        if (mRecyclerView == null) {
            return;
        }
        mRecyclerView.scrollToPosition(pos);
        mCurrentItemOffset = 0;
        mLastPos = pos;
//        mRecyclerView.post(new Runnable() {
//            @Override
//            public void run() {
//                View view = mRecyclerView.getLayoutManager().findViewByPosition(pos);
//                view.setAlpha(0);
//                view.animate()
//                        .alpha(1.f)                                //设置最终效果为完全不透明
//                        .setDuration(1500)
//                        .setListener(new AnimatorListenerAdapter() {
//                            @Override
//                            public void onAnimationEnd(Animator animation) {
//                            }
//                        })
//                        .start();
//            }
//        });
    }

    public void setFirstItemPos(int firstItemPos) {
        this.mFirstItemPos = firstItemPos;
    }


    /**
     * RecyclerView位移事件监听, view大小随位移事件变化
     */
    private void onScrolledChangedCallback() {
        if (mOnePageWidth == 0) {
            return;
        }
        int currentItemPos = getCurrentItem();
        int offset = mCurrentItemOffset - (currentItemPos - mLastPos) * mOnePageWidth;
        float percent = (float) Math.max(Math.abs(offset) * 1.0 / mOnePageWidth, 0.0001);

        //Log.e("TAG",String.format("offset=%s, percent=%s", offset, percent));
        View leftView = null;
        View currentView = null;
        View rightView = null;
        if (currentItemPos > 0) {
            leftView = mRecyclerView.getLayoutManager().findViewByPosition(currentItemPos - 1);
        }
        currentView = mRecyclerView.getLayoutManager().findViewByPosition(currentItemPos);
        if (currentItemPos < mRecyclerView.getAdapter().getItemCount() - 1) {
            rightView = mRecyclerView.getLayoutManager().findViewByPosition(currentItemPos + 1);
        }

        if (leftView != null) {
            if (onBannerHelperListener != null) {
                onBannerHelperListener.onScrolledOther(percent);
            }
            // y = (1 - mAlpha)x + mAlpha
//            leftView.setAlpha((1 - mAlpha) * percent + mAlpha);
//            leftView.setScaleY((1 - mAlpha) * percent + mAlpha);
        }
        if (currentView != null) {
            // y = (mAlpha - 1)x + 1
//            currentView.setAlpha((mAlpha - 1) * percent + 1);
//            currentView.setScaleY((mAlpha - 1) * percent + 1);
            if (onBannerHelperListener != null) {
                onBannerHelperListener.onScrolledCurrent(percent / 0.5f);
            }
        }
        if (rightView != null) {
            if (onBannerHelperListener != null) {
                onBannerHelperListener.onScrolledOther(percent);
            }
            // y = (1 - mAlpha)x + mAlpha
//            rightView.setAlpha((1 - mAlpha) * percent + mAlpha);
//            rightView.setScaleY((1 - mAlpha) * percent + mAlpha);
        }
    }

    public int getCurrentItem() {
        try {
//            return ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastVisibleItemPosition();
            return mRecyclerView.getLayoutManager().getPosition(Objects.requireNonNull(mLinearSnapHelper.findSnapView(mRecyclerView.getLayoutManager())));
        } catch (Exception e) {
            return 0;
        }

    }

    public interface OnBannerHelperListener {
        void onScrolledCurrent(float currentPercent);

        void onScrolledOther(float otherPercent);
    }

    private OnBannerHelperListener onBannerHelperListener;

    public void setOnBannerHelperListener(OnBannerHelperListener listener) {
        onBannerHelperListener = listener;
    }

    public void setAlpha(float scale) {
        mAlpha = scale;
    }

    public void setPagePadding(int pagePadding) {
        mPagePadding = pagePadding;
    }

    public void setShowLeftCardWidth(int showLeftCardWidth) {
        mShowLeftCardWidth = showLeftCardWidth;
    }

    @Override
    public void onGlobalLayout() {
        mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        mCardGalleryWidth = mRecyclerView.getWidth();
        mCardWidth = mCardGalleryWidth - AppUtils.dp2px(mContext, 2 * (mPagePadding + mShowLeftCardWidth));
        mOnePageWidth = mCardWidth;
        scrollToPosition(mFirstItemPos);
    }


}
