package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Process;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.adapter.SelectVideoAdapter;
import com.hf.live.dto.PhotoDto;
import com.hf.live.videoedit.VideoTrimActivity;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 视频列表
 */
public class SelectVideoActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private TextView tvControl = null;
    private GridView gridView;
    private SelectVideoAdapter adapter = null;
    private List<PhotoDto> mList = new ArrayList<>();
    private Thread thread = null;
    private int selectCount = 0;
    private List<PhotoDto> selectList = new ArrayList<>();
    private int width = 0;

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
        tvTitle.setText("已选中"+selectCount+"段（最多5段）");
        tvControl = (TextView) findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        tvControl.setText("确定");

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
    }

    private void initGridView() {
        gridView = (GridView) findViewById(R.id.gridView);
        adapter = new SelectVideoAdapter(mContext, mList, width/4);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoDto dto = mList.get(position);
                if (dto.isSelected) {
                    dto.isSelected = false;
                    selectCount--;
                    dto.selectSequnce = 0;
                }else {
                    if (selectCount >= 5) {
                        Toast.makeText(mContext, "最多只能选择5段视频", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dto.isSelected = true;
                    selectCount++;
                    dto.selectSequnce = selectCount;
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                if (selectCount <= 0) {
                    tvControl.setVisibility(View.GONE);
                }else {
                    tvControl.setVisibility(View.VISIBLE);
                }

                tvTitle.setText("已选中"+selectCount+"段（最多5段）");
            }
        });

        loadImages();
    }

    /**
     * 获取相册信息
     */
    private void loadImages() {
        abortLoading();
        ImageLoaderRunnable runnable = new ImageLoaderRunnable();
        thread = new Thread(runnable);
        thread.start();
    }

    private void abortLoading() {
        if (thread == null) {
            return;
        }

        if (thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class ImageLoaderRunnable implements Runnable {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            if (Thread.interrupted()) {
                return;
            }

            String albumName = getIntent().getStringExtra("albumName");
            if (TextUtils.isEmpty(albumName)) {
                return;
            }
            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{ albumName }, MediaStore.Video.Media.DATE_ADDED);

            /*
            In case this runnable is executed to onChange calling loadImages,
            using countSelected variable can result in a race condition. To avoid that,
            tempCountSelected keeps track of number of selected images. On handling
            FETCH_COMPLETED message, countSelected is assigned value of tempCountSelected.
             */
            ArrayList<PhotoDto> temp = new ArrayList<>(cursor.getCount());
            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    String videoName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                    String videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));

                    File file = new File(videoPath);
                    if (file.exists()) {
                        PhotoDto dto = new PhotoDto();
                        dto.imageName = videoName;
                        dto.videoUrl = videoPath;
                        temp.add(dto);
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            mList.clear();
            mList.addAll(temp);

            if (mList.size() > 0 && adapter != null) {
                adapter.notifyDataSetChanged();
            }

            Thread.interrupted();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        abortLoading();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
            case R.id.tvControl:
                selectList.clear();
                for (int i = 0; i < mList.size(); i++) {
                    PhotoDto dto = mList.get(i);
                    if (dto.isSelected) {
                        selectList.add(dto);
                    }
                }

                //按照选择顺序排序
                Collections.sort(selectList, new Comparator<PhotoDto>() {
                    @Override
                    public int compare(PhotoDto dto1, PhotoDto dto2) {
                        return dto1.selectSequnce - dto2.selectSequnce;
                    }
                });

                Intent intent = new Intent(mContext, VideoTrimActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("selectList", (ArrayList<? extends Parcelable>) selectList);
                intent.putExtras(bundle);
                startActivity(intent);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //恢复初始状态
                        for (int i = 0; i < mList.size(); i++) {
                            PhotoDto d = mList.get(i);
                            d.selectSequnce = 0;
                            d.isSelected = false;
                        }
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        selectCount = 0;
                        tvTitle.setText("已选中"+selectCount+"段（最多5段）");
                    }
                }, 200);

                break;

            default:
                break;
        }
    }

}
