package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.hf.live.R;
import com.tencent.rtmp.TXLiveConstants;

/**
 * 推流设置-编辑界面
 */

public class PushRtmpSettingEditActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private LinearLayout llBack;
    private TextView tvTitle,tvControl,tvScreen,tvCamera;
    private EditText etName, etStream;
    private Switch swScreen,swCamera;
    private RadioGroup rgResolution;
    private RadioButton rbResolution1, rbResolution2, rbResolution3;
    private boolean orientation = true;//推流方向，默认横屏
    private boolean isFront = true;//摄像头，默认为前置
    private int videoQuality = TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION;//视频质量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_rtmp_setting_edit);
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
        etName = (EditText) findViewById(R.id.etName);
        etStream = (EditText) findViewById(R.id.etStream);
        etStream.addTextChangedListener(textWatcher);
        tvScreen = (TextView) findViewById(R.id.tvScreen);
        swScreen = (Switch) findViewById(R.id.swScreen);
        swScreen.setOnCheckedChangeListener(screenListener);
        tvCamera = (TextView) findViewById(R.id.tvCamera);
        swCamera = (Switch) findViewById(R.id.swCamera);
        swCamera.setOnCheckedChangeListener(cameraListener);
        rgResolution = (RadioGroup) findViewById(R.id.rgResolution);
        rgResolution.setOnCheckedChangeListener(resolutionListener);
        rbResolution1 = (RadioButton) findViewById(R.id.rbResolution1);
        rbResolution2 = (RadioButton) findViewById(R.id.rbResolution2);
        rbResolution3 = (RadioButton) findViewById(R.id.rbResolution3);


        String name = getIntent().getStringExtra("name");
        if (!TextUtils.isEmpty(name)) {
            etName.setText(name);
            etName.setSelection(name.length());
        }

        String stream = getIntent().getStringExtra("stream");
        if (!TextUtils.isEmpty(stream)) {
            etStream.setText(stream);
            etStream.setSelection(stream.length());
        }

        orientation = getIntent().getBooleanExtra("orientation", orientation);
        swScreen.setChecked(orientation);

        isFront = getIntent().getBooleanExtra("isFront", isFront);
        swCamera.setChecked(isFront);

        videoQuality = getIntent().getIntExtra("videoQuality", videoQuality);
        if (videoQuality == TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION) {
            rgResolution.check(R.id.rbResolution1);
        }else if (videoQuality == TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION) {
            rgResolution.check(R.id.rbResolution2);
        }else if (videoQuality == TXLiveConstants.VIDEO_QUALITY_SUPER_DEFINITION) {
            rgResolution.check(R.id.rbResolution3);
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

    private CompoundButton.OnCheckedChangeListener screenListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                tvScreen.setText("横屏");
            }else {
                tvScreen.setText("竖屏");
            }
            orientation = isChecked;
        }
    };

    private CompoundButton.OnCheckedChangeListener cameraListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                tvCamera.setText("前置");
            }else {
                tvCamera.setText("后置");
            }
            isFront = isChecked;
        }
    };

    private RadioGroup.OnCheckedChangeListener resolutionListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.rbResolution1:
                    rbResolution1.setBackgroundColor(getResources().getColor(R.color.cell_pressed));
                    rbResolution2.setBackgroundColor(getResources().getColor(R.color.cell_default));
                    rbResolution3.setBackgroundColor(getResources().getColor(R.color.cell_default));
                    videoQuality = TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION;
                    break;
                case R.id.rbResolution2:
                    rbResolution1.setBackgroundColor(getResources().getColor(R.color.cell_default));
                    rbResolution2.setBackgroundColor(getResources().getColor(R.color.cell_pressed));
                    rbResolution3.setBackgroundColor(getResources().getColor(R.color.cell_default));
                    videoQuality = TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION;
                    break;
                case R.id.rbResolution3:
                    rbResolution1.setBackgroundColor(getResources().getColor(R.color.cell_default));
                    rbResolution2.setBackgroundColor(getResources().getColor(R.color.cell_default));
                    rbResolution3.setBackgroundColor(getResources().getColor(R.color.cell_pressed));
                    videoQuality = TXLiveConstants.VIDEO_QUALITY_SUPER_DEFINITION;
                    break;
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
                intent.putExtra("name", etName.getText().toString());
                intent.putExtra("stream", etStream.getText().toString());
                intent.putExtra("orientation", orientation);
                intent.putExtra("isFront", isFront);
                intent.putExtra("videoQuality", videoQuality);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
}
