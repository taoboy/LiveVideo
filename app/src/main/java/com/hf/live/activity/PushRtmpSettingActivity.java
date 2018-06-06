package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.tencent.rtmp.TXLiveConstants;

/**
 * 推流设置
 */

public class PushRtmpSettingActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;
    private LinearLayout llBack;
    private TextView tvTitle,tvName,tvStream,tvScreen,tvCamera,tvResolution,tvStart;
    private RelativeLayout reStream,reScreen,reResolution;
    private String name = "自定义", stream = "rtmp://5107.livepush.myqcloud.com/live/5107_82eca2351d2911e892905cb9018cf0d4?bizid=5107";
    private boolean orientation = true;//推流方向，默认横屏
    private boolean isFront = true;//摄像头，默认为前置
    private int videoQuality = TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION;//视频质量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_rtmp_setting);
        mContext = this;
        initWidget();
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("直播设置");
        reStream = (RelativeLayout) findViewById(R.id.reStream);
        reStream.setOnClickListener(this);
        reScreen = (RelativeLayout) findViewById(R.id.reScreen);
        reScreen.setOnClickListener(this);
        reResolution = (RelativeLayout) findViewById(R.id.reResolution);
        reResolution.setOnClickListener(this);
        tvName = (TextView) findViewById(R.id.tvName);
        tvStream = (TextView) findViewById(R.id.tvStream);
        tvScreen = (TextView) findViewById(R.id.tvScreen);
        tvCamera = (TextView) findViewById(R.id.tvCamera);
        tvResolution = (TextView) findViewById(R.id.tvResolution);
        tvStart = (TextView) findViewById(R.id.tvStart);
        tvStart.setOnClickListener(this);

        readSetting();
    }

    /**
     * 读取推流设置
     */
    private void readSetting() {
        SharedPreferences sp = getSharedPreferences("PUSHRTMPSETTING", Context.MODE_PRIVATE);
        name = sp.getString(CONST.NAME, name);
        stream = sp.getString(CONST.STREAM, stream);
        orientation = sp.getBoolean(CONST.ORIENTATION, orientation);
        isFront = sp.getBoolean(CONST.ISFRONT, isFront);
        videoQuality = sp.getInt(CONST.VIDEOQUALITY, videoQuality);

        if (!TextUtils.isEmpty(name)) {
            tvName.setText(name);
        }

        if (!TextUtils.isEmpty(stream)) {
            tvStream.setText(stream);
        }

        if (orientation) {
            tvScreen.setText("横屏");
        }else {
            tvScreen.setText("竖屏");
        }

        if (isFront) {
            tvCamera.setText("前置镜头");
        }else {
            tvCamera.setText("后置镜头");
        }

        if (videoQuality == TXLiveConstants.VIDEO_QUALITY_STANDARD_DEFINITION) {
            tvResolution.setText("标清（360P，25fps，300~800kbps）");
        }else if (videoQuality == TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION) {
            tvResolution.setText("高清（540P，25fps，600~1500kbps）");
        }else if (videoQuality == TXLiveConstants.VIDEO_QUALITY_SUPER_DEFINITION) {
            tvResolution.setText("超清（720P，25fps，600~1800kbps）");
        }
    }

    /**
     * 保存推流设置
     */
    private void writeSetting() {
        SharedPreferences sp = getSharedPreferences("RTMPSETTING", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CONST.NAME, tvName.getText().toString());
        editor.putString(CONST.STREAM, tvStream.getText().toString());
        editor.putBoolean(CONST.ORIENTATION, orientation);
        editor.putBoolean(CONST.ISFRONT, isFront);
        editor.putInt(CONST.VIDEOQUALITY, videoQuality);
        editor.commit();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.reStream:
            case R.id.reScreen:
            case R.id.reResolution:
            case R.id.reRatio:
                intent = new Intent(mContext, PushRtmpSettingEditActivity.class);
                intent.putExtra(CONST.NAME, tvName.getText().toString());
                intent.putExtra(CONST.STREAM, tvStream.getText().toString());
                intent.putExtra(CONST.ORIENTATION, orientation);
                intent.putExtra(CONST.ISFRONT, isFront);
                intent.putExtra(CONST.VIDEOQUALITY, videoQuality);
                startActivityForResult(intent, 1001);
                break;
            case R.id.tvStart:
                if (TextUtils.isEmpty(tvStream.getText().toString())) {
                    Toast.makeText(mContext, "请输入直播地址", Toast.LENGTH_SHORT).show();
                    return;
                }

                intent = new Intent(mContext, PushRtmpActivity.class);
                intent.putExtra(CONST.NAME, tvName.getText().toString());
                intent.putExtra(CONST.STREAM, tvStream.getText().toString());
                intent.putExtra(CONST.ORIENTATION, orientation);
                intent.putExtra(CONST.ISFRONT, isFront);
                intent.putExtra(CONST.VIDEOQUALITY, videoQuality);
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
                    readSetting();
                    break;
            }
        }
    }

}
