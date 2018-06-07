package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.igexin.sdk.PushManager;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.activity.MainActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.ILoginView;
import com.sensoro.smartcity.push.SensoroPushIntentService;
import com.sensoro.smartcity.push.SensoroPushService;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.response.CityObserver;
import com.sensoro.smartcity.server.response.LoginRsp;
import com.sensoro.smartcity.server.response.ResponseBase;
import com.sensoro.smartcity.util.AESUtil;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public class LoginPresenter extends BasePresenter<ILoginView> implements Constants {
    private Activity mContext;

    private void readLoginData() {
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_LOGIN, Context
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
        try {
            ApplicationInfo appInfo = mContext.getPackageManager()
                    .getApplicationInfo(mContext.getPackageName(),
                            PackageManager.GET_META_DATA);
            String msg = appInfo.metaData.getString("InstallChannel");
            if (msg.equalsIgnoreCase("Mocha")) {
                RetrofitServiceHelper.INSTANCE.setDemoTypeBaseUrl(true);
            } else if (msg.equalsIgnoreCase("Master")) {
                RetrofitServiceHelper.INSTANCE.setDemoTypeBaseUrl(false);
            } else {
                RetrofitServiceHelper.INSTANCE.setDemoTypeBaseUrl(true);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_SCOPE, Context
                .MODE_PRIVATE);
        try {
            boolean isDemo = sp.getBoolean(PREFERENCE_KEY_URL, false);
            RetrofitServiceHelper.INSTANCE.setDemoTypeBaseUrl(isDemo);
            if (isDemo) {
                getView().setLogButtonState(1);
            } else {
                getView().setLogButtonState(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void saveScopeData(int which) {
        getView().setLogButtonState(which);
        RetrofitServiceHelper.INSTANCE.setDemoTypeBaseUrl(which != 0);
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_SCOPE, Context
                .MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        RetrofitServiceHelper.INSTANCE.setDemoTypeBaseUrl(which != 0);
        editor.putBoolean(PREFERENCE_KEY_URL, which != 0);
        editor.commit();
    }

    public void login(final String account, final String pwd) {
        if (TextUtils.isEmpty(account)) {
            getView().toastShort(R.string.tips_username_empty);
        } else if (TextUtils.isEmpty(pwd)) {
            getView().toastShort(R.string.tips_login_pwd_empty);
        } else {
            final String phoneId = PushManager.getInstance().getClientid(SensoroCityApplication.getInstance());
            getView().showProgressDialog();
            RetrofitServiceHelper.INSTANCE.login(account, pwd, phoneId).subscribeOn
                    (Schedulers
                            .io()).doOnNext(new Action1<LoginRsp>() {
                @Override
                public void call(LoginRsp loginRsp) {
                    String sessionID = loginRsp.getData().getSessionID();
//                    NetUtils.INSTANCE.setSessionId(sessionID);
                    RetrofitServiceHelper.INSTANCE.setSessionId(sessionID);
                    saveLoginData(account, pwd);
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<LoginRsp>() {
                @Override
                public void onCompleted() {
                    getView().dismissProgressDialog();
                    getView().finishAc();
                }

                @Override
                public void onNext(LoginRsp loginRsp) {
                    if (loginRsp.getErrcode() == ResponseBase.CODE_SUCCESS) {
                        String isSpecific = loginRsp.getData().getIsSpecific();
                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.putExtra(EXTRA_USER_ID, loginRsp.getData().get_id());
                        intent.putExtra(EXTRA_USER_NAME, loginRsp.getData().getNickname());
                        intent.putExtra(EXTRA_PHONE, loginRsp.getData().getPhone());
                        intent.putExtra(EXTRA_CHARACTER, loginRsp.getData().getCharacter());
                        intent.putExtra(EXTRA_USER_ROLES, loginRsp.getData().getRoles());
                        intent.putExtra(EXTRA_IS_SPECIFIC, isSpecific);
                        intent.putExtra(EXTRA_PHONE_ID, phoneId);
                        getView().startAC(intent);
                    } else {
                        getView().toastShort(R.string.tips_user_info_error);
                    }
                }


                @Override
                public void onErrorMsg(String errorMsg) {
                    getView().dismissProgressDialog();
                    getView().toastShort(errorMsg);
                }
            });
        }
    }

    public void initPushSDK() {
        PushManager.getInstance().initialize(SensoroCityApplication.getInstance(), SensoroPushService.class);
        // 注册 intentService 后 PushDemoReceiver 无效, sdk 会使用 DemoIntentService 传递数据,
        // AndroidManifest 对应保留一个即可(如果注册 DemoIntentService, 可以去掉 PushDemoReceiver, 如果注册了
        // IntentService, 必须在 AndroidManifest 中声明)
        PushManager.getInstance().registerPushIntentService(SensoroCityApplication.getInstance(),
                SensoroPushIntentService
                        .class);
    }

    /**
     * 保存账户名称
     *
     * @param username
     * @param pwd
     */
    private void saveLoginData(String username, String pwd) {
        String aes_pwd = AESUtil.encode(pwd);
        SharedPreferences sp = mContext.getSharedPreferences(PREFERENCE_LOGIN, Context
                .MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFERENCE_KEY_NAME, username);
        editor.putString(PREFERENCE_KEY_PASSWORD, aes_pwd);

        editor.commit();
    }

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        readLoginData();
        initSeverUrl();
    }

}