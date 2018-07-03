package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.adapter.EditVideoFragmentAdapter;
import com.hf.live.common.CONST;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;

import java.io.File;
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
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_edit_video);
		mContext = this;
		initRefreshLayout();
		initWidget();
		initGridView();
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout() {
		refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setColorSchemeResources(CONST.color1, CONST.color2, CONST.color3, CONST.color4);
		refreshLayout.setProgressViewEndTarget(true, 300);
		refreshLayout.setRefreshing(true);
		refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				loadVideos();
			}
		});
	}

	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("选择视频");
		tvControl = (TextView) findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);
		tvControl.setText("确定");

		loadVideos();
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
	}

	/**
	 * 获取本地视频文件
	 */
	private void loadVideos() {
		mList.clear();
		List<PhotoDto> list1 = new ArrayList<>();
		CommonUtil.getAllLocalVideos(list1, new File(CONST.VIDEO_ADDR), 1);
		CommonUtil.getAllLocalVideos(list1, new File(CONST.TRIMPATH), 2);
		CommonUtil.getAllLocalVideos(list1, new File(CONST.MERGEPATH), 3);
		CommonUtil.getAllLocalVideos(list1, new File(CONST.DOWNLOAD_ADDR), 4);

		List<PhotoDto> list2 = new ArrayList<>();
		list2.addAll(CommonUtil.getAllLocalVideos(mContext));

		mList.addAll(list1);
		mList.addAll(list2);

		refreshLayout.setRefreshing(false);
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
