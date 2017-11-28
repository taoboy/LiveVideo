package com.hf.live.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.adapter.VideoAdapter;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;
import com.hf.live.util.EmojiMapUtil;
import com.hf.live.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * 在线预览视频
 */

@SuppressLint("SimpleDateFormat")
public class OnlineVideoActivity extends Activity implements SurfaceHolder.Callback, OnPreparedListener, OnVideoSizeChangedListener, OnCompletionListener, OnClickListener{
	
	private Context mContext = null;
	private SurfaceView surfaceView = null;
	private SurfaceHolder surfaceHolder = null;
	private MediaPlayer mPlayer = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
	private Timer timer = null;
	private int displayW = 0;//屏幕宽
	private int displayH = 0;//屏幕高
	private PhotoDto data = null;
	private ListView mListView = null;
	private VideoAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int page = 1;
	private int pageSize = 1000;
	private boolean praiseState = false;//点赞状态
	private Configuration configuration = null;//方向监听器
	private ProgressBar progressBar = null;
//	private int position = 0;
	private static final int HANDLER_PROCESS = 0;
	private static final int HANDLER_VISIBILITY = 1;
	private long delayTime = 5000;//延迟时间
	private boolean executeOnce = true;//只执行一次
	private LinearLayout llSurfaceView = null;
	private TextView tvWeatherFlag, tvOtherFlag;
	
	//竖屏布局
	private TextView tvPositon = null;//地址
	private TextView tvDate = null;//日期
	private TextView tvTitle = null;//标题
	private TextView tvContent = null;//内容
	private TextView tvCommentCount = null;//评论次数
	private LinearLayout reOperate = null;
	private LinearLayout llSubmit = null;
	private EditText etComment = null;
	private TextView tvSubmit = null;
	private ImageView ivComment = null;//评论
	private ImageView ivPraise = null;//点赞
	private ImageView ivShare = null;//分享
	private ImageView ivDownload = null;//下载
	
	//横屏布局
	private ImageView ivBackLand = null;//返回按钮
	private ImageView ivPlayLand = null;//播放按钮
	private TextView tvStartTimeLand = null;//开始时间
	private TextView tvEndTimeLand = null;//结束时间
	private SeekBar seekBarLand = null;//进度条
	private ImageView ivInFull = null;//全屏按钮
	private RelativeLayout reTop = null;//屏幕上方区域
	private RelativeLayout reBottom = null;//屏幕下方区域
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_online_video);
		mContext = this;
		initWidget();
		initListView();
	}
	
	private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		configuration = newConfig;
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			showPort();
			fullScreen(false);
		}else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			showLand();
			fullScreen(true);
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		//竖屏布局
		tvPositon = (TextView) findViewById(R.id.tvPosition);
		tvDate = (TextView) findViewById(R.id.tvDate);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvContent = (TextView) findViewById(R.id.tvContent);
		tvCommentCount = (TextView) findViewById(R.id.tvCommentCount);
		reOperate = (LinearLayout) findViewById(R.id.reOperate);
		ivComment = (ImageView) findViewById(R.id.ivComment);
		ivComment.setOnClickListener(this);
		ivPraise = (ImageView) findViewById(R.id.ivPraise);
		ivPraise.setOnClickListener(this);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		ivDownload = (ImageView) findViewById(R.id.ivDownload);
		ivDownload.setOnClickListener(this);
		llSubmit = (LinearLayout) findViewById(R.id.llSubmit);
		etComment = (EditText) findViewById(R.id.etComment);
		tvSubmit = (TextView) findViewById(R.id.tvSubmit);
		tvSubmit.setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		tvWeatherFlag = (TextView) findViewById(R.id.tvWeatherFlag);
		tvOtherFlag = (TextView) findViewById(R.id.tvOtherFlag);
		
		//横屏布局
		ivBackLand = (ImageView) findViewById(R.id.ivBackLand);
		ivBackLand.setOnClickListener(this);
		ivPlayLand = (ImageView) findViewById(R.id.ivPlayLand);
		ivPlayLand.setOnClickListener(this);
		seekBarLand = (SeekBar) findViewById(R.id.seekBarLand);
		seekBarLand.setOnTouchListener(seekbarListener);
		tvStartTimeLand = (TextView) findViewById(R.id.tvStartTimeLand);
		tvStartTimeLand.setText("00:00");
		tvEndTimeLand = (TextView) findViewById(R.id.tvEndTimeLand);
		ivInFull = (ImageView) findViewById(R.id.ivInFull);
		ivInFull.setOnClickListener(this);
		reTop = (RelativeLayout) findViewById(R.id.reTop);
		reBottom = (RelativeLayout) findViewById(R.id.reBottom);
		llSurfaceView = (LinearLayout) findViewById(R.id.llSurfaceView);
		llSurfaceView.setOnClickListener(this);
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				initSurfaceView();
				
				tvPositon.setText(data.getLocation());
				tvTitle.setText(data.getTitle());
				if (!TextUtils.isEmpty(data.content)) {
					tvContent.setText(data.content);
					tvContent.setVisibility(View.VISIBLE);
				}
				tvCommentCount.setText(getString(R.string.comment) + "（"+data.getCommentCount()+"）");
				tvDate.setText(data.getWorkTime());

				String weatherFlag = CommonUtil.getWeatherFlag(data.weatherFlag);
				if (!TextUtils.isEmpty(weatherFlag)) {
					tvWeatherFlag.setText(weatherFlag);
					tvWeatherFlag.setBackgroundResource(R.drawable.corner_flag);
					tvWeatherFlag.setVisibility(View.VISIBLE);
				}
				String otherFlag = CommonUtil.getWeatherFlag(data.otherFlag);
				if (!TextUtils.isEmpty(otherFlag)) {
					tvOtherFlag.setText(otherFlag);
					tvOtherFlag.setBackgroundResource(R.drawable.corner_flag);
					tvOtherFlag.setVisibility(View.VISIBLE);
				}
				
				//获取点赞状态
				SharedPreferences sharedPreferences = getSharedPreferences(data.getVideoId(), Context.MODE_PRIVATE);
				if (sharedPreferences.getBoolean("praiseState", false)) {
					praiseState = true;
					ivPraise.setImageResource(R.drawable.iv_like);
				}else {
					praiseState = false;
					ivPraise.setImageResource(R.drawable.iv_unlike);
				}

				//获取评论列表
				OkHttpCommentList(CONST.GET_WORK_COMMENT_URL);
			}
		}
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new VideoAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
	}
	
	/**
	 * 禁止seekbar监听事件
	 */
	private OnTouchListener seekbarListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			return true;
		}
	};
	
	/**
	 * 显示竖屏，隐藏横屏
	 */
	private void showPort() {
		reOperate.setVisibility(View.VISIBLE);
		ivInFull.setImageResource(R.drawable.iv_out_full);
		changeVideo(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
	}
	
	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
		reOperate.setVisibility(View.GONE);
		ivInFull.setImageResource(R.drawable.iv_in_full);
		changeVideo(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
	}
	
	/**
	 * 初始化surfaceView
	 */
	@SuppressWarnings("deprecation")
	private void initSurfaceView() {
		surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm);
		displayW = dm.widthPixels;
		displayH = dm.heightPixels;
		
		surfaceView.setLayoutParams(new LinearLayout.LayoutParams(CONST.standarH, CONST.standarH));
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceHolder = holder;
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mPlayer.setDisplay(holder);
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnVideoSizeChangedListener(this);
		mPlayer.setOnCompletionListener(this);
        //设置显示视频显示在SurfaceView上
        try {
        	if (data.getVideoUrl() != null) {
            	mPlayer.setDataSource(data.getVideoUrl());
            	mPlayer.prepareAsync();
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2, int arg3) {
		surfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceHolder = holder;
		releaseTimer();
        releaseMediaPlayer();
	}
	
	@Override
	public void onPrepared(MediaPlayer player) {
		tvStartTimeLand.setText(sdf.format(player.getCurrentPosition()));
		tvEndTimeLand.setText(sdf.format(player.getDuration()));

		seekBarLand.setProgress(0);
		seekBarLand.setMax(player.getDuration()/1000);
		
    	startPlayVideo();
	}
	
	/**
	 * 开始播放视频
	 */
	private void startPlayVideo() {
		if (mPlayer != null) {
			if (mPlayer.isPlaying()) {
				ivPlayLand.setImageResource(R.drawable.iv_play);
				mPlayer.pause();
				releaseTimer();
			}else {
				ivPlayLand.setImageResource(R.drawable.iv_pause);
				mPlayer.start();
				
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if(mPlayer == null) {
							return;
						}
				        if (mPlayer.isPlaying() && seekBarLand.isPressed() == false) {  
				        	handler.sendEmptyMessage(HANDLER_PROCESS);  
				        }  
					}
				}, 0, 1000);
			}
		}
	}
	
	private Handler handler = new Handler() {  
	    public void handleMessage(Message msg) {  
	    	switch (msg.what) {
			case HANDLER_PROCESS:
				if (mPlayer != null) {
		    		int position = mPlayer.getCurrentPosition();  
			        int duration = mPlayer.getDuration();  
			        
			        if (position > 0) {
						progressBar.setVisibility(View.GONE);
						if (executeOnce) {
							dismissColunm();
						}
					}
			          
			        if (duration > 0) {  
			            long posLand = seekBarLand.getMax() * position / duration;  
			            seekBarLand.setProgress((int) posLand);  
			            tvStartTimeLand.setText(sdf.format(position));
			        }  
				}
				break;
			case HANDLER_VISIBILITY:
				reTop.setVisibility(View.GONE);
				reBottom.setVisibility(View.GONE);
				ivPlayLand.setVisibility(View.GONE);
				break;

			default:
				break;
			}
	    	
	    };  
	};  
	
	/**
	 * 启动线程,隐藏操作栏
	 */
	private void dismissColunm() {
		handler.removeMessages(HANDLER_VISIBILITY);
		Message msg = new Message();
		msg.what = HANDLER_VISIBILITY;
		handler.sendMessageDelayed(msg, delayTime);
		executeOnce = false;
	}
	
	/**
	 * 改变横竖屏切换是视频的比例
	 * @param videoW
	 * @param videoH
	 */
	private void changeVideo(int videoW, int videoH) {
		if (surfaceView != null) {
			if (mPlayer != null) {
				int standarH = CONST.standarH;//自定义高度
				if (configuration != null) {
					if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
						standarH = CONST.standarH;
					}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
						standarH = displayW;
					}
				}
				if (videoW == 0 || videoH == 0) {
					surfaceView.setLayoutParams(new LinearLayout.LayoutParams(standarH, standarH));
					return;
				}else {
					surfaceView.setLayoutParams(new LinearLayout.LayoutParams(videoW*standarH/videoH, standarH));
				}
			}
			
//			if (configuration != null) {
//				if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
//					surfaceView.setLayoutParams(new LinearLayout.LayoutParams(displayW, displayW*videoH/videoW));
//				}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//					surfaceView.setLayoutParams(new LinearLayout.LayoutParams(displayH, displayH*videoH/videoW));
//				}
//			}else {
//				surfaceView.setLayoutParams(new LinearLayout.LayoutParams(displayW, displayW*videoH/videoW));
//			}
		}
	}
	
	@Override
	public void onVideoSizeChanged(MediaPlayer player, int videoW, int videoH) {
		changeVideo(videoW, videoH);
	}
	
	@Override
	public void onCompletion(MediaPlayer player) {
		releaseTimer();
		ivPlayLand.setImageResource(R.drawable.iv_play);
		seekBarLand.setProgress(0);
		tvStartTimeLand.setText("00:00");
		handler.removeMessages(HANDLER_VISIBILITY);
		reTop.setVisibility(View.VISIBLE);
		reBottom.setVisibility(View.VISIBLE);
		ivPlayLand.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 释放MediaPlayer资源
	 */
	private void releaseMediaPlayer() {
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	/**
	 * 释放timer
	 */
	private void releaseTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
		releaseTimer();
        releaseMediaPlayer();
    }
	
	/**
	 * 获取评论列表
	 */
	private void OkHttpCommentList(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("wid", data.getVideoId());
		builder.add("page", page+"");
		builder.add("pagesize", pageSize+"");
		builder.add("appid", CONST.APPID);
		RequestBody body = builder.build();
		OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject object = new JSONObject(result);
						if (object != null) {
							if (!object.isNull("status")) {
								int status  = object.getInt("status");
								if (status == 1) {//成功
									if (!object.isNull("info")) {
										JSONArray array = object.getJSONArray("info");
										int length = array.length();
										if (length <= 0) {
											return;
										}
										mList.clear();
										for (int i = 0; i < array.length(); i++) {
											JSONObject obj = array.getJSONObject(i);
											PhotoDto dto = new PhotoDto();
											if (!obj.isNull("create_time")) {
												dto.createTime = obj.getString("create_time");
											}
											if (!obj.isNull("username")) {
												dto.userName = obj.getString("username");
											}
											if (!obj.isNull("comment")) {
												dto.comment = EmojiMapUtil.replaceCheatSheetEmojis(obj.getString("comment"));
											}
											if (!obj.isNull("photo")) {
												dto.portraitUrl = obj.getString("photo");
											}
											mList.add(dto);
										}
									}

									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											tvCommentCount.setText(getString(R.string.comment) + "（"+mList.size()+"）");
											if (mList.size() > 0 && mAdapter != null) {
												mAdapter.notifyDataSetChanged();
											}
										}
									});

								}else {
									//失败
									if (!object.isNull("msg")) {
										final String msg = object.getString("msg");
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												if (msg != null) {
													Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
												}
											}
										});

									}
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	/**
	 * 提交评论
	 */
	private void OkHttpSubmitComment(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", MyApplication.TOKEN);
		builder.add("wid", data.videoId);
		builder.add("comment", EmojiMapUtil.replaceUnicodeEmojis(etComment.getText().toString()));
		RequestBody body = builder.build();
		OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				final String result = response.body().string();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject object = new JSONObject(result);
								if (object != null) {
									if (!object.isNull("status")) {
										int status  = object.getInt("status");
										if (status == 1) {//成功
											etComment.setText("");
											OkHttpCommentList(CONST.GET_WORK_COMMENT_URL);
										}else {
											//失败
											if (!object.isNull("msg")) {
												String msg = object.getString("msg");
												if (msg != null) {
													Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
												}
											}
										}
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		});
	}
	
	/**
	 * 隐藏虚拟键盘
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (etComment != null) {
			CommonUtil.hideInputSoft(etComment, mContext);
		}
		if (llSubmit != null) {
			llSubmit.setVisibility(View.GONE);
		}
		return super.onTouchEvent(event);
	}
	
	/**
	 * 点赞
	 */
	private void OkHttpPraise(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", MyApplication.TOKEN);
		builder.add("id", data.videoId);
		RequestBody body = builder.build();
		OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
				if (result != null) {
					try {
						JSONObject object = new JSONObject(result);
						if (object != null) {
							if (!object.isNull("status")) {
								int status  = object.getInt("status");
								if (status == 1) {//成功
									//保存点赞状态
									SharedPreferences sharedPreferences = getSharedPreferences(data.videoId, Context.MODE_PRIVATE);
									Editor editor = sharedPreferences.edit();
									editor.putBoolean("praiseState", true);
									editor.commit();
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											ivPraise.setImageResource(R.drawable.iv_like);
										}
									});
								}else {
									//失败
									if (!object.isNull("msg")) {
										final String msg = object.getString("msg");
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												if (msg != null) {
													Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
												}
											}
										});
									}
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void exit() {
		if (configuration == null) {
			releaseTimer();
	        releaseMediaPlayer();
	        
	        if (llSubmit.getVisibility() == View.VISIBLE) {
	        	commentAnimation(true, llSubmit);
				llSubmit.setVisibility(View.GONE);
			}else {
				finish();
			}
		}else {
			if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
				releaseTimer();
		        releaseMediaPlayer();
		        
		        if (llSubmit.getVisibility() == View.VISIBLE) {
		        	commentAnimation(true, llSubmit);
					llSubmit.setVisibility(View.GONE);
				}else {
					finish();
				}
			}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}
	}
	
	private void commentAnimation(boolean flag, final LinearLayout llLayout) {
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation = null;
		if (flag == false) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 1f, 
					Animation.RELATIVE_TO_SELF, 0);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,1.0f);
		}
		animation.setDuration(200);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		llLayout.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				llLayout.clearAnimation();
			}
		});
	}

	/**
	 * 下载视频
	 * @param videoUrl
	 */
	private void downloadVideo(String videoUrl) {
		if (TextUtils.isEmpty(videoUrl)) {
			return;
		}
		DownloadManager dManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		Uri uri = Uri.parse(videoUrl);
		DownloadManager.Request request = new DownloadManager.Request(uri);
		// 设置下载路径和文件名
		String filename = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);//获取文件名称
		File files = new File(CONST.DOWNLOAD_ADDR);
		if (!files.exists()) {
			files.mkdirs();
		}
		request.setDestinationInExternalPublicDir(files.getAbsolutePath(), filename);
		request.setDescription(filename);
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.setMimeType("application/vnd.android.package-archive");
		// 设置为可被媒体扫描器找到
		request.allowScanningByMediaScanner();
		// 设置为可见和可管理
		request.setVisibleInDownloadsUi(true);
		long refernece = dManager.enqueue(request);
//		// 把当前下载的ID保存起来
//		SharedPreferences sPreferences = mContext.getSharedPreferences("downloadplato", 0);
//		sPreferences.edit().putLong("plato", refernece).commit();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivBackLand:
			exit();
			break;
		case R.id.llSurfaceView:
			if (mPlayer != null && mPlayer.isPlaying()) {
				if (reBottom.getVisibility() == View.VISIBLE) {
					reTop.setVisibility(View.GONE);
					reBottom.setVisibility(View.GONE);
					ivPlayLand.setVisibility(View.GONE);
				}else {
					reTop.setVisibility(View.VISIBLE);
					reBottom.setVisibility(View.VISIBLE);
					ivPlayLand.setVisibility(View.VISIBLE);
					dismissColunm();
				}
			}else {
				reTop.setVisibility(View.VISIBLE);
				reBottom.setVisibility(View.VISIBLE);
				ivPlayLand.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.ivInFull:
			dismissColunm();
			
			if (configuration == null) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}else {
				if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
			}
			break;
		case R.id.ivPlayLand:
			dismissColunm();
			startPlayVideo();
			break;
		case R.id.tvSubmit:
			if (!TextUtils.isEmpty(etComment.getText().toString())) {
				CommonUtil.hideInputSoft(etComment, mContext);
				OkHttpSubmitComment(CONST.COMMENT_WORD_URL);
			}
			break;
		case R.id.ivComment:
			if (MyApplication.TOKEN != null) {
				if (llSubmit.getVisibility() == View.GONE) {
					commentAnimation(false, llSubmit);
					llSubmit.setVisibility(View.VISIBLE);
				}else {
					commentAnimation(true, llSubmit);
					llSubmit.setVisibility(View.GONE);
				}
			}else {
				Intent intent = new Intent(mContext, LoginActivity.class);
				startActivityForResult(intent, 0);
			}
			break;
		case R.id.ivPraise:
			if (praiseState) {
				return;
			}else {
				OkHttpPraise(CONST.PRAISE_WORK_URL);
			}
			break;
		case R.id.ivShare:
			CommonUtil.share(OnlineVideoActivity.this, data.title, data.title, data.imgUrl, CONST.WEB+data.getVideoId()+CONST.WEB_SUFFIX);
			break;
		case R.id.ivDownload:
			downloadVideo(data.videoUrl);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 0:
				llSubmit.setVisibility(View.VISIBLE);
				break;

			default:
				break;
			}
		}
	}

}
