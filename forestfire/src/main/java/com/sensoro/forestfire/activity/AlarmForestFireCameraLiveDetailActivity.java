package com.sensoro.forestfire.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.gyf.immersionbar.ImmersionBar;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.constant.ARouterConstants;
import com.sensoro.common.server.bean.ForestFireCameraDetailInfo;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.forestfire.R;
import com.sensoro.forestfire.R2;
import com.sensoro.forestfire.adapter.AlarmForestFireCameraLiveDetailAdapter;
import com.sensoro.forestfire.imainviews.IAlarmForestFireCameraLiveDetailActivityView;
import com.sensoro.forestfire.presenter.AlarmForestFireCameraLiveDetailActivityPresenter;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.CityStandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * @Author: jack
 * 时  间: 2019-09-17
 * 包  名: com.sensoro.forestfire.activity
 * 简  述: <功能简述:森林防火预警日志直播页面>
 */
@Route(path = ARouterConstants.ACTIVITY_FORESTFIRE_CAMERA_LIVE_DETAIL)
public class AlarmForestFireCameraLiveDetailActivity extends BaseActivity<IAlarmForestFireCameraLiveDetailActivityView, AlarmForestFireCameraLiveDetailActivityPresenter>
        implements IAlarmForestFireCameraLiveDetailActivityView {
    @BindView(R2.id.include_imv_title_imv_arrows_left)
    ImageView includeImvTitleImvArrowsLeft;
    @BindView(R2.id.include_imv_title_tv_title)
    TextView includeImvTitleTvTitle;
    @BindView(R2.id.include_imv_title_imv_subtitle)
    ImageView includeImvTitleImvSubtitle;
    @BindView(R2.id.gsy_player_ac_alarm_camera_live_detail)
    CityStandardGSYVideoPlayer gsyPlayerAcAlarmCameraLiveDetail;
    View icNoContent;
    @BindView(R2.id.rv_list_include)
    RecyclerView rvListInclude;
    @BindView(R2.id.refreshLayout_include)
    SmartRefreshLayout refreshLayoutInclude;
    @BindView(R2.id.return_top_include)
    ImageView returnTopInclude;
    @BindView(R2.id.view_top_ac_alarm_camera_live_detail)
    View viewTopAcAlarmCameraLiveDetail;
    private ProgressUtils mProgressUtils;
    private ImageView ivGsyCover;
    private GSYVideoOptionBuilder gsyVideoOption;
    private OrientationUtils orientationUtils;
    private boolean isPlay;
    private boolean isPause;
    private Animation returnTopAnimation;
    private AlarmForestFireCameraLiveDetailAdapter mListAdapter;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_alarm_forest_fire_camera_live_detail);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
    }

    private void initView() {

        icNoContent = LayoutInflater.from(this).inflate(R.layout.no_content, null);
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());
        includeImvTitleTvTitle.setText(mActivity.getString(R.string.deploy_camera_watch_live));
        includeImvTitleImvSubtitle.setVisibility(GONE);

        initSmartRefresh();

        returnTopAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.return_top_in_anim);
        returnTopInclude.setAnimation(returnTopAnimation);
        returnTopInclude.setVisibility(GONE);

        initRvList();

        initViewHeight();

        initGsyPlayer();

    }

    private void initViewHeight() {
        int resourceId = this.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            int result = this.getResources().getDimensionPixelSize(resourceId);
            ViewGroup.LayoutParams lp = viewTopAcAlarmCameraLiveDetail.getLayoutParams();
            lp.height = result;
            viewTopAcAlarmCameraLiveDetail.setLayoutParams(lp);
        }
    }

    private void initRvList() {
        mListAdapter = new AlarmForestFireCameraLiveDetailAdapter(mActivity);
        mListAdapter.setOnAlarmCameraLiveItemClickListener(new AlarmForestFireCameraLiveDetailAdapter.AlarmCameraLiveItemClickListener() {
            @Override
            public void OnAlarmCameraLiveItemClick(int position) {
                mPresenter.doItemClick(position);
                orientationUtils.setEnable(false);

            }
        });
        final LinearLayoutManager manager = new LinearLayoutManager(mActivity);
        manager.setOrientation(RecyclerView.VERTICAL);
//        MarginBottomNoDividerItemDecoration dividerItemDecoration = new MarginBottomNoDividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL);
//        rvListInclude.addItemDecoration(dividerItemDecoration);
        rvListInclude.setLayoutManager(manager);
        rvListInclude.setAdapter(mListAdapter);


        rvListInclude.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//

                if (manager.findFirstVisibleItemPosition() > 4) {
                    if (newState == 0) {
                        returnTopInclude.setVisibility(VISIBLE);
                        if (returnTopAnimation != null && returnTopAnimation.hasEnded()) {
                            returnTopInclude.startAnimation(returnTopAnimation);
                        }
                    } else {
                        returnTopInclude.setVisibility(GONE);
                    }
                } else {
                    returnTopInclude.setVisibility(GONE);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });

    }

    private void initSmartRefresh() {
        refreshLayoutInclude.setEnableAutoLoadMore(false);//开启自动加载功能（非必须）
        refreshLayoutInclude.setEnableLoadMore(false);
        refreshLayoutInclude.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull final RefreshLayout refreshLayout) {
                mPresenter.doRefresh();
            }
        });

    }

    private void initGsyPlayer() {
        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, gsyPlayerAcAlarmCameraLiveDetail);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);
        gsyPlayerAcAlarmCameraLiveDetail.setIsLive(View.INVISIBLE);
        gsyPlayerAcAlarmCameraLiveDetail.setICityChangeUiVideoPlayerListener(new CityStandardGSYVideoPlayer.ICityChangeUiVideoPlayerListener() {
            @Override
            public void OnCityChangeUiToPlayingShow() {
                orientationUtils.setEnable(true);

            }

            @Override
            public void OnCityChangeUiToPlayingBufferingShow() {
                orientationUtils.setEnable(false);

            }

            @Override
            public void OnchangeVideoFormat() {
                orientationUtils.setEnable(false);
            }
        });
        //增加封面
        if (ivGsyCover == null) {
            ivGsyCover = new ImageView(this);
            ivGsyCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivGsyCover.setImageResource(R.drawable.camera_detail_mask);
        }
        gsyPlayerAcAlarmCameraLiveDetail.setICityChangeUiVideoPlayerListener(new CityStandardGSYVideoPlayer.ICityChangeUiVideoPlayerListener() {
            @Override
            public void OnCityChangeUiToPlayingShow() {
                orientationUtils.setEnable(true);

            }

            @Override
            public void OnCityChangeUiToPlayingBufferingShow() {
                orientationUtils.setEnable(false);

            }

            @Override
            public void OnchangeVideoFormat() {
                orientationUtils.setEnable(false);
            }
        });


        gsyVideoOption = new GSYVideoOptionBuilder();
        gsyVideoOption.setThumbImageView(ivGsyCover)
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setAutoFullWithSize(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
//                .setUrl(url)
                .setCacheWithPlay(false)
//                .setVideoTitle(cameraName)
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPlayError(final String url, Object... objects) {
                        gsyPlayerAcAlarmCameraLiveDetail.setCityPlayState(3);
                        orientationUtils.setEnable(false);
                        backFromWindowFull();
                        gsyPlayerAcAlarmCameraLiveDetail.getPlayAndRetryBtn().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                gsyVideoOption.setUrl(url).build(getCurPlay());
                                getCurPlay().startPlayLogic();
                            }
                        });
                    }


                    @Override
                    public void onAutoComplete(final String url, Object... objects) {
                        orientationUtils.setEnable(false);
                        backFromWindowFull();
//
                    }

                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                        //开始播放了才能旋转和全屏
                        orientationUtils.setEnable(true);
                        isPlay = true;
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        super.onQuitFullscreen(url, objects);

                        if ((gsyPlayerAcAlarmCameraLiveDetail.getCurrentState() != GSYVideoView.CURRENT_STATE_PLAYING)
                                && (gsyPlayerAcAlarmCameraLiveDetail.getCurrentState() != GSYVideoView.CURRENT_STATE_PAUSE)) {
                            orientationUtils.setEnable(false);
                        }
                        if (orientationUtils != null) {
                            orientationUtils.backToProtVideo();
                        }
                    }
                }).setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        }).build(getCurPlay());

        //增加title
        getCurPlay().getTitleTextView().setVisibility(GONE);
        //设置返回键
        getCurPlay().getBackButton().setVisibility(GONE);

        getCurPlay().getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getCurPlay().setEnlargeImageRes(R.drawable.ic_camera_full_screen);

        getCurPlay().setShrinkImageRes(R.drawable.video_shrink);

        getCurPlay().getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                orientationUtils.resolveByClick();
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                getCurPlay().startWindowFullscreen(AlarmForestFireCameraLiveDetailActivity.this, true, true);
            }
        });
        getPlayView().setIsShowBackMaskTv(false);

    }

    private GSYBaseVideoPlayer getCurPlay() {
//        gsyPlayerAcAlarmCameraLiveDetail.setGLRenderMode(GSYVideoGLView.MODE_RENDER_SIZE);
        if (gsyPlayerAcAlarmCameraLiveDetail.getFullWindowPlayer() != null) {
            return gsyPlayerAcAlarmCameraLiveDetail.getFullWindowPlayer();
        }
        return gsyPlayerAcAlarmCameraLiveDetail;
    }

    @Override
    protected AlarmForestFireCameraLiveDetailActivityPresenter createPresenter() {
        return new AlarmForestFireCameraLiveDetailActivityPresenter();
    }

    @Override
    public void showProgressDialog() {
        if (mProgressUtils != null) {
            mProgressUtils.showProgress();
        }
    }

    @Override
    public void dismissProgressDialog() {
        if (mProgressUtils != null) {
            mProgressUtils.dismissProgress();
        }
    }

    @Override
    public void toastShort(String msg) {
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public CityStandardGSYVideoPlayer getPlayView() {
        return gsyPlayerAcAlarmCameraLiveDetail;
    }

    @Override
    public void onVideoPause() {

        onPause();
    }

    @Override
    public void onVideoResume() {

        onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orientationUtils != null)
            orientationUtils.releaseListener();

        if (mProgressUtils != null) {
            mProgressUtils.destroyProgress();
        }

        if (isPlay) {
            getCurPlay().release();
        }

        GSYVideoManager.releaseAllVideos();


    }

    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoManager.onPause();

//        getCurPlay().onVideoPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoManager.onResume();

//        getCurPlay().onVideoResume();
        isPause = false;
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


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause && orientationUtils.isEnable()) {
            getCurPlay().onConfigurationChanged(this, newConfig, orientationUtils, true, true);
        }
    }

    @Override
    public void offlineType(final String url, final String sn) {
        orientationUtils.setEnable(false);
        gsyPlayerAcAlarmCameraLiveDetail.setCityPlayState(5);
        getPlayView().setIsShowBackMaskTv(false);

        gsyPlayerAcAlarmCameraLiveDetail.getPlayAndRetryBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.regainGetCameraState(sn);
                gsyVideoOption.setUrl(url).build(getCurPlay());
                getCurPlay().startPlayLogic();
            }
        });

    }

    @Override
    public void onPullRefreshComplete() {
        refreshLayoutInclude.finishRefresh();
        refreshLayoutInclude.finishLoadMore();
    }

    @Override
    public void setImage(Drawable resource) {
        if (ivGsyCover != null) {
            ivGsyCover.setImageDrawable(resource);
        } else {
            ivGsyCover = new ImageView(this);
            ivGsyCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivGsyCover.setImageDrawable(resource);
        }
        gsyPlayerAcAlarmCameraLiveDetail.setMobileFace(resource);

    }

    @Override
    public void updateData(ArrayList<ForestFireCameraDetailInfo.ListBean> mList) {
        mListAdapter.updateData(mList);
        setNoContentVisible(mList == null || mList.size() < 1);
    }

    @SuppressLint("RestrictedApi")
    private void setNoContentVisible(boolean isVisible) {


        RefreshHeader refreshHeader = refreshLayoutInclude.getRefreshHeader();
        if (refreshHeader != null) {
            if (isVisible) {
                refreshHeader.setPrimaryColors(getResources().getColor(R.color.c_f4f4f4));
            } else {
                refreshHeader.setPrimaryColors(getResources().getColor(R.color.white));
            }
        }

        if (isVisible) {
            refreshLayoutInclude.setRefreshContent(icNoContent);
        } else {
            refreshLayoutInclude.setRefreshContent(rvListInclude);
        }
    }
////
//
////    @Override
//    public void playError(final int pos) {
//        orientationUtils.setEnable(false);
//
//        gsyPlayerAcAlarmCameraLiveDetail.changeRetryType();
//        gsyPlayerAcAlarmCameraLiveDetail.getPlayRetryBtn().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                mPresenter.onCameraItemClick(pos);
//
//
//            }
//        });
//    }


    @Override
    protected void onRestart() {
        //直播重新拉去否则一直lodding，可能是音频有aac编码播放器不支持问题
        mPresenter.doLive();
        super.onRestart();

    }

    @Override
    public void doPlayLive(ArrayList<String> urlList) {
        orientationUtils.setEnable(false);
        gsyPlayerAcAlarmCameraLiveDetail.setCityURl(urlList, "");
        getCurPlay().startPlayLogic();


    }

    @Override
    public void onBackPressed() {
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void backFromWindowFull() {

        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
    }

    @Override
    public void setVerOrientationUtilEnable(boolean enable) {
        if (orientationUtils != null) {
            orientationUtils.setEnable(enable);
        }
    }

    @OnClick(R2.id.include_imv_title_imv_arrows_left)
    public void onViewClicked(View view) {
        int viewID=view.getId();
        if(viewID==R.id.include_imv_title_imv_arrows_left){
            mActivity.finish();
        }else if(viewID==R.id.return_top_include){
            rvListInclude.smoothScrollToPosition(0);
            returnTopInclude.setVisibility(GONE);
            refreshLayoutInclude.closeHeaderOrFooter();
        }
    }


}
