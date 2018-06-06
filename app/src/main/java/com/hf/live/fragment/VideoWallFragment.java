package com.hf.live.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.ScaleAnimation;
import com.hf.live.R;
import com.hf.live.activity.OnlinePictureActivity;
import com.hf.live.activity.OnlineVideoActivity;
import com.hf.live.activity.PersonCenterActivity;
import com.hf.live.activity.SearchVideoActivity;
import com.hf.live.adapter.VideoWallAdapter;
import com.hf.live.adapter.VideoWallMapAdapter;
import com.hf.live.common.CONST;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;
import com.hf.live.util.OkHttpUtil;
import com.hf.live.util.WeatherUtil;
import com.hf.live.view.MyDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.weather.api.WeatherAPI;
import cn.com.weather.beans.Weather;
import cn.com.weather.constants.Constants;
import cn.com.weather.listener.AsyncResponseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 视频墙列表
 * @author shawn_sun
 *
 */

public class VideoWallFragment extends Fragment implements View.OnClickListener, AMapLocationListener, AMap.OnCameraChangeListener, AMap.OnMarkerClickListener,
		AMap.OnMapClickListener, AMap.InfoWindowAdapter{

	private ImageView ivSearch, ivPerson;
	private AMapLocationClientOption mLocationOption = null;//声明mLocationOption对象
	private AMapLocationClient mLocationClient = null;//声明AMapLocationClient类对象
	private TextView tvMap, tvList, tvPosition, tvPhe, tvTemp, tvWind;
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局
	private ListView mListView = null;
	private VideoWallAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int page = 1;
	private int pageSize = 20;
	private MapView mapView = null;//高德地图
	private AMap aMap = null;//高德地图
	private float zoom = 3.5f, proLevel = 5.0f, cityLevel = 7.0f;
	private List<PhotoDto> mapList = new ArrayList<>();
	private Map<String, List<PhotoDto>> proMap = new HashMap<>();
	private Map<String, List<PhotoDto>> cityMap = new HashMap<>();
	private List<Marker> markers = new ArrayList<>();
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
	private ListView listView2 = null;
	private VideoWallMapAdapter adapter2 = null;
	private List<PhotoDto> list2 = new ArrayList<>();
	private LinearLayout llSelect, llPhe;
	private RelativeLayout reForecast;
	private TextView tv1, tv2, tv3, tv4;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_wall, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRefreshLayout(view);
		initAmap(view, savedInstanceState);
		initWidget(view);
		initListView(view);
		initListView2(view);
	}

	private void initWidget(View view) {
		tvMap = (TextView) view.findViewById(R.id.tvMap);
		tvMap.setOnClickListener(this);
		tvList = (TextView) view.findViewById(R.id.tvList);
		tvList.setOnClickListener(this);
		ivSearch = (ImageView) view.findViewById(R.id.ivSearch);
		ivSearch.setOnClickListener(this);
		ivPerson = (ImageView) view.findViewById(R.id.ivPerson);
		ivPerson.setOnClickListener(this);
		tvPosition = (TextView) view.findViewById(R.id.tvPosition);
		tvPhe = (TextView) view.findViewById(R.id.tvPhe);
		tvTemp = (TextView) view.findViewById(R.id.tvTemp);
		tvWind = (TextView) view.findViewById(R.id.tvWind);
		llSelect = (LinearLayout) view.findViewById(R.id.llSelect);
		llPhe = (LinearLayout) view.findViewById(R.id.llPhe);
		reForecast = (RelativeLayout) view.findViewById(R.id.reForecast);
		tv1 = (TextView) view.findViewById(R.id.tv1);
		tv1.setOnClickListener(this);
		tv2 = (TextView) view.findViewById(R.id.tv2);
		tv2.setOnClickListener(this);
		tv3 = (TextView) view.findViewById(R.id.tv3);
		tv3.setOnClickListener(this);
		tv4 = (TextView) view.findViewById(R.id.tv4);
		tv4.setOnClickListener(this);

		startLocation();

		refresh();
	}

	/**
	 * 获取地图上直报数据
	 * @param day 天数
	 */
	private void getMapData(int day) {
		long eTime = new Date().getTime();
		long sTime = eTime-day*86400000L;
		String startTime = sdf1.format(new Date(sTime));
		String endTime = sdf1.format(new Date(eTime));
		OkHttpMap(CONST.GET_VIDEO_PIC_URL, startTime, endTime);
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
		refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 300);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refresh();
			}
		});
	}

	private void refresh() {
		startLocation();

		mList.clear();
		page = 1;
		OkHttpVideoList(CONST.GET_VIDEO_PIC_URL);
	}

	/**
	 * 开始定位
	 */
	private void startLocation() {
		mLocationOption = new AMapLocationClientOption();//初始化定位参数
		mLocationClient = new AMapLocationClient(getActivity());//初始化定位
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
		String position = amapLocation.getAoiName();
		if (TextUtils.isEmpty(position)) {
			position = amapLocation.getStreet()+amapLocation.getStreetNum();
		}
		position = amapLocation.getCity()+amapLocation.getDistrict()+position;
		tvPosition.setText(position);

		getWeatherInfo(amapLocation.getLatitude(), amapLocation.getLongitude());
	}

	/**
	 * 获取天气数据
	 * @param lat
	 * @param lng
	 */
	private void getWeatherInfo(final double lat, final double lng) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				WeatherAPI.getGeo(getActivity(),lng+"", lat+"", new AsyncResponseHandler(){
					@Override
					public void onComplete(final JSONObject content) {
						super.onComplete(content);
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!content.isNull("geo")) {
									try {
										JSONObject geoObj = content.getJSONObject("geo");
										if (!geoObj.isNull("id")) {
											String cityId = geoObj.getString("id");
											if (cityId != null) {
												WeatherAPI.getWeather2(getActivity(), cityId, Constants.Language.ZH_CN, new AsyncResponseHandler() {
													@Override
													public void onComplete(Weather content) {
														super.onComplete(content);
														if (content != null) {
															try {
																JSONObject object = content.getWeatherFactInfo();//实况信息
																if (!object.isNull("l5")) {
																	String weatherCode = WeatherUtil.lastValue(object.getString("l5"));
																	if (weatherCode != null) {
																		tvPhe.setText(getString(WeatherUtil.getWeatherId(Integer.valueOf(weatherCode))));
																	}
																}
																if (!object.isNull("l1")) {
																	String factTemp = WeatherUtil.lastValue(object.getString("l1"));
																	if (factTemp != null) {
																		tvTemp.setText(factTemp+"℃");
																	}
																}
																if (!object.isNull("l4") && !object.isNull("l3")) {
																	String windDir = WeatherUtil.lastValue(object.getString("l4"));
																	String windForce = WeatherUtil.lastValue(object.getString("l3"));
																	if (windDir != null && windForce != null) {
																		tvWind.setText(getString(WeatherUtil.getWindDirection(Integer.valueOf(windDir))) + WeatherUtil.getFactWindForce(Integer.valueOf(windForce)));
																	}
																}
																llPhe.setVisibility(View.VISIBLE);
															} catch (JSONException e) {
																e.printStackTrace();
															}

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
						});
					}

					@Override
					public void onError(Throwable error, String content) {
						super.onError(error, content);
					}
				});
			}
		}).start();
	}

	/**
	 * 初始化高德地图
	 */
	private void initAmap(View view, Bundle bundle) {
		mapView = (MapView) view.findViewById(R.id.mapView);
		mapView.onCreate(bundle);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.926628, 105.178100), zoom));
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.getUiSettings().setRotateGesturesEnabled(false);
		aMap.setMapType(AMap.MAP_TYPE_NIGHT);
		aMap.setOnMapClickListener(this);
		aMap.setOnMarkerClickListener(this);
		aMap.setInfoWindowAdapter(this);
		aMap.setOnCameraChangeListener(this);
	}

	/**
	 * 获取地图视频列表
	 */
	private void OkHttpMap(final String url, String startTime, String endTime) {
		if (listView2 != null && listView2.getVisibility() == View.VISIBLE) {
			hideAnimation(listView2);
			listView2.setVisibility(View.GONE);
		}

		FormBody.Builder builder = new FormBody.Builder();
		builder.add("appid", CONST.APPID);
		builder.add("starttime", startTime);
		builder.add("endtime", endTime);
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//成功
													if (!object.isNull("info")) {
														aMap.clear();
														proMap.clear();
														cityMap.clear();
														mapList.clear();
														JSONArray array = object.getJSONArray("info");
														for (int i = 0; i < array.length(); i++) {
															JSONObject obj = array.getJSONObject(i);
															PhotoDto dto = new PhotoDto();
															if (!obj.isNull("id")) {
																dto.videoId = obj.getString("id");
															}
															if (!obj.isNull("title")) {
																dto.title = obj.getString("title");
															}
															if (!obj.isNull("content")) {
																dto.content = obj.getString("content");
															}
															if (!obj.isNull("create_time")) {
																dto.createTime = obj.getString("create_time");
															}
															if (!obj.isNull("location")) {
																dto.location = obj.getString("location");
															}
															if (!obj.isNull("citycode")) {
																dto.adcode = obj.getString("citycode");
															}
															if (!obj.isNull("latlon")) {
																String latLng = obj.getString("latlon");
																if (!TextUtils.isEmpty(latLng)) {
																	String[] latlon = latLng.split(",");
																	dto.lat = latlon[0];
																	dto.lng = latlon[1];
																}
															}
															if (!obj.isNull("nickname")) {
																dto.nickName = obj.getString("nickname");
															}
															if (!obj.isNull("username")) {
																dto.userName = obj.getString("username");
															}
															if (!obj.isNull("phonenumber")) {
																dto.phoneNumber = obj.getString("phonenumber");
															}
															if (!obj.isNull("praise")) {
																dto.praiseCount = obj.getString("praise");
															}
															if (!obj.isNull("browsecount")) {
																dto.playCount = obj.getString("browsecount");
															}
															if (!obj.isNull("comments")) {
																dto.commentCount = obj.getString("comments");
															}
															if (!obj.isNull("work_time")) {
																dto.workTime = obj.getString("work_time");
															}
															if (!obj.isNull("workstype")) {
																dto.workstype = obj.getString("workstype");
															}
															if (!obj.isNull("weather_flag")) {
																dto.weatherFlag = obj.getString("weather_flag");
															}
															if (!obj.isNull("other_flags")) {
																dto.otherFlag = obj.getString("other_flags");
															}
															if (!obj.isNull("worksinfo")) {
																JSONObject workObj = new JSONObject(obj.getString("worksinfo"));
																//视频
																if (!workObj.isNull("video")) {
																	JSONObject video = workObj.getJSONObject("video");
																	if (!video.isNull("ORG")) {//腾讯云结构解析
																		JSONObject ORG = video.getJSONObject("ORG");
																		if (!ORG.isNull("url")) {
																			dto.videoUrl = ORG.getString("url");
																		}
																		if (!video.isNull("SD")) {
																			JSONObject SD = video.getJSONObject("SD");
																			if (!SD.isNull("url")) {
																				dto.sd = SD.getString("url");
																			}
																		}
																		if (!video.isNull("HD")) {
																			JSONObject HD = video.getJSONObject("HD");
																			if (!HD.isNull("url")) {
																				dto.hd = HD.getString("url");
																				dto.videoUrl = HD.getString("url");
																			}
																		}
																		if (!video.isNull("FHD")) {
																			JSONObject FHD = video.getJSONObject("FHD");
																			if (!FHD.isNull("url")) {
																				dto.fhd = FHD.getString("url");
																			}
																		}
																	}else {
																		dto.videoUrl = video.getString("url");
																	}
																}
																if (!workObj.isNull("thumbnail")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("thumbnail"));
																	if (!imgObj.isNull("url")) {
																		dto.imgUrl = imgObj.getString("url");
																	}
																}

																//上传的图片地址，最多9张
																List<String> urlList = new ArrayList<>();
																if (!workObj.isNull("imgs1")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs1"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																		dto.imgUrl = imgObj.getString("url");
																	}
																}
																if (!workObj.isNull("imgs2")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs2"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs3")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs3"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs4")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs4"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs5")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs5"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs6")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs6"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs7")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs7"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs8")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs8"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs9")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs9"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																dto.urlList.addAll(urlList);
															}

															if (!TextUtils.isEmpty(dto.workTime)) {
																mapList.add(dto);
															}

															if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
																if (!TextUtils.isEmpty(dto.adcode) && dto.adcode.length() == 6) {
																	String proKey = dto.adcode.substring(0, 2);
																	if (proMap.containsKey(proKey)) {
																		proMap.get(proKey).add(dto);
																	}else {
																		List<PhotoDto> proList = new ArrayList<>();
																		proList.add(dto);
																		proMap.put(proKey, proList);
																	}

																	String cityKey = dto.adcode.substring(0, 4);
																	if (cityMap.containsKey(cityKey)) {
																		cityMap.get(cityKey).add(dto);
																	}else {
																		List<PhotoDto> cityList = new ArrayList<>();
																		cityList.add(dto);
																		cityMap.put(cityKey, cityList);
																	}

																}
															}
														}
													}

													if (mapList.isEmpty()) {
														Toast.makeText(getActivity(), "没有符合条件的数据！", Toast.LENGTH_LONG).show();
													}

													addMarkers(proMap);

												}else {
													//失败
													if (!object.isNull("msg")) {
														String msg = object.getString("msg");
														if (!TextUtils.isEmpty(msg)) {
															Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
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
		}).start();
	}

	private void addMarkers(Map<String, List<PhotoDto>> map) {
		for (String key : map.keySet()) {
			List<PhotoDto> list = map.get(key);
			if (list.size() > 0) {
				PhotoDto dto = list.get(0);
				if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
					double lat = Double.valueOf(dto.lat);
					double lng = Double.valueOf(dto.lng);
					MarkerOptions options = new MarkerOptions();
					options.title(dto.adcode);
					options.snippet(dto.videoId);
					options.anchor(0.5f, 0.5f);
					options.position(new LatLng(lat, lng));
					options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(list.size())));
					Marker marker = aMap.addMarker(options);
					markers.add(marker);
					markerExpandAnimation(marker);
				}
			}
		}
	}

	private void removeMarkers() {
		for (int i = 0; i < markers.size(); i++) {
			Marker marker = markers.get(i);
			markerColloseAnimation(marker);
			marker.remove();
		}
		markers.clear();
	}

	/**
	 * 给marker添加文字
	 * @param size
	 * @return
	 */
	private View getTextBitmap(int size) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.layout_marker_statistic, null);
		if (view == null) {
			return null;
		}
		ImageView ivMarker = (ImageView) view.findViewById(R.id.ivMarker);
		if (size <= 1) {
			ivMarker.setImageResource(R.drawable.iv_circle_blue);
		}else {
			ivMarker.setImageResource(R.drawable.iv_circle_orange);
		}
		TextView tvName = (TextView) view.findViewById(R.id.tvName);
		tvName.setText(size+"");
		return view;
	}

	private void markerExpandAnimation(Marker marker) {
		ScaleAnimation animation = new ScaleAnimation(0,1,0,1);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(300);
		marker.setAnimation(animation);
		marker.startAnimation();
	}

	private void markerColloseAnimation(Marker marker) {
		ScaleAnimation animation = new ScaleAnimation(1,0,1,0);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(300);
		marker.setAnimation(animation);
		marker.startAnimation();
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		if (listView2.getVisibility() == View.VISIBLE) {
			hideAnimation(listView2);
			listView2.setVisibility(View.GONE);
		}
	}

	@Override
	public void onCameraChangeFinish(CameraPosition cameraPosition) {
		if (zoom == cameraPosition.zoom) {
			return;
		}
		zoom = cameraPosition.zoom;
		removeMarkers();
		if (zoom <= proLevel) {
			addMarkers(proMap);
		}else if (zoom > proLevel && zoom <= cityLevel) {
			addMarkers(cityMap);
		}else {
			for (int i = 0; i < mapList.size(); i++) {
				PhotoDto dto = mapList.get(i);
				if (!TextUtils.isEmpty(dto.lat) && !TextUtils.isEmpty(dto.lng)) {
					double lat = Double.valueOf(dto.lat);
					double lng = Double.valueOf(dto.lng);
					MarkerOptions options = new MarkerOptions();
					options.title(dto.adcode);
					options.snippet(dto.videoId);
					options.anchor(0.5f, 0.5f);
					options.position(new LatLng(lat, lng));
					options.icon(BitmapDescriptorFactory.fromView(getTextBitmap(1)));
					Marker marker = aMap.addMarker(options);
					markers.add(marker);
					markerExpandAnimation(marker);
				}
			}
		}
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}

	@Override
	public View getInfoContents(Marker marker) {
		return null;
	}

	@Override
	public void onMapClick(LatLng latLng) {
		if (listView2.getVisibility() == View.VISIBLE) {
			hideAnimation(listView2);
			listView2.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		list2.clear();
		String proKey = marker.getTitle().substring(0,2);
		String cityKey = marker.getTitle().substring(0,4);
		String disKey = marker.getTitle();
		if (zoom <= proLevel) {
			for (String key : proMap.keySet()) {
				if (TextUtils.equals(proKey, key)) {
					List<PhotoDto> list = proMap.get(key);
					list2.addAll(list);
					break;
				}
			}
		}else if (zoom > proLevel && zoom <= cityLevel) {
			for (String key : cityMap.keySet()) {
				if (TextUtils.equals(cityKey, key)) {
					List<PhotoDto> list = cityMap.get(key);
					list2.addAll(list);
					break;
				}
			}
		}else {
			for (PhotoDto dto : mapList) {
				if (TextUtils.equals(dto.adcode, disKey) && TextUtils.equals(dto.videoId, marker.getSnippet())) {
					list2.add(dto);
					break;
				}
			}
		}
		if (adapter2 != null) {
			adapter2.notifyDataSetChanged();
			setListViewHeight(listView2, list2.size(), 70, 140, 210);
		}

		if (listView2.getVisibility() == View.GONE) {
			showAnimation(listView2);
			listView2.setVisibility(View.VISIBLE);
		}
		return true;
	}

	/**
	 * 向上弹出动画
	 * @param layout
	 */
	private void showAnimation(View layout) {
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, 1f,
				TranslateAnimation.RELATIVE_TO_SELF, 0);
		animation.setDuration(300);
		layout.startAnimation(animation);
	}

	/**
	 * 向下隐藏动画
	 * @param layout
	 */
	private void hideAnimation(View layout) {
		TranslateAnimation animation = new TranslateAnimation(
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, 0,
				TranslateAnimation.RELATIVE_TO_SELF, 1f);
		animation.setDuration(300);
		layout.startAnimation(animation);
	}

	/**
	 * 设置listview高度
	 * @param listView
	 * @param size
	 */
	private void setListViewHeight(ListView listView, int size, int height1, int height2, int height3) {
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		if (size == 1) {
			params.height = (int) CommonUtil.dip2px(getActivity(), height1);
		}else if (size == 2) {
			params.height = (int) CommonUtil.dip2px(getActivity(), height2);
		}else if (size > 2){
			params.height = (int) CommonUtil.dip2px(getActivity(), height3);
		}
		listView.setLayoutParams(params);
	}

	/**
	 * 初始化listview
	 */
	private void initListView2(View view) {
		listView2 = (ListView) view.findViewById(R.id.listView2);
		adapter2 = new VideoWallMapAdapter(getActivity(), list2);
		listView2.setAdapter(adapter2);
		listView2.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PhotoDto dto = list2.get(arg2);
				Intent intent = new Intent();
				if (dto.workstype.equals("imgs")) {
					intent.setClass(getActivity(), OnlinePictureActivity.class);
				}else {
					intent.setClass(getActivity(), OnlineVideoActivity.class);
				}
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}

	/**
	 * 初始化listview
	 */
	private void initListView(View view) {
		mListView = (ListView) view.findViewById(R.id.listView);
		mAdapter = new VideoWallAdapter(getActivity(), mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PhotoDto dto = mList.get(arg2);
				Intent intent = new Intent();
				if (dto.getWorkstype().equals("imgs")) {
					intent.setClass(getActivity(), OnlinePictureActivity.class);
				}else {
					intent.setClass(getActivity(), OnlineVideoActivity.class);
				}
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition() == view.getCount() - 1) {
					page += 1;
					OkHttpVideoList(CONST.GET_VIDEO_PIC_URL);
				}
			}
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
			}
		});
	}

	/**
	 * 获取视频列表
	 */
	private void OkHttpVideoList(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("appid", CONST.APPID);
		builder.add("page", page+"");
		builder.add("pagesize", pageSize+"");
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
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
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (!TextUtils.isEmpty(result)) {
									try {
										JSONObject object = new JSONObject(result);
										if (object != null) {
											if (!object.isNull("status")) {
												int status  = object.getInt("status");
												if (status == 1) {//成功
													if (!object.isNull("info")) {
														JSONArray array = object.getJSONArray("info");
														for (int i = 0; i < array.length(); i++) {
															JSONObject obj = array.getJSONObject(i);
															PhotoDto dto = new PhotoDto();
															if (!obj.isNull("id")) {
																dto.videoId = obj.getString("id");
															}
															if (!obj.isNull("title")) {
																dto.title = obj.getString("title");
															}
															if (!obj.isNull("content")) {
																dto.content = obj.getString("content");
															}
															if (!obj.isNull("create_time")) {
																dto.createTime = obj.getString("create_time");
															}
															if (!obj.isNull("location")) {
																dto.location = obj.getString("location");
															}
															if (!obj.isNull("citycode")) {
																dto.adcode = obj.getString("citycode");
															}
															if (!obj.isNull("nickname")) {
																dto.nickName = obj.getString("nickname");
															}
															if (!obj.isNull("username")) {
																dto.userName = obj.getString("username");
															}
															if (!obj.isNull("phonenumber")) {
																dto.phoneNumber = obj.getString("phonenumber");
															}
															if (!obj.isNull("praise")) {
																dto.praiseCount = obj.getString("praise");
															}
															if (!obj.isNull("browsecount")) {
																dto.playCount = obj.getString("browsecount");
															}
															if (!obj.isNull("comments")) {
																dto.commentCount = obj.getString("comments");
															}
															if (!obj.isNull("work_time")) {
																dto.workTime = obj.getString("work_time");
															}
															if (!obj.isNull("workstype")) {
																dto.workstype = obj.getString("workstype");
															}
															if (!obj.isNull("weather_flag")) {
																dto.weatherFlag = obj.getString("weather_flag");
															}
															if (!obj.isNull("other_flags")) {
																dto.otherFlag = obj.getString("other_flags");
															}
															if (!obj.isNull("worksinfo")) {
																JSONObject workObj = new JSONObject(obj.getString("worksinfo"));
																//视频
																if (!workObj.isNull("video")) {
																	JSONObject video = workObj.getJSONObject("video");
																	if (!video.isNull("ORG")) {//腾讯云结构解析
																		JSONObject ORG = video.getJSONObject("ORG");
																		if (!ORG.isNull("url")) {
																			dto.videoUrl = ORG.getString("url");
																		}
																		if (!video.isNull("SD")) {
																			JSONObject SD = video.getJSONObject("SD");
																			if (!SD.isNull("url")) {
																				dto.sd = SD.getString("url");
																			}
																		}
																		if (!video.isNull("HD")) {
																			JSONObject HD = video.getJSONObject("HD");
																			if (!HD.isNull("url")) {
																				dto.hd = HD.getString("url");
																				dto.videoUrl = HD.getString("url");
																			}
																		}
																		if (!video.isNull("FHD")) {
																			JSONObject FHD = video.getJSONObject("FHD");
																			if (!FHD.isNull("url")) {
																				dto.fhd = FHD.getString("url");
																			}
																		}
																	}else {
																		dto.videoUrl = video.getString("url");
																	}
																}
																if (!workObj.isNull("thumbnail")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("thumbnail"));
																	if (!imgObj.isNull("url")) {
																		dto.imgUrl = imgObj.getString("url");
																	}
																}

																//上传的图片地址，最多9张
																List<String> urlList = new ArrayList<>();
																if (!workObj.isNull("imgs1")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs1"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																		dto.imgUrl = imgObj.getString("url");
																	}
																}
																if (!workObj.isNull("imgs2")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs2"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs3")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs3"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs4")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs4"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs5")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs5"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs6")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs6"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs7")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs7"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs8")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs8"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs9")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs9"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																dto.urlList.addAll(urlList);
															}

															if (!TextUtils.isEmpty(dto.workTime)) {
																mList.add(dto);
															}
														}
													}

													if (mList.isEmpty()) {
														Toast.makeText(getActivity(), "没有符合条件的数据！", Toast.LENGTH_LONG).show();
													}

													if (mList.size() > 0 && mAdapter != null) {
														mAdapter.notifyDataSetChanged();
													}
													refreshLayout.setRefreshing(false);

												}else {
													//失败
													if (!object.isNull("msg")) {
														String msg = object.getString("msg");
														if (!TextUtils.isEmpty(msg)) {
															Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
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
		}).start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ivSearch:
				startActivity(new Intent(getActivity(), SearchVideoActivity.class));
				break;
			case R.id.ivPerson:
				startActivity(new Intent(getActivity(), PersonCenterActivity.class));
				break;
			case R.id.tvList:
				tvMap.setTextColor(getResources().getColor(R.color.text_color4));
				tvList.setTextColor(getResources().getColor(R.color.white));
				tvMap.setBackgroundResource(R.drawable.btn_rb_corner_unselected);
				tvList.setBackgroundResource(R.drawable.btn_lb_corner_selected);
				mapView.setVisibility(View.GONE);
				llSelect.setVisibility(View.GONE);
				reForecast.setVisibility(View.VISIBLE);
				refreshLayout.setVisibility(View.VISIBLE);

				if (mList.isEmpty()) {
					refresh();
				}
				break;
			case R.id.tvMap:
				tvMap.setBackgroundResource(R.drawable.btn_rb_corner_selected);
				tvList.setBackgroundResource(R.drawable.btn_lb_corner_unselected);
				tvMap.setTextColor(getResources().getColor(R.color.white));
				tvList.setTextColor(getResources().getColor(R.color.text_color4));
				mapView.setVisibility(View.VISIBLE);
				llSelect.setVisibility(View.VISIBLE);
				reForecast.setVisibility(View.GONE);
				refreshLayout.setVisibility(View.GONE);

				if (mapList.isEmpty()) {
					getMapData(30);//30天
				}
				break;
			case R.id.tv1:
				tv1.setTextColor(getResources().getColor(R.color.white));
				tv2.setTextColor(getResources().getColor(R.color.text_color4));
				tv3.setTextColor(getResources().getColor(R.color.text_color4));
				tv4.setTextColor(getResources().getColor(R.color.text_color4));
				getMapData(1);//1天
				break;
			case R.id.tv2:
				tv1.setTextColor(getResources().getColor(R.color.text_color4));
				tv2.setTextColor(getResources().getColor(R.color.white));
				tv3.setTextColor(getResources().getColor(R.color.text_color4));
				tv4.setTextColor(getResources().getColor(R.color.text_color4));
				getMapData(7);//7天
				break;
			case R.id.tv3:
				tv1.setTextColor(getResources().getColor(R.color.text_color4));
				tv2.setTextColor(getResources().getColor(R.color.text_color4));
				tv3.setTextColor(getResources().getColor(R.color.white));
				tv4.setTextColor(getResources().getColor(R.color.text_color4));
				getMapData(15);//15天
				break;
			case R.id.tv4:
				tv1.setTextColor(getResources().getColor(R.color.text_color4));
				tv2.setTextColor(getResources().getColor(R.color.text_color4));
				tv3.setTextColor(getResources().getColor(R.color.text_color4));
				tv4.setTextColor(getResources().getColor(R.color.white));
				getMapData(30);//30天
				break;

		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onResume() {
		super.onResume();
		if (mapView != null) {
			mapView.onResume();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (mapView != null) {
			mapView.onPause();
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mapView != null) {
			mapView.onSaveInstanceState(outState);
		}
	}

	/**
	 * 方法必须重写
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mapView != null) {
			mapView.onDestroy();
		}
	}

}
