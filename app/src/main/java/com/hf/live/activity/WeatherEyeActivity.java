package com.hf.live.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.hf.live.R;
import com.hf.live.util.CustomHttpClient;

/**
 * 天气网眼
 * @author shawn_sun
 *
 */

public class WeatherEyeActivity extends BaseActivity implements OnClickListener, OnMarkerClickListener, 
InfoWindowAdapter, OnInfoWindowClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private AMap aMap = null;
	private MapView mapView = null;
	private String url = "http://channellive2.tianqi.cn/Weather/work/getNetEyeInfos";
	private ProgressBar progressBar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_eye);
		mContext = this;
		initWidget();
		initAmap(savedInstanceState);
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("天气网眼在线点播");
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
	}
	
	private void initAmap(Bundle savedInstanceState) {
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);
		if (aMap == null) {
			aMap = mapView.getMap();
		}
		aMap.moveCamera(CameraUpdateFactory.zoomTo(4.0f));
		aMap.getUiSettings().setZoomControlsEnabled(false);
		aMap.setOnMarkerClickListener(this);
		aMap.setInfoWindowAdapter(this);
		aMap.setOnInfoWindowClickListener(this);
		
		asyncQuery(url);
	}
	
	@Override
	public boolean onMarkerClick(Marker arg0) {
		if (arg0 != null) {
			arg0.showInfoWindow();
		}
		return true;
	}
	
	@Override
	public View getInfoContents(Marker arg0) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.marker_info, null);
		TextView tvTitle = (TextView) view.findViewById(R.id.tvTitle);
		if (!TextUtils.isEmpty(arg0.getTitle())) {
			tvTitle.setText(arg0.getTitle());
		}
		return view;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}
	
	@Override
	public void onInfoWindowClick(Marker arg0) {
		Intent intent = new Intent(mContext, VideoViewActivity.class);
		intent.putExtra("url", arg0.getSnippet());
		startActivity(intent);
	}
	
	/**
	 * 异步请求
	 */
	private void asyncQuery(String requestUrl) {
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("GET");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "GET";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask() {
		}
		
		@Override
		protected String doInBackground(String... url) {
			String result = null;
			if (method.equalsIgnoreCase("POST")) {
				result = CustomHttpClient.post(url[0], nvpList);
			} else if (method.equalsIgnoreCase("GET")) {
				result = CustomHttpClient.get(url[0]);
			}
			return result;
		}

		@Override
		protected void onPostExecute(String requestResult) {
			super.onPostExecute(requestResult);
			progressBar.setVisibility(View.GONE);
			if (requestResult != null) {
				try {
					aMap.clear();
					JSONArray array = new JSONArray(requestResult);
					for (int i = 0; i < array.length(); i++) {
						JSONObject obj = array.getJSONObject(i);
						String url = null;
						String name = null;
						String lat = null;
						String lng = null;
						if (!obj.isNull("url")) {
							url = obj.getString("url");
						}
						if (!obj.isNull("name")) {
							name = obj.getString("name");
						}
						if (!obj.isNull("lat")) {
							lat = obj.getString("lat");
						}
						if (!obj.isNull("lon")) {
							lng = obj.getString("lon");
						}
						
						MarkerOptions options = new MarkerOptions();
						options.title(name);
						options.snippet(url);
						options.anchor(0.5f, 0.5f);
						LatLng latLng = null;
						if (!TextUtils.isEmpty(lat) && !TextUtils.isEmpty(lng)) {
							latLng = new LatLng(Double.valueOf(lat), Double.valueOf(lng));
							options.position(latLng);
						}
						options.icon(BitmapDescriptorFactory.fromView(markerView()));
						aMap.addMarker(options);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		@SuppressWarnings("unused")
		private void setParams(NameValuePair nvp) {
			nvpList.add(nvp);
		}

		private void setMethod(String method) {
			this.method = method;
		}

		private void setTimeOut(int timeOut) {
			CustomHttpClient.TIME_OUT = timeOut;
		}

		/**
		 * 取消当前task
		 */
		@SuppressWarnings("unused")
		private void cancelTask() {
			CustomHttpClient.shuttdownRequest();
			this.cancel(true);
		}
	}
	
	private View markerView() {      
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.weather_eye_marker, null);
		if (view == null) {
			return null;
		}
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}

}
