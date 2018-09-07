package com.sensoro.smartcity.server.bean;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by sensoro on 17/11/13.
 */

public class DeviceAlarmLogInfo implements Serializable , Comparable<DeviceAlarmLogInfo>{
    private String _id;
    private String appId;
    private String deviceSN;
    private String deviceName;
    private String sensorType;
    private String unionType;
    private String deviceType;
    private String _updatedTime;
    private long updatedTime;
    private AlarmInfo.RuleInfo []rules;
    private AlarmInfo.RecordInfo []records;
    private AlarmInfo.OwnerInfo owners;
    private boolean isDeleted;
    private int displayStatus;
    private int sort;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getUnionType() {
        return unionType;
    }

    public void setUnionType(String unionType) {
        this.unionType = unionType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String get_updatedTime() {
        return _updatedTime;
    }

    public void set_updatedTime(String _updatedTime) {
        this._updatedTime = _updatedTime;
    }

    public AlarmInfo.RuleInfo[] getRules() {
        return rules;
    }

    public void setRules(AlarmInfo.RuleInfo[] rules) {
        this.rules = rules;
    }

    public AlarmInfo.RecordInfo[] getRecords() {
        return records;
    }

    public void setRecords(AlarmInfo.RecordInfo[] records) {
        this.records = records;
    }

    public AlarmInfo.OwnerInfo getOwners() {
        return owners;
    }

    public void setOwners(AlarmInfo.OwnerInfo owners) {
        this.owners = owners;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public int getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(int displayStatus) {
        this.displayStatus = displayStatus;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    @Override
    public int compareTo(@NonNull DeviceAlarmLogInfo anotherAlarmLogInfo) {
        if (this.getSort() < anotherAlarmLogInfo.getSort()) {
            return -1;
        } else if (this.getSort() == anotherAlarmLogInfo.getSort()) {
            return 0;
        } else {
            return 1;
        }
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }
}