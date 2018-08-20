package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.igexin.sdk.PushManager;
import com.lzy.imagepicker.ImagePicker;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.activity.LoginActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.factory.MenuPageFactory;
import com.sensoro.smartcity.fragment.AlarmListFragment;
import com.sensoro.smartcity.fragment.ContractFragment;
import com.sensoro.smartcity.fragment.IndexFragment;
import com.sensoro.smartcity.fragment.MerchantSwitchFragment;
import com.sensoro.smartcity.fragment.PointDeployFragment;
import com.sensoro.smartcity.fragment.ScanLoginFragment;
import com.sensoro.smartcity.fragment.StationDeployFragment;
import com.sensoro.smartcity.imainviews.IMainView;
import com.sensoro.smartcity.iwidget.IOnCreate;
import com.sensoro.smartcity.model.EventData;
import com.sensoro.smartcity.model.MenuPageInfo;
import com.sensoro.smartcity.push.SensoroPushIntentService;
import com.sensoro.smartcity.push.SensoroPushService;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.Character;
import com.sensoro.smartcity.server.bean.DeviceInfo;
import com.sensoro.smartcity.server.bean.GrantsInfo;
import com.sensoro.smartcity.server.bean.UserInfo;
import com.sensoro.smartcity.server.response.ResponseBase;
import com.sensoro.smartcity.server.response.UpdateRsp;
import com.sensoro.smartcity.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainPresenter extends BasePresenter<IMainView> implements Constants, IOnCreate {
    private Activity mActivity;
    private long exitTime = 0;
    private String mUserName = null;
    private String mPhone = null;
    private String mPhoneId = null;
    private volatile boolean mIsSupperAccount;
    private String roles;


    private final List<Fragment> fragmentList = new ArrayList<>();
    //
    private IndexFragment indexFragment = null;
    private AlarmListFragment alarmListFragment = null;
    private MerchantSwitchFragment merchantSwitchFragment = null;
    private PointDeployFragment pointDeployFragment = null;
    private StationDeployFragment stationDeployFragment = null;
    private ContractFragment contractFragment = null;
    private ScanLoginFragment scanLoginFragment = null;
    //
    private volatile Socket mSocket = null;
    private final DeviceInfoListener mInfoListener = new DeviceInfoListener();

    private final Handler mHandler = new Handler();
    private final TaskRunnable mRunnable = new TaskRunnable();
    //
    private boolean hasStation = false;
    private boolean hasContract = false;
    private boolean hasScanLogin = false;

    /**
     * 超级用户
     *
     * @return
     */
    public boolean isSupperAccount() {
        return mIsSupperAccount;
    }

    public String getRoles() {
        return roles;
    }

    public void checkPush() {
        boolean pushTurnedOn = PushManager.getInstance().isPushTurnedOn(SensoroCityApplication.getInstance());
        LogUtils.logd(this, "checkPush: " + pushTurnedOn);
        if (!pushTurnedOn) {
            PushManager.getInstance().initialize(SensoroCityApplication.getInstance(), SensoroPushService.class);
            // 注册 intentService 后 PushDemoReceiver 无效, sdk 会使用 DemoIntentService 传递数据,
            // AndroidManifest 对应保留一个即可(如果注册 DemoIntentService, 可以去掉 PushDemoReceiver, 如果注册了
            // IntentService, 必须在 AndroidManifest 中声明)
            PushManager.getInstance().registerPushIntentService(SensoroCityApplication.getInstance(),
                    SensoroPushIntentService
                            .class);
        }
    }


    @Override
    public void initData(Context context) {
        mActivity = (Activity) context;
        //
        mUserName = mActivity.getIntent().getStringExtra(EXTRA_USER_NAME);
        mPhone = mActivity.getIntent().getStringExtra(EXTRA_PHONE);
        mPhoneId = mActivity.getIntent().getStringExtra(EXTRA_PHONE_ID);
        Character character = (Character) mActivity.getIntent().getSerializableExtra(EXTRA_CHARACTER);
        roles = mActivity.getIntent().getStringExtra(EXTRA_USER_ROLES);
        mIsSupperAccount = mActivity.getIntent().getBooleanExtra(EXTRA_IS_SPECIFIC, false);
        hasStation = mActivity.getIntent().getBooleanExtra(EXTRA_GRANTS_HAS_STATION, false);
        hasContract = mActivity.getIntent().getBooleanExtra(EXTRA_GRANTS_HAS_CONTRACT, false);
        hasScanLogin = mActivity.getIntent().getBooleanExtra(EXTRA_GRANTS_HAS_SCAN_LOGIN, false);
        //
        indexFragment = IndexFragment.newInstance(character);
        alarmListFragment = AlarmListFragment.newInstance("alarm");
        merchantSwitchFragment = MerchantSwitchFragment.newInstance("merchant");
        pointDeployFragment = PointDeployFragment.newInstance("point");
        stationDeployFragment = StationDeployFragment.newInstance("station");
        contractFragment = ContractFragment.newInstance("contract");
        scanLoginFragment = ScanLoginFragment.newInstance("scanLogin");

        //
        fragmentList.add(indexFragment);
        fragmentList.add(alarmListFragment);
        fragmentList.add(merchantSwitchFragment);
        fragmentList.add(pointDeployFragment);
        fragmentList.add(stationDeployFragment);
        fragmentList.add(scanLoginFragment);
        fragmentList.add(contractFragment);
        getView().updateMainPageAdapterData(fragmentList);
        getView().showAccountInfo(mUserName, mPhone);
        mHandler.postDelayed(mRunnable, 3000L);
    }

    public void changeAccount(String userName, String phone, String roles, boolean isSpecific, boolean hasStation,
                              boolean hasContract, boolean hasScanLogin) {
        this.mUserName = userName;
        this.mPhone = phone;
        this.mIsSupperAccount = isSpecific;
        this.roles = roles;
        this.hasStation = hasStation;
        this.hasContract = hasContract;
        this.hasScanLogin = hasScanLogin;
        //
        getView().showAccountInfo(userName, phone);
        if (indexFragment != null) {
            if (isSpecific) {
                merchantSwitchFragment.requestDataByDirection(DIRECTION_DOWN, true);
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        indexFragment.reFreshDataByDirection(DIRECTION_DOWN);
                        indexFragment.requestTopData(true);
                    }
                });
            }
            //
            getView().updateMenuPager(MenuPageFactory.createMenuPageList(isSpecific, roles, hasStation,
                    hasContract, hasScanLogin));
            getView().setCurrentPagerItem(0);
            getView().setMenuSelected(0);
            reconnect();
        }

    }

    public void setAppVersion() {
        PackageManager manager = mActivity.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(mActivity.getPackageName(), 0);
            String appVersionName = info.versionName; // 版本名
            int currentVersionCode = info.versionCode; // 版本号
            getView().setAPPVersionCode("City " + appVersionName);
            System.out.println(currentVersionCode + " " + appVersionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 简单判断账户类型
     */
    public void freshAccountType() {
        if (mIsSupperAccount) {
            getView().setCurrentPagerItem(2);
        } else {
            getView().setCurrentPagerItem(0);
        }
        //TODO 考虑到声明周期问题 暂时延缓后续优化

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getView().updateMenuPager(MenuPageFactory.createMenuPageList(mIsSupperAccount, roles, hasStation,
                        hasContract, hasScanLogin));
                if (mIsSupperAccount) {
                    merchantSwitchFragment.requestDataByDirection(DIRECTION_DOWN, true);
                }
                merchantSwitchFragment.refreshData(mUserName, (mPhone == null ? "" : mPhone), mPhoneId);
                getView().setMenuSelected(0);
            }
        }, 50);
    }

    private void requestUpdate() {
        RetrofitServiceHelper.INSTANCE.getUpdateInfo().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers
                .mainThread()).subscribe(new CityObserver<UpdateRsp>() {


            @Override
            public void onCompleted() {

            }

            @Override
            public void onNext(UpdateRsp updateRsp) {
                LogUtils.logd(this, "获取app升级ok========" + updateRsp.toString());
                try {
                    PackageInfo info = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
                    if (info.versionCode < updateRsp.getVersion()) {
                        String changelog = updateRsp.getChangelog();
                        String install_url = updateRsp.getInstall_url();
                        getView().showUpdateAppDialog(changelog, install_url);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                LogUtils.loge(this, "app升级" + errorMsg);
            }
        });
    }


    private void createSocket() {
        try {
            String sessionId = RetrofitServiceHelper.INSTANCE.getSessionId();
            IO.Options options = new IO.Options();
            options.query = "session=" + sessionId;
            options.forceNew = true;
            mSocket = IO.socket(RetrofitServiceHelper.INSTANCE.BASE_URL, options);
            mSocket.on(SOCKET_EVENT_DEVICE_INFO, mInfoListener);
            mSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    private void reconnect() {
        try {
            if (mSocket != null) {
                mSocket.disconnect();
                mSocket.off(SOCKET_EVENT_DEVICE_INFO, mInfoListener);
                mSocket = null;
            }
            String sessionId = RetrofitServiceHelper.INSTANCE.getSessionId();
            IO.Options options = new IO.Options();
            options.query = "session=" + sessionId;
            options.forceNew = true;
            mSocket = IO.socket(RetrofitServiceHelper.INSTANCE.BASE_URL, options);
            mSocket.on(SOCKET_EVENT_DEVICE_INFO, mInfoListener);
            mSocket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public void logout() {
        String phoneId = mActivity.getIntent().getStringExtra(EXTRA_PHONE_ID);
        String uid = mActivity.getIntent().getStringExtra(EXTRA_USER_ID);
        getView().showProgressDialog();
        RetrofitServiceHelper.INSTANCE.logout(phoneId, uid).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers
                .mainThread()).subscribe(new CityObserver<ResponseBase>() {
            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);
            }

            @Override
            public void onCompleted() {
                getView().dismissProgressDialog();
            }


            @Override
            public void onNext(ResponseBase responseBase) {
                if (responseBase.getErrcode() == ResponseBase.CODE_SUCCESS) {
                    Intent intent = new Intent(mActivity, LoginActivity.class);
                    getView().startAC(intent);
                    getView().finishAc();
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off(SOCKET_EVENT_DEVICE_INFO, mInfoListener);
            mSocket = null;
        }
        mHandler.removeCallbacks(mRunnable);
        mHandler.removeCallbacksAndMessages(null);
        fragmentList.clear();
    }

    public void updateApp(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        getView().startAC(intent);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData eventData) {
        //TODO 可以修改以此种方式传递，方便管理
        int code = eventData.code;
        Object data = eventData.data;
        if (code == EVENT_DATA_FINISH_CODE) {
            if (contractFragment != null) {
                contractFragment.requestDataByDirection(DIRECTION_DOWN, false);
            }
        } else if (code == EVENT_DATA_DEPLOY_RESULT_FINISH) {
            if (data != null && data instanceof DeviceInfo) {
                refreshDeviceInfo((DeviceInfo) data);
            }
            getView().setCurrentPagerItem(0);
            getView().setMenuSelected(0);
        } else if (code == EVENT_DATA_SEARCH_MERCHANT) {
            if (data != null && data instanceof UserInfo) {
                UserInfo dataUser = (UserInfo) data;
                //
                String sessionID = dataUser.getSessionID();
                RetrofitServiceHelper.INSTANCE.setSessionId(sessionID);
                String nickname = dataUser.getNickname();
                String phone = dataUser.getContacts();
                String roles = dataUser.getRoles();
                String isSpecific = dataUser.getIsSpecific();
                //grants Info
                GrantsInfo grants = dataUser.getGrants();
                //
                changeAccount(nickname, phone, roles, MenuPageFactory.getIsSupperAccount(isSpecific), MenuPageFactory
                        .getHasStationDeploy(grants), MenuPageFactory.getHasContract(grants), MenuPageFactory
                        .getHasScanLogin(grants));
            }
        }
//        LogUtils.loge(this, eventData.toString());
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (alarmListFragment.onKeyDown(keyCode, event)) {
            exit();
        }
        return false;
    }

    private class TaskRunnable implements Runnable {

        @Override
        public void run() {
            requestUpdate();
            if (!mIsSupperAccount) {
                createSocket();
            }
        }
    }

    private final class DeviceInfoListener implements Emitter.Listener {

        @Override
        public void call(Object... args) {
            try {
                synchronized (DeviceInfoListener.class) {
                    for (Object arg : args) {
                        if (arg instanceof JSONArray) {
                            JSONArray jsonArray = (JSONArray) arg;
                            final JSONObject jsonObject = jsonArray.getJSONObject(0);
                            String json = jsonObject.toString();
//                            LogUtils.loge(this, "jsonArray = " + json);
                            if (!mIsSupperAccount) {
                                try {
                                    DeviceInfo data = RetrofitServiceHelper.INSTANCE.getGson().fromJson(json,
                                            DeviceInfo.class);
                                    final EventData eventData = new EventData();
                                    eventData.code = EVENT_DATA_SOCKET_DATA;
                                    eventData.data = data;
                                    EventBus.getDefault().post(eventData);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            getView().toastShort(mActivity.getResources().getString(R.string.exit_main));
            exitTime = System.currentTimeMillis();
        } else {
            getView().finishAc();
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS || resultCode == ImagePicker.RESULT_CODE_BACK) {
            if (alarmListFragment != null) {
                alarmListFragment.handlerActivityResult(requestCode, resultCode, data);
            }
        }

    }

    /**
     * 通过menupage判断类型
     */
    public void clickMenuItem(int menuPageId) {
        switch (menuPageId) {
            case MenuPageInfo.MENU_PAGE_INDEX:
                indexFragment.reFreshDataByDirection(DIRECTION_DOWN);
                getView().setCurrentPagerItem(0);
                break;
            case MenuPageInfo.MENU_PAGE_ALARM:
                alarmListFragment.requestDataByDirection(DIRECTION_DOWN, true);
                getView().setCurrentPagerItem(1);
                break;
            case MenuPageInfo.MENU_PAGE_MERCHANT:
                merchantSwitchFragment.requestDataByDirection(DIRECTION_DOWN, true);
                merchantSwitchFragment.refreshData(mUserName, mPhone, mPhoneId);
                getView().setCurrentPagerItem(2);
                break;
            case MenuPageInfo.MENU_PAGE_POINT:
                getView().setCurrentPagerItem(3);
                break;
            case MenuPageInfo.MENU_PAGE_STATION:
                getView().setCurrentPagerItem(4);
                break;
            case MenuPageInfo.MENU_PAGE_SCAN_LOGIN:
                getView().setCurrentPagerItem(5);
                break;
            case MenuPageInfo.MENU_PAGE_CONTRACT:
                contractFragment.requestDataByDirection(DIRECTION_DOWN, true);
                getView().setCurrentPagerItem(6);
                break;
            default:
                break;
        }
    }

    private void refreshDeviceInfo(DeviceInfo deviceInfo) {
        for (int i = 0; i < SensoroCityApplication.getInstance().getData().size(); i++) {
            DeviceInfo tempDeviceInfo = SensoroCityApplication.getInstance().getData().get(i);
            if (deviceInfo.getSn().equals(tempDeviceInfo.getSn())) {
                SensoroCityApplication.getInstance().getData().set(i, deviceInfo);
                break;
            }
        }
    }
}
