package com.sensoro.smartcity.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sensoro.common.callback.RecycleViewItemClickListener;
import com.sensoro.common.model.CameraFilterModel;
import com.sensoro.basestation.R;
import com.sensoro.basestation.R2;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BaseStationPopAdapter extends RecyclerView.Adapter<BaseStationPopAdapter.InspectionTaskStateSelectHolder> {
    private final Context mContext;

    private RecycleViewItemClickListener mListener;
    private List<CameraFilterModel> mStateCountList = new ArrayList<>();

    public BaseStationPopAdapter(Context context) {
        mContext = context;
    }

    public List<CameraFilterModel> getmStateCountList() {

        return mStateCountList;
    }


    @Override
    public InspectionTaskStateSelectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_pop_adapter_basestation_list_filter, parent, false);
        InspectionTaskStateSelectHolder holder = new InspectionTaskStateSelectHolder(view);
        view.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(InspectionTaskStateSelectHolder holder, int position) {


        BaseStationListFilterAdapter cameraListFilterAdapter = new BaseStationListFilterAdapter(mContext);

        GridLayoutManager manager = new GridLayoutManager(mContext, 3);
        holder.itemPopRvCamerListFilter.setLayoutManager(manager);
        holder.itemPopRvCamerListFilter.setAdapter(cameraListFilterAdapter);


        if (null != mStateCountList.get(position)) {
            CameraFilterModel model = mStateCountList.get(position);
            holder.itemPopTvCamerListFilterTitle.setText(model.getTitle().trim());
            holder.itemPopRvCamerListFilter.setTag(cameraListFilterAdapter);
            cameraListFilterAdapter.updateDeviceTypList(model.getList(), model.isMulti());
        }
    }

    public CameraFilterModel getItem(int position) {
        return mStateCountList.get(position);
    }

    public void setOnItemClickListener(RecycleViewItemClickListener listener) {
        mListener = listener;
    }

    public void updateDeviceTypList(List<CameraFilterModel> list) {
        mStateCountList.clear();
        mStateCountList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(InspectionTaskStateSelectHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        onBindViewHolder(holder, position);
    }


    @Override
    public int getItemCount() {
        return mStateCountList.size();
    }


    class InspectionTaskStateSelectHolder extends RecyclerView.ViewHolder {
        @BindView(R2.id.item_pop_tv_camer_list_filter_title)
        TextView itemPopTvCamerListFilterTitle;
        @BindView(R2.id.item_pop_rv_basestation_list_filter)
        RecyclerView itemPopRvCamerListFilter;

        InspectionTaskStateSelectHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
