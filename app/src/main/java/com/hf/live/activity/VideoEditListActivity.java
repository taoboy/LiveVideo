package com.hf.live.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.dto.PhotoDto;
import com.hf.live.qcloud.TCConstants;
import com.hf.live.util.CommonUtil;
import com.hf.live.view.MyDialog;
import com.tencent.ugc.TXVideoEditConstants;
import com.tencent.ugc.TXVideoJoiner;
import com.yydcdut.sdlv.Menu;
import com.yydcdut.sdlv.MenuItem;
import com.yydcdut.sdlv.SlideAndDragListView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 视频编辑列表
 */

public class VideoEditListActivity extends BaseActivity implements View.OnClickListener, TXVideoJoiner.TXVideoPreviewListener, TXVideoJoiner.TXVideoJoinerListener{

    private Context mContext;
    private LinearLayout llBack;
    private TextView tvTitle, tvControl;
    private SlideAndDragListView listView;
    private VideoEditListAdapter mAdapter;
    private List<PhotoDto> dataList = new ArrayList<>();
    private int width;
    private ImageView ivAdd,ivPreview;
    private FrameLayout frameLayout;
    private TXVideoJoiner mTXVideoJoiner;
    private String mVideoOutputPath;                        // 视频输出路径
    private MyDialog mergeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit_list);
        mContext = this;
        initBroadCast();
        initWidget();
        initListView();
    }

    private void showMergeDialog() {
        if (mergeDialog == null) {
            mergeDialog = new MyDialog(mContext);
            mergeDialog.setCanceledOnTouchOutside(false);
        }
        mergeDialog.show();
        mergeDialog.setPercent(0);
    }

    private void cancelMergeDialog() {
        if (mergeDialog != null) {
            mergeDialog.dismiss();
        }
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("视频剪辑");
        tvControl = (TextView) findViewById(R.id.tvControl);
        tvControl.setOnClickListener(this);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        ivAdd = (ImageView) findViewById(R.id.ivAdd);
        ivAdd.setOnClickListener(this);
        ivPreview = (ImageView) findViewById(R.id.ivPreview);
        ivPreview.setOnClickListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width*2/3, width*2/3*9/16);
        frameLayout.setLayoutParams(params);
    }

    private Menu mMenu;
    public void initMenu() {
        mMenu = new Menu(true);
        mMenu.addItem(new MenuItem.Builder().setWidth((int) CommonUtil.dip2px(mContext, 50))
                .setBackground(getDrawable(R.color.red))
//                .setText("删除")
                .setDirection(MenuItem.DIRECTION_RIGHT)
//                .setTextColor(Color.BLACK)
//                .setTextSize(14)
                .setIcon(getResources().getDrawable(R.drawable.iv_delete_white))
                .build());
    }

    PhotoDto mDraggedEntity;
    private void initListView() {
        dataList.clear();
        dataList.addAll(getIntent().getExtras().<PhotoDto>getParcelableArrayList("dataList"));

        initMenu();

        listView = (SlideAndDragListView) findViewById(R.id.listView);
        listView.setMenu(mMenu);
        mAdapter = new VideoEditListAdapter(mContext, dataList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < dataList.size(); i++) {
                    dataList.get(i).isEditing = false;
                }

                PhotoDto dto = dataList.get(position);
                dto.isEditing = true;

                Intent intent = new Intent(mContext, TCVideoPreprocessActivity.class);
                intent.putExtra(TCConstants.VIDEO_EDITER_PATH, dto.videoUrl);
                intent.putExtra("isNeedEdit", false);//是否需要编辑
                startActivity(intent);
            }
        });
        listView.setOnDragDropListener(new SlideAndDragListView.OnDragDropListener() {
            @Override
            public void onDragViewStart(int beginPosition) {
                mDraggedEntity = dataList.get(beginPosition);
            }

            @Override
            public void onDragDropViewMoved(int fromPosition, int toPosition) {
                PhotoDto dto = dataList.remove(fromPosition);
                dataList.add(toPosition, dto);
            }

            @Override
            public void onDragViewDown(int finalPosition) {
                dataList.set(finalPosition, mDraggedEntity);
            }
        });
        listView.setOnSlideListener(new SlideAndDragListView.OnSlideListener() {
            @Override
            public void onSlideOpen(View view, View parentView, int position, int direction) {
            }

            @Override
            public void onSlideClose(View view, View parentView, int position, int direction) {
            }
        });
        listView.setOnMenuItemClickListener(new SlideAndDragListView.OnMenuItemClickListener() {
            @Override
            public int onMenuItemClick(View v, int itemPosition, int buttonPosition, int direction) {
                switch (direction) {
                    case MenuItem.DIRECTION_LEFT:
                        switch (buttonPosition) {
                            case 0:
                                return Menu.ITEM_NOTHING;
                            case 1:
                                return Menu.ITEM_SCROLL_BACK;
                        }
                        break;
                    case MenuItem.DIRECTION_RIGHT:
                        switch (buttonPosition) {
                            case 0:
                                dataList.remove(itemPosition - listView.getHeaderViewsCount());
                                mAdapter.notifyDataSetChanged();
                                return Menu.ITEM_SCROLL_BACK;
                            case 1:
                                return Menu.ITEM_DELETE_FROM_BOTTOM_TO_TOP;
                        }
                }
                return Menu.ITEM_NOTHING;
            }
        });
        listView.setOnItemDeleteListener(new SlideAndDragListView.OnItemDeleteListener() {
            @Override
            public void onItemDeleteAnimationFinished(View view, int position) {
                dataList.remove(position - listView.getHeaderViewsCount());
                mAdapter.notifyDataSetChanged();
            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        break;
                }
            }
        });
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

    }

    public class VideoEditListAdapter extends BaseAdapter {

        private Context mContext;
        private LayoutInflater mInflater;
        private List<PhotoDto> mArrayList;
        private int width;
        private RelativeLayout.LayoutParams params;

        private final class ViewHolder{
            ImageView imageView,ivMenu;
            TextView tvDuration;
        }

        private ViewHolder mHolder = null;

        public VideoEditListAdapter(Context context, List<PhotoDto> mArrayList) {
            mContext = context;
            this.mArrayList = mArrayList;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            width = wm.getDefaultDisplay().getWidth();

            params = new RelativeLayout.LayoutParams(width*2/4, width*2/4*9/16);
        }

        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.adapter_edit_list, null);
                mHolder = new ViewHolder();
                mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                mHolder.ivMenu = (ImageView) convertView.findViewById(R.id.ivMenu);
                mHolder.ivMenu.setOnTouchListener(mOnTouchListener);
                mHolder.tvDuration = (TextView) convertView.findViewById(R.id.tvDuration);
                convertView.setTag(mHolder);
            }else {
                mHolder = (ViewHolder) convertView.getTag();
            }

            try {
                PhotoDto dto = mArrayList.get(position);

                if (!TextUtils.isEmpty(dto.videoUrl)) {
                    String imgPath = CommonUtil.getVideoThumbnail(dto.videoUrl, MediaStore.Video.Thumbnails.MINI_KIND);
                    if (!TextUtils.isEmpty(imgPath) && new File(imgPath).exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                        if (bitmap != null) {
                            mHolder.imageView.setImageBitmap(bitmap);
                        }
                    }else {
                        CommonUtil.videoThumbnail(dto.videoUrl, width*2/4, width*2/4*9/16, MediaStore.Video.Thumbnails.MINI_KIND, mHolder.imageView);
                    }
                }
                if (params != null) {
                    mHolder.imageView.setLayoutParams(params);
                }

                mHolder.tvDuration.setText(String.format("%02d:%02d", dto.duration/1000/60, dto.duration/1000%60));

            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            return convertView;
        }

        private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Object o = v.getTag();
                if (o != null && o instanceof Integer) {
                    listView.startDrag(((Integer) o).intValue());
                }
                return false;
            }
        };

    }



    /**
     * 预览合成后视频
     */
    private void previewVideo() {
        if (dataList.size() <= 1) {
            tvControl.setText("上传");
            tvControl.setVisibility(View.VISIBLE);
            cancelMergeDialog();
        }else {
            tvControl.setText("合成");
            List<String> videoPaths = new ArrayList<>();
            for (int i = 0; i < dataList.size(); i++) {
                videoPaths.add(dataList.get(i).videoUrl);
            }

            //准备预览 View
            TXVideoEditConstants.TXPreviewParam param = new TXVideoEditConstants.TXPreviewParam();
            param.videoView = frameLayout;
            param.renderMode = TXVideoEditConstants.PREVIEW_RENDER_MODE_FILL_EDGE;

            // 创建 TXUGCJoiner 对象并设置预览 view
            if (mTXVideoJoiner == null) {
                mTXVideoJoiner = new TXVideoJoiner(this);
                mTXVideoJoiner.setTXVideoPreviewListener(this);
            }
            mTXVideoJoiner.initWithPreview(param);
            // 设置待拼接的视频文件组 mVideoSourceList，也就是第一步中选择的若干个文件
            int ret = mTXVideoJoiner.setVideoPathList(videoPaths);
            cancelMergeDialog();
            if (ret == 0) {
                tvControl.setVisibility(View.VISIBLE);
                mTXVideoJoiner.startPlay();
            } else if (ret == TXVideoEditConstants.ERR_UNSUPPORT_VIDEO_FORMAT) {
                Toast.makeText(mContext, "视频合成失败，本机型暂不支持此视频格式", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onPreviewProgress(int time) {
    }

    @Override
    public void onPreviewFinished() {
        if (mTXVideoJoiner != null) {
            mTXVideoJoiner.startPlay();
        }
    }

    /**
     * 生成合成后输出视频路径
     *
     */
    private String mergeVideoPath() {
        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String time = sdf.format(new Date(currentTime));
        File outputFolder = new File(CONST.MERGEPATH);
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }
        String tempOutputPath = outputFolder.getPath() + File.separator + time + CONST.VIDEOTYPE;
        return tempOutputPath;
    }

    @Override
    public void onJoinProgress(float progress) {
        if (mergeDialog != null) {
            mergeDialog.setPercent((int) (progress * 100));
        }
    }

    @Override
    public void onJoinComplete(TXVideoEditConstants.TXJoinerResult result) {
        cancelMergeDialog();
        if (result.retCode == TXVideoEditConstants.JOIN_RESULT_OK) {
            Intent intent = new Intent(mContext, DisplayVideoActivity.class);
            intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, mVideoOutputPath);
            startActivity(intent);
        } else {
            TXVideoEditConstants.TXJoinerResult ret = result;
            Toast.makeText(mContext, "视频合成失败"+result.descMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlay() {
        if (mTXVideoJoiner != null) {
            mTXVideoJoiner.stopPlay();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTXVideoJoiner != null) {
            mTXVideoJoiner.resumePlay();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTXVideoJoiner != null) {
            mTXVideoJoiner.pausePlay();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTXVideoJoiner != null) {
            mTXVideoJoiner.setTXVideoPreviewListener(null);
            mTXVideoJoiner.setVideoJoinerListener(null);
        }
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            stopPlay();
            setResult(RESULT_OK);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                stopPlay();
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.ivAdd:
                startActivityForResult(new Intent(mContext, VideoSelectEditActivity.class), 1001);
                break;
            case R.id.ivPreview:
                showMergeDialog();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        previewVideo();
                    }
                }, 1000);
                break;
            case R.id.tvControl:
                stopPlay();
                if (dataList.size() <= 1) {//单个视频上传
                    Intent intent = new Intent(mContext, DisplayVideoActivity.class);
                    intent.putExtra(TCConstants.VIDEO_RECORD_VIDEPATH, dataList.get(0).videoUrl);
                    startActivity(intent);
                }else {//视频合成
                    if (mTXVideoJoiner != null) {
                        showMergeDialog();
                        mVideoOutputPath = mergeVideoPath();
                        mTXVideoJoiner.setVideoJoinerListener(this);
                        mTXVideoJoiner.joinVideo(TXVideoEditConstants.VIDEO_COMPRESSED_720P, mVideoOutputPath);
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1001:
                    if (data != null) {
                        dataList.addAll(data.getExtras().<PhotoDto>getParcelableArrayList("dataList"));
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
            }
        }
    }


    //刷新视频编辑列表
    private MyBroadCastReceiver mReceiver;

    private void initBroadCast() {
        mReceiver = new MyBroadCastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("refresh_edit_list");
        registerReceiver(mReceiver, intentFilter);
    }

    private class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String videoPath = intent.getStringExtra(TCConstants.VIDEO_RECORD_VIDEPATH);
                if (!TextUtils.isEmpty(videoPath)) {
                    for (int i = 0; i < dataList.size(); i++) {
                        PhotoDto dto = dataList.get(i);
                        if (dto.isEditing) {
                            dto.videoUrl = videoPath;
                            break;
                        }
                    }
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

}
