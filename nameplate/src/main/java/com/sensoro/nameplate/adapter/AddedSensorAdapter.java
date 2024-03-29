package com.sensoro.nameplate.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sensoro.common.server.bean.NamePlateInfo;
import com.sensoro.nameplate.R;
import com.sensoro.nameplate.R2;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddedSensorAdapter extends RecyclerView.Adapter<AddedSensorAdapter.AddedSensorAdapterViewHolder> {
    private final Context mContext;

    public List<NamePlateInfo> getmList() {
        return mList;
    }

    private List<NamePlateInfo> mList = new ArrayList<>();
    private onDeleteClickListenre mListener;

    public AddedSensorAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public AddedSensorAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_adapter_added_sensor, parent, false);
        AddedSensorAdapterViewHolder holder = new AddedSensorAdapterViewHolder(inflate);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AddedSensorAdapterViewHolder holder, int position) {
        NamePlateInfo plateInfo = mList.get(position);
        holder.tvDeleteItemAdapterAddedSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDeleteClick(position);
                }
            }
        });
        if (!TextUtils.isEmpty(plateInfo.getName())) {
            holder.tvNameItemAdapterAddedSensor.setText(plateInfo.getName());
        } else {
            holder.tvNameItemAdapterAddedSensor.setText(plateInfo.getSn());

        }
        if (!TextUtils.isEmpty(plateInfo.getSn())) {
            holder.tvDeviceSnItemAdapterAddedSensor.setText(plateInfo.getSn());
        } else {
            holder.tvDeviceSnItemAdapterAddedSensor.setText("");

        }
        if (!TextUtils.isEmpty(plateInfo.deviceTypeName)) {
            holder.tvDeviceNameItemAdapterAddedSensor.setText(plateInfo.deviceTypeName);
        } else {
            holder.tvDeviceNameItemAdapterAddedSensor.setText("");

        }
        holder.ivIconItemAdapterAddedSensor.setColorFilter(Color.parseColor("#a6a6a6"));


        Glide.with(mContext)
                .load(plateInfo.iconUrl)
                .thumbnail(0.1f)
                .apply(new RequestOptions().placeholder(R.drawable.ic_default_image).error(R.drawable.ic_default_image))
                .into(holder.ivIconItemAdapterAddedSensor);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void updateData(List<NamePlateInfo> data) {
        mList.clear();
        mList.addAll(data);
        notifyDataSetChanged();
    }

    public void setOnDeleteClickListener(onDeleteClickListenre listener) {
        mListener = listener;
    }

    class AddedSensorAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R2.id.tv_name_item_adapter_added_sensor)
        TextView tvNameItemAdapterAddedSensor;
        @BindView(R2.id.iv_icon_item_adapter_added_sensor)
        ImageView ivIconItemAdapterAddedSensor;
        @BindView(R2.id.tv_device_name_item_adapter_added_sensor)
        TextView tvDeviceNameItemAdapterAddedSensor;
        @BindView(R2.id.tv_device_sn_item_adapter_added_sensor)
        TextView tvDeviceSnItemAdapterAddedSensor;
        @BindView(R2.id.tv_delete_item_adapter_added_sensor)
        TextView tvDeleteItemAdapterAddedSensor;

        public AddedSensorAdapterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface onDeleteClickListenre {
        void onDeleteClick(int position);
    }
}
