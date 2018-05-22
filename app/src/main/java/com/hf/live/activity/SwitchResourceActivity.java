package com.hf.live.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.adapter.SwitchResourceAdapter;
import com.hf.live.common.CONST;
import com.hf.live.dto.SwitchDto;
import com.hf.live.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 切换数据源
 * @author shawn_sun
 *
 */

public class SwitchResourceActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private ListView mListView = null;
	private SwitchResourceAdapter mAdapter = null;
	private List<SwitchDto> mList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_switch_resource);
		mContext = this;
		initWidget();
		initListView();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.switch_source));

		OkHttpDataResource("http://channellive2.tianqi.cn/Weather/work/getappids");
	}
	
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new SwitchResourceAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				for (int i = 0; i < mList.size(); i++) {
					mList.get(i).isSelected = false;
				}
				mList.get(arg2).isSelected = true;
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
			}
		});
	}
	
	private void saveData() {
		SharedPreferences sp = getSharedPreferences("DATASOURCE", Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putInt("size", mList.size());
		for (int i = 0; i < mList.size(); i++) {
			editor.remove("name"+i);
			editor.remove("appid"+i);
			editor.remove("isSelected"+i);
			
			editor.putString("name"+i, mList.get(i).name);
			editor.putString("appid"+i, mList.get(i).appid);
			editor.putBoolean("isSelected"+i, mList.get(i).isSelected);
			if (mList.get(i).isSelected) {
				CONST.APPID = mList.get(i).appid;
				CONST.SOURCENAME = mList.get(i).name;
			}
		}
		editor.commit();
		
		setResult(RESULT_OK);
		finish();
	}
	
	/**
	 * 获取视频墙数据源
	 */
	private void OkHttpDataResource(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
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
										mList.clear();
										JSONArray array = new JSONArray(result);
										SharedPreferences sp = getSharedPreferences("DATASOURCE", Context.MODE_PRIVATE);
										int size = sp.getInt("size", 0);
										if (size == array.length()) {
											for (int i = 0; i < size; i++) {
												SwitchDto dto = new SwitchDto();
												dto.name = sp.getString("name"+i, "");
												dto.appid = sp.getString("appid"+i, "");
												dto.isSelected = sp.getBoolean("isSelected"+i, false);
												mList.add(dto);
											}
										}else {
											for (int i = 0; i < array.length(); i++) {
												JSONObject itemObj = array.getJSONObject(i);
												SwitchDto dto = new SwitchDto();
												dto.name = itemObj.getString("name");
												dto.appid = itemObj.getString("id");
												if (TextUtils.equals(dto.appid, "0")) {
													dto.isSelected = true;
												}else {
													dto.isSelected = false;
												}
												mList.add(dto);
											}
										}

										if (mAdapter != null) {
											mAdapter.notifyDataSetChanged();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			saveData();
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			saveData();
			break;

		default:
			break;
		}
	}
	
}
