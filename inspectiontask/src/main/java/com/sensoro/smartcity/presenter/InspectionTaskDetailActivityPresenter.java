package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.helper.PreferencesHelper;
import com.sensoro.common.iwidget.IOnCreate;
import com.sensoro.common.model.EventData;
import com.sensoro.common.server.CityObserver;
import com.sensoro.common.server.RetrofitServiceHelper;
import com.sensoro.common.server.bean.ChangeInspectionTaskStateInfo;
import com.sensoro.common.server.bean.InspectionIndexTaskInfo;
import com.sensoro.common.server.bean.InspectionTaskExecutionModel;
import com.sensoro.common.server.response.ResponseResult;
import com.sensoro.common.utils.DateUtil;
import com.sensoro.common.utils.WidgetUtil;
import com.sensoro.inspectiontask.R;
import com.sensoro.smartcity.activity.InspectionInstructionActivity;
import com.sensoro.smartcity.activity.InspectionTaskActivity;
import com.sensoro.smartcity.constant.InspectionConstant;
import com.sensoro.smartcity.imainviews.IInspectionTaskDetailActivityView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class InspectionTaskDetailActivityPresenter extends BasePresenter<IInspectionTaskDetailActivityView>
        implements IOnCreate {
    private Activity mContext;
    private InspectionIndexTaskInfo mTaskInfo;

    @Override
    public void initData(Context context) {
        mContext = (Activity) context;
        onCreate();
        mTaskInfo = (InspectionIndexTaskInfo) mContext.getIntent().getSerializableExtra(Constants.EXTRA_INSPECTION_INDEX_TASK_INFO);
        if (mTaskInfo != null) {
            String identifier = mTaskInfo.getIdentifier();
            if (!TextUtils.isEmpty(identifier)) {
                getView().setTvTaskNumber(identifier);
            }
            getView().setTvTaskTime(DateUtil.getDateByOtherFormatPoint(mTaskInfo.getBeginTime()) + " - " + DateUtil.getDateByOtherFormatPoint(mTaskInfo.getEndTime()));
            freshTvState(mTaskInfo.getStatus());
            initDeviceTag();
        }
    }

    private void initDeviceTag() {
        ArrayList<String> tags = new ArrayList<>();

        List<InspectionIndexTaskInfo.DeviceSummaryBean> deviceSummary = mTaskInfo.getDeviceSummary();
        for (InspectionIndexTaskInfo.DeviceSummaryBean deviceSummaryBean : deviceSummary) {
            String deviceType = deviceSummaryBean.getDeviceType();
            String inspectionDeviceName = WidgetUtil.getInspectionDeviceName(deviceType);
            tags.add(inspectionDeviceName + " （" + deviceSummaryBean.getNum() + "） ");
        }
        getView().updateTagsData(tags);
    }

    private void freshTvState(int status) {
        switch (status) {
            case InspectionConstant.TASK_STATUS_PEDNING_EXC:
                getView().setTvbtnStartState(R.drawable.shape_bg_inspectiontask_corner_29c_shadow, R.color.white, mContext.getString(R.string.inspection_task_detail_start_inspection));
                break;
            case InspectionConstant.TASK_STATUS_EXCING:
            case InspectionConstant.TASK_STATUS_TIMEOUE_UNDONE:
                getView().setTvbtnStartState(R.drawable.shape_bg_inspectiontask_corner_29c_shadow, R.color.white,  mContext.getString(R.string.inspection_task_detail_go_on_inspection));
                break;
            case InspectionConstant.TASK_STATUS_DONE:
            case InspectionConstant.TASK_STATUS_TIMEOUE_DONE:
                getView().setTvbtnStartState(R.drawable.shape_bg_solid_ff_corner, R.color.c_252525,  mContext.getString(R.string.inspection_task_detail_title));
                break;
        }

        setTvState(status);

    }
    private void setTvState(int status){
        int color=R.color.c_8058a5;
        String text=mContext.getString(R.string.inspection_status_text_pending_execution);
        switch (status) {
            case 0:
                color=R.color.c_8058a5;
                text=mContext.getString(R.string.inspection_status_text_pending_execution);
                break;
            case 1:
                color=R.color.c_3aa7f0;
                text=mContext.getString(R.string.inspection_status_text_executing);
                break;
            case 2:
                color=R.color.c_ff8d34;
                text=mContext.getString(R.string.inspection_status_text_timeout_not_completed);
                break;
            case 3:
                color=R.color.c_1dbb99;
                text=mContext.getString(R.string.inspection_status_text_completed);
                break;
            case 4:
                color=R.color.c_a6a6a6;
                text=mContext.getString(R.string.inspection_status_text_timeout_completed);
                break;
        }
        getView().setTvState(color, text);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData eventData) {
        int code = eventData.code;
        Object data = eventData.data;
        //上报异常结果成功
        switch (code) {
            case Constants.EVENT_DATA_DEPLOY_RESULT_FINISH:
                getView().finishAc();
                break;
            case Constants.EVENT_DATA_INSPECTION_UPLOAD_EXCEPTION_CODE:
            case Constants.EVENT_DATA_INSPECTION_UPLOAD_NORMAL_CODE:
            case Constants.EVENT_DATA_DEPLOY_RESULT_CONTINUE:
                // todo 刷新任务状态
                refreshTaskState();
                break;
        }

    }

    public void doRlContent() {
        Intent intent = new Intent(mContext, InspectionInstructionActivity.class);
        List<InspectionIndexTaskInfo.DeviceSummaryBean> deviceSummary = mTaskInfo.getDeviceSummary();
        ArrayList<String> deviceTypes = new ArrayList<>();
        for (InspectionIndexTaskInfo.DeviceSummaryBean deviceSummaryBean : deviceSummary) {
            deviceTypes.add(deviceSummaryBean.getDeviceType());
        }
        intent.putExtra(Constants.EXTRA_INSPECTION_INSTRUCTION_DEVICE_TYPE, deviceTypes);
        getView().startAC(intent);
    }

    //与ios逻辑一致
    public void doBtnStart() {
        if (PreferencesHelper.getInstance().getUserData().hasInspectionDeviceList) {
            if (mTaskInfo.getStatus() == 0 || mTaskInfo.getStatus() == 2) {
                changeTaskState();
            } else {
                Intent intent = new Intent(mContext, InspectionTaskActivity.class);
                intent.putExtra(Constants.EXTRA_INSPECTION_INDEX_TASK_INFO, mTaskInfo);
                getView().startAC(intent);
            }
        } else {
            getView().toastShort(mContext.getString(R.string.account_does_not_have_permission_to_view_the_inspection_device_list));
        }

    }

    private void changeTaskState() {
        getView().showProgressDialog();
        RetrofitServiceHelper.getInstance().doChangeInspectionTaskState(mTaskInfo.getId(), null, 1).
                subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<ChangeInspectionTaskStateInfo>>(this) {
            @Override
            public void onCompleted(ResponseResult<ChangeInspectionTaskStateInfo> changeInspectionTaskStateRsp) {
                ChangeInspectionTaskStateInfo data = changeInspectionTaskStateRsp.getData();
                int status = data.getStatus();
                Intent intent = new Intent(mContext, InspectionTaskActivity.class);
                intent.putExtra(Constants.EXTRA_INSPECTION_INDEX_TASK_INFO, mTaskInfo);
                getView().startAC(intent);
                freshTvState(status);
                EventData eventData = new EventData();
                eventData.code = Constants.EVENT_DATA_INSPECTION_TASK_STATUS_CHANGE;
                EventBus.getDefault().post(eventData);
                getView().dismissProgressDialog();
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {
                getView().dismissProgressDialog();
                getView().toastShort(errorMsg);

            }
        });
    }

    private void refreshTaskState() {
        RetrofitServiceHelper.getInstance().getInspectTaskExecution(mTaskInfo.getId()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new CityObserver<ResponseResult<InspectionTaskExecutionModel>>(this) {
            @Override
            public void onCompleted(ResponseResult<InspectionTaskExecutionModel> inspectionTaskExecutionRsp) {
                InspectionTaskExecutionModel data = inspectionTaskExecutionRsp.getData();
                InspectionTaskExecutionModel.BaseInfoBean baseInfo = data.getBaseInfo();
                if (baseInfo != null) {
                    int status = baseInfo.getStatus();
                    mTaskInfo.setStatus(status);
                    freshTvState(status);
                }
            }

            @Override
            public void onErrorMsg(int errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);
    }
}
