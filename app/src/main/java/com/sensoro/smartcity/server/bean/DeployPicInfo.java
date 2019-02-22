package com.sensoro.smartcity.server.bean;

import com.google.gson.annotations.Expose;
import com.sensoro.smartcity.widget.imagepicker.bean.ImageItem;

public class DeployPicInfo {
    public String title;
    public String description;
    public String imgUrl;

    @Expose(serialize = false, deserialize = false)
    public ImageItem photoItem;

    public boolean isRequired;

    public DeployPicInfo() {
    }

    public DeployPicInfo copy() {
        DeployPicInfo deployPicInfo = new DeployPicInfo();
        deployPicInfo.title = this.title;
        deployPicInfo.description = this.description;
        deployPicInfo.isRequired = this.isRequired;
        deployPicInfo.imgUrl = this.imgUrl;

        return deployPicInfo;
    }
}