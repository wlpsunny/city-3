package com.sensoro.smartcity.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorRes;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sensoro.smartcity.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InspectionTaskRcContentAdapter extends RecyclerView.Adapter<InspectionTaskRcContentAdapter.InspectionTaskRcContentHolder> {
    private final Context mContext;

    private InspectionTaskRcItemClickListener listener;

    public InspectionTaskRcContentAdapter(Context context) {
        mContext = context;
    }

    @Override
    public InspectionTaskRcContentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_adapter_inspection_task_content, parent, false);
        return new InspectionTaskRcContentHolder(view);
    }

    @Override
    public void onBindViewHolder(InspectionTaskRcContentHolder holder, final int position) {
        holder.itemAdapterInspectionTaskTvName.setText("费家村");
        holder.itemAdapterInspectionTaskTvSn.setText("温度贴片 01A00134");
        if (position == 0) {
            //未巡检调用的函数跟其他的不一样，我们不一样，每个人都有不同的境遇
            setState(holder);
        } else if (position == 1) {
            setState(holder, R.color.c_ff8d34, "巡检异常");
        } else if (position == 2) {
            setState(holder, R.color.c_29c093, "巡检正常");
            holder.itemAdapterInspectionTaskTvInspection.setVisibility(View.GONE);
            holder.itemAdapterInspectionTaskTvInspection.setVisibility(View.GONE);
        }

        holder.itemAdapterInspectionTaskTvInspection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    //第二个参数为state 目前先用position，根据需要改
                    listener.onInspectionTaskInspectionClick(position,position);
                }
            }
        });

        holder.itemAdapterInspectionTaskTvNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onInspectionTaskNavigationClick(position);
            }
        });

    }

    private void setState(InspectionTaskRcContentHolder holder, @ColorRes int colorId, String text) {
        int color = mContext.getResources().getColor(colorId);
        holder.itemAdapterInspectionTaskTvState.setText(text);
        holder.itemAdapterInspectionTaskTvState.setTextColor(color);
        GradientDrawable gd = (GradientDrawable) mContext.getResources().getDrawable(R.drawable.shape_small_oval_29c);
        gd.setColor(color);
        gd.setBounds(0, 0, gd.getMinimumWidth(), gd.getMinimumHeight());
        holder.itemAdapterInspectionTaskTvState.setCompoundDrawables(gd, null, null, null);

    }

    /**
     * 未巡检独有的函数
     */
    private void setState(InspectionTaskRcContentHolder holder) {
        Resources resources = mContext.getResources();
        holder.itemAdapterInspectionTaskTvState.setText("未巡检");
        holder.itemAdapterInspectionTaskTvState.setTextColor(resources.getColor(R.color.c_a6a6a6));
        GradientDrawable gd = (GradientDrawable) resources.getDrawable(R.drawable.shape_small_oval_29c);
        gd.setColor(resources.getColor(R.color.c_626262));
        gd.setBounds(0, 0, gd.getMinimumWidth(), gd.getMinimumHeight());
        holder.itemAdapterInspectionTaskTvState.setCompoundDrawables(gd, null, null, null);

    }

    public void setOnRecycleViewItemClickListener(InspectionTaskRcItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    class InspectionTaskRcContentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_adapter_inspection_task_tv_name)
        TextView itemAdapterInspectionTaskTvName;
        @BindView(R.id.item_adapter_inspection_task_tv_near)
        TextView itemAdapterInspectionTaskTvNear;
        @BindView(R.id.item_adapter_inspection_task_tv_state)
        TextView itemAdapterInspectionTaskTvState;
        @BindView(R.id.item_adapter_inspection_task_ll_top)
        LinearLayout itemAdapterInspectionTaskLlTop;
        @BindView(R.id.item_adapter_inspection_task_tv_sn)
        TextView itemAdapterInspectionTaskTvSn;
        @BindView(R.id.item_adapter_inspection_task_view)
        View itemAdapterInspectionTaskView;
        @BindView(R.id.item_adapter_inspection_task_tv_inspection)
        TextView itemAdapterInspectionTaskTvInspection;
        @BindView(R.id.item_adapter_inspection_task_tv_navigation)
        TextView itemAdapterInspectionTaskTvNavigation;

        InspectionTaskRcContentHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface InspectionTaskRcItemClickListener {
        void onInspectionTaskInspectionClick(int position,int state);

        void onInspectionTaskNavigationClick(int position);
    }
}