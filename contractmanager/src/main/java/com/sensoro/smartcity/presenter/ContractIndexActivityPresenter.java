package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baidu.ocr.sdk.model.Word;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.sensoro.common.base.BaseApplication;
import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.iwidget.IOnCreate;
import com.sensoro.common.model.EventData;
import com.sensoro.common.server.RetrofitServiceHelper;
import com.sensoro.common.utils.FileUtil;
import com.sensoro.common.utils.LogUtils;
import com.sensoro.smartcity.activity.ContractServiceActivity;
import com.sensoro.smartcity.imainviews.IContractIndexActivityView;
import com.sensoro.smartcity.model.BusinessLicenseData;
import com.sensoro.smartcity.push.RecognizeService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class ContractIndexActivityPresenter extends BasePresenter<IContractIndexActivityView> implements IOnCreate {
    private Activity mContext;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        onCreate();
    }

    public void startLicense() {
        if (!BaseApplication.getInstance().hasGotToken) {
            return;
        }
        Intent intent = new Intent(mContext, CameraActivity.class);
        String absolutePath = FileUtil.getSaveFile(mContext.getApplicationContext()).getAbsolutePath();
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                absolutePath);
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        if (isAttachedView()) {
            getView().startACForResult(intent, Constants.REQUEST_CODE_BUSINESS_LICENSE);
        }
    }

    public void startPerson() {
        if (!BaseApplication.getInstance().hasGotToken) {
            return;
        }
        Intent intent = new Intent(mContext, CameraActivity.class);
        String absolutePath = FileUtil.getSaveFile(mContext.getApplication()).getAbsolutePath();
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                absolutePath);
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
        if (isAttachedView()) {
            getView().startACForResult(intent, Constants.REQUEST_CODE_CAMERA);
        }
    }

    public void startManual() {
        Intent intent = new Intent(mContext, ContractServiceActivity.class);
        intent.putExtra(Constants.EXTRA_CONTRACT_TYPE, 3);
        if (isAttachedView()) {
            getView().startAC(intent);
        }

    }

    public void handActivityResult(int requestCode, int resultCode, Intent data) {
        // 识别成功回调，营业执照识别
        if (requestCode == Constants.REQUEST_CODE_BUSINESS_LICENSE && resultCode == Activity.RESULT_OK) {
            if (isAttachedView()) {
                getView().showProgressDialog();
            }
            try {
                RecognizeService.recBusinessLicense(mContext, FileUtil.getSaveFile(mContext.getApplicationContext())
                                .getAbsolutePath(),
                        new RecognizeService.ServiceListener() {
                            @Override
                            public void onResult(final String result) {
                                if (isAttachedView()) {
                                    getView().dismissProgressDialog();
                                }
                                String 单位名称 = "无";
                                String 地址 = "无";
                                String 成立日期 = "无";
                                String 有效期 = "无";
                                String 法人 = "无";
                                String 社会信用代码 = "无";
                                String 证件编号 = "无";
                                try {
                                    BusinessLicenseData businessLicenseData = RetrofitServiceHelper.getInstance()
                                            .getGson()
                                            .fromJson(result, BusinessLicenseData.class);
                                    BusinessLicenseData.WordsResultBean words_result = businessLicenseData
                                            .getWords_result();
                                    //
                                    if (words_result != null) {
                                        BusinessLicenseData.WordsResultBean.单位名称Bean words_result单位名称 = words_result
                                                .get单位名称();
                                        if (words_result单位名称 != null) {
                                            单位名称 = words_result单位名称.getWords();
                                        }
                                        BusinessLicenseData.WordsResultBean.地址Bean words_result地址 = words_result
                                                .get地址();
                                        if (words_result地址 != null) {
                                            地址 = words_result地址.getWords();
                                        }
                                        BusinessLicenseData.WordsResultBean.成立日期Bean words_result成立日期 = words_result
                                                .get成立日期();
                                        if (words_result成立日期 != null) {
                                            成立日期 = words_result成立日期.getWords();
                                        }
                                        BusinessLicenseData.WordsResultBean.有效期Bean words_result有效期 = words_result
                                                .get有效期();
                                        if (words_result有效期 != null) {
                                            有效期 = words_result有效期.getWords();
                                        }
                                        BusinessLicenseData.WordsResultBean.法人Bean words_result法人 = words_result
                                                .get法人();
                                        if (words_result法人 != null) {
                                            法人 = words_result法人.getWords();
                                        }
                                        BusinessLicenseData.WordsResultBean.社会信用代码Bean words_result社会信用代码 =
                                                words_result
                                                        .get社会信用代码();
                                        if (words_result社会信用代码 != null) {
                                            社会信用代码 = words_result社会信用代码.getWords();
                                        }
                                        BusinessLicenseData.WordsResultBean.证件编号Bean words_result证件编号 = words_result
                                                .get证件编号();
                                        if (words_result证件编号 != null) {
                                            证件编号 = words_result证件编号.getWords();
                                        }
                                    }

                                    try {
                                        LogUtils.loge(this, businessLicenseData.toString());
                                    } catch (Throwable throwable) {
                                        throwable.printStackTrace();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                startServiceByLicense(单位名称, 地址, 成立日期, 有效期, 法人, 社会信用代码, 证件编号);
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
                if (isAttachedView()) {
                    getView().dismissProgressDialog();
                    getView().toastShort("读取失败请重试");
                }
            }

        }
        if (requestCode == Constants.REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (isAttachedView()) {
                    getView().showProgressDialog();
                }
                try {
                    String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                    String filePath = FileUtil.getSaveFile(mContext.getApplicationContext()).getAbsolutePath();
                    if (!TextUtils.isEmpty(contentType)) {
                        if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                            recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, filePath);
                        } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
                            recIDCard(IDCardParams.ID_CARD_SIDE_BACK, filePath);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (isAttachedView()) {
                        getView().dismissProgressDialog();
                        getView().toastShort("读取失败请重试");
                    }
                }
            }
        }
    }

    private void recIDCard(String idCardSide, String filePath) {
        IDCardParams param = new IDCardParams();
        param.setImageFile(new File(filePath));
        // 设置身份证正反面
        param.setIdCardSide(idCardSide);
        // 设置方向检测
        param.setDetectDirection(true);
        // 设置图像参数压缩质量0-100, 越大图像质量越好但是请求时间越长。 不设置则默认值为20
        param.setImageQuality(20);

        OCR.getInstance(mContext).recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(IDCardResult result) {
                if (isAttachedView()) {
                    getView().dismissProgressDialog();
                }
                String name = "无";
                String idNumber = "无";
                String sex = "无";
                String address = "无";
                try {
                    if (result != null) {
                        try {
                            LogUtils.loge(this, result.toString());
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        Word resultName = result.getName();
                        if (resultName != null) {
                            name = resultName.getWords();
                        }
                        Word resultGender = result.getGender();
                        if (resultGender != null) {
                            sex = resultGender.getWords();
                        }
                        Word resultIdNumber = result.getIdNumber();
                        if (resultIdNumber != null) {
                            idNumber = resultIdNumber.getWords();
                        }
                        Word resultAddress = result.getAddress();
                        if (resultAddress != null) {
                            address = resultAddress.getWords();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startServiceByNumber(name, sex, idNumber, address);
            }

            @Override
            public void onError(OCRError error) {
                if (isAttachedView()) {
                    getView().dismissProgressDialog();
                    getView().toastShort("身份证识别失败：" + error.getMessage());
                }
                try {
                    LogUtils.loge(this, error.getMessage());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    private void startServiceByLicense(String 单位名称, String 地址, String 成立日期, String 有效期, String 法人, String 社会信用代码,
                                       String 证件编号) {
        Intent intent = new Intent();
        intent.setClass(mContext, ContractServiceActivity.class);
        intent.putExtra(Constants.EXTRA_CONTRACT_TYPE, 1);
        intent.putExtra("legal_person", 法人);
        intent.putExtra("company_name", 单位名称);
        intent.putExtra("credit_code", 社会信用代码);
        intent.putExtra("registration_number", 证件编号);
        intent.putExtra("address", 地址);
        intent.putExtra("validity_period", 有效期);
        getView().startAC(intent);
    }

    private void startServiceByNumber(String personName, String sex, String idNumber, String address) {
        Intent intent = new Intent();
        intent.setClass(mContext, ContractServiceActivity.class);
        intent.putExtra(Constants.EXTRA_CONTRACT_TYPE, 2);
        if (TextUtils.isEmpty(personName)) {
            intent.putExtra("person_name", "无");
        } else {
            intent.putExtra("person_name", personName);
        }
        if (TextUtils.isEmpty(sex)) {
            intent.putExtra("sex", "无");
        } else {
            intent.putExtra("sex", sex);
        }
        if (TextUtils.isEmpty(idNumber)) {
            intent.putExtra("id_number", "无");
        } else {
            intent.putExtra("id_number", idNumber);
        }
        if (TextUtils.isEmpty(address)) {
            intent.putExtra("address", "无");
        } else {
            intent.putExtra("address", address);
        }
        getView().startAC(intent);
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData eventData) {
        int code = eventData.code;
        switch (code) {
            case Constants.EVENT_DATA_FINISH_CODE:
                getView().finishAc();
                break;
        }
//        LogUtils.loge(this, eventData.toString());
    }
}
