package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.adapter.EditVideoFragmentAdapter;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 选择视频
 * @author shawn_sun-
 *
 */

public class VideoSelectEditActivity extends BaseActivity implements View.OnClickListener {

	private Context mContext;
	private TextView tvTitle, tvControl;
	private GridView gridView;
	private EditVideoFragmentAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int selectSize = 0;//已经选择的条数
	private int selectSequnce = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_edit_video);
		mContext = this;
		initWidget();
		initGridView();
	}

	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("选择视频");
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);
		tvControl.setText("确定");
	}

	private void initGridView() {
		gridView = (GridView) findViewById(R.id.gridView);
		mAdapter = new EditVideoFragmentAdapter(mContext, mList);
		gridView.setAdapter(mAdapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PhotoDto dto = mList.get(position);
				if (dto.isSelected) {
					dto.isSelected = false;
					selectSize--;
				}else {
					selectSequnce++;
					dto.selectSequnce = selectSequnce;
					dto.isSelected = true;
					selectSize++;
				}
				mAdapter.notifyDataSetChanged();

				if (selectSize <= 0) {
					tvControl.setVisibility(View.INVISIBLE);
				}else {
					tvControl.setVisibility(View.VISIBLE);
				}
			}
		});

		loadVideos();
	}

	private void loadVideos() {
		mList.clear();
		mList.addAll(CommonUtil.getAllLocalVideos(mContext));

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tvControl:
				List<PhotoDto> dataList = new ArrayList<>();
				for (int i = 0; i < mList.size(); i++) {
					PhotoDto dto = mList.get(i);
					if (dto.isSelected) {
						dataList.add(dto);
					}
				}

				//按选择顺序排序
				Collections.sort(dataList, new Comparator<PhotoDto>() {
					@Override
					public int compare(PhotoDto dto1, PhotoDto dto2) {
						return dto1.selectSequnce-dto2.selectSequnce;
					}
				});

				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) dataList);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
				break;
		}
	}

}
