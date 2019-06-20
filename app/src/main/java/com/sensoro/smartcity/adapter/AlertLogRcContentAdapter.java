package com.sensoro.smartcity.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sensoro.common.constant.Constants;
import com.sensoro.common.helper.PreferencesHelper;
import com.sensoro.common.model.SecurityRisksAdapterModel;
import com.sensoro.common.server.bean.AlarmInfo;
import com.sensoro.common.server.bean.ScenesData;
import com.sensoro.common.server.bean.SensorTypeStyles;
import com.sensoro.common.utils.DateUtil;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.analyzer.AlarmPopupConfigAnalyzer;
import com.sensoro.smartcity.util.WidgetUtil;
import com.sensoro.smartcity.widget.HtmlImageSpan;
import com.sensoro.smartcity.widget.dialog.WarnPhoneMsgDialogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sensoro.smartcity.constant.CityConstants.confirmAlarmResultInfoArray;
import static com.sensoro.smartcity.constant.CityConstants.confirmStatusArray;
import static com.sensoro.smartcity.constant.CityConstants.confirmStatusTextColorArray;

public class AlertLogRcContentAdapter extends RecyclerView.Adapter<AlertLogRcContentAdapter.AlertLogRcContentHolder> implements Constants {
    private final Context mContext;
    private final List<AlarmInfo.RecordInfo> timeShaftParentBeans = new ArrayList<>();


    private HashMap<Integer, List[]> hashMap = new HashMap<>();

    public AlertLogRcContentAdapter(Context context) {
        mContext = context;

    }

    private OnPhotoClickListener onPhotoClickListener;

    public void setData(List<AlarmInfo.RecordInfo> recordInfoList) {
        this.timeShaftParentBeans.clear();
        this.timeShaftParentBeans.addAll(recordInfoList);
    }

    public interface OnPhotoClickListener {
        void onPhotoItemClick(int position, List<ScenesData> scenesDataList);
    }

    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
        this.onPhotoClickListener = onPhotoClickListener;
    }

    @Override
    public AlertLogRcContentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.item_adapter_alert_log_content, parent, false);

        return new AlertLogRcContentHolder(inflate);
    }

    @Override
    public void onBindViewHolder(AlertLogRcContentHolder holder, int position) {

        //

        AlarmInfo.RecordInfo recordInfo = timeShaftParentBeans.get(position);
        String time = DateUtil.getStrTimeToday(mContext, recordInfo.getUpdatedTime(), 0);
        holder.itemAlertContentTvTime.setText(time);
        holder.itemAlertContentTvContent.setOnClickListener(null);
        //
        if ("confirm".equals(recordInfo.getType())) {
            //TODO 设置图标
            holder.itemAlertContentImvIcon.setImageResource(R.drawable.contact_icon);
            String source = recordInfo.getSource();
            String confirm_text = null;
            Integer displayStatus = recordInfo.getDisplayStatus();
            String reasonStr = "";
            try {
                if (displayStatus != null) {
                    reasonStr = mContext.getString(confirmStatusArray[displayStatus]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ("auto".equals(source)) {
                int day = 2;
                try {
                    long timeout = recordInfo.getTimeout();
                    day = (int) (timeout / 3600);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                confirm_text = day + mContext.getString(R.string.no_one_confirmed_the_system_automatically_confirms);
                SpannableString spannableString = new SpannableString(confirm_text);
                String temp = day + mContext.getString(R.string.hour);
                changTextColor(confirm_text, temp, spannableString, R.color.c_252525);
                temp = mContext.getString(R.string.test_patrol);
                changTextColor(confirm_text, temp, spannableString, R.color.c_8058a5);
                holder.itemAlertContentTvContent.setText(spannableString);
            } else if ("app".equals(source)) {
                //TODO 状态兼容
                confirm_text = mContext.getString(R.string.contact) + " [" + recordInfo.getName() + "] " + mContext.getString(R.string.confirm_that_the_alert_type_app_is) + ":\n" +
                        reasonStr;
                //用span改变字体颜色,换行 用\n
//            String content = "联系人[高鹏]通过 平台 确认本次预警类型为：\n安全隐患";
                SpannableString spannableString = new SpannableString(confirm_text);
                // 改变高鹏 颜色
                String temp = "[" + recordInfo.getName() + "]";
                changTextColor(confirm_text, temp, spannableString, R.color.c_131313);
                //改变安全隐患颜色
                //TODO 状态兼容
                temp = reasonStr;
                try {
                    if (displayStatus != null) {
                        changTextColor(confirm_text, temp, spannableString, confirmStatusTextColorArray[displayStatus]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                holder.itemAlertContentTvContent.setText(spannableString);
            } else if ("platform".equals(source)) {
                //TODO 状态兼容
                confirm_text = mContext.getString(R.string.contact) + " [" + recordInfo.getName() + "]" + mContext.getString(R.string.confirm_that_the_alert_type_web_is) + ":\n" +
                        reasonStr;
                SpannableString spannableString = new SpannableString(confirm_text);
                // 改变高鹏 颜色
                String temp = "[" + recordInfo.getName() + "]";
                changTextColor(confirm_text, temp, spannableString, R.color.c_252525);
                //改变安全隐患颜色
                temp = reasonStr;
                //TODO 状态兼容
                try {
                    if (displayStatus != null) {
                        changTextColor(confirm_text, temp, spannableString, confirmStatusTextColorArray[displayStatus]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                holder.itemAlertContentTvContent.setText(spannableString);
            }

            //
            holder.llConfirm.setVisibility(View.VISIBLE);
            //预警结果
            //TODO 状态问题
            StringBuilder stringBuilder = new StringBuilder();
            String desc = "";
            try {
                if (displayStatus != null) {
                    desc = mContext.getString(confirmAlarmResultInfoArray[displayStatus]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.itemAlarmDetailChildAlarmResult.setText(stringBuilder.append(reasonStr).append("(").append(desc).append(")").toString());
            //预警成因
            Integer reason = recordInfo.getReason();
            if (reason != null) {
                holder.llItemAlarmDetailChildAlarmType.setVisibility(View.VISIBLE);
                //TODO model配置
                holder.itemAlarmDetailChildAlarmType.setText(AlarmPopupConfigAnalyzer.gerAlarmPopModelName("reason", reason, mContext));
            } else {
                holder.llItemAlarmDetailChildAlarmType.setVisibility(View.GONE);
            }
            //预警场所
            Integer place = recordInfo.getPlace();
            if (place != null) {
                holder.llItemAlarmDetailChildAlarmPlace.setVisibility(View.VISIBLE);
                holder.itemAlarmDetailChildAlarmPlace.setText(AlarmPopupConfigAnalyzer.gerAlarmPopModelName("place", place, mContext));
            } else {
                holder.llItemAlarmDetailChildAlarmPlace.setVisibility(View.GONE);
            }
            Integer fireStage = recordInfo.getFireStage();
            if (fireStage != null) {
                holder.llItemAlarmDetailChildAlarmFirePhase.setVisibility(View.VISIBLE);
                holder.itemAlarmDetailChildAlarmFirePhase.setText(AlarmPopupConfigAnalyzer.gerAlarmPopModelName("fireStage", fireStage, mContext));
            } else {
                holder.llItemAlarmDetailChildAlarmFirePhase.setVisibility(View.GONE);
            }
            Integer fireType = recordInfo.getFireType();
            if (fireType != null) {
                holder.llItemAlarmDetailChildAlarmFireType.setVisibility(View.VISIBLE);
                holder.itemAlarmDetailChildAlarmFireType.setText(AlarmPopupConfigAnalyzer.gerAlarmPopModelName("fireType", fireType, mContext));
            } else {
                holder.llItemAlarmDetailChildAlarmFireType.setVisibility(View.GONE);
            }
            List<SecurityRisksAdapterModel> danger = recordInfo.getDanger();
            if (danger != null && danger.size() > 0) {
                holder.llItemAlarmDetailChildAlarmRisk.setVisibility(View.VISIBLE);
                String securityRisksText = AlarmPopupConfigAnalyzer.getSecurityRisksText(danger);
                if (TextUtils.isEmpty(securityRisksText)) {
                    holder.itemAlarmDetailChildAlarmRisk.setText(mContext.getString(R.string.unknown));
                } else {
                    holder.itemAlarmDetailChildAlarmRisk.setText(securityRisksText);
                }
            } else {
                holder.llItemAlarmDetailChildAlarmRisk.setVisibility(View.GONE);
            }
            //备注说明
            String remark = recordInfo.getRemark();
            if (!TextUtils.isEmpty(remark)) {
                holder.itemAlarmDetailChildAlarmRemarks.setText(remark);
            } else {
                holder.itemAlarmDetailChildAlarmRemarks.setText("");
            }
            final List<ScenesData> scenes = recordInfo.getScenes();
            if (scenes != null && scenes.size() > 0) {
                //TODO 防止数据错误清除
                holder.rvAlarmPhoto.setVisibility(View.VISIBLE);
                if (holder.rvAlarmPhoto.getTag() instanceof AlarmDetailPhotoAdapter) {
                    holder.rvAlarmPhoto.removeAllViews();
                }
                //
                final GridLayoutManager layoutManager = new GridLayoutManager(mContext, 4) {
                    @Override
                    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                    }
                };
                holder.rvAlarmPhoto.setLayoutManager(layoutManager);
                holder.rvAlarmPhoto.setHasFixedSize(true);
                AlarmDetailPhotoAdapter adapter = new AlarmDetailPhotoAdapter(mContext);
                adapter.setOnItemClickListener(new AlarmDetailPhotoAdapter.OnRecyclerViewItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (onPhotoClickListener != null) {
                            onPhotoClickListener.onPhotoItemClick(position, scenes);
                        }
                    }
                });
                holder.rvAlarmPhoto.setAdapter(adapter);
                //设置包裹不允许滑动，套一层父布局解决最后一项可能不显示的问题
                holder.rvAlarmPhoto.setNestedScrollingEnabled(false);
                adapter.setImages(scenes);
                //TODO 防止数据错误打标签
                holder.rvAlarmPhoto.setTag(adapter);
            } else {
                holder.rvAlarmPhoto.setVisibility(View.GONE);
            }


        } else if ("recovery".equals(recordInfo.getType())) {
            //TODO 设置图标
            holder.itemAlertContentImvIcon.setImageResource(R.drawable.no_smoke_icon);
            //
            String sensorType = recordInfo.getSensorType();
            try {
                StringBuilder stringBuilder = new StringBuilder();
                SensorTypeStyles sensorTypeStyles = PreferencesHelper.getInstance().getConfigSensorType(sensorType);
                if (sensorTypeStyles != null) {
//                    String trueMean = sensorTypeStyles.getTrueMean();
                    boolean bool = sensorTypeStyles.isBool();
                    if (bool) {
                        int thresholds = recordInfo.getThresholds();
                        switch (thresholds) {
                            case 1:
                                //true
                                String trueMean = sensorTypeStyles.getTrueMean();
                                stringBuilder.append(trueMean).append("，").append(mContext.getString(R.string.back_to_normal));
                                break;
                            case 0:
                                //false
                                String falseMean = sensorTypeStyles.getFalseMean();
                                stringBuilder.append(falseMean).append("，").append(mContext.getString(R.string.back_to_normal));
                                break;
                            default:
                                String falseMean1 = sensorTypeStyles.getFalseMean();
                                stringBuilder.append(falseMean1).append("，").append(mContext.getString(R.string.back_to_normal));
                                break;
                        }
                    } else {
//                    "电量低于预警值, 恢复正常";
////                    info = "温度 值为 " + thresholds + "°C 达到预警值";
                        String name = sensorTypeStyles.getName();
                        stringBuilder.append(name);
                        stringBuilder.append(mContext.getString(R.string.below_the_warning_value)).append("，").append(mContext.getString(R.string.back_to_normal));
                    }
                    holder.itemAlertContentTvContent.setText(stringBuilder.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                holder.itemAlertContentTvContent.setText(WidgetUtil.getAlarmDetailInfo(sensorType, recordInfo
                        .getThresholds(), 0));
            }
            holder.llConfirm.setVisibility(View.GONE);
        } else if ("sendVoice".equals(recordInfo.getType())) {
            //TODO 设置图标
            holder.itemAlertContentImvIcon.setImageResource(R.drawable.phone_icon);
            StringBuilder stringBuffer = new StringBuilder();

            switch (recordInfo.getStatus()) {
                case "alarm":
                    stringBuffer.append(mContext.getString(R.string.alarm_phone_sent_tip_new));

                    break;
                case "recovery":
                    stringBuffer.append(mContext.getString(R.string.alarm_phone_reciver_sent_tip_new));

                    break;
                case "timeout":
                    stringBuffer.append(mContext.getString(R.string.alarm_phone_timeout_sent_tip_new));

                    break;
                case "real":
                    stringBuffer.append(mContext.getString(R.string.alarm_phone_real_sent_tip_new));
                    break;
                default:
                    stringBuffer.append(mContext.getString(R.string.the_system_calls_to)).append(":");

                    break;
            }


            holder.itemAlertContentTvContent.setText(appendResult(stringBuffer, position, recordInfo.getPhoneList()));
            holder.llConfirm.setVisibility(View.GONE);

            holder.itemAlertContentTvContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    List[] receiveStautus = {receiveStautus0, receiveStautus1, receiveStautus2, receiveStautus3};

                    WarnPhoneMsgDialogUtil phoneMsgDialogUtil = new WarnPhoneMsgDialogUtil((Activity) mContext);
                    phoneMsgDialogUtil.setTitleTv(mContext.getResources().getString(R.string.alarm_contact_tip_phone));
                    phoneMsgDialogUtil.show(0, hashMap.get(position));
                }
            });
        } else if ("sendSMS".equals(recordInfo.getType())) {
            //TODO 设置图标
            holder.itemAlertContentImvIcon.setImageResource(R.drawable.msg_icon);
            final StringBuilder stringBuffer = new StringBuilder();
            switch (recordInfo.getStatus()) {
                case "alarm":
                    stringBuffer.append(mContext.getString(R.string.alarm_sms_sent_tip_new));

                    break;
                case "recovery":
                    stringBuffer.append(mContext.getString(R.string.alarm_sms_reciver_sent_tip_new));

                    break;
                case "timeout":
                    stringBuffer.append(mContext.getString(R.string.alarm_sms_timeout_sent_tip_new));

                    break;
                case "real":
                    stringBuffer.append(mContext.getString(R.string.alarm_sms_real_sent_tip_new));
                    break;
                default:
                    stringBuffer.append(mContext.getString(R.string.the_system_sends_msg_to)).append(":");

                    break;
            }


//            holder.itemAlertContentTvContent.setText();


            holder.itemAlertContentTvContent.setText(appendResult(stringBuffer, position, recordInfo.getPhoneList()));


            holder.llConfirm.setVisibility(View.GONE);

            holder.itemAlertContentTvContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ;
//                    List[] receiveStautus = {receiveStautus0, receiveStautus1, receiveStautus2, receiveStautus3};

                    WarnPhoneMsgDialogUtil phoneMsgDialogUtil = new WarnPhoneMsgDialogUtil((Activity) mContext);
                    phoneMsgDialogUtil.setTitleTv(mContext.getResources().getString(R.string.alarm_contact_tip_msg));
                    phoneMsgDialogUtil.show(1, hashMap.get(position));
                }
            });
        } else if ("alarm".equals(recordInfo.getType())) {
            //TODO 设置图标
            holder.itemAlertContentImvIcon.setImageResource(R.drawable.smoke_icon);
            //
            String sensorType = recordInfo.getSensorType();
            StringBuilder stringBuilder = new StringBuilder();
            try {
                SensorTypeStyles sensorTypeStyles = PreferencesHelper.getInstance().getConfigSensorType(sensorType);
                if (sensorTypeStyles != null) {
                    boolean bool = sensorTypeStyles.isBool();
                    if (bool) {
//                    info = "烟雾浓度高，设备预警";
                        int thresholds = recordInfo.getThresholds();
                        switch (thresholds) {
                            case 1:
                                //true
                                String trueMean = sensorTypeStyles.getTrueMean();
                                stringBuilder.append(trueMean).append("，").append(mContext.getString(R.string.equipment_warning));
                                break;
                            case 0:
                                //false
                                String falseMean = sensorTypeStyles.getFalseMean();
                                stringBuilder.append(falseMean).append("，").append(mContext.getString(R.string.equipment_warning));
                                break;
                            default:
                                String trueMean1 = sensorTypeStyles.getTrueMean();
                                stringBuilder.append(trueMean1).append("，").append(mContext.getString(R.string.equipment_warning));
                                break;
                        }

                    } else {
                        String name = sensorTypeStyles.getName();
                        stringBuilder.append(name);
////                    info = "温度 值为 " + thresholds + "°C 达到预警值";
                        int thresholds = recordInfo.getThresholds();
                        String unit = sensorTypeStyles.getUnit();
                        stringBuilder.append(" ").append(mContext.getString(R.string.value_is)).append(" ").append(thresholds).append(unit).append(" ").append(mContext.getString(R.string.achieve_warning_value));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                stringBuilder.append(WidgetUtil.getAlarmDetailInfo(recordInfo.getSensorType(), recordInfo.getThresholds(), 1));
            }
            //

            String alarmDetailInfo = stringBuilder.toString();
            SpannableString spannableString = new SpannableString(alarmDetailInfo);
            holder.itemAlertContentTvContent.setText(changTextColor(alarmDetailInfo, alarmDetailInfo, spannableString, R.color.c_252525));
            holder.llConfirm.setVisibility(View.GONE);
        }

    }

    /**
     * @param stringBuffer
     * @param pos
     * @param phoneList
     * @return
     */
    private SpannableString appendResult(StringBuilder stringBuffer, int pos, AlarmInfo.RecordInfo.Event[] phoneList) {
        //情况集合
//        receiveStatusListClear();
        List<AlarmInfo.RecordInfo.Event> receiveStautus0 = new ArrayList<>();
        List<AlarmInfo.RecordInfo.Event> receiveStautus1 = new ArrayList<>();
        List<AlarmInfo.RecordInfo.Event> receiveStautus2 = new ArrayList<>();
        List<AlarmInfo.RecordInfo.Event> receiveStautus3 = new ArrayList<>();
        List[] lists = hashMap.get(pos);
        if (null == lists || lists.length == 0) {
            lists = new List[]{receiveStautus0, receiveStautus1, receiveStautus2, receiveStautus3};
            hashMap.put(pos, lists);
        } else {
            List[] lists1 = hashMap.get(pos);
            receiveStautus0 = lists1[0];
            receiveStautus1 = lists1[1];
            receiveStautus2 = lists1[2];
            receiveStautus3 = lists1[3];

            //多条短信或者电话类型数据清除
//            receiveStautus0.clear();
//            receiveStautus1.clear();
//            receiveStautus2.clear();
//            receiveStautus3.clear();
        }


        for (int i = 0; i < phoneList.length; i++) {
            AlarmInfo.RecordInfo.Event event = phoneList[i];
            switch (event.getReciveStatus()) {
                case 0:
                    receiveStautus0.add(event);
                    break;
                case 1:
                    receiveStautus1.add(event);
                    break;
                case 2:
                    receiveStautus2.add(event);
                    break;
                default:
                    receiveStautus3.add(event);
                    break;
            }

        }
        List[] receiveStautus = {receiveStautus0, receiveStautus1, receiveStautus2, receiveStautus3};
        StringBuilder temp = null;
        for (List stautus : receiveStautus) {
            if (stautus.size() > 0) {
                temp = new StringBuilder();
                for (int i = 0; i < stautus.size(); i++) {
                    //最多显示两个
                    if (i < 2) {
                        AlarmInfo.RecordInfo.Event event = (AlarmInfo.RecordInfo.Event) stautus.get(i);
                        String number = event.getNumber();
                        String name = event.getName();
                        temp.append(" ").append(name).append("(").append(number).append(")");
                        //一个的时候不显示；
                        if (stautus.size() > 1 && i != 1) {
                            temp.append(" ;");
                        }

                    }
                }

                stringBuffer.append(temp).append(" ");

//                tempList.add(temp);
            }

        }
        if (phoneList.length > 2) {
            stringBuffer.append(mContext.getString(R.string.etc) + phoneList.length + mContext.getString(R.string.contacts));
        }

        String lookDetail = mContext.getResources().getString(R.string.look_detail);
        stringBuffer.append(" ");
        stringBuffer.append(lookDetail);
        stringBuffer.append("   ");

        SpannableString spannableString = new SpannableString(stringBuffer);


        int indexOfLookDetail = stringBuffer.indexOf(lookDetail);
        ForegroundColorSpan lookdetailfcs = new ForegroundColorSpan(mContext.getResources().getColor(R.color.c_1dbb99));
        spannableString.setSpan(lookdetailfcs, indexOfLookDetail, indexOfLookDetail + lookDetail.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);


        //获取一张图片
        Drawable drawable = mContext.getDrawable(R.mipmap.see_detail_rightarrow);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());

        //居中对齐imageSpan
        HtmlImageSpan imageSpan = new HtmlImageSpan(drawable);
        spannableString.setSpan(imageSpan, spannableString.length() - 1, spannableString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableString;
    }

//    private void receiveStatusListClear() {
//        receiveStautus0.clear();
//        receiveStautus1.clear();
//        receiveStautus2.clear();
//        receiveStautus3.clear();
//    }

    private SpannableString changTextColor(final String content, final String temp, SpannableString spannableString, @ColorRes int color) {
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(mContext.getResources().getColor(color));
        int i = 0;
        try {
            i = content.indexOf(temp);
            if (i == -1) {
                i = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        spannableString.setSpan(foregroundColorSpan, i, i + temp.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableString;
    }

    @Override
    public int getItemCount() {
        return timeShaftParentBeans.size();
    }

    class AlertLogRcContentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_alert_content_imv_icon)
        ImageView itemAlertContentImvIcon;
        @BindView(R.id.item_alert_content_tv_content)
        TextView itemAlertContentTvContent;
        @BindView(R.id.item_alert_content_tv_time)
        TextView itemAlertContentTvTime;
        @BindView(R.id.item_alarm_detail_child_alarm_result)
        TextView itemAlarmDetailChildAlarmResult;
        @BindView(R.id.ll_item_alarm_detail_child_alarm_type)
        LinearLayout llItemAlarmDetailChildAlarmType;
        @BindView(R.id.item_alarm_detail_child_alarm_type)
        TextView itemAlarmDetailChildAlarmType;
        @BindView(R.id.ll_item_alarm_detail_child_alarm_fire_phase)
        LinearLayout llItemAlarmDetailChildAlarmFirePhase;
        @BindView(R.id.item_alarm_detail_child_alarm_fire_phase)
        TextView itemAlarmDetailChildAlarmFirePhase;
        @BindView(R.id.ll_item_alarm_detail_child_alarm_place)
        LinearLayout llItemAlarmDetailChildAlarmPlace;
        @BindView(R.id.item_alarm_detail_child_alarm_place)
        TextView itemAlarmDetailChildAlarmPlace;
        @BindView(R.id.ll_item_alarm_detail_child_alarm_fire_type)
        LinearLayout llItemAlarmDetailChildAlarmFireType;
        @BindView(R.id.item_alarm_detail_child_alarm_fire_type)
        TextView itemAlarmDetailChildAlarmFireType;
        @BindView(R.id.ll_item_alarm_detail_child_alarm_risk)
        LinearLayout llItemAlarmDetailChildAlarmRisk;
        @BindView(R.id.item_alarm_detail_child_alarm_risk)
        TextView itemAlarmDetailChildAlarmRisk;
        @BindView(R.id.item_alarm_detail_child_alarm_remarks)
        TextView itemAlarmDetailChildAlarmRemarks;
        @BindView(R.id.rv_alarm_photo)
        RecyclerView rvAlarmPhoto;
        @BindView(R.id.ll_confirm)
        LinearLayout llConfirm;

        AlertLogRcContentHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
