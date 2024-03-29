package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.constant.ContractOrderInfo;
import com.sensoro.common.helper.PreferencesHelper;
import com.sensoro.common.model.EventData;
import com.sensoro.common.server.CityObserver;
import com.sensoro.common.server.RetrofitServiceHelper;
import com.sensoro.common.server.bean.ContractListInfo;
import com.sensoro.common.server.response.ResponseResult;
import com.sensoro.common.utils.DateUtil;
import com.sensoro.contractmanager.R;
import com.sensoro.smartcity.activity.ContractEditorActivity;
import com.sensoro.smartcity.activity.ContractPreviewActivity;
import com.sensoro.smartcity.activity.ContractResultActivity;
import com.sensoro.smartcity.imainviews.IContractDetailView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ContractDetailPresenter extends BasePresenter<IContractDetailView> {
    private Activity mActivity;
    private ContractListInfo mContractInfo;
    private int contractId;

    @Override
    public void initData(Context context) {
        mActivity = (Activity) context;
        contractId = mActivity.getIntent().getIntExtra(Constants.EXTRA_CONTRACT_ID, 0);
        EventBus.getDefault().register(this);
        if (contractId == 0) {
            getView().toastShort(mActivity.getString(R.string.not_obtain_contract_id));
        } else {
            requestContractInfo(contractId);
        }
    }

    private void requestContractInfo(int id) {
        getView().showProgressDialog();
        RetrofitServiceHelper.getInstance().getContractInfo(String.valueOf(id)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CityObserver<ResponseResult<ContractListInfo>>(this) {
                    @Override
                    public void onCompleted(ResponseResult<ContractListInfo> responseBase) {
                        mContractInfo = responseBase.getData();
                        if (mContractInfo != null) {
                            getView().setContractNumber(String.format(Locale.ROOT, "%s%s", mActivity.getString(R.string.contract_number), mContractInfo.getContract_number()));
                            boolean confirmed = mContractInfo.isConfirmed();
                            getView().setSignStatus(confirmed);
                            if (!confirmed && PreferencesHelper.getInstance().getUserData().hasContractModify) {
                                getView().setContractEditVisible(true);
                            } else {
                                getView().setContractEditVisible(false);
                            }
                            getView().setCustomerEnterpriseName(mContractInfo.getCustomer_enterprise_name());
                            getView().setCustomerName(mContractInfo.getCustomer_name());
                            getView().setCustomerPhone(mContractInfo.getCustomer_phone());
                            getView().setCustomerAddress(mContractInfo.getCustomer_address());
                            getView().setPlaceType(mContractInfo.getPlace_type());
                            getView().setServerAge(String.format(Locale.CHINESE, "%d%s", mContractInfo.getServiceTime(), mActivity.getString(R.string.year)));
                            getView().setPeriodAge(String.format(Locale.CHINESE, "%d%s", mContractInfo.getPayTimes(), mActivity.getString(R.string.year)));
                            getView().setFirstAge(String.format(Locale.CHINESE, "%d%s", mContractInfo.getFirstPayTimes(), mActivity.getString(R.string.year)));
                            switch (mContractInfo.getContract_type()) {
                                case 1:
                                    getView().setCardIdOrEnterpriseId(mContractInfo.getEnterprise_card_id());
                                    break;
                                case 2:
                                    getView().setCardIdOrEnterpriseId(mContractInfo.getCard_id());
                                    break;
                            }
                            getView().setTipText(mContractInfo.getContract_type());

                            if (confirmed) {
                                getView().setContractTime(String.format(Locale.ROOT, "%s%s", mActivity.getString(R.string.contract_info_contract_sign_time),
                                        DateUtil.getChineseCalendar(mContractInfo.getConfirmTimestamp())));
                                getView().setContractCreateTime(String.format(Locale.ROOT, "%s%s", mActivity.getString(R.string.contract_info_contract_created_time),
                                        DateUtil.getChineseCalendar(mContractInfo.getCreatedAtTimestamp())));
                                ContractListInfo.Order order = mContractInfo.getOrder();
                                if (order != null) {
                                    getView().setContractOrder(ContractOrderInfo.SUCCESS.equals(order.getTrade_state()), String.format(Locale.ROOT, "%s%s", mActivity.getString(R.string.contract_info_contract_pay_time),
                                            DateUtil.getChineseCalendar(order.getPayTimestamp())));
                                }

                            } else {
                                getView().setContractTime(String.format(Locale.ROOT, "%s%s", mActivity.getString(R.string.contract_info_contract_created_time),
                                        DateUtil.getChineseCalendar(mContractInfo.getCreatedAtTimestamp())));
                            }

                            getView().updateContractTemplateAdapterInfo(mContractInfo.getDevices());
                        } else {
                            getView().toastShort(mActivity.getString(R.string.not_obtain_contract_info));
                        }
                        getView().dismissProgressDialog();
                    }

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        getView().dismissProgressDialog();
                        getView().toastShort(errorMsg);
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData eventData) {
        int code = eventData.code;
        Object data = eventData.data;
        switch (code) {
            case Constants.EVENT_DATA__CONTRACT_EDIT_REFRESH_CODE:
                requestContractInfo((int) data);
                break;
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    public void doEditContract() {
        if (mContractInfo != null) {
            Intent intent = new Intent(mActivity, ContractEditorActivity.class);
            intent.putExtra(Constants.EXTRA_CONTRACT_INFO, mContractInfo);
            intent.putExtra(Constants.EXTRA_CONTRACT_ORIGIN_TYPE, 2);
            getView().startAC(intent);
        }
    }

    public void doViewContractQrCode() {
        if (mContractInfo != null) {
            if (mContractInfo == null) {
                getView().toastShort(mActivity.getString(R.string.not_obtain_contract_info));
                return;
            }
            if (mContractInfo.isConfirmed()) {
                doPreviewActivity();
                return;
            }
            int id = mContractInfo.getId();
            if (id == 0) {
                getView().toastShort(mActivity.getString(R.string.contract_id_failed));
                return;
            }
            Intent intent = new Intent();
            final String code = Constants.CONTRACT_WE_CHAT_BASE_URL + id;

            intent.putExtra(Constants.EXTRA_CONTRACT_ID_QRCODE, code);
            intent.setClass(mActivity, ContractResultActivity.class);
            getView().startAC(intent);
        }

    }

    public void doPreviewActivity() {
        if (mContractInfo != null) {
            if (TextUtils.isEmpty(mContractInfo.getFdd_viewpdf_url())) {
                getView().toastShort(mActivity.getString(R.string.preview_contract_failed));
                return;
            }
            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_CONTRACT_PREVIEW_URL, mContractInfo.getFdd_viewpdf_url());
            intent.setClass(mActivity, ContractPreviewActivity.class);
            getView().startAC(intent);
        }
    }
}
