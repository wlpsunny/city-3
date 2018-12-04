package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.activity.SearchMerchantActivity;
import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.factory.UserPermissionFactory;
import com.sensoro.smartcity.imainviews.IMerchantSwitchActivityView;
import com.sensoro.smartcity.iwidget.IOnCreate;
import com.sensoro.smartcity.model.EventData;
import com.sensoro.smartcity.model.EventLoginData;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.DeviceMergeTypesInfo;
import com.sensoro.smartcity.server.bean.UserInfo;
import com.sensoro.smartcity.server.response.DevicesMergeTypesRsp;
import com.sensoro.smartcity.server.response.UserAccountControlRsp;
import com.sensoro.smartcity.server.response.UserAccountRsp;
import com.sensoro.smartcity.util.PreferencesHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MerchantSwitchActivityPresenter extends BasePresenter<IMerchantSwitchActivityView> implements Constants, IOnCreate {

    private final List<UserInfo> mUserInfoList = new ArrayList<>();
    private Activity mContext;
    private volatile int cur_page = 0;
    private EventLoginData eventLoginData = null;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        onCreate();
        EventLoginData eventLoginData = (EventLoginData) mContext.getIntent().getSerializableExtra(EXTRA_EVENT_LOGIN_DATA);
        if (eventLoginData != null) {
            getView().setCurrentNameAndPhone(eventLoginData.userName, eventLoginData.phone);
            getView().setCurrentStatusImageViewVisible(true);
            requestDataByDirection(DIRECTION_DOWN, true);
        }
    }

    public void requestDataByDirection(int direction, boolean isForce) {
        if (isForce) {
            getView().showProgressDialog();
        }
        switch (direction) {
            case DIRECTION_DOWN:
                cur_page = 0;
                RetrofitServiceHelper.INSTANCE.getUserAccountList(null, null, cur_page * 20, 20).subscribeOn(Schedulers.io()).observeOn
                        (AndroidSchedulers.mainThread()).subscribe(new CityObserver<UserAccountRsp>(this) {


                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                        getView().onPullRefreshComplete();
                    }

                    @Override
                    public void onCompleted(UserAccountRsp userAccountRsp) {
                        List<UserInfo> list = userAccountRsp.getData();
                        if (list == null) {
                            mUserInfoList.clear();
                        } else {
                            mUserInfoList.clear();
                            mUserInfoList.addAll(list);
                        }
                        getView().updateAdapterUserInfo(mUserInfoList);
                        getView().showSeperatorView(mUserInfoList.size() != 0);
                        getView().dismissProgressDialog();
                        getView().onPullRefreshComplete();
                    }
                });
                break;
            case DIRECTION_UP:
                cur_page++;
                RetrofitServiceHelper.INSTANCE.getUserAccountList(null, null, cur_page * 20, 20).subscribeOn(Schedulers.io()).observeOn
                        (AndroidSchedulers.mainThread()).subscribe(new CityObserver<UserAccountRsp>(this) {

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        cur_page--;
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                        getView().onPullRefreshComplete();
                    }

                    @Override
                    public void onCompleted(UserAccountRsp userAccountRsp) {
                        List<UserInfo> list = userAccountRsp.getData();
                        if (list == null || list.size() == 0) {
                            cur_page--;
                            getView().toastShort(mContext.getString(R.string.no_more_data));
                            getView().showSeperatorView(false);
                        } else {
                            mUserInfoList.addAll(list);
                            getView().updateAdapterUserInfo(mUserInfoList);
                            getView().showSeperatorView(true);
                        }
                        getView().dismissProgressDialog();
                        getView().onPullRefreshComplete();
                    }
                });
                break;
            default:
                break;
        }
    }

    private void doAccountSwitch(String uid) {
        getView().showProgressDialog();
        eventLoginData = null;
        final String phoneId = PreferencesHelper.getInstance().getUserData().phoneId;
        RetrofitServiceHelper.INSTANCE.doAccountControl(uid, phoneId).subscribeOn(Schedulers.io()).flatMap(new Func1<UserAccountControlRsp, Observable<DevicesMergeTypesRsp>>() {
            @Override
            public Observable<DevicesMergeTypesRsp> call(UserAccountControlRsp userAccountControlRsp) {
                UserInfo userInfo = userAccountControlRsp.getData();
                RetrofitServiceHelper.INSTANCE.saveSessionId(userInfo.getSessionID());
                //
                eventLoginData = UserPermissionFactory.createLoginData(userInfo, phoneId);
                //
                return RetrofitServiceHelper.INSTANCE.getDevicesMergeTypes();
            }
        }).doOnNext(new Action1<DevicesMergeTypesRsp>() {
            @Override
            public void call(DevicesMergeTypesRsp devicesMergeTypesRsp) {
                DeviceMergeTypesInfo data = devicesMergeTypesRsp.getData();
                PreferencesHelper.getInstance().saveLocalDevicesMergeTypes(data);
                //
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<DevicesMergeTypesRsp>(this) {
            @Override
            public void onCompleted(DevicesMergeTypesRsp devicesMergeTypesRsp) {
                EventData eventData = new EventData();
                eventData.code = EVENT_DATA_SEARCH_MERCHANT;
                eventData.data = eventLoginData;
                EventBus.getDefault().post(eventData);
                getView().dismissProgressDialog();
                getView().finishAc();
            }


            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);
            }
        });
    }

    public void clickItem(UserInfo userInfo) {
        if (!PreferencesHelper.getInstance().getUserData().hasMerchantChange) {
            getView().toastShort(mContext.getString(R.string.merchant_has_no_change_permission));
            return;
        }
        if (!userInfo.isStop()) {
//            getView().setAdapterSelectedIndex(position);
//            mMerchantAdapter.setSelectedIndex(position);
//            mMerchantAdapter.notifyDataSetChanged();
//            getView().updateAdapterUserInfo(mUserInfoList);
//            getView().setCurrentStatusImageViewVisible(false);
//            mCurrentStatusImageView.setVisibility(View.GONE);
            String uid = userInfo.get_id();
            doAccountSwitch(uid);
        } else {
            getView().toastShort(mContext.getString(R.string.account_has_been_disabled));
        }
    }

    public void startToSearchAC() {
        Intent searchIntent = new Intent(mContext, SearchMerchantActivity.class);
        searchIntent.putExtra("phone_id", PreferencesHelper.getInstance().getUserData().phoneId);
        if (!TextUtils.isEmpty(PreferencesHelper.getInstance().getUserData().userName)) {
            searchIntent.putExtra("user_name", PreferencesHelper.getInstance().getUserData().userName);
        }
        if (!TextUtils.isEmpty(PreferencesHelper.getInstance().getUserData().phone)) {
            searchIntent.putExtra("user_phone", PreferencesHelper.getInstance().getUserData().phone);
        }
        getView().startAC(searchIntent);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        mUserInfoList.clear();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData eventData) {
        //TODO 可以修改以此种方式传递，方便管理
        int code = eventData.code;
//        Object data = eventData.data;
        switch (code) {
            case EVENT_DATA_SEARCH_MERCHANT:
                getView().finishAc();
                break;
        }
//        LogUtils.loge(this, eventData.toString());
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }
}
