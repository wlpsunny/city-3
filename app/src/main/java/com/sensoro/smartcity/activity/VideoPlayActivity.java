package com.sensoro.smartcity.activity;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.util.NavigationBarChangeListener;
import com.lzy.imagepicker.util.Utils;
import com.lzy.imagepicker.view.SystemBarTintManager;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.widget.MyVideoView;
import com.sensoro.smartcity.widget.ProgressUtils;

import java.io.File;

import static com.sensoro.smartcity.constant.Constants.RESULT_CODE_RECORD;

/**
 * Created by zhaoshuang on 17/2/24.
 */

public class VideoPlayActivity extends AppCompatActivity implements View.OnClickListener, NavigationBarChangeListener.OnSoftInputStateChangeListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

    private MyVideoView vv_play;
    protected View topBar;
    protected TextView mTitleCount;
    private ImageItem mImageItem;
    private SystemBarTintManager tintManager;
    private ProgressUtils mProgressUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.ImagePickerThemeFullScreen);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
        tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(com.lzy.imagepicker.R.color.ip_color_primary_dark);  //设置上方状态栏的颜色
        setContentView(R.layout.activity_video_play);
        //
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(this).build());
        //
        vv_play = (MyVideoView) findViewById(R.id.vv_play);
        //
        //因为状态栏透明后，布局整体会上移，所以给头部加上状态栏的margin值，保证头部不会被覆盖
        topBar = findViewById(com.lzy.imagepicker.R.id.top_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) topBar.getLayoutParams();
            params.topMargin = Utils.getStatusHeight(this);
            topBar.setLayoutParams(params);
        }
        topBar.findViewById(com.lzy.imagepicker.R.id.btn_ok).setVisibility(View.GONE);
        topBar.findViewById(com.lzy.imagepicker.R.id.btn_back).setOnClickListener(this);
        ImageView mBtnDel = (ImageView) findViewById(com.lzy.imagepicker.R.id.btn_del);
        //TODO 控制删除显示
        mBtnDel.setVisibility(View.VISIBLE);
        //
        mBtnDel.setOnClickListener(this);
        topBar.findViewById(com.lzy.imagepicker.R.id.btn_back).setOnClickListener(this);
        mTitleCount = (TextView) findViewById(com.lzy.imagepicker.R.id.tv_des);
        mTitleCount.setText("视频");
        NavigationBarChangeListener.with(this, NavigationBarChangeListener.ORIENTATION_HORIZONTAL)
                .setListener(this);

        Intent intent = getIntent();
        mImageItem = (ImageItem) intent.getSerializableExtra("path_record");
        if (mImageItem != null) {
            mProgressUtils.showProgress();
            Log.e("VideoPlayActivity", "path = " + mImageItem.recordPath);
            File file = new File(mImageItem.recordPath);
            Log.e("VideoPlayActivity", "path size = " + file.length());
//            vv_play.setVideoPath(mImageItem.recordPath);
            vv_play.setVideoPath("https://resource-city.sensoro.com/24F6087E70945A4D809203E8EC904D2B");
            vv_play.setOnPreparedListener(this);
            vv_play.setOnErrorListener(this);
        }

        vv_play.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case com.lzy.imagepicker.R.id.btn_del:
                showDeleteDialog();
                break;
            case com.lzy.imagepicker.R.id.btn_back:
                finish();
                break;
            case com.lzy.imagepicker.R.id.top_bar:
                if (topBar.getVisibility() == View.VISIBLE) {
                    topBar.setAnimation(AnimationUtils.loadAnimation(VideoPlayActivity.this, com.lzy.imagepicker.R.anim.top_out));
                    topBar.setVisibility(View.GONE);
                    tintManager.setStatusBarTintResource(Color.TRANSPARENT);//通知栏所需颜色
                    //给最外层布局加上这个属性表示，Activity全屏显示，且状态栏被隐藏覆盖掉。
//            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                } else {
                    topBar.setAnimation(AnimationUtils.loadAnimation(VideoPlayActivity.this, com.lzy.imagepicker.R.anim.top_in));
                    topBar.setVisibility(View.VISIBLE);
                    tintManager.setStatusBarTintResource(com.lzy.imagepicker.R.color.ip_color_primary_dark);//通知栏所需颜色
                    //Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住
//            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
                break;
            default:
                break;

        }
    }

    /**
     * 是否删除此视频
     */
    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("要删除这个视频吗吗？");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFile();

            }
        });
        builder.show();
    }

    private void deleteFile() {
        //删除视频
        try {
            File file = new File(mImageItem.recordPath);
            if (file.exists()) {
                file.delete();
            }
            File file1 = new File(mImageItem.path);
            if (file1.exists()) {
                file1.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setResult(RESULT_CODE_RECORD);
        finish();
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    public void onNavigationBarShow(int orientation, int height) {
        topBar.setPadding(0, 0, height, 0);
    }

    @Override
    public void onNavigationBarHide(int orientation) {
        topBar.setPadding(0, 0, 0, 0);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mProgressUtils.dismissProgress();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mProgressUtils.dismissProgress();
        vv_play.setLooping(true);
        vv_play.start();
    }
}