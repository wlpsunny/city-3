package com.sensoro.smartcity.model;

import com.sensoro.smartcity.R;
import com.sensoro.common.model.SecurityRisksAdapterModel;

import java.util.ArrayList;
import java.util.List;

public class AlarmPopupModel {
    public boolean isSecurityRiskRequire;
    public boolean securityRiskVisible;
    public String title;
    public List<AlarmPopupSubModel> subAlarmPopupModels = new ArrayList<>();
    public List<AlarmPopupTagModel> mainTags = new ArrayList<>();
    public String desc;
    public String mRemark;
    public int resButtonBg = R.drawable.shape_button;
    public String mergeType;
    public String sensorType;
    public ArrayList<SecurityRisksAdapterModel> securityRisksList = new ArrayList<>();
    public int alarmStatus;
    public Long updateTime;

    public static class AlarmPopupSubModel {
        public boolean isRequire;
        public String title;
        public String tips;
        public String key;
        public List<AlarmPopupTagModel> subTags;
    }

    public static class AlarmPopupTagModel {
        public Integer id;
        public boolean isChose;
        public String name;
        public boolean isRequire;
        public int resDrawable = R.drawable.shape_bg_solid_29c_20dp_corner;
    }

}
