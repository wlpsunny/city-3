package com.sensoro.smartcity.widget.calendar.cardgallery;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.sensoro.common.utils.AppUtils;

/**
 * adapter中调用onCreateViewHolder, onBindViewHolder
 * Created by jameson on 9/1/16.
 * changed by 二精-霁雪清虹 on 2017/11/19
 */
public class BannerAdapterHelper {
    public static int sPagePadding = 15;
    public static int sShowLeftCardWidth = 15;

    public void onCreateViewHolder(ViewGroup parent, View itemView) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) itemView.getLayoutParams();
        lp.width = parent.getWidth() - AppUtils.dp2px(itemView.getContext(), 2 * (sPagePadding + sShowLeftCardWidth));
        itemView.setLayoutParams(lp);
    }

    public void onBindViewHolder(View itemView, final int position, int itemCount) {
        int padding = AppUtils.dp2px(itemView.getContext(), sPagePadding);
        itemView.setPadding(padding, 0, padding, 0);
        int leftMarin = position == 0 ? padding + AppUtils.dp2px(itemView.getContext(), sShowLeftCardWidth) : 0;
        int rightMarin = position == itemCount - 1 ? padding + AppUtils.dp2px(itemView.getContext(), sShowLeftCardWidth) : 0;
        setViewMargin(itemView, leftMarin, 0, rightMarin, 0);
    }

    private void setViewMargin(View view, int left, int top, int right, int bottom) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (lp.leftMargin != left || lp.topMargin != top || lp.rightMargin != right || lp.bottomMargin != bottom) {
            lp.setMargins(left, top, right, bottom);
            view.setLayoutParams(lp);
        }
    }

    public void setPagePadding(int pagePadding) {
        sPagePadding = pagePadding;
    }

    public void setShowLeftCardWidth(int showLeftCardWidth) {
        sShowLeftCardWidth = showLeftCardWidth;
    }
}
