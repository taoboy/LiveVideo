package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.adapter.SelectVideoAdapter;
import com.hf.live.dto.PhotoDto;
import com.hf.live.qcloud.TCConstants;
import com.hf.live.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择视频
 */
public class SelectVideoActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle;
    private GridView gridView;
    private SelectVideoAdapter mAdapter = null;
    private List<PhotoDto> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_video);
        mContext = this;
        initWidget();
        initGridView();
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("选择视频");
    }

    private void initGridView() {
        gridView = (GridView) findViewById(R.id.gridView);
        mAdapter = new SelectVideoAdapter(mContext, mList);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoDto dto = mList.get(position);
                Intent intent = new Intent(mContext, DisplayVideoActivity.class);
                intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, dto.videoUrl);
                startActivity(intent);
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
            case R.id.llBack:
                finish();
                break;

            default:
                break;
        }
    }

}
