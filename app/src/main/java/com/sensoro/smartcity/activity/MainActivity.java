package com.sensoro.smartcity.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.gyf.immersionbar.ImmersionBar;
import com.sensoro.bottomnavigation.BadgeItem;
import com.sensoro.bottomnavigation.BottomNavigationBar;
import com.sensoro.bottomnavigation.BottomNavigationItem;
import com.sensoro.bottomnavigation.TextBadgeItem;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.helper.PreferencesHelper;
import com.sensoro.common.manger.ActivityTaskManager;
import com.sensoro.common.model.DeployAnalyzerModel;
import com.sensoro.common.model.EventLoginData;
import com.sensoro.common.utils.Repause;
import com.sensoro.common.widgets.ProgressUtils;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.MainFragmentPageAdapter;
import com.sensoro.smartcity.imainviews.IMainView;
import com.sensoro.smartcity.presenter.MainPresenter;
import com.sensoro.smartcity.widget.HomeViewPager;
import com.sensoro.smartcity.widget.dialog.DefaultTipDialogUtils;
import com.sensoro.smartcity.widget.dialog.PermissionChangeDialogUtils;

import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity<IMainView, MainPresenter> implements IMainView
        , BottomNavigationBar.OnTabSelectedListener, Repause.Listener {

    @BindView(R.id.ac_main_hvp_content)
    HomeViewPager acMainHvpContent;
    @BindView(R.id.ac_main_bottom_navigation_bar)
    BottomNavigationBar acMainBottomBar;
    private MainFragmentPageAdapter mPageAdapter;
    private BottomNavigationItem warnItem;
    public ProgressUtils mProgressUtils;

    private PermissionChangeDialogUtils permissionChangeDialogUtils;

    private long lastOpenTime;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
        lastOpenTime = System.currentTimeMillis();
        //TODO 这里应该是在application中 注册过 目前不需要多处注册 只需要在初始化检查一次就可以了
        Repause.registerListener(this);

        //冷启动显示
        LinkedHashMap<String, DeployAnalyzerModel> allTask = PreferencesHelper.getInstance().getOfflineDeployData();
        if (null != allTask && allTask.size() > 0) {
            showOffLineDialog();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mPresenter.onKeyDown(keyCode, event);
    }

    private void initView() {
        mProgressUtils = new ProgressUtils(new ProgressUtils.Builder(mActivity).build());
        initViewPager();

        initBottomBar();
    }

    private void initBottomBar() {
        BottomNavigationItem homeItem = new BottomNavigationItem(R.drawable.selector_ac_main_home, mActivity.getString(R.string.main_page_home));
        warnItem = new BottomNavigationItem(R.drawable.selector_ac_main_warning, mActivity.getString(R.string.main_page_warn));
        warnItem.setBadgeItem(new TextBadgeItem());
        BottomNavigationItem malfunctionItem = new BottomNavigationItem(R.drawable.selector_ac_main_malfunction, mActivity.getString(R.string.main_page_malfunction));
        BottomNavigationItem managerItem = new BottomNavigationItem(R.drawable.selector_ac_main_manage, mActivity.getString(R.string.main_page_manage));
        acMainBottomBar.setTabSelectedListener(this);
        acMainBottomBar
                .addItem(homeItem)
                .addItem(warnItem)
                .addItem(malfunctionItem)
                .addItem(managerItem)
                .setFirstSelectedPosition(0)
                .initialise();
        warnItem.getBadgeItem().hide(false);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected MainPresenter createPresenter() {

        return new MainPresenter();
    }


    @Override
    public void startAC(Intent intent) {
        mActivity.startActivity(intent);
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
    public void toastShort(String msg) {
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_LONG).show();
    }


    @Override
    public void setHpCurrentItem(int position) {
        acMainHvpContent.setCurrentItem(position, false);

    }


    @Override
    public void updateMainPageAdapterData(List<Fragment> fragments) {
        mPageAdapter.setFragmentList(fragments);
        mPageAdapter.notifyDataSetChanged();
    }

    @Override
    public void setHasDeviceBriefControl(boolean hasDeviceBriefControl) {
        acMainBottomBar.setBottomNavigationItemVisible(0, hasDeviceBriefControl);

    }

    @Override
    public void setHasAlarmInfoControl(boolean hasDeviceAlarmInfoControl) {
        acMainBottomBar.setBottomNavigationItemVisible(1, hasDeviceAlarmInfoControl);
    }

    @Override
    public void setHasMalfunctionControl(boolean hasManagerControl) {
        acMainBottomBar.setBottomNavigationItemVisible(2, hasManagerControl);
    }

    @Override
    public void setAlarmWarnCount(int count) {
        BadgeItem badgeItem = warnItem.getBadgeItem();
        if (badgeItem instanceof TextBadgeItem) {
            TextBadgeItem warnBadgeItem = (TextBadgeItem) badgeItem;
            try {
                if (PreferencesHelper.getInstance().getUserData().hasAlarmInfo) {
                    if (count > 0) {
                        if (warnBadgeItem.isHidden()) {
                            warnBadgeItem.show();
                        }
                        warnBadgeItem.setText(String.valueOf(count));
                    } else {
                        warnBadgeItem.hide();
                    }
                } else {
                    warnBadgeItem.hide();
                }
            } catch (Exception e) {
                e.printStackTrace();
                warnBadgeItem.hide();
            }
        }
    }


    @Override
    public void setBottomBarSelected(int position) {
        acMainBottomBar.selectTab(position);
    }

    @Override
    public boolean isHomeFragmentChecked() {
        return acMainHvpContent.getCurrentItem() == 0;
    }

    @Override
    public void showPermissionChangeDialog() {

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Activity topActivity = ActivityTaskManager.getInstance().getTopActivity();
                if (null == permissionChangeDialogUtils) {
                    permissionChangeDialogUtils = new PermissionChangeDialogUtils(topActivity);
                    permissionChangeDialogUtils.setDismissListener(new PermissionChangeDialogUtils.OnPopupDismissListener() {
                        @Override
                        public void onDismiss() {
                            permissionChangeDialogUtils = null;
                        }
                    });
                }


                permissionChangeDialogUtils.show();

            }
        });

    }


    private void initViewPager() {
        mPageAdapter = new MainFragmentPageAdapter(mActivity.getSupportFragmentManager());
        acMainHvpContent.setAdapter(mPageAdapter);
        acMainHvpContent.setOffscreenPageLimit(6);

    }

    @Override
    public boolean setMyCurrentStatusBar() {
        immersionBar = ImmersionBar.with(mActivity);
        immersionBar
                .transparentStatusBar()
                .statusBarDarkFont(true)
                .init();
        return true;
    }

    @Override
    public void onTabSelected(int position) {
        setHpCurrentItem(position);
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {
        //
    }


    @Override
    protected void onDestroy() {
        if (mProgressUtils != null) {
            mProgressUtils.destroyProgress();
            mProgressUtils = null;
        }
        Repause.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    public void onApplicationResumed() {
        Long s = (System.currentTimeMillis() - lastOpenTime) / (1000 * 60);
        if (s > 60) {
            LinkedHashMap<String, DeployAnalyzerModel> allTask = PreferencesHelper.getInstance().getOfflineDeployData();
            if (null != allTask && allTask.size() > 0) {
                showOffLineDialog();
                lastOpenTime = System.currentTimeMillis();
            }
        }
    }

    /**
     * 提示离线上传
     */
    public void showOffLineDialog() {

        EventLoginData userData = PreferencesHelper.getInstance().getUserData();
        if (userData != null) {
            if (userData.hasDeployOfflineTask) {

                Activity topActivity = ActivityTaskManager.getInstance().getTopActivity();
                DefaultTipDialogUtils offlineDialogUtils = new DefaultTipDialogUtils(topActivity);

                offlineDialogUtils.setTipMessageText(getResources().getString(R.string.offline_dialog_content));
                offlineDialogUtils.setTipConfirmText(getResources().getString(R.string.determine), getResources().getColor(R.color.c_1dbb99));
                offlineDialogUtils.setTipCacnleText(getResources().getString(R.string.cancel), getResources().getColor(R.color.c_a6a6a6));
                offlineDialogUtils.setUtilsClickListener(new DefaultTipDialogUtils.DialogUtilsClickListener() {
                    @Override
                    public void onCancelClick() {
                        offlineDialogUtils.dismiss();
                    }

                    @Override
                    public void onConfirmClick() {
                        offlineDialogUtils.dismiss();
                        mActivity.startActivity(new Intent(mActivity, OfflineDeployActivity.class));
                    }
                });
                offlineDialogUtils.show();
            }

        }


    }


    @Override
    public void onApplicationPaused() {

    }
}
