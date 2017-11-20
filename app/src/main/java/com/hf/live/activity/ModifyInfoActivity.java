package com.hf.live.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.common.CONST;
import com.hf.live.R;
import com.hf.live.util.CustomHttpClient;

/**
 * 修改用户信息
 * @author shawn_sun
 *
 */

public class ModifyInfoActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvControl = null;
	private EditText etContent = null;
	private ImageView ivClear = null;
	private String title = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_info);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		etContent = (EditText) findViewById(R.id.etContent);
		etContent.addTextChangedListener(watcher);
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);
		tvControl.setVisibility(View.VISIBLE);
		tvControl.setTextColor(0xff960c1b);
		tvControl.setText("保存");
		ivClear = (ImageView) findViewById(R.id.ivClear);
		ivClear.setOnClickListener(this);
		
		if (getIntent().hasExtra("title")) {
			title = getIntent().getStringExtra("title");
			if (title != null) {
				tvTitle.setText(title);
			}
		}
		
		if (getIntent().hasExtra("content")) {
			String content = getIntent().getStringExtra("content");
			if (content != null) {
				etContent.setText(content);
				etContent.setSelection(content.length());
			}
		}
	}
	
	/**
	 * 监听etcontent内容变化
	 */
	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}
		@Override
		public void afterTextChanged(Editable arg0) {
			if (!TextUtils.isEmpty(etContent.getText().toString())) {
				ivClear.setVisibility(View.VISIBLE);
			}else {
				ivClear.setVisibility(View.GONE);
			}
		}
	};
	
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
			NameValuePair pair1 = new BasicNameValuePair("token", TOKEN);
			NameValuePair pair2 = null;
			if (TextUtils.equals(title, "昵称")) {
				pair2 = new BasicNameValuePair("nickname", etContent.getText().toString().trim());
			}else if (TextUtils.equals(title, "邮箱")) {
				pair2 = new BasicNameValuePair("mail", etContent.getText().toString().trim());
			}else if (TextUtils.equals(title, "单位名称")) {
				pair2 = new BasicNameValuePair("department", etContent.getText().toString().trim());
			}
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
			if (requestResult != null) {
				try {
					JSONObject object = new JSONObject(requestResult);
					if (object != null) {
						if (!object.isNull("status")) {
							int status = object.getInt("status");
							if (status == 1) {//成功
								if (!object.isNull("info")) {
									JSONObject obj = new JSONObject(object.getString("info"));
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
									
									//把用户信息保存在sharedPreferance里
									SharedPreferences sharedPreferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
									Editor editor = sharedPreferences.edit();
									editor.putString(UserInfo.oldUserName, OLDUSERNAME);
									editor.putString(UserInfo.userName, USERNAME);
									editor.putString(UserInfo.groupId, GROUPID);
									editor.putString(UserInfo.token, TOKEN);
									editor.putString(UserInfo.points, POINTS);
									editor.putString(UserInfo.photo, PHOTO);
									editor.putString(UserInfo.nickName, NICKNAME);
									editor.putString(UserInfo.mail, MAIL);
									editor.putString(UserInfo.unit, UNIT);
									editor.commit();
									
									setResult(RESULT_OK);
									finish();
								}
							}else {
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
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivClear:
			etContent.setText("");
			break;
		case R.id.tvControl:
			if (!TextUtils.isEmpty(etContent.getText().toString().trim())) {
				asyncQuery(CONST.MODIFY_USERINFO_URL);
			}else {
				if (title != null) {
					Toast.makeText(mContext, "请输入"+title, Toast.LENGTH_SHORT).show();
				}
			}
			break;

		default:
			break;
		}
	}
	
}
