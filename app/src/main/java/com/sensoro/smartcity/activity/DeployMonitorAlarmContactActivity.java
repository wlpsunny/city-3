package com.sensoro.smartcity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sensoro.common.base.BaseActivity;
import com.sensoro.common.callback.RecycleViewItemClickListener;
import com.sensoro.common.manger.SensoroLinearLayoutManager;
import com.sensoro.common.model.DeployContactModel;
import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.utils.SoftHideKeyBoardUtil;
import com.sensoro.common.widgets.SensoroToast;
import com.sensoro.common.widgets.TipOperationDialogUtils;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.AlarmContactHistoryAdapter;
import com.sensoro.smartcity.adapter.AlarmContactRcContentAdapter;
import com.sensoro.smartcity.imainviews.IAlarmContactActivityView;
import com.sensoro.smartcity.presenter.AlarmContactActivityPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeployMonitorAlarmContactActivity extends BaseActivity<IAlarmContactActivityView, AlarmContactActivityPresenter>
        implements IAlarmContactActivityView, RecycleViewItemClickListener, TipOperationDialogUtils.TipDialogUtilsClickListener, AlarmContactRcContentAdapter.OnAlarmContactAdapterListener {

    @BindView(R.id.alarm_contact_tv_add)
    TextView alarmContactTvAdd;
    @BindView(R.id.include_text_title_tv_cancel)
    TextView includeTextTitleTvCancel;
    @BindView(R.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R.id.rc_ac_deploy_alarm_contact_history)
    RecyclerView rcAcDeployAlarmContactHistory;
    //    @BindView(R.id.ac_name_address_et_alarm_contact_name)
//    EditText acNameAddressEtAlarmContactName;
//    @BindView(R.id.ac_name_address_et_alarm_contact_phone)
//    EditText acNameAddressEtAlarmContactPhone;
    @BindView(R.id.iv_ac_name_address_delete_tag)
    ImageView ivAcDeployAlarmContactDeleteHistory;
    //    @BindView(R.id.ac_name_address_ll_add_name_phone)
//    LinearLayout acNameAddressLlAddNamePhone;
    @BindView(R.id.rc_add_alarm_contact)
    RecyclerView rcAddAlarmContactRv;
    @BindView(R.id.item_adapter_alarm_contact_add_ll)
    LinearLayout itemAdapterAlarmContactAddLl;
    private AlarmContactHistoryAdapter mHistoryAdapter;
    private AlarmContactRcContentAdapter mAlarmContactRcContentAdapter;
    //    private AlarmContactRcContentAdapter mAlarmContactRcContentAdapter;
    private TipOperationDialogUtils historyClearDialog;

    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_alarm_contact);
        ButterKnife.bind(this);
        SoftHideKeyBoardUtil.assistActivity(this);
        initView();
        mPresenter.initData(mActivity);

    }

    private void initView() {
        //TODO 暂不支持多个联系人，所以先不做喽
        initRcContent();
        includeTextTitleTvTitle.setText(R.string.alert_contact);
        includeTextTitleTvSubtitle.setVisibility(View.GONE);
        initTitle();


        mAlarmContactRcContentAdapter.setOnAlarmContactAdapterListener(this);


        //键盘遮挡
//        root_ll_rc.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//
//            @Override
//            public void onGlobalLayout() {
//                Rect rect = new Rect();
////获取root在窗体的可视区域
//                root_ll_rc.getWindowVisibleDisplayFrame(rect);
////获取root在窗体的不可视区域高度(被其他View遮挡的区域高度)
//                int rootInvisibleHeight = root_ll_rc.getRootView().getHeight() - rect.bottom;
////若不可视区域高度大于100，则键盘显示
//                if (rootInvisibleHeight > 100) {
//                    int[] location = new int[2];
////获取scrollToView在窗体的坐标
//                    rcAddAlarmContactRv.getLocationInWindow(location);
////计算root滚动高度，使scrollToView在可见区域的底部
//                    int srollHeight = (location[1] + rcAddAlarmContactRv.getHeight()) - rect.bottom;
//                    rcAddAlarmContactRv.scrollTo(0, srollHeight);
//                } else {
////键盘隐藏
////                    rootView.scrollTo(0, 0);
//                }
//            }
//        });

        View rootView = findViewById(R.id.rc_root_add_alarm_contact);


        //底部按钮顶上去

        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom - oldBottom < -1) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            0);
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    itemAdapterAlarmContactAddLl.setLayoutParams(params);

                } else if (bottom - oldBottom > 1) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                            (int) AppUtils.dp2px(mActivity, 45));
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    itemAdapterAlarmContactAddLl.setLayoutParams(params);
                }
            }
        });
        initRcHistory();
        initClearHistoryDialog();

    }

    private void initClearHistoryDialog() {
        historyClearDialog = new TipOperationDialogUtils(mActivity, true);
        historyClearDialog.setTipTitleText(getString(R.string.history_clear_all));
        historyClearDialog.setTipMessageText(getString(R.string.confirm_clear_history_record), R.color.c_a6a6a6);
        historyClearDialog.setTipCancelText(getString(R.string.cancel), getResources().getColor(R.color.c_1dbb99));
        historyClearDialog.setTipConfirmText(getString(R.string.clear), getResources().getColor(R.color.c_a6a6a6));
        historyClearDialog.setTipDialogUtilsClickListener(this);


    }


    private void initRcHistory() {
        mHistoryAdapter = new AlarmContactHistoryAdapter(mActivity);
        mHistoryAdapter.setRecycleViewItemClickListener(this);
        SensoroLinearLayoutManager manager = new SensoroLinearLayoutManager(mActivity);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rcAcDeployAlarmContactHistory.setLayoutManager(manager);
        rcAcDeployAlarmContactHistory.setAdapter(mHistoryAdapter);


        LinearLayoutManager contactManager = new LinearLayoutManager(mActivity);
        contactManager.setOrientation(LinearLayoutManager.VERTICAL);
        rcAddAlarmContactRv.setLayoutManager(contactManager);
        rcAddAlarmContactRv.setAdapter(mAlarmContactRcContentAdapter);


    }

    private void initTitle() {
        includeTextTitleTvTitle.setText(R.string.alert_contact);
        includeTextTitleTvCancel.setVisibility(View.VISIBLE);
        includeTextTitleTvCancel.setTextColor(getResources().getColor(R.color.c_b6b6b6));
        includeTextTitleTvCancel.setText(R.string.cancel);
        includeTextTitleTvSubtitle.setVisibility(View.VISIBLE);
        includeTextTitleTvSubtitle.setText(getString(R.string.save));
        updateSaveStatus(true);
    }

    @Override
    public void updateSaveStatus(boolean isEnable) {
        includeTextTitleTvSubtitle.setEnabled(isEnable);
        includeTextTitleTvSubtitle.setTextColor(isEnable ? getResources().getColor(R.color.c_1dbb99) : getResources().getColor(R.color.c_dfdfdf));

    }

    @Override
    public void showHistoryClearDialog() {
        if (historyClearDialog != null) {
            historyClearDialog.show();
        }
    }


    private void initRcContent() {
        mAlarmContactRcContentAdapter = new AlarmContactRcContentAdapter(this);
    }

    @Override
    protected AlarmContactActivityPresenter createPresenter() {
        return new AlarmContactActivityPresenter();
    }

    @Override
    public void startAC(Intent intent) {

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

    @Override
    public void toastShort(String msg) {
        SensoroToast.getInstance().makeText(msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void toastLong(String msg) {

    }


    @OnClick({R.id.alarm_contact_tv_add, R.id.include_text_title_tv_cancel, R.id.include_text_title_tv_subtitle, R.id.iv_ac_name_address_delete_tag})
    public void onViewClicked(View view) {

        switch (view.getId()) {


            case R.id.alarm_contact_tv_add:
                mAlarmContactRcContentAdapter.addNewDataAdapter();

                break;


            case R.id.include_text_title_tv_cancel:
                AppUtils.dismissInputMethodManager(mActivity);
                finishAc();
                break;
            case R.id.include_text_title_tv_subtitle:
//                AppUtils.dismissInputMethodManager(mActivity, acNameAddressEtAlarmContactName);
//                String name = acNameAddressEtAlarmContactName.getText().toString();
//                String phone = acNameAddressEtAlarmContactPhone.getText().toString();


                mPresenter.doFinish(mAlarmContactRcContentAdapter.mList);
                break;
            case R.id.iv_ac_name_address_delete_tag:
                AppUtils.dismissInputMethodManager(mActivity);
                showHistoryClearDialog();
                break;
//            case R.id.ac_name_address_et_alarm_contact_name:
//                acNameAddressEtAlarmContactName.requestFocus();
//                acNameAddressEtAlarmContactName.setCursorVisible(true);
//                break;
//            case R.id.ac_name_address_et_alarm_contact_phone:
//                acNameAddressEtAlarmContactPhone.requestFocus();
//                acNameAddressEtAlarmContactPhone.setCursorVisible(true);
//                break;
        }
    }


    @Override
    public void updateContactData(ArrayList<DeployContactModel> mdContactModelList) {
        mAlarmContactRcContentAdapter.updateAdapter(mdContactModelList);


    }

    @Override
    public void updateRepeatAdapter(List<Integer> list) {

        mAlarmContactRcContentAdapter.updateRepeatAdapter(list);

    }

    @Override
    public void updateHistoryData(ArrayList<String> mHistoryKeywords) {
        mHistoryAdapter.updateSearchHistoryAdapter(mHistoryKeywords);
        ivAcDeployAlarmContactDeleteHistory.setVisibility(mHistoryKeywords.size() > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(View view, int position) {
        String s = mHistoryAdapter.getSearchHistoryList().get(position);

        if (mAlarmContactRcContentAdapter.mFocusPos != -1) {
            DeployContactModel model = mAlarmContactRcContentAdapter.mList.get(mAlarmContactRcContentAdapter.mFocusPos);

            if (model.clickType == 1) {

                model.name = s;
            } else if (model.clickType == 2) {
                model.phone = s;

            }
            mAlarmContactRcContentAdapter.notifyDataSetChanged();
        }


    }

    @Override
    public void onCancelClick() {
        if (historyClearDialog != null) {
            historyClearDialog.dismiss();

        }

    }

    @Override
    public void onConfirmClick(String content, String diameter) {
        mPresenter.clearTag();
        if (historyClearDialog != null) {
            historyClearDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        if (historyClearDialog != null) {
            historyClearDialog.destroy();
            historyClearDialog = null;
        }
        super.onDestroy();
    }


    @Override
    public void onPhoneFocusChange(boolean hasFocus) {

        mPresenter.updateStatus(1);
    }

    @Override
    public void onNameFocusChange(boolean hasFocus) {
        mPresenter.updateStatus(0);

    }
}
