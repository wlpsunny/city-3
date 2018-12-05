package com.sensoro.smartcity.widget.dialog;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.util.AppUtils;

public class TipOperationDialogUtils {

    //    private AlertDialog mDialog;
    private final TextView mTvMessage;
    private final TextView mTvCancel;
    private final TextView mTvConfirm;
    private final TextView mTvTitle;
    private final LinearLayout mLlEtRoot;
    private final EditText mEt;
    private TipDialogUtilsClickListener listener;
    private CustomCornerDialog mDialog;
    private Activity mActivity;
    public static final int REQUEST_CODE_BLUETOOTH_ON = 0x222;

    public TipOperationDialogUtils(Activity activity, boolean cancelable) {
        this(activity);
        mDialog.setCancelable(cancelable);
    }

    public TipOperationDialogUtils(Activity activity) {
        mActivity = activity;
        View view = View.inflate(activity, R.layout.item_dialog_monitor_point_operation, null);
        mTvTitle = view.findViewById(R.id.dialog_tip_operation_tv_title);
        mTvMessage = view.findViewById(R.id.dialog_tip_operation_tv_message);
        mTvCancel = view.findViewById(R.id.dialog_tip_operation_tv_cancel);
        mTvConfirm = view.findViewById(R.id.dialog_tip_operation_tv_confirm);
        mLlEtRoot = view.findViewById(R.id.dialog_operation_ll_et_root);
        mEt = view.findViewById(R.id.dialog_operation_et);
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setView(view);
//        builder.setCancelable(false);
//        mDialog = builder.create();
//        Window window = mDialog.getWindow();
//        if (window != null) {
//            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        }

        mDialog = new CustomCornerDialog(activity, R.style.CustomCornerDialogStyle, view,true);

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                if (listener != null) {
                    listener.onCancelClick();

                }
            }
        });

        mTvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLlEtRoot.getVisibility() == View.VISIBLE){
                    listener.onConfirmClick(mEt.getText().toString());
                }else{
                    listener.onConfirmClick(null);
                }
            }
        });

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
//                AppUtils.dismissInputMethodManager(mActivity,mEt);
//                mEt.setEnabled(false);
            }
        });

    }

    public boolean isShowing() {
        if (mDialog != null) {
            return mDialog.isShowing();
        }
        return false;
    }

    public void setTipTitleText(String text) {
        mTvTitle.setText(text);
    }

    public void setTipMessageText(String text) {
        mTvMessage.setText(text);
    }

    public void setTipCacnleText(String text, @ColorInt int color) {
        mTvCancel.setText(text);
        mTvCancel.setTextColor(color);
    }

    public void setTipConfirmText(String text, @ColorInt int color) {
        mTvConfirm.setText(text);
        mTvConfirm.setTextColor(color);
    }

    public void show() {
        if (mDialog != null) {
            mEt.getText().clear();
            mDialog.show();
//            WindowManager m = mDialog.getWindow().getWindowManager();
//            Display d = m.getDefaultDisplay();
//            WindowManager.LayoutParams p = mDialog.getWindow().getAttributes();
//            p.width = d.getWidth() - 100;
//            mDialog.getWindow().setAttributes(p);
        }
    }


    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    public void destroy() {
        if (mDialog != null) {
            mDialog.cancel();
            mDialog = null;
        }
    }

    public void setTipDialogUtilsClickListener(TipDialogUtilsClickListener listener) {
        this.listener = listener;
    }

    public void setTipConfirmVisible(boolean isVisible) {
        mTvConfirm.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void setTipEtRootVisible(boolean isVisible) {
        mLlEtRoot.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }



    public interface TipDialogUtilsClickListener {
        void onCancelClick();

        void onConfirmClick(String content);
    }

}