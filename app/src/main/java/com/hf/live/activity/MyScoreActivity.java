package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.adapter.MyScoreAdapter;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.OkHttpUtil;

import org.json.JSONArray;
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
 * 我的积分
 */

public class MyScoreActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private TextView tvTitle = null;
	private LinearLayout llBack = null;//返回按钮
	private TextView tvControl = null;//兑换
	private ListView mListView = null;
	private MyScoreAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private TextView tvScore = null;//总积分
	private int page = 1;
	private int pageSize = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_score);
		mContext = this;
		showDialog();
		initWidget();
		initListView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.my_score));
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvScore = (TextView) findViewById(R.id.tvScore);
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);
		tvControl.setText("积分排行");
		tvControl.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
		tvControl.setVisibility(View.VISIBLE);

		OkHttpScore(CONST.GET_SCORE_URL);
//		OkHttpAuthority(CONST.EXCHANGE_SCORE_AUTHORITY_URL);
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new MyScoreAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PhotoDto dto = mList.get(arg2);
				Intent intent = new Intent();
				if (dto.getWorkstype().equals("imgs")) {
					intent.setClass(mContext, OnlinePictureActivity.class);
				}else {
					intent.setClass(mContext, OnlineVideoActivity.class);
				}
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition() == view.getCount() - 1) {
					page += 1;
					OkHttpScore(CONST.GET_SCORE_URL);
				}
			}
			
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
			}
		});
	}
	
	/**
	 * 获取用户积分
	 */
	private void OkHttpScore(String requestUrl) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", MyApplication.TOKEN);
		builder.add("page", page+"");
		builder.add("pagesize", pageSize+"");
		builder.add("appid", CONST.APPID);
		RequestBody body = builder.build();
		OkHttpUtil.enqueue(new Request.Builder().post(body).url(requestUrl).build(), new Callback() {
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
						final JSONObject object = new JSONObject(result);
						if (object != null) {
							if (!object.isNull("status")) {
								int status  = object.getInt("status");
								if (status == 1) {//成功
									if (!object.isNull("count")) {
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												try {
													tvScore.setText(object.getString("count"));
												} catch (JSONException e) {
													e.printStackTrace();
												}
											}
										});
									}
									if (!object.isNull("info")) {
										JSONArray array = object.getJSONArray("info");
										for (int i = 0; i < array.length(); i++) {
											JSONObject obj = array.getJSONObject(i);
											PhotoDto dto = new PhotoDto();
											if (!obj.isNull("points")) {
												dto.score = obj.getString("points");
											}
											if (!obj.isNull("create_time")) {
												dto.createTime = obj.getString("create_time");
											}
											if (!obj.isNull("why")) {
												JSONObject whyObj = new JSONObject(obj.getString("why"));
												if (!whyObj.isNull("type")) {
													dto.scoreWhy = whyObj.getString("type");
												}
												if (!whyObj.isNull("wid")) {
													dto.workId = whyObj.getString("wid");
												}
											}

											if (!obj.isNull("winfo")) {
												JSONObject workObj = new JSONObject(obj.getString("winfo"));
												if (!workObj.isNull("id")) {
													dto.videoId = workObj.getString("id");
												}
												if (!workObj.isNull("location")) {
													dto.location = workObj.getString("location");
												}
												if (!workObj.isNull("title")) {
													dto.title = workObj.getString("title");
												}
												if (!workObj.isNull("praise")) {
													dto.praiseCount = workObj.getString("praise");
												}
												if (!workObj.isNull("comments")) {
													dto.commentCount = workObj.getString("comments");
												}
												if (!workObj.isNull("work_time")) {
													dto.workTime = workObj.getString("work_time");
												}
												if (!workObj.isNull("workstype")) {
													dto.workstype = workObj.getString("workstype");
												}
												if (!workObj.isNull("worksinfo")) {
													JSONObject itemObj = new JSONObject(workObj.getString("worksinfo"));

													if (!itemObj.isNull("thumbnail")) {
														JSONObject imgObj = new JSONObject(itemObj.getString("thumbnail"));
														if (!imgObj.isNull("url")) {
															dto.imgUrl = imgObj.getString("url");
														}
													}

													if (!itemObj.isNull("video")) {
														JSONObject imgObj = new JSONObject(itemObj.getString("video"));
														if (!imgObj.isNull("url")) {
															dto.videoUrl = imgObj.getString("url");
														}
													}

													List<String> urlList = new ArrayList<>();
													if (!itemObj.isNull("imgs1")) {
														JSONObject imgObj = new JSONObject(itemObj.getString("imgs1"));
														if (!imgObj.isNull("url")) {
															urlList.add(imgObj.getString("url"));
															dto.imgUrl = imgObj.getString("url");
														}
													}
													if (!itemObj.isNull("imgs2")) {
														JSONObject imgObj = new JSONObject(itemObj.getString("imgs2"));
														if (!imgObj.isNull("url")) {
															urlList.add(imgObj.getString("url"));
														}
													}
													if (!itemObj.isNull("imgs3")) {
														JSONObject imgObj = new JSONObject(itemObj.getString("imgs3"));
														if (!imgObj.isNull("url")) {
															urlList.add(imgObj.getString("url"));
														}
													}
													if (!itemObj.isNull("imgs4")) {
														JSONObject imgObj = new JSONObject(itemObj.getString("imgs4"));
														if (!imgObj.isNull("url")) {
															urlList.add(imgObj.getString("url"));
														}
													}
													if (!itemObj.isNull("imgs5")) {
														JSONObject imgObj = new JSONObject(itemObj.getString("imgs5"));
														if (!imgObj.isNull("url")) {
															urlList.add(imgObj.getString("url"));
														}
													}
													if (!itemObj.isNull("imgs6")) {
														JSONObject imgObj = new JSONObject(itemObj.getString("imgs6"));
														if (!imgObj.isNull("url")) {
															urlList.add(imgObj.getString("url"));
														}
													}
													if (!itemObj.isNull("imgs7")) {
														JSONObject imgObj = new JSONObject(itemObj.getString("imgs7"));
														if (!imgObj.isNull("url")) {
															urlList.add(imgObj.getString("url"));
														}
													}
													if (!itemObj.isNull("imgs8")) {
														JSONObject imgObj = new JSONObject(itemObj.getString("imgs8"));
														if (!imgObj.isNull("url")) {
															urlList.add(imgObj.getString("url"));
														}
													}
													if (!itemObj.isNull("imgs9")) {
														JSONObject imgObj = new JSONObject(itemObj.getString("imgs9"));
														if (!imgObj.isNull("url")) {
															urlList.add(imgObj.getString("url"));
														}
													}
													dto.setUrlList(urlList);
												}
											}

											if (!TextUtils.isEmpty(dto.getWorkTime())) {
												mList.add(dto);
											}
										}
									}

									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											cancelDialog();
											if (mList.size() > 0 && mAdapter != null) {
												mAdapter.notifyDataSetChanged();
											}
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
	}
	
	/**
	 * 获取积分兑换权限
	 */
	private void OkHttpAuthority(String url) {
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
				String result = response.body().string();
				if (result != null) {
					try {
						JSONObject object = new JSONObject(result);
						if (object != null) {
							if (!object.isNull("status")) {
								int status  = object.getInt("status");
								if (status == 1) {//成功
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											tvControl.setVisibility(View.VISIBLE);
										}
									});
								}else {
									//失败
									if (!object.isNull("msg")) {
										final String msg = object.getString("msg");
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												if (msg != null) {
													Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
												}
											}
										});
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_OK);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.tvControl:
			startActivity(new Intent(mContext, ScoreRankActivity.class));
			break;

		default:
			break;
		}
	}

}
