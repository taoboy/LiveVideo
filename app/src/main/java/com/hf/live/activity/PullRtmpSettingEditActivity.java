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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

/**
 * 拉流设置-编辑界面
 */

public class PullRtmpSettingEditActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private LinearLayout llBack;
    private TextView tvTitle,tvControl,tvScane;
    private EditText etStream;
    private ImageView ivStreamClear;

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
        tvTitle.setText("观看地址");
        tvControl = (TextView) findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setText("确定");
        etStream = (EditText) findViewById(R.id.etStream);
        etStream.addTextChangedListener(streamWatcher);
        ivStreamClear = (ImageView) findViewById(R.id.ivStreamClear);
        ivStreamClear.setOnClickListener(this);
        tvScane = (TextView) findViewById(R.id.tvScane);
        tvScane.setOnClickListener(this);
        tvScane.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

        String stream = getIntent().getStringExtra(CONST.STREAM);
        if (!TextUtils.isEmpty(stream)) {
            etStream.setText(stream);
            etStream.setSelection(stream.length());
        }

    }

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

    /**
     * 保存拉流设置
     */
    private void writeSetting() {
        SharedPreferences sp = getSharedPreferences("PULLRTMPSETTING", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CONST.STREAM, etStream.getText().toString());
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
                finish();
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
