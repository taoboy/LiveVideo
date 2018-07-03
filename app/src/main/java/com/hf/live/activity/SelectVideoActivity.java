package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.adapter.SelectVideoAdapter;
import com.hf.live.common.CONST;
import com.hf.live.dto.PhotoDto;
import com.hf.live.qcloud.TCConstants;
import com.hf.live.stickygridheaders.StickyGridHeadersGridView;
import com.hf.live.util.CommonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 选择视频
 */
public class SelectVideoActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle;
    private StickyGridHeadersGridView gridView;
    private SelectVideoAdapter mAdapter = null;
    private List<PhotoDto> mList = new ArrayList<>();
    private SwipeRefreshLayout refreshLayout = null;//下拉刷新布局

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_video);
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
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("选择视频");

        loadVideos();
    }

    private void initGridView() {
        gridView = (StickyGridHeadersGridView) findViewById(R.id.gridView);
        mAdapter = new SelectVideoAdapter(mContext, mList);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoDto dto = mList.get(position);
                Intent intent = new Intent(mContext, DisplayVideoActivity.class);
                intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, dto.videoUrl);
                if (getIntent().hasExtra("appid")) {
                    intent.putExtra("appid", "26");//活动专用频道
                }
                startActivity(intent);
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
            case R.id.llBack:
                finish();
                break;

            default:
                break;
        }
    }

}
