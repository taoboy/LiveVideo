package com.hf.live.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.common.CONST;
import com.hf.live.R;
import com.hf.live.util.CommonUtil;
import com.hf.live.util.CustomHttpClient;
import com.hf.live.view.CircleImageView;
import com.hf.live.view.MyDialog;

/**
 * 选择新用户或者老用户
 * @author shawn_sun
 *
 */

public class SelecteUserActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvNew = null;
	private TextView tvOld = null;
	private String oldUserName = null;
	private String oldPwd = null;
	private String phonenumber = null, userName = null, nickName= null, mail = null, photo = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_user);
		mContext = this;
		initWidget();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("选择用户");
		tvNew = (TextView) findViewById(R.id.tvNew);
		tvNew.setOnClickListener(this);
		tvOld = (TextView) findViewById(R.id.tvOld);
		tvOld.setOnClickListener(this);
	}
	
	/**
	 * 登录、绑定账号
	 */
	private void asyncQueryLoginBind(String requestUrl) {
		HttpAsyncTaskLogin task = new HttpAsyncTaskLogin();
		task.setMethod("POST");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTaskLogin extends AsyncTask<String, Void, String> {
		private String method = "POST";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTaskLogin() {
			transParams();
		}
		
		/**
		 * 传参数
		 */
		private void transParams() {
			NameValuePair pair1 = new BasicNameValuePair("phonenumber", USERNAME);
	        NameValuePair pair2 = new BasicNameValuePair("token", TOKEN);
			nvpList.add(pair1);
			nvpList.add(pair2);
			
			if (!TextUtils.isEmpty(oldUserName)) {
	        	NameValuePair pair3 = new BasicNameValuePair("username", oldUserName);
	        	nvpList.add(pair3);
			}
			if (!TextUtils.isEmpty(oldPwd)) {
				NameValuePair pair4 = new BasicNameValuePair("passwd", oldPwd);
				nvpList.add(pair4);
			}
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
			cancelDialog();
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						if (!object.isNull("status")) {
							int status  = object.getInt("status");
							if (status == 401) {//401新增账号成功
								parseUserinfo(object);
							}else if (status == 403) {//403绑定账号成功
								parseUserinfo(object);
								if (!object.isNull("msg")) {
									String msg = object.getString("msg");
									if (msg != null) {
										Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
									}
								}
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
	
	/**
	 * 解析用户数据
	 */
	private void parseUserinfo(JSONObject object) {
		try {
			if (!object.isNull("info")) {
				JSONObject obj = new JSONObject(object.getString("info"));
				if (!obj.isNull("groupid")) {
					GROUPID = obj.getString("groupid");
				}
				if (!obj.isNull("token")) {
					TOKEN = obj.getString("token");
				}
				if (!obj.isNull("phonenumber")) {
					USERNAME = obj.getString("phonenumber");
				}
				if (!obj.isNull("points")) {
					POINTS = obj.getString("points");
				}
				if (!obj.isNull("photo")) {
					PHOTO = obj.getString("photo");
					if (!TextUtils.isEmpty(PHOTO)) {
						downloadPortrait(PHOTO, CONST.PORTRAIT_ADDR, null);
					}
				}

				CommonUtil.saveUserInfo(mContext);

				startActivity(new Intent(mContext, MainActivity.class));
				finish();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 下载头像保存在本地
	 */
	private void downloadPortrait(String imgUrl, final String portraitName, final CircleImageView imageView) {
		AsynLoadTask task = new AsynLoadTask(new AsynLoadCompleteListener() {
			@Override
			public void loadComplete(Bitmap bitmap) {
				if (imageView != null && bitmap != null) {
					imageView.setImageBitmap(bitmap);
				}
				
				FileOutputStream fos = null;
				try {
					File files = new File(CONST.SDCARD_PATH);
					if (!files.exists()) {
						files.mkdirs();
					}
					
					fos = new FileOutputStream(portraitName);
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
		public void loadComplete(Bitmap bitmap);
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
	
	/**
	 * 验证登录信息
	 */
	private boolean checkInfo() {
		if (TextUtils.isEmpty(oldUserName)) {
			Toast.makeText(mContext, "请输入手机号码", Toast.LENGTH_SHORT).show();
			return false;
		}
		if (TextUtils.isEmpty(oldPwd)) {
			Toast.makeText(mContext, "请输入手机验证码", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	/**
	 * 绑定账号对话框
	 */
	private void dialogBindUser() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_bind_user, null);
		final EditText etUserName = (EditText) view.findViewById(R.id.etUserName);
		final EditText etPwd = (EditText) view.findViewById(R.id.etPwd);
		TextView tvSure = (TextView) view.findViewById(R.id.tvSure);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvSure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				oldUserName = etUserName.getText().toString().trim();
				oldPwd = etPwd.getText().toString().trim();
				if (checkInfo()) {
					dialog.dismiss();
					showDialog();
					asyncQueryOldLogin("http://channellive2.tianqi.cn/weather/user/Login");
				}
			}
		});
	}
	
	/**
	 * 异步请求
	 */
	private void asyncQueryOldLogin(String requestUrl) {
		HttpAsyncTaskOldLogin task = new HttpAsyncTaskOldLogin();
		task.setMethod("POST");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTaskOldLogin extends AsyncTask<String, Void, String> {
		private String method = "POST";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTaskOldLogin() {
			transParams();
		}
		
		/**
		 * 传参数
		 */
		private void transParams() {
			NameValuePair pair1 = new BasicNameValuePair("username", oldUserName);
	        NameValuePair pair2 = new BasicNameValuePair("passwd", oldPwd);
			nvpList.add(pair1);
			nvpList.add(pair2);
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
			cancelDialog();
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						if (!object.isNull("status")) {
							int status  = object.getInt("status");
							if (status == 1) {//成功
								if (!object.isNull("info")) {
									JSONObject obj = new JSONObject(object.getString("info"));
									if (!obj.isNull("phonenumber")) {
										phonenumber = obj.getString("phonenumber");
									}
									if (!obj.isNull("mail")) {
										mail = obj.getString("mail");
									}
									if (!obj.isNull("nickname")) {
										nickName = obj.getString("nickname");
									}
									if (!obj.isNull("username")) {
										userName = obj.getString("username");
									}
									if (!obj.isNull("photo")) {
										photo = obj.getString("photo");
									}
									
									dialogConfirmInfo();
								}
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
	
	/**
	 * 确认用户信息对话框
	 */
	private void dialogConfirmInfo() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_confirm_info, null);
		CircleImageView ivPortrait = (CircleImageView) view.findViewById(R.id.ivPortrait);
		TextView tvNickName = (TextView) view.findViewById(R.id.tvNickName);
		TextView tvUserName = (TextView) view.findViewById(R.id.tvUserName);
		TextView tvPhone = (TextView) view.findViewById(R.id.tvPhone);
		TextView tvMail = (TextView) view.findViewById(R.id.tvMail);
		TextView tvBack = (TextView) view.findViewById(R.id.tvBack);
		TextView tvSure = (TextView) view.findViewById(R.id.tvSure);
		
		if (!TextUtils.isEmpty(photo)) {
			downloadPortrait(photo, CONST.OLD_PORTRAIT_ADDR, ivPortrait);
		}
		
		if (nickName != null) {
			tvNickName.setText(nickName);
		}
		if (userName != null) {
			tvUserName.setText(userName);
		}
		if (phonenumber != null) {
			tvPhone.setText(phonenumber);
		}
		if (mail != null) {
			tvMail.setText(mail);
		}
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		tvSure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				showDialog();
				asyncQueryLoginBind("http://channellive2.tianqi.cn/Weather/User/Login3Bind");
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.tvNew:
			showDialog();
			asyncQueryLoginBind("http://channellive2.tianqi.cn/Weather/User/Login3Bind");
			break;
		case R.id.tvOld:
			dialogBindUser();
			break;

		default:
			break;
		}
	}
	
}
