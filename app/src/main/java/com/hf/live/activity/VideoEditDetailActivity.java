package com.hf.live.activity;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.qcloud.PlayState;
import com.hf.live.qcloud.TCBGMSettingFragment;
import com.hf.live.qcloud.TCBubbleViewInfoManager;
import com.hf.live.qcloud.TCConstants;
import com.hf.live.qcloud.TCCutterFragment;
import com.hf.live.qcloud.TCMotionFragment;
import com.hf.live.qcloud.TCPasterViewInfoManager;
import com.hf.live.qcloud.TCStaticFilterFragment;
import com.hf.live.qcloud.TCTimeFragment;
import com.hf.live.qcloud.TCToolsView;
import com.hf.live.qcloud.TCVideoEditerWrapper;
import com.hf.live.qcloud.VideoProgressController;
import com.hf.live.qcloud.VideoProgressView;
import com.hf.live.view.MyDialog;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 视频剪辑详情
 */

public class VideoEditDetailActivity extends FragmentActivity implements TCToolsView.OnItemClickListener,
        View.OnClickListener, TCVideoEditerWrapper.TXVideoPreviewListenerWrapper, TXVideoEditer.TXVideoGenerateListener{

    private Context mContext;
    private LinearLayout llBack;
    private TextView tvTitle, tvControl;
    private TXVideoEditer mTXVideoEditer;                   // SDK接口类
    private FrameLayout mVideoPlayerLayout;                 // 视频承载布局
    private ImageView ivPlay;                            // 播放按钮
    private TCToolsView mToolsView;                         // 底部工具栏
    private Fragment mCurrentFragment,                      // 标记当前的Fragment
            mCutterFragment,                                // 裁剪的Fragment
            mTimeFragment,                                  // 时间特效的Fragment
            mStaticFilterFragment,                          // 静态滤镜的Fragment
            mMotionFragment,                                // 动态滤镜的Fragment
            mBGMSettingFragment;                            // BGM设置的Fragment

    private int mCurrentState = PlayState.STATE_NONE;       // 播放器当前状态
    private String mVideoOutputPath;                        // 视频输出路径
    private long mVideoDuration;                            // 视频的总时长
    private long mPreviewAtTime;                            // 当前单帧预览的时间
    private VideoEditDetailActivity.TXPhoneStateListener mPhoneListener;            // 电话监听
    private KeyguardManager mKeyguardManager;
    private MyDialog mDialog;

    /**
     * 缩略图进度条相关
     */
    private VideoProgressView mVideoProgressView;
    private VideoProgressController mVideoProgressController;
    private VideoProgressController.VideoProgressSeekListener mVideoProgressSeekListener = new VideoProgressController.VideoProgressSeekListener() {
        @Override
        public void onVideoProgressSeek(long currentTimeMs) {
            previewAtTime(currentTimeMs);
        }

        @Override
        public void onVideoProgressSeekFinish(long currentTimeMs) {
            previewAtTime(currentTimeMs);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit_detail);
        mContext = this;
        initWidget();
    }

    private void showDialog() {
        if (mDialog == null) {
            mDialog = new MyDialog(mContext);
            mDialog.setCanceledOnTouchOutside(false);
        }
        mDialog.show();
        mDialog.setPercent(0);
    }

    private void cancelDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("视频剪辑");
        tvControl = (TextView) findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setText("完成");
        tvControl.setVisibility(View.VISIBLE);
        mToolsView = (TCToolsView) findViewById(R.id.editer_tools_view);
        mToolsView.setOnItemClickListener(this);
        mVideoPlayerLayout = (FrameLayout) findViewById(R.id.editer_fl_video);
        ivPlay = (ImageView) findViewById(R.id.ivPlay);
        ivPlay.setOnClickListener(this);

        TCVideoEditerWrapper wrapper = TCVideoEditerWrapper.getInstance();
        wrapper.addTXVideoPreviewListenerWrapper(this);

        mTXVideoEditer = wrapper.getEditer();
        if (mTXVideoEditer == null || wrapper.getTXVideoInfo() == null) {
            Toast.makeText(this, "状态异常，结束编辑", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mVideoDuration = mTXVideoEditer.getTXVideoInfo().duration;
        TCVideoEditerWrapper.getInstance().setCutterStartTime(0, mVideoDuration);

        initPhoneListener();
        initVideoProgressLayout();
        previewVideo();// 开始预览视频
        mKeyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
    }

    private void initPhoneListener() {
        //设置电话监听
        if (mPhoneListener == null) {
            mPhoneListener = new TXPhoneStateListener(this);
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private long getCutterStartTime() {
        return mCutterFragment != null ? ((TCCutterFragment) mCutterFragment).getCutterStartTime() : 0;
    }

    private long getCutterEndTime() {
        return mCutterFragment != null ? ((TCCutterFragment) mCutterFragment).getCutterEndTime() : 0;
    }

    /**
     * ==========================================SDK播放器生命周期==========================================
     */

    private void previewVideo() {
        showCutterFragment();
        initVideoProgressLayout();  // 初始化进度布局
        initPlayerLayout();         // 初始化预览视频布局
        startPlay(getCutterStartTime(), getCutterEndTime());  // 开始播放
    }


    private void initVideoProgressLayout() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        int screenWidth = point.x;
        mVideoProgressView = (VideoProgressView) findViewById(R.id.editer_video_progress_view);
        mVideoProgressView.setViewWidth(screenWidth);

        List<Bitmap> thumbnailList = TCVideoEditerWrapper.getInstance().getAllThumbnails();
        mVideoProgressView.setThumbnailData(thumbnailList);

        mVideoProgressController = new VideoProgressController(mVideoDuration);
        mVideoProgressController.setVideoProgressView(mVideoProgressView);
        mVideoProgressController.setVideoProgressSeekListener(mVideoProgressSeekListener);
        mVideoProgressController.setVideoProgressDisplayWidth(screenWidth);

    }

    public void switchReverse() {
        mVideoProgressView.setReverse();
    }

    private void initPlayerLayout() {
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = mVideoPlayerLayout;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;
        mTXVideoEditer.initWithPreview(param);
    }

    /**
     * 调用mTXVideoEditer.previewAtTime后，需要记录当前时间，下次播放时从当前时间开始
     * x
     *
     * @param timeMs
     */
    public void previewAtTime(long timeMs) {
        pausePlay();
        mTXVideoEditer.previewAtTime(timeMs);
        mPreviewAtTime = timeMs;
        mCurrentState = PlayState.STATE_PREVIEW_AT_TIME;
    }

    /**
     * 给子Fragment调用 （子Fragment不在意Activity中对于播放器的生命周期）
     */
    public void startPlayAccordingState(long startTime, long endTime) {
        if (mCurrentState == PlayState.STATE_STOP || mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
            startPlay(startTime, endTime);
        } else if (mCurrentState == PlayState.STATE_PAUSE) {
            resumePlay();
        }
    }

    /**
     * 给子Fragment调用 （子Fragment不在意Activity中对于播放器的生命周期）
     */
    public void restartPlay() {
        stopPlay();
        startPlay(getCutterStartTime(), getCutterEndTime());
    }

    public void startPlay(long startTime, long endTime) {
        mTXVideoEditer.startPlayFromTime(startTime, endTime);
        mCurrentState = PlayState.STATE_PLAY;
        ivPlay.setImageResource(R.drawable.iv_pause);
    }


    public void resumePlay() {
        if (mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoEditer.resumePlay();
            mCurrentState = PlayState.STATE_RESUME;
            ivPlay.setImageResource(R.drawable.iv_pause);

        }
    }

    public void pausePlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            mTXVideoEditer.pausePlay();
            mCurrentState = PlayState.STATE_PAUSE;
            ivPlay.setImageResource(R.drawable.iv_play);
        }
    }

    public void stopPlay() {
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY ||
                mCurrentState == PlayState.STATE_STOP || mCurrentState == PlayState.STATE_PAUSE) {
            mTXVideoEditer.stopPlay();
            mCurrentState = PlayState.STATE_STOP;
            ivPlay.setImageResource(R.drawable.iv_play);
        }
    }


    /**
     * ==========================================activity生命周期==========================================
     */

    @Override
    protected void onRestart() {
        super.onRestart();
        // 在oppo r9s上，锁屏后，按电源键进入解锁状态（屏保画面），也会走onRestart和onResume。因此做个保护
        if( !mKeyguardManager.inKeyguardRestrictedInputMode() ){
            initPlayerLayout();
//            startPlayAccordingState(getCutterStartTime(), getCutterEndTime());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( !mKeyguardManager.inKeyguardRestrictedInputMode() ){
            restartPlay();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlay();
        // 若当前处于生成状态，离开当前activity，直接停止生成
        if (mCurrentState == PlayState.STATE_GENERATE) {
            stopGenerate();
        }
    }

    protected void onStop() {
        super.onStop();
//        stopGenerate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhoneListener != null) {
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
        }
        if (mTXVideoEditer != null) {
            stopPlay();
            mTXVideoEditer.setVideoGenerateListener(null);
            mTXVideoEditer.release();
        }
        // 清除对TXVideoEditer的引用以及相关配置
        TCVideoEditerWrapper.getInstance().removeTXVideoPreviewListenerWrapper(this);
        TCVideoEditerWrapper.getInstance().clear();

        // 清空保存的气泡字幕参数 （避免下一个视频混入上一个视频的气泡设定
        TCBubbleViewInfoManager.getInstance().clear();
        // 清空保存的贴纸参数
        TCPasterViewInfoManager.getInstance().clear();
    }


    /**
     * ==========================================SDK回调==========================================
     */
    @Override // 预览进度回调
    public void onPreviewProgressWrapper(int timeMs) {
        // 视频的进度回调是异步的，如果不是处于播放状态，那么无需修改进度
        if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
            mVideoProgressController.setCurrentTimeMs(timeMs);
        }
    }

    @Override // 预览完成回调
    public void onPreviewFinishedWrapper() {
        stopPlay();
        if ((mMotionFragment != null && mMotionFragment.isAdded() && !mMotionFragment.isHidden()) ||
                (mTimeFragment != null && mTimeFragment.isAdded() && !mTimeFragment.isHidden())) {
            // 处于动态滤镜或者时间特效界面,忽略 不做任何操作
        } else {
            // 如果当前不是动态滤镜界面或者时间特效界面，那么会自动开始重复播放
            startPlay(getCutterStartTime(), getCutterEndTime());
        }
    }


    /**
     * 创建缩略图，并跳转至视频预览的Activity
     */
    private void createThumbFile(final TXVideoEditConstants.TXGenerateResult result) {
        startPreviewActivity(result);
//        AsyncTask<Void, String, String> task = new AsyncTask<Void, String, String>() {
//            @Override
//            protected String doInBackground(Void... voids) {
//                File outputVideo = new File(mVideoOutputPath);
//                if (!outputVideo.exists())
//                    return null;
//                Bitmap bitmap = TXVideoInfoReader.getInstance().getSampleImage(0, mVideoOutputPath);
//                if (bitmap == null)
//                    return null;
//                String mediaFileName = outputVideo.getAbsolutePath();
//                if (mediaFileName.lastIndexOf(".") != -1) {
//                    mediaFileName = mediaFileName.substring(0, mediaFileName.lastIndexOf("."));
//                }
//                String folder = Environment.getExternalStorageDirectory() + File.separator + TCConstants.DEFAULT_MEDIA_PACK_FOLDER + File.separator + mediaFileName;
//                File appDir = new File(folder);
//                if (!appDir.exists()) {
//                    appDir.mkdirs();
//                }
//
//                String fileName = "thumbnail" + ".jpg";
//                File file = new File(appDir, fileName);
//                try {
//                    FileOutputStream fos = new FileOutputStream(file);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                    fos.flush();
//                    fos.close();
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return file.getAbsolutePath();
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                startPreviewActivity(result, s);
//            }
//
//        };
//        task.execute();
    }

    private void startPreviewActivity(TXVideoEditConstants.TXGenerateResult result) {
//        Intent intent = new Intent(getApplicationContext(), DisplayVideoActivity.class);
//        intent.putExtra(TCConstants.VIDEO_RECORD_TYPE, TCConstants.VIDEO_RECORD_TYPE_EDIT);
//        intent.putExtra(TCConstants.VIDEO_RECORD_RESULT, result.retCode);
//        intent.putExtra(TCConstants.VIDEO_RECORD_DESCMSG, result.descMsg);
//        intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, mVideoOutputPath);
//        if (thumbPath != null)
//            intent.putExtra(TCConstants.VIDEO_RECORD_COVERPATH, thumbPath);
//        intent.putExtra(TCConstants.VIDEO_RECORD_DURATION, getCutterEndTime() - getCutterStartTime());
//        startActivity(intent);
//        finish();





        if (getIntent().hasExtra("isNeedEdit") && getIntent().getBooleanExtra("isNeedEdit", false) == false) {//是否需要编辑
            //发送广播，刷新视频编辑列表
            Intent intent = new Intent();
            intent.setAction("refresh_edit_list");
            intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, mVideoOutputPath);
            sendBroadcast(intent);
            finish();
        }else {
            Intent intent = new Intent(mContext, DisplayVideoActivity.class);
            intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, mVideoOutputPath);
            if (getIntent().hasExtra("appid")) {
                intent.putExtra("appid", "26");//活动专用频道
            }
            startActivity(intent);
        }

    }

    /**
     * ==========================================工具栏的点击回调==========================================
     */
    private void showCutterFragment() {
        if (mCutterFragment == null) {
            mCutterFragment = new TCCutterFragment();
        }
        showFragment(mCutterFragment, "cutter_fragment");
    }

    @Override
    public void onClickTime() {
        showTimeFragment();
    }

    @Override
    public void onClickCutter() {
        showCutterFragment();
    }

    private void showTimeFragment() {
        if (mTimeFragment == null) {
            mTimeFragment = new TCTimeFragment();
        }
        showFragment(mTimeFragment, "time_fragment");
    }

    @Override
    public void onClickStaticFilter() {
        if (mStaticFilterFragment == null) {
            mStaticFilterFragment = new TCStaticFilterFragment();
        }
        showFragment(mStaticFilterFragment, "static_filter_fragment");
    }

    @Override
    public void onClickMotionFilter() {
        if (mMotionFragment == null) {
            mMotionFragment = new TCMotionFragment();
        }
        showFragment(mMotionFragment, "motion_fragment");
    }

    @Override
    public void onClickBGM() {
        if (mBGMSettingFragment == null) {
            mBGMSettingFragment = new TCBGMSettingFragment();
        }
        showFragment(mBGMSettingFragment, "bgm_setting_fragment");
    }

    @Override
    public void onClickPaster() {
        stopPlay();
//        Intent intent = new Intent(this, TCPasterActivity.class);
//        startActivity(intent);
    }

    @Override
    public void onClickBubbleWord() {
        stopPlay();
//        Intent intent = new Intent(this, TCWordEditActivity.class);
//        startActivity(intent);
    }


    private void showFragment(Fragment fragment, String tag) {
        if (fragment == mCurrentFragment) return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }
        if (!fragment.isAdded()) {
            transaction.add(R.id.editer_fl_container, fragment, tag);
        } else {
            transaction.show(fragment);
        }
        mCurrentFragment = fragment;
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:// 返回
                finish();
                break;
            case R.id.tvControl:// 开始生成
                startGenerateVideo();
                break;
            case R.id.ivPlay:// 播放
                if (mCurrentState == PlayState.STATE_NONE || mCurrentState == PlayState.STATE_STOP) {
                    startPlay(getCutterStartTime(), getCutterEndTime());
                } else if (mCurrentState == PlayState.STATE_RESUME || mCurrentState == PlayState.STATE_PLAY) {
                    pausePlay();
                } else if (mCurrentState == PlayState.STATE_PAUSE) {
                    resumePlay();
                } else if (mCurrentState == PlayState.STATE_PREVIEW_AT_TIME) {
                    startPlay(mPreviewAtTime, getCutterEndTime());
                }
                break;
        }
    }


    /**
     * 生成编辑后输出视频路径
     *
     */
    public static String generateVideoPath() {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = sdf.format(new Date(currentTime));
        File outputFolder = new File(CONST.TRIMPATH);
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }
        String tempOutputPath = outputFolder.getPath() + File.separator + time + CONST.VIDEOTYPE;
        return tempOutputPath;
    }
    /**
     * =========================================视频生成相关==========================================
     */
    private void startGenerateVideo() {
        stopPlay(); // 停止播放

        // 处于生成状态
        mCurrentState = PlayState.STATE_GENERATE;
        // 防止
        tvControl.setEnabled(false);
        tvControl.setClickable(false);
        // 生成视频输出路径
        mVideoOutputPath = generateVideoPath();

        ivPlay.setImageResource(R.drawable.iv_play);

        showDialog();

        // 添加片尾水印
//        addTailWaterMark();

        mTXVideoEditer.setCutFromTime(getCutterStartTime(), getCutterEndTime());
        mTXVideoEditer.setVideoGenerateListener(this);
        mTXVideoEditer.generateVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mVideoOutputPath);
    }

    @Override // 生成进度回调
    public void onGenerateProgress(float progress) {
        if (mDialog != null) {
            mDialog.setPercent((int) (progress * 100));
        }
    }

    @Override // 生成完成
    public void onGenerateComplete(TXVideoEditConstants.TXGenerateResult result) {
        if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
            // 生成成功
            cancelDialog();
            createThumbFile(result);
        } else {
            Toast.makeText(VideoEditDetailActivity.this, result.descMsg, Toast.LENGTH_SHORT).show();
        }
        tvControl.setEnabled(true);
        tvControl.setClickable(true);
        mCurrentState = PlayState.STATE_NONE;
    }

    private void stopGenerate() {
        if (mCurrentState == PlayState.STATE_GENERATE) {
            tvControl.setEnabled(true);
            tvControl.setClickable(true);
            Toast.makeText(VideoEditDetailActivity.this, "取消视频生成", Toast.LENGTH_SHORT).show();
            cancelDialog();
            mCurrentState = PlayState.STATE_NONE;
            if (mTXVideoEditer != null) {
                mTXVideoEditer.cancel();
            }
        }
    }

    /**
     * 添加片尾水印
     */
    private void addTailWaterMark() {
        TXVideoEditConstants.TXVideoInfo info = TCVideoEditerWrapper.getInstance().getTXVideoInfo();

        Bitmap tailWaterMarkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.iv_logo);
        float widthHeightRatio = tailWaterMarkBitmap.getWidth() / (float) tailWaterMarkBitmap.getHeight();

        TXVideoEditConstants.TXRect txRect = new TXVideoEditConstants.TXRect();
        txRect.width = 0.25f; // 归一化的片尾水印，这里设置了一个固定值，水印占屏幕宽度的0.25。
        // 后面根据实际图片的宽高比，计算出对应缩放后的图片的宽度：txRect.width * videoInfo.width 和高度：txRect.width * videoInfo.width / widthHeightRatio，然后计算出水印放中间时的左上角位置
        txRect.x = (info.width - txRect.width * info.width) / (2f * info.width);
        txRect.y = (info.height - txRect.width * info.width / widthHeightRatio) / (2f * info.height);

        mTXVideoEditer.setTailWaterMark(tailWaterMarkBitmap, txRect, 3);
    }

    public VideoProgressController getVideoProgressViewController() {
        return mVideoProgressController;
    }


    /*********************************************监听电话状态**************************************************/
    static class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<VideoEditDetailActivity> mEditer;

        public TXPhoneStateListener(VideoEditDetailActivity editer) {
            mEditer = new WeakReference<VideoEditDetailActivity>(editer);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            VideoEditDetailActivity activity = mEditer.get();
            if (activity == null) return;
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:  //电话等待接听
                case TelephonyManager.CALL_STATE_OFFHOOK:  //电话接听
                    // 生成状态 取消生成
                    if (activity.mCurrentState == PlayState.STATE_GENERATE) {
                        activity.stopGenerate();
                    }
                    // 直接停止播放
                    activity.stopPlay();
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    // 重新开始播放
                    activity.restartPlay();
                    break;
            }
        }
    }

}

