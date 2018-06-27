package com.hf.live.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.hf.live.R;
import com.hf.live.adapter.EventTypeAdapter;
import com.hf.live.adapter.WeatherTypeAdapter;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.dto.UploadVideoDto;
import com.hf.live.qcloud.TCConstants;
import com.hf.live.qcloud.TXUGCPublish;
import com.hf.live.qcloud.TXUGCPublishTypeDef;
import com.hf.live.util.OkHttpUtil;
import com.hf.live.view.ScrollviewGridview;
import com.hf.live.view.UploadDialog;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_BEGIN;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_END;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_PROGRESS;


/**
 * 预览、上传界面
 * @author shawn_sun
 *
 */

public class DisplayVideoActivity extends BaseActivity implements ITXVodPlayListener, OnClickListener, AMapLocationListener{
	
	private Context mContext;
    private ImageView ivBack, ivInFull, ivPlay;
    private TextView tvPositon, tvPlayTime, tvTextCount, tvRemove, tvUpload;
    private SeekBar seekBar;//进度条
	private EditText etTitle, etContent;//编辑视频标题
	private ScrollView scrollView;//操作区域
	private RelativeLayout reBottom;//屏幕上方区域
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmss");
	private int width, height;//屏幕宽
	private Configuration configuration = null;//方向监听器
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private double lat = 0, lng = 0;
	private UploadDialog uploadDialog;
	private String position = "";

	private TXCloudVideoView mTXCloudVideoView;
	private TXVodPlayer mTXVodPlayer;
	private TXVodPlayConfig mTXVodPlayConfig;
	private boolean isPlaying = true;//是否正在播放
	private boolean isFirstPlay = true;//是否为第一次播放
	private String txSign;//腾讯云签名
	private String videoPath, thumbPath;//视频路径
	private static final int HANDLER_DELAY = 1001;//点击视频显示按钮延时

	private ScrollviewGridview gridView1 = null;
	private WeatherTypeAdapter adapter1 = null;
	private List<UploadVideoDto> list1 = new ArrayList<>();
	private String weatherType = "";//天气类型
	private ScrollviewGridview gridView2 = null;
	private EventTypeAdapter adapter2 = null;
	private List<UploadVideoDto> list2 = new ArrayList<>();
	private String eventType = "";//事件类型

	//活动
	private TextView tvWeatherType, tvWeatherFlag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_video);
		mContext = this;
		initWidget();
        initVideoView();
		initGridView1();
		initGridView2();

		event();
	}

	/**
	 * 活动
	 */
	private void event() {
		if (getIntent().hasExtra("appid")) {
			String appid = getIntent().getStringExtra("appid");//活动专用频道
			tvWeatherType.setVisibility(View.GONE);
			tvWeatherFlag.setVisibility(View.GONE);
			gridView1.setVisibility(View.GONE);
			gridView2.setVisibility(View.GONE);

			weatherType = "wt04";
			eventType = "et01";

			etTitle.setText(MyApplication.OLDUSERNAME+"-"+MyApplication.USERNAME+"-"+MyApplication.COLLEGE+"-"+MyApplication.MAJOR);
			etTitle.setSelection(etTitle.length());
		}
	}

	/**
	 * 设置全屏
	 * @param enable
	 */
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
	 * 显示竖屏，隐藏横屏
	 */
	private void showPort() {
		scrollView.setVisibility(View.VISIBLE);
		ivInFull.setImageResource(R.drawable.iv_out_full);

		if (mTXCloudVideoView != null) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width*9/16);
			mTXCloudVideoView.setLayoutParams(params);
		}
	}

	/**
	 * 显示横屏，隐藏竖屏
	 */
	private void showLand() {
		scrollView.setVisibility(View.GONE);
		ivInFull.setImageResource(R.drawable.iv_in_full);

		if (mTXCloudVideoView != null) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(height, height*9/16);
			mTXCloudVideoView.setLayoutParams(params);
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
        ivBack = (ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);
        tvPositon = (TextView) findViewById(R.id.tvPosition);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        tvPlayTime = (TextView) findViewById(R.id.tvPlayTime);
        ivInFull = (ImageView) findViewById(R.id.ivInFull);
        ivInFull.setOnClickListener(this);
        ivPlay = (ImageView) findViewById(R.id.ivPlay);
        ivPlay.setOnClickListener(this);
        etTitle = (EditText) findViewById(R.id.etTitle);
        etContent = (EditText) findViewById(R.id.etContent);
        etContent.addTextChangedListener(contentWatcher);
        tvTextCount = (TextView) findViewById(R.id.tvTextCount);
        tvRemove = (TextView) findViewById(R.id.tvRemove);
        tvRemove.setOnClickListener(this);
        tvUpload = (TextView) findViewById(R.id.tvUpload);
        tvUpload.setOnClickListener(this);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
		reBottom = (RelativeLayout) findViewById(R.id.reBottom);
		tvWeatherType = (TextView) findViewById(R.id.tvWeatherType);
		tvWeatherFlag = (TextView) findViewById(R.id.tvWeatherFlag);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getRealMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (mTXVodPlayer != null) {
					mTXVodPlayer.seek(seekBar.getProgress());
				}
			}
		});

		//获取视频路径
		if (getIntent().hasExtra(TCConstants.VIDEO_RECORD_VIDEPATH)) {
			videoPath = getIntent().getStringExtra(TCConstants.VIDEO_RECORD_VIDEPATH);
		}

		//获取缩略图路径
		if (getIntent().hasExtra(TCConstants.VIDEO_RECORD_COVERPATH)) {
			thumbPath = getIntent().getStringExtra(TCConstants.VIDEO_RECORD_COVERPATH);
		}

		startLocation();

		OkHttpTxCloudSign("http://channellive2.tianqi.cn/weather/Tensent/tensentSign");

	}

	/**
	 * 开始定位
	 */
	private void startLocation() {
		mLocationOption = new AMapLocationClientOption();//初始化定位参数
		mLocationClient = new AMapLocationClient(mContext);//初始化定位
		mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
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
		lat = amapLocation.getLatitude();
		lng = amapLocation.getLongitude();

		position = amapLocation.getPoiName();
		if (TextUtils.isEmpty(position)) {
			position = amapLocation.getStreet()+amapLocation.getStreetNum();
		}
		if (amapLocation.getCity().contains(amapLocation.getProvince())) {
			position = amapLocation.getCity()+amapLocation.getDistrict()+position;
		}else {
			position = amapLocation.getProvince()+amapLocation.getCity()+amapLocation.getDistrict()+position;
		}
		tvPositon.setText("拍摄地点："+position);
	}

	/**
	 * 获取腾讯云视频发布签名
	 * @param url
	 */
	private void OkHttpTxCloudSign(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
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
								JSONObject obj = new JSONObject(result);
								if (!obj.isNull("sign")) {
									txSign = obj.getString("sign");
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		}).start();
	}

	private void initVideoView() {
		mTXVodPlayer = new TXVodPlayer(mContext);
		mTXVodPlayConfig = new TXVodPlayConfig();
        mTXCloudVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        mTXCloudVideoView.disableLog(true);
		mTXCloudVideoView.setOnClickListener(this);

		showPort();
        startPlay();
    }

	/**
	 * 开始播放
	 * @return
	 */
	private void startPlay() {
		if (isFirstPlay) {
			ivPlay.setImageResource(R.drawable.iv_pause);
			mTXVodPlayer.setPlayerView(mTXCloudVideoView);
			mTXVodPlayer.enableHardwareDecode(false);
			mTXVodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
			mTXVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
			mTXVodPlayer.setConfig(mTXVodPlayConfig);
			mTXVodPlayer.setVodListener(this);
			int result = mTXVodPlayer.startPlay(videoPath); // result返回值：0 success;  -1 empty url; -2 invalid url; -3 invalid playType;
			if (result != 0) {
				ivPlay.setImageResource(R.drawable.iv_play);
				isPlaying = false;
			}
			isFirstPlay = false;
		}else {
			if (mTXVodPlayer.isPlaying()) {
				ivPlay.setImageResource(R.drawable.iv_play);
				mTXVodPlayer.pause();
				isPlaying = false;
			}else {
				ivPlay.setImageResource(R.drawable.iv_pause);
				mTXVodPlayer.resume();
				isPlaying = true;
			}
		}
        delayShowControl();
	}

    @Override
    public void onPlayEvent(TXVodPlayer txVodPlayer, int event, Bundle param) {
		switch (event) {
			case PLAY_EVT_PLAY_BEGIN:
				hideControlLayout();
				break;
			case PLAY_EVT_PLAY_PROGRESS:
				int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS);// 播放进度, 单位是秒
				int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION);// 视频总长, 单位是秒

				if (seekBar != null) {
					seekBar.setProgress(progress);
					seekBar.setMax(duration);
				}
				if (tvPlayTime != null) {
					tvPlayTime.setText(String.format(Locale.CHINA, "%02d:%02d/%02d:%02d", (progress) / 60, progress % 60, (duration) / 60, duration % 60));
				}
				break;
			case PLAY_EVT_PLAY_END:
//				mTXLivePlayer.pause();
				ivPlay.setImageResource(R.drawable.iv_play);
				showControlLayout();
				break;
		}
    }

    @Override
    public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {

    }

	/**
	 * 延时显示操作按钮
	 */
	private void delayShowControl() {
		handler.removeMessages(HANDLER_DELAY);
		Message msg = handler.obtainMessage(HANDLER_DELAY);
		msg.what = HANDLER_DELAY;
		handler.sendMessageDelayed(msg, 5000);
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HANDLER_DELAY:
					if (isPlaying) {
						if (ivPlay.getVisibility() == View.VISIBLE) {
							hideControlLayout();
						}
					}else {
						showControlLayout();
					}
					break;

				default:
					break;
			}
		}
	};

	/**
	 * 显示操作按钮
	 */
	private void showControlLayout() {
		reBottom.setVisibility(View.VISIBLE);
		ivPlay.setVisibility(View.VISIBLE);
	}

	/**
	 * 隐藏操作按钮
	 */
	private void hideControlLayout() {
		reBottom.setVisibility(View.GONE);
		ivPlay.setVisibility(View.GONE);
	}
	
	/**
	 * 初始化天气类型gridview
	 */
	private void initGridView1() {
		//wt01雪，wt02雨，wt03冰雹，wt04晴，wt05霾，wt06大风，wt07沙尘
		list1.clear();
		UploadVideoDto dto = new UploadVideoDto();
		dto.weatherType = "wt01";
		dto.weatherName = "雪";
		dto.isSelected = false;
		list1.add(dto);
		dto = new UploadVideoDto();
		dto.weatherType = "wt02";
		dto.weatherName = "雨";
		dto.isSelected = false;
		list1.add(dto);
		dto = new UploadVideoDto();
		dto.weatherType = "wt03";
		dto.weatherName = "冰雹";
		dto.isSelected = false;
		list1.add(dto);
		dto = new UploadVideoDto();
		dto.weatherType = "wt04";
		dto.weatherName = "晴";
		dto.isSelected = false;
		list1.add(dto);
		dto = new UploadVideoDto();
		dto.weatherType = "wt05";
		dto.weatherName = "霾";
		dto.isSelected = false;
		list1.add(dto);
		dto = new UploadVideoDto();
		dto.weatherType = "wt06";
		dto.weatherName = "大风";
		dto.isSelected = false;
		list1.add(dto);
		dto = new UploadVideoDto();
		dto.weatherType = "wt07";
		dto.weatherName = "沙尘";
		dto.isSelected = false;
		list1.add(dto);
		
		gridView1 = (ScrollviewGridview) findViewById(R.id.gridView1);
		adapter1 = new WeatherTypeAdapter(mContext, list1);
		gridView1.setAdapter(adapter1);
		gridView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < list1.size(); i++) {
					if (i == arg2) {
						list1.get(i).isSelected = true;
						weatherType = list1.get(i).weatherType;
					}else {
						list1.get(i).isSelected = false;
					}
				}
				if (adapter1 != null) {
					adapter1.notifyDataSetChanged();
				}
			}
		});
	}
	
	/**
	 * 初始化事件类型gridview
	 */
	private void initGridView2() {
		//et01自然灾害，et02事故灾害，et03公共卫生，et04社会安全
		list2.clear();
		UploadVideoDto dto = new UploadVideoDto();
		dto.eventType = "et01";
		dto.eventName = "自然灾害";
		dto.isSelected = false;
		list2.add(dto);dto = new UploadVideoDto();
		dto.eventType = "et02";
		dto.eventName = "事故灾害";
		dto.isSelected = false;
		list2.add(dto);dto = new UploadVideoDto();
		dto.eventType = "et03";
		dto.eventName = "公共卫生";
		dto.isSelected = false;
		list2.add(dto);dto = new UploadVideoDto();
		dto.eventType = "et04";
		dto.eventName = "社会安全";
		dto.isSelected = false;
		list2.add(dto);
		
		gridView2 = (ScrollviewGridview) findViewById(R.id.gridView2);
		adapter2 = new EventTypeAdapter(mContext, list2);
		gridView2.setAdapter(adapter2);
		gridView2.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < list2.size(); i++) {
					if (i == arg2) {
						list2.get(i).isSelected = true;
						eventType = list2.get(i).eventType;
					}else {
						list2.get(i).isSelected = false;
					}
				}
				if (adapter2 != null) {
					adapter2.notifyDataSetChanged();
				}
			}
		});
	}

	/**
	 * 输入内容监听器
	 */
	private TextWatcher contentWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (etContent.getText().length() == 0) {
				tvTextCount.setText("(200字以内)");
			}else {
				int count = 200-etContent.getText().length();
				tvTextCount.setText("(还可输入"+count+"字)");
			}
		}
	};
	
	/**
	 * 删除对应的视频和缩略图文件
	 */
	private void deleteFiles() {
		File file1 = new File(videoPath);
		if (file1.exists()) {
			file1.delete();
		}
		File file2 = new File(thumbPath);
		if (file2.exists()) {
			file2.delete();
		}
	}

	/**
	 * 发送广播，刷新已上传、未上传
	 */
	private void sendRefreshBroadCast() {
		Intent intent = new Intent();
		intent.setAction(CONST.REFRESH_UPLOAD);
		sendBroadcast(intent);

		Intent intent2 = new Intent();
		intent2.setAction(CONST.REFRESH_NOTUPLOAD);
		sendBroadcast(intent2);
	}

	private void showUploadDialog() {
		if (uploadDialog == null) {
			uploadDialog = new UploadDialog(mContext);
		}
		uploadDialog.setPercent(0);
		uploadDialog.show();
	}

	private void cancelUploadDialog() {
		if (uploadDialog != null) {
			uploadDialog.dismiss();
		}
	}

	/**
	 * 检查上传条件
	 */
	private void checkDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.upload_dialog, null);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
	}

	/**
	 * 上传腾讯云
	 */
	private void QQCloudPublish() {
		TXUGCPublish txugcPublish = new TXUGCPublish(mContext);
		txugcPublish.setListener(new TXUGCPublishTypeDef.ITXVideoPublishListener() {
			@Override
			public void onPublishProgress(long uploadBytes, long totalBytes) {
				Log.e("progress", uploadBytes+"");
				if (uploadDialog != null) {
					uploadDialog.setPercent((int)(100*uploadBytes/totalBytes));
				}
			}

			@Override
			public void onPublishComplete(TXUGCPublishTypeDef.TXPublishResult result) {
				cancelUploadDialog();
				if (result.retCode == 0) {
					try {
						JSONObject obj = new JSONObject();

						JSONObject video = new JSONObject();
						JSONObject videoUrl = new JSONObject();
						videoUrl.put("url", result.videoURL);
						video.put("ORG", videoUrl);
						obj.put("video", video);

						JSONObject thumbUrl = new JSONObject();
						thumbUrl.put("url", result.coverURL);
						obj.put("thumbnail", thumbUrl);

						String worksinfo = obj.toString();
						Log.e("worksinfo", worksinfo);
						OkHttpPostVideo("http://channellive2.tianqi.cn/weather/Tensent/upload", worksinfo, result.videoId);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});

		TXUGCPublishTypeDef.TXPublishParam param = new TXUGCPublishTypeDef.TXPublishParam();
		// signature计算规则可参考 https://www.qcloud.com/document/product/266/9221
		param.signature = txSign;
		param.videoPath = videoPath;
		param.coverPath = thumbPath;
		txugcPublish.publishVideo(param);
	}
	
	/**
	 * 上传视频
	 * @param url 接口地址
	 */
	private void OkHttpPostVideo(final String url, final String worksinfo, final String fileid) {
		File videoFile = new File(videoPath);
		String fileName = videoFile.getName().substring(0, videoFile.getName().length()-4);
		FormBody.Builder builder = new FormBody.Builder();
		if (getIntent().hasExtra("appid")) {
			builder.add("appid", getIntent().getStringExtra("appid"));//活动专用频道
		}else {
			builder.add("appid", CONST.APPID);
		}
		builder.add("token", MyApplication.TOKEN);
		builder.add("workstype", "video");
		builder.add("location", position);
		builder.add("title", etTitle.getText().toString());
		if (fileName.length() == 14) {
			try {
				builder.add("work_time", sdf2.format(sdf3.parse(fileName)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		builder.add("weather_flag", weatherType);
		builder.add("other_flags", eventType);
		builder.add("content", etContent.getText().toString());
		builder.add("latlon", lat+","+lng);
		builder.add("worksinfo", worksinfo);
		builder.add("fileid", fileid);
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(mContext, "上传失败！", Toast.LENGTH_SHORT).show();
							}
						});
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
										JSONObject obj = new JSONObject(result);
										if (!obj.isNull("status")) {
											int status = obj.getInt("status");
											if (status == 1) {
//												deleteFiles();
//												sendRefreshBroadCast();
												uploadComplete();
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
		}).start();
	}
	
	/**
	 * 上传视频成功对话框
	 */
	private void uploadComplete() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.upload_success_dialog, null);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	private void exitDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();

		tvMessage.setText(getString(R.string.sure_exit));
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				finish();
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (configuration == null) {
				exitDialog();
			}else {
				if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}else {
					exitDialog();
				}
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivBack:
			if (configuration == null) {
				exitDialog();
			}else {
				if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}else {
					exitDialog();
				}
			}
			break;
		case R.id.video_view:
			if (mTXVodPlayer != null && mTXVodPlayer.isPlaying()) {
				if (reBottom.getVisibility() == View.VISIBLE) {
					reBottom.setVisibility(View.GONE);
					ivPlay.setVisibility(View.GONE);
				}else {
					reBottom.setVisibility(View.VISIBLE);
					ivPlay.setVisibility(View.VISIBLE);
				}
			}else {
				reBottom.setVisibility(View.VISIBLE);
				ivPlay.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.ivInFull:
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
		case R.id.ivPlay:
			startPlay();
			break;
		case R.id.tvRemove:
			exitDialog();
			break;
		case R.id.tvUpload:
			if (TextUtils.isEmpty(weatherType) || TextUtils.isEmpty(etTitle.getText().toString())) {
				checkDialog();
			}else {
				showUploadDialog();
				QQCloudPublish();
			}
			break;

		default:
			break;
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        if (mTXCloudVideoView != null) {
			mTXCloudVideoView.onResume();
		}
		if (mTXVodPlayer != null) {
			mTXVodPlayer.resume();
		}
    }

    @Override
    protected void onPause() {
        super.onPause();
		if (mTXCloudVideoView != null) {
			mTXCloudVideoView.onPause();
		}
		if (mTXVodPlayer != null) {
			mTXVodPlayer.pause();
		}
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
		if (mTXCloudVideoView != null) {
			mTXCloudVideoView.onDestroy();
		}
		if (mTXVodPlayer != null) {
			mTXVodPlayer.setPlayListener(null);
			mTXVodPlayer.stopPlay(true);
		}
    }

}
