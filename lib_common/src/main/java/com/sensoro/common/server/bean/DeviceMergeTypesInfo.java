package com.sensoro.common.server.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class DeviceMergeTypesInfo implements Serializable {


    private DeviceMergeTypeConfig config;
    private List<String> tags;

    public DeviceMergeTypeConfig getConfig() {
        return config;
    }

    public void setConfig(DeviceMergeTypeConfig config) {
        this.config = config;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public static class DeviceMergeTypeConfig {
        public Map<String, DeviceTypeStyles> getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(Map<String, DeviceTypeStyles> deviceType) {
            this.deviceType = deviceType;
        }

        public Map<String, MergeTypeStyles> getMergeType() {
            return mergeType;
        }

        public void setMergeType(Map<String, MergeTypeStyles> mergeType) {
            this.mergeType = mergeType;
        }

        public Map<String, SensorTypeStyles> getSensorType() {
            return sensorType;
        }

        public void setSensorType(Map<String, SensorTypeStyles> sensorType) {
            this.sensorType = sensorType;
        }

        public MalfunctionTypeBean getMalfunctionType() {
            return malfunctionType;
        }

        public void setMalfunctionType(MalfunctionTypeBean malfunctionType) {
            this.malfunctionType = malfunctionType;
        }

        private Map<String, DeviceTypeStyles> deviceType;
        private Map<String, MergeTypeStyles> mergeType;
        private Map<String, SensorTypeStyles> sensorType;
        private MalfunctionTypeBean malfunctionType;

        public static class MalfunctionTypeBean {
            private Map<String, MalfunctionTypeStyles> mainTypes;
            private Map<String, MalfunctionTypeStyles> subTypes;

            //
            public Map<String, MalfunctionTypeStyles> getMainTypes() {
                return mainTypes;
            }

            public void setMainTypes(Map<String, MalfunctionTypeStyles> mainTypes) {
                this.mainTypes = mainTypes;
            }

            public Map<String, MalfunctionTypeStyles> getSubTypes() {
                return subTypes;
            }

            public void setSubTypes(Map<String, MalfunctionTypeStyles> subTypes) {
                this.subTypes = subTypes;
            }
        }
    }
}
