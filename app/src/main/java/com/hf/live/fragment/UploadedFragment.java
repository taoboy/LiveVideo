package com.hf.live.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.activity.OnlinePictureActivity;
import com.hf.live.activity.OnlineVideoActivity;
import com.hf.live.adapter.MyUploadedAdapter;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.dto.PhotoDto;
import com.hf.live.stickygridheaders.StickyGridHeadersGridView;
import com.hf.live.util.OkHttpUtil;
import com.hf.live.view.MyDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 已上传
 * @author shawn_sun
 *
 */

public class UploadedFragment extends Fragment{
	
	private StickyGridHeadersGridView mGridView = null;
	private MyUploadedAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int section = 1;
	private HashMap<String, Integer> sectionMap = new HashMap<>();
	private MyDialog mDialog = null;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
	private int page = 1;
	private int pageSize = 100;
	private MyBroadCastReceiver mReceiver = null;
	private String dataUrl = "http://channellive2.tianqi.cn/weather/work/getmywork";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_uploaded, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		showDialog();
		initBroadCast();
		initStickyGridView(view);
		OKHttpUploaded(dataUrl);
	}
	
	private void initBroadCast() {
		mReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CONST.REFRESH_UPLOAD);
		getActivity().registerReceiver(mReceiver, intentFilter);
	}
	
	private class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (TextUtils.equals(intent.getAction(), CONST.REFRESH_UPLOAD)) {
				mList.clear();
				page = 1;
				OKHttpUploaded(dataUrl);
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mReceiver != null) {
			getActivity().unregisterReceiver(mReceiver);
		}
	}
	
	/**
	 * 初始化dialog
	 */
	private void showDialog() {
		if (mDialog == null) {
			mDialog = new MyDialog(getActivity());
		}
		mDialog.show();
	}
	private void cancelDialog() {
		if (mDialog != null) {
			mDialog.cancel();
		}
	}

	/**
	 * 初始化gridview
	 */
	private void initStickyGridView(View view) {
		mGridView = (StickyGridHeadersGridView) view.findViewById(R.id.stickyGridView);
		mAdapter = new MyUploadedAdapter(getActivity(), mList);
		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				PhotoDto dto = mList.get(arg2);
				Intent intent = new Intent();
				if (dto.getWorkstype().equals("imgs")) {
					intent.setClass(getActivity(), OnlinePictureActivity.class);
				}else {
					intent.setClass(getActivity(), OnlineVideoActivity.class);
				}
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		mGridView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition() == view.getCount() - 1) {
					page += 1;
					OKHttpUploaded(dataUrl);
				}
			}
			
			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
			}
		});
	}
	
	/**
	 * 获取已上传信息
	 */
	private void OKHttpUploaded(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", MyApplication.TOKEN);
		builder.add("page", page+"");
		builder.add("pagesize", pageSize+"");
		if (TextUtils.equals(MyApplication.TYPE, "2")) {//活动用户
			builder.add("appid", "26");
		}else {
			builder.add("appid", CONST.APPID);
		}
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
														JSONArray array = object.getJSONArray("info");
														for (int i = 0; i < array.length(); i++) {
															JSONObject obj = array.getJSONObject(i);
															PhotoDto dto = new PhotoDto();
															if (!obj.isNull("id")) {
																dto.videoId = obj.getString("id");
															}
															if (!obj.isNull("title")) {
																dto.title = obj.getString("title");
															}
															if (!obj.isNull("content")) {
																dto.content = obj.getString("content");
															}
															if (!obj.isNull("create_time")) {
																dto.createTime = obj.getString("create_time");
															}
															if (!obj.isNull("location")) {
																dto.location = obj.getString("location");
															}
															if (!obj.isNull("username")) {
																dto.userName = obj.getString("username");
															}
															if (!obj.isNull("praise")) {
																dto.praiseCount = obj.getString("praise");
															}
															if (!obj.isNull("browsecount")) {
																dto.playCount = obj.getString("browsecount");
															}
															if (!obj.isNull("comments")) {
																dto.commentCount = obj.getString("comments");
															}
															if (!obj.isNull("work_time")) {
																dto.workTime = obj.getString("work_time");
															}
															if (!obj.isNull("workstype")) {
																dto.workstype = obj.getString("workstype");
															}
															if (!obj.isNull("weather_flag")) {
																dto.weatherFlag = obj.getString("weather_flag");
															}
															if (!obj.isNull("other_flags")) {
																dto.otherFlag = obj.getString("other_flags");
															}
															if (!obj.isNull("worksinfo")) {
																JSONObject workObj = new JSONObject(obj.getString("worksinfo"));

																//视频
																if (!workObj.isNull("video")) {
																	JSONObject video = workObj.getJSONObject("video");
																	if (!video.isNull("ORG")) {//腾讯云结构解析
																		JSONObject ORG = video.getJSONObject("ORG");
																		if (!ORG.isNull("url")) {
																			dto.videoUrl = ORG.getString("url");
																		}
																		if (!video.isNull("SD")) {
																			JSONObject SD = video.getJSONObject("SD");
																			if (!SD.isNull("url")) {
																				dto.sd = SD.getString("url");
																			}
																		}
																		if (!video.isNull("HD")) {
																			JSONObject HD = video.getJSONObject("HD");
																			if (!HD.isNull("url")) {
																				dto.hd = HD.getString("url");
																				dto.videoUrl = HD.getString("url");
																			}
																		}
																		if (!video.isNull("FHD")) {
																			JSONObject FHD = video.getJSONObject("FHD");
																			if (!FHD.isNull("url")) {
																				dto.fhd = FHD.getString("url");
																			}
																		}
																	}else {
																		dto.videoUrl = video.getString("url");
																	}
																}
																if (!workObj.isNull("thumbnail")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("thumbnail"));
																	if (!imgObj.isNull("url")) {
																		dto.imgUrl = imgObj.getString("url");
																	}
																}

																//图片
																List<String> urlList = new ArrayList<>();
																if (!workObj.isNull("imgs1")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs1"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																		dto.imgUrl = imgObj.getString("url");
																	}
																}
																if (!workObj.isNull("imgs2")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs2"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs3")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs3"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs4")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs4"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs5")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs5"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs6")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs6"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs7")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs7"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs8")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs8"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																if (!workObj.isNull("imgs9")) {
																	JSONObject imgObj = new JSONObject(workObj.getString("imgs9"));
																	if (!imgObj.isNull("url")) {
																		urlList.add(imgObj.getString("url"));
																	}
																}
																dto.urlList.addAll(urlList);
															}

															if (!TextUtils.isEmpty(dto.workTime)) {
																mList.add(dto);
															}
														}
													}

													for (int i = 0; i < mList.size(); i++) {
														PhotoDto dto2 = mList.get(i);
														try {
															String date = sdf2.format(sdf1.parse(dto2.workTime));
															if (!sectionMap.containsKey(date)) {
																dto2.setSection(section);
																sectionMap.put(date, section);
																section++;
															}else {
																dto2.setSection(sectionMap.get(date));
															}
															mList.set(i, dto2);
														} catch (ParseException e) {
															e.printStackTrace();
														}
													}

													cancelDialog();
													if (mAdapter != null) {
														mAdapter.notifyDataSetChanged();
													}

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
	
}
