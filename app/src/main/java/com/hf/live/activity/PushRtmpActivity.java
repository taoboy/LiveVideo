package com.hf.live.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.qcloud.BeautySettingPannel;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

import static android.view.View.GONE;

/**
 * rtmp推流
 */

public class PushRtmpActivity extends BaseActivity implements View.OnClickListener, ITXLivePushListener, BeautySettingPannel.IOnBeautyParamsChangeListener {

    private TXLivePushConfig mLivePushConfig;
    private TXLivePusher mLivePusher;
    private TXCloudVideoView mCaptureView;
    private int videoQuality = TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION;//视频质量
    private boolean mAutoBitrate = true;//是否为动态码率
    private boolean mAutoResolution = false;//是否为动态分辨率
    private boolean isFront = true;//是否为前置摄像头
    private boolean orientation = true;//推流方向，默认为横屏
    private boolean isFlashOn = false;//是否打开闪光灯
    private boolean isShowLog = false, isHard = false;//是否显示日志、软硬件解码
    private ImageView ivBack, ivStart, ivSwitch, ivFlash, ivBeauty;
    private TextView tvSpeed, tvLog, tvHard;
    private RelativeLayout reBottom;

    private BeautySettingPannel mBeautyPannelView;//美颜面板
    private int mBeautyLevel = 5;
    private int mWhiteningLevel = 3;
    private int mRuddyLevel = 2;
    private int mBeautyStyle = TXLiveConstants.BEAUTY_STYLE_SMOOTH;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        orientation = getIntent().getBooleanExtra("orientation", true);
        if (orientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        isFront = getIntent().getBooleanExtra("isFront", isFront);
        videoQuality = getIntent().getIntExtra("videoQuality", videoQuality);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_push_rtmp);
        initWidget();
    }

    private void initWidget() {
        ivBack = (ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);
        ivStart = (ImageView) findViewById(R.id.ivStart);
        ivStart.setOnClickListener(this);
        ivSwitch = (ImageView) findViewById(R.id.ivSwitch);
        ivSwitch.setOnClickListener(this);
        ivFlash = (ImageView) findViewById(R.id.ivFlash);
        ivFlash.setOnClickListener(this);
        tvSpeed = (TextView) findViewById(R.id.tvSpeed);
        tvLog = (TextView) findViewById(R.id.tvLog);
        tvLog.setOnClickListener(this);
        tvHard = (TextView) findViewById(R.id.tvHard);
        tvHard.setOnClickListener(this);
        ivBeauty = (ImageView) findViewById(R.id.ivBeauty);
        ivBeauty.setOnClickListener(this);
        mBeautyPannelView = (BeautySettingPannel) findViewById(R.id.beauty_pannel);
        mBeautyPannelView.setBeautyParamsChangeListener(this);
        mBeautyPannelView.disableExposure();
        reBottom = (RelativeLayout) findViewById(R.id.reBottom);

        initPusher();
    }

    /**
     * 初始化推流器
     */
    private void initPusher() {
        mLivePusher = new TXLivePusher(this);
        mLivePusher.setPushListener(this);
        mLivePusher.setVideoQuality(videoQuality, mAutoBitrate, mAutoResolution);
        mLivePushConfig = new TXLivePushConfig();
        mLivePushConfig.setFrontCamera(isFront);//默认前置摄像头
        mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_AUTO);//设置硬件解码为auto
        mLivePushConfig.setBeautyFilter(mBeautyLevel, mWhiteningLevel, mRuddyLevel);
        mLivePushConfig.setTouchFocus(true);//自动对焦
        mLivePushConfig.setVideoFPS(25);//帧率
        //设置观众端方向
        if (orientation) {
            mLivePushConfig.setHomeOrientation(TXLiveConstants.VIDEO_ANGLE_HOME_RIGHT);
        }else {
            mLivePushConfig.setHomeOrientation(TXLiveConstants.VIDEO_ANGLE_HOME_DOWN);
        }
        mLivePusher.setConfig(mLivePushConfig);

        mCaptureView = (TXCloudVideoView) findViewById(R.id.video_view);
        mCaptureView.setLogMargin(10, 10, 10, 60);//设置日志边界
        mLivePusher.startCameraPreview(mCaptureView);
        mCaptureView.setVisibility(View.VISIBLE);
    }

    /**
     * 开始推流
     */
    private void startPushRtmp() {
        String stream = getIntent().getStringExtra("stream");
        if (mLivePusher != null && !TextUtils.isEmpty(stream)) {
            mLivePusher.startPusher(stream);
        }
    }

    /**
     * 停止推流
     */
    private void stopPushRtmp() {
        if (mLivePusher != null) {
            mLivePusher.stopPusher();
        }
    }

    /**
     * 切换推流状态
     */
    private void switchPushRtmp() {
        if (mLivePusher != null) {
            if (mLivePusher.isPushing()) {
                stopPushRtmp();
                ivStart.setImageResource(R.drawable.iv_start);
            }else {
                startPushRtmp();
                ivStart.setImageResource(R.drawable.iv_stop);
            }
        }
    }

    /**
     * 控制闪光灯
     */
    private void switchFlash() {
        if (isFront) {
            return;
        }
        if (isFlashOn == false) {
            if (mLivePusher != null) {
                mLivePusher.turnOnFlashLight(true);
            }
            ivFlash.setImageResource(R.drawable.iv_flash_on);
        }else {
            if (mLivePusher != null) {
                mLivePusher.turnOnFlashLight(false);
            }
            ivFlash.setImageResource(R.drawable.iv_flash_off);
        }
        isFlashOn = !isFlashOn;
    }

    /**
     * 关闭闪光灯
     */
    private void closeFlash() {
        if (isFlashOn) {
            isFlashOn = false;
            if (isFront) {
                ivFlash.setImageResource(R.drawable.iv_flash_disable);
                ivFlash.setEnabled(false);
            } else {
                ivFlash.setImageResource(R.drawable.iv_flash_off);
                ivFlash.setEnabled(true);
            }
        }
    }

    /**
     * 切换摄像头
     */
    private void switchCamera() {
        isFront = !isFront;
        isFlashOn = false;
        if (isFront) {//前置
            ivFlash.setImageResource(R.drawable.iv_flash_disable);
            ivFlash.setEnabled(false);
        }else {//后置
            ivFlash.setImageResource(R.drawable.iv_flash_off);
            ivFlash.setEnabled(true);
        }
        if (mLivePusher != null) {
            mLivePusher.switchCamera();
        }
    }

    /**
     * 切换日志状态
     */
    private void switchLog() {
        if (isShowLog) {
            if (mCaptureView != null) {
                mCaptureView.showLog(false);
            }
            isShowLog = false;
        }else {
            if (mCaptureView != null) {
                mCaptureView.showLog(true);
            }
            isShowLog = true;
        }
    }

    /**
     * 切换软硬件解码状态
     */
    private void switchHard() {
        if (mLivePushConfig != null) {
            if (isHard == false){
                if(Build.VERSION.SDK_INT < 18){
                    Toast.makeText(getApplicationContext(), "硬件加速失败，当前手机API级别过低（最低18）", Toast.LENGTH_SHORT).show();
                }else {
                    mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_HARDWARE);
                    Toast.makeText(getApplicationContext(), "开启硬件加速", Toast.LENGTH_SHORT).show();
                    isHard = true;
                }
            }else {
                mLivePushConfig.setHardwareAcceleration(TXLiveConstants.ENCODE_VIDEO_SOFTWARE);
                Toast.makeText(getApplicationContext(), "取消硬件加速", Toast.LENGTH_SHORT).show();
                isHard = false;
            }
            if (mLivePusher != null) {
                mLivePusher.setConfig(mLivePushConfig);
            }
        }
    }

    @Override
    public void onPushEvent(int event, Bundle param) {

    }

    @Override
    public void onNetStatus(Bundle status) {
        String onNetStatus =
//                "Current status, CPU:"+status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE)+
                "RES:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH)+"*"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT)+
                        "  SPD:"+status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED)+"Kbps"+
                        "  FPS:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS);
//                ", ARA:"+status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE)+"Kbps"+
//                ", VRA:"+status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE)+"Kbps";
        Log.e("onNetStatus", onNetStatus);
        tvSpeed.setText(onNetStatus);
    }

    /**
     * 美颜控制面板
     * @param params
     * @param key
     */
    @Override
    public void onBeautyParamsChange(BeautySettingPannel.BeautyParams params, int key) {
        switch (key) {
            case BeautySettingPannel.BEAUTYPARAM_EXPOSURE:
                if (mLivePusher != null) {
                    mLivePusher.setExposureCompensation(params.mExposure);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_BEAUTY:
                mBeautyLevel = params.mBeautyLevel;
                if (mLivePusher != null) {
                    mLivePusher.setBeautyFilter(mBeautyStyle, mBeautyLevel, mWhiteningLevel, mRuddyLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_WHITE:
                mWhiteningLevel = params.mWhiteLevel;
                if (mLivePusher != null) {
                    mLivePusher.setBeautyFilter(mBeautyStyle, mBeautyLevel, mWhiteningLevel, mRuddyLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_BIG_EYE:
                if (mLivePusher != null) {
                    mLivePusher.setEyeScaleLevel(params.mBigEyeLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_FACE_LIFT:
                if (mLivePusher != null) {
                    mLivePusher.setFaceSlimLevel(params.mFaceSlimLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_FILTER:
                if (mLivePusher != null) {
                    mLivePusher.setFilter(params.mFilterBmp);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_GREEN:
                if (mLivePusher != null) {
                    mLivePusher.setGreenScreenFile(params.mGreenFile);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_MOTION_TMPL:
                if (mLivePusher != null) {
                    mLivePusher.setMotionTmpl(params.mMotionTmplPath);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_RUDDY:
                mRuddyLevel = params.mRuddyLevel;
                if (mLivePusher != null) {
                    mLivePusher.setBeautyFilter(mBeautyStyle, mBeautyLevel, mWhiteningLevel, mRuddyLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_BEAUTY_STYLE:
                mBeautyStyle = params.mBeautyStyle;
                if (mLivePusher != null) {
                    mLivePusher.setBeautyFilter(mBeautyStyle, mBeautyLevel, mWhiteningLevel, mRuddyLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_FACEV:
                if (mLivePusher != null) {
                    mLivePusher.setFaceVLevel(params.mFaceVLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_FACESHORT:
                if (mLivePusher != null) {
                    mLivePusher.setFaceShortLevel(params.mFaceShortLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_CHINSLIME:
                if (mLivePusher != null) {
                    mLivePusher.setChinLevel(params.mChinSlimLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_NOSESCALE:
                if (mLivePusher != null) {
                    mLivePusher.setNoseSlimLevel(params.mNoseScaleLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_FILTER_MIX_LEVEL:
                if (mLivePusher != null) {
                    mLivePusher.setSpecialRatio(params.mFilterMixLevel/10.f);
                }
                break;
//            case BeautySettingPannel.BEAUTYPARAM_CAPTURE_MODE:
//                if (mLivePusher != null) {
//                    boolean bEnable = ( 0 == params.mCaptureMode ? false : true);
//                    mLivePusher.enableHighResolutionCapture(bEnable);
//                }
//                break;
//            case BeautySettingPannel.BEAUTYPARAM_SHARPEN:
//                if (mLivePusher != null) {
//                    mLivePusher.setSharpenLevel(params.mSharpenLevel);
//                }
//                break;
        }
    }

    /**
     * 离开
     * @param message 标题
     */
    private void dialogExit(String message) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_delete, null);
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
        LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

        final Dialog dialog = new Dialog(this, R.style.CustomProgressDialog);
        dialog.setContentView(view);
        dialog.show();

        tvMessage.setText(message);
        llNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });

        llPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                stopPushRtmp();
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dialogExit("正在推流，确定离开？");
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                dialogExit("正在推流，确定离开？");
                break;
            case R.id.ivStart:
                switchPushRtmp();
                break;
            case R.id.ivFlash:
                switchFlash();
                break;
            case R.id.ivSwitch:
                switchCamera();
                break;
            case R.id.tvLog:
                switchLog();
                break;
            case R.id.tvHard:
                switchHard();
                break;
            case R.id.ivBeauty:
                mBeautyPannelView.setVisibility(mBeautyPannelView.getVisibility() == View.VISIBLE ? GONE : View.VISIBLE);
                ivBeauty.setImageResource(mBeautyPannelView.getVisibility() == View.VISIBLE ? R.drawable.iv_record_beautiful_girl_press : R.drawable.iv_record_beautiful_girl);
                reBottom.setVisibility(mBeautyPannelView.getVisibility() == View.VISIBLE ? GONE : View.VISIBLE);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCaptureView != null) {
            mCaptureView.onResume();
        }

        if (mLivePusher != null && mLivePusher.isPushing()) {
            mLivePusher.resumePusher();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if (mCaptureView != null) {
            mCaptureView.onPause();
        }
        closeFlash();

        if (mLivePusher != null && mLivePusher.isPushing()) {
            mLivePusher.pausePusher();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPushRtmp();
        if (mCaptureView != null) {
            mCaptureView.onDestroy();
        }
    }

}
