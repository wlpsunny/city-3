package com.sensoro.smartcity.server.response;

import com.sensoro.smartcity.server.bean.InspectionTaskDeviceModel;

public class InspectionTaskDeviceRsp extends ResponseBase {
    public InspectionTaskDeviceModel getData() {
        return data;
    }

    public void setData(InspectionTaskDeviceModel data) {
        this.data = data;
    }

    private InspectionTaskDeviceModel data;
}
