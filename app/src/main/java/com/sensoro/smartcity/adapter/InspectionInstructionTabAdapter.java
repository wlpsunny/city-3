package com.sensoro.smartcity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.widget.RecycleViewItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InspectionInstructionTabAdapter extends RecyclerView.Adapter<InspectionInstructionTabAdapter.InspectionInstructionTabHolder> {
    private final Context mContext;
    private int selectPosition;
    private List<String> tabs = new ArrayList<>();
    private RecycleViewItemClickListener listener;


    public InspectionInstructionTabAdapter(Context context) {
        mContext = context;
    }

    @Override
    public InspectionInstructionTabHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_adapter_inspection_instruction_tab, parent, false);
        return new InspectionInstructionTabHolder(view);
    }

    @Override
    public void onBindViewHolder(InspectionInstructionTabHolder holder, final int position) {
        if(position == selectPosition){
            holder.itemAdapterInspectionInstructionTv.setBackgroundResource(R.drawable.shape_bg_solid_29c_full_corner);
            holder.itemAdapterInspectionInstructionTv.setTextColor(Color.WHITE);
            holder.itemAdapterInspectionInstructionTv.setText(tabs.get(position));
        }else{
            holder.itemAdapterInspectionInstructionTv.setBackgroundResource(R.drawable.shape_bg_solid_transparent_full_corner);
            holder.itemAdapterInspectionInstructionTv.setTextColor(mContext.getResources().getColor(R.color.c_a6a6a6));
            holder.itemAdapterInspectionInstructionTv.setText(tabs.get(position));
        }

        holder.itemAdapterInspectionInstructionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPosition = position;
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onItemClick(v,position);
                }
            }
        });
    }

    public void setRecycleViewItemClickListener(RecycleViewItemClickListener listener){
        this.listener = listener;
    }

    public  void settabs(List<String> list){
        tabs.clear();
        tabs.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }

    class InspectionInstructionTabHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_adapter_inspection_instruction_tv)
        TextView itemAdapterInspectionInstructionTv;

        InspectionInstructionTabHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}