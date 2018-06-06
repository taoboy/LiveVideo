package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.tencent.rtmp.TXLiveConstants;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

/**
 * 推流设置-编辑界面
 */

public class PushRtmpSettingEditActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private LinearLayout llBack;
    private TextView tvTitle,tvControl,tvScreen,tvCamera,tvScane;
    private EditText etName, etStream;
    private ImageView ivNameClear, ivStreamClear;
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
        etName.addTextChangedListener(nameWatcher);
        etStream = (EditText) findViewById(R.id.etStream);
        etStream.addTextChangedListener(streamWatcher);
        ivNameClear = (ImageView) findViewById(R.id.ivNameClear);
        ivNameClear.setOnClickListener(this);
        ivStreamClear = (ImageView) findViewById(R.id.ivStreamClear);
        ivStreamClear.setOnClickListener(this);
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
        tvScane = (TextView) findViewById(R.id.tvScane);
        tvScane.setOnClickListener(this);
        tvScane.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);


        String name = getIntent().getStringExtra(CONST.NAME);
        if (!TextUtils.isEmpty(name)) {
            etName.setText(name);
            etName.setSelection(name.length());
        }

        String stream = getIntent().getStringExtra(CONST.STREAM);
        if (!TextUtils.isEmpty(stream)) {
            etStream.setText(stream);
            etStream.setSelection(stream.length());
        }

        orientation = getIntent().getBooleanExtra(CONST.ORIENTATION, orientation);
        swScreen.setChecked(orientation);

        isFront = getIntent().getBooleanExtra(CONST.ISFRONT, isFront);
        swCamera.setChecked(isFront);

        videoQuality = getIntent().getIntExtra(CONST.VIDEOQUALITY, videoQuality);
        if (videoQuality == TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION) {
            rgResolution.check(R.id.rbResolution1);
        }else if (videoQuality == TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION) {
            rgResolution.check(R.id.rbResolution2);
        }else if (videoQuality == TXLiveConstants.VIDEO_QUALITY_SUPER_DEFINITION) {
            rgResolution.check(R.id.rbResolution3);
        }

    }

    private TextWatcher nameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s)) {
                ivNameClear.setVisibility(View.VISIBLE);
            }else {
                ivNameClear.setVisibility(View.GONE);
            }
        }
    };

    private TextWatcher streamWatcher = new TextWatcher() {
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
                ivStreamClear.setVisibility(View.VISIBLE);
            }else {
                tvControl.setVisibility(View.GONE);
                ivStreamClear.setVisibility(View.GONE);
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

    /**
     * 保存推流设置
     */
    private void writeSetting() {
        SharedPreferences sp = getSharedPreferences("PUSHRTMPSETTING", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CONST.NAME, etName.getText().toString());
        editor.putString(CONST.STREAM, etStream.getText().toString());
        editor.putBoolean(CONST.ORIENTATION, orientation);
        editor.putBoolean(CONST.ISFRONT, isFront);
        editor.putInt(CONST.VIDEOQUALITY, videoQuality);
        editor.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            writeSetting();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                writeSetting();
                finish();
                break;
            case R.id.ivNameClear:
                if (etName != null) {
                    etName.setText("");
                }
                break;
            case R.id.ivStreamClear:
                if (etStream != null) {
                    etStream.setText("");
                }
                break;
            case R.id.tvControl:
                writeSetting();
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.tvScane:
                Intent intent = new Intent(getApplication(), CaptureActivity.class);
                startActivityForResult(intent, 100000);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 100000:
                    //处理二维码扫描结果
                    if (null != data) {
                        Bundle bundle = data.getExtras();
                        if (bundle == null) {
                            return;
                        }
                        if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                            String result = bundle.getString(CodeUtils.RESULT_STRING);
                            if (!TextUtils.isEmpty(result)) {
                                etStream.setText(result);
                            }
                        } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                            Toast.makeText(this, "解析二维码失败", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }
        }
    }

}
