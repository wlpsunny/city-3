package com.sensoro.smartcity.imainviews;

import com.sensoro.smartcity.iwidget.IActivityIntent;
import com.sensoro.smartcity.iwidget.IProgressDialog;
import com.sensoro.smartcity.iwidget.IToast;

public interface IDeployDeviceDetailActivityView extends IToast,IProgressDialog,IActivityIntent{
    void updateUploadState(boolean isAvailable);
}
