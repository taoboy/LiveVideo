package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Process;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.adapter.SelectPictureAdapter;
import com.hf.live.dto.PhotoDto;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 相册列表
 */
public class SelectPictureActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private TextView tvControl = null;
    private GridView gridView;
    private SelectPictureAdapter adapter = null;
    private List<PhotoDto> mList = new ArrayList<>();
    private Thread thread = null;
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
        adapter = new SelectPictureAdapter(mContext, mList);
        gridView.setAdapter(adapter);
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
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
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
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            if (Thread.interrupted()) {
                return;
            }

            String albumName = getIntent().getStringExtra("albumName");
            if (TextUtils.isEmpty(albumName)) {
                return;
            }
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA },
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{ albumName }, MediaStore.Images.Media.DATE_ADDED);

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

                    String imageName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
                    String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    File file = new File(imagePath);
                    if (file.exists()) {
                        PhotoDto dto = new PhotoDto();
                        dto.imageName = imageName;
                        dto.imgUrl = imagePath;
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
                if (selectCount <= 0) {
                    tvControl.setTextColor(0x60ffffff);
                }else {
                    tvControl.setTextColor(Color.WHITE);

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
