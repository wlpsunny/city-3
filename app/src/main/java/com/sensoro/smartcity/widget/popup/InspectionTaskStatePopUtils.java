package com.sensoro.smartcity.widget.popup;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.InspectionTaskStateSelectAdapter;
import com.sensoro.smartcity.model.InspectionStatusCountModel;
import com.sensoro.smartcity.widget.RecycleViewItemClickListener;

import java.util.List;

public class InspectionTaskStatePopUtils {
    private final Activity mActivity;
    private final InspectionTaskStateSelectAdapter mSelectStateAdapter;
    private final PopupWindow mPopupWindow;
    private InspectionTaskStatePopUtils.SelectDeviceTypeItemClickListener listener;

    public InspectionTaskStatePopUtils(Activity activity) {
        mActivity = activity;
        View view = LayoutInflater.from(activity).inflate(R.layout.item_pop_inspection_task_select_state, null);
        RecyclerView mRcStateSelect = view.findViewById(R.id.pop_inspection_task_rc_select_state);

        mSelectStateAdapter = new InspectionTaskStateSelectAdapter(activity);
        GridLayoutManager manager = new GridLayoutManager(activity, 3);
        mRcStateSelect.setLayoutManager(manager);
        mRcStateSelect.setAdapter(mSelectStateAdapter);
        mSelectStateAdapter.setOnItemClickListener(new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                listener.onSelectDeviceTypeItemClick(view,position);

            }
        });
        mPopupWindow = new PopupWindow(activity);
        mPopupWindow.setContentView(view);
        mPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(mActivity.getResources().getColor(R.color.c_aa000000)));
        mPopupWindow.setAnimationStyle(R.style.DialogFragmentDropDownAnim);
        mPopupWindow.setFocusable(true);

    }

    public void updateSelectDeviceStatusList(List<InspectionStatusCountModel> list){
        mSelectStateAdapter.updateDeviceTypList(list);
    }
    public void setSelectDeviceTypeItemClickListener(InspectionTaskStatePopUtils.SelectDeviceTypeItemClickListener listener){
        this.listener = listener;
    }

    public void dismiss() {
        mPopupWindow.dismiss();
    }

    public void showAtLocation(View view,int gravity) {
        mPopupWindow.showAtLocation(view, gravity,0,0);
    }

    /**
     * poup 展示在某个控件下
     */
    public void showAsDropDown(View view) {
        if (Build.VERSION.SDK_INT < 24) {
            mPopupWindow.showAsDropDown(view);
        } else {  // 适配 android 7.0
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            Point point = new Point();
            mActivity.getWindowManager().getDefaultDisplay().getSize(point);
            int tempHeight = mPopupWindow.getHeight();
            if (tempHeight == WindowManager.LayoutParams.MATCH_PARENT || point.y <= tempHeight) {
                mPopupWindow.setHeight(point.y - location[1] - view.getHeight());
            }
            mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0], location[1] + view.getHeight());
        }
        mPopupWindow.showAsDropDown(view);
    }
    public void setUpAnimation() {
        mPopupWindow.setAnimationStyle(R.style.DialogFragmentUpAnim);
    }

    public InspectionStatusCountModel getItem(int position) {
        return mSelectStateAdapter.getItem(position);
    }

    public void clearAnimation() {
        mPopupWindow.setAnimationStyle(-1);
    }

    public interface SelectDeviceTypeItemClickListener{
        void onSelectDeviceTypeItemClick(View view, int position);
    }
}