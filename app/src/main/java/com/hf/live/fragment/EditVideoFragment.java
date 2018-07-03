package com.hf.live.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.activity.VideoEditListActivity;
import com.hf.live.adapter.EditVideoFragmentAdapter;
import com.hf.live.common.CONST;
import com.hf.live.dto.PhotoDto;
import com.hf.live.stickygridheaders.StickyGridHeadersGridView;
import com.hf.live.util.CommonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 视频编辑
 * @author shawn_sun-
 *
 */

public class EditVideoFragment extends Fragment implements View.OnClickListener {

	private TextView tvControl;
	private StickyGridHeadersGridView gridView;
	private EditVideoFragmentAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int selectSize = 0;//已经选择的条数
	private int selectSequnce = 0;
	private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_edit_video, null);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initRefreshLayout(view);
		initWidget(view);
		initGridView(view);
	}

	/**
	 * 初始化下拉刷新布局
	 */
	private void initRefreshLayout(View view) {
		refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
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

	private void initWidget(View view) {
		tvControl = (TextView) view.findViewById(R.id.tvControl);
		tvControl.setOnClickListener(this);

		loadVideos();
	}

	private void initGridView(View view) {
		gridView = (StickyGridHeadersGridView) view.findViewById(R.id.gridView);
		mAdapter = new EditVideoFragmentAdapter(getActivity(), mList);
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
		list2.addAll(CommonUtil.getAllLocalVideos(getActivity()));

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

				Intent intent = new Intent(getActivity(), VideoEditListActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("dataList", (ArrayList<? extends Parcelable>) dataList);
				intent.putExtras(bundle);
				startActivity(intent);
//				startActivityForResult(intent, 1001);
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == getActivity().RESULT_OK) {
			switch (requestCode) {
				case 1001:
					loadVideos();
					break;
			}
		}
	}
}
