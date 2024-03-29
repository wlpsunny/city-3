package com.sensoro.common.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.sensoro.common.R;
import com.sensoro.common.callback.RecycleViewItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class NameAddressHistoryAdapter extends RecyclerView.Adapter<NameAddressHistoryAdapter.NameAddressHistoryHolder> {

    private final Context mContext;
    private final List<String> mList = new ArrayList<>();
    private RecycleViewItemClickListener itemClickListener;

    //    String[] tests = {"黎明","只要我还活着，就不会有人遭受苦难","大师，永远怀着一颗学徒的心","即使双眼失明，也丝毫不影响我追捕敌人，我能够闻到他们身上的臭味"
//    ,"提莫队长，正在送命"};
    public NameAddressHistoryAdapter(Context context) {
        mContext = context;
    }

    public List<String> getSearchHistoryList() {
        return mList;
    }

    public void setRecycleViewItemClickListener(RecycleViewItemClickListener listener) {
        itemClickListener = listener;
    }

    @Override
    public NameAddressHistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_adapter_name_address_view, parent, false);
        return new NameAddressHistoryHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onBindViewHolder(NameAddressHistoryHolder holder, final int position) {
        //一定要设置，因为是通用的，所以要设置这个
        Resources resources = mContext.getResources();
        Drawable drawable = resources.getDrawable(R.drawable.shape_bg_solid_e7_full_corner);
        holder.itemAdapterTv.setBackground(drawable);
        holder.itemAdapterTv.setText(mList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, position);
                }
            }
        });
    }

    class NameAddressHistoryHolder extends RecyclerView.ViewHolder {
        TextView itemAdapterTv;

        NameAddressHistoryHolder(View itemView) {
            super(itemView);
            itemAdapterTv = itemView.findViewById(R.id.item_adapter_tv);
        }
    }

    public void updateSearchHistoryAdapter(List<String> list) {
        this.mList.clear();
        this.mList.addAll(list);
        notifyDataSetChanged();
    }
}
