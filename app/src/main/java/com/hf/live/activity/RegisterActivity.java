package com.hf.live.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.hf.live.common.CONST;
import com.hf.live.R;
import com.hf.live.common.MyApplication;
import com.hf.live.util.CustomHttpClient;
import com.hf.live.view.MyDialog;

public class RegisterActivity extends Activity implements OnClickListener{
	
	private Context mContext = null;
	private ImageView ivBack = null;
	private TextView tvTitle = null;
	private EditText etUserName = null;//用户名
	private EditText etPwd = null;//密码
	private EditText etRepeatPwd = null;//重复密码
	private ImageView ivConfirm = null;//确认
	private MyDialog mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化dialog
	 */
	private void showDialog() {
		mDialog = new MyDialog(mContext);
		if (mDialog != null) {
			mDialog.show();
		}
	}
	private void cancelDialog() {
		if (mDialog != null) {
			mDialog.cancel();
		}
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		ivBack = (ImageView) findViewById(R.id.ivBack);
		ivBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.register));
		etUserName = (EditText) findViewById(R.id.etUserName);
		etPwd = (EditText) findViewById(R.id.etPwd);
		etRepeatPwd = (EditText) findViewById(R.id.etRepeatPwd);
		ivConfirm = (ImageView) findViewById(R.id.ivConfirm);
		ivConfirm.setOnClickListener(this);
	}
	
	/**
	 * 验证注册信息是否合法
	 */
	private boolean checkInfo() {
		String username = etUserName.getText().toString();
		if (TextUtils.isEmpty(username) || username.length() < 6) {
			Toast.makeText(mContext, getResources().getString(R.string.input_username_hint), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		String pwd = etPwd.getText().toString();
		if (TextUtils.isEmpty(pwd) || pwd.length() < 6) {
			Toast.makeText(mContext, getResources().getString(R.string.input_password_hint), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		String repwd = etRepeatPwd.getText().toString();
		if (TextUtils.isEmpty(repwd) || !TextUtils.equals(pwd, repwd)) {
			Toast.makeText(mContext, getResources().getString(R.string.input_password_again), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}
	
	/**
	 * 异步请求
	 */
	private void asyncQuery(String requestUrl) {
		HttpAsyncTask task = new HttpAsyncTask();
		task.setMethod("POST");
		task.setTimeOut(CustomHttpClient.TIME_OUT);
		task.execute(requestUrl);
	}
	
	/**
	 * 异步请求方法
	 * @author dell
	 *
	 */
	private class HttpAsyncTask extends AsyncTask<String, Void, String> {
		private String method = "POST";
		private List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
		
		public HttpAsyncTask() {
			transParams();
		}
		
		/**
		 * 传参数
		 */
		private void transParams() {
			NameValuePair pair1 = new BasicNameValuePair("username", etUserName.getText().toString());
	        NameValuePair pair2 = new BasicNameValuePair("passwd", etPwd.getText().toString());
	        NameValuePair pair3 = new BasicNameValuePair("nickname", "");
	        NameValuePair pair4 = new BasicNameValuePair("mail", "");
	        NameValuePair pair5 = new BasicNameValuePair("phonenumber", "");
	        NameValuePair pair6 = new BasicNameValuePair("sex", "");
			NameValuePair pair7 = new BasicNameValuePair("appid", CONST.APPID);
			nvpList.add(pair1);
			nvpList.add(pair2);
			nvpList.add(pair3);
			nvpList.add(pair4);
			nvpList.add(pair5);
			nvpList.add(pair6);
			nvpList.add(pair7);
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
							int status = object.getInt("status");
							if (status == 1) {//成功
								MyApplication.USERNAME = etUserName.getText().toString();
//								CONST.PASSWORD = etPwd.getText().toString();
								
								setResult(RESULT_OK);
								finish();
							}else {
								//失败
							}
						}
						if (!object.isNull("msg")) {
							String msg = object.getString("msg");
							if (msg != null) {
								Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
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
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivBack:
			finish();
			break;
		case R.id.ivConfirm:
			if (checkInfo()) {
				showDialog();
				asyncQuery(CONST.REGISTER_URL);
			}
			break;

		default:
			break;
		}
	}
	
}
