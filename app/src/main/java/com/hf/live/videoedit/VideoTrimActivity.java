package com.hf.live.videoedit;

/**
 * 视频剪辑
 */

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.hf.live.R;
import com.hf.live.activity.BaseActivity;
import com.hf.live.activity.DisplayVideoActivity;
import com.hf.live.adapter.SelectVideoAdapter;
import com.hf.live.adapter.VideoTrimAdapter;
import com.hf.live.common.CONST;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.DataCleanManager;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTrimmer;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

public class VideoTrimActivity extends BaseActivity  implements View.OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private TextView tvControl = null;
    private static final int SLICE_COUNT = 10;//视频显示帧数
    private PLShortVideoTrimmer mShortVideoTrimmer;
    private LinearLayout llContainer;//装载视频帧数容器
    private View mHandlerLeft;
    private View mHandlerRight;
    private CustomProgressDialog mProcessingDialog;
    private VideoView videoView;//视频预览
    private TextView tvStartTime, tvEndTime, tvRangeTime;
    private long mSelectedBeginMs = 100, mSelectedEndMs;//选择裁剪视频的开始、结束时间
    private long mDurationMs;//视频时长
    private int mVideoFrameCount;
    private int mSlicesTotalLength;

    private GridView gridView;
    private VideoTrimAdapter adapter = null;
    private List<PhotoDto> selectList = new ArrayList<>();
    private int width = 0;
    private TextView tvTrim = null;
    private GridView gridView2;
    private VideoTrimAdapter adapter2 = null;
    private List<PhotoDto> selectList2 = new ArrayList<>();
    private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
    private List<EpVideo> mergeList = new ArrayList<>();//合并视频列表
    private final int MSG_SHOWTRIM = 1001;//显示裁剪部分布局

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim);
        mContext = this;
        initDialog();
        initWidget();
        initGridView();
        initGridView2();
    }

    private void initDialog() {
        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoTrimmer.cancelTrim();
            }
        });
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("视频编辑");
        tvControl = (TextView) findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setVisibility(View.VISIBLE);
        tvControl.setText("下一步");
        tvStartTime = (TextView) findViewById(R.id.tvStartTime);
        tvStartTime.setText("00:00");
        tvEndTime = (TextView) findViewById(R.id.tvEndTime);
        tvRangeTime = (TextView) findViewById(R.id.tvRangeTime);
        llContainer = (LinearLayout) findViewById(R.id.llContainer);
        mHandlerLeft = findViewById(R.id.handler_left);
        mHandlerRight = findViewById(R.id.handler_right);
        videoView = (VideoView) findViewById(R.id.videoView);
        tvTrim = (TextView) findViewById(R.id.tvTrim);
        tvTrim.setOnClickListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
    }

    private void initGridView() {
        selectList.clear();
        selectList.addAll(getIntent().getExtras().<PhotoDto>getParcelableArrayList("selectList"));
        for (int i = 0; i < selectList.size(); i++) {
            PhotoDto data = selectList.get(i);
            if (i == 0) {
                data.isSelected = true;
            }else {
                data.isSelected = false;
            }
        }

        gridView = (GridView) findViewById(R.id.gridView);
        adapter = new VideoTrimAdapter(mContext, selectList, width/5);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < selectList.size(); i++) {
                    PhotoDto dto = selectList.get(i);
                    if (i == position) {
                        dto.isSelected = true;
                    }else {
                        dto.isSelected = false;
                    }
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                tvStartTime.setVisibility(View.INVISIBLE);
                tvRangeTime.setVisibility(View.INVISIBLE);
                tvEndTime.setVisibility(View.INVISIBLE);
                mHandlerLeft.setVisibility(View.GONE);
                mHandlerRight.setVisibility(View.GONE);
                tvTrim.setVisibility(View.INVISIBLE);

                initVideoTrim(selectList.get(position).videoUrl);
            }
        });

        String videoUrl = selectList.get(0).videoUrl;
        initVideoTrim(videoUrl);
    }

    private void initVideoTrim(String videoPath) {
        if (TextUtils.isEmpty(videoPath)) {
            return;
        }
        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        retr.setDataSource(videoPath);
        String videoWidth = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
        String videoHeight = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
        String videoRotation = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION); // 视频旋转
        int videoW = Integer.valueOf(videoWidth);
        int videoH = Integer.valueOf(videoHeight);
        int videoR = Integer.valueOf(videoRotation);
        if (videoR != 0) {
            videoView.setLayoutParams(new LinearLayout.LayoutParams(videoH*CONST.standarH/videoW, CONST.standarH));
        }else {
            videoView.setLayoutParams(new LinearLayout.LayoutParams(videoW*CONST.standarH/videoH, CONST.standarH));
        }

        if (mShortVideoTrimmer != null) {
            mShortVideoTrimmer.destroy();
            mShortVideoTrimmer = null;
        }
        mShortVideoTrimmer = new PLShortVideoTrimmer(mContext, videoPath, CONST.TRIMPATH+"/"+sdf1.format(new Date())+".mp4");
        mVideoFrameCount = mShortVideoTrimmer.getVideoFrameCount(false);
        mSelectedEndMs = mDurationMs = mShortVideoTrimmer.getSrcDurationMs();
        tvEndTime.setText(formatTime(mDurationMs));

        videoView.setVideoPath(videoPath);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                play();
            }
        });

        initVideoFrameList();
    }

    private void initVideoFrameList() {
        llContainer.removeAllViews();
        llContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                llContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final int sliceEdge = llContainer.getWidth() / SLICE_COUNT;
                mSlicesTotalLength = sliceEdge * SLICE_COUNT;
                final float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

                new AsyncTask<Void, PLVideoFrame, Void>() {
                    @Override
                    protected Void doInBackground(Void... v) {
                        int i = 0;
                        for (i = 0; i < SLICE_COUNT; ++i) {
                            PLVideoFrame frame = mShortVideoTrimmer.getVideoFrameByTime((long) ((1.0f * i / SLICE_COUNT) * mDurationMs), false, sliceEdge, sliceEdge);
                            publishProgress(frame);
                        }
                        if (i == SLICE_COUNT) {
                            handler.removeMessages(MSG_SHOWTRIM);
                            Message msg = handler.obtainMessage();
                            msg.what = MSG_SHOWTRIM;
                            handler.sendMessage(msg);
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(PLVideoFrame... values) {
                        super.onProgressUpdate(values);
                        PLVideoFrame frame = values[0];
                        if (frame != null) {
                            //视频每一帧
                            View root = LayoutInflater.from(VideoTrimActivity.this).inflate(R.layout.layout_video_frame, null);
                            int rotation = frame.getRotation();
                            ImageView ivFrame = (ImageView) root.findViewById(R.id.ivFrame);
                            ivFrame.setImageBitmap(frame.toBitmap());
                            ivFrame.setRotation(rotation);
                            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) ivFrame.getLayoutParams();
                            if (rotation == 90 || rotation == 270) {
                                layoutParams.leftMargin = layoutParams.rightMargin = (int) px;
                            } else {
                                layoutParams.topMargin = layoutParams.bottomMargin = (int) px;
                            }
                            ivFrame.setLayoutParams(layoutParams);

                            LinearLayout.LayoutParams rootLP = new LinearLayout.LayoutParams(sliceEdge, 40*(int)px);
                            llContainer.addView(root, rootLP);
                        }
                    }
                }.execute();
            }
        });

    }

    /**
     * 显示裁剪部分布局
     */
    private void showTrimLayout() {
        updateHandlerLeftPosition(0);
        updateHandlerRightPosition(llContainer.getWidth());
        mSelectedBeginMs = 100;
        mSelectedEndMs = mDurationMs = mShortVideoTrimmer.getSrcDurationMs();
        updateRangeTime();

        tvStartTime.setVisibility(View.VISIBLE);
        tvRangeTime.setVisibility(View.VISIBLE);
        tvEndTime.setVisibility(View.VISIBLE);
        mHandlerLeft.setVisibility(View.VISIBLE);
        mHandlerRight.setVisibility(View.VISIBLE);
        tvTrim.setVisibility(View.VISIBLE);

        mHandlerLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float viewX = v.getX();
                float movedX = event.getX();
                float finalX = viewX + movedX;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        updateHandlerLeftPosition(finalX);
                        calculateRange();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }

                return true;
            }
        });

        mHandlerRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float viewX = v.getX();
                float movedX = event.getX();
                float finalX = viewX + movedX;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        updateHandlerRightPosition(finalX);
                        calculateRange();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }

                return true;
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SHOWTRIM:
                    showTrimLayout();
                    break;
            }
        }
    };

    private Handler mHandler = new Handler();
    private void startTrackPlayProgress() {
        stopTrackPlayProgress();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (videoView.getCurrentPosition() >= mSelectedEndMs) {
                    videoView.seekTo((int) mSelectedBeginMs);
                }
                mHandler.postDelayed(this, 100);
            }
        }, 100);
    }

    private void stopTrackPlayProgress() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private void play() {
        if (videoView != null) {
            videoView.seekTo((int) mSelectedBeginMs);
            videoView.start();
            startTrackPlayProgress();
        }
    }

    private void updateHandlerLeftPosition(float movedPosition) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHandlerLeft.getLayoutParams();
        if ((movedPosition + mHandlerLeft.getWidth()) > mHandlerRight.getX()) {
            lp.leftMargin = (int) (mHandlerRight.getX() - mHandlerLeft.getWidth());
        } else if (movedPosition < 0) {
            lp.leftMargin = 0;
        } else {
            lp.leftMargin = (int) movedPosition;
        }
        mHandlerLeft.setLayoutParams(lp);
    }

    private void updateHandlerRightPosition(float movedPosition) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHandlerRight.getLayoutParams();
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (movedPosition < (mHandlerLeft.getX() + mHandlerLeft.getWidth())) {
            lp.leftMargin = (int) (mHandlerLeft.getX() + mHandlerLeft.getWidth());
        } else if ((movedPosition + (mHandlerRight.getWidth() / 2)) > (llContainer.getX() + mSlicesTotalLength)) {
            lp.leftMargin = (int) ((llContainer.getX() + mSlicesTotalLength) - (mHandlerRight.getWidth() / 2));
        } else {
            lp.leftMargin = (int) movedPosition;
        }
        mHandlerRight.setLayoutParams(lp);
    }

    private float clamp(float origin) {
        if (origin < 0) {
            return 0;
        }
        if (origin > 1) {
            return 1;
        }
        return origin;
    }

    private void calculateRange() {
        float beginPercent = 1.0f * ((mHandlerLeft.getX() + mHandlerLeft.getWidth() / 2) - llContainer.getX()) / mSlicesTotalLength;
        float endPercent = 1.0f * ((mHandlerRight.getX() + mHandlerRight.getWidth() / 2) - llContainer.getX()) / mSlicesTotalLength;
        beginPercent = clamp(beginPercent);
        endPercent = clamp(endPercent);

        mSelectedBeginMs = (long) (beginPercent * mDurationMs);
        mSelectedEndMs = (long) (endPercent * mDurationMs);

        updateRangeTime();
        play();
    }

    /**
     * 剪辑单个视频
     */
    private void trimVideo() {
        mProcessingDialog.show();
        mShortVideoTrimmer.trim(mSelectedBeginMs, mSelectedEndMs, new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(final String path) {
                mProcessingDialog.dismiss();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                PhotoDto dto = new PhotoDto();
                                dto.videoUrl = path;
                                dto.isSelected = false;
                                selectList2.add(dto);
                                if (adapter2 != null) {
                                    adapter2.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onSaveVideoFailed(int errorCode) {
                mProcessingDialog.dismiss();
            }

            @Override
            public void onSaveVideoCanceled() {
                mProcessingDialog.dismiss();
            }

            @Override
            public void onProgressUpdate(float percentage) {
                mProcessingDialog.setProgress((int) (100 * percentage));
            }
        });
    }

    private String formatTime(long timeMs) {
        return String.format(Locale.CHINA, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeMs),
                TimeUnit.MILLISECONDS.toSeconds(timeMs) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMs))
        );
    }

    /**
     * 更新剪辑视频开始、结束时间
     */
    private void updateRangeTime() {
        tvRangeTime.setText(formatTime(mSelectedBeginMs) + " - " + formatTime(mSelectedEndMs));
    }

    private void initGridView2() {
        gridView2 = (GridView) findViewById(R.id.gridView2);
        adapter2 = new VideoTrimAdapter(mContext, selectList2, width/5);
        gridView2.setAdapter(adapter2);
        gridView2.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoDto dto = selectList2.get(position);
                if (!TextUtils.isEmpty(dto.videoUrl)) {
                    deleteDialog("确定删除？", dto.videoUrl, position);
                }
                return true;
            }
        });
    }

    /**
     * 删除对话框
     * @param message 标题
     */
    private void deleteDialog(String message, final String videoUrl, final int position) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.delete_dialog, null);
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
        LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

        final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
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
                File file = new File(videoUrl);
                if (file.exists()) {
                    file.delete();
                }
                selectList2.remove(position);
                if (adapter2 != null) {
                    adapter2.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * 合并视频
     */
    private void mergeVideo() {
        if (mergeList.size() > 1) {
            mProcessingDialog.show();
            File file = new File(CONST.SAVEPATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            final String videoUrl = CONST.SAVEPATH + "/"+sdf1.format(new Date())+"merge.mp4";
            new MyEpEditor(this).merge(mergeList, new MyEpEditor.OutputOption(videoUrl), new OnEditorListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(mContext, "编辑完成", Toast.LENGTH_SHORT).show();
                    mProcessingDialog.dismiss();
                    PhotoDto data = new PhotoDto();
                    data.workstype = "video";
                    data.workTime = sdf1.format(System.currentTimeMillis());
                    data.videoUrl = videoUrl;
                    Intent intent = new Intent(mContext, DisplayVideoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("data", data);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

                @Override
                public void onFailure() {
                    Toast.makeText(mContext, "编辑失败", Toast.LENGTH_SHORT).show();
                    mProcessingDialog.dismiss();
                }

                @Override
                public void onProgress(float v) {
                    mProcessingDialog.setProgress((int) (v * 100));
                }

            });
        } else {
            Toast.makeText(this, "至少添加两个视频", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvTrim:
                trimVideo();
                break;
            case R.id.tvControl:
                int size = selectList2.size();
                if (size <= 0) {//没有经过编辑，点击"下一步"默认上传预览的那条视频
                    String videoUrl = selectList.get(0).videoUrl;
                    for (int i = 0; i < selectList.size(); i++) {
                        if (selectList.get(i).isSelected) {
                            videoUrl = selectList.get(i).videoUrl;
                        }
                    }

                    PhotoDto data = new PhotoDto();
                    data.workstype = "video";
                    data.workTime = sdf1.format(System.currentTimeMillis());
                    data.videoUrl = videoUrl;
                    Intent intent = new Intent(mContext, DisplayVideoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("data", data);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else if (size == 1) {//编辑状态下，只有一条视频
                    PhotoDto data = new PhotoDto();
                    data.workstype = "video";
                    data.workTime = sdf1.format(System.currentTimeMillis());
                    data.videoUrl = selectList2.get(0).videoUrl;
                    Intent intent = new Intent(mContext, DisplayVideoActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("data", data);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else if (size > 1) {//编辑状态下，有两条活以上视频，需要合并后才能上传
                    mergeList.clear();
                    for (int i = 0; i < selectList2.size(); i++) {
                        PhotoDto dto = selectList2.get(i);
                        mergeList.add(new EpVideo(dto.videoUrl));
                    }
                    mergeVideo();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTrackPlayProgress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mShortVideoTrimmer != null) {
            mShortVideoTrimmer.destroy();
        }
    }

}
