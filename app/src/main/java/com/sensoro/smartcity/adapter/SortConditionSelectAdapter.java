/**
 * 首页排序条件列表适配器
 */
package com.sensoro.smartcity.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sensoro.common.callback.RecycleViewItemClickListener;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.model.SortConditionModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SortConditionSelectAdapter extends RecyclerView.Adapter<SortConditionSelectAdapter.SortConditionSelectHolder> {
    private final Context mContext;
    private RecycleViewItemClickListener mListener;
    private List<SortConditionModel> mSortConditionList = new ArrayList<>();

    public SortConditionModel getmSelectSortCondition() {
        return mSelectSortCondition;
    }

    public void setmSelectSortCondition(SortConditionModel mSelectSortCondition) {
        this.mSelectSortCondition = mSelectSortCondition;
    }

    private SortConditionModel mSelectSortCondition;

    public SortConditionSelectAdapter(Context context) {
        mContext = context;
    }


    public void updateSortConditionList(List<SortConditionModel> mSortConditionList) {

        this.mSortConditionList.clear();
        this.mSortConditionList.addAll(mSortConditionList);
        notifyDataSetChanged();
    }

    @Override
    public SortConditionSelectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_pop_adapter_sortcondition_select, parent, false);
        SortConditionSelectHolder holder = new SortConditionSelectHolder(view);
        view.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(final SortConditionSelectHolder mHolder, final int position) {

        final SortConditionModel mSortCondition = mSortConditionList.get(position);
        if (mSortCondition.isSelected) {
            mHolder.itemPopSelectImvSortconditionIcon.setVisibility(View.VISIBLE);
            mHolder.itemPopSelectTvSortconditionName.setTextColor(mContext.getResources().getColor(R.color.c_1dbb99));
        } else {
            mHolder.itemPopSelectImvSortconditionIcon.setVisibility(View.INVISIBLE);
            mHolder.itemPopSelectTvSortconditionName.setTextColor(mContext.getResources().getColor(R.color.c_252525));
        }

        mHolder.itemPopSelectTvSortconditionName.setText(mSortCondition.title);
        mHolder.itemPopSelectLlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectSortCondition != null) {
                    mSelectSortCondition.isSelected = false;
                }
                mSelectSortCondition = getItem(position);
                mSelectSortCondition.isSelected = true;
                notifyDataSetChanged();
                if (mListener != null) {
                    mListener.onItemClick(v, position);
                }

            }
        });


    }

    public void setOnItemClickListener(RecycleViewItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onBindViewHolder(SortConditionSelectHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

    }

    public SortConditionModel getItem(int position) {
        return mSortConditionList.get(position);
    }

    @Override
    public int getItemCount() {
        return mSortConditionList.size();
    }

    public List<SortConditionModel> getDataList() {
        return mSortConditionList;
    }

    class SortConditionSelectHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_pop_select_imv_sortcondition_icon)
        ImageView itemPopSelectImvSortconditionIcon;
        @BindView(R.id.item_pop_select_tv_sortcondition_name)
        TextView itemPopSelectTvSortconditionName;
        @BindView(R.id.item_pop_sortcondition_ll_root)
        LinearLayout itemPopSelectLlRoot;

        SortConditionSelectHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
