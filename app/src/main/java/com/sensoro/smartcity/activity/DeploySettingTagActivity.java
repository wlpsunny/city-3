package com.sensoro.smartcity.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.sensoro.smartcity.R;
import com.sensoro.smartcity.SensoroCityApplication;
import com.sensoro.smartcity.adapter.RelationAdapter;
import com.sensoro.smartcity.adapter.SearchHistoryAdapter;
import com.sensoro.smartcity.constant.Constants;
import com.sensoro.smartcity.server.bean.DeviceInfo;
import com.sensoro.smartcity.widget.ClearableTextView;
import com.sensoro.smartcity.widget.RecycleViewItemClickListener;
import com.sensoro.smartcity.widget.SensoroLinearLayoutManager;
import com.sensoro.smartcity.widget.SpacesItemDecoration;
import com.sensoro.smartcity.widget.statusbar.StatusBarCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class DeploySettingTagActivity extends BaseActivity implements Constants, TextView.OnEditorActionListener,
        TextWatcher, RecycleViewItemClickListener {


    @BindView(R.id.deploy_setting_tag_back)
    ImageView backImageView;
    @BindView(R.id.deploy_setting_tag_finish)
    TextView finishImageView;
    @BindView(R.id.deploy_setting_tag_et)
    EditText mKeywordEt;
    @BindView(R.id.deploy_setting_tag_history_rv)
    RecyclerView mSearchHistoryRv;
    @BindView(R.id.deploy_setting_tag_relation_rv)
    RecyclerView mSearchRelationRv;
    @BindView(R.id.deploy_setting_tag_relation_layout)
    LinearLayout mSearchRelationLayout;
    @BindView(R.id.deploy_setting_tag_history_layout)
    LinearLayout mSearchHistoryLayout;
    @BindView(R.id.deploy_setting_tag_input_layout)
    LinearLayout inputLayout;

    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private List<String> mHistoryKeywords = new ArrayList<>();
    private List<String> mTagList = new ArrayList<>();
    private SearchHistoryAdapter mSearchHistoryAdapter;
    private RelationAdapter mRelationAdapter;
    private int add_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deploy_setting_tag);
        ButterKnife.bind(this);
        init();
        StatusBarCompat.setStatusBarColor(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

    private void init() {
        try {
            mPref = getApplicationContext().getSharedPreferences(PREFERENCE_DEPLOY_TAG_HISTORY, Activity.MODE_PRIVATE);
            mEditor = mPref.edit();
            initRelation();
            initSearchHistory();
            initInputLayout();
            mKeywordEt.setOnEditorActionListener(this);
            mKeywordEt.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (TextUtils.isEmpty(mKeywordEt.getText())) {
                        if (keyCode == KeyEvent.KEYCODE_DEL
                                && event.getAction() == KeyEvent.ACTION_DOWN) {
                            if (mTagList.size() > 0) {
                                mTagList.remove(mTagList.size() - 1);
                                initInputLayout();

                            }
                            return true;
                        }
                    }

                    return false;
                }
            });
            mKeywordEt.addTextChangedListener(this);
            mKeywordEt.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initInputLayout() {
        inputLayout.removeAllViews();
        mKeywordEt.getText().clear();
        inputLayout.addView(mKeywordEt);
        int textSize = getResources().getDimensionPixelSize(R.dimen.tag_default_size);
        mTagList = getIntent().getStringArrayListExtra(EXTRA_SETTING_TAG_LIST);
        for (int i = 0; i < mTagList.size(); i++) {
            ClearableTextView textView = new ClearableTextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.setMargins(10, 0, 0, 0);
            textView.setTextColor(getResources().getColor(R.color.white));
            textView.setText(mTagList.get(i));
            textView.setPadding(5, 0, 0, 0);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            textView.setGravity(Gravity.CENTER);
            textView.setCompoundDrawables(null, null, null, null);
            textView.setBackground(getResources().getDrawable(R.drawable.shape_textview));
            textView.setOnTextClearListener(new ClearableTextView.OnTextClearListener() {
                @Override
                public void onTextClear(ClearableTextView v) {
                    mTagList.remove(v.getText().toString());
                    inputLayout.removeView(v);
                }
            });
//            inputLayout.remo
            int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            textView.measure(spec, spec);
            inputLayout.addView(textView, i, params);
        }
        mKeywordEt.requestFocus();
    }

    private void initSearchHistory() {
        String history = mPref.getString(PREFERENCE_KEY_DEPLOY, "");
        if (!TextUtils.isEmpty(history)) {
            mHistoryKeywords.clear();
            mHistoryKeywords.addAll(Arrays.asList(history.split(",")));
        }
        if (mHistoryKeywords.size() > 0) {
            mSearchHistoryLayout.setVisibility(View.VISIBLE);
        } else {
            mSearchHistoryLayout.setVisibility(View.GONE);
        }
        SensoroLinearLayoutManager layoutManager = new SensoroLinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mSearchHistoryRv.setLayoutManager(layoutManager);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.x10);
        mSearchHistoryRv.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        mSearchHistoryAdapter = new SearchHistoryAdapter(this, mHistoryKeywords, new RecycleViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mKeywordEt.setText(mHistoryKeywords.get(position).trim());
                mKeywordEt.clearFocus();
                mKeywordEt.setSelection(mHistoryKeywords.get(position).trim().length());
                dismissInputMethodManager(view);
                doChoose(false);

            }
        });
        mSearchHistoryRv.setAdapter(mSearchHistoryAdapter);
        mSearchHistoryAdapter.notifyDataSetChanged();
        mKeywordEt.requestFocus();
        //弹出框value unit对齐，搜索框有内容点击历史搜索出现没有搜索内容
    }

    private void initRelation() {
        mRelationAdapter = new RelationAdapter(this, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mSearchRelationRv.setLayoutManager(linearLayoutManager);
        mSearchRelationRv.setAdapter(mRelationAdapter);
    }

    @Override
    protected boolean isNeedSlide() {
        return true;
    }

    private boolean containsTag(List<String> tags, String filter) {
        for (String tag : tags) {
            if (tag.contains(filter)) {
                return true;
            }
        }
        return false;
    }

    public void filterDeviceInfoByTag(String filter) {
        List<DeviceInfo> originDeviceInfoList = new ArrayList<>();
        originDeviceInfoList.addAll(SensoroCityApplication.getInstance().getData());
        ArrayList<DeviceInfo> deleteDeviceInfoList = new ArrayList<>();
        for (DeviceInfo deviceInfo : originDeviceInfoList) {
            if (deviceInfo.getTags() != null) {
                List<String> tagList = Arrays.asList(deviceInfo.getTags());
                if (!containsTag(tagList, filter)) {
                    deleteDeviceInfoList.add(deviceInfo);
                }
            } else {
                deleteDeviceInfoList.add(deviceInfo);
            }

        }
        for (DeviceInfo deviceInfo : deleteDeviceInfoList) {
            originDeviceInfoList.remove(deviceInfo);
        }
        List<String> tempList = new ArrayList<>();
        for (DeviceInfo deviceInfo : originDeviceInfoList) {
            String name = deviceInfo.getName();
            if (!TextUtils.isEmpty(name)) {
                tempList.add(name);
            }
        }
        mRelationAdapter.setData(tempList);
        mRelationAdapter.notifyDataSetChanged();
    }

    public void dismissInputMethodManager(View view) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);//从控件所在的窗口中隐藏
    }

    public void save() {
        String text = mKeywordEt.getText().toString();
        String oldText = mPref.getString(PREFERENCE_KEY_DEPLOY, "");
        if (add_count > 1) {
            mKeywordEt.getText().clear();
            mKeywordEt.clearFocus();
            Toast.makeText(DeploySettingTagActivity.this, "标签不能重复", Toast.LENGTH_SHORT).show();
        }
        if (!TextUtils.isEmpty(text)) {
            if (mHistoryKeywords.contains(text)) {
                List<String> list = new ArrayList<String>();
                for (String o : oldText.split(",")) {
                    if (!o.equalsIgnoreCase(text)) {
                        list.add(o);
                    }
                }
                list.add(0, text);
                mHistoryKeywords = list;
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < list.size(); i++) {
                    if (i == (list.size() - 1)) {
                        stringBuffer.append(list.get(i));
                    } else {
                        stringBuffer.append(list.get(i) + ",");
                    }
                }
                mEditor.putString(PREFERENCE_KEY_DEPLOY, stringBuffer.toString());
                mEditor.commit();
                add_count++;
            } else {
                if (TextUtils.isEmpty(oldText)) {
                    mEditor.putString(PREFERENCE_KEY_DEPLOY, text);
                    mEditor.commit();
                } else {
                    mEditor.putString(PREFERENCE_KEY_DEPLOY, text + "," + oldText);
                    mEditor.commit();
                }
                mHistoryKeywords.add(0, text);
                add_count++;
            }
        }
    }

    public void doChoose(Boolean isFinish) {
        if (TextUtils.isEmpty(mKeywordEt.getText().toString().trim())) {
            if (!isFinish) {
                return;
            }
        } else {
            if (!mTagList.contains(mKeywordEt.getText().toString())) {
                mTagList.add(mKeywordEt.getText().toString());
                initInputLayout();
            } else {
                mKeywordEt.getText().clear();
                mKeywordEt.clearFocus();
                Toast.makeText(this, "标签不能重复", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        save();
        if (isFinish) {
            Intent data = new Intent();
            data.putStringArrayListExtra(EXTRA_SETTING_TAG_LIST, (ArrayList<String>) mTagList);
            setResult(RESULT_CODE_SETTING_TAG, data);
            finish();
        }
    }

    @OnClick(R.id.deploy_setting_tag_finish)
    public void doFinish() {
        doChoose(true);
    }


    @OnClick(R.id.deploy_setting_tag_back)
    public void back() {
        this.finish();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            mKeywordEt.clearFocus();
            dismissInputMethodManager(v);
            doChoose(false);
            return true;
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!TextUtils.isEmpty(s)) {
            mSearchHistoryLayout.setVisibility(View.GONE);
            mSearchRelationLayout.setVisibility(View.VISIBLE);
            filterDeviceInfoByTag(s.toString());

        } else {
            mSearchHistoryLayout.setVisibility(View.VISIBLE);
            mSearchRelationLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onItemClick(View view, int position) {
        String text = mRelationAdapter.getData().get(position);
//        mKeywordEt.setText(text);
//        addTagtoInputLayout(text);
        initInputLayout();
        mRelationAdapter.getData().clear();
        mRelationAdapter.notifyDataSetChanged();
    }
}
