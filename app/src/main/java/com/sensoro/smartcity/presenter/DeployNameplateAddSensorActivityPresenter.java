package com.sensoro.smartcity.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.sensoro.common.base.BasePresenter;
import com.sensoro.smartcity.activity.DeployNameplateAddSensorFromListActivity;
import com.sensoro.smartcity.imainviews.IDeployNameplateAddSensorActivityView;

public class DeployNameplateAddSensorActivityPresenter extends BasePresenter<IDeployNameplateAddSensorActivityView> {
    private Activity mActivity;

    @Override
    public void initData(Context context) {
        mActivity = (Activity) context;
    }

    @Override
    public void onDestroy() {

    }

    public void doAddFromList() {
        Intent intent = new Intent(mActivity, DeployNameplateAddSensorFromListActivity.class);
        getView().startAC(intent);
    }
}
