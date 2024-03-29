package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.helper.PreferencesHelper;
import com.sensoro.common.manger.ActivityTaskManager;
import com.sensoro.common.model.EventLoginData;
import com.sensoro.common.server.CityObserver;
import com.sensoro.common.server.RetrofitServiceHelper;
import com.sensoro.common.server.bean.UserInfo;
import com.sensoro.common.server.response.ResponseResult;
import com.sensoro.smartcity.activity.LoginActivity;
import com.sensoro.common.factory.UserPermissionFactory;
import com.sensoro.smartcity.imainviews.IPermissionChangeActivityView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PermissionChangeActivityPresenter extends BasePresenter<IPermissionChangeActivityView> {
    private Activity mContext;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
    }

    @Override
    public void onDestroy() {

    }

    public void doGetNewPermission() {
        getView().showProgressDialog();
        RetrofitServiceHelper.getInstance().getPermissionChangeInfo().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<UserInfo>>(this) {
            @Override
            public void onCompleted(ResponseResult<UserInfo> loginRsp) {
                EventLoginData userData = PreferencesHelper.getInstance().getUserData();
                UserInfo userInfo = loginRsp.getData();
                EventLoginData loginData = UserPermissionFactory.createLoginData(userInfo, userData.phoneId);
                PreferencesHelper.getInstance().saveUserData(loginData);
                getView().dismissProgressDialog();
                getView().finishAc();
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().toastShort(errorMsg);
                getView().dismissProgressDialog();
            }
        });


    }

    public void doReLogin() {
        getView().showProgressDialog();
        RetrofitServiceHelper.getInstance().cancelAllRsp();
        RetrofitServiceHelper.getInstance().clearLoginDataSessionId();
        Intent loginIntent = new Intent();
        loginIntent.setClass(mContext, LoginActivity.class);
        getView().startAC(loginIntent);
        ActivityTaskManager.getInstance().finishAllActivity();
    }
}
