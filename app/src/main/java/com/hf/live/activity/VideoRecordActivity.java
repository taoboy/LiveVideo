package com.hf.live.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.qcloud.BeautySettingPannel;
import com.hf.live.qcloud.RecordProgressView;
import com.hf.live.qcloud.TCConstants;
import com.hf.live.util.CommonUtil;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.ugc.TXRecordCommon;
import com.tencent.ugc.TXUGCRecord;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.view.View.GONE;

/**
 * 视频录制
 */

public class VideoRecordActivity extends BaseActivity implements View.OnClickListener, AMapLocationListener, TXRecordCommon.ITXVideoRecordListener,
        BeautySettingPannel.IOnBeautyParamsChangeListener {

    private ImageView ivBack;
    private TXCloudVideoView mVideoView;
    private TXUGCRecord mTXCameraRecord;
    private RelativeLayout reRatio;//屏比
    private ImageView ivRatio, ivRatio1, ivRatio2, ivRatioMask;
    private RelativeLayout reResolution;//分辨率
    private ImageView ivResolutionMask;//蒙板
    private TextView tvResolution, tvResolution1, tvResolution2;
    private ImageView ivBeauty;//美颜按钮
    private BeautySettingPannel mBeautyPannelView;//美颜面板
    private BeautySettingPannel.BeautyParams mBeautyParams = new BeautySettingPannel.BeautyParams();//美颜设置
    private RelativeLayout reBottom;//录制布局
    private RecordProgressView mRecordProgressView;//录制进度条
    private TextView mProgressTime;//录制时间
    private ImageView ivFlash, ivSwitch, ivRecord, ivRemove, ivComplete;
    private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
    private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
    private String pro = "", city = "", dis = "", street = "";

    private boolean isStartPreview = false;//是否开始预览
    private boolean isRecording = false;//录制状态
    private boolean isPause = false;//是否暂停
    private boolean isSelected = false; // 回删状态
    private int minDuration = 5000, maxDuration = 60000;////视频录制的时长ms
    private int selectRatio = TXRecordCommon.VIDEO_ASPECT_RATIO_9_16; // 视频比例
    private int ratioFirst = TXRecordCommon.VIDEO_ASPECT_RATIO_1_1; // 视频比例
    private int rationSecond = TXRecordCommon.VIDEO_ASPECT_RATIO_3_4; // 视频比例
    private int selectResolution = TXRecordCommon.VIDEO_RESOLUTION_540_960; // 录制分辨率
    private int resolutionFirst = TXRecordCommon.VIDEO_RESOLUTION_360_640; // 录制分辨率
    private int resolutionSecond = TXRecordCommon.VIDEO_RESOLUTION_720_1280; // 录制分辨率
    private int mHomeOrientation = TXLiveConstants.VIDEO_ANGLE_HOME_DOWN; // 录制方向
    private int mRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT; // 渲染方向
    private boolean isFront = true;//是否为前置摄像头
    private boolean isFlashOn = false;//是否打开闪光灯
    private boolean isRatio = false;//是否显示屏比选择
    private boolean isResolution = false;//是否显示分辨率选择

    @Override
    protected void onStart() {
        super.onStart();
        onActivityRotation();
        startCameraPreview();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video_record);
        startLocation();
        initWidget();
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        mLocationOption = new AMapLocationClientOption();//初始化定位参数
        mLocationClient = new AMapLocationClient(this);//初始化定位
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setNeedAddress(true);//设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setOnceLocation(true);//设置是否只定位一次,默认为false
        mLocationOption.setWifiActiveScan(true);//设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setMockEnable(false);//设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setInterval(2000);//设置定位间隔,单位毫秒,默认为2000ms
        mLocationClient.setLocationOption(mLocationOption);//给定位客户端对象设置定位参数
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();//启动定位
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        pro = amapLocation.getProvince();
        city = amapLocation.getCity();
        dis = amapLocation.getDistrict();
        street = amapLocation.getStreet()+amapLocation.getStreetNum();
    }

    private void initWidget() {
        ivBack = (ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);
        reRatio = (RelativeLayout) findViewById(R.id.reRatio);
        ivRatio = (ImageView) findViewById(R.id.ivRatio);
        ivRatio.setOnClickListener(this);
        ivRatio1 = (ImageView) findViewById(R.id.ivRatio1);
        ivRatio1.setOnClickListener(this);
        ivRatio2 = (ImageView) findViewById(R.id.ivRatio2);
        ivRatio2.setOnClickListener(this);
        ivRatioMask = (ImageView) findViewById(R.id.ivRatioMask);
        reResolution = (RelativeLayout) findViewById(R.id.reResolution);
        tvResolution = (TextView) findViewById(R.id.tvResolution);
        tvResolution.setOnClickListener(this);
        tvResolution1 = (TextView) findViewById(R.id.tvResolution1);
        tvResolution1.setOnClickListener(this);
        tvResolution2 = (TextView) findViewById(R.id.tvResolution2);
        tvResolution2.setOnClickListener(this);
        ivResolutionMask = (ImageView) findViewById(R.id.ivResolutionMask);
        ivBeauty = (ImageView) findViewById(R.id.ivBeauty);
        ivBeauty.setOnClickListener(this);
        mBeautyPannelView = (BeautySettingPannel) findViewById(R.id.beauty_pannel);
        mBeautyPannelView.setBeautyParamsChangeListener(this);
        mBeautyPannelView.disableExposure();
        ivFlash = (ImageView) findViewById(R.id.ivFlash);
        ivFlash.setOnClickListener(this);
        ivSwitch = (ImageView) findViewById(R.id.ivSwitch);
        ivSwitch.setOnClickListener(this);
        ivRecord = (ImageView) findViewById(R.id.ivRecord);
        ivRecord.setOnClickListener(this);
        ivRemove = (ImageView) findViewById(R.id.ivRemove);
        ivRemove.setOnClickListener(this);
        ivComplete = (ImageView) findViewById(R.id.ivComplete);
        ivComplete.setOnClickListener(this);
        reBottom = (RelativeLayout) findViewById(R.id.reBottom);

        mVideoView = (TXCloudVideoView) findViewById(R.id.video_view);//准备一个预览摄像头画面的
        mVideoView.enableHardwareDecode(true);
        mRecordProgressView = (RecordProgressView) findViewById(R.id.record_progress_view);
        mRecordProgressView.setMinDuration(minDuration);
        mRecordProgressView.setMaxDuration(maxDuration);
        mProgressTime = (TextView) findViewById(R.id.progress_time);

    }

    /**
     * 初始化腾讯云录制容器
     */
    private void startCameraPreview() {
        if (isStartPreview) {
            return;
        }
        isStartPreview = true;

        mTXCameraRecord = TXUGCRecord.getInstance(this.getApplicationContext());
        mTXCameraRecord.setVideoRecordListener(this);//设置录制回调
        mTXCameraRecord.toggleTorch(isFlashOn);//设置闪光灯
        mTXCameraRecord.setAspectRatio(selectRatio);//视频比例
        mTXCameraRecord.setVideoResolution(selectResolution);//分辨率
        mTXCameraRecord.setHomeOrientation(mHomeOrientation);
        mTXCameraRecord.setRenderRotation(mRenderRotation);
        TXRecordCommon.TXUGCSimpleConfig param = new TXRecordCommon.TXUGCSimpleConfig();
        param.videoQuality = TXRecordCommon.VIDEO_QUALITY_HIGH;//视频质量，默认使用高质量
        param.isFront = isFront;//是否前置摄像头，使用
        param.minDuration = minDuration;//视频录制的最小时长ms
        param.maxDuration = maxDuration;//视频录制的最大时长ms
        mTXCameraRecord.startCameraSimplePreview(param,mVideoView);

        //美颜
        mTXCameraRecord.setBeautyDepth(mBeautyParams.mBeautyStyle, mBeautyParams.mBeautyLevel, mBeautyParams.mWhiteLevel, mBeautyParams.mRuddyLevel);
        mTXCameraRecord.setFaceScaleLevel(mBeautyParams.mFaceSlimLevel);
        mTXCameraRecord.setEyeScaleLevel(mBeautyParams.mBigEyeLevel);
        mTXCameraRecord.setFilter(mBeautyParams.mFilterBmp);
        mTXCameraRecord.setGreenScreenFile(mBeautyParams.mGreenFile, true);
        mTXCameraRecord.setMotionTmpl(mBeautyParams.mMotionTmplPath);
        mTXCameraRecord.setFaceShortLevel(mBeautyParams.mFaceShortLevel);
        mTXCameraRecord.setFaceVLevel(mBeautyParams.mFaceVLevel);
        mTXCameraRecord.setChinLevel(mBeautyParams.mChinSlimLevel);
        mTXCameraRecord.setNoseSlimLevel(mBeautyParams.mNoseScaleLevel);
    }

    @Override
    public void onRecordEvent(int event, Bundle bundle) {
        if (event == TXRecordCommon.EVT_ID_PAUSE) {
            mRecordProgressView.clipComplete();
        } else if (event == TXRecordCommon.EVT_CAMERA_CANNOT_USE) {
            Toast.makeText(this, "摄像头打开失败，请检查权限", Toast.LENGTH_SHORT).show();
        } else if (event == TXRecordCommon.EVT_MIC_CANNOT_USE) {
            Toast.makeText(this, "麦克风打开失败，请检查权限", Toast.LENGTH_SHORT).show();
        } else if (event == TXRecordCommon.EVT_ID_RESUME) {

        }
    }

    @Override
    public void onRecordProgress(long milliSecond) {
        if (mRecordProgressView == null) {
            return;
        }
        mRecordProgressView.setProgress((int) milliSecond);
        float timeSecondFloat = milliSecond / 1000f;
        int timeSecond = Math.round(timeSecondFloat);
        mProgressTime.setText(String.format(Locale.CHINA, "00:%02d", timeSecond));
        if (timeSecondFloat < minDuration / 1000) {
            ivComplete.setImageResource(R.drawable.iv_complete_disable);
            ivComplete.setEnabled(false);
        } else {
            ivComplete.setImageResource(R.drawable.iv_complete);
            ivComplete.setEnabled(true);
        }
    }

    @Override
    public void onRecordComplete(TXRecordCommon.TXRecordResult txRecordResult) {
        cancelDialog();

        if (txRecordResult.retCode < 0) {
            isRecording = false;
            int timeSecond = mTXCameraRecord.getPartsManager().getDuration() / 1000;
            mProgressTime.setText(String.format(Locale.CHINA, "00:%02d", timeSecond));
            Toast.makeText(this, "录制失败，原因：" + txRecordResult.descMsg, Toast.LENGTH_SHORT).show();
        } else {
//            mDuration = mTXCameraRecord.getPartsManager().getDuration();//视频总时长
            if (mTXCameraRecord != null) {
                mTXCameraRecord.getPartsManager().deleteAllParts();
            }
            startPreview(txRecordResult);
//            dialogEdit("是否要进行编辑？", txRecordResult);
        }
    }

    /**
     * 是否编辑对话框
     * @param message 标题
     */
    private void dialogEdit(String message, final TXRecordCommon.TXRecordResult txRecordResult) {
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
                startPreview(txRecordResult);
            }
        });

        llPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
//                startEditVideo();
            }
        });
    }

    /**
     * 进入预览界面
     */
    private void startPreview(TXRecordCommon.TXRecordResult mTXRecordResult) {
        if (mTXRecordResult != null && (mTXRecordResult.retCode == TXRecordCommon.RECORD_RESULT_OK
                || mTXRecordResult.retCode == TXRecordCommon.RECORD_RESULT_OK_REACHED_MAXDURATION
                || mTXRecordResult.retCode == TXRecordCommon.RECORD_RESULT_OK_LESS_THAN_MINDURATION)) {
            Intent intent = new Intent(getApplicationContext(), DisplayVideoActivity.class);
            intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, mTXRecordResult.videoPath);
            intent.putExtra(TCConstants.VIDEO_RECORD_COVERPATH, mTXRecordResult.coverPath);
//            intent.putExtra(TCConstants.VIDEO_RECORD_TYPE, TCConstants.VIDEO_RECORD_TYPE_UGC_RECORD);
//            intent.putExtra(TCConstants.VIDEO_RECORD_RESULT, mTXRecordResult.retCode);
//            intent.putExtra(TCConstants.VIDEO_RECORD_DESCMSG, mTXRecordResult.descMsg);
//            intent.putExtra(TCConstants.VIDEO_RECORD_DURATION, mTXCameraRecord.getPartsManager().getDuration());
//            intent.putExtra(TCConstants.VIDEO_RECORD_RESOLUTION, selectResolution);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onActivityRotation();
        if(mTXCameraRecord != null){
            mTXCameraRecord.stopCameraPreview();
        }
        if (isRecording && !isPause) {
            pauseRecord();
        }
        if (mTXCameraRecord != null) {
            mTXCameraRecord.pauseBGM();
        }
        isStartPreview = false;
        startCameraPreview();
    }

    /**
     * 用来在activity随着重力感应切换方向时，切换横竖屏录制
     * 注意：使用时，录制过程中或暂停后不允许切换横竖屏，如果开始录制时使用的是横屏录制，那么整段录制都要用横屏，否则录制失败。
     */
    protected void onActivityRotation() {
        // 自动旋转打开，Activity随手机方向旋转之后，需要改变录制方向
        int mobileRotation = this.getWindowManager().getDefaultDisplay().getRotation();
        mRenderRotation = TXLiveConstants.RENDER_ROTATION_PORTRAIT; // 渲染方向，因为activity也旋转了，本地渲染相对正方向的角度为0。
        mHomeOrientation = TXLiveConstants.VIDEO_ANGLE_HOME_DOWN;
        switch (mobileRotation) {
            case Surface.ROTATION_0:
                mHomeOrientation = TXLiveConstants.VIDEO_ANGLE_HOME_DOWN;
                break;
            case Surface.ROTATION_90:
                mHomeOrientation = TXLiveConstants.VIDEO_ANGLE_HOME_RIGHT;
                break;
            case Surface.ROTATION_270:
                mHomeOrientation = TXLiveConstants.VIDEO_ANGLE_HOME_LEFT;
                break;
            default:
                break;
        }
    }

    private long mLastClickTime;
    private void switchRecord() {
        long currentClickTime = System.currentTimeMillis();
        if (currentClickTime - mLastClickTime < 200) {
            return;
        }
        if (isRecording) {
            if (isPause) {
                if (mTXCameraRecord.getPartsManager().getPartsPathList().size() == 0) {
                    startRecord();
                } else {
                    resumeRecord();
                }
            } else {
                pauseRecord();
            }
        } else {
            startRecord();
        }
        mLastClickTime = currentClickTime;
    }

    private void startRecord() {
        // 在开始录制的时候，就不能再让activity旋转了，否则生成视频出错
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (mTXCameraRecord == null) {
            mTXCameraRecord = TXUGCRecord.getInstance(this.getApplicationContext());
        }

        String customVideoPath = getCustomVideoOutputPath();
        String customCoverPath = customVideoPath.replace(CONST.VIDEOTYPE, CONST.IMGTYPE);

        int result = mTXCameraRecord.startRecord(customVideoPath, customCoverPath);
        if (result != TXRecordCommon.START_RECORD_OK) {
            if (result == TXRecordCommon.START_RECORD_ERR_NOT_INIT) {
                Toast.makeText(this.getApplicationContext(), "别着急，画面还没出来", Toast.LENGTH_SHORT).show();
            } else if (result == TXRecordCommon.START_RECORD_ERR_IS_IN_RECORDING) {
                Toast.makeText(this.getApplicationContext(), "还有录制的任务没有结束", Toast.LENGTH_SHORT).show();
            } else if (result == TXRecordCommon.START_RECORD_ERR_VIDEO_PATH_IS_EMPTY) {
                Toast.makeText(this.getApplicationContext(), "传入的视频路径为空", Toast.LENGTH_SHORT).show();
            } else if (result == TXRecordCommon.START_RECORD_ERR_API_IS_LOWER_THAN_18) {
                Toast.makeText(this.getApplicationContext(), "版本太低", Toast.LENGTH_SHORT).show();
            }
//            mTXCameraRecord.setVideoRecordListener(null);
//            mTXCameraRecord.stopRecord();
            return;
        }

        ivRecord.setImageResource(R.drawable.iv_stop);
        ivRemove.setImageResource(R.drawable.iv_del_last_disable);
        ivRemove.setEnabled(false);
        ivRatioMask.setVisibility(View.VISIBLE);
        ivResolutionMask.setVisibility(View.VISIBLE);

//        if (!TextUtils.isEmpty(mBGMPath)) {
//            mBGMDuration = mTXCameraRecord.setBGM(mBGMPath);
//            mTXCameraRecord.playBGMFromTime(0, mBGMDuration);
//            mBGMPlayingPath = mBGMPath;
//            TXCLog.i(TAG, "music duration = " + mTXCameraRecord.getMusicDuration(mBGMPath));
//        }
//
//        mAudioCtrl.setPusher(mTXCameraRecord);
        isRecording = true;
        isPause = false;
//        ImageView liveRecord = (ImageView) findViewById(R.id.record);
//        if (liveRecord != null) liveRecord.setBackgroundResource(R.drawable.video_stop);
//        requestAudioFocus();
//
//        mIvMusicMask.setVisibility(View.VISIBLE);
//        mRadioGroup.setVisibility(GONE);
    }

    private String getCustomVideoOutputPath() {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = sdf.format(new Date(currentTime));
        CommonUtil.saveVideoInfo(this, time, "video", pro, city, dis, street);
        File outputFolder = new File(CONST.VIDEO_ADDR);
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }
        String tempOutputPath = outputFolder.getPath() + File.separator + time + CONST.VIDEOTYPE;
        return tempOutputPath;
    }

    private void resumeRecord() {
        if (mTXCameraRecord == null) {
            return;
        }
        int startResult = mTXCameraRecord.resumeRecord();
        if (startResult != TXRecordCommon.START_RECORD_OK) {
            if (startResult == TXRecordCommon.START_RECORD_ERR_NOT_INIT) {
                Toast.makeText(this.getApplicationContext(), "别着急，画面还没出来", Toast.LENGTH_SHORT).show();
            } else if (startResult == TXRecordCommon.START_RECORD_ERR_IS_IN_RECORDING) {
                Toast.makeText(this.getApplicationContext(), "还有录制的任务没有结束", Toast.LENGTH_SHORT).show();
            }
            return;
        }
//        if (!TextUtils.isEmpty(mBGMPath)) {
//            if (mBGMPlayingPath == null || !mBGMPath.equals(mBGMPlayingPath)) {
//                mTXCameraRecord.setBGM(mBGMPath);
//                mTXCameraRecord.playBGMFromTime(0, mBGMDuration);
//                mBGMPlayingPath = mBGMPath;
//            } else {
//                mTXCameraRecord.resumeBGM();
//            }
//        }


//        ImageView liveRecord = (ImageView) findViewById(R.id.record);
//        if (liveRecord != null) {
//            liveRecord.setBackgroundResource(R.drawable.video_stop);
//        }
        ivRecord.setImageResource(R.drawable.iv_stop);
        ivRemove.setImageResource(R.drawable.iv_del_last_disable);
        ivRemove.setEnabled(false);
        ivRatioMask.setVisibility(View.VISIBLE);
        ivResolutionMask.setVisibility(View.VISIBLE);

        isPause = false;
        isSelected = false;
//        requestAudioFocus();
//
//        mRadioGroup.setVisibility(GONE);
    }

    private void pauseRecord() {
        ivRecord.setImageResource(R.drawable.iv_start);
//        ImageView liveRecord = (ImageView) findViewById(R.id.record);
//        if (liveRecord != null) {
//            liveRecord.setBackgroundResource(R.drawable.start_record);
//        }
        isPause = true;
        ivRemove.setImageResource(R.drawable.iv_del_last);
        ivRemove.setEnabled(true);

        if (mTXCameraRecord != null) {
//            if (!TextUtils.isEmpty(mBGMPlayingPath)) {
//                mTXCameraRecord.pauseBGM();
//            }
            mTXCameraRecord.pauseRecord();
        }
//        abandonAudioFocus();
//
//        mRadioGroup.setVisibility(View.VISIBLE);
    }

    private void stopRecord() {
        if (mTXCameraRecord != null) {
            mTXCameraRecord.stopBGM();
            mTXCameraRecord.stopRecord();
        }
//        ImageView liveRecord = (ImageView) findViewById(R.id.record);
//        if (liveRecord != null) liveRecord.setBackgroundResource(R.drawable.start_record);
        isRecording = false;
        isPause = false;
//        abandonAudioFocus();
//        mRadioGroup.setVisibility(View.VISIBLE);
    }

    /**
     * 控制闪光灯
     */
    private void switchFlash() {
        if (isFront) {
            return;
        }
        if (isFlashOn == false) {
            if (mTXCameraRecord != null) {
                mTXCameraRecord.toggleTorch(true);
            }
            ivFlash.setImageResource(R.drawable.iv_flash_on);
        }else {
            if (mTXCameraRecord != null) {
                mTXCameraRecord.toggleTorch(false);
            }
            ivFlash.setImageResource(R.drawable.iv_flash_off);
        }
        isFlashOn = !isFlashOn;
    }

    /**
     * 关闭闪光灯
     */
    private void closeFlash() {
        // 设置闪光灯的状态为关闭
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
        if (mTXCameraRecord != null) {
            mTXCameraRecord.switchCamera(isFront);
        }
    }

    /**
     * 删除上一个片段
     */
    private void deleteLastPart() {
        if (isRecording && !isPause) {
            return;
        }
        if (!isSelected) {
            isSelected = true;
            mRecordProgressView.selectLast();
        } else {
            isSelected = false;
            mRecordProgressView.deleteLast();
            mTXCameraRecord.getPartsManager().deleteLastPart();
            int timeSecond = mTXCameraRecord.getPartsManager().getDuration() / 1000;
            mProgressTime.setText(String.format(Locale.CHINA, "00:%02d", timeSecond));
            if (timeSecond < minDuration / 1000) {
                ivComplete.setImageResource(R.drawable.iv_complete_disable);
                ivComplete.setEnabled(false);
            } else {
                ivComplete.setImageResource(R.drawable.iv_complete);
                ivComplete.setEnabled(true);
            }

            if (mTXCameraRecord.getPartsManager().getPartsPathList().size() == 0) {
                ivRatioMask.setVisibility(GONE);
                ivResolutionMask.setVisibility(GONE);
            }
        }
    }

    /**
     * 屏比动画
     */
    private void ratioAnimation() {
        if (!isRatio) {
            showAnimation(reRatio);
        } else {
            hideAnimation(reRatio);
        }
        isRatio = !isRatio;
    }

    /**
     * 选择另一个屏比
     * @param targetRatio
     */
    private void selectAnotherRatio(int targetRatio) {
        if (mTXCameraRecord != null) {
            ratioAnimation();

            selectRatio = targetRatio;

            if (selectRatio == TXRecordCommon.VIDEO_ASPECT_RATIO_9_16) {
                mTXCameraRecord.setAspectRatio(TXRecordCommon.VIDEO_ASPECT_RATIO_9_16);

            } else if (selectRatio == TXRecordCommon.VIDEO_ASPECT_RATIO_3_4) {
                mTXCameraRecord.setAspectRatio(TXRecordCommon.VIDEO_ASPECT_RATIO_3_4);

            } else if (selectRatio == TXRecordCommon.VIDEO_ASPECT_RATIO_1_1) {
                mTXCameraRecord.setAspectRatio(TXRecordCommon.VIDEO_ASPECT_RATIO_1_1);
            }

            setSelectRatio();
        }
    }

    /**
     * 设置选择的屏比
     */
    private void setSelectRatio() {
        if (selectRatio == TXRecordCommon.VIDEO_ASPECT_RATIO_9_16) {
            ivRatio.setImageResource(R.drawable.iv_aspect169);
            ratioFirst = TXRecordCommon.VIDEO_ASPECT_RATIO_1_1;
            ivRatio1.setImageResource(R.drawable.iv_aspect11);
            rationSecond = TXRecordCommon.VIDEO_ASPECT_RATIO_3_4;
            ivRatio2.setImageResource(R.drawable.iv_aspect43);
        } else if (selectRatio == TXRecordCommon.VIDEO_ASPECT_RATIO_1_1) {
            ivRatio.setImageResource(R.drawable.iv_aspect11);
            ratioFirst = TXRecordCommon.VIDEO_ASPECT_RATIO_3_4;
            ivRatio1.setImageResource(R.drawable.iv_aspect43);
            rationSecond = TXRecordCommon.VIDEO_ASPECT_RATIO_9_16;
            ivRatio2.setImageResource(R.drawable.iv_aspect169);
        } else {
            ivRatio.setImageResource(R.drawable.iv_aspect43);
            ratioFirst = TXRecordCommon.VIDEO_ASPECT_RATIO_1_1;
            ivRatio1.setImageResource(R.drawable.iv_aspect11);
            rationSecond = TXRecordCommon.VIDEO_ASPECT_RATIO_9_16;
            ivRatio2.setImageResource(R.drawable.iv_aspect169);
        }
    }

    /**
     * 分辨率动画
     */
    private void resolutionAnimation() {
        if (!isResolution) {
            showAnimation(reResolution);
        } else {
            hideAnimation(reResolution);
        }
        isResolution = !isResolution;
    }

    /**
     * 选择另一个屏比
     * @param targetResolution
     */
    private void selectAnotherResolution(int targetResolution) {
        if (mTXCameraRecord != null) {
            resolutionAnimation();

            selectResolution = targetResolution;

            if (selectResolution == TXRecordCommon.VIDEO_RESOLUTION_720_1280) {
                mTXCameraRecord.setVideoResolution(TXRecordCommon.VIDEO_RESOLUTION_720_1280);
            } else if (selectResolution == TXRecordCommon.VIDEO_RESOLUTION_540_960) {
                mTXCameraRecord.setVideoResolution(TXRecordCommon.VIDEO_RESOLUTION_540_960);
            } else if (selectResolution == TXRecordCommon.VIDEO_RESOLUTION_360_640) {
                mTXCameraRecord.setVideoResolution(TXRecordCommon.VIDEO_RESOLUTION_360_640);
            }

            setSelectResolution();
        }
    }

    /**
     * 设置选择的分辨率
     */
    private void setSelectResolution() {
        if (selectResolution == TXRecordCommon.VIDEO_RESOLUTION_720_1280) {
            tvResolution.setText("720p");
            resolutionFirst = TXRecordCommon.VIDEO_RESOLUTION_360_640;
            tvResolution1.setText("360p");
            resolutionSecond = TXRecordCommon.VIDEO_RESOLUTION_540_960;
            tvResolution2.setText("540p");
        } else if (selectResolution == TXRecordCommon.VIDEO_RESOLUTION_540_960) {
            tvResolution.setText("540p");
            resolutionFirst = TXRecordCommon.VIDEO_RESOLUTION_360_640;
            tvResolution1.setText("360p");
            resolutionSecond = TXRecordCommon.VIDEO_RESOLUTION_720_1280;
            tvResolution2.setText("720p");
        } else {
            tvResolution.setText("360p");
            resolutionFirst = TXRecordCommon.VIDEO_RESOLUTION_540_960;
            tvResolution1.setText("540p");
            resolutionSecond = TXRecordCommon.VIDEO_RESOLUTION_720_1280;
            tvResolution2.setText("720p");
        }
    }

    /**
     * 屏比、分辨率动画
     * @param view
     */
    private void hideAnimation(final View view) {
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(view, "translationX", 0f, 2 * (getResources().getDimension(R.dimen.ugc_aspect_divider) + getResources().getDimension(R.dimen.ugc_aspect_width)));
        showAnimator.setDuration(80);
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        showAnimator.start();
    }

    /**
     * 屏比、分辨率动画
     * @param view
     */
    private void showAnimation(final View view) {
        ObjectAnimator showAnimator = ObjectAnimator.ofFloat(view, "translationX",  2 * (getResources().getDimension(R.dimen.ugc_aspect_divider) + getResources().getDimension(R.dimen.ugc_aspect_width)), 0f);
        showAnimator.setDuration(80);
        showAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        showAnimator.start();
    }

    /**
     * 美颜控制面板
     * @param params
     * @param key
     */
    @Override
    public void onBeautyParamsChange(BeautySettingPannel.BeautyParams params, int key) {
        switch (key) {
            case BeautySettingPannel.BEAUTYPARAM_BEAUTY:
                mBeautyParams.mBeautyLevel = params.mBeautyLevel;
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setBeautyDepth(mBeautyParams.mBeautyStyle, mBeautyParams.mBeautyLevel, mBeautyParams.mWhiteLevel, mBeautyParams.mRuddyLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_WHITE:
                mBeautyParams.mWhiteLevel = params.mWhiteLevel;
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setBeautyDepth(mBeautyParams.mBeautyStyle, mBeautyParams.mBeautyLevel, mBeautyParams.mWhiteLevel, mBeautyParams.mRuddyLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_FACE_LIFT:
                mBeautyParams.mFaceSlimLevel = params.mFaceSlimLevel;
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setFaceScaleLevel(params.mFaceSlimLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_BIG_EYE:
                mBeautyParams.mBigEyeLevel = params.mBigEyeLevel;
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setEyeScaleLevel(params.mBigEyeLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_FILTER:
                mBeautyParams.mFilterBmp = params.mFilterBmp;
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setFilter(params.mFilterBmp);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_MOTION_TMPL:
                mBeautyParams.mMotionTmplPath = params.mMotionTmplPath;
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setMotionTmpl(params.mMotionTmplPath);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_GREEN:
                mBeautyParams.mGreenFile = params.mGreenFile;
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setGreenScreenFile(params.mGreenFile, true);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_RUDDY:
                mBeautyParams.mRuddyLevel = params.mRuddyLevel;
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setBeautyDepth(mBeautyParams.mBeautyStyle, mBeautyParams.mBeautyLevel, mBeautyParams.mWhiteLevel, mBeautyParams.mRuddyLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_BEAUTY_STYLE:
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setBeautyStyle(params.mBeautyStyle);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_FACEV:
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setFaceVLevel(params.mFaceVLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_FACESHORT:
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setFaceShortLevel(params.mFaceShortLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_CHINSLIME:
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setChinLevel(params.mChinSlimLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_NOSESCALE:
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setNoseSlimLevel(params.mNoseScaleLevel);
                }
                break;
            case BeautySettingPannel.BEAUTYPARAM_FILTER_MIX_LEVEL:
                if (mTXCameraRecord != null) {
                    mTXCameraRecord.setSpecialRatio(params.mFilterMixLevel / 10.f);
                }
                break;
            default:
                break;
        }
    }

    private void back() {
        if (!isRecording) {
            finish();
        }
        if (isPause) {
            if (mTXCameraRecord != null) {
                mTXCameraRecord.getPartsManager().deleteAllParts();
            }
            finish();
        } else {
            pauseRecord();
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
                pauseRecord();
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dialogExit("正在拍摄，确定离开？");
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                dialogExit("正在拍摄，确定离开？");
                break;
            case R.id.ivRatio:
                ratioAnimation();
                break;
            case R.id.ivRatio1:
                selectAnotherRatio(ratioFirst);
                break;
            case R.id.ivRatio2:
                selectAnotherRatio(rationSecond);
                break;
            case R.id.tvResolution:
                resolutionAnimation();
                break;
            case R.id.tvResolution1:
                selectAnotherResolution(resolutionFirst);
                break;
            case R.id.tvResolution2:
                selectAnotherResolution(resolutionSecond);
                break;
            case R.id.ivBeauty:
                mBeautyPannelView.setVisibility(mBeautyPannelView.getVisibility() == View.VISIBLE ? GONE : View.VISIBLE);
                ivBeauty.setImageResource(mBeautyPannelView.getVisibility() == View.VISIBLE ? R.drawable.iv_record_beautiful_girl_press : R.drawable.iv_record_beautiful_girl);
                reBottom.setVisibility(mBeautyPannelView.getVisibility() == View.VISIBLE ? GONE : View.VISIBLE);
                break;
            case R.id.ivFlash:
                switchFlash();
                break;
            case R.id.ivSwitch:
                switchCamera();
                break;
            case R.id.ivRecord:
                if (isRatio) {
                    hideAnimation(reRatio);
                    isRatio = !isRatio;
                }
                switchRecord();
                break;
            case R.id.ivRemove:
                deleteLastPart();
                break;
            case R.id.ivComplete:
                showDialog();
                stopRecord();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mTXCameraRecord != null) {
            mTXCameraRecord.setVideoProcessListener(null); // 这里要取消监听，否则在上面的回调中又会重新开启预览
            mTXCameraRecord.stopCameraPreview();
            isStartPreview = false;
            closeFlash();
        }
        if (isRecording && !isPause) {
            pauseRecord();
        }
        if (mTXCameraRecord != null) {
            mTXCameraRecord.pauseBGM();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //进度条
        if (mRecordProgressView != null) {
            mRecordProgressView.release();
        }

        if (mTXCameraRecord != null) {
            mTXCameraRecord.stopBGM();
            mTXCameraRecord.stopCameraPreview();
            mTXCameraRecord.setVideoRecordListener(null);
            mTXCameraRecord.getPartsManager().deleteAllParts();
            mTXCameraRecord.release();
            mTXCameraRecord = null;
            isStartPreview = false;
        }
    }

}
