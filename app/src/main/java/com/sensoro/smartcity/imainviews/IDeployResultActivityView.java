package com.sensoro.smartcity.imainviews;

import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;

import com.sensoro.common.iwidget.IActivityIntent;
import com.sensoro.common.iwidget.IToast;

public interface IDeployResultActivityView extends IToast, IActivityIntent {
    void refreshSignal(long updateTime, String signal);

    void setResultImageView(int resId);

    void setTipsTextView(String text,int resId);

    void setSnTextView(String sn);

    void setNameTextView(String name);

    void setContactTextView(String content);

    void setWeChatTextView(String content);

    void setStatusTextView(String status, @ColorInt int color);

    void setUpdateTextView(String update);

    void setAddressTextView(String address);

    void setUpdateTextViewVisible(boolean isVisible);

    void setContactAndSignalVisible(boolean isVisible);

    void setStateTextView(String msg);

    void setDeployResultRightButtonText(String text);

    void setDeployResultLeftButtonText(String text);

    void setDeployResultRightButtonVisible(boolean isVisible);

    void setStateTextViewVisible(boolean isVisible);

    void setResultSettingVisible(boolean isVisible);

    void setTitleText(String text);

    void setDeployResultHasSetting(String setting);

    void setDeployResultTvStateTextColor(int resColor);

    void setDeployResultDividerVisible(boolean isVisible);

    void setDeployResultRightButtonTextBackground(Drawable drawable);
}
