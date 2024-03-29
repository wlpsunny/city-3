package com.sensoro.smartcity.imainviews;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.sensoro.common.iwidget.IActivityIntent;
import com.sensoro.common.iwidget.IProgressDialog;
import com.sensoro.common.iwidget.IToast;
import com.sensoro.common.model.Elect3DetailModel;
import com.sensoro.common.model.MonitoringPointRcContentAdapterModel;
import com.sensoro.common.server.bean.ScenesData;
import com.sensoro.smartcity.adapter.model.EarlyWarningthresholdDialogUtilsAdapterModel;
import com.sensoro.smartcity.model.TaskOptionModel;

import java.util.ArrayList;
import java.util.List;

public interface IMonitorPointDetailActivityView extends IToast, IProgressDialog, IActivityIntent {
    void setTitleNameTextView(String name);

    void setUpdateTime(String time);

    void setAlarmStateColor(int color);

    void setContractName(String contractName);

    void setContractPhone(String contractPhone);

    void setContractCount(int count);

    void setDeviceLocation(String location, boolean isArrowsRight);

    void updateDeviceInfoAdapter(List<MonitoringPointRcContentAdapterModel> data);

    void updateDeviceMalfunctionInfoAdapter(List<MonitoringPointRcContentAdapterModel> data);

    void setSNText(String sn);

    void updateTags(List<String> list);

    void updateMonitorPhotos(List<ScenesData> data);

    void setBatteryInfo(String battery);

    void setInterval(String interval);

    void setStatusInfo(String statusInfo, int textColor);

    void setContactPhoneIconVisible(boolean isVisible);

    void setNoContact();

    void setDeviceLocationTextColor(int color);

    void setDeviceTypeName(String typeName);

    void setDeviceOperationVisible(boolean isVisible);

    void showOperationSuccessToast();

    void showOperationSuccessToast(String text);

    void showErrorTipDialog(String errorMsg);

    void showOperationTipLoadingDialog();

    void dismissTipDialog();

    void dismissOperatingLoadingDialog();

    void setBlePwd(String pwd);

    void setElectDetailVisible(boolean isVisible);

    void setIvAlarmStatusVisible(boolean isVisible);

    void setElect3DetailVisible(boolean isVisible);

    void set3ElectTopDetail(Elect3DetailModel detailModel);

    void set3ElectADetail(Elect3DetailModel detailModel);

    void set3ElectVDetail(Elect3DetailModel detailModel);

    void set3ElectTDetail(Elect3DetailModel detailModel);

    void updateEarlyWarningThresholdAdapterDialogUtils(List<EarlyWarningthresholdDialogUtilsAdapterModel> data);

    void dismissEarlyWarningThresholdAdapterDialogUtils();

    void showUpdateDialogUtils(String title, String version, String date, boolean hasNewVersion);

    void dismissUpdateDialogUtils();

    void setLlElectTopVisible(boolean isVisible);

    void setTopElectData(String value, int color, String typeName);

    void setAcMonitoringElectPointLineVisible(boolean isVisible);

    void setLlAllElectDetailVisible(boolean isVisible);

    void setElectInfoTipVisible(boolean isVisible);

    void setMonitorDetailTvCategory(String category);

    void showTipDialog(boolean isEdit, String deviceType, CharSequence title, CharSequence message, @ColorRes int messageColor, CharSequence confirm, @ColorRes int confirmColor, int type);

    void updateTaskOptionModelAdapter(ArrayList<TaskOptionModel> optionModels);

    void setMonitorDeployTime(String time);

    void setDeviceVision(String text);

    void updateDialogProgress(String msg, int progress, int status);

    void setIvHasNewVersionViewVisible(boolean isVisible);

    void setSignalStatus(@DrawableRes int drawable, String text);

    void showBleTips();

    void hideBleTips();

    void setDeviceDemoModeViewStatus(int status);

    void showOpenDemoDialog();

    void dismissOpenDemoDialog();

    void showCloseDemoDialog();

    void dismissCloseDemoDialog();

    void showOperationTipLoadingDialog(String text);

    void setDeviceCamerasText(String text);

    void setDeviceDetailConfigInfo(String actual, String trans);

    void set2GDeviceConfigVisible(boolean visible);

    void set2GDeviceConfigInfo(String imei, String imsi, String iccid);

    void setMonitorDetailTitleIccid(String title);

}
