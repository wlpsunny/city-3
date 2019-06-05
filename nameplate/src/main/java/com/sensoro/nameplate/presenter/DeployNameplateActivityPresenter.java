package com.sensoro.nameplate.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.constant.ARouterConstants;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.model.EventData;
import com.sensoro.nameplate.R;
import com.sensoro.nameplate.activity.DeployNameplateAddSensorActivity;
import com.sensoro.nameplate.activity.DeployNameplateNameActivity;
import com.sensoro.nameplate.IMainViews.IDeployNameplateActivityView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class DeployNameplateActivityPresenter extends BasePresenter<IDeployNameplateActivityView> {
    private Activity mActivity;

    @Override
    public void initData(Context context) {
        mActivity = (Activity) context;
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventData eventData) {
        Object data = eventData.data;
        switch (eventData.code) {
            case Constants.EVENT_DATA_DEPLOY_NAMEPLATE_NAME:
                getView().setName((String)data, R.color.c_252525);
                break;
            case Constants.EVENT_DATA_DEPLOY_SETTING_TAG:

                if (data instanceof List) {
//                    deployAnalyzerModel.tagList.clear();
//                    deployAnalyzerModel.tagList.addAll((List<String>) data);
                    getView().updateTagsData((List<String>) data);
                }
                break;
            case Constants.EVENT_DATA_DEPLOY_SETTING_PHOTO:
                if (data instanceof List) {
//                    deployAnalyzerModel.images.clear();
//
//                    deployAnalyzerModel.images.addAll((ArrayList<ImageItem>) data);
//
//                    if (getRealImageSize() > 0) {
//                        getView().setDeployPhotoText(mContext.getString(R.string.added) + getRealImageSize() + mContext.getString(R.string.images));
//                    } else {
//                        getView().setDeployPhotoText(mContext.getString(R.string.not_added));
//                    }
//                    getView().setUploadBtnStatus(checkCanUpload());
                }
                break;
        }
    }
    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    public void doName(String name) {
        Intent intent = new Intent(mActivity, DeployNameplateNameActivity.class);
        intent.putExtra(Constants.EXTRA_DEPLOY_NAMEPLATE_NAME,name);
        getView().startAC(intent);
    }

    public void doTag() {
        Bundle bundle = new Bundle();
//        if (deployAnalyzerModel.tagList.size() > 0) {
//            bundle.putStringArrayList(Constants.EXTRA_SETTING_TAG_LIST, (ArrayList<String>) deployAnalyzerModel.tagList);
//        }
        startActivity(ARouterConstants.ACTIVITY_DEPLOY_DEVICE_TAG,bundle,mActivity);
    }

    public void doPic() {
//        Intent intent = new Intent(mActivity, DeployMonitorDeployPicActivity.class);
//////        if (getRealImageSize() > 0) {
//////            intent.putExtra(EXTRA_DEPLOY_TO_PHOTO, deployAnalyzerModel.images);
//////        }
//////        intent.putExtra(Constants.EXTRA_SETTING_DEPLOY_DEVICE_TYPE, deployAnalyzerModel.deviceType);
////        getView().startAC(intent);

        Bundle bundle = new Bundle();
        bundle.putString(Constants.EXTRA_SETTING_DEPLOY_DEVICE_TYPE, "deploy_nameplate");
        startActivity(ARouterConstants.ACTIVITY_DEPLOY_DEVICE_PIC,bundle,mActivity);
    }

    public void doAssociationSensor() {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.EXTRA_ASSOCIATION_SENSOR_ORIGIN_TYPE,"deploy");
        startActivity(ARouterConstants.ACTIVITY_DEPLOY_ASSOCIATE_SENSOR,bundle,mActivity);
    }
}
