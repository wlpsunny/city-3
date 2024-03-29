package com.sensoro.common.widgets.dialog;

import android.app.Activity;
import androidx.annotation.ColorInt;
import android.view.View;
import android.widget.TextView;

import com.sensoro.common.R;
import com.sensoro.common.widgets.CustomCornerDialog;

public class TipDialogUtils {

//    private AlertDialog mDialog;
    private final TextView mTvMessage;
    private final TextView mTvCancel;
    private final TextView mTvConfirm;
    private TipDialogUtilsClickListener listener;
    private CustomCornerDialog mDialog;

    public TipDialogUtils(Activity activity) {
        View view = View.inflate(activity, R.layout.item_dialog_tip, null);
        mTvMessage = view.findViewById(R.id.dialog_tip_tv_message);
        mTvCancel = view.findViewById(R.id.dialog_tip_tv_cancel);
        mTvConfirm = view.findViewById(R.id.dialog_tip_tv_confirm);
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setView(view);
//        builder.setCancelable(false);
//        mDialog = builder.create();
//        Window window = mDialog.getWindow();
//        if (window != null) {
//            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        }

        mDialog = new CustomCornerDialog(activity, R.style.CustomCornerDialogStyle,view);

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCancelClick();
                }
            }
        });

        mTvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirmClick();
                }
            }
        });

    }

    public void setTipMessageText(String text){
        mTvMessage.setText(text);
    }
    public void setTipCacnleText(String text, @ColorInt int color){
        mTvCancel.setText(text);
        mTvConfirm.setTextColor(color);
    }
    public void setTipConfirmText(String text,@ColorInt int color){
        mTvConfirm.setText(text);
        mTvConfirm.setTextColor(color);
    }

    public void show(){
        if (mDialog != null) {
            mDialog.show();
//            WindowManager m = mDialog.getWindow().getWindowManager();
//            Display d = m.getDefaultDisplay();
//            WindowManager.LayoutParams p = mDialog.getWindow().getAttributes();
//            p.width = d.getWidth() - 100;
//            mDialog.getWindow().setAttributes(p);
        }
    }

    public void dismiss(){
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    public void destory(){
        if(mDialog != null){
            mDialog.cancel();
            mDialog = null;
        }
    }

    public void setTipDialogUtilsClickListener(TipDialogUtilsClickListener listener){
        this.listener = listener;
    }

    public interface TipDialogUtilsClickListener {
        void onCancelClick();

        void onConfirmClick();
    }

}
