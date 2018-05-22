package com.hf.live.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.activity.LoginActivity;
import com.hf.live.activity.MainActivity;
import com.hf.live.activity.MainActivity2;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.util.CommonUtil;
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
 * 引导页
 */

public class GuideFragment extends Fragment implements OnClickListener{
	
	private RelativeLayout reMain = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_guide, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initWidget(view);
	}
	
	private void initWidget(View view) {
		reMain = (RelativeLayout) view.findViewById(R.id.reMain);
		
		int index = getArguments().getInt("index");
		if (index == 0) {
			reMain.setBackgroundResource(R.drawable.guide1);
		}else if (index == 1) {
			reMain.setBackgroundResource(R.drawable.guide2);
		}else if (index == 2) {
			reMain.setBackgroundResource(R.drawable.guide3);
			reMain.setOnClickListener(this);
		}
	}
	
	/**
	 * 获取我的信息，目的是为了验证token是否失效
	 */
	private void OkHttpUserinfo(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", MyApplication.TOKEN);
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
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
						getActivity().runOnUiThread(new Runnable() {
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
																CommonUtil.OkHttpLoadPortrait(getActivity(), MyApplication.PHOTO);
															}
														}

														MyApplication.saveUserInfo(getActivity());

														startActivity(new Intent(getActivity(), MainActivity2.class));
														getActivity().finish();

													}
												}else if (status == 401) {//token无效
													startActivity(new Intent(getActivity(), LoginActivity.class));
													getActivity().finish();
												}else {
													//失败
													if (!object.isNull("msg")) {
														String msg = object.getString("msg");
														if (msg != null) {
															Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
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
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.reMain:
			//保存版本号，为下次启动判断是否启动引导页
			SharedPreferences sharedGuide = getActivity().getSharedPreferences(CONST.SHOWGUIDE, Context.MODE_PRIVATE);
			Editor editor = sharedGuide.edit();
			editor.putString(CONST.VERSION, CommonUtil.getVersion(getActivity()));
			editor.commit();
			
			if (!TextUtils.isEmpty(MyApplication.TOKEN)) {
				OkHttpUserinfo("http://channellive2.tianqi.cn/Weather/User/getUser2");
			}else {
				startActivity(new Intent(getActivity(), LoginActivity.class));
				getActivity().finish();
			}
			break;

		default:
			break;
		}
	}
	
}
