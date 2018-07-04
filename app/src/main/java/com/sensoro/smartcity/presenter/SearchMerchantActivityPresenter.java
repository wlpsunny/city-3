package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.sensoro.smartcity.base.BasePresenter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.ISearchMerchantActivityView;
import com.sensoro.smartcity.server.RetrofitServiceHelper;
import com.sensoro.smartcity.server.bean.UserInfo;
import com.sensoro.smartcity.server.CityObserver;
import com.sensoro.smartcity.server.response.ResponseBase;
import com.sensoro.smartcity.server.response.UserAccountControlRsp;
import com.sensoro.smartcity.server.response.UserAccountRsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchMerchantActivityPresenter extends BasePresenter<ISearchMerchantActivityView> implements Constants {
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private final List<String> mHistoryKeywords = new ArrayList<>();
    private Activity mContext;
    private List<UserInfo> mUserInfoList = new ArrayList<>();
    private String phoneId = null;
    private String userName = null;
    private String phone = null;

    public List<String> getmHistoryKeywords() {
        return mHistoryKeywords;
    }

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        mPref = mContext.getSharedPreferences(PREFERENCE_MERCHANT_HISTORY, Activity.MODE_PRIVATE);
        mEditor = mPref.edit();
        phoneId = mContext.getIntent().getStringExtra("phone_id");
        userName = mContext.getIntent().getStringExtra("user_name");
        phone = mContext.getIntent().getStringExtra("user_phone");
        initSearchHistory();
    }


    private void initSearchHistory() {
        String history = mPref.getString(PREFERENCE_KEY_DEVICE, "");
        if (!TextUtils.isEmpty(history)) {
            mHistoryKeywords.clear();
            mHistoryKeywords.addAll(Arrays.asList(history.split(",")));
        }
        getView().setSearchHistoryLayoutVisible(mHistoryKeywords.size() > 0);
        getView().setCurrentNameAndPhone(userName, phone);
        getView().setCurrentStatusImageViewVisible(true);
    }

    public void save(String text) {
        String oldText = mPref.getString(PREFERENCE_KEY_DEVICE, "");
        if (!TextUtils.isEmpty(text)) {
            if (mHistoryKeywords.contains(text)) {
                mHistoryKeywords.clear();
                for (String o : oldText.split(",")) {
                    if (!o.equalsIgnoreCase(text)) {
                        mHistoryKeywords.add(o);
                    }
                }
                mHistoryKeywords.add(0, text);
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < mHistoryKeywords.size(); i++) {
                    if (i == (mHistoryKeywords.size() - 1)) {
                        stringBuffer.append(mHistoryKeywords.get(i));
                    } else {
                        stringBuffer.append(mHistoryKeywords.get(i) + ",");
                    }
                }
                mEditor.putString(PREFERENCE_KEY_DEVICE, stringBuffer.toString());
                mEditor.commit();
            } else {
                mEditor.putString(PREFERENCE_KEY_DEVICE, text + "," + oldText);
                mEditor.commit();
                mHistoryKeywords.add(0, text);
            }
            getView().updateSearchHistory();
        }
    }

    public void requestData(String text) {
        getView().showProgressDialog();
        RetrofitServiceHelper.INSTANCE.getUserAccountList(text).subscribeOn(Schedulers.io()).observeOn
                (AndroidSchedulers.mainThread()).subscribe(new CityObserver<UserAccountRsp>() {


            @Override
            public void onCompleted() {
                getView().dismissProgressDialog();
            }

            @Override
            public void onNext(UserAccountRsp userAccountRsp) {
                refreshUI(userAccountRsp);
//                List<UserInfo> list = userAccountRsp.getData();
//                if (list.size() == 0) {
//                    getView().setTipsLinearLayoutVisible(true);
//                } else {
//                    Intent data = new Intent();
//                    data.putExtra(EXTRA_MERCHANT_INFO, userAccountRsp);
//                    getView().setIntentResult(RESULT_CODE_CHANGE_MERCHANT, data);
//                    getView().finishAc();
//                }
            }

            @Override
            public void onErrorMsg(int errorCode,String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);
            }
        });
    }

    public void refreshUI(UserAccountRsp userAccountRsp) {
        List<UserInfo> list = userAccountRsp.getData();
        if (list.size() > 0) {
            mUserInfoList.clear();
            mUserInfoList.addAll(list);
            getView().updateMerchantInfo();
            getView().setSearchHistoryLayoutVisible(false);
            getView().setLlMerchantItemViewVisible(true);
        } else {
            getView().setTipsLinearLayoutVisible(true);
            getView().setLlMerchantItemViewVisible(false);
            getView().setSearchHistoryLayoutVisible(false);
        }

//        getView().setAdapterSelectedIndex(-1);
//        getView().updateAdapterUserInfo(list);
//        getView().showSeperatorBottomView(list.size() != 0);
    }

    public void cleanHistory() {
        mEditor.clear();
        mHistoryKeywords.clear();
        mEditor.commit();
        getView().updateSearchHistory();
        getView().setSearchHistoryLayoutVisible(false);
    }

    public List<UserInfo> getUserInfoList() {
        return mUserInfoList;
    }


    private void doAccountSwitch(String uid) {
        getView().showProgressDialog();
        RetrofitServiceHelper.INSTANCE.doAccountControl(uid, phoneId).subscribeOn(Schedulers.io()).observeOn
                (AndroidSchedulers.mainThread()).subscribe(new CityObserver<UserAccountControlRsp>() {
            @Override
            public void onCompleted() {
                getView().dismissProgressDialog();
            }

            @Override
            public void onNext(UserAccountControlRsp userAccountControlRsp) {
                if (userAccountControlRsp.getErrcode() == ResponseBase.CODE_SUCCESS) {
                    String sessionID = userAccountControlRsp.getData().getSessionID();
                    RetrofitServiceHelper.INSTANCE.setSessionId(sessionID);
                    String nickname = userAccountControlRsp.getData().getNickname();
                    String phone = userAccountControlRsp.getData().getContacts();
                    String roles = userAccountControlRsp.getData().getRoles();
                    String isSpecific = userAccountControlRsp.getData().getIsSpecific();
                    //
                    Intent data = new Intent();
                    data.putExtra("nickname", nickname);
                    if (!TextUtils.isEmpty(phone)) {
                        data.putExtra("phone", phone);
                    }
                    data.putExtra("roles", roles);
                    data.putExtra("isSpecific", isSpecific);
                    getView().setIntentResult(RESULT_CODE_SEARCH_MERCHANT, data);
//                    EventBus.getDefault().post(data);
                    getView().finishAc();
                } else {
                    getView().toastShort(userAccountControlRsp.getErrmsg());
                }
            }

            @Override
            public void onErrorMsg(int errorCode,String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);
            }
        });
    }

    public void clickItem(int position) {
        if (!mUserInfoList.get(position).isStop()) {
//            getView().setAdapterSelectedIndex(position);
//            mMerchantAdapter.setSelectedIndex(position);
//            mMerchantAdapter.notifyDataSetChanged();
            getView().updateMerchantInfo();
//            mCurrentStatusImageView.setVisibility(View.GONE);
            String uid = mUserInfoList.get(position).get_id();
            doAccountSwitch(uid);
        } else {
            getView().toastShort("账户已停用");
        }
    }
}
