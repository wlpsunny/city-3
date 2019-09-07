package com.sensoro.smartcity.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.temp.entity.VideoModel;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.CityStandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView;

import java.util.ArrayList;
import java.util.List;

/**
 * 多个播放的listview adapter
 * Created by shuyu on 2016/11/12.
 */

public class ListMultiNormalAdapter extends BaseAdapter {

    public static final String TAG = "ListMultiNormalAdapter";

    private List<VideoModel> list = new ArrayList<>();
    private LayoutInflater inflater = null;
    private Context context;
    private OrientationUtils orientationUtils;


    public ListMultiNormalAdapter(Context context) {
        super();
        this.context = context;
        inflater = LayoutInflater.from(context);
        list.add(new VideoModel("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4"));
        list.add(new VideoModel("https://scpub-oss1.antelopecloud.cn/records/m3u8_info2/1567763820_1567763851.m3u8?access_token=540409951_3356491776_1598775818_24520bdc9e5d45495b8213de4faf5bea&head=1"));

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_video_item_mutli, null);
            holder.gsyVideoPlayer = (CityStandardGSYVideoPlayer) convertView.findViewById(R.id.video_item_player);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        //多个播放时必须在setUpLazy、setUp和getGSYVideoManager()等前面设置
        holder.gsyVideoPlayer.setPlayTag(TAG);
        holder.gsyVideoPlayer.setPlayPosition(position);


        //增加title
        holder.gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);

        //设置返回键
        holder.gsyVideoPlayer.getBackButton().setVisibility(View.GONE);

        //设置全屏按键功能
//        holder.gsyVideoPlayer.setRotateViewAuto(true);
//        holder.gsyVideoPlayer.setLockLand(true);
        holder.gsyVideoPlayer.setReleaseWhenLossAudio(false);
//        holder.gsyVideoPlayer.setShowFullAnimation(true);
        holder.gsyVideoPlayer.setIsTouchWiget(false);

        holder.gsyVideoPlayer.setNeedLockFull(true);


        orientationUtils = new OrientationUtils((Activity) context, holder.gsyVideoPlayer);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);


        GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();

        gsyVideoOption
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
                        holder.gsyVideoPlayer.setCityPlayState(3);
                        backFromWindowFull();
                        holder.gsyVideoPlayer.getPlayAndRetryBtn().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                gsyVideoOption.setUrl(url).build(holder.gsyVideoPlayer);
                                holder.gsyVideoPlayer.startPlayLogic();
                            }
                        });
                    }

                    @Override
                    public void onAutoComplete(final String url, Object... objects) {

                        backFromWindowFull();
//
                    }

                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        super.onQuitFullscreen(url, objects);

                        if ((holder.gsyVideoPlayer.getCurrentState() != GSYVideoView.CURRENT_STATE_PLAYING)
                                && (holder.gsyVideoPlayer.getCurrentState() != GSYVideoView.CURRENT_STATE_PAUSE)) {
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
        }).build(holder.gsyVideoPlayer);


        holder.gsyVideoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backFromWindowFull();
            }
        });
        VideoModel videoModel = list.get(position);

        boolean isPlaying = holder.gsyVideoPlayer.getCurrentPlayer().isInPlayingState();

        if (!isPlaying) {
            holder.gsyVideoPlayer.setUpLazy(videoModel.url, false, null, null, "这是title");
            holder.gsyVideoPlayer.startPlayLogic();
//            holder.gsyVideoPlayer.setIsLive(View.INVISIBLE);

            holder.gsyVideoPlayer.startPlayLogic();
        }
        holder.gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                orientationUtils.resolveByClick();
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                holder.gsyVideoPlayer.startWindowFullscreen(context, true, true);
            }
        });

        return convertView;
    }

    public void backFromWindowFull() {
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
        if (GSYVideoManager.backFromWindowFull(context)) {
            return;
        }
    }


    class ViewHolder {
        CityStandardGSYVideoPlayer gsyVideoPlayer;
    }


//    public String getFullKey() {
//        return fullKey;
//    }

}
