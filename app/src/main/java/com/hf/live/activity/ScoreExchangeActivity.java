package com.hf.live.activity;

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

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.util.CustomHttpClient;
import com.hf.live.view.MyDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ScoreExchangeActivity extends Activity implements OnClickListener{
	
	private Context mContext = null;
	private ImageView ivBack = null;
	private TextView tvTitle = null;
	private EditText etUserName = null;//用户名
	private EditText etPwd = null;//密码
	private TextView tvExchange = null;
	private MyDialog mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.score_exchange);
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
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.score_exchange));
		ivBack = (ImageView) findViewById(R.id.ivBack);
		ivBack.setOnClickListener(this);
		etUserName = (EditText) findViewById(R.id.etUserName);
		etPwd = (EditText) findViewById(R.id.etPwd);
		tvExchange = (TextView) findViewById(R.id.tvExchange);
		tvExchange.setOnClickListener(this);
	}
	
	/**
	 * 验证注册信息是否合法
	 */
	private boolean checkInfo() {
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			Toast.makeText(mContext, getResources().getString(R.string.input_username), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if (TextUtils.isEmpty(etPwd.getText().toString())) {
			Toast.makeText(mContext, getResources().getString(R.string.input_password), Toast.LENGTH_SHORT).show();
			return false;
		}
//		if (!etPwd.getText().toString().equals(CONST.PASSWORD)) {
//			Toast.makeText(mContext, getResources().getString(R.string.error_pwd), Toast.LENGTH_SHORT).show();
//			return false;
//		}
		
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
			NameValuePair pair1 = new BasicNameValuePair("token", MyApplication.TOKEN);
			NameValuePair pair2 = new BasicNameValuePair("alipay", etUserName.getText().toString());
			NameValuePair pair3 = new BasicNameValuePair("appid", CONST.APPID);
			nvpList.add(pair1);
			nvpList.add(pair2);
			nvpList.add(pair3);
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
								Toast.makeText(mContext, getString(R.string.apply_submited), Toast.LENGTH_SHORT).show();
								finish();
							}else {
								//失败
							}
						}
//						if (!object.isNull("msg")) {
//							String msg = object.getString("msg");
//							if (msg != null) {
//								Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
//							}
//						}
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
		case R.id.tvExchange:
			if (checkInfo()) {
				showDialog();
				asyncQuery(CONST.EXCHANGE_SCORE_URL);
			}
			break;

		default:
			break;
		}
	}
	
}
