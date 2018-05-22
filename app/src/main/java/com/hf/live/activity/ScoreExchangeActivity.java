package com.hf.live.activity;

import android.content.Context;
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
import com.hf.live.util.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 积分兑换
 */
public class ScoreExchangeActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private ImageView ivBack = null;
	private TextView tvTitle = null;
	private EditText etUserName = null;//用户名
	private EditText etPwd = null;//密码
	private TextView tvExchange = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.score_exchange);
		mContext = this;
		initWidget();
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
	 * 积分兑换
	 */
	private void OkHttpScore(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", MyApplication.TOKEN);
		builder.add("alipay", etUserName.getText().toString());
		builder.add("appid", CONST.APPID);
		RequestBody body = builder.build();
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
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (!TextUtils.isEmpty(result)) {
							try {
								cancelDialog();
								JSONObject object = new JSONObject(result);
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
				});
			}
		});
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
				OkHttpScore(CONST.EXCHANGE_SCORE_URL);
			}
			break;

		default:
			break;
		}
	}
	
}
