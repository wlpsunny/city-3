package com.sensoro.smartcity.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.gyf.immersionbar.ImmersionBar;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.PersonLocusCameraGaoDeAdapter;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.smartcity.imainviews.ICameraPersonLocusActivityView;
import com.sensoro.smartcity.presenter.CameraPersonLocusActivityPresenter;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SensoroToast;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CameraPersonLocusActivity extends BaseActivity<ICameraPersonLocusActivityView, CameraPersonLocusActivityPresenter>
        implements ICameraPersonLocusActivityView, AMap.OnMapLoadedListener, AMap.OnMarkerClickListener, AMap.OnCameraChangeListener
        , AMap.OnMapClickListener, PersonLocusCameraGaoDeAdapter.onVideoButtonClickListener {

    @BindView(R.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R.id.tv_time_right_ac_person_locus)
    TextView tvTimeRightAcPersonLocus;
    @BindView(R.id.tv_address_right_ac_person_locus)
    TextView tvAddressRightAcPersonLocus;
    @BindView(R.id.tv_one_day_ac_person_locus)
    TextView tvOneDayAcPersonLocus;
    @BindView(R.id.tv_three_day_ac_person_locus)
    TextView tvThreeDayAcPersonLocus;
    @BindView(R.id.tv_seven_day_ac_person_locus)
    TextView tvSevenDayAcPersonLocus;
    @BindView(R.id.include_text_title_divider)
    View includeTextTitleDivider;
    @BindView(R.id.include_text_title_cl_root)
    ConstraintLayout includeTextTitleClRoot;
    @BindView(R.id.mv_ac_person_locus)
    MapView mvAcPersonLocus;
    @BindView(R.id.iv_move_left_ac_person_locus)
    ImageView ivMoveLeftAcPersonLocus;
    @BindView(R.id.iv_monitor_map_location_ac_person_locus)
    ImageView ivMonitorMapLocationAcPersonLocus;
    @BindView(R.id.seek_bar_track_ac_person_locus)
    IndicatorSeekBar seekBarTrackAcPersonLocus;
    @BindView(R.id.iv_move_right_ac_person_locus)
    ImageView ivMoveRightAcPersonLocus;
    private ProgressUtils mProgressUtils;
    private AMap mMap;
    private Polyline mDisPlayLine;
    private PersonLocusCameraGaoDeAdapter mGaoDeInfoAdapter;
    private Marker mAvatarMarker;
    private Polyline mDisplayNormalLine;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_person_locus);
        ButterKnife.bind(this);
        mvAcPersonLocus.onCreate(savedInstanceState);
        initView();
        mPresenter.initData(mActivity);

    }


    private void initView() {
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(this).build());
        setMoveLeftClickable(false);
        includeTextTitleTvTitle.setText(getString(R.string.move_locus));
        includeTextTitleTvSubtitle.setVisibility(View.GONE);
        //seekbar默认时间气泡是不显示的，如果修改，在IndicatorSeekBar.java的onmearsure中
        seekBarTrackAcPersonLocus.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
            private int mSeekBarProgres;

            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, final int progress, float progressFloat, boolean fromUserTouch) {
                mSeekBarProgres = progress;
                seekBarTrackAcPersonLocus.setIndicatorVisible(true);
            }

            @Override
            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                mPresenter.doSeekBarTouch(mSeekBarProgres);
                seekBarTrackAcPersonLocus.setIndicatorVisible(false);

            }
        });

        initMap();

        initGsyVideoView();
    }

    private void initGsyVideoView() {
    }

    private void initMap() {
        mMap = mvAcPersonLocus.getMap();
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setLogoBottomMargin(-100);
        mGaoDeInfoAdapter = new PersonLocusCameraGaoDeAdapter(mActivity);
        mGaoDeInfoAdapter.setOnCloseClickListener(this);
        mMap.setInfoWindowAdapter(mGaoDeInfoAdapter);
        mMap.setMapCustomEnable(true);
        mMap.setOnMapLoadedListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMapClickListener(this);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mGaoDeInfoAdapter.onConfigurationChanged(newConfig);
        seekBarTrackAcPersonLocus.setIndicatorVisible(newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE);

    }

    @Override
    protected CameraPersonLocusActivityPresenter createPresenter() {
        return new CameraPersonLocusActivityPresenter();
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
    public void startAC(Intent intent) {

    }

    @Override
    public void finishAc() {
        mActivity.finish();
    }

    @Override
    public void startACForResult(Intent intent, int requestCode) {

    }

    @Override
    public void setIntentResult(int resultCode) {

    }

    @Override
    public void setIntentResult(int resultCode, Intent data) {

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
    protected void onResume() {
        if (mGaoDeInfoAdapter != null) {
            mGaoDeInfoAdapter.onResume();
        }
        super.onResume();
        mvAcPersonLocus.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mvAcPersonLocus.onPause();
        if (mGaoDeInfoAdapter != null) {
            mGaoDeInfoAdapter.onPause();
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mvAcPersonLocus.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (mProgressUtils != null) {
            mProgressUtils.destroyProgress();
        }
        if (mGaoDeInfoAdapter != null) {
            mGaoDeInfoAdapter.destroyGsyVideo();
        }
        super.onDestroy();
        mvAcPersonLocus.onDestroy();
    }


    @Override
    public void onMapLoaded() {

    }

    @Override
    public void setMapCenter(CameraUpdate cameraUpdate) {
        if (mMap != null) {
            mMap.moveCamera(cameraUpdate);
        }
    }

    @Override
    public void addMarker(MarkerOptions markerOptions, int tag) {
        if (mMap != null) {
            Marker marker = mMap.addMarker(markerOptions);
            if (tag == -1) {
                mAvatarMarker = marker;
            }
            marker.setObject(tag);
        }
    }

    @Override
    public void addPolyLine(PolylineOptions linePoints, boolean b) {
        Polyline polyline = mMap.addPolyline(linePoints);
        if (b) {
            mDisPlayLine = polyline;
        }else{
            mDisplayNormalLine = polyline;
        }

    }


    @Override
    public void setMoveLeftClickable(boolean clickable) {
        ivMoveLeftAcPersonLocus.setEnabled(clickable);
        ivMoveLeftAcPersonLocus.setColorFilter(mActivity.getResources().getColor(clickable ? R.color.c_1dbb99 : R.color.c_dfdfdf));
    }

    @Override
    public void setMoveRightClickable(boolean clickable) {
        ivMoveRightAcPersonLocus.setClickable(clickable);
        ivMoveRightAcPersonLocus.setColorFilter(mActivity.getResources().getColor(clickable ? R.color.c_1dbb99 : R.color.c_dfdfdf));
    }

    @Override
    public void removeAllMarker() {
        if (mMap != null) {
            List<Marker> markers = mMap.getMapScreenMarkers();
            for (Marker marker : markers) {
                if (marker != null) {
                    marker.remove();
                }
            }
            mvAcPersonLocus.invalidate();
        }
    }


    @Override
    public void initSeekBar(int size) {
        seekBarTrackAcPersonLocus.setMax(size);
        seekBarTrackAcPersonLocus.setProgress(0);
    }

    @Override
    public void clearDisplayLine() {
        if (mDisPlayLine != null) {
            mDisPlayLine.remove();
        }
    }

    @Override
    public void updateSeekBar(int index) {
        seekBarTrackAcPersonLocus.setProgress(index);
    }

    @Override
    public void updateAvatarMarker(LatLng latLng, Bitmap resource) {
        if (mAvatarMarker != null) {
            mAvatarMarker.setPosition(latLng);
            mAvatarMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resource));
        }
    }


    @Override
    public boolean setMyCurrentActivityTheme() {
        setTheme(R.style.Theme_AppCompat_Translucent);
        return true;
    }

    @Override
    public boolean setMyCurrentStatusBar() {
        immersionBar = ImmersionBar.with(mActivity);
        immersionBar.transparentStatusBar().statusBarDarkFont(true).init();
        return true;
    }

    @Override
    public void startPlay(String url1) {
        mGaoDeInfoAdapter.startPlayLogic(url1);
    }

    @Override
    public void playError(int index) {

    }

    @Override
    public void setMarkerAddress(String address) {
        tvAddressRightAcPersonLocus.setText(address);
    }

    @Override
    public void removeNormalMarker(Integer tag) {
        if (mMap != null) {
            List<Marker> markers = mMap.getMapScreenMarkers();
            for (Marker marker : markers) {
                Object object = marker.getObject();
                if (object instanceof Integer && tag.equals(object)) {
                    marker.remove();
                    mvAcPersonLocus.invalidate();
                }
            }
        }
    }

    @Override
    public void clearNormalMarker() {
        if (mMap != null) {
            List<Marker> markers = mMap.getMapScreenMarkers();
            for (Marker marker : markers) {
                Object object = marker.getObject();
                if (object instanceof Integer && (Integer) object > -1) {
                    marker.remove();
                    mvAcPersonLocus.invalidate();
                }
            }
        }
    }

    @Override
    public void setSelectDayBg(int day) {

        tvOneDayAcPersonLocus.setBackgroundColor(mActivity.getResources().getColor(day == 1 ? R.color.c_1dbb99 : R.color.white));
        tvOneDayAcPersonLocus.setTextColor(mActivity.getResources().getColor(day == 1 ? R.color.white : R.color.c_252525));

        tvThreeDayAcPersonLocus.setBackgroundColor(mActivity.getResources().getColor(day == 3 ? R.color.c_1dbb99 : R.color.white));
        tvThreeDayAcPersonLocus.setTextColor(mActivity.getResources().getColor(day == 3 ? R.color.white : R.color.c_252525));

        tvSevenDayAcPersonLocus.setBackgroundColor(mActivity.getResources().getColor(day == 7 ? R.color.c_1dbb99 : R.color.white));
        tvSevenDayAcPersonLocus.setTextColor(mActivity.getResources().getColor(day == 7 ? R.color.white : R.color.c_252525));
    }

    @Override
    public void setSeekBarTime(String time) {
        seekBarTrackAcPersonLocus.setIndicatorText(time);
    }


    @Override
    public void moveAvatarMarker(LatLng latLng) {
        if (mAvatarMarker != null) {
            mAvatarMarker.setPosition(latLng);
//            mAvatarMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resource));
        }
    }

    @Override
    public void setLastCover(BitmapDrawable bitmapDrawable) {
        mGaoDeInfoAdapter.setLastCover(bitmapDrawable);
    }

    @Override
    public void setSeekBarVisible(boolean isVisible) {
        seekBarTrackAcPersonLocus.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void clearDisplayNormalLine() {
        if (mDisplayNormalLine != null) {
            mDisplayNormalLine.remove();
        }
    }

    @Override
    public void setSeekBarTimeVisible(boolean isVisible) {
        seekBarTrackAcPersonLocus.setIndicatorVisible(isVisible);
    }

    @Override
    public void setCityPlayState(int state) {
        if (mGaoDeInfoAdapter != null) {
            mGaoDeInfoAdapter.setCityPlayState(state);
        }
    }

    @Override
    public void setVerOrientationUtil(boolean enable) {
        if (mGaoDeInfoAdapter != null) {
            mGaoDeInfoAdapter.setVerOrientationUtil(enable);

        }
    }

    @Override
    public int getCurrentState() {
        return mGaoDeInfoAdapter.getCurrentState();
    }

    @Override
    public void clickCityStartIcon() {
        if (mGaoDeInfoAdapter != null) {
            mGaoDeInfoAdapter.clickCityStartIcon();

        }
    }

    @Override
    public View getPlayAndRetryBtn() {
        return mGaoDeInfoAdapter.getPlayAndRetryBtn();
    }

    @Override
    public void backFromWindowFull() {
        if (mGaoDeInfoAdapter != null) {
            mGaoDeInfoAdapter.backFromWindowFull();

        }

    }

    @Override
    public void onVideoResume() {
        if (mGaoDeInfoAdapter != null) {
            mGaoDeInfoAdapter.onResume();
        }
    }

    @Override
    public void onVideoPause() {
        if (mGaoDeInfoAdapter != null) {
            mGaoDeInfoAdapter.onPause();
        }
    }

    @Override
    public void setMarkerTime(String time) {
        tvTimeRightAcPersonLocus.setText(time);
    }

    @OnClick({R.id.iv_move_left_ac_person_locus, R.id.iv_move_right_ac_person_locus
            , R.id.tv_one_day_ac_person_locus, R.id.tv_three_day_ac_person_locus
            , R.id.tv_seven_day_ac_person_locus, R.id.iv_monitor_map_location_ac_person_locus,
            R.id.include_text_title_imv_arrows_left,R.id.ll_bottom_ac_person_locus})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_move_left_ac_person_locus:
                mPresenter.doMoveLeft();
                break;
            case R.id.iv_move_right_ac_person_locus:
                mPresenter.doMoveRight();
                break;
            case R.id.tv_one_day_ac_person_locus:
                mPresenter.doOneDay();
                break;
            case R.id.tv_three_day_ac_person_locus:
                mPresenter.doThreeDay();
                break;
            case R.id.tv_seven_day_ac_person_locus:
                mPresenter.doSevenDay();
                break;
            case R.id.iv_monitor_map_location_ac_person_locus:
                mPresenter.doMonitorMapLocation();
                break;
            case R.id.include_text_title_imv_arrows_left:
                finishAc();
                break;
            case R.id.ll_bottom_ac_person_locus:
                //添加点击事件仅仅为了拦截掉手势，不让map获取事件，所以这里不做任何事情
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mAvatarMarker != null && mAvatarMarker.isInfoWindowShown()) {
            mAvatarMarker.hideInfoWindow();
            mGaoDeInfoAdapter.onPause();
        } else {
            super.onBackPressed();
        }


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mAvatarMarker != null && !mAvatarMarker.isInfoWindowShown()) {
            marker.showInfoWindow();
            mPresenter.doPlay();
        }

        return true;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        mPresenter.setMapZoom(cameraPosition.zoom);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mAvatarMarker != null && mAvatarMarker.isInfoWindowShown()) {
            mAvatarMarker.hideInfoWindow();
            mGaoDeInfoAdapter.onPause();

        }
    }

    @Override
    public void onCloseClick() {
        if (mAvatarMarker != null && mAvatarMarker.isInfoWindowShown()) {
            mAvatarMarker.hideInfoWindow();
            mGaoDeInfoAdapter.onPause();
        }
    }

    @Override
    public void onFullScreenClick() {

    }
}
