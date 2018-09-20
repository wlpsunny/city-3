package com.sensoro.smartcity.imainviews;

import com.sensoro.smartcity.iwidget.IActivityIntent;
import com.sensoro.smartcity.iwidget.IProgressDialog;
import com.sensoro.smartcity.iwidget.IToast;

public interface IInspectionTaskListActivityView extends IToast,IProgressDialog,IActivityIntent{
    void setRlDateEditVisible(boolean isVisible);

    boolean getRlDateEditIsVisible();

    void setSelectedDateSearchText(String time);
}