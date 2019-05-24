package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.sensoro.common.base.BasePresenter;
import com.sensoro.common.model.EventData;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.activity.DeployDeviceTagActivity;
import com.sensoro.smartcity.activity.DeployMonitorDeployPicActivity;
import com.sensoro.smartcity.activity.DeployNameplateAddSensorActivity;
import com.sensoro.smartcity.activity.DeployNameplateNameActivity;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.imainviews.IDeployNameplateActivityView;

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
            Intent intent = new Intent(mActivity, DeployDeviceTagActivity.class);
//            if (deployAnalyzerModel.tagList.size() > 0) {
//                intent.putStringArrayListExtra(EXTRA_SETTING_TAG_LIST, (ArrayList<String>) deployAnalyzerModel.tagList);
//            }
            getView().startAC(intent);
    }

    public void doPic() {
        Intent intent = new Intent(mActivity, DeployMonitorDeployPicActivity.class);
//        if (getRealImageSize() > 0) {
//            intent.putExtra(EXTRA_DEPLOY_TO_PHOTO, deployAnalyzerModel.images);
//        }
//        intent.putExtra(Constants.EXTRA_SETTING_DEPLOY_DEVICE_TYPE, deployAnalyzerModel.deviceType);
        getView().startAC(intent);
    }

    public void doAssociationSensor() {
        Intent intent = new Intent(mActivity, DeployNameplateAddSensorActivity.class);
        getView().startAC(intent);
    }
}