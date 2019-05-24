package com.sensoro.smartcity.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.sensoro.common.base.BaseActivity;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.adapter.FrequencyPointAdapter;
import com.sensoro.smartcity.imainviews.IFrequencyPointActivityView;
import com.sensoro.smartcity.presenter.FrequencyPointActivityPresenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 频点信息
 */
public class FrequencyPointActivity extends BaseActivity<IFrequencyPointActivityView, FrequencyPointActivityPresenter> {
    @BindView(R.id.include_text_title_imv_arrows_left)
    ImageView includeTextTitleImvArrowsLeft;
    @BindView(R.id.include_text_title_tv_title)
    TextView includeTextTitleTvTitle;
    @BindView(R.id.include_text_title_tv_subtitle)
    TextView includeTextTitleTvSubtitle;
    @BindView(R.id.include_text_title_divider)
    View includeTextTitleDivider;
    @BindView(R.id.include_text_title_cl_root)
    ConstraintLayout includeTextTitleClRoot;
    @BindView(R.id.tv_channel_name)
    TextView tvChannelName;
    @BindView(R.id.ac_frequency_point_rc)
    RecyclerView acFrequencyPointRc;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;

    FrequencyPointAdapter frequencyPointAdapter;


    @Override
    protected void onCreateInit(Bundle savedInstanceState) {
        setContentView(R.layout.activity_frequency_point);
        ButterKnife.bind(this);
        includeTextTitleTvTitle.setText(R.string.frequency_poin_info);
        includeTextTitleTvSubtitle.setVisibility(View.GONE);

        GridLayoutManager manager = new GridLayoutManager(this, 4);
        acFrequencyPointRc.setLayoutManager(manager);
        frequencyPointAdapter = new FrequencyPointAdapter(this);


        acFrequencyPointRc.setAdapter(frequencyPointAdapter);
    }

    @Override
    protected FrequencyPointActivityPresenter createPresenter() {
        return new FrequencyPointActivityPresenter();
    }


    @OnClick(R.id.include_text_title_imv_arrows_left)
    public void onViewClicked() {

        finish();
    }

}