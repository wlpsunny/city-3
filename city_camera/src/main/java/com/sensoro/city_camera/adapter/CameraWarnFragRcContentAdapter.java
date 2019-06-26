package com.sensoro.city_camera.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sensoro.city_camera.R;
import com.sensoro.city_camera.R2;
import com.sensoro.common.constant.Constants;
import com.sensoro.common.server.security.bean.SecurityAlarmInfo;
import com.sensoro.common.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraWarnFragRcContentAdapter extends RecyclerView.Adapter<CameraWarnFragRcContentAdapter.MyViewHolder> implements Constants {

    private final Context mContext;
    private CameraWarnConfirmStatusClickListener mListener;
    private final List<SecurityAlarmInfo> mList = new ArrayList<>();

    public CameraWarnFragRcContentAdapter(Context context) {
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_adapter_camerawarn_rc_content, parent, false);
        return new MyViewHolder(inflate);
    }

    public void setAlarmConfirmStatusClickListener(CameraWarnConfirmStatusClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
//        if(position==1){
//            changeStrokeColor(holder.mainWarnRcContentTvTag,R.color.c_ff8d34);
//            holder.mainWarnRcContentTvTag.setText("误报");
//        }
        boolean isReConfirm = false;
        SecurityAlarmInfo securityAlarmInfo = mList.get(position);
        if (securityAlarmInfo != null) {
            boolean isWarnValid = securityAlarmInfo.isValid;
            int warnType = securityAlarmInfo.warnType;
            String capturePhotoUrl = securityAlarmInfo.capturePhotoUrl;
            String focusOriPhoto = securityAlarmInfo.focusOriPhoto;
            String focusMatchrate = securityAlarmInfo.focusMatchrate;
            String warnName = securityAlarmInfo.warnName;
            String warnAddress = securityAlarmInfo.warnAddress;
            long warnTime = securityAlarmInfo.warnTime;
            holder.tvWarnName.setText(warnName);
            holder.tvWarnAddress.setText(warnAddress);
            holder.tvWarnTime.setText(DateUtil.getStrTimeToday(mContext, warnTime, 0));
            isReConfirm = false;
            if(isWarnValid){
                holder.tvCamerawarnValid.setBackground(mContext.getDrawable(R.drawable.shape_camera_warn_valid));
                holder.tvCamerawarnValid.setText(R.string.word_valid);
                isReConfirm = true;
            }else{
                holder.tvCamerawarnValid.setBackground(mContext.getDrawable(R.drawable.shape_camera_warn_unvalid));
                holder.tvCamerawarnValid.setText(R.string.word_unvalid);
                isReConfirm = false;
            }
            switch (warnType){
                //1:外来 2:重点 3：入侵
                case 1:
                    holder.tvWarnType.setText(R.string.external_type);
                    holder.tvWarnType.setBackgroundResource(R.drawable.security_type_foreign_bg);
                    holder.layoutSinglePhoto.setVisibility(View.VISIBLE);
                    holder.layoutMultPhoto.setVisibility(View.GONE);
                     //加载抓拍图片
                    Glide.with(mContext).load(capturePhotoUrl).into(holder.ivSiglePhoto);
                    break;
                case 2:
                    holder.tvWarnType.setText(R.string.focus_type);
                    holder.tvWarnType.setBackgroundResource(R.drawable.security_type_focus_bg);
                    holder.layoutSinglePhoto.setVisibility(View.GONE);
                    holder.layoutMultPhoto.setVisibility(View.VISIBLE);
                    //加载布控 抓拍 照片
                    Glide.with(mContext).load(capturePhotoUrl).into(holder.ivLeftPhoto);
                    Glide.with(mContext).load(focusOriPhoto).into(holder.ivRightPhoto);
                    holder.tvRightMatchrate.setText(focusMatchrate);
                    break;
                case 3:
                    holder.tvWarnType.setText(R.string.invade_type);
                    holder.tvWarnType.setBackgroundResource(R.drawable.security_type_invade_bg);
                    holder.layoutSinglePhoto.setVisibility(View.VISIBLE);
                    holder.layoutMultPhoto.setVisibility(View.GONE);
                    //加载抓拍照片
                    Glide.with(mContext).load(capturePhotoUrl).into(holder.ivSiglePhoto);
                    break;

            }
        }

        final Boolean finalIsReConfirm = isReConfirm;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(v, position, finalIsReConfirm);
                }
            }
        });
        holder.btnWarnConfim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onConfirmStatusClick(v, position,finalIsReConfirm);
                }
            }
        });
    }

    public void setData(List<SecurityAlarmInfo> list) {
        this.mList.clear();
        this.mList.addAll(list);
    }

    public List<SecurityAlarmInfo> getData() {
        return mList;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        //====单个照片====
        @BindView(R2.id.layout_single_photo_content)
        RelativeLayout layoutSinglePhoto;
        @BindView(R2.id.iv_single_photo)
        ImageView ivSiglePhoto;
        //======多个照片====
        @BindView(R2.id.layout_mult_photo_content)
        RelativeLayout layoutMultPhoto;

        @BindView(R2.id.layout_left_photo_content)
        RelativeLayout layoutMultLeftContent;
        @BindView(R2.id.iv_left_photo)
        ImageView ivLeftPhoto;

        @BindView(R2.id.layout_right_photo_content)
        RelativeLayout layoutMultRightContent;
        @BindView(R2.id.iv_right_photo)
        ImageView ivRightPhoto;
        @BindView(R2.id.tv_right_matchrate)
        TextView tvRightMatchrate;

        @BindView(R2.id.tv_camera_warn_type)
        TextView tvWarnType;
        @BindView(R2.id.tv_camera_warn_name)
        TextView tvWarnName;
        @BindView(R2.id.tv_camera_warn_adress)
        TextView tvWarnAddress;
        @BindView(R2.id.tv_camera_warn_time)
        TextView tvWarnTime;
        @BindView(R2.id.btn_camerawarn_confim)
        TextView btnWarnConfim;

        @BindView(R2.id.tv_camerawarn_valid)
        TextView tvCamerawarnValid;


        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface CameraWarnConfirmStatusClickListener {
        void onConfirmStatusClick(View view, int position, boolean isReConfirm);

        void onItemClick(View view, int position, boolean isReConfirm);
    }

}