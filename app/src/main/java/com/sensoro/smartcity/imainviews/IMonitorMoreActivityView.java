package com.sensoro.smartcity.imainviews;

import com.sensoro.common.iwidget.IProgressDialog;
import com.sensoro.common.iwidget.IToast;

import java.util.List;

public interface IMonitorMoreActivityView extends IToast, IProgressDialog {
    void setAlarmRecentInfo(String info);

    void setAlarmRecentInfo(int resID);

    void setSNText(String sn);

    void setTypeText(String type);

    void setLongitudeLatitude(String lon, String lat);

    void setAlarmSetting(String alarmSetting);

    void setInterval(String interval);

    void setName(String name);

    void setName(int resId);

    void setStatusInfo(String status, int background);

    void setBatteryInfo(String battery);

    void updateTags(List<String> list);

    void setPhoneText(String phone);

    void setReportText(String report);
}



