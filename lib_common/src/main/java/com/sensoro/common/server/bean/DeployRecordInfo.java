package com.sensoro.common.server.bean;

import com.sensoro.common.model.DeviceNotificationBean;

import java.io.Serializable;
import java.util.List;

public class DeployRecordInfo implements Serializable {


    private String _id;
    private String id;
    private int count;
    private String sn;
    private String deviceName;
    private long createdTime;
    private String deployStaff;
    private DeviceNotificationBean notification;
    private List<DeviceNotificationBean> notifications;
    private String signalQuality;
    private String deviceType;
    private String deviceOwners;
    private String appId;
    private String unionType;
    private String wxPhone;
    private List<String> tags;
    private List<Double> lonlat;
    private List<String> deployPics;
    private DeployControlSettingData config;
    private String forceReason;
    private Integer status;

    public String getForceReason() {
        return forceReason;
    }

    public void setForceReason(String forceReason) {
        this.forceReason = forceReason;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public DeployControlSettingData getConfig() {
        return config;
    }

    public void setConfig(DeployControlSettingData config) {
        this.config = config;
    }

    public String getWxPhone() {
        return wxPhone;
    }

    public void setWxPhone(String wxPhone) {
        this.wxPhone = wxPhone;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public String getDeployStaff() {
        return deployStaff;
    }

    public void setDeployStaff(String deployStaff) {
        this.deployStaff = deployStaff;
    }

    public DeviceNotificationBean getNotification() {
        return notification;
    }

    public void setNotification(DeviceNotificationBean notification) {
        this.notification = notification;
    }

    public String getSignalQuality() {
        return signalQuality;
    }

    public void setSignalQuality(String signalQuality) {
        this.signalQuality = signalQuality;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceOwners() {
        return deviceOwners;
    }

    public void setDeviceOwners(String deviceOwners) {
        this.deviceOwners = deviceOwners;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUnionType() {
        return unionType;
    }

    public void setUnionType(String unionType) {
        this.unionType = unionType;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Double> getLonlat() {
        return lonlat;
    }

    public void setLonlat(List<Double> lonlat) {
        this.lonlat = lonlat;
    }

    public List<String> getDeployPics() {
        return deployPics;
    }

    public void setDeployPics(List<String> deployPics) {
        this.deployPics = deployPics;
    }

    public List<DeviceNotificationBean> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<DeviceNotificationBean> notifications) {
        this.notifications = notifications;
    }
}
