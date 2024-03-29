package com.sensoro.smartcity.imainviews;

import android.widget.ImageView;

import com.sensoro.common.iwidget.IActivityIntent;
import com.sensoro.common.iwidget.IProgressDialog;
import com.sensoro.common.iwidget.IToast;
import com.sensoro.common.server.bean.DeviceCameraFacePic;
import com.shuyu.gsyvideoplayer.video.CityStandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

public interface ICameraDetailActivityView extends IProgressDialog, IToast, IActivityIntent {

    void updateCameraList(List<DeviceCameraFacePic> data);

    void startPlayLogic(String url1, String title);

    DeviceCameraFacePic getItemData(int position);

    void setDateTime(String time);

    List<DeviceCameraFacePic> getRvListData();

    void onPullRefreshComplete();

    void setLiveState(boolean isLiveStream);

//    void setImage(Drawable resource);

    void clearClickPosition();

    boolean isSelectedDateLayoutVisible();

    void setSelectedDateLayoutVisible(boolean isVisible);

    void setSelectedDateSearchText(String s);

    //播放失败
    void playError(int pos);

    void offlineType(String mCameraName);

    void autoRefresh();

    ImageView getImageView();

    void doPlayLive(ArrayList<String> urlList, String cameraName, boolean b);

    void setGsyVideoNoVideo();

    void setVerOrientationUtilEnable(boolean enable);

    CityStandardGSYVideoPlayer getPlayView();

    void backFromWindowFull();

    void onVideoResume(boolean isLive);

    void loadCoverImage(String url);

    int getCurrentClickPosition();
}
