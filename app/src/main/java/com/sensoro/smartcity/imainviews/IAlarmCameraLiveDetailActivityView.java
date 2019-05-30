package com.sensoro.smartcity.imainviews;

import android.graphics.drawable.Drawable;

import com.sensoro.common.iwidget.IProgressDialog;
import com.sensoro.common.iwidget.IToast;
import com.sensoro.common.server.response.AlarmCameraLiveRsp;
import com.shuyu.gsyvideoplayer.video.CityStandardGSYVideoPlayer;

import java.util.ArrayList;

public interface IAlarmCameraLiveDetailActivityView extends IToast, IProgressDialog {
    void doPlayLive(final String url);

    void offlineType(String url, String sn);

    void updateData(ArrayList<AlarmCameraLiveRsp.DataBean> mList);

    void onPullRefreshComplete();

    void setImage(Drawable bitmapDrawable);

    void backFromWindowFull();

    void setVerOrientationUtil(boolean enable);


    CityStandardGSYVideoPlayer getPlayView();

    void onVideoPause();

    void onVideoResume();
}
