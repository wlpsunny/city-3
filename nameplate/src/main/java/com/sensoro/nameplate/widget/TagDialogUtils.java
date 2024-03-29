package com.sensoro.nameplate.widget;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sensoro.common.utils.AppUtils;
import com.sensoro.common.widgets.CustomCornerDialog;
import com.sensoro.nameplate.R;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class TagDialogUtils implements View.OnClickListener {
    private final Activity mActivity;
    private CustomCornerDialog mAddTagDialog;
    private EditText mDialogEtInput;
    private ImageView mDialogImvClear;
    private TextView mDialogTvCancel;
    private TextView mDialogTvConfirm;
    private OnTagDialogListener onTagDialogListener;
    public static final int DIALOG_TAG_ADD = 1;
    public static final int DIALOG_TAG_EDIT = 2;
    private int mType = -1;
    private int currentPosition = -1;

    public TagDialogUtils(final Activity activity) {
        View view = View.inflate(activity, R.layout.dialog_frag_deploy_device_add_tag, null);
        mDialogEtInput = view.findViewById(R.id.dialog_add_tag_et_input);
        mDialogImvClear = view.findViewById(R.id.dialog_add_tag_imv_clear);
        mDialogTvCancel = view.findViewById(R.id.dialog_add_tag_tv_cancel);
        mDialogTvConfirm = view.findViewById(R.id.dialog_add_tag_tv_confirm);
        mDialogTvConfirm.setOnClickListener(this);
        mDialogTvCancel.setOnClickListener(this);
        mDialogImvClear.setOnClickListener(this);
        mDialogEtInput.setOnClickListener(this);
        mDialogEtInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return (event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        mAddTagDialog = new CustomCornerDialog(activity, R.style.CustomCornerDialogStyle, view);
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setView(view);
//        builder.setCancelable(false);
        mActivity = activity;
        mAddTagDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                currentPosition = -1;
                mDialogEtInput.getText().clear();
            }
        });
        mAddTagDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                currentPosition = -1;
                mDialogEtInput.getText().clear();
            }
        });


//        mAddTagDialog = builder.create();
//        Window window = mAddTagDialog.getWindow();
//        if (window != null) {
//            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        }


    }

    public void registerListener(OnTagDialogListener onTagDialogListener) {
        this.onTagDialogListener = onTagDialogListener;
    }

    public interface OnTagDialogListener {
        void onConfirm(int type, String text, int position);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (R.id.dialog_add_tag_tv_cancel == id) {
            AppUtils.dismissInputMethodManager(mActivity, mDialogEtInput);
            dismissDialog();
        } else if (R.id.dialog_add_tag_tv_confirm == id) {
            if (mDialogEtInput != null) {
                AppUtils.dismissInputMethodManager(mActivity, mDialogEtInput);
                String tag = mDialogEtInput.getText().toString();
                if (onTagDialogListener != null) {
                    onTagDialogListener.onConfirm(mType, tag, currentPosition);
                }
            }
        } else if (R.id.dialog_add_tag_et_input == id) {
            mDialogEtInput.requestFocus();
            mDialogEtInput.setCursorVisible(true);

        } else if (R.id.dialog_add_tag_imv_clear == id) {
            if (mDialogEtInput != null) {
                mDialogEtInput.getText().clear();
            }
        } else {

        }
    }

    public void unregisterListener() {
        if (mAddTagDialog != null) {
            mAddTagDialog.cancel();
        }
        if (this.onTagDialogListener != null) {
            this.onTagDialogListener = null;
        }
    }

    public void show() {
        if (mAddTagDialog != null) {
            mType = DIALOG_TAG_ADD;
            mAddTagDialog.show();
            mDialogEtInput.setCursorVisible(true);
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(mDialogEtInput, InputMethodManager.SHOW_IMPLICIT);
            showSoftKeyboard();
        }
    }

    public void show(String text, int position) {
        if (mAddTagDialog != null) {
            mType = DIALOG_TAG_EDIT;
            mDialogEtInput.setText(text);
            String string = mDialogEtInput.getText().toString();
            mDialogEtInput.setSelection(string.length());
            currentPosition = position;
            mAddTagDialog.show();
            mDialogEtInput.setCursorVisible(true);
            showSoftKeyboard();
        }

    }

    public void dismissDialog() {
        if (mAddTagDialog != null) {
            mAddTagDialog.dismiss();
            hideSoftKeyboard(mActivity);
        }

    }


    public void showSoftKeyboard() {

        mDialogEtInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(mDialogEtInput, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 300);

    }

    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
