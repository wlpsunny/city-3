package com.sensoro.smartcity.temp.entity;


import com.sensoro.common.server.bean.DeviceCameraInfo;

/**
 * Created by shuyu on 2016/11/11.
 */

public class VideoModel {
    public DeviceCameraInfo deviceCameraInfo;

    public VideoModel(String url) {
        this.url = url;
    }

    public String url;

    public VideoModel() {
    }
}
