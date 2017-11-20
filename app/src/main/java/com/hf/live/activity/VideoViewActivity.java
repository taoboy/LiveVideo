package com.hf.live.activity;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import android.widget.VideoView;
import com.hf.live.R;

public class VideoViewActivity extends Activity{
	
	private Context mContext = null;
	private VideoView videoView = null;
    private int currentPosition;
    private int duration;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.videoview);
		mContext = this;
		initVideoView();
	}
	
	private void initVideoView() {
		String url = getIntent().getStringExtra("url");
		if (TextUtils.isEmpty(url)) {
			Toast.makeText(mContext, "播放地址异常", Toast.LENGTH_SHORT).show();
			return;
		}
		videoView = (VideoView) findViewById(R.id.videoView);
//		MediaController mc = new MediaController(this);
//        mc.setAnchorView(videoView);
//        videoView.setMediaController(mc);
        videoView.setVideoPath(url);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer arg0) {
		        videoView.start();
//		        arg0.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
//					@Override
//					public void onBufferingUpdate(MediaPlayer arg0, int percent) {
//						// 获得当前播放时间和当前视频的长度
//						currentPosition = videoView.getCurrentPosition();
//						duration = videoView.getDuration(); 
//						int time = ((currentPosition * 100) / duration);
//						// 设置进度条的主要进度，表示当前的播放时间
//						SeekBar seekBar = new SeekBar(VideoViewActivity.this);
//						seekBar.setProgress(time);
//						// 设置进度条的次要进度，表示视频的缓冲进度
//						seekBar.setSecondaryProgress(percent);
//					}
//				});
			}
		});
	}
	
}
