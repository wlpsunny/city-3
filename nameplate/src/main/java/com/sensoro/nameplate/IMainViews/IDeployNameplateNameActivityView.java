package com.sensoro.nameplate.IMainViews;

import com.sensoro.common.iwidget.IActivityIntent;
import com.sensoro.common.iwidget.IProgressDialog;
import com.sensoro.common.iwidget.IToast;

import java.util.List;

public interface IDeployNameplateNameActivityView extends IToast, IActivityIntent,IProgressDialog {
    void setEditText(String text);

    void updateSearchHistoryData(List<String> searchStr);

    void updateTvTitle(String sn);

    void updateSaveStatus(boolean isEnable);

    void showHistoryClearDialog();
}
