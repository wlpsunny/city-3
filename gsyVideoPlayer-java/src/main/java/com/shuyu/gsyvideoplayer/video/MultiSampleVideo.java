package com.shuyu.gsyvideoplayer.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sensoro.common.imagepicker.util.BitmapUtil;
import com.sensoro.common.utils.ScreenUtils;
import com.sensoro.common.utils.VideoCorverUtils;
import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.utils.CustomManager;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoViewBridge;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;


/**
 * 多个同时播放的播放控件
 * Created by guoshuyu on 2018/1/31.
 */

public class MultiSampleVideo extends MutilStandardGSYVideoPlayer {

    private final static String TAG = "MultiSampleVideo";


    public ImageView mCoverImage;

    String mCoverOriginUrl;

    int mDefaultRes;

    public MultiSampleVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public MultiSampleVideo(Context context) {
        super(context);
    }

    public MultiSampleVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    VideoCorverUtils mVideoCorverUtils;
    @Override
    protected void init(Context context) {
        super.init(context);
        mVideoCorverUtils=new VideoCorverUtils(context);
        mCoverImage = (ImageView) findViewById(R.id.thumbImage);
        if (mThumbImageViewLayout != null &&
                (mCurrentState == -1 || mCurrentState == CURRENT_STATE_NORMAL || mCurrentState == CURRENT_STATE_ERROR)) {
            mThumbImageViewLayout.setVisibility(VISIBLE);
        }
        onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        //todo 判断如果不是外界造成的就不处理
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        //todo 判断如果不是外界造成的就不处理
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        break;
                }
            }
        };
    }

    @Override
    public GSYVideoViewBridge getGSYVideoManager() {
        CustomManager.getCustomManager(getKey()).initContext(getContext().getApplicationContext());
        return CustomManager.getCustomManager(getKey());
    }

//    @Override
//    protected boolean backFromFull(Context context) {
//        return CustomManager.backFromWindowFull(context, getKey());
//    }

    @Override
    protected void releaseVideos() {
        CustomManager.releaseAllVideos(getKey());
    }


    @Override
    protected int getFullId() {
        return CustomManager.FULLSCREEN_ID;
    }

    @Override
    protected int getSmallId() {
        return CustomManager.SMALL_ID;
    }


//    @Override
//    public int getLayoutId() {
//        return R.layout.video_layout_cover;
//    }

    public void loadCoverImage(String url, int res) {
        mCoverOriginUrl = url;
        mDefaultRes = res;
//        Glide.with(getContext().getApplicationContext())
//                .setDefaultRequestOptions(
//                        new RequestOptions()
//                                .frame(1000000)
//                                .centerCrop()
//                                .error(res)
//                                .placeholder(res))
//                .load(url)
//                .into(mCoverImage);

        Glide.with(getContext().getApplicationContext()).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    resource=mVideoCorverUtils.getCorverBitmap(url,resource,false);
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(resource);
                    mCoverImage.setImageDrawable(bitmapDrawable);
            }
        });
    }

    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        GSYBaseVideoPlayer gsyBaseVideoPlayer = super.startWindowFullscreen(context, actionBar, statusBar);
        MultiSampleVideo multiSampleVideo = (MultiSampleVideo) gsyBaseVideoPlayer;
        multiSampleVideo.loadCoverImage(mCoverOriginUrl, mDefaultRes);
        return multiSampleVideo;
    }


    @Override
    public GSYBaseVideoPlayer showSmallVideo(Point size, boolean actionBar, boolean statusBar) {
        //下面这里替换成你自己的强制转化
        MultiSampleVideo multiSampleVideo = (MultiSampleVideo) super.showSmallVideo(size, actionBar, statusBar);
        multiSampleVideo.mStartButton.setVisibility(GONE);
        multiSampleVideo.mStartButton = null;
        return multiSampleVideo;
    }

    public String getKey() {
        if (mPlayPosition == -22) {
            Debuger.printfError(getClass().getSimpleName() + " used getKey() " + "******* PlayPosition never set. ********");
        }
        if (TextUtils.isEmpty(mPlayTag)) {
            Debuger.printfError(getClass().getSimpleName() + " used getKey() " + "******* PlayTag never set. ********");
        }
        return TAG + mPlayPosition + mPlayTag;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVideoCorverUtils.onDestory();
    }

}
