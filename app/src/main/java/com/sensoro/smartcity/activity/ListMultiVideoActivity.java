package com.sensoro.smartcity.activity;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.gyf.immersionbar.ImmersionBar;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.model.EventData;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.ListMultiNormalAdapter;
import com.sensoro.smartcity.imainviews.IMutilCameraView;
import com.sensoro.smartcity.presenter.MutilCamerPresenter;
import com.shuyu.gsyvideoplayer.utils.CustomManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 多个同时播放的demo
 */
public class ListMultiVideoActivity extends BaseActivity<IMutilCameraView, MutilCamerPresenter> {

    @BindView(R.id.video_list)
    ListView videoList;
    @BindView(R.id.view_top_ac_alarm_camera_video_detail)
    View viewTopAcAlarmCameraVideoDetail;
    ListMultiNormalAdapter listMultiNormalAdapter;

    private boolean isPause;

    /**
     * 网络改变状态
     *
     * @param eventData
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData eventData) {
        int code = eventData.code;
        if (code == Constants.NetworkInfo) {
            int data = (int) eventData.data;

            switch (data) {

                case ConnectivityManager.TYPE_WIFI:

                    listMultiNormalAdapter.setState(-1);

                    break;

                case ConnectivityManager.TYPE_MOBILE:
                    listMultiNormalAdapter.setState(2);
                    CustomManager.onPauseAll();

                    break;

                default:

                    listMultiNormalAdapter.setState(1);
                    CustomManager.onPauseAll();
                    break;


            }
        }
    }

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {

        setContentView(R.layout.activity_list_video);
        ButterKnife.bind(this);
        initViewHeight();
        EventBus.getDefault().register(this);

        listMultiNormalAdapter = new ListMultiNormalAdapter(this);
        videoList.setAdapter(listMultiNormalAdapter);

        videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                //大于0说明有播放
//                if (CustomManager.instance().size() >= 0) {
//                    Map<String, CustomManager> map = CustomManager.instance();
//                    List<String> removeKey = new ArrayList<>();
//                    for (Map.Entry<String, CustomManager> customManagerEntry : map.entrySet()) {
//                        CustomManager customManager = customManagerEntry.getValue();
//                        //当前播放的位置
//                        int position = customManager.getPlayPosition();
//                        //对应的播放列表TAG
//                        if (customManager.getPlayTag().equals(ListMultiNormalAdapter.TAG)
//                                && (position < firstVisibleItem || position > lastVisibleItem)) {
//                            CustomManager.releaseAllVideos(customManagerEntry.getKey());
//                            removeKey.add(customManagerEntry.getKey());
//                        }
//                    }
//                    if (removeKey.size() > 0) {
//                        for (String key : removeKey) {
//                            map.remove(key);
//                        }
//                        listMultiNormalAdapter.notifyDataSetChanged();
//                    }
//                }
            }

        });

    }

    private void initViewHeight() {
        int resourceId = this.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            int result = this.getResources().getDimensionPixelSize(resourceId);
            ViewGroup.LayoutParams lp = viewTopAcAlarmCameraVideoDetail.getLayoutParams();
            lp.height = result;
            viewTopAcAlarmCameraVideoDetail.setLayoutParams(lp);
        }
    }


    @Override
    public boolean setMyCurrentStatusBar() {
        immersionBar = ImmersionBar.with(mActivity);
        immersionBar.statusBarDarkFont(true).statusBarColor(R.color.white).init();
        return true;
    }

    @Override
    public boolean setMyCurrentActivityTheme() {
        setTheme(R.style.Theme_AppCompat_Translucent);
        return true;
    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
////        listMultiNormalAdapter.backFromWindowFull();
//
//    }

    @Override
    public void onBackPressed() {
        if (CustomManager.backFromWindowFull(this, listMultiNormalAdapter.getFullKey())) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        CustomManager.onPauseAll();
        isPause = true;
    }

    @Override
    protected MutilCamerPresenter createPresenter() {
        return new MutilCamerPresenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CustomManager.onResumeAll();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

        CustomManager.clearAllVideo();
    }


}
