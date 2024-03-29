package com.sensoro.smartcity.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sensoro.smartcity.R;
import com.sensoro.common.callback.RecycleViewItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlarmContactHistoryAdapter extends RecyclerView.Adapter<AlarmContactHistoryAdapter.AlarmContactHistoryHolder> {

    private final Context mContext;
    private final List<String> mList = new ArrayList<>();
    private RecycleViewItemClickListener itemClickListener;

    //    String[] tests = {"黎明","只要我还活着，就不会有人遭受苦难","大师，永远怀着一颗学徒的心","即使双眼失明，也丝毫不影响我追捕敌人，我能够闻到他们身上的臭味"
//    ,"提莫队长，正在送命"};
    public AlarmContactHistoryAdapter(Context context) {
        mContext = context;
    }

    public List<String> getSearchHistoryList() {
        return mList;
    }

    public void setRecycleViewItemClickListener(RecycleViewItemClickListener listener) {
        itemClickListener = listener;
    }

    @Override
    public AlarmContactHistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_adapter_name_address_view, parent, false);
        return new AlarmContactHistoryHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onBindViewHolder(AlarmContactHistoryHolder holder, final int position) {
        //一定要设置，因为是通用的，所以要设置这个
        holder.itemAdapterTv.setBackground(mContext.getResources().getDrawable(R.drawable.shape_bg_solid_e7_full_corner));
        String s = mList.get(position);
        if (!TextUtils.isEmpty(s)) {
            String[] split = s.split("#");
            holder.itemAdapterTv.setText(split[0]);
        }else{
            //基本不会为空
            holder.itemAdapterTv.setText(s);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    class AlarmContactHistoryHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_adapter_tv)
        TextView itemAdapterTv;

        AlarmContactHistoryHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void updateSearchHistoryAdapter(List<String> list) {
        this.mList.clear();
        this.mList.addAll(list);
        notifyDataSetChanged();
    }
}
