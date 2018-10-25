package com.sensoro.smartcity.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.server.bean.DeployRecordInfo;
import com.sensoro.smartcity.util.DateUtil;
import com.sensoro.smartcity.widget.RecycleViewItemClickListener;
import com.sensoro.smartcity.widget.SensoroLinearLayoutManager;
import com.sensoro.smartcity.widget.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeployRecordContentAdapter extends RecyclerView.Adapter<DeployRecordContentAdapter.DeployRecordContentHolder> {

    private final Activity mActivity;
    private List<DeployRecordInfo> recordInfoList = new ArrayList();
    private RecycleViewItemClickListener listener;


    public DeployRecordContentAdapter(Activity activity) {
        mActivity = activity;
    }

    @NonNull
    @Override
    public DeployRecordContentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.item_adapter_deploy_record_content, parent, false);
        return new DeployRecordContentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeployRecordContentHolder holder, final int position) {
        DeployRecordInfo deployRecordInfo = recordInfoList.get(position);
        holder.itemAdapterDeployRecordTvName.setText(deployRecordInfo.getDeviceName());
        holder.itemAdapterDeployRecordTvSn.setText(deployRecordInfo.getSn());
        holder.itemAdapterDeployRecordTvTime.setText(DateUtil.getStrTime_ymd_hm(deployRecordInfo.getCreatedTime()));

        TagAdapter tagAdapter = new TagAdapter(mActivity, R.color.c_252525, R.color.c_dfdfdf);
        SensoroLinearLayoutManager layoutManager = new SensoroLinearLayoutManager(mActivity);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        holder.itemAdapterDeployRecordRcTag.setLayoutManager(layoutManager);
        int spacingInPixels = mActivity.getResources().getDimensionPixelSize(R.dimen.x10);
        holder.itemAdapterDeployRecordRcTag.addItemDecoration(new SpacesItemDecoration(false, spacingInPixels));
        holder.itemAdapterDeployRecordRcTag.setAdapter(tagAdapter);
        tagAdapter.updateTags(deployRecordInfo.getTags());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(v,position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordInfoList.size();
    }

    public void setData(List<DeployRecordInfo> data) {
        recordInfoList.clear();
        recordInfoList.addAll(data);
    }

    public void setOnClickListener(RecycleViewItemClickListener listener){
        this.listener = listener;
    }

    public DeployRecordInfo getItem(int position) {
        return recordInfoList.get(position);
    }

    class DeployRecordContentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_adapter_deploy_record_tv_sn)
        TextView itemAdapterDeployRecordTvSn;
        @BindView(R.id.item_adapter_deploy_record_tv_name)
        TextView itemAdapterDeployRecordTvName;
        @BindView(R.id.item_adapter_deploy_record_rc_tag)
        RecyclerView itemAdapterDeployRecordRcTag;
        @BindView(R.id.item_adapter_deploy_record_tv_time)
        TextView itemAdapterDeployRecordTvTime;
        public DeployRecordContentHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }


}