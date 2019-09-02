package com.sensoro.smartcity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sensoro.common.model.CameraFilterModel;
import com.sensoro.common.server.bean.BaseStationInfo;
import com.sensoro.basestation.R;
import com.sensoro.common.constant.Constants;
import com.sensoro.basestation.R2;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseStationListAdapter extends RecyclerView.Adapter<BaseStationListAdapter.DeviceCameraContentHolder> implements Constants {


    private Context mContext;
    private OnDeviceCameraContentClickListener listener;
    private final List<BaseStationInfo> mData = new ArrayList<>();
    private List<CameraFilterModel.ListBean> types;

    public BaseStationListAdapter(Context context) {
        mContext = context;
    }

    public void setOnAlarmHistoryLogConfirmListener(OnDeviceCameraContentClickListener onDeviceCameraContentClickListener) {
        listener = onDeviceCameraContentClickListener;
    }

    public void updateAdapter(List<BaseStationInfo> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void setaseStationType(List<CameraFilterModel.ListBean> data) {

        types = data;

    }

    public List<BaseStationInfo> getData() {
        return mData;
    }

    public interface OnDeviceCameraContentClickListener {
        void onItemClick(View v, int position);
    }

    @Override
    public DeviceCameraContentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_base_station, parent, false);
        return new DeviceCameraContentHolder(inflate);
    }

    @Override
    public void onBindViewHolder(final DeviceCameraContentHolder holder, final int position) {

        BaseStationInfo deviceCameraInfo = mData.get(position);
        if (deviceCameraInfo != null) {
            //
            String name = deviceCameraInfo.getName();
            String type = deviceCameraInfo.getTypeName();
            if (TextUtils.isEmpty(name)) {
                name = deviceCameraInfo.getSn();
            }
            if (!TextUtils.isEmpty(type)) {
                holder.itemDeviceCameraTvDeviceName.setText(type + " ");

            }
            holder.itemDeviceCameraTvDeviceName.append(name);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v, position);
                }
            }
        });


//        if (null != types && types.size() > 0) {
//            for (CameraFilterModel.ListBean type : types) {
//                if (deviceCameraInfo.getType().equals(type.getCode())) {
//
//                    holder.item_basestation_tv_type.setVisibility(View.VISIBLE);
//                    holder.item_basestation_tv_type.setText(type.getTitle());
//                    break;
//                } else {
//                    holder.item_basestation_tv_type.setVisibility(View.GONE);
//
//                }
//            }
//        }


        if (!TextUtils.isEmpty(deviceCameraInfo.getSn())) {
            holder.itemDeviceCameraTvId.setText(deviceCameraInfo.getSn());
        } else {
            holder.itemDeviceCameraTvId.setText("");

        }


        if (null != deviceCameraInfo.getTags() && deviceCameraInfo.getTags().size() > 0) {

            holder.itemDeviceCameraLlDetail.setVisibility(View.VISIBLE);
            holder.itemDeviceCameraLlDetail.removeAllViews();
            for (String tag : deviceCameraInfo.getTags()) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.item_basestation_tag_item, null);
                TextView textView = view.findViewById(R.id.item_basestation_tag_tv);
                textView.setText(tag);
                holder.itemDeviceCameraLlDetail.addView(view);
            }
        } else {
            holder.itemDeviceCameraLlDetail.setVisibility(View.GONE);
        }


        if ("offline".equals(deviceCameraInfo.getStatus())) {


            Drawable drawable = mContext.getResources().getDrawable(R.drawable.item_basestation_offline);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());

            holder.itemDeviceCameraTvOnlinestate.setText(mContext.getResources().getString(R.string.offline));
            holder.itemDeviceCameraTvOnlinestate.setTextColor(Color.parseColor("#5D5D5D"));
            holder.itemDeviceCameraTvOnlinestate.setCompoundDrawables(drawable, null, null, null);


        } else if ("inactive".equals(deviceCameraInfo.getStatus())) {
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.item_inactive);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            holder.itemDeviceCameraTvOnlinestate.setCompoundDrawables(drawable, null, null, null);
            holder.itemDeviceCameraTvOnlinestate.setText(mContext.getResources().getString(R.string.inactive));
            holder.itemDeviceCameraTvOnlinestate.setTextColor(Color.parseColor("#A6A6A6"));
        } else {
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.item_device_online);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            holder.itemDeviceCameraTvOnlinestate.setCompoundDrawables(drawable, null, null, null);
            holder.itemDeviceCameraTvOnlinestate.setText(mContext.getResources().getString(R.string.normal));
            holder.itemDeviceCameraTvOnlinestate.setTextColor(Color.parseColor("#1DBB99"));

        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class DeviceCameraContentHolder extends RecyclerView.ViewHolder {
        @BindView(R2.id.item_device_camera_tv_device_name)
        TextView itemDeviceCameraTvDeviceName;
        @BindView(R2.id.item_device_camera_tv_onlinestate)
        TextView itemDeviceCameraTvOnlinestate;
        @BindView(R2.id.item_device_camera_tv_id)
        TextView itemDeviceCameraTvId;
        @BindView(R2.id.item_basestation_tv_type)
        TextView item_basestation_tv_type;
        @BindView(R2.id.item_device_camera_ll_detail)
        LinearLayout itemDeviceCameraLlDetail;

        DeviceCameraContentHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}