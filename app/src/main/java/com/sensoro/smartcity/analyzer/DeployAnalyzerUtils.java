package com.sensoro.smartcity.analyzer;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.constant.ARouterConstants;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.helper.PreferencesHelper;
import com.sensoro.common.model.DeployAnalyzerModel;
import com.sensoro.common.model.DeployContactModel;
import com.sensoro.common.model.DeployResultModel;
import com.sensoro.common.model.DeviceNotificationBean;
import com.sensoro.common.server.CityObserver;
import com.sensoro.common.server.RetrofitServiceHelper;
import com.sensoro.common.server.bean.AlarmInfo;
import com.sensoro.common.server.bean.DeployStationInfo;
import com.sensoro.common.server.bean.DeviceCameraDetailInfo;
import com.sensoro.common.server.bean.DeviceInfo;
import com.sensoro.common.server.bean.DeviceTypeStyles;
import com.sensoro.common.server.bean.ForestGatewayDeployInfo;
import com.sensoro.common.server.bean.InspectionIndexTaskInfo;
import com.sensoro.common.server.bean.InspectionTaskDeviceDetail;
import com.sensoro.common.server.bean.InspectionTaskDeviceDetailModel;
import com.sensoro.common.server.bean.NamePlateInfo;
import com.sensoro.common.server.response.ResponseResult;
import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.utils.LogUtils;
import com.sensoro.common.utils.WidgetUtil;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.activity.DeployCameraDetailActivity;
import com.sensoro.smartcity.activity.DeployForestCameraDetailActivity;
import com.sensoro.smartcity.activity.DeployMonitorDetailActivity;
import com.sensoro.smartcity.activity.DeployResultActivity;
import com.sensoro.smartcity.activity.ScanLoginResultActivity;
import com.sensoro.smartcity.activity.SignalCheckActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeployAnalyzerUtils {

    private DeployAnalyzerUtils() {
    }

    public static DeployAnalyzerUtils getInstance() {
        return DeployAnalyzerUtilsHolder.instance;
    }

    private static class DeployAnalyzerUtilsHolder {
        private static final DeployAnalyzerUtils instance = new DeployAnalyzerUtils();
    }


    /**
     * 铭牌扫码绑定传感器
     *
     * @param presenter
     * @param result
     * @param activity
     * @param nameplateId
     * @param listener
     */
    public void handlerDeployAnalyzerResult(int scanType, boolean needAssociate, BasePresenter presenter, final String result, final Activity activity, String nameplateId, final OnDeployAnalyzerListener listener) {
        if (TextUtils.isEmpty(result)) {
            listener.onError(0, null, activity.getResources().getString(R.string.please_re_scan_try_again));
            return;
        }
        String scanSerialNumber = parseResultMac(result);
        if (TextUtils.isEmpty(scanSerialNumber)) {
            listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
        } else {
            try {
                String[] strings = scanSerialNumber.split(" ");
                scanSerialNumber = strings[0];
                if (scanSerialNumber.length() >= 8 && scanSerialNumber.length() <= 32) {
                    //转换大写
                    String finalScanSerialNumber = scanSerialNumber.toUpperCase();
                    RetrofitServiceHelper.getInstance().getDeployDeviceDetail(finalScanSerialNumber, null, null).subscribeOn
                            (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(presenter) {
                        @Override
                        public void onErrorMsg(int errorCode, String errorMsg) {
                            if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                                listener.onError(errorCode, null, errorMsg);
                            } else if (errorCode == 4013101 || errorCode == 4000013) {
                                //TODO 不在账户下
                                //不在账户下
                                Intent intent = new Intent();
                                intent.setClass(activity, DeployResultActivity.class);
                                DeployResultModel deployResultModel = new DeployResultModel();
                                deployResultModel.scanType = scanType;
                                deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                deployResultModel.sn = finalScanSerialNumber;
                                intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                listener.onError(errorCode, intent, errorMsg);
                            } else {
                                //TODO 控制逻辑
                                Intent intent = new Intent();
                                intent.setClass(activity, DeployResultActivity.class);
                                DeployResultModel deployResultModel = new DeployResultModel();
                                deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_SCAN_FAILED;
                                deployResultModel.sn = finalScanSerialNumber;
                                deployResultModel.scanType = scanType;
                                deployResultModel.errorMsg = errorMsg;
                                intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                listener.onError(errorCode, intent, errorMsg);
                            }
                        }

                        @Override
                        public void onCompleted(ResponseResult<DeviceInfo> deployDeviceDetailRsp) {
                            DeviceInfo data = deployDeviceDetailRsp.getData();
                            if (data == null) {
                                //不在账户下
                                Intent intent = new Intent();
                                intent.setClass(activity, DeployResultActivity.class);
                                DeployResultModel deployResultModel = new DeployResultModel();
                                deployResultModel.scanType = scanType;
                                deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                deployResultModel.sn = finalScanSerialNumber;
                                intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                listener.onError(0, intent, null);
                            } else {
                                String sn = data.getSn();
                                if (TextUtils.isEmpty(sn)) {
                                    //拿不到sn认为为空对象
                                    //不在账户下
                                    Intent intent = new Intent();
                                    intent.setClass(activity, DeployResultActivity.class);
                                    DeployResultModel deployResultModel = new DeployResultModel();
                                    deployResultModel.scanType = scanType;
                                    deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                    deployResultModel.sn = finalScanSerialNumber;
                                    intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                    listener.onError(0, intent, null);
                                } else {
                                    //在账户下 即可关联
                                    if (needAssociate) {
                                        nameplateAssociateDevice(nameplateId, finalScanSerialNumber, listener, activity, presenter);
                                    } else {
                                        //目前只取需要的字段
                                        DeployAnalyzerModel deployAnalyzerModel = new DeployAnalyzerModel();
                                        deployAnalyzerModel.sn = sn;
                                        deployAnalyzerModel.deviceType = data.getDeviceType();
                                        deployAnalyzerModel.nameAndAddress = data.getName();
                                        Intent intent = new Intent();
                                        intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
                                        listener.onSuccess(intent);
                                    }
                                }
                            }

                        }

                    });
                } else {
                    listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                }
            } catch (Exception e) {
                listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
            }

        }
    }

    /**
     * 分析来源，返回扫描等结果
     *
     * @param presenter
     * @param scanType
     * @param result
     * @param activity
     * @param inspectionIndexTaskInfo
     * @param oldDeviceDetail
     * @param listener
     */
    public void handlerDeployAnalyzerResult(BasePresenter presenter, final int scanType, final String result, final Activity activity, InspectionIndexTaskInfo inspectionIndexTaskInfo, InspectionTaskDeviceDetail oldDeviceDetail, final OnDeployAnalyzerListener listener) {
        if (TextUtils.isEmpty(result)) {
            listener.onError(0, null, activity.getResources().getString(R.string.please_re_scan_try_again));
            return;

        }
        switch (scanType) {
            //搜索铭牌
            case Constants.EVENT_DATA_SEARCH_NAMEPLATE:
                if (result.startsWith("http")) {
                    try {
                        String nameplateId = result.substring(result.length() - 24);
                        handleNameplate(scanType, presenter, nameplateId, activity, listener);
                    } catch (Exception e) {
                        listener.onError(0, null, activity.getResources().getString(R.string.please_re_scan_try_again));
                    }

                } else {
                    listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                }
                break;
            //基站部署
            case Constants.TYPE_SCAN_DEPLOY_STATION:
                //铭牌部署扫码关联传感器
            case Constants.EVENT_DATA_ADD_SENSOR_FROM_DEPLOY:
                //设备部署
            case Constants.TYPE_SCAN_DEPLOY_DEVICE:
                //TODO 暂时去掉铭牌部署
//                if (result.startsWith("http")) {
//                    if (PreferencesHelper.getInstance().getUserData().hasNameplateDeploy) {
//                        try {
//                            String nameplateId = result.substring(result.lastIndexOf("/") + 1);
//                            //目前只要是以http开头的部署 暂认为为铭牌部署
//                            handleNameplate(Constants.TYPE_SCAN_NAMEPLATE_DEPLOY, presenter, nameplateId, activity, listener);
//                        } catch (Exception e) {
//                            listener.onError(0, null, activity.getResources().getString(R.string.please_re_scan_try_again));
//                        }
//                    } else {
//                        //无权限不在账户下
//                        Intent intent = new Intent();
//                        intent.setClass(activity, DeployResultActivity.class);
//                        DeployResultModel deployResultModel = new DeployResultModel();
//                        deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
//                        //
//                        String sn = result;
//                        try {
//                            sn = result.substring(result.lastIndexOf("/") + 1);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        deployResultModel.sn = sn;
//                        deployResultModel.scanType = Constants.TYPE_SCAN_NAMEPLATE_DEPLOY;
//                        intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
//                        listener.onError(0, intent, null);
//                    }
//                } else {
                String scanSerialNumber = parseResultMac(result);
                if (TextUtils.isEmpty(scanSerialNumber)) {
                    listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                    return;
                } else {
                    try {
                        String[] strings = scanSerialNumber.split(" ");
                        scanSerialNumber = strings[0];
                        if (scanSerialNumber.length() >= 8 && scanSerialNumber.length() <= 32) {
                            //大小写转换
                            scanSerialNumber = scanSerialNumber.toUpperCase();
                            handleDeployDeviceStation(presenter, scanSerialNumber, activity, listener);
                        } else {
                            listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                            return;
                        }
                    } catch (Exception e) {
                        listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                    }

                }
//                }

                break;
            case Constants.TYPE_SCAN_LOGIN:
                //登录
                if (TextUtils.isEmpty(result)) {
                    listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                    return;
                }
                handleScanLogin(presenter, result, activity, listener);
                break;
            case Constants.TYPE_SCAN_DEPLOY_INSPECTION_DEVICE_CHANGE:
            case Constants.TYPE_SCAN_DEPLOY_MALFUNCTION_DEVICE_CHANGE:
                //巡检/故障设备更换
                if (oldDeviceDetail == null) {
                    listener.onError(0, null, null);
                    return;
                }
                String scanNewDeviceSN = parseResultMac(result);
                if (TextUtils.isEmpty(scanNewDeviceSN)) {
                    listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                    return;
                } else {
                    if (scanNewDeviceSN.length() >= 8 && scanNewDeviceSN.length() <= 32) {
                        //大小写转换
                        scanNewDeviceSN = scanNewDeviceSN.toUpperCase();
                        String oldDeviceDetailSn = oldDeviceDetail.getSn();
                        if (scanNewDeviceSN.equalsIgnoreCase(oldDeviceDetailSn)) {
                            listener.onError(scanType, null, "请使用不同的设备进行更换");
                            return;
                        }
                        handleDeviceDeployChange(scanType, presenter, oldDeviceDetail, scanNewDeviceSN, activity, listener);
                    } else {
                        listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                        return;
                    }
                }
                break;
            case Constants.TYPE_SCAN_INSPECTION:
                if (inspectionIndexTaskInfo == null) {
                    listener.onError(0, null, null);
                    return;
                }
                //扫描巡检设备
                String scanInspectionDeviceSn = parseResultMac(result);
                if (TextUtils.isEmpty(scanInspectionDeviceSn)) {
                    listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                    return;
                } else {
                    if (scanInspectionDeviceSn.length() >= 8 && scanInspectionDeviceSn.length() <= 32) {
                        scanInspectionDeviceSn = scanInspectionDeviceSn.toLowerCase();
                        handleScanInspectionDevice(presenter, scanInspectionDeviceSn, inspectionIndexTaskInfo.getId(), activity, listener);
                    } else {
                        listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                        return;
                    }
                }
                break;
            case Constants.TYPE_SCAN_SIGNAL_CHECK:
                //信号测试
                String signalCheckNum = parseResultMac(result);
                if (TextUtils.isEmpty(signalCheckNum)) {
                    listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                    return;
                } else {
                    if (signalCheckNum.length() == 16) {
                        signalCheckNum = signalCheckNum.toUpperCase();
                        handleScanSignalCheck(presenter, signalCheckNum, activity, listener);
                    } else {
                        listener.onError(0, null, activity.getResources().getString(R.string.invalid_qr_code));
                        return;
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 铭牌扫码绑定传感器
     *
     * @param nameplateId
     * @param scanSerialNumber
     * @param listener
     * @param activity
     * @param presenter
     */

    private void nameplateAssociateDevice(String nameplateId, String scanSerialNumber, OnDeployAnalyzerListener listener, Activity activity, BasePresenter presenter) {

        ArrayList<NamePlateInfo> arrayList = new ArrayList<>();
        NamePlateInfo info = new NamePlateInfo();
        info.setSn(scanSerialNumber);
        arrayList.add(info);
        RetrofitServiceHelper.getInstance().doBindDevices(nameplateId, arrayList).subscribeOn
                (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<Integer>>(presenter) {
            @Override
            public void onCompleted(ResponseResult<Integer> result) {
                if (result.getData() > 0) {
                    listener.onSuccess(null);
                } else {
                    listener.onError(0, null, activity.getResources().getString(R.string.equipment_associated));

                }
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                listener.onError(0, null, errorMsg);

            }
        });


    }

    private void handleNameplate(int scanType, BasePresenter presenter, String nameplateId, Activity activity, OnDeployAnalyzerListener listener) {
        RetrofitServiceHelper.getInstance().getNameplateDetail(nameplateId, true).subscribeOn
                (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<NamePlateInfo>>(presenter) {
            @Override
            public void onCompleted(ResponseResult<NamePlateInfo> namePlateInfoResponseResult) {
                NamePlateInfo data = namePlateInfoResponseResult.getData();
                if (data == null) {
                    //查找新设备
                    Intent intent = new Intent();
                    intent.setClass(activity, DeployResultActivity.class);
                    DeployResultModel deployResultModel = new DeployResultModel();
                    deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                    deployResultModel.sn = nameplateId;
                    deployResultModel.scanType = scanType;
                    intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                    listener.onError(0, intent, null);
                } else {
                    String name = data.getName();
                    String nameplateId = data.get_id();
                    ArrayList<String> tags = data.getTags();
                    DeployAnalyzerModel deployAnalyzerModel = new DeployAnalyzerModel();
                    deployAnalyzerModel.nameAndAddress = name;
                    deployAnalyzerModel.sn = nameplateId;
                    deployAnalyzerModel.deployNameplateFlag = data.getDeployFlag();
                    if (tags != null && tags.size() > 0) {
                        deployAnalyzerModel.tagList.addAll(tags);
                    }
                    Intent intent = new Intent();
                    deployAnalyzerModel.deployType = scanType;
                    intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
                    intent.putExtra(ARouterConstants.AROUTER_PATH_NAMEPLATE, "path");
                    listener.onSuccess(intent);
                }
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                    listener.onError(errorCode, null, errorMsg);
                } else if (errorCode == 4013101 || errorCode == 4000013) {
                    //查找新设备
                    Intent intent = new Intent();
                    intent.setClass(activity, DeployResultActivity.class);
                    DeployResultModel deployResultModel = new DeployResultModel();
                    deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                    deployResultModel.sn = nameplateId;
                    deployResultModel.scanType = scanType;
                    intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                    listener.onError(errorCode, intent, errorMsg);
                } else {
                    //TODO 控制逻辑
                    Intent intent = new Intent();
                    intent.setClass(activity, DeployResultActivity.class);
                    DeployResultModel deployResultModel = new DeployResultModel();
                    deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_SCAN_FAILED;
                    deployResultModel.sn = nameplateId;
                    deployResultModel.scanType = scanType;
                    deployResultModel.errorMsg = errorMsg;
                    intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                    listener.onError(errorCode, intent, errorMsg);
                }
            }
        });
    }

    private void handleScanSignalCheck(BasePresenter presenter, final String signalCheckNum, final Activity activity, final OnDeployAnalyzerListener listener) {
        RetrofitServiceHelper.getInstance().getDeployDeviceDetail(signalCheckNum, null, null).subscribeOn
                (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(presenter) {
            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                    listener.onError(errorCode, null, errorMsg);
                } else if (errorCode == 4013101 || errorCode == 4000013) {
                    //查找新设备
                    Intent intent = new Intent();
                    intent.setClass(activity, DeployResultActivity.class);
                    DeployResultModel deployResultModel = new DeployResultModel();
                    deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                    deployResultModel.sn = signalCheckNum;
                    deployResultModel.scanType = Constants.TYPE_SCAN_SIGNAL_CHECK;
                    intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                    listener.onError(errorCode, intent, errorMsg);
                } else {
                    //TODO 控制逻辑
                    Intent intent = new Intent();
                    intent.setClass(activity, DeployResultActivity.class);
                    DeployResultModel deployResultModel = new DeployResultModel();
                    deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_SCAN_FAILED;
                    deployResultModel.sn = signalCheckNum;
                    deployResultModel.scanType = Constants.TYPE_SCAN_SIGNAL_CHECK;
                    deployResultModel.errorMsg = errorMsg;
                    intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                    listener.onError(errorCode, intent, errorMsg);
                }
            }


            @Override
            public void onCompleted(ResponseResult<DeviceInfo> deployDeviceDetailRsp) {
                DeviceInfo data = deployDeviceDetailRsp.getData();
                if (data == null) {
                    //查找新设备
                    Intent intent = new Intent();
                    intent.setClass(activity, DeployResultActivity.class);
                    DeployResultModel deployResultModel = new DeployResultModel();
                    deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                    deployResultModel.sn = signalCheckNum;
                    deployResultModel.scanType = Constants.TYPE_SCAN_SIGNAL_CHECK;
                    intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                    listener.onError(0, intent, null);
                } else {
                    String sn = data.getSn();
                    if (TextUtils.isEmpty(sn)) {
                        //拿不到sn认为为空对象
                        //查找新设备
                        Intent intent = new Intent();
                        intent.setClass(activity, DeployResultActivity.class);
                        DeployResultModel deployResultModel = new DeployResultModel();
                        deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                        deployResultModel.sn = signalCheckNum;
                        deployResultModel.scanType = Constants.TYPE_SCAN_SIGNAL_CHECK;
                        intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                        listener.onError(0, intent, null);
                    } else {
                        DeployAnalyzerModel deployAnalyzerModel = new DeployAnalyzerModel();
                        deployAnalyzerModel.deployType = Constants.TYPE_SCAN_SIGNAL_CHECK;
                        deployAnalyzerModel.status = data.getStatus();
                        deployAnalyzerModel.updatedTime = data.getUpdatedTime();
                        deployAnalyzerModel.nameAndAddress = data.getName();
                        deployAnalyzerModel.status = data.getStatus();
                        deployAnalyzerModel.deviceType = data.getDeviceType();
                        deployAnalyzerModel.sn = data.getSn();
                        deployAnalyzerModel.blePassword = data.getBlePassword();
                        List<String> tags = data.getTags();
                        if (tags != null && tags.size() > 0) {
                            deployAnalyzerModel.tagList.clear();
                            deployAnalyzerModel.tagList.addAll(tags);
                        }
                        Intent intent = new Intent();
                        intent.setClass(activity, SignalCheckActivity.class);
                        intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
                        listener.onSuccess(intent);
                    }
                }
            }
        });

    }

    private void handleScanInspectionDevice(BasePresenter presenter, String scanInspectionDevice, String inspectionId, final Activity activity, final OnDeployAnalyzerListener listener) {
        RetrofitServiceHelper.getInstance().getInspectionDeviceList(inspectionId, null, scanInspectionDevice.toUpperCase(), null, null, null, null).
                subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<InspectionTaskDeviceDetailModel>>(presenter) {
            @Override
            public void onCompleted(ResponseResult<InspectionTaskDeviceDetailModel> inspectionTaskDeviceDetailRsp) {
                InspectionTaskDeviceDetailModel data = inspectionTaskDeviceDetailRsp.getData();
                List<InspectionTaskDeviceDetail> devices = data.getDevices();
                if (devices != null && devices.size() > 0) {
                    InspectionTaskDeviceDetail deviceDetail = devices.get(0);
                    Intent intent = new Intent();
                    int status = deviceDetail.getStatus();

                    switch (status) {
                        case 0:
                            if (PreferencesHelper.getInstance().getUserData().hasInspectionDeviceModify) {
//                                intent.setClass(activity, InspectionActivity.class);
                                intent.putExtra(ARouterConstants.AROUTER_PATH, ARouterConstants.ACTIVITY_INSPECTION);
                            } else {
                                listener.onError(0, null, activity.getString(R.string.account_no_patrol_device_permissions));
                                return;
                            }
                            break;
                        case 1:
                            listener.onError(0, null, activity.getString(R.string.device_patrolled_status_normal));
                            return;
                        case 2:
//                            intent.setClass(activity, InspectionExceptionDetailActivity.class);
                            intent.putExtra(ARouterConstants.AROUTER_PATH, ARouterConstants.ACTIVITY_INSPECTION_EXCEPTION_DETAIL);
                            break;
                    }
                    intent.putExtra(Constants.EXTRA_INSPECTION_TASK_ITEM_DEVICE_DETAIL, deviceDetail);
                    listener.onSuccess(intent);
                } else {
                    listener.onError(0, null, activity.getString(R.string.device_not_in_inspection_mission));
                }

            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                listener.onError(errorCode, null, errorMsg);
            }
        });
    }

    private void handleDeployDeviceStation(final BasePresenter presenter, final String scanSerialNumber, final Activity activity, final OnDeployAnalyzerListener listener) {
        RetrofitServiceHelper.getInstance().getDeployDeviceDetail(scanSerialNumber, null, null).subscribeOn
                (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(presenter) {
            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                    listener.onError(errorCode, null, errorMsg);
                } else {
                    //TODO 目前认为不在账户下
                    doStation();
                }

            }

            private void doStation() {
                RetrofitServiceHelper.getInstance().getStationDetail(scanSerialNumber.toUpperCase()).subscribeOn
                        (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeployStationInfo>>(presenter) {
                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                            listener.onError(errorCode, null, errorMsg);
                        } else {
                            if (AppUtils.isChineseLanguage()) {
                                if (PreferencesHelper.getInstance().getUserData().hasDeviceCameraDeploy) {
                                    doCamera();
                                } else if (PreferencesHelper.getInstance().getUserData().hasDeviceForestCameraDeploy) {
                                    doForestCamera();
                                } else {
                                    //不在账户下
                                    Intent intent = new Intent();
                                    intent.setClass(activity, DeployResultActivity.class);
                                    DeployResultModel deployResultModel = new DeployResultModel();
                                    deployResultModel.scanType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
                                    deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                    deployResultModel.sn = scanSerialNumber;
                                    intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                    listener.onError(errorCode, intent, errorMsg);
                                }
                            } else {
                                //不在账户下
                                Intent intent = new Intent();
                                intent.setClass(activity, DeployResultActivity.class);
                                DeployResultModel deployResultModel = new DeployResultModel();
                                deployResultModel.scanType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
                                deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                deployResultModel.sn = scanSerialNumber;
                                intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                listener.onError(errorCode, intent, errorMsg);
                            }
                        }
                    }

                    @Override
                    public void onCompleted(ResponseResult<DeployStationInfo> deployStationInfoRsp) {
                        DeployStationInfo deployStationInfo = deployStationInfoRsp.getData();
                        try {
                            //todo 包装类
                            if (deployStationInfo == null || TextUtils.isEmpty(deployStationInfo.getSn())) {
                                if (AppUtils.isChineseLanguage()) {
                                    if (PreferencesHelper.getInstance().getUserData().hasDeviceCameraDeploy) {
                                        doCamera();
                                    } else if (PreferencesHelper.getInstance().getUserData().hasDeviceForestCameraDeploy) {
                                        doForestCamera();
                                    } else {
                                        //不在账户下
                                        Intent intent = new Intent();
                                        intent.setClass(activity, DeployResultActivity.class);
                                        DeployResultModel deployResultModel = new DeployResultModel();
                                        deployResultModel.scanType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
                                        deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                        deployResultModel.sn = scanSerialNumber;
                                        intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                        listener.onError(0, intent, null);
                                    }
                                } else {
                                    //不在账户下
                                    Intent intent = new Intent();
                                    intent.setClass(activity, DeployResultActivity.class);
                                    DeployResultModel deployResultModel = new DeployResultModel();
                                    deployResultModel.scanType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
                                    deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                    deployResultModel.sn = scanSerialNumber;
                                    intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                    listener.onError(0, intent, null);
                                }
                            } else {
                                DeployAnalyzerModel deployAnalyzerModel = new DeployAnalyzerModel();
                                deployAnalyzerModel.deployType = Constants.TYPE_SCAN_DEPLOY_STATION;
                                deployAnalyzerModel.sn = deployStationInfo.getSn();
                                deployAnalyzerModel.nameAndAddress = deployStationInfo.getName();
                                deployAnalyzerModel.status = deployStationInfo.getNormalStatus();
                                List<Double> lonlat = deployStationInfo.getLonlat();
                                deployAnalyzerModel.status = deployStationInfo.getNormalStatus();
                                if (lonlat != null && lonlat.size() == 2) {
                                    deployAnalyzerModel.latLng.clear();
                                    deployAnalyzerModel.latLng.addAll(lonlat);
                                }
                                List<String> tags = deployStationInfo.getTags();
                                if (tags != null && tags.size() > 0) {
                                    deployAnalyzerModel.tagList.clear();
                                    deployAnalyzerModel.tagList.addAll(tags);
                                }
                                deployAnalyzerModel.updatedTime = deployStationInfo.getUpdatedTime();
                                Intent intent = new Intent();
                                //TODO 测试
//                            intent.setClass(activity, DeployCameraDetailActivity.class);
                                intent.setClass(activity, DeployMonitorDetailActivity.class);
                                intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
                                listener.onSuccess(intent);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    /**
                     * 查找森林防火摄像机
                     */
                    private void doForestCamera() {
                        RetrofitServiceHelper.getInstance().getForestGateway(scanSerialNumber.toUpperCase()).subscribeOn
                                (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<ForestGatewayDeployInfo>>(presenter) {
                            @Override
                            public void onCompleted(ResponseResult<ForestGatewayDeployInfo> responseResult) {
                                ForestGatewayDeployInfo data = responseResult.getData();
                                if (data != null) {
                                    List<ForestGatewayDeployInfo.ForestGatewayDeployInfoListData> list = data.getList();
                                    if (list != null && list.size() > 0) {
                                        ForestGatewayDeployInfo.ForestGatewayDeployInfoListData forestGatewayDeployInfoListData = list.get(0);
                                        if (forestGatewayDeployInfoListData != null) {
                                            //TODO 结果
                                            //
                                            DeployAnalyzerModel deployAnalyzerModel = new DeployAnalyzerModel();
                                            //部署类型
                                            deployAnalyzerModel.deployType = Constants.TYPE_SCAN_DEPLOY_FOREST_CAMERA;
                                            //sn
                                            deployAnalyzerModel.sn = scanSerialNumber;
                                            //名称和地址
                                            deployAnalyzerModel.nameAndAddress = forestGatewayDeployInfoListData.getName();
                                            //标签
                                            List<String> label = forestGatewayDeployInfoListData.getLabel();
                                            deployAnalyzerModel.tagList.clear();
                                            if (label != null && label.size() > 0) {
                                                deployAnalyzerModel.tagList.addAll(label);
                                            }
                                            //经纬度
                                            deployAnalyzerModel.latLng.clear();
                                            double latitude = forestGatewayDeployInfoListData.getLatitude();
                                            double longitude = forestGatewayDeployInfoListData.getLongitude();
                                            if (0 != latitude && 0 != longitude) {
//                                                        final double lon = deployAnalyzerModel.latLng.get(0);
//                                                        final double lan = deployAnalyzerModel.latLng.get(1);
                                                deployAnalyzerModel.latLng.add(longitude);
                                                deployAnalyzerModel.latLng.add(latitude);
                                            }
                                            //联系人
                                            deployAnalyzerModel.deployContactModelList.clear();
                                            List<DeviceNotificationBean> notifications = WidgetUtil.handleDeviceNotifications(forestGatewayDeployInfoListData.getNotifications());
                                            if (notifications != null && notifications.size() > 0) {
                                                for (DeviceNotificationBean notification : notifications) {
                                                    if (!TextUtils.isEmpty(notification.getContent())) {
                                                        DeployContactModel deployContactModel = new DeployContactModel();
                                                        deployContactModel.phone = notification.getContent();
                                                        if (!TextUtils.isEmpty(notification.getContact())) {
                                                            deployContactModel.name = notification.getContact();
                                                        }
                                                        deployAnalyzerModel.deployContactModelList.add(deployContactModel);
                                                    }
                                                }
                                            }
                                            deployAnalyzerModel.installationLocation = forestGatewayDeployInfoListData.getInstallationLocation();
                                            deployAnalyzerModel.location = forestGatewayDeployInfoListData.getLocation();
                                            Intent intent = new Intent();
                                            //TODO
                                            intent.setClass(activity, DeployForestCameraDetailActivity.class);
                                            intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
                                            listener.onSuccess(intent);
                                            return;
                                        }
                                    }
                                }
                                //不在账户下
                                Intent intent = new Intent();
                                intent.setClass(activity, DeployResultActivity.class);
                                DeployResultModel deployResultModel = new DeployResultModel();
                                deployResultModel.scanType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
                                deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                deployResultModel.sn = scanSerialNumber;
                                intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                listener.onError(0, intent, null);
                            }

                            @Override
                            public void onErrorMsg(int errorCode, String errorMsg) {
                                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                                    listener.onError(errorCode, null, errorMsg);
                                } else {
                                    //不在账户下
                                    Intent intent = new Intent();
                                    intent.setClass(activity, DeployResultActivity.class);
                                    DeployResultModel deployResultModel = new DeployResultModel();
                                    deployResultModel.scanType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
                                    deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                    deployResultModel.sn = scanSerialNumber;
                                    intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                    listener.onError(errorCode, intent, errorMsg);
                                }
                            }
                        });
                    }

                    /**
                     * 查找普通摄像机
                     */
                    private void doCamera() {
                        RetrofitServiceHelper.getInstance().getDeployCameraInfo(scanSerialNumber.toUpperCase()).subscribeOn
                                (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceCameraDetailInfo>>(presenter) {
                            @Override
                            public void onCompleted(ResponseResult<DeviceCameraDetailInfo> deviceCameraDetailRsp) {
                                DeviceCameraDetailInfo data = deviceCameraDetailRsp.getData();
                                try {
                                    //todo 包装类
                                    DeployAnalyzerModel deployAnalyzerModel = new DeployAnalyzerModel();
                                    if (data == null) {
                                        //TODO 查找森林防火摄像头
                                        if (PreferencesHelper.getInstance().getUserData().hasDeviceForestCameraDeploy) {
                                            doForestCamera();
                                        } else {
                                            //不在账户下
                                            Intent intent = new Intent();
                                            intent.setClass(activity, DeployResultActivity.class);
                                            DeployResultModel deployResultModel = new DeployResultModel();
                                            deployResultModel.scanType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
                                            deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                            deployResultModel.sn = scanSerialNumber;
                                            intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                            listener.onError(0, intent, null);
                                        }

                                    } else {

                                        deployAnalyzerModel.deployType = Constants.TYPE_SCAN_DEPLOY_CAMERA;
                                        deployAnalyzerModel.sn = scanSerialNumber;
                                        deployAnalyzerModel.hls = data.getHls();
                                        deployAnalyzerModel.flv = data.getFlv();
                                        DeviceCameraDetailInfo.CameraBean camera = data.getCamera();
                                        //
                                        if (camera != null) {
                                            deployAnalyzerModel.nameAndAddress = camera.getName();
                                            deployAnalyzerModel.orientation = camera.getOrientation();
                                            deployAnalyzerModel.installationMode = camera.getInstallationMode();
                                            deployAnalyzerModel.sn = camera.getSn();
                                            List<String> label = camera.getLabel();
                                            if (label != null && label.size() > 0) {
                                                deployAnalyzerModel.tagList.clear();
                                                deployAnalyzerModel.tagList.addAll(label);
                                            }
                                            DeviceCameraDetailInfo.CameraBean.InfoBean info = camera.getInfo();
                                            if (info != null) {
                                                deployAnalyzerModel.cameraStatus = info.getDeviceStatus();
                                                deployAnalyzerModel.latLng.clear();
                                                String latitudeStr = info.getLatitude();
                                                String longitudeStr = info.getLongitude();
                                                if (!TextUtils.isEmpty(latitudeStr) && !TextUtils.isEmpty(longitudeStr)) {
                                                    try {
                                                        double latitude = Double.parseDouble(latitudeStr);
                                                        double longitude = Double.parseDouble(longitudeStr);
                                                        if (0 != latitude && 0 != longitude) {
//                                                        final double lon = deployAnalyzerModel.latLng.get(0);
//                                                        final double lan = deployAnalyzerModel.latLng.get(1);
                                                            deployAnalyzerModel.latLng.add(longitude);
                                                            deployAnalyzerModel.latLng.add(latitude);
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                        Intent intent = new Intent();
                                        intent.setClass(activity, DeployCameraDetailActivity.class);
                                        intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
                                        listener.onSuccess(intent);

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                            @Override
                            public void onErrorMsg(int errorCode, String errorMsg) {
                                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                                    listener.onError(errorCode, null, errorMsg);
                                } else {
                                    if (PreferencesHelper.getInstance().getUserData().hasDeviceForestCameraDeploy) {
                                        doForestCamera();
                                    } else {
                                        //不在账户下
                                        Intent intent = new Intent();
                                        intent.setClass(activity, DeployResultActivity.class);
                                        DeployResultModel deployResultModel = new DeployResultModel();
                                        deployResultModel.scanType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
                                        deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                        deployResultModel.sn = scanSerialNumber;
                                        intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                        listener.onError(errorCode, intent, errorMsg);
                                    }

                                }

                            }
                        });
                    }
                });
            }

            @Override
            public void onCompleted(ResponseResult<DeviceInfo> deployDeviceDetailRsp) {
                DeviceInfo data = deployDeviceDetailRsp.getData();
                if (data == null) {
                    doStation();
                } else {
                    String sn = data.getSn();
                    if (TextUtils.isEmpty(sn)) {
                        //拿不到sn认为为空对象
                        doStation();
                    } else {
                        DeployAnalyzerModel deployAnalyzerModel = new DeployAnalyzerModel();
                        List<Double> lonlat = data.getLonlat();
                        if (lonlat != null && lonlat.size() > 1 && lonlat.get(0) != 0 && lonlat.get(1) != 0) {
                            deployAnalyzerModel.latLng.clear();
                            deployAnalyzerModel.latLng.addAll(lonlat);
                            getAllDeviceInfo(deployAnalyzerModel);
                        } else {
                            deployAnalyzerModel.deployType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
                            deployAnalyzerModel.sn = sn;
                            String deviceType = data.getDeviceType();
                            deployAnalyzerModel.deviceType = deviceType;
                            deployAnalyzerModel.whiteListDeployType = handleWhiteListDeployType(deviceType);
                            deployAnalyzerModel.nameAndAddress = data.getName();
                            deployAnalyzerModel.notOwn = data.isNotOwn();
                            deployAnalyzerModel.blePassword = data.getBlePassword();
                            deployAnalyzerModel.weChatAccount = data.getWxPhone();
                            deployAnalyzerModel.status = data.getStatus();
                            deployAnalyzerModel.signal = data.getSignal();
                            List<String> tags = data.getTags();
                            if (tags != null && tags.size() > 0) {
                                deployAnalyzerModel.tagList.clear();
                                deployAnalyzerModel.tagList.addAll(tags);
                            }
                            deployAnalyzerModel.updatedTime = data.getUpdatedTime();
                            AlarmInfo alarmInfo = data.getAlarms();
                            if (alarmInfo != null) {
                                deployAnalyzerModel.deployContactModelList.clear();
                                List<DeviceNotificationBean> notifications = WidgetUtil.handleDeviceNotifications(alarmInfo.getNotifications());
                                if (notifications != null && notifications.size() > 0) {
                                    for (DeviceNotificationBean notification : notifications) {
                                        if (!TextUtils.isEmpty(notification.getContent())) {
                                            DeployContactModel deployContactModel = new DeployContactModel();
                                            deployContactModel.phone = notification.getContent();
                                            if (!TextUtils.isEmpty(notification.getContact())) {
                                                deployContactModel.name = notification.getContact();
                                            }
                                            deployAnalyzerModel.deployContactModelList.add(deployContactModel);
                                        }
                                    }
                                } else {
                                    DeviceNotificationBean notification = alarmInfo.getNotification();
                                    if (notification != null) {
                                        if (!TextUtils.isEmpty(notification.getContent())) {
                                            DeployContactModel deployContactModel = new DeployContactModel();
                                            deployContactModel.phone = notification.getContent();
                                            if (!TextUtils.isEmpty(notification.getContact())) {
                                                deployContactModel.name = notification.getContact();
                                            }
                                            deployAnalyzerModel.deployContactModelList.add(deployContactModel);
                                        }
                                    }
                                }
                            }
                            Intent intent = new Intent();
//                            intent.setClass(activity, DeployCameraDetailActivity.class);
                            intent.setClass(activity, DeployMonitorDetailActivity.class);
                            intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
                            listener.onSuccess(intent);
                        }
                    }

                }
            }

            private void getAllDeviceInfo(final DeployAnalyzerModel deployAnalyzerModel) {
                RetrofitServiceHelper.getInstance().getDeployDeviceDetail(scanSerialNumber, deployAnalyzerModel.latLng.get(0), deployAnalyzerModel.latLng.get(1)).subscribeOn
                        (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(presenter) {
                    @Override
                    public void onCompleted(ResponseResult<DeviceInfo> deployDeviceDetailRsp) {
                        DeviceInfo data = deployDeviceDetailRsp.getData();
                        deployAnalyzerModel.deployType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
                        deployAnalyzerModel.sn = data.getSn();
                        String deviceType = data.getDeviceType();
                        deployAnalyzerModel.deviceType = deviceType;
                        deployAnalyzerModel.whiteListDeployType = handleWhiteListDeployType(deviceType);
                        deployAnalyzerModel.nameAndAddress = data.getName();
                        deployAnalyzerModel.status = data.getStatus();
                        deployAnalyzerModel.notOwn = data.isNotOwn();
                        deployAnalyzerModel.blePassword = data.getBlePassword();
                        deployAnalyzerModel.signal = data.getSignal();
                        deployAnalyzerModel.weChatAccount = data.getWxPhone();
                        deployAnalyzerModel.status = data.getStatus();
                        List<String> tags = data.getTags();
                        if (tags != null && tags.size() > 0) {
                            deployAnalyzerModel.tagList.clear();
                            deployAnalyzerModel.tagList.addAll(tags);
                        }
                        deployAnalyzerModel.updatedTime = data.getUpdatedTime();
                        List<Integer> channelMask = data.getChannelMask();
                        if (channelMask != null && channelMask.size() > 0) {
                            deployAnalyzerModel.channelMask.addAll(channelMask);
                        }
                        AlarmInfo alarmInfo = data.getAlarms();
                        if (alarmInfo != null) {
                            deployAnalyzerModel.deployContactModelList.clear();
                            List<DeviceNotificationBean> notifications = WidgetUtil.handleDeviceNotifications(alarmInfo.getNotifications());
                            if (notifications.size() > 0) {
                                for (DeviceNotificationBean notification : notifications) {
                                    if (!TextUtils.isEmpty(notification.getContent())) {
                                        DeployContactModel deployContactModel = new DeployContactModel();
                                        deployContactModel.phone = notification.getContent();
                                        if (!TextUtils.isEmpty(notification.getContact())) {
                                            deployContactModel.name = notification.getContact();
                                        }
                                        deployAnalyzerModel.deployContactModelList.add(deployContactModel);
                                    }
                                }
                            } else {
                                DeviceNotificationBean notification = alarmInfo.getNotification();
                                if (notification != null) {
                                    if (!TextUtils.isEmpty(notification.getContent())) {
                                        DeployContactModel deployContactModel = new DeployContactModel();
                                        deployContactModel.phone = notification.getContent();
                                        if (!TextUtils.isEmpty(notification.getContact())) {
                                            deployContactModel.name = notification.getContact();
                                        }
                                        deployAnalyzerModel.deployContactModelList.add(deployContactModel);
                                    }
                                }
                            }
                        }
                        Intent intent = new Intent();
                        //TODO
//                        intent.setClass(activity, DeployCameraDetailActivity.class);
                        intent.setClass(activity, DeployMonitorDetailActivity.class);
                        intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
                        listener.onSuccess(intent);
                    }

                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                            listener.onError(errorCode, null, errorMsg);
                        } else {
                            //TODO 控制逻辑
                            doStation();
                        }

                    }
                });
            }
        });

    }

    private void handleScanLogin(BasePresenter presenter, final String result,
                                 final Activity activity, final OnDeployAnalyzerListener listener) {
        RetrofitServiceHelper.getInstance().getLoginScanResult(result).subscribeOn
                (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult>(presenter) {
            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                listener.onError(errorCode, null, errorMsg);
            }

            @Override
            public void onCompleted(ResponseResult responseBase) {
                if (responseBase.getErrcode() == 0) {
                    try {
                        try {
                            LogUtils.loge("qrcodeId = " + result);
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        Intent intent = new Intent();
                        intent.setClass(activity, ScanLoginResultActivity.class);
                        intent.putExtra("qrcodeId", result);
                        listener.onSuccess(intent);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                listener.onError(0, null, activity.getString(R.string.please_re_scan_try_again));
            }
        });
    }

    private void handleDeviceDeployChange(final int scanType, final BasePresenter presenter,
                                          final InspectionTaskDeviceDetail oldDeviceDetail, final String scanSerialNumber,
                                          final Activity activity, final OnDeployAnalyzerListener listener) {
        //todo 信息替换
        final DeployAnalyzerModel deployAnalyzerModel = new DeployAnalyzerModel();
        RetrofitServiceHelper.getInstance().getDeployDeviceDetail(oldDeviceDetail.getSn(), null, null).subscribeOn
                (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(presenter) {
            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                    listener.onError(errorCode, null, errorMsg);
                } else {
                    //TODO 控制逻辑
                    getNesDeviceInfo();
                }
            }

            private void getNesDeviceInfo() {
                Double lon = null;
                Double lat = null;
                if (deployAnalyzerModel.latLng.size() == 2) {
                    lon = deployAnalyzerModel.latLng.get(0);
                    lat = deployAnalyzerModel.latLng.get(1);
                }
                RetrofitServiceHelper.getInstance().getDeployDeviceDetail(scanSerialNumber, lon, lat).subscribeOn
                        (Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<DeviceInfo>>(presenter) {
                    @Override
                    public void onErrorMsg(int errorCode, String errorMsg) {
                        if (errorCode == ERR_CODE_NET_CONNECT_EX || errorCode == ERR_CODE_UNKNOWN_EX) {
                            listener.onError(errorCode, null, errorMsg);
                        } else {
                            //TODO 控制逻辑
                            Intent intent = new Intent();
                            intent.setClass(activity, DeployResultActivity.class);
                            DeployResultModel deployResultModel = new DeployResultModel();
                            deployResultModel.scanType = scanType;
                            deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                            deployResultModel.sn = scanSerialNumber;
                            intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                            listener.onError(errorCode, intent, errorMsg);
                        }
                    }


                    @Override
                    public void onCompleted(ResponseResult<DeviceInfo> deployDeviceDetailRsp) {
                        DeviceInfo data = deployDeviceDetailRsp.getData();
                        if (data == null) {
                            Intent intent = new Intent();
                            intent.setClass(activity, DeployResultActivity.class);
                            DeployResultModel deployResultModel = new DeployResultModel();
                            deployResultModel.scanType = scanType;
                            deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                            deployResultModel.sn = scanSerialNumber;
                            intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                            listener.onError(0, intent, null);
                        } else {
                            String sn = data.getSn();
                            if (TextUtils.isEmpty(sn)) {
                                //拿不到sn认为为空对象
                                Intent intent = new Intent();
                                intent.setClass(activity, DeployResultActivity.class);
                                DeployResultModel deployResultModel = new DeployResultModel();
                                deployResultModel.scanType = scanType;
                                deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                                deployResultModel.sn = scanSerialNumber;
                                intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                                listener.onError(0, intent, null);
                            } else {
                                deployAnalyzerModel.deployType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
                                deployAnalyzerModel.sn = sn;
                                deployAnalyzerModel.notOwn = data.isNotOwn();
                                deployAnalyzerModel.mDeviceDetail = oldDeviceDetail;
                                deployAnalyzerModel.blePassword = data.getBlePassword();
                                deployAnalyzerModel.status = data.getStatus();
                                String deviceType = data.getDeviceType();
                                if (!TextUtils.isEmpty(deviceType)) {
                                    deployAnalyzerModel.deviceType = deviceType;
                                    deployAnalyzerModel.whiteListDeployType = handleWhiteListDeployType(deviceType);
                                }
                                List<Integer> channelMask = data.getChannelMask();
                                if (channelMask != null && channelMask.size() > 0) {
                                    deployAnalyzerModel.channelMask.clear();
                                    deployAnalyzerModel.channelMask.addAll(channelMask);
                                }
                                Intent intent = new Intent();
//                                intent.setClass(activity, DeployCameraDetailActivity.class);
                                intent.setClass(activity, DeployMonitorDetailActivity.class);
                                intent.putExtra(Constants.EXTRA_DEPLOY_ANALYZER_MODEL, deployAnalyzerModel);
                                listener.onSuccess(intent);
                            }
                        }
                    }
                });
            }

            @Override
            public void onCompleted(ResponseResult<DeviceInfo> deployDeviceDetailRsp) {
                DeviceInfo data = deployDeviceDetailRsp.getData();
                if (data == null) {
                    Intent intent = new Intent();
                    intent.setClass(activity, DeployResultActivity.class);
                    DeployResultModel deployResultModel = new DeployResultModel();
                    deployResultModel.scanType = scanType;
                    deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                    deployResultModel.sn = scanSerialNumber;
                    intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                    listener.onError(0, intent, null);
                } else {
                    String sn = data.getSn();
                    if (TextUtils.isEmpty(sn)) {
                        //拿不到sn认为为空对象
                        Intent intent = new Intent();
                        intent.setClass(activity, DeployResultActivity.class);
                        DeployResultModel deployResultModel = new DeployResultModel();
                        deployResultModel.scanType = scanType;
                        deployResultModel.resultCode = Constants.DEPLOY_RESULT_MODEL_CODE_DEPLOY_NOT_UNDER_THE_ACCOUNT;
                        deployResultModel.sn = scanSerialNumber;
                        intent.putExtra(Constants.EXTRA_DEPLOY_RESULT_MODEL, deployResultModel);
                        listener.onError(0, intent, null);
                    } else {
                        deployAnalyzerModel.deployType = scanType;
                        deployAnalyzerModel.nameAndAddress = data.getName();
                        deployAnalyzerModel.status = data.getStatus();
                        deployAnalyzerModel.deviceType = data.getDeviceType();
                        deployAnalyzerModel.weChatAccount = data.getWxPhone();
                        deployAnalyzerModel.status = data.getStatus();
                        List<Double> lonlat = data.getLonlat();
                        if (lonlat != null && lonlat.size() > 1 && lonlat.get(0) != 0 && lonlat.get(1) != 0) {
                            deployAnalyzerModel.latLng.clear();
                            deployAnalyzerModel.latLng.addAll(lonlat);
                        }
                        deployAnalyzerModel.signal = data.getSignal();
                        List<String> tags = data.getTags();
                        if (tags != null && tags.size() > 0) {
                            deployAnalyzerModel.tagList.clear();
                            deployAnalyzerModel.tagList.addAll(tags);
                        }
                        deployAnalyzerModel.updatedTime = data.getUpdatedTime();
                        AlarmInfo alarmInfo = data.getAlarms();
                        if (alarmInfo != null) {
                            deployAnalyzerModel.deployContactModelList.clear();
                            List<DeviceNotificationBean> notifications = WidgetUtil.handleDeviceNotifications(alarmInfo.getNotifications());
                            if (notifications != null && notifications.size() > 0) {
                                for (DeviceNotificationBean notification : notifications) {
                                    if (!TextUtils.isEmpty(notification.getContent())) {
                                        DeployContactModel deployContactModel = new DeployContactModel();
                                        deployContactModel.phone = notification.getContent();
                                        if (!TextUtils.isEmpty(notification.getContact())) {
                                            deployContactModel.name = notification.getContact();
                                        }
                                        deployAnalyzerModel.deployContactModelList.add(deployContactModel);
                                    }
                                }
                            } else {
                                DeviceNotificationBean notification = alarmInfo.getNotification();
                                if (notification != null) {
                                    if (!TextUtils.isEmpty(notification.getContent())) {
                                        DeployContactModel deployContactModel = new DeployContactModel();
                                        deployContactModel.phone = notification.getContent();
                                        if (!TextUtils.isEmpty(notification.getContact())) {
                                            deployContactModel.name = notification.getContact();
                                        }
                                        deployAnalyzerModel.deployContactModelList.add(deployContactModel);
                                    }
                                }
                            }
                        }
                        getNesDeviceInfo();
                    }
                }
            }
        });
    }

    public interface OnDeployAnalyzerListener {
        void onSuccess(Intent intent);


        void onError(int errType, Intent intent, String errMsg);

    }

    private String parseResultMac(String result) {

        String serialNumber = null;
        if (result != null) {
            String[] data;
            String type;
            data = result.split("\\|");
            type = data[0];
            serialNumber = type;
        }
        return serialNumber;
    }

    /**
     * 处理一下是否白名单
     */
    private int handleWhiteListDeployType(String deviceType) {
        int whiteListDeployType = Constants.TYPE_SCAN_DEPLOY_DEVICE;
        DeviceTypeStyles configDeviceType = PreferencesHelper.getInstance().getConfigDeviceType(deviceType);
        if (configDeviceType != null && configDeviceType.isIgnoreSignal()) {
            //白名单设备
            if (PreferencesHelper.getInstance().getUserData().hasSignalConfig) {
                whiteListDeployType = Constants.TYPE_SCAN_DEPLOY_WHITE_LIST_HAS_SIGNAL_CONFIG;
            } else {
                whiteListDeployType = Constants.TYPE_SCAN_DEPLOY_WHITE_LIST;
            }
        }
        return whiteListDeployType;
    }
}
