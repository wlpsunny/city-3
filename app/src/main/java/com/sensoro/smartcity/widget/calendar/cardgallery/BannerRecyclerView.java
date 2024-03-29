package com.sensoro.smartcity.widget.calendar.cardgallery;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.sensoro.common.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 控制fling速度的RecyclerView
 * <p>
 * Created by jameson on 9/1/16.
 * changed by 二精-霁雪清虹 on 2017/11/19
 */
public class BannerRecyclerView extends RecyclerView {

    private static final float FLING_SCALE_DOWN_FACTOR = 0.5f; // 减速因子
    private static final int FLING_MAX_VELOCITY = (int) (2.7f * 1000f); // 最大顺时滑动速度
    private static boolean mEnableLimitVelocity = true; // 最大顺时滑动速度
    private List<OnPageChangeListener> mOnPageChangeListeners;
    private OnPageChangeListener mOnPageChangeListener;
    private int currentPosition = 0;
    private float lastY;
    private float lastX;
    private boolean isContent = false;

    public BannerRecyclerView(Context context) {
        super(context);
    }

    public BannerRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BannerRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static boolean ismEnableLimitVelocity() {
        return mEnableLimitVelocity;
    }

    public static void setmEnableLimitVelocity(boolean mEnableLimitVelocity) {
        BannerRecyclerView.mEnableLimitVelocity = mEnableLimitVelocity;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        if (mEnableLimitVelocity) {
            velocityX = solveVelocity(velocityX);
            velocityY = solveVelocity(velocityY);
        }
        return super.fling(velocityX, velocityY);
    }

    private int solveVelocity(int velocity) {
        if (velocity > 0) {
            return Math.min(velocity, FLING_MAX_VELOCITY);
        } else {
            return Math.max(velocity, -FLING_MAX_VELOCITY);
        }
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    public void addOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = new ArrayList<>();
        }
        mOnPageChangeListeners.add(listener);
    }

    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.remove(listener);
        }
    }

    public void clearOnPageChangeListeners() {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.clear();
        }
    }


    public interface OnPageChangeListener {
        void onPageSelected(int position);
    }

    public void dispatchOnPageSelected(int position) {
        if (currentPosition == position) return;
        try {
            LogUtils.loge(this, "dispatchOnPageSelected -->> currentItem = " + position);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        currentPosition = position;
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(position);
        }
        if (mOnPageChangeListeners != null) {
            for (int i = 0, z = mOnPageChangeListeners.size(); i < z; i++) {
                OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                if (listener != null) {
                    listener.onPageSelected(position);
                }
            }
        }
    }


    public void setContent(boolean content) {
        isContent = content;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        float x = ev.getRawX();
        float y = ev.getRawY();
        float dealtX = 0;
        float dealtY = 0;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                dealtX = 0;
                dealtY = 0;
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                dealtX += Math.abs(x - lastX);
                dealtY += Math.abs(y - lastY);
                if (dealtX >= dealtY) {
//                    return true;
//                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
//        ViewParent parent = this;
//        while (!((parent = parent.getParent()) instanceof CoordinatorLayout)) ;
//        parent.requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if(isContent){
            return true;
        }else{
            return super.onTouchEvent(e);
        }
//        switch (e.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//            case MotionEvent.ACTION_MOVE:
//                return false;
//            case MotionEvent.ACTION_UP:
//
//                return true;
//        }
//        return super.onTouchEvent(e);
    }
}
