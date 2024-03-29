package com.sensoro.city_camera.IMainViews;

import com.sensoro.common.iwidget.IActivityIntent;
import com.sensoro.common.iwidget.IProgressDialog;
import com.sensoro.common.iwidget.IToast;
import com.sensoro.common.server.security.bean.SecurityAlarmDetailInfo;
import com.sensoro.common.server.security.bean.SecurityAlarmEventInfo;
import com.sensoro.common.server.security.bean.SecurityAlarmInfo;
import com.sensoro.common.server.security.bean.SecurityCameraInfo;

import java.util.List;

/**
 * @author bin.tian
 */
public interface ISecurityWarnDetailView extends IActivityIntent, IProgressDialog, IToast {
    /**
     * 显示安防预警确认弹窗
     * @param securityAlarmDetailInfo
     */
    void showConfirmDialog(SecurityAlarmDetailInfo securityAlarmDetailInfo);
    /**
     * 显示摄像头信息详情弹窗
     * @param securityAlarmDetailInfo
     */
    void showCameraDetailsDialog(SecurityAlarmDetailInfo securityAlarmDetailInfo);

    /**
     * 显示布控信息详情
     * @param securityAlarmDetailInfo
     */
    void showDeployDetail(SecurityAlarmDetailInfo securityAlarmDetailInfo);

    /**
     * 更新安防预警详情数据
     * @param securityAlarmInfo
     */
    void updateSecurityWarnDetail(SecurityAlarmInfo securityAlarmInfo);

    /**
     * 更新预警修改数据
     * @param list
     */
    void updateSecurityWarnTimeLine(List<SecurityAlarmEventInfo> list);

    /**
     * 更新确认信息
     * @param securityAlarmInfo
     */
    void updateSecurityConfirmResult(SecurityAlarmInfo securityAlarmInfo);

    /**
     * 是否可以查看预警录像
     * @param isVideoRecordEnable
     */
    void updateVideoRecordEnable(boolean isVideoRecordEnable);

    /**
     * 刷新相机详情弹框数据
     */
    void onRefreshCameraDetailsData(SecurityCameraInfo securityCameraInfo);
}
