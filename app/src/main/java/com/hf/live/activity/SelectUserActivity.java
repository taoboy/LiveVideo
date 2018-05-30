package com.hf.live.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.util.CommonUtil;
import com.hf.live.util.OkHttpUtil;
import com.hf.live.view.CircleImageView;

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
 * 选择新用户或者老用户
 * @author shawn_sun
 *
 */

public class SelectUserActivity extends BaseActivity implements OnClickListener{
	
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
	private void OkHttpLoginBind(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("phonenumber", MyApplication.USERNAME);
		builder.add("token", MyApplication.TOKEN);
		if (!TextUtils.isEmpty(oldUserName)) {
			builder.add("username", oldUserName);
		}
		if (!TextUtils.isEmpty(oldPwd)) {
			builder.add("passwd", oldPwd);
		}
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
								JSONObject object = new JSONObject(result);
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
												cancelDialog();
											}
										}else {
											//失败
											if (!object.isNull("msg")) {
												String msg = object.getString("msg");
												if (msg != null) {
													Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
												}
												cancelDialog();
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
	
	/**
	 * 解析用户数据
	 */
	private void parseUserinfo(final JSONObject object) {
		try {
			if (!object.isNull("info")) {
				JSONObject obj = new JSONObject(object.getString("info"));
				if (!obj.isNull("groupid")) {
					MyApplication.GROUPID = obj.getString("groupid");
				}
				if (!obj.isNull("token")) {
					MyApplication.TOKEN = obj.getString("token");
				}
				if (!obj.isNull("phonenumber")) {
					MyApplication.USERNAME = obj.getString("phonenumber");
				}
				if (!obj.isNull("points")) {
					MyApplication.POINTS = obj.getString("points");
				}
				if (!obj.isNull("photo")) {
					MyApplication.PHOTO = obj.getString("photo");
					if (!TextUtils.isEmpty(MyApplication.PHOTO)) {
						CommonUtil.OkHttpLoadPortrait(SelectUserActivity.this, MyApplication.PHOTO);
					}
				}

				MyApplication.saveUserInfo(mContext);

				cancelDialog();
				startActivity(new Intent(mContext, MainActivity2.class));
				finish();
			}
		} catch (JSONException e) {
			e.printStackTrace();
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
					OkHttpOldLogin("http://channellive2.tianqi.cn/weather/user/Login");
				}
			}
		});
	}
	
	/**
	 * 老用户登录
	 */
	private void OkHttpOldLogin(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				FormBody.Builder builder = new FormBody.Builder();
				builder.add("username", oldUserName);
				builder.add("passwd", oldPwd);
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
										JSONObject object = new JSONObject(result);
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

														cancelDialog();
														dialogConfirmInfo();

													}
												}else {
													//失败
													if (!object.isNull("msg")) {
														String msg = object.getString("msg");
														if (msg != null) {
															cancelDialog();
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
		}).start();
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
			CommonUtil.OkHttpLoadPortrait(SelectUserActivity.this, MyApplication.PHOTO);
			Bitmap bitmap = BitmapFactory.decodeFile(CONST.PORTRAIT_ADDR);
			if (bitmap != null) {
				ivPortrait.setImageBitmap(bitmap);
			}
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
				OkHttpLoginBind("http://channellive2.tianqi.cn/Weather/User/Login3Bind");
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
			OkHttpLoginBind("http://channellive2.tianqi.cn/Weather/User/Login3Bind");
			break;
		case R.id.tvOld:
			dialogBindUser();
			break;

		default:
			break;
		}
	}
	
}
