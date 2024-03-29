package com.sensoro.smartcity.constant;

public interface DeoloyCheckPointConstants {
    //dialog显示几个配置项
    int DEPLOY_CHECK_DIALOG_ORIGIN_STATE_SINGLE = 0x01;
    int DEPLOY_CHECK_DIALOG_ORIGIN_STATE_THREE = 0x02;
    int DEPLOY_CHECK_DIALOG_ORIGIN_STATE_FOUR = 0x03;
    int DEPLOY_CHECK_DIALOG_ORIGIN_STATE_TWO = 0x04;
    //信号检测
    int DEPLOY_CHECK_DIALOG_SIGNAL_GOOD = 0x01;
    int DEPLOY_CHECK_DIALOG_SIGNAL_NORMAL = 0x02;
    int DEPLOY_CHECK_DIALOG_SIGNAL_BAD = 0x03;
    int DEPLOY_CHECK_DIALOG_SIGNAL_NONE = 0x04;

    //
    int DEPLOY_CHECK_DIALOG_STATUS_ALARM = -0x01;
    int DEPLOY_CHECK_DIALOG_STATUS_MALFUNCTION = -0x02;
    int DEPLOY_CHECK_DIALOG_STATUS_INTERNET_FAILED = -0x03;

}
