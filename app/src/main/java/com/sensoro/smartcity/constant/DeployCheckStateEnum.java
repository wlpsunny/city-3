package com.sensoro.smartcity.constant;

public enum DeployCheckStateEnum {
    DEVICE_CHECK_NEARBY_START, DEVICE_CHECK_NEARBY_SUC, DEVICE_CHECK_NEARBY_FAIL, DEVICE_CHECK_CONFIG_START, DEVICE_CHECK_CONFIG_SUC, DEVICE_CHECK_CONFIG_FAIL, DEVICE_CHECK_SIGNAL_START,
    DEVICE_CHECK_SIGNAL_SUC_GOOD, DEVICE_CHECK_SIGNAL_SUC_NORMAL, DEVICE_CHECK_SIGNAL_FAIL_NONE, DEVICE_CHECK_SIGNAL_FAIL_BAD, DEVICE_CHECK_STATUS_START, DEVICE_CHECK_STATUS_SUC, DEVICE_CHECK_STATUS_FAIL_ALARM,
    DEVICE_CHECK_STATUS_FAIL_MALFUNCTION, DEVICE_CHECK_STATUS_FAIL_INTERNET, DEVICE_CHECK_ALL_SUC;
}