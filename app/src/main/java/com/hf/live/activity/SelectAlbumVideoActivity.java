package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Process;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.adapter.SelectAlbumVideoAdapter;
import com.hf.live.dto.PhotoDto;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 视频列表
 */
public class SelectAlbumVideoActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private GridView gridView;
    private SelectAlbumVideoAdapter mAdapter = null;
    private List<PhotoDto> mList = new ArrayList<>();
    private Thread albumsThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_album_video);
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
        mAdapter = new SelectAlbumVideoAdapter(mContext, mList);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, SelectVideoActivity.class);
                intent.putExtra("albumName", mList.get(position).albumName);
                startActivityForResult(intent, 0);
            }
        });

        loadAlbums();
    }

    /**
     * 终止扫描相册任务
     */
    private void abortLoadingAlbums() {
        if (albumsThread == null) {
            return;
        }
        if (albumsThread.isAlive()) {
            albumsThread.interrupt();
            try {
                albumsThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取相册信息
     */
    private void loadAlbums() {
        abortLoadingAlbums();
        albumsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                if (Thread.interrupted()) {
                    return;
                }

                Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null,
                        null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);

                if (cursor == null) {
                    return;
                }

                ArrayList<PhotoDto> list = new ArrayList<>(cursor.getCount());
                HashSet<String> albumSet = new HashSet<>();

                if (cursor.moveToLast()) {
                    do {
                        if (Thread.interrupted()) {
                            return;
                        }

                        String album = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                        String image = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));

                    /*
                    It may happen that some image file paths are still present in cache,
                    though image file does not exist. These last as long as media
                    scanner is not run again. To avoid get such image file paths, check
                    if image file exists.
                     */
                        File file = new File(image);
                        if (file.exists() && !albumSet.contains(album)) {
                            PhotoDto dto = new PhotoDto();
                            dto.albumName = album;
                            dto.albumCover = image;
                            list.add(dto);
                            albumSet.add(album);
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
        albumsThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        abortLoadingAlbums();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
//            setResult(RESULT_OK, data);
//            finish();
//        }
    }

}
