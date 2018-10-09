package com.sensoro.smartcity.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sensoro.smartcity.R;
import com.sensoro.smartcity.server.bean.InspectionTaskInstructionModel;
import com.sensoro.smartcity.server.bean.ScenesData;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InspectionInstructionContentAdapter extends RecyclerView.Adapter<InspectionInstructionContentAdapter.InspectionInstructionContentHolder> {
    private final Context mContext;
    private List<InspectionTaskInstructionModel.DataBean> dataList = new ArrayList<>();
    private OnInspectionInstructionContentPicClickListenter mListener;

    public InspectionInstructionContentAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public InspectionInstructionContentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_adapter_inspection_instruction_content, parent, false);
        return new InspectionInstructionContentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InspectionInstructionContentHolder holder, int position) {
        InspectionTaskInstructionModel.DataBean dataBean = dataList.get(position);
        holder.itemAdapterInspectionInstructionContentTvTitle.setText(dataBean.getTitle());
        holder.itemAdapterInspectionInstructionContentTvContent.setText(dataBean.getText());

        List<String> images = dataBean.getImages();
        ArrayList<ScenesData> pics = new ArrayList<>();
        for (String image : images) {
            ScenesData scenesData = new ScenesData();
            scenesData.type = "image";
            scenesData.url = image;
            pics.add(scenesData);
        }

        final InspectionExceptionThumbnailAdapter mPhotoAdapter = new InspectionExceptionThumbnailAdapter(mContext);
        final GridLayoutManager manager = new GridLayoutManager(mContext, 4){
            @Override
            public boolean canScrollVertically() {
                return false;
            }

        };
        holder.itemAdapterInspectionInstructionContentRcPic.setLayoutManager(manager);
        holder.itemAdapterInspectionInstructionContentRcPic.setHasFixedSize(true);
        holder.itemAdapterInspectionInstructionContentRcPic.setNestedScrollingEnabled(false);
        mPhotoAdapter.updateDataList(pics);
        holder.itemAdapterInspectionInstructionContentRcPic.setAdapter(mPhotoAdapter);


        mPhotoAdapter.setOnExceptionThumbnailItemClickListener(new InspectionExceptionThumbnailAdapter.ExceptionThumbnailItemClickListener() {
            @Override
            public void onExceptionThumbnailItemClickListener(int position) {
                if (mListener != null) {
                    mListener.onInspectionInstructionContentPicClick(mPhotoAdapter.getDataList(),position);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateDataList(List<InspectionTaskInstructionModel.DataBean> data) {
        dataList.clear();
        dataList.addAll(data);
        notifyDataSetChanged();
    }

    public void setOnInspectionInstructionContentPicClickListenter(OnInspectionInstructionContentPicClickListenter listenter){
        mListener = listenter;
    }

    class InspectionInstructionContentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_adapter_inspection_instruction_content_tv_title)
        TextView itemAdapterInspectionInstructionContentTvTitle;
        @BindView(R.id.item_adapter_inspection_instruction_content_tv_content)
        TextView itemAdapterInspectionInstructionContentTvContent;
        @BindView(R.id.item_adapter_inspection_instruction_content_rc_pic)
        RecyclerView itemAdapterInspectionInstructionContentRcPic;

        public InspectionInstructionContentHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

        }
    }

    public interface OnInspectionInstructionContentPicClickListenter{
        void onInspectionInstructionContentPicClick(List<ScenesData> dataList, int position);
    }
}