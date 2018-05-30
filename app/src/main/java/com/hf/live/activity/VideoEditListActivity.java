package com.hf.live.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.adapter.VideoEditListAdapter;
import com.hf.live.common.CONST;
import com.hf.live.dto.PhotoDto;
import com.hf.live.qcloud.TCConstants;
import com.hf.live.view.MyDialog;
import com.hf.live.view.ScrollviewListview;
import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoJoiner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 视频编辑列表
 */

public class VideoEditListActivity extends BaseActivity implements View.OnClickListener, TXVideoJoiner.TXVideoPreviewListener, TXVideoJoiner.TXVideoJoinerListener{

    private Context mContext;
    private LinearLayout llBack;
    private TextView tvTitle, tvControl;
    private ScrollviewListview listView;
    private VideoEditListAdapter mAdapter;
    private List<PhotoDto> dataList = new ArrayList<>();
    private int width;
    private ImageView ivAdd,ivPreview;
    private FrameLayout frameLayout;
    private TXVideoJoiner mTXVideoJoiner;
    private String mVideoOutputPath;                        // 视频输出路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit_list);
        mContext = this;
        initBroadCast();
        initWidget();
        initListView();
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("视频剪辑");
        tvControl = (TextView) findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setText("合成");
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        ivAdd = (ImageView) findViewById(R.id.ivAdd);
        ivAdd.setOnClickListener(this);
        ivPreview = (ImageView) findViewById(R.id.ivPreview);
        ivPreview.setOnClickListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width*2/3, width*2/3*9/16);
        frameLayout.setLayoutParams(params);
    }

    private void initListView() {
        dataList.clear();
        dataList.addAll(getIntent().getExtras().<PhotoDto>getParcelableArrayList("dataList"));

        listView = (ScrollviewListview) findViewById(R.id.listView);
        mAdapter = new VideoEditListAdapter(mContext, dataList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < dataList.size(); i++) {
                    dataList.get(i).isEditing = false;
                }

                PhotoDto dto = dataList.get(position);
                dto.isEditing = true;

                Intent intent = new Intent(mContext, TCVideoPreprocessActivity.class);
                intent.putExtra(TCConstants.VIDEO_EDITER_PATH, dto.videoUrl);
                startActivity(intent);

            }
        });
    }

    /**
     * 预览合成后视频
     */
    private void previewVideo() {
        if (dataList.size() <= 1) {
            return;
        }

        List<String> videoPaths = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            videoPaths.add(dataList.get(i).videoUrl);
        }

        //准备预览 View
        TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
        param.videoView = frameLayout;
        param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;

        // 创建 TXUGCJoiner 对象并设置预览 view
        if (mTXVideoJoiner == null) {
            mTXVideoJoiner = new TXVideoJoiner(this);
            mTXVideoJoiner.setTXVideoPreviewListener(this);
        }
        mTXVideoJoiner.initWithPreview(param);
        // 设置待拼接的视频文件组 mVideoSourceList，也就是第一步中选择的若干个文件
        int ret = mTXVideoJoiner.setVideoPathList(videoPaths);
        if (ret == 0) {
            tvControl.setVisibility(View.VISIBLE);
            mTXVideoJoiner.startPlay();
        } else if (ret == TXVideoEditConstants.ERR_UNSUPPORT_VIDEO_FORMAT) {
            Toast.makeText(mContext, "视频合成失败，本机型暂不支持此视频格式", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPreviewProgress(int time) {
    }

    @Override
    public void onPreviewFinished() {
        if (mTXVideoJoiner != null) {
            mTXVideoJoiner.startPlay();
        }
    }

    /**
     * 生成合成后输出视频路径
     *
     */
    private String mergeVideoPath() {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = sdf.format(new Date(currentTime));
        File outputFolder = new File(CONST.MERGEPATH);
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }
        String tempOutputPath = outputFolder.getPath() + File.separator + time + CONST.VIDEOTYPE;
        return tempOutputPath;
    }

    @Override
    public void onJoinProgress(float progress) {
    }

    @Override
    public void onJoinComplete(TXVideoEditConstants.TXJoinerResult result) {
        cancelDialog();
        if (result.retCode == TXVideoEditConstants.JOIN_RESULT_OK) {
            Intent intent = new Intent(mContext, DisplayVideoActivity.class);
            intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, mVideoOutputPath);
            startActivity(intent);
        } else {
            TXVideoEditConstants.TXJoinerResult ret = result;
            Toast.makeText(mContext, "视频合成失败"+result.descMsg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTXVideoJoiner != null) {
            mTXVideoJoiner.setTXVideoPreviewListener(null);
            mTXVideoJoiner.setVideoJoinerListener(null);
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_OK);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.ivAdd:
                startActivityForResult(new Intent(mContext, VideoSelectEditActivity.class), 1001);
                break;
            case R.id.ivPreview:
                previewVideo();
                break;
            case R.id.tvControl:
                if (mTXVideoJoiner != null) {
                    showDialog();
                    mVideoOutputPath = mergeVideoPath();
                    mTXVideoJoiner.setVideoJoinerListener(this);
                    mTXVideoJoiner.joinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mVideoOutputPath);
                }
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
                        dataList.addAll(data.getExtras().<PhotoDto>getParcelableArrayList("dataList"));
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
            }
        }
    }


    //刷新视频编辑列表
    private MyBroadCastReceiver mReceiver;

    private void initBroadCast() {
        mReceiver = new MyBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("refresh_edit_list");
        registerReceiver(mReceiver, intentFilter);
    }

    private class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String videoPath = intent.getStringExtra(TCConstants.VIDEO_RECORD_VIDEPATH);
                if (!TextUtils.isEmpty(videoPath)) {
                    for (int i = 0; i < dataList.size(); i++) {
                        PhotoDto dto = dataList.get(i);
                        if (dto.isEditing) {
                            dto.videoUrl = videoPath;
                            break;
                        }
                    }
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

}
