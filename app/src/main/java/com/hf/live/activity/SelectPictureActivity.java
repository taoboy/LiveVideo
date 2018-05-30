package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.adapter.SelectPictureAdapter;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 相册列表
 */
public class SelectPictureActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle,tvControl;
    private GridView gridView;
    private SelectPictureAdapter mAdapter = null;
    private List<PhotoDto> mList = new ArrayList<>();
    private int selectCount = 0;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private List<PhotoDto> selectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_picture);
        mContext = this;
        initWidget();
        initGridView();
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("已选中"+selectCount+"张（最多9张）");
        tvControl = (TextView) findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setText("确定");
        tvControl.setVisibility(View.VISIBLE);
    }

    private void initGridView() {
        gridView = (GridView) findViewById(R.id.gridView);
        mAdapter = new SelectPictureAdapter(mContext, mList);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoDto dto = mList.get(position);
                if (dto.isSelected) {
                    dto.isSelected = false;
                    selectCount--;
                }else {
                    if (selectCount >= 9) {
                        Toast.makeText(mContext, "最多只能选择9张图片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dto.isSelected = true;
                    selectCount++;
                }
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }

                tvTitle.setText("已选中"+selectCount+"张（最多9张）");
            }
        });

        loadImages();
    }

    /**
     * 获取相册信息
     */
    private void loadImages() {
        mList.clear();
        mList.addAll(CommonUtil.getAllLocalImages(mContext));

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
            case R.id.tvControl:
                if (selectCount <= 0) {
                    Toast.makeText(mContext, "请选择需要上传的图片！", Toast.LENGTH_SHORT).show();
                }else {
                    selectList.clear();
                    for (int i = 0; i < mList.size(); i++) {
                        PhotoDto dto = mList.get(i);
                        if (dto.isSelected) {
                            selectList.add(dto);
                        }
                    }

                    Intent intent = new Intent(mContext, DisplayPictureActivity.class);
                    intent.putExtra("takeTime", sdf.format(System.currentTimeMillis()));
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("selectList", (ArrayList<? extends Parcelable>) selectList);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
                break;

            default:
                break;
        }
    }

}
