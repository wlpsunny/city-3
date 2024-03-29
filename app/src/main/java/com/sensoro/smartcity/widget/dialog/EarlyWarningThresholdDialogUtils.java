package com.sensoro.smartcity.widget.dialog;

import android.app.Activity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sensoro.common.widgets.CustomCornerDialog;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.EarlyWarningThresholdDialogUtilsAdapter;
import com.sensoro.smartcity.adapter.model.EarlyWarningthresholdDialogUtilsAdapterModel;

import java.util.List;

public class EarlyWarningThresholdDialogUtils {

    //    private final TextView mTvTitle;
    //    private AlertDialog mDialog;
//    private final TextView mTvMessage;
    private TextView mTvCancel;
    private RecyclerView rvEarlyWarningThreshold;
    private EarlyWarningThresholdDialogUtilsAdapter mAdapter;
    //    private final TextView mTvConfirm;
    private DialogUtilsChangeClickListener listener;
    private CustomCornerDialog mDialog;
    private ImageView ivCancel;
    private ImageView mImvNoContent;
    private TextView tvTitle;

    public EarlyWarningThresholdDialogUtils(Activity activity) {
        this(activity, activity.getString(R.string.warning_threshold));

    }

    public EarlyWarningThresholdDialogUtils(Activity activity, String title) {
        View view = View.inflate(activity, R.layout.item_dialog_elect_threshold, null);
//        mTvTitle = view.findViewById(R.id.dialog_tip_tv_title);
//        mTvMessage = view.findViewById(R.id.dialog_tip_tv_message);
        tvTitle = view.findViewById(R.id.tv_early_warning_threshold_title);
        mTvCancel = view.findViewById(R.id.dialog_tv_change_info);
        mImvNoContent = view.findViewById(R.id.no_content);
        ivCancel = view.findViewById(R.id.iv_cancel);

        tvTitle.setText(title);
        rvEarlyWarningThreshold = view.findViewById(R.id.rv_early_warning_threshold);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvEarlyWarningThreshold.setLayoutManager(layoutManager);
        mAdapter = new EarlyWarningThresholdDialogUtilsAdapter(activity);
        rvEarlyWarningThreshold.setAdapter(mAdapter);
//        mTvConfirm = view.findViewById(R.id.dialog_tip_tv_confirm);
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        builder.setView(view);
//        builder.setCancelable(false);
//        mDialog = builder.create();
//        Window window = mDialog.getWindow();
//        if (window != null) {
//            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        }

        mDialog = new CustomCornerDialog(activity, R.style.CustomCornerDialogStyle, view, 560 / 750f);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onChangeInfoClick();
                }
            }
        });

//        mTvConfirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (listener != null) {
//                    listener.onConfirmClick();
//                }
//            }
//        });

    }

//    public void setTipMessageText(String text) {
//        mTvMessage.setText(text);
//    }

//    public void setTipCancelText(String text, @ColorInt int color) {
//        mTvCancel.setText(text);
//        mTvConfirm.setTextColor(color);
//    }

    //    public void setTipConfirmText(String text, @ColorInt int color) {
//        mTvConfirm.setText(text);
//        mTvConfirm.setTextColor(color);
//    }
    public void updateEarlyWarningThresholdAdapter(List<EarlyWarningthresholdDialogUtilsAdapterModel> data) {
        if (data == null || data.size() == 0) {
            mImvNoContent.setVisibility(View.VISIBLE);
            rvEarlyWarningThreshold.setVisibility(View.GONE);
        } else {
            mImvNoContent.setVisibility(View.GONE);
            rvEarlyWarningThreshold.setVisibility(View.VISIBLE);
            mAdapter.updateList(data);
        }

    }

    public void show() {
        if (mDialog != null) {
            mDialog.show();
        }
    }

    public void show(List<EarlyWarningthresholdDialogUtilsAdapterModel> data) {
        if (mDialog != null) {
            updateEarlyWarningThresholdAdapter(data);
            mDialog.show();
        }
    }

    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    public void destory() {
        if (mDialog != null) {
            mDialog.cancel();
            mDialog = null;
        }
    }

    public void setDialogUtilsChangeClickListener(DialogUtilsChangeClickListener listener) {
        this.listener = listener;
    }

    public interface DialogUtilsChangeClickListener {
        void onChangeInfoClick();
    }

}
