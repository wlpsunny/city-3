package com.shuyu.gsyvideoplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.view.OrientationEventListener;

import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;

/**
 * 处理屏幕旋转的的逻辑
 * Created by shuyu on 2016/11/11.
 */

public class OrientationUtils {

    private Activity activity;
    private GSYBaseVideoPlayer gsyVideoPlayer;
    private OrientationEventListener orientationEventListener;

    private int screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    private int mIsLand;

    private boolean mClick = false;
    private boolean mClickLand = false;
    private boolean mClickPort;
    private boolean mEnable = true;
    //是否跟随系统
    private boolean mRotateWithSystem = true;

    /**
     * @param activity
     * @param gsyVideoPlayer
     */
    public OrientationUtils(Activity activity, GSYBaseVideoPlayer gsyVideoPlayer) {
        this.activity = activity;
        this.gsyVideoPlayer = gsyVideoPlayer;
        init();
    }

    public static boolean isOpenSensor(Context context) {
        boolean isOpen = false;
        if (getSensorState(context) == 1) {
            isOpen = true;
        } else if (getSensorState(context) == 0) {
            isOpen = false;
        }
        return isOpen;
    }

    public static int getSensorState(Context context) {
        int sensorState = 0;
        try {
            sensorState = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            return sensorState;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return sensorState;
    }

    private void init() {
        orientationEventListener = new OrientationEventListener(activity.getApplicationContext()) {
            @Override
            public void onOrientationChanged(int rotation) {

//                if (!isOpenSensor(activity)) {
//                    return;
//                }
//

                boolean autoRotateOn = (Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
                if (!autoRotateOn && mRotateWithSystem) {
                    return;
                }
                if (gsyVideoPlayer != null && gsyVideoPlayer.isVerticalFullByVideoSize()) {
                    return;
                }

                if (rotation == -1) {
                    return;
                }
                // 设置竖屏
                if (((rotation > -1) && (rotation <= 60)) || (rotation >= 300)) {
                    if (mClick) {
                        if (mIsLand > 0 && !mClickLand) {
                            return;
                        } else {
                            mClickPort = true;
                            mClick = false;
                            mIsLand = 0;
                        }
                    } else {
                        if (mIsLand > 0) {
                            screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            if (gsyVideoPlayer.getFullscreenButton() != null) {
                                if (gsyVideoPlayer.isIfCurrentIsFullscreen()) {
                                    gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
                                } else {
                                    gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getEnlargeImageRes());
                                }
                            }
                            mIsLand = 0;
                            mClick = false;
                        }
                    }
                }
                // 设置横屏
                else if ((rotation > 180/*) && (rotation <= 270)*/)) {
                    if (mClick) {
                        if (!(mIsLand == 1) && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClick = false;
                            mIsLand = 1;
                        }
                    } else {
                        if (!(mIsLand == 1)) {
                            screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            if (gsyVideoPlayer.getFullscreenButton() != null) {
                                gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
                            }
                            mIsLand = 1;
                            mClick = false;
                        }
                    }
                }
                // 设置反向横屏
                else {
                    if (mClick) {
                        if (!(mIsLand == 2) && !mClickPort) {
                            return;
                        } else {
                            mClickLand = true;
                            mClick = false;
                            mIsLand = 2;
                        }
                    } else if (!(mIsLand == 2)) {
                        screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        if (gsyVideoPlayer.getFullscreenButton() != null) {
                            gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
                        }
                        mIsLand = 2;
                        mClick = false;
                    }
                }
            }
        };
        orientationEventListener.enable();
    }

    /**
     * 点击切换的逻辑，比如竖屏的时候点击了就是切换到横屏不会受屏幕的影响
     */
    public void resolveByClick() {
        if (mIsLand == 0 && gsyVideoPlayer != null && gsyVideoPlayer.isVerticalFullByVideoSize()) {
            return;
        }
        mClick = true;
        if (mIsLand == 0) {
            screenType = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if (gsyVideoPlayer.getFullscreenButton() != null) {
                gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
            }
            mIsLand = 1;
            mClickLand = false;
        } else {
            screenType = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (gsyVideoPlayer.getFullscreenButton() != null) {
                if (gsyVideoPlayer.isIfCurrentIsFullscreen()) {
                    gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getShrinkImageRes());
                } else {
                    gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getEnlargeImageRes());
                }
            }
            mIsLand = 0;
            mClickPort = false;
        }

    }

    /**
     * 列表返回的样式判断。因为立即旋转会导致界面跳动的问题
     */
    public int backToProtVideo() {
        if (mIsLand > 0) {
            mClick = true;
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (gsyVideoPlayer != null && gsyVideoPlayer.getFullscreenButton() != null)
                gsyVideoPlayer.getFullscreenButton().setImageResource(gsyVideoPlayer.getEnlargeImageRes());
            mIsLand = 0;
            mClickPort = false;
            return 500;
        }
        return 0;
    }


    public boolean isEnable() {
        return mEnable;
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
        if (mEnable) {
            orientationEventListener.enable();
        } else {
            orientationEventListener.disable();
        }
    }

    public void releaseListener() {
        if (orientationEventListener != null) {
            orientationEventListener.disable();
        }
    }

    public boolean isClick() {
        return mClick;
    }

    public void setClick(boolean Click) {
        this.mClick = mClick;
    }

    public boolean isClickLand() {
        return mClickLand;
    }

    public void setClickLand(boolean ClickLand) {
        this.mClickLand = ClickLand;
    }

    public int getIsLand() {
        return mIsLand;
    }

    public void setIsLand(int IsLand) {
        this.mIsLand = IsLand;
    }


    public boolean isClickPort() {
        return mClickPort;
    }

    public void setClickPort(boolean ClickPort) {
        this.mClickPort = ClickPort;
    }

    public int getScreenType() {
        return screenType;
    }

    public void setScreenType(int screenType) {
        this.screenType = screenType;
    }


    public boolean isRotateWithSystem() {
        return mRotateWithSystem;
    }

    /**
     * 是否更新系统旋转，false的话，系统禁止旋转也会跟着旋转
     *
     * @param rotateWithSystem 默认true
     */
    public void setRotateWithSystem(boolean rotateWithSystem) {
        this.mRotateWithSystem = rotateWithSystem;
    }
}
