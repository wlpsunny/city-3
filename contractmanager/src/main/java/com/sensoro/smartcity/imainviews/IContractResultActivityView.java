package com.sensoro.smartcity.imainviews;

import android.graphics.Bitmap;

import com.sensoro.common.iwidget.IActivityIntent;
import com.sensoro.common.iwidget.IToast;

public interface IContractResultActivityView extends IToast,IActivityIntent{
    void setImageBitmap(Bitmap bitmap);
    void setTextResultInfo(String text);
}
