package com.hf.live.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.CONST;
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

import static com.hf.live.activity.BaseActivity.PHOTO;
import static com.hf.live.activity.BaseActivity.TOKEN;
import static com.hf.live.activity.BaseActivity.USERNAME;
import static com.hf.live.activity.BaseActivity.OLDUSERNAME;
import static com.hf.live.activity.BaseActivity.NICKNAME;
import static com.hf.live.activity.BaseActivity.MAIL;
import static com.hf.live.activity.BaseActivity.UNIT;
import static com.hf.live.activity.BaseActivity.GROUPID;
import static com.hf.live.activity.BaseActivity.POINTS;

/**
 * 欢迎界面
 */

public class WelcomeActivity extends Activity{
	
	private Context mContext = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		mContext = this;

		//获取用户信息
		CommonUtil.getUserInfo(mContext);

		//判断是否显示引导页面
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				SharedPreferences sharedGuide = getSharedPreferences(CONST.SHOWGUIDE, Context.MODE_PRIVATE);
				String version = sharedGuide.getString(CONST.VERSION, "");
				if (!TextUtils.equals(version, CommonUtil.getVersion(getApplicationContext()))) {
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
		if (!TextUtils.isEmpty(TOKEN)) {
			String url = "http://channellive2.tianqi.cn/Weather/User/getUser2";//刷新token
			FormBody.Builder builder = new FormBody.Builder();
			builder.add("token", TOKEN);
			RequestBody requestBody = builder.build();
			OkHttpUtil.enqueue(new Request.Builder().url(url).post(requestBody).build(), new Callback() {
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
										if (!object.isNull("info")) {
											JSONObject obj = object.getJSONObject("info");
											if (!obj.isNull("token")) {
												TOKEN = obj.getString("token");
											}
											if (!obj.isNull("phonenumber")) {
												USERNAME = obj.getString("phonenumber");
											}
											if (!obj.isNull("username")) {
												OLDUSERNAME = obj.getString("username");
											}
											if (!obj.isNull("nickname")) {
												NICKNAME = obj.getString("nickname");
											}
											if (!obj.isNull("mail")) {
												MAIL = obj.getString("mail");
											}
											if (!obj.isNull("department")) {
												UNIT = obj.getString("department");
											}
											if (!obj.isNull("groupid")) {
												GROUPID = obj.getString("groupid");
											}
											if (!obj.isNull("points")) {
												POINTS = obj.getString("points");
											}
											if (!obj.isNull("photo")) {
												PHOTO = obj.getString("photo");
												if (!TextUtils.isEmpty(PHOTO)) {
													downloadPortrait(PHOTO);
												}
											}

											CommonUtil.saveUserInfo(mContext);

											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													startActivity(new Intent(mContext, MainActivity.class));
													finish();
												}
											});

										}
									}else if (status == 401) {//token无效
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												startActivity(new Intent(mContext, LoginActivity.class));
												finish();
											}
										});
									}else {
										//失败
										if (!object.isNull("msg")) {
											final String msg = object.getString("msg");
											if (msg != null) {
												runOnUiThread(new Runnable() {
													@Override
													public void run() {
														Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
													}
												});

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
		}else {
			startActivity(new Intent(mContext, LoginActivity.class));
			finish();
		}
	}
	
	/**
	 * 下载头像保存在本地
	 */
	private void downloadPortrait(String imgUrl) {
		AsynLoadTask task = new AsynLoadTask(new AsynLoadCompleteListener() {
			@Override
			public void loadComplete(Bitmap bitmap) {
				FileOutputStream fos = null;
				try {
					File files = new File(CONST.SDCARD_PATH);
					if (!files.exists()) {
						files.mkdirs();
					}
					
					fos = new FileOutputStream(CONST.PORTRAIT_ADDR);
					if (bitmap != null && fos != null) {
						bitmap.compress(CompressFormat.PNG, 100, fos);
						
						if (bitmap != null && !bitmap.isRecycled()) {
							bitmap.recycle();
							bitmap = null;
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}, imgUrl);  
        task.execute();
	}
	
	private interface AsynLoadCompleteListener {
		void loadComplete(Bitmap bitmap);
	}
    
	private class AsynLoadTask extends AsyncTask<Void, Bitmap, Bitmap> {
		
		private String imgUrl;
		private AsynLoadCompleteListener completeListener;

		private AsynLoadTask(AsynLoadCompleteListener completeListener, String imgUrl) {
			this.imgUrl = imgUrl;
			this.completeListener = completeListener;
		}

		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onProgressUpdate(Bitmap... values) {
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bitmap = CommonUtil.getHttpBitmap(imgUrl);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (completeListener != null) {
				completeListener.loadComplete(bitmap);
            }
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
