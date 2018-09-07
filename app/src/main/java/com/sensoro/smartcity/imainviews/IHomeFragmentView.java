package com.sensoro.smartcity.imainviews;

import com.sensoro.smartcity.iwidget.IActivityIntent;
import com.sensoro.smartcity.iwidget.IProgressDialog;
import com.sensoro.smartcity.iwidget.IToast;
import com.sensoro.smartcity.server.bean.DeviceInfo;

import java.util.List;

public interface IHomeFragmentView extends IToast, IProgressDialog, IActivityIntent {
    void updateRcTypeAdapter(List<String> data);

    void setImvAddVisible(boolean isVisible);

    void setImvSearchVisible(boolean isVisible);
    void refreshTop(boolean isFirstInit, int alarmCount, int lostCount, int inactiveCount);

    void returnTop();
    void refreshData(List<DeviceInfo> dataList);
    void showTypePopupView();
    void setTypeView(String typesText);
    void recycleViewRefreshComplete();
}
