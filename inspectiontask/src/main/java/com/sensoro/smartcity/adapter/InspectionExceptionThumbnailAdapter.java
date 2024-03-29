package com.sensoro.smartcity.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.server.bean.ScenesData;
import com.sensoro.inspectiontask.R;
import com.sensoro.inspectiontask.R2;
import com.sensoro.smartcity.constant.InspectionConstant;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InspectionExceptionThumbnailAdapter extends RecyclerView.Adapter<InspectionExceptionThumbnailAdapter.InspectionExceptionThumbnailHolder> {
    private final Context mContext;
    private List<ScenesData> datas = new ArrayList<>();
    private ExceptionThumbnailItemClickListener listener;
    private boolean hasVideo = false;

    public InspectionExceptionThumbnailAdapter(Context context) {
        mContext = context;
    }


    @Override
    public InspectionExceptionThumbnailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_adapter_inspection_exception_thumbnail, parent, false);
        return new InspectionExceptionThumbnailHolder(view);
    }

    public void setPhotoType(boolean hasVideo) {
        this.hasVideo = hasVideo;
    }

    @Override
    public void onBindViewHolder(InspectionExceptionThumbnailHolder holder, final int position) {
        String url;
        if (Constants.RES_IMAGE.equals(datas.get(position).type)) {
            url = datas.get(position).url;
        } else {
            url = datas.get(position).thumbUrl;
        }
        if (hasVideo) {
            holder.ivRecordPlay.setVisibility(View.VISIBLE);
        }else{
            holder.ivRecordPlay.setVisibility(View.GONE);
        }

        Glide.with((Activity) mContext)
                .load(url)
                .apply(new RequestOptions().error(R.drawable.ic_default_image)
                        .placeholder(R.drawable.ic_default_image).diskCacheStrategy(DiskCacheStrategy.ALL))
                .thumbnail(0.01f)
                .into(holder.itemAdapterInspectionExceptionImvThumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onExceptionThumbnailItemClickListener(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public void updateDataList(List<ScenesData> picUrls) {
        datas.clear();
        datas.addAll(picUrls);
        notifyDataSetChanged();
    }

    public void setOnExceptionThumbnailItemClickListener(ExceptionThumbnailItemClickListener listener) {
        this.listener = listener;
    }

    public List<ScenesData> getDataList() {
        return datas;
    }

    public ScenesData getItem(int position) {
        return datas.get(position);
    }

    class InspectionExceptionThumbnailHolder extends RecyclerView.ViewHolder {
        @BindView(R2.id.item_adapter_inspection_exception_imv_thumbnail)
        ImageView itemAdapterInspectionExceptionImvThumbnail;
        @BindView(R2.id.iv_record_play)
        ImageView ivRecordPlay;

        InspectionExceptionThumbnailHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface ExceptionThumbnailItemClickListener {
        void onExceptionThumbnailItemClickListener(int position);
    }

}
