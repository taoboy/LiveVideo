package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Process;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.adapter.SelectVideoAdapter;
import com.hf.live.dto.PhotoDto;
import com.hf.live.qcloud.TCConstants;

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
    private GridView gridView;
    private SelectVideoAdapter mAdapter = null;
    private List<PhotoDto> mList = new ArrayList<>();
    private Thread imagesThread = null;

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

        loadImages();
    }

    /**
     * 终止扫描某个相册里的照片
     */
    private void abortLoadingImages() {
        if (imagesThread == null) {
            return;
        }
        if (imagesThread.isAlive()) {
            imagesThread.interrupt();
            try {
                imagesThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取相册信息
     */
    private void loadImages() {
        abortLoadingImages();
        imagesThread = new Thread(new Runnable() {
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
                ArrayList<PhotoDto> list = new ArrayList<>(cursor.getCount());
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
                            list.add(dto);
                        }

                    } while (cursor.moveToPrevious());
                }
                cursor.close();

                mList.clear();
                mList.addAll(list);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });

                Thread.interrupted();
            }
        });
        imagesThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        abortLoadingImages();
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
