package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;

/**
 * 拉流设置
 */

public class PullRtmpSettingActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private LinearLayout llBack;
    private TextView tvTitle,tvStream,tvStart;
    private RelativeLayout reStream;
    private String stream = "rtmp://5107.liveplay.myqcloud.com/live/5107_82eca2351d2911e892905cb9018cf0d4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_rtmp_setting);
        mContext = this;
        initWidget();
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("观看设置");
        reStream = (RelativeLayout) findViewById(R.id.reStream);
        reStream.setOnClickListener(this);
        tvStream = (TextView) findViewById(R.id.tvStream);
        tvStart = (TextView) findViewById(R.id.tvStart);
        tvStart.setOnClickListener(this);

        setting();

    }

    private void setting() {
        if (!TextUtils.isEmpty(stream)) {
            tvStream.setText(stream);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.reStream:
                intent = new Intent(mContext, PullRtmpSettingEditActivity.class);
                intent.putExtra("stream", tvStream.getText().toString());
                startActivityForResult(intent, 1001);
                break;
            case R.id.tvStart:
                if (TextUtils.isEmpty(tvStream.getText().toString())) {
                    Toast.makeText(mContext, "请输入观看地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent(mContext, PullRtmpActivity.class);
                intent.putExtra("stream", tvStream.getText().toString());
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1001:
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            stream = bundle.getString("stream");
                            setting();
                        }
                    }
                    break;
            }
        }
    }

}
