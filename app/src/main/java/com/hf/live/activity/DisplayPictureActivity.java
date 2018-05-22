package com.hf.live.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.hf.live.R;
import com.hf.live.adapter.DisplayPictureAdapter;
import com.hf.live.adapter.EventTypeAdapter;
import com.hf.live.adapter.WeatherTypeAdapter;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.dto.PhotoDto;
import com.hf.live.dto.UploadVideoDto;
import com.hf.live.util.OkHttpUtil;
import com.hf.live.view.ScrollviewGridview;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 图片预览并上传
 */

public class DisplayPictureActivity extends BaseActivity implements OnClickListener, AMapLocationListener{

	private Context mContext = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle,tvPositon,tvDate,tvTextCount,tvRemove,tvUpload;
	private ScrollviewGridview mGridView = null;
	private DisplayPictureAdapter mAdapter = null;
	private EditText etTitle,etContent;//标题
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private List<PhotoDto> selectList = new ArrayList<>();
	private ScrollviewGridview gridView1 = null;
	private WeatherTypeAdapter adapter1 = null;
	private List<UploadVideoDto> list1 = new ArrayList<>();
	private String weatherType = "";//天气类型
	private ScrollviewGridview gridView2 = null;
	private EventTypeAdapter adapter2 = null;
	private List<UploadVideoDto> list2 = new ArrayList<>();
	private String eventType = "";//事件类型
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private double lat = 0, lng = 0;
	private String position = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_picture);
		mContext = this;
		initWidget();
		initGridView();
		initGridView1();
		initGridView2();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("图片上传");
		tvPositon = (TextView) findViewById(R.id.tvPosition);
		tvDate = (TextView) findViewById(R.id.tvDate);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvRemove = (TextView) findViewById(R.id.tvRemove);
		tvRemove.setOnClickListener(this);
		tvUpload = (TextView) findViewById(R.id.tvUpload);
		tvUpload.setOnClickListener(this);
		etTitle = (EditText) findViewById(R.id.etTitle);
		etContent = (EditText) findViewById(R.id.etContent);
		etContent.addTextChangedListener(contentWatcher);
		tvTextCount = (TextView) findViewById(R.id.tvTextCount);

		try {
			String takeTime = getIntent().getStringExtra("takeTime");
			if (!TextUtils.isEmpty(takeTime)) {
				tvDate.setText(sdf2.format(sdf1.parse(takeTime)));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		selectList.clear();
		selectList.addAll(getIntent().getExtras().<PhotoDto>getParcelableArrayList("selectList"));

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
	 * 初始化gridview
	 */
	private void initGridView() {
		mGridView = (ScrollviewGridview) findViewById(R.id.gridView);
		mAdapter = new DisplayPictureAdapter(mContext, selectList);
		mGridView.setAdapter(mAdapter);
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
	 * 检查上传标题dialog
	 */
	private void checkTitleDialog() {
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
	 * 上传图片
	 */
	private void OkHttpPostImgs(final String url) {
		showDialog();
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart("appid", CONST.APPID);
        builder.addFormDataPart("token", MyApplication.TOKEN);
        builder.addFormDataPart("workstype", "imgs");
        builder.addFormDataPart("latlon", lat+","+lng);
        builder.addFormDataPart("title", etTitle.getText().toString());
        if (!TextUtils.isEmpty(etContent.getText().toString())) {
            builder.addFormDataPart("content", etContent.getText().toString());
        }
        builder.addFormDataPart("weatherType", weatherType);
        if (!TextUtils.isEmpty(eventType)) {
            builder.addFormDataPart("eventType", eventType);
        }
        builder.addFormDataPart("location", position);

        for (int i = 0; i < selectList.size(); i++) {
            File pictureFile = new File(selectList.get(i).imgUrl);
			builder.addFormDataPart("work_time", sdf2.format(new Date()));
			builder.addFormDataPart("imgs" + Integer.valueOf(i + 1), pictureFile.getName(), RequestBody.create(MediaType.parse("image/*"), pictureFile));
        }

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
								cancelDialog();
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
												cancelDialog();
												Toast.makeText(mContext, "上传成功！", Toast.LENGTH_SHORT).show();

												//发送刷新未上传广播
												Intent intent = new Intent();
												intent.setAction(CONST.REFRESH_NOTUPLOAD);
												sendBroadcast(intent);
												finish();
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
			exitDialog();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			exitDialog();
			break;
		case R.id.tvRemove:
			exitDialog();
			break;
		case R.id.tvUpload:
			if (TextUtils.isEmpty(weatherType) || TextUtils.isEmpty(etTitle.getText().toString())) {
				checkTitleDialog();
			}else {
                OkHttpPostImgs("http://channellive2.tianqi.cn/weather/Work/upload");
			}
			break;

		default:
			break;
		}
	}
}
