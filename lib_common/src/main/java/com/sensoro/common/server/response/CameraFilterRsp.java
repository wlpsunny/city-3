package com.sensoro.common.server.response;

import com.sensoro.common.model.CameraFilterModel;

import java.util.List;

public class CameraFilterRsp extends ResponseBase {

    public List<CameraFilterModel> getData() {
        return data;
    }

    public void setData(List<CameraFilterModel> data) {
        this.data = data;
    }

    private List<CameraFilterModel> data;
}