package com.sensoro.smartcity.server.bean;

import java.io.Serializable;
import java.util.List;

public class GrantsInfo implements Serializable {


    //    List<String> deploy;
//    List<String> groupNotice;
//    List<String> deviceGroup;
//    List<String> indoorMap;
//    List<String> account;
//    List<String> user;
//    List<String> task;
    List<String> station;
    List<String> contract;
    List<String> tv;

    public List<String> getStation() {
        return station;
    }

    public void setStation(List<String> station) {
        this.station = station;
    }

    public List<String> getContract() {
        return contract;
    }

    public void setContract(List<String> contract) {
        this.contract = contract;
    }

    public List<String> getTv() {
        return tv;
    }

    public void setTv(List<String> tv) {
        this.tv = tv;
    }
}