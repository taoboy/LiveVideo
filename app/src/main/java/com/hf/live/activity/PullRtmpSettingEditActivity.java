package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hf.live.R;

/**
 * 拉流设置-编辑界面
 */

public class PullRtmpSettingEditActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private LinearLayout llBack;
    private TextView tvTitle,tvControl;
    private EditText etStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_rtmp_setting_edit);
        mContext = this;
        initWidget();
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("直播地址");
        tvControl = (TextView) findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setText("确定");
        etStream = (EditText) findViewById(R.id.etStream);
        etStream.addTextChangedListener(textWatcher);

        String stream = getIntent().getStringExtra("stream");
        if (!TextUtils.isEmpty(stream)) {
            etStream.setText(stream);
            etStream.setSelection(stream.length());
        }

    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s)) {
                tvControl.setVisibility(View.VISIBLE);
            }else {
                tvControl.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvControl:
                Intent intent = new Intent();
                intent.putExtra("stream", etStream.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
}
