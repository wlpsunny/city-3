package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.igexin.sdk.PushManager;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.activity.MainActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.factory.MenuPageFactory;
import com.sensoro.smartcity.imainviews.ILoginView;
import com.sensoro.smartcity.model.EventLoginData;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.GrantsInfo;
import com.sensoro.smartcity.server.bean.UserInfo;
import com.sensoro.smartcity.server.response.LoginRsp;
import com.sensoro.smartcity.server.response.ResponseBase;
import com.sensoro.smartcity.util.AESUtil;
import com.sensoro.smartcity.util.PreferencesHelper;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class LoginPresenter extends BasePresenter<ILoginView> implements Constants {
    private Activity mContext;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        readLoginData();
        initSeverUrl();
    }

    private void readLoginData() {
        SharedPreferences sp = SensoroCityApplication.getInstance().getSharedPreferences(PREFERENCE_LOGIN_NAME_PWD, Context
                .MODE_PRIVATE);
        String name = sp.getString(PREFERENCE_KEY_NAME, null);
        String pwd = sp.getString(PREFERENCE_KEY_PASSWORD, null);
        if (!TextUtils.isEmpty(name)) {
            getView().showAccountName(name);
        }
        if (!TextUtils.isEmpty(pwd)) {
            String aes_pwd = AESUtil.decode(pwd);
            getView().showAccountPwd(aes_pwd);
        }
    }

    private void initSeverUrl() {
        //去除从用户安装渠道设置登录环境
//        try {
//            ApplicationInfo appInfo = mContext.getPackageManager()
//                    .getApplicationInfo(mContext.getPackageName(),
//                            PackageManager.GET_META_DATA);
//            String msg = appInfo.metaData.getString("InstallChannel");
//            if (msg.equalsIgnoreCase("Mocha")) {
//                RetrofitServiceHelper.INSTANCE.saveBaseUrlType(true);
//            } else if (msg.equalsIgnoreCase("Master")) {
//                RetrofitServiceHelper.INSTANCE.saveBaseUrlType(false);
//            } else {
//                RetrofitServiceHelper.INSTANCE.saveBaseUrlType(true);
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
        getView().setLogButtonState(RetrofitServiceHelper.INSTANCE.getBaseUrlType());
//        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_SCOPE, Context
//                .MODE_PRIVATE);
//        int urlType = 0;
//        try {
//            urlType = sp.getInt(PREFERENCE_KEY_URL, 0);
//            RetrofitServiceHelper.INSTANCE.saveBaseUrlType(urlType);
//            getView().setLogButtonState(urlType);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }

    public void saveScopeData(int which) {
        RetrofitServiceHelper.INSTANCE.saveBaseUrlType(which);
        getView().setLogButtonState(which);

    }

    public void login(final String account, final String pwd) {
        if (TextUtils.isEmpty(account)) {
            getView().toastShort(mContext.getResources().getString(R.string.tips_username_empty));
        } else if (TextUtils.isEmpty(pwd)) {
            getView().toastShort(mContext.getResources().getString(R.string.tips_login_pwd_empty));
        } else {
            final String phoneId = PushManager.getInstance().getClientid(SensoroCityApplication.getInstance());
            getView().showProgressDialog();
            RetrofitServiceHelper.INSTANCE.login(account, pwd, phoneId).subscribeOn
                    (Schedulers
                            .io()).doOnNext(new Action1<LoginRsp>() {
                @Override
                public void call(LoginRsp loginRsp) {
                    String sessionID = loginRsp.getData().getSessionID();
                    RetrofitServiceHelper.INSTANCE.saveSessionId(sessionID);
                    saveLoginData(account, pwd);
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<LoginRsp>() {
                @Override
                public void onCompleted() {
//                    getView().dismissProgressDialog();
                    getView().finishAc();
                }

                @Override
                public void onNext(LoginRsp loginRsp) {
                    if (loginRsp.getErrcode() == ResponseBase.CODE_SUCCESS) {
                        UserInfo userInfo = loginRsp.getData();
                        //
                        EventLoginData eventLoginData = new EventLoginData();
                        GrantsInfo grants = userInfo.getGrants();
                        //
                        eventLoginData.userId = userInfo.get_id();
                        eventLoginData.userName = userInfo.getNickname();
                        eventLoginData.phone = userInfo.getContacts();
                        eventLoginData.phoneId = phoneId;
                        //TODO 处理Character信息
//                      mCharacter = userInfo.getCharacter();
                        eventLoginData.roles = userInfo.getRoles();
                        eventLoginData.isSupperAccount = MenuPageFactory.getIsSupperAccount(userInfo.getIsSpecific());
                        eventLoginData.hasStation = MenuPageFactory.getHasStationDeploy(grants);
                        eventLoginData.hasContract = MenuPageFactory.getHasContract(grants);
                        eventLoginData.hasScanLogin = MenuPageFactory.getHasScanLogin(grants);
                        //
                        PreferencesHelper.getInstance().saveUserData(eventLoginData);
                        //
                        if (!PushManager.getInstance().isPushTurnedOn(SensoroCityApplication.getInstance())) {
                            PushManager.getInstance().turnOnPush(SensoroCityApplication.getInstance());
                        }
                        openMain(eventLoginData);
                    } else {
                        getView().dismissProgressDialog();
                        getView().toastShort(mContext.getResources().getString(R.string.tips_user_info_error));
                    }
                }


                @Override
                public void onErrorMsg(int errorCode, String errorMsg) {
                    getView().dismissProgressDialog();
                    getView().toastShort(errorMsg);
                }
            });
        }
    }

    private void openMain(EventLoginData eventLoginData) {
        Intent mainIntent = new Intent();
        mainIntent.setClass(mContext, MainActivity.class);
        mainIntent.putExtra("eventLoginData", eventLoginData);
        getView().startAC(mainIntent);
        getView().finishAc();
    }

    /**
     * 保存账户名称
     *
     * @param username
     * @param pwd
     */
    private void saveLoginData(String username, String pwd) {
        String aes_pwd = AESUtil.encode(pwd);
        PreferencesHelper.getInstance().saveLoginNamePwd(username, aes_pwd);
    }

    @Override
    public void onDestroy() {

    }
}
