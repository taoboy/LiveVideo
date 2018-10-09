package com.hf.live.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.util.CommonUtil;
import com.hf.live.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;


/**
 * 欢迎界面
 */

public class WelcomeActivity extends BaseActivity{
	
	private Context mContext = null;
	private MyBroadCastReceiver mRecerver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		mContext = this;
		initBroadCast();

		//获取用户信息
		MyApplication.getUserInfo(mContext);

	}

	private void initBroadCast() {
		mRecerver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");//网络状态监听
		registerReceiver(mRecerver, intentFilter);
	}

	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager connectionManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isAvailable()) {
				switch (networkInfo.getType()) {
					case TYPE_MOBILE:
						refresh();
//						Toast.makeText(context, "正在使用2G/3G/4G网络", Toast.LENGTH_SHORT).show();
						break;
					case TYPE_WIFI:
						refresh();
//						Toast.makeText(context, "正在使用wifi上网", Toast.LENGTH_SHORT).show();
						break;
					default:
						break;
				}
			} else {
				Toast.makeText(context, "当前无网络连接", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mRecerver != null) {
			unregisterReceiver(mRecerver);
		}
	}

	private void refresh() {
		//判断是否显示引导页面
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				SharedPreferences sharedGuide = getSharedPreferences(CONST.SHOWGUIDE, Context.MODE_PRIVATE);
				String version = sharedGuide.getString(CONST.VERSION, "");
				if (!TextUtils.equals(version, CommonUtil.getVersion(mContext))) {
					startActivity(new Intent(getApplication(), GuideActivity.class));
					finish();
				}else {
					OkHttpUserinfo();
				}
			}
		}, 1000);
	}

	/**
	 * 刷新用户信息，主要为了刷新token
	 */
	private void OkHttpUserinfo() {
		if (!TextUtils.isEmpty(MyApplication.TOKEN)) {
			final String url = "http://channellive2.tianqi.cn/Weather/User/getUser2";//刷新token
			FormBody.Builder builder = new FormBody.Builder();
			builder.add("token", MyApplication.TOKEN);
			final RequestBody requestBody = builder.build();
			new Thread(new Runnable() {
				@Override
				public void run() {
					OkHttpUtil.enqueue(new Request.Builder().url(url).post(requestBody).build(), new Callback() {
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
														if (!object.isNull("info")) {
															JSONObject obj = object.getJSONObject("info");
															if (!obj.isNull("token")) {
																MyApplication.TOKEN = obj.getString("token");
															}
															if (!obj.isNull("uid")) {
																MyApplication.UID = obj.getString("uid");
															}
															if (!obj.isNull("phonenumber")) {
																MyApplication.USERNAME = obj.getString("phonenumber");
															}
															if (!obj.isNull("username")) {
																MyApplication.OLDUSERNAME = obj.getString("username");
															}
															if (!obj.isNull("nickname")) {
																MyApplication.NICKNAME = obj.getString("nickname");
															}
															if (!obj.isNull("mail")) {
																MyApplication.MAIL = obj.getString("mail");
															}
															if (!obj.isNull("department")) {
																MyApplication.UNIT = obj.getString("department");
															}
															if (!obj.isNull("groupid")) {
																MyApplication.GROUPID = obj.getString("groupid");
															}
															if (!obj.isNull("points")) {
																MyApplication.POINTS = obj.getString("points");
															}
															if (!obj.isNull("photo")) {
																MyApplication.PHOTO = obj.getString("photo");
																if (!TextUtils.isEmpty(MyApplication.PHOTO)) {
																	CommonUtil.OkHttpLoadPortrait(WelcomeActivity.this, MyApplication.PHOTO);
																}
															}

															//活动
															if (!obj.isNull("type")) {
																MyApplication.TYPE = obj.getString("type");
															}
															if (!obj.isNull("college")) {
																MyApplication.COLLEGE = obj.getString("college");
															}
															if (!obj.isNull("major")) {
																MyApplication.MAJOR = obj.getString("major");
															}
															if (!obj.isNull("votes")) {
																MyApplication.VOTES = obj.getString("votes");
															}
															if (!obj.isNull("code")) {
																MyApplication.CODE = obj.getString("code");
															}

															MyApplication.saveUserInfo(mContext);

															startActivity(new Intent(mContext, MainActivity2.class));
															finish();

														}
													}else if (status == 401) {//token无效
														startActivity(new Intent(mContext, LoginActivity.class));
														finish();
													}else {
														//失败
														if (!object.isNull("msg")) {
															String msg = object.getString("msg");
															if (msg != null) {
																Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
															}
														}
														startActivity(new Intent(mContext, LoginActivity.class));
														finish();
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
		}else {
			startActivity(new Intent(mContext, LoginActivity.class));
			finish();
		}
	}
	
	@Override
	public boolean onKeyDown(int KeyCode, KeyEvent event){
		if (KeyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyDown(KeyCode, event);
	}
	
}
