package com.sensoro.smartcity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.widgets.BoldTextView;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.imainviews.ISettingNotificationActivityView;
import com.sensoro.smartcity.presenter.SettingNotificationActivityPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingNotificationActivity extends BaseActivity<ISettingNotificationActivityView, SettingNotificationActivityPresenter> implements ISettingNotificationActivityView {


    @BindView(R.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R.id.include_text_title_tv_title)
    BoldTextView includeTextTitleTvTitle;
    @BindView(R.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R.id.include_text_title_divider)
    View includeTextTitleDivider;
    @BindView(R.id.include_text_title_cl_root)
    ConstraintLayout includeTextTitleClRoot;
    @BindView(R.id.devicein_sw)
    Switch deviceinSw;
    @BindView(R.id.devicein_edit)
    EditText deviceinEdit;
    @BindView(R.id.deviceout_sw)
    Switch deviceoutSw;
    @BindView(R.id.deviceout_edit)
    EditText deviceoutEdit;
    @BindView(R.id.ll_device_uuid)
    LinearLayout llDeviceUuid;
    @BindView(R.id.tv_device_uuid)
    TextView tvDeviceUuid;
    @BindView(R.id.et_device_major)
    EditText etDeviceMajor;
    @BindView(R.id.et_device_mirror)
    EditText etDeviceMirror;


    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_settingnotification);

        ButterKnife.bind(this);
        initView();
        mPresenter.initData(mActivity);
//        deviceinSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//            }
//        });
//        deviceoutSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//            }
//        });
    }

    private void initView() {
        includeTextTitleTvTitle.setText("设备通知提示");
        includeTextTitleTvSubtitle.setText("保存");
    }


    @Override
    protected void onDestroy() {


        super.onDestroy();
    }

    @Override
    protected SettingNotificationActivityPresenter createPresenter() {
        return new SettingNotificationActivityPresenter();
    }


    @OnClick({R.id.include_text_title_imv_arrows_left, R.id.include_text_title_tv_subtitle, R.id.ll_device_uuid})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.include_text_title_imv_arrows_left:
                finish();
                break;
            case R.id.include_text_title_tv_subtitle:

                //保存

                boolean deviceinSwstate = deviceinSw.isChecked();
                String devicein = deviceinEdit.getText().toString().trim();
                boolean deviceoutSwstate = deviceoutSw.isChecked();
                String deviceout = deviceoutEdit.getText().toString().trim();
                String currentUUid = tvDeviceUuid.getText().toString().trim();
                String major = etDeviceMajor.getText().toString().trim();
                String mirror = etDeviceMirror.getText().toString().trim();
                mPresenter.doSave(deviceinSwstate, devicein, deviceoutSwstate, deviceout, currentUUid, major, mirror);
                break;
            case R.id.ll_device_uuid:
                mPresenter.goSettingUUID(tvDeviceUuid.getText().toString());
                break;
        }
    }

    @Override
    public void setDeviceInChecked(boolean isChecked) {
        deviceinSw.setChecked(isChecked);
    }

    @Override
    public void setDeviceOutChecked(boolean isChecked) {
        deviceoutSw.setChecked(isChecked);
    }

    @Override
    public void setDeviceInEditContent(String text) {
        if (text != null) {
            deviceinEdit.setText(text);
        }

    }

    @Override
    public void setDeviceOutEditContent(String text) {
        if (text != null) {
            deviceoutEdit.setText(text);
        }

    }

    @Override
    public void setDeviceUUID(String uuid) {
        if (uuid != null) {
            tvDeviceUuid.setText(uuid);
        }
    }

    @Override
    public void setDeviceMajor(String major) {
        if (major != null) {
            etDeviceMajor.setText(major);
            etDeviceMajor.setSelection(major.length());
        }
    }

    @Override
    public void setDeviceMirror(String mirror) {
        if (mirror != null) {
            etDeviceMirror.setText(mirror);
            etDeviceMirror.setSelection(mirror.length());
        }
    }

    @Override
    public void startAC(Intent intent) {
        mActivity.startActivity(intent);
    }

    @Override
    public void finishAc() {
        mActivity.finish();
    }

    @Override
    public void startACForResult(Intent intent, int requestCode) {

    }

    @Override
    public void setIntentResult(int resultCode) {

    }

    @Override
    public void setIntentResult(int resultCode, Intent data) {

    }
}
