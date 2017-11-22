package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.util.CustomHttpClient;
import com.hf.live.util.OkHttpUtil;
import com.hf.live.view.CircleImageView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 个人中心
 */

public class PersonCenterActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private CircleImageView ivPortrait = null;//头像
	private TextView tvUserName = null;//用户名
	private LinearLayout llMyVideo = null;//我的上传
	private LinearLayout llMyMsg = null;//我的消息
	private LinearLayout llMyScore = null;//我的积分
	private LinearLayout llCheck = null;//视频审核
	private LinearLayout llMySetting = null;//设置
	private LinearLayout llMyAbout = null;//关于
	private LinearLayout llResponse = null;//免责申明
	private LinearLayout llOperate = null;//使用说明
	private TextView tvScore = null;//积分
	private RelativeLayout reNewsCount = null;
	private TextView tvNewsCount = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person_center);
		MyApplication.addDestoryActivity(PersonCenterActivity.this, "PersonCenterActivity");
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.my));
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		ivPortrait = (CircleImageView) findViewById(R.id.ivPortrait);
		ivPortrait.setOnClickListener(this);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		llMyVideo = (LinearLayout) findViewById(R.id.llMyVideo);
		llMyVideo.setOnClickListener(this);
		llMyMsg = (LinearLayout) findViewById(R.id.llMyMsg);
		llMyMsg.setOnClickListener(this);
		llMyScore = (LinearLayout) findViewById(R.id.llMyScore);
		llMyScore.setOnClickListener(this);
		llCheck = (LinearLayout) findViewById(R.id.llCheck);
		llCheck.setOnClickListener(this);
		llMySetting = (LinearLayout) findViewById(R.id.llMySetting);
		llMySetting.setOnClickListener(this);
		tvScore = (TextView) findViewById(R.id.tvScore);
		reNewsCount = (RelativeLayout) findViewById(R.id.reNewsCount);
		tvNewsCount = (TextView) findViewById(R.id.tvNewsCount);
		llResponse = (LinearLayout) findViewById(R.id.llResponse);
		llResponse.setOnClickListener(this);
		llMyAbout = (LinearLayout) findViewById(R.id.llMyAbout);
		llMyAbout.setOnClickListener(this);
		llOperate = (LinearLayout) findViewById(R.id.llOperate);
		llOperate.setOnClickListener(this);
		
		refreshUserInfo();
	}
	
	private void refreshUserInfo() {
		getPortrait();
		if (!TextUtils.isEmpty(MyApplication.NICKNAME)) {
			tvUserName.setText(MyApplication.NICKNAME);
		}
		if (!TextUtils.isEmpty(MyApplication.POINTS)) {
			tvScore.setText(MyApplication.POINTS);
		}
		if (TextUtils.equals(MyApplication.GROUPID, "100")) {
			llCheck.setVisibility(View.VISIBLE);
		}else {
			llCheck.setVisibility(View.GONE);
		}

		//获取我的消息条数
		OkHttpNewsCount(CONST.GET_MY_MESSAGE_COUNT_URL);
	}
	
	/**
	 * 获取头像
	 */
	private void getPortrait() {
		Bitmap bitmap = BitmapFactory.decodeFile(CONST.PORTRAIT_ADDR);
		if (bitmap != null) {
			ivPortrait.setImageBitmap(bitmap);
		}
	}
	
	/**
	 * 获取我的消息条数
	 */
	private void OkHttpNewsCount(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", MyApplication.TOKEN);
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
						if (result != null) {
							try {
								JSONObject object = new JSONObject(result);
								if (object != null) {
									if (!object.isNull("status")) {
										int status  = object.getInt("status");
										if (status == 1) {//成功
											if (!object.isNull("count")) {
												String count = object.getString("count");
												if (Integer.valueOf(count) > 99) {
													tvNewsCount.setText("99+");
												}else {
													tvNewsCount.setText(count);
												}
												if (count.equals("0")) {
													reNewsCount.setVisibility(View.GONE);
												}else {
													reNewsCount.setVisibility(View.VISIBLE);
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
				});
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.ivPortrait:
			startActivityForResult(new Intent(mContext, PersonInfoActivity.class), 0);
			break;
		case R.id.llMyVideo:
			startActivity(new Intent(mContext, MyUploadActivity.class));
			break;
		case R.id.llMyMsg:
			startActivityForResult(new Intent(mContext, MyMessageActivity.class), 1);
			break;
		case R.id.llMyScore:
			startActivityForResult(new Intent(mContext, MyScoreActivity.class), 2);
			break;
		case R.id.llCheck:
			startActivity(new Intent(mContext, CheckActivity.class));
			break;
		case R.id.llMySetting:
			startActivity(new Intent(mContext, MySettingActivity.class));
			break;
		case R.id.llMyAbout:
			startActivity(new Intent(mContext, MyAboutActivity.class));
			break;
		case R.id.llResponse:
			startActivity(new Intent(mContext, MyResponseActivity.class));
			break;
		case R.id.llOperate:
			startActivity(new Intent(mContext, UseActivity.class));
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 0:
			case 1:
			case 2:
				refreshUserInfo();
				break;

			default:
				break;
			}
		}
	}
}
