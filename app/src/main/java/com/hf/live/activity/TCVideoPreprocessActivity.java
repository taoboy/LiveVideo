package com.hf.live.activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.qcloud.TCConstants;
import com.hf.live.qcloud.TCVideoEditerWrapper;
import com.hf.live.view.MyDialog;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoEditer;
import com.tencent.ugc.TXVideoInfoReader;

import java.lang.ref.WeakReference;


/**
 * Created by hans on 2017/11/7.
 * 对进入编辑的视频进行一步预处理的Activity
 */
public class TCVideoPreprocessActivity extends FragmentActivity implements TXVideoEditer.TXVideoProcessListener {

    private Context mContext;
    private String videoPath;                                // 编辑的视频源路径
    private TXVideoEditer mTXVideoEditer;                       // SDK接口类
    private VideoMainHandler mVideoMainHandler;                 // 加载完信息后的回调主线程Hanlder
    private Thread mLoadBackgroundThread;                       // 后台加载视频信息的线程
    private boolean isSuccess = false;// 是否预处理成功
    private MyDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_processor);
        mContext = this;
        showDialog();
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
        videoPath = getIntent().getStringExtra(TCConstants.VIDEO_EDITER_PATH);
        TCVideoEditerWrapper.getInstance().clear();
        mTXVideoEditer = new TXVideoEditer(this);
        mTXVideoEditer.setVideoPath(videoPath);
        TCVideoEditerWrapper wrapper = TCVideoEditerWrapper.getInstance();
        wrapper.setEditer(mTXVideoEditer);

        initPhoneListener();

        // 开始加载视频信息
        mVideoMainHandler = new VideoMainHandler(this);
        mLoadBackgroundThread = new Thread(new LoadVideoRunnable(this));
        mLoadBackgroundThread.start();
    }

    /**
     * ===========================================加载视频相关 ===========================================
     */

    /**
     * 加在视频信息的runnable
     */
    private static class LoadVideoRunnable implements Runnable {
        private WeakReference<TCVideoPreprocessActivity> mWekActivity;

        LoadVideoRunnable(TCVideoPreprocessActivity activity) {
            mWekActivity = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            if (mWekActivity == null || mWekActivity.get() == null) {
                return;
            }
            TCVideoPreprocessActivity activity = mWekActivity.get();
            if (activity == null) return;
            TXVideoEditConstants.TXVideoInfo info = TXVideoInfoReader.getInstance().getVideoFileInfo(activity.videoPath);

            if (info == null) {// error 发生错误
                activity.mVideoMainHandler.sendEmptyMessage(VideoMainHandler.LOAD_VIDEO_ERROR);
            } else {
                activity.mVideoMainHandler.sendEmptyMessage(VideoMainHandler.LOAD_VIDEO_SUCCESS);
            }
        }
    }

    /**
     * 主线程的Handler 用于处理load 视频信息的完后的动作
     */
    private static class VideoMainHandler extends Handler {
        static final int LOAD_VIDEO_SUCCESS = 0;
        static final int LOAD_VIDEO_ERROR = -1;
        private WeakReference<TCVideoPreprocessActivity> mWefActivity;


        VideoMainHandler(TCVideoPreprocessActivity activity) {
            mWefActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            TCVideoPreprocessActivity activity = mWefActivity.get();
            if (activity == null) return;
            switch (msg.what) {
                case LOAD_VIDEO_ERROR:
                    Toast.makeText(activity, "编辑失败，暂不支持Android 4.3以下的系统", Toast.LENGTH_SHORT).show();
                    break;
                case LOAD_VIDEO_SUCCESS:
                    activity.startProcess();
                    break;
            }
        }
    }

    /**
     * ===========================================开始预处理相关 ===========================================
     */
    private void startProcess() {
        mTXVideoEditer.setVideoProcessListener(this);

        int thumbnailCount = (int) mTXVideoEditer.getTXVideoInfo().duration / 1000;

        TXVideoEditConstants.TXThumbnail thumbnail = new TXVideoEditConstants.TXThumbnail();
        thumbnail.count = thumbnailCount;
        thumbnail.width = 100;
        thumbnail.height = 100;

        mTXVideoEditer.setThumbnail(thumbnail);
        mTXVideoEditer.setThumbnailListener(mThumbnailListener);
        mTXVideoEditer.processVideo();
    }

    private TXVideoEditer.TXThumbnailListener mThumbnailListener = new TXVideoEditer.TXThumbnailListener() {
        @Override

        public void onThumbnail(int index, long timeMs, Bitmap bitmap) {
            TCVideoEditerWrapper.getInstance().addThumbnailBitmap(timeMs, bitmap);
        }
    };

    @Override
    public void onProcessProgress(float progress) {
        if (mDialog != null) {
            mDialog.setPercent((int) (progress * 100));
        }
    }

    @Override
    public void onProcessComplete(TXVideoEditConstants.TXGenerateResult result) {
        if (result.retCode == TXVideoEditConstants.GENERATE_RESULT_OK) {
            cancelDialog();
            isSuccess = true;
            Intent intent = new Intent(this, VideoEditDetailActivity.class);
            intent.putExtra(TCConstants.VIDEO_EDITER_PATH, videoPath);
            if (getIntent().hasExtra("isNeedEdit")) {//是否需要编辑
                boolean isNeedEdit = getIntent().getBooleanExtra("isNeedEdit", false);
                intent.putExtra("isNeedEdit", isNeedEdit);
            }
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(mContext, "预处理失败", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }

    /**
     * 取消预处理视频
     */
    private void cancelProcessVideo() {
        if (!isSuccess) {
            TCVideoEditerWrapper.getInstance().clear();
            Toast.makeText(mContext, "取消预处理", Toast.LENGTH_SHORT).show();
            if (mTXVideoEditer != null)
                mTXVideoEditer.cancel();
            setResult(RESULT_OK);
            finish();
        }
    }

    /*********************************************监听电话状态**************************************************/
    private void initPhoneListener() {
        //设置电话监听
        if (mPhoneListener == null) {
            mPhoneListener = new TXPhoneStateListener(this);
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private TXPhoneStateListener mPhoneListener;

    static class TXPhoneStateListener extends PhoneStateListener {
        WeakReference<TCVideoPreprocessActivity> mWefActivity;

        public TXPhoneStateListener(TCVideoPreprocessActivity activity) {
            mWefActivity = new WeakReference<>(activity);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            TCVideoPreprocessActivity activity = mWefActivity.get();
            if (activity == null) return;
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:  //电话等待接听
                case TelephonyManager.CALL_STATE_OFFHOOK:  //电话接听
                    // 直接停止播放
                    activity.cancelProcessVideo();
                    break;
                //电话挂机
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhoneListener != null) {
            TelephonyManager tm = (TelephonyManager) this.getApplicationContext().getSystemService(Service.TELEPHONY_SERVICE);
            tm.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
        }
        if (mLoadBackgroundThread != null && !mLoadBackgroundThread.isInterrupted() && mLoadBackgroundThread.isAlive()) {
            mLoadBackgroundThread.interrupt();
            mLoadBackgroundThread = null;
        }
    }

    protected void onStop() {
        super.onStop();
        cancelProcessVideo();
    }

}
