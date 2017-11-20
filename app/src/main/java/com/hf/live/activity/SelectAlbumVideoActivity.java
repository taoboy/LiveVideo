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
import com.hf.live.adapter.SelectAlbumPictureAdapter;
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
    private SelectAlbumVideoAdapter adapter = null;
    private List<PhotoDto> mList = new ArrayList<>();
    private Thread thread = null;

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
        adapter = new SelectAlbumVideoAdapter(mContext, mList);
        gridView.setAdapter(adapter);
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
     * 获取相册信息
     */
    private void loadAlbums() {
        abortLoading();
        AlbumLoaderRunnable runnable = new AlbumLoaderRunnable();
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

    private class AlbumLoaderRunnable implements Runnable {
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

            ArrayList<PhotoDto> temp = new ArrayList<>(cursor.getCount());
            HashSet<String> albumSet = new HashSet<>();
            File file;

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
                    file = new File(image);
                    if (file.exists() && !albumSet.contains(album)) {
                        PhotoDto dto = new PhotoDto();
                        dto.albumName = album;
                        dto.albumCover = image;
                        temp.add(dto);
                        albumSet.add(album);
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
