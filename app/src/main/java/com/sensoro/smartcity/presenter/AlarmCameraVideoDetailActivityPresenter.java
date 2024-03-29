package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.manger.ThreadPoolManager;
import com.sensoro.common.model.EventData;
import com.sensoro.common.server.CityObserver;
import com.sensoro.common.server.RetrofitServiceHelper;
import com.sensoro.common.server.bean.AlarmCloudVideoBean;
import com.sensoro.common.server.download.DownloadListener;
import com.sensoro.common.server.download.DownloadUtil;
import com.sensoro.common.server.response.ResponseResult;
import com.sensoro.common.utils.DateUtil;
import com.sensoro.common.utils.FileUtil;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.imainviews.IAlarmCameraVideoDetailActivityView;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_AUTO_COMPLETE;
import static com.shuyu.gsyvideoplayer.video.base.GSYVideoView.CURRENT_STATE_PAUSE;

public class AlarmCameraVideoDetailActivityPresenter extends BasePresenter<IAlarmCameraVideoDetailActivityView>
        implements DownloadListener {
    private Activity mActivity;
    private DownloadUtil mDownloadUtil;
    private String downLoadFilePath;
    private String currentPlayUrl;
    private ArrayList<AlarmCloudVideoBean.MediasBean> mList = new ArrayList<>();
    private AlarmCloudVideoBean mVideoData;
    private AlarmCloudVideoBean.MediasBean mDownloadBean;


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
                    getView().getPlayView().setCityPlayState(-1);
                    getView().setVerOrientationUtil(true);

                    if (getView().getPlayView().getCurrentState() == CURRENT_STATE_PAUSE) {
                        getView().getPlayView().clickCityStartIcon();

                        GSYVideoManager.onResume(true);

                    } else {
                        getView().doPlayLive(currentPlayUrl);
                    }

                    break;

                case ConnectivityManager.TYPE_MOBILE:
                    getView().setVerOrientationUtil(false);

                    if (isAttachedView()) {
                        getView().getPlayView().setCityPlayState(2);

                        getView().getPlayView().getPlayAndRetryBtn().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                getView().getPlayView().setCityPlayState(-1);
                                if (getView().getPlayView().getCurrentState() == CURRENT_STATE_PAUSE) {
                                    getView().getPlayView().clickCityStartIcon();
                                    getView().setVerOrientationUtil(true);

                                }
                                GSYVideoManager.onResume(true);


                            }
                        });

                        getView().backFromWindowFull();

                    }

                    break;

                default:
                    if (isAttachedView()) {


                        getView().backFromWindowFull();
                        getView().getPlayView().setCityPlayState(1);
                        getView().setVerOrientationUtil(false);
                    }
                    break;


            }
        } else if (code == Constants.VIDEO_START) {
            getView().setVerOrientationUtil(true);

            if (getView().getPlayView().getCurrentState() == CURRENT_STATE_PAUSE) {
                getView().getPlayView().clickCityStartIcon();

                GSYVideoManager.onResume(true);

            } else {
                getView().doPlayLive(currentPlayUrl);
            }
        } else if (code == Constants.VIDEO_STOP) {
            getView().setVerOrientationUtil(false);
            GSYVideoManager.onPause();

            getView().backFromWindowFull();


        }
    }

    @Override
    public void initData(Context context) {
        mActivity = (Activity) context;
        EventBus.getDefault().register(this);
        Serializable extra = mActivity.getIntent().getSerializableExtra(Constants.EXTRA_ALARM_CAMERA_VIDEO);
        if (extra instanceof AlarmCloudVideoBean) {
            mVideoData = (AlarmCloudVideoBean) extra;
            mList.clear();
            mList.addAll(mVideoData.getMedias());
            AlarmCloudVideoBean.MediasBean mediasBean = mVideoData.getMedias().get(0);
            currentPlayUrl = mediasBean.getMediaUrl();
            getView().doPlayLive(mediasBean.getMediaUrl());
            getLastCoverImage(mediasBean.getCoverUrl());

            setCreateTime(mediasBean.getCreateTime());
        }
        getView().updateData(mList);

    }

    private void setCreateTime(String createTime) {
        //这里的时间是格林尼治时间，而不是时间戳，原因是，后端说做了转发，不太容易转成时间戳
        try {
            getView().setPlayVideoTime(DateUtil.parseUTC(createTime));
        } catch (ParseException e) {
            e.printStackTrace();
            getView().setPlayVideoTime(mActivity.getString(R.string.time_parse_error));
        }
    }
    public void doOnRestart() {
        if (currentPlayUrl == null) {
            getView().doPlayLive(currentPlayUrl);
        } else {
            if (getView().getPlayView().getCurrentState() == CURRENT_STATE_PAUSE) {
                GSYVideoManager.onResume(false);
            } else if (getView().getPlayView().getCurrentState() != CURRENT_STATE_AUTO_COMPLETE) {
                getView().doPlayLive(currentPlayUrl);
            }
        }
    }
    @Override
    public void onDestroy() {
        mList.clear();
        EventBus.getDefault().unregister(this);
    }

    public void doRefresh() {
        String[] eventIds = {mVideoData.getEventId()};
        RetrofitServiceHelper.getInstance().getCloudVideo(eventIds)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CityObserver<ResponseResult<List<AlarmCloudVideoBean>>>(this) {
                    @Override
                    public void onCompleted(ResponseResult<List<AlarmCloudVideoBean>> response) {
                        List<AlarmCloudVideoBean> data = response.getData();
                        mList.clear();
                        if (data != null && data.size() > 0) {
                            List<AlarmCloudVideoBean.MediasBean> medias = data.get(0).getMedias();
                            if (medias != null && medias.size() > 0) {
                                mList.addAll(medias);
                                AlarmCloudVideoBean.MediasBean mediasBean = medias.get(0);
                                String createTime = mediasBean.getCreateTime();

                                setCreateTime(createTime);

                                getView().doPlayLive(mediasBean.getMediaUrl());
                                getLastCoverImage(mediasBean.getCoverUrl());
                            }

                        }
                        getView().updateData(mList);
                        getView().onPullRefreshComplete();
                    }

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().toastShort(errorMsg);
                        getView().onPullRefreshComplete();
                    }
                });
    }

    public void doItemClick(AlarmCloudVideoBean.MediasBean bean) {
        currentPlayUrl = bean.getMediaUrl();

        getView().doPlayLive(bean.getMediaUrl());
        setCreateTime(bean.getCreateTime());
        getLastCoverImage(bean.getCoverUrl());
    }

    public void doDownload() {
        if (mDownloadBean == null) {
            onFailed(mActivity.getString(R.string.tips_data_error));
            return;
        }
//        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM+"/Camera");
//        if (dcim == null) {
//            dcim = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/DICM/Camera");
//        }
        File cacheFile = FileUtil.getDiskCachePath(mActivity);
        if (cacheFile == null) {
            cacheFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Sensoro/Camera");
        }
        if (!cacheFile.isDirectory()) {
            boolean mkdir = cacheFile.mkdir();
        }
        downLoadFilePath = String.format(Locale.ROOT, "%s/%s%s%s"
                , cacheFile.getAbsolutePath(), mDownloadBean.getSn(), mDownloadBean.getCreateTime(), ".mp4");

        getView().setDownloadStartState(mDownloadBean.getVideoSize());
        if (mDownloadUtil == null) {
            mDownloadUtil = new DownloadUtil(this);
        }
        mDownloadUtil.downloadFile(mDownloadBean.getMediaUrl(), downLoadFilePath);


    }

    @Override
    public void onFinish(final File file) {
        //拷贝文件 并删除缓存文件
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera");
        if (dcim == null) {
            dcim = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DICM/Camera");
        }
        if (!dcim.isDirectory()) {
            boolean mkdir = dcim.mkdir();
        }
        try {
            final String newPath = String.format(Locale.ROOT, "%s/%s%s%s"
                    , dcim.getAbsolutePath(), mDownloadBean.getSn(), mDownloadBean.getCreateTime(), ".mp4");
            ThreadPoolManager.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    FileUtil.copyFile(file.getAbsolutePath(), newPath);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            File fileNew = new File(newPath);
                            insertVideoToMediaStore(fileNew.getAbsolutePath());
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(Uri.fromFile(fileNew));
                            SensoroCityApplication.getInstance().sendBroadcast(intent);
                            if (isAttachedView()) {
                                getView().doDownloadFinish();
                            }
                            deleteCacheFile();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertVideoToMediaStore(String filePath) {
        long createTime = System.currentTimeMillis();
        ContentValues values = initCommonContentValues(filePath, createTime);
        values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, createTime);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        SensoroCityApplication.getInstance().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }

    private ContentValues initCommonContentValues(String filePath, long time) {
        ContentValues values = new ContentValues();
        File saveFile = new File(filePath);
        values.put(MediaStore.MediaColumns.TITLE, saveFile.getName());
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, saveFile.getName());
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time);
        values.put(MediaStore.MediaColumns.DATE_ADDED, time);
        values.put(MediaStore.MediaColumns.DATA, saveFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.SIZE, saveFile.length());
        return values;
    }

    @Override
    public void onProgress(int progress, String totalBytesRead, String fileSize) {
        if (isAttachedView()) {
            getView().updateDownLoadProgress(progress, totalBytesRead, fileSize);
        }
    }

    @Override
    public void onFailed(String errMsg) {
        if (isAttachedView()) {
            getView().setDownloadErrorState();
        }
        deleteCacheFile();
    }

    public void doDownloadCancel() {
        if (mDownloadUtil != null) {
            mDownloadUtil.cancelDownload();
        }
        deleteCacheFile();
    }

    private void getLastCoverImage(String lastCover) {
        Glide.with(mActivity).asBitmap().load(lastCover).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (isAttachedView()){
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(resource);
                    getView().setImage(bitmapDrawable);
                }
            }
        });
    }

    public void setDownloadBean(AlarmCloudVideoBean.MediasBean bean) {
        mDownloadBean = bean;
    }

    /**
     * 删除临时文件
     */
    private void deleteCacheFile() {
        if (!TextUtils.isEmpty(downLoadFilePath)) {
            try {
                ThreadPoolManager.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        FileUtil.deleteDirectory(downLoadFilePath);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
