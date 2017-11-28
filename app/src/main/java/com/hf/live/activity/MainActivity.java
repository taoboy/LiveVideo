package com.hf.live.activity;

/**
 * 主界面
 */

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.hf.live.R;
import com.hf.live.common.MyApplication;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.AutoUpdateUtil;
import com.hf.live.util.GetPathFromUri4kitkat;
import com.hf.live.util.WeatherUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants.Language;
import cn.com.weather.listener.AsyncResponseHandler;

public class MainActivity extends BaseActivity implements AMapLocationListener, OnClickListener{
	
	private Context mContext = null;
	private TextView tvAddress = null;//定位地点
	private TextView tvTime = null;//更新时间
	private ImageView ivRefresh = null;//更新按钮
	private ImageView ivPhenomenon = null;//天气现象
	private TextView tvPhenomenon = null;//天气现象
	private TextView tvFactTemp = null;//实况温度
	private TextView tvBodyTemp = null;//体感温度
	private TextView tvWind = null;//风向风力
	private TextView tvHumitidy = null;//湿度
	private TextView tvRainFall = null;//降水
	private TextView tvAQI = null;//空气质量
	private ImageView ivMeet = null;
	private TextView tvMeet = null;
	private LinearLayout llTable = null;//数据桌面
	private RelativeLayout reLive = null;//直播
	private RelativeLayout reMeet = null;//会商
	private RelativeLayout reCamera = null;//拍摄
	private RelativeLayout reVideo = null;//视频
	private ProgressBar progressBar = null;
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmmss");
	private long mExitTime;//记录点击完返回按钮后的long型时间
	private ImageView ivPerson = null;
	private int flag = 1;//1为影视、2为会商,别忘记修改安装logo、高德地图key

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		MyApplication.addDestoryActivity(MainActivity.this, "MainActivity");
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		AutoUpdateUtil.checkUpdate(MainActivity.this, mContext, "51", getString(R.string.app_name), true);
		
		tvAddress = (TextView) findViewById(R.id.tvAddress);
		tvTime = (TextView) findViewById(R.id.tvTime);
		ivPhenomenon = (ImageView) findViewById(R.id.ivPhenomenon);
		tvPhenomenon = (TextView) findViewById(R.id.tvPhenomenon);
		tvFactTemp = (TextView) findViewById(R.id.tvFactTemp);
		tvBodyTemp = (TextView) findViewById(R.id.tvBodyTemp);
		tvWind = (TextView) findViewById(R.id.tvWind);
		tvHumitidy = (TextView) findViewById(R.id.tvHumitidy);
		tvRainFall = (TextView) findViewById(R.id.tvRainFall);
		tvAQI = (TextView) findViewById(R.id.tvAQI);
		ivRefresh = (ImageView) findViewById(R.id.ivRefresh);
		ivRefresh.setOnClickListener(this);
		llTable = (LinearLayout) findViewById(R.id.llTable);
		reLive = (RelativeLayout) findViewById(R.id.reLive);
		reLive.setOnClickListener(this);
		reMeet = (RelativeLayout) findViewById(R.id.reMeet);
		reMeet.setOnClickListener(this);
		reCamera = (RelativeLayout) findViewById(R.id.reCamera);
		reCamera.setOnClickListener(this);
		reVideo = (RelativeLayout) findViewById(R.id.reVideo);
		reVideo.setOnClickListener(this);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		ivPerson = (ImageView) findViewById(R.id.ivPerson);
		ivPerson.setOnClickListener(this);
		ivMeet = (ImageView) findViewById(R.id.ivMeet);
		tvMeet = (TextView) findViewById(R.id.tvMeet);
		
		if (flag == 1) {
			ivPerson.setVisibility(View.GONE);
			ivMeet.setBackgroundResource(R.drawable.iv_person2);
			tvMeet.setText(getString(R.string.person));
		}else if (flag == 2){
			ivPerson.setVisibility(View.VISIBLE);
			ivMeet.setBackgroundResource(R.drawable.iv_meet);
			tvMeet.setText(getString(R.string.meet));
		}
		
		startLocation();
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
		if (!TextUtils.isEmpty(amapLocation.getAoiName())) {
			tvAddress.setText(amapLocation.getAoiName());
		}else {
			tvAddress.setText(amapLocation.getStreet()+amapLocation.getStreetNum());
		}
    	getWeatherInfo(amapLocation.getLatitude(), amapLocation.getLongitude());
	}

	/**
	 * 获取天气数据
	 * @param lat
	 * @param lng
	 */
	private void getWeatherInfo(double lat, double lng) {
		WeatherAPI.getGeo(mContext,lng+"", lat+"", new AsyncResponseHandler(){
			@Override
			public void onComplete(JSONObject content) {
				super.onComplete(content);
				if (!content.isNull("geo")) {
					try {
						JSONObject geoObj = content.getJSONObject("geo");
						if (!geoObj.isNull("id")) {
							String cityId = geoObj.getString("id");
							if (cityId != null) {
								WeatherAPI.getWeather2(mContext, cityId, Language.ZH_CN, new AsyncResponseHandler() {
									@Override
									public void onComplete(Weather content) {
										super.onComplete(content);
										if (content != null) {
											try {
												JSONObject object = content.getWeatherFactInfo();//实况信息
												if (!object.isNull("l7")) {
													String time = object.getString("l7");
													if (time != null) {
														tvTime.setText(time + getString(R.string.refresh));
													}
												}
												if (!object.isNull("l5")) {
													String weatherCode = WeatherUtil.lastValue(object.getString("l5"));
													if (weatherCode != null) {
														Drawable drawable = getResources().getDrawable(R.drawable.phenomenon_drawable);
														drawable.setLevel(Integer.valueOf(weatherCode));
														ivPhenomenon.setImageDrawable(drawable);
														tvPhenomenon.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(weatherCode))));
													}
												}
												if (!object.isNull("l1")) {
													String factTemp = WeatherUtil.lastValue(object.getString("l1"));
													if (factTemp != null) {
														tvFactTemp.setText(getString(R.string.current) + factTemp + getString(R.string.unit_c));
													}
												}
												if (!object.isNull("l12")) {
													String bodyTemp = WeatherUtil.lastValue(object.getString("l12"));
													if (bodyTemp != null) {
														tvBodyTemp.setText(getString(R.string.body) + bodyTemp + getString(R.string.unit_c));
													}
												}
												if (!object.isNull("l4") && !object.isNull("l3")) {
													String windDir = WeatherUtil.lastValue(object.getString("l4"));
													String windForce = WeatherUtil.lastValue(object.getString("l3"));
													if (windDir != null && windForce != null) {
														tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))) + WeatherUtil.getFactWindForce(Integer.valueOf(windForce)));
													}
												}
												if (!object.isNull("l2")) {
													String humidity = WeatherUtil.lastValue(object.getString("l2"));
													if (humidity != null) {
														tvHumitidy.setText(getString(R.string.humitidy) + humidity + getString(R.string.unit_percent));
													}
												}
												if (!object.isNull("l6")) {
													String rainFall = WeatherUtil.lastValue(object.getString("l6"));
													if (rainFall != null) {
														tvRainFall.setText(getString(R.string.rainfall) + rainFall + getString(R.string.mm));
													}
												}

												JSONObject airObj = content.getAirQualityInfo();//空气质量信息
												if (!airObj.isNull("k3")) {
													String airQua = WeatherUtil.lastValue(airObj.getString("k3"));
													if (airQua != null) {
														tvAQI.setText(getString(R.string.aqi) + airQua);
													}
												}
											} catch (JSONException e) {
												e.printStackTrace();
											}

								        	progressBar.setVisibility(View.GONE);
											llTable.setVisibility(View.VISIBLE);
											ivRefresh.clearAnimation();
										}
									}
								});
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			
			@Override
			public void onError(Throwable error, String content) {
				super.onError(error, content);
	        	progressBar.setVisibility(View.GONE);
				ivRefresh.clearAnimation();
				Toast.makeText(mContext, getString(R.string.get_information_failed), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	/**
	 * 验证用户信息
	 */
	private boolean checkInfo(String ip, String userName, String pwd) {
		if (TextUtils.isEmpty(ip)) {
			Toast.makeText(mContext, getResources().getString(R.string.input_address), Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(userName)) {
			Toast.makeText(mContext, getResources().getString(R.string.input_username), Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(pwd)) {
			Toast.makeText(mContext, getResources().getString(R.string.input_password), Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	/**
	 * 登录对话框
	 */
	private void loginDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.live_dialog, null);
		final EditText etIp = (EditText) view.findViewById(R.id.etIp);
		final EditText etUserName = (EditText) view.findViewById(R.id.etUserName);
		final EditText etPwd = (EditText) view.findViewById(R.id.etPwd);
		TextView tvCancel = (TextView) view.findViewById(R.id.tvCancel);
		TextView tvLogin = (TextView) view.findViewById(R.id.tvLogin);
		
		SharedPreferences sharedPreferences = getSharedPreferences("LIVE", Context.MODE_PRIVATE);
		etIp.setText(sharedPreferences.getString("ip", null));
		etUserName.setText(sharedPreferences.getString("userName", null));
		etUserName.setSelection(etUserName.getText().toString().length());
		etPwd.setText(sharedPreferences.getString("pwd", null));
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		tvLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			}
		});
	}
	
	/**
	 * 拍摄对话框
	 */
	private void cameraDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflater.inflate(R.layout.camera_dialog, null);
		RelativeLayout reCamera = (RelativeLayout) view.findViewById(R.id.reCamera);
		RelativeLayout reSelect = (RelativeLayout) view.findViewById(R.id.reSelect);
		RelativeLayout rePicture = (RelativeLayout) view.findViewById(R.id.rePicture);
		TextView tvCamera = (TextView) view.findViewById(R.id.tvCamera);
		TextView tvSelect = (TextView) view.findViewById(R.id.tvSelect);
		TextView tvPicture = (TextView) view.findViewById(R.id.tvPicture);
		tvCamera.setText(getString(R.string.camera_video));
		tvSelect.setText(getString(R.string.select_video));
		tvPicture.setText(getString(R.string.select_pic));
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.getWindow().setGravity(Gravity.CENTER|Gravity.BOTTOM);
		dialog.show();
		
		reCamera.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (MyApplication.TOKEN != null) {
					startActivity(new Intent(mContext, CameraActivity.class));
				}else {
					startActivityForResult(new Intent(mContext, LoginActivity.class), 0);
				}
				dialog.dismiss();
			}
		});
		
		reSelect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (MyApplication.TOKEN != null) {
//					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//				    intent.setType("video/*");
//				    startActivityForResult(intent, 1);
					startActivity(new Intent(mContext, SelectAlbumVideoActivity.class));
				}else {
					startActivityForResult(new Intent(mContext, LoginActivity.class), 2);
				}
				dialog.dismiss();
			}
		});
		
		rePicture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (MyApplication.TOKEN != null) {
//					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//				    intent.setType("image/*");
//				    startActivityForResult(intent, 3);
					startActivity(new Intent(mContext, SelectAlbumPictureActivity.class));
				}else {
					startActivityForResult(new Intent(mContext, LoginActivity.class), 4);
				}
				dialog.dismiss();
			}
		});
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(mContext, getString(R.string.confirm_exit)+getString(R.string.app_name), Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				finish();
			}
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivRefresh:
			Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.round_animation);
			ivRefresh.startAnimation(animation);
			startLocation();
			break;
		case R.id.reLive:
//			loginDialog();
			Toast.makeText(mContext, "正在研发中", Toast.LENGTH_SHORT).show();
			break;
		case R.id.ivPerson:
			startActivity(new Intent(mContext, PersonCenterActivity.class));
			break;
		case R.id.reCamera:
			cameraDialog();
			break;
		case R.id.reVideo:
			startActivity(new Intent(mContext, VideoWallActivity.class));//视频墙
			break;
		case R.id.reMeet:
			if (flag == 1) {
				startActivity(new Intent(mContext, PersonCenterActivity.class));
			}else if (flag == 2){
//				startActivity(new Intent(MainActivity.this, XViewLoginActivity.class));
			}
			break;

		default:
			break;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 0:
				startActivity(new Intent(mContext, CameraActivity.class));
				break;
			case 1:
				if (data != null) {
					Uri uri = data.getData();
					String filePath = GetPathFromUri4kitkat.getPath(mContext, uri);
					if (filePath == null) {
						Toast.makeText(mContext, getString(R.string.not_found_video), Toast.LENGTH_SHORT).show();    
						return;
					}
					
					//跳转到预览视频界面
					PhotoDto dto = new PhotoDto();
					dto.setLocation(getString(R.string.no_location));
					dto.setWorkTime(sdf2.format(System.currentTimeMillis()));
					dto.setVideoUrl(filePath);
					Intent intent = new Intent(mContext, DisplayVideoActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("data", dto);
					intent.putExtras(bundle);
					startActivity(intent);
				}else {
					Toast.makeText(mContext, getString(R.string.not_found_video), Toast.LENGTH_SHORT).show();    
					return;
				}
				break;
			case 2:
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			    i.setType("video/*");
			    startActivityForResult(i, 1);
				break;
			case 3:
				if (data != null) {
					Uri uri = data.getData();
					String filePath = GetPathFromUri4kitkat.getPath(mContext, uri);
					if (filePath == null) {
						Toast.makeText(mContext, getString(R.string.not_found_pic), Toast.LENGTH_SHORT).show();    
						return;
					}
					
					//跳转到预览图片界面
					List<PhotoDto> selectList = new ArrayList<>();
					PhotoDto dto = new PhotoDto();
				    dto.setState(true);
					dto.setWorkstype("imgs");
				    dto.imgUrl = filePath;
				    selectList.add(dto);
					Intent intent = new Intent(mContext, DisplayPictureActivity.class);
				    intent.putExtra("cityName", getString(R.string.no_location));
				    intent.putExtra("takeTime", sdf2.format(System.currentTimeMillis()));
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList("selectList", (ArrayList<? extends Parcelable>) selectList);
					intent.putExtras(bundle);
					startActivity(intent);
				}else {
					Toast.makeText(mContext, getString(R.string.not_found_pic), Toast.LENGTH_SHORT).show();    
					return;
				}
				break;
			case 4:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
			    startActivityForResult(intent, 3);
				break;

			default:
				break;
			}
		}
	}

}
