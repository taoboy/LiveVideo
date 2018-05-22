package com.hf.live.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.adapter.CommentAdapter;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;
import com.hf.live.util.EmojiMapUtil;
import com.hf.live.util.OkHttpUtil;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.tencent.rtmp.TXLiveConstants.PLAY_ERR_NET_DISCONNECT;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_BEGIN;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_END;
import static com.tencent.rtmp.TXLiveConstants.PLAY_EVT_PLAY_PROGRESS;


/**
 * 在线预览视频
 */

public class OnlineVideoActivity extends BaseActivity implements OnClickListener, ITXVodPlayListener{

    private Context mContext = null;
    private TXCloudVideoView mTXCloudVideoView;
    private TXVodPlayer mTXVodPlayer;
    private TXVodPlayConfig mTXVodPlayConfig;
    private static final int HANDLER_DELAY = 1;
    private boolean isPlaying = true;//是否正在播放
    private boolean isFirstPlay = true;//是否为第一次播放

    private TextView tvTitle, tvContent, tvPositon, tvDate, tvCommentCount, tvSubmit, tvPlayTime, tvWeatherFlag, tvOtherFlag, tvUserName, tvPlayCount, tvPraiseCount;
    private ImageView ivBack, ivPlay, ivInFull, ivPortrait, ivPraise, ivShare, ivDownload, ivClear;
    private EditText etComment = null;
    private RelativeLayout reBottom = null;//屏幕下方区域
    private LinearLayout llSubmit;
    private SeekBar seekBar = null;//进度条
    private int width, height;
    private PhotoDto data = null;
    private boolean praiseState = false;//点赞状态
    private Configuration configuration = null;//方向监听器
    private ProgressBar progressBar = null;

    //评论
    private ListView mListView = null;
    private CommentAdapter mAdapter = null;
    private List<PhotoDto> mList = new ArrayList<>();
    private int page = 1;
    private int pageSize = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_video);
        mContext = this;
        initWidget();
        initVideoView();
        initListView();
    }

    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configuration = newConfig;
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            showPort();
            fullScreen(false);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            showLand();
            fullScreen(true);
        }
    }

    /**
     * 显示竖屏，隐藏横屏
     */
    private void showPort() {
        llSubmit.setVisibility(View.VISIBLE);
        ivInFull.setImageResource(R.drawable.iv_out_full);

        if (mTXCloudVideoView != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width*9/16);
            mTXCloudVideoView.setLayoutParams(params);
        }
    }

    /**
     * 显示横屏，隐藏竖屏
     */
    private void showLand() {
        llSubmit.setVisibility(View.GONE);
        ivInFull.setImageResource(R.drawable.iv_in_full);

        if (mTXCloudVideoView != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(height, height*9/16);
            mTXCloudVideoView.setLayoutParams(params);
        }
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        ivBack = (ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);
        ivPlay = (ImageView) findViewById(R.id.ivPlay);
        ivPlay.setOnClickListener(this);
        ivPortrait = (ImageView) findViewById(R.id.ivPortrait);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvPlayCount = (TextView) findViewById(R.id.tvPlayCount);
        tvPraiseCount = (TextView) findViewById(R.id.tvPraiseCount);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        tvPlayTime = (TextView) findViewById(R.id.tvPlayTime);
        tvPlayTime.setText("00:00/00:00");
        ivInFull = (ImageView) findViewById(R.id.ivInFull);
        ivInFull.setOnClickListener(this);
        reBottom = (RelativeLayout) findViewById(R.id.reBottom);
        tvPositon = (TextView) findViewById(R.id.tvPosition);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvCommentCount = (TextView) findViewById(R.id.tvCommentCount);
        ivPraise = (ImageView) findViewById(R.id.ivPraise);
        ivPraise.setOnClickListener(this);
        ivShare = (ImageView) findViewById(R.id.ivShare);
        ivShare.setOnClickListener(this);
        ivDownload = (ImageView) findViewById(R.id.ivDownload);
        ivDownload.setOnClickListener(this);
        llSubmit = (LinearLayout) findViewById(R.id.llSubmit);
        etComment = (EditText) findViewById(R.id.etComment);
        etComment.addTextChangedListener(watcher);
        tvSubmit = (TextView) findViewById(R.id.tvSubmit);
        tvSubmit.setOnClickListener(this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvWeatherFlag = (TextView) findViewById(R.id.tvWeatherFlag);
        tvOtherFlag = (TextView) findViewById(R.id.tvOtherFlag);
        ivClear = (ImageView) findViewById(R.id.ivClear);
        ivClear.setOnClickListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mTXVodPlayer != null) {
                    mTXVodPlayer.seek(seekBar.getProgress());
                }
            }
        });

        if (getIntent().hasExtra("data")) {
            data = getIntent().getExtras().getParcelable("data");
            if (data != null) {
                if (!TextUtils.isEmpty(data.nickName)) {
                    tvUserName.setText(data.nickName);
                } else if (!TextUtils.isEmpty(data.userName)) {
                    tvUserName.setText(data.userName);
                } else if (!TextUtils.isEmpty(data.phoneNumber)) {
                    tvUserName.setText(data.phoneNumber);
                }

                if (!TextUtils.isEmpty(data.playCount)) {
                    tvPlayCount.setText(data.playCount + "次播放");
                }

                if (!TextUtils.isEmpty(data.praiseCount)) {
                    tvPraiseCount.setText(data.praiseCount);
                }

                //获取点赞状态
                SharedPreferences sharedPreferences = getSharedPreferences(data.getVideoId(), Context.MODE_PRIVATE);
                if (sharedPreferences.getBoolean("praiseState", false)) {
                    praiseState = true;
                    ivPraise.setImageResource(R.drawable.iv_like);
                } else {
                    praiseState = false;
                    ivPraise.setImageResource(R.drawable.iv_unlike);
                }

                if (!TextUtils.isEmpty(data.title)) {
                    tvTitle.setText(data.title);
                }

                if (!TextUtils.isEmpty(data.content)) {
                    tvContent.setText(data.content);
                    tvContent.setVisibility(View.VISIBLE);
                }

                String weatherFlag = CommonUtil.getWeatherFlag(data.weatherFlag);
                if (!TextUtils.isEmpty(weatherFlag)) {
                    tvWeatherFlag.setText(weatherFlag);
                    tvWeatherFlag.setBackgroundResource(R.drawable.corner_flag);
                    tvWeatherFlag.setVisibility(View.VISIBLE);
                }
                String otherFlag = CommonUtil.getOtherFlag(data.otherFlag);
                if (!TextUtils.isEmpty(otherFlag)) {
                    tvOtherFlag.setText(otherFlag);
                    tvOtherFlag.setBackgroundResource(R.drawable.corner_flag);
                    tvOtherFlag.setVisibility(View.VISIBLE);
                }

                if (!TextUtils.isEmpty(data.location)) {
                    tvPositon.setText("拍摄地点：" + data.location);
                }

                if (!TextUtils.isEmpty(data.workTime)) {
                    tvDate.setText("拍摄时间：" + data.workTime);
                }

                if (!TextUtils.isEmpty(data.commentCount)) {
                    tvCommentCount.setText("评论" + "（" + data.commentCount + "）");
                }

                if (!TextUtils.isEmpty(data.videoId)) {
                    //提交访问次数
                    OkHttpPlayCount("http://channellive2.tianqi.cn/weather/work/fyjp_browsecount/resourceid/" + data.videoId);
                }

                //获取评论列表
                OkHttpCommentList(CONST.GET_WORK_COMMENT_URL);
            }
        }
    }

    /**
     * 评论监听
     */
    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void afterTextChanged(Editable arg0) {
            if (!TextUtils.isEmpty(etComment.getText().toString())) {
                ivClear.setVisibility(View.VISIBLE);
                tvSubmit.setVisibility(View.VISIBLE);
            } else {
                ivClear.setVisibility(View.GONE);
                tvSubmit.setVisibility(View.GONE);
            }
        }
    };

    /**
     * 清空输入内容
     */
    private void clearContent() {
        if (etComment != null) {
            etComment.setText("");
        }
    }

    private void initVideoView() {
        mTXVodPlayer = new TXVodPlayer(mContext);
        mTXVodPlayConfig = new TXVodPlayConfig();
        mTXCloudVideoView = (TXCloudVideoView) findViewById(R.id.video_view);
        mTXCloudVideoView.setOnClickListener(this);

        showPort();
        startPlay();
    }

    /**
     * 开始播放
     * @return
     */
    private void startPlay() {
        if (isFirstPlay) {
            ivPlay.setImageResource(R.drawable.iv_pause);
            mTXVodPlayer.setPlayerView(mTXCloudVideoView);
            mTXVodPlayer.enableHardwareDecode(false);
            mTXVodPlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
            mTXVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
            mTXVodPlayer.setConfig(mTXVodPlayConfig);
            mTXVodPlayer.setVodListener(this);
            if (data != null) {
                if (!TextUtils.isEmpty(data.videoUrl)) {
                    int result = mTXVodPlayer.startPlay(data.videoUrl); // result返回值：0 success;  -1 empty url; -2 invalid url; -3 invalid playType;
                    if (result != 0) {
                        ivPlay.setImageResource(R.drawable.iv_play);
                        isPlaying = false;
                    }
                    isFirstPlay = false;
                }
            }
        }else {
            if (mTXVodPlayer.isPlaying()) {
                ivPlay.setImageResource(R.drawable.iv_play);
                mTXVodPlayer.pause();
                isPlaying = false;
            }else {
                ivPlay.setImageResource(R.drawable.iv_pause);
                mTXVodPlayer.resume();
                isPlaying = true;
            }
        }
        delayShowControl();
    }

    @Override
    public void onPlayEvent(TXVodPlayer txVodPlayer, int event, Bundle param) {
        switch (event) {
            case PLAY_EVT_PLAY_BEGIN:
                progressBar.setVisibility(View.GONE);
                hideControlLayout();
                break;
            case PLAY_EVT_PLAY_PROGRESS:
                int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS);// 播放进度, 单位是秒
                int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION);// 视频总长, 单位是秒

                if (seekBar != null) {
                    seekBar.setProgress(progress);
                    seekBar.setMax(duration);
                }
                if (tvPlayTime != null) {
                    tvPlayTime.setText(String.format(Locale.CHINA, "%02d:%02d/%02d:%02d", (progress) / 60, progress % 60, (duration) / 60, duration % 60));
                }
                break;
            case PLAY_ERR_NET_DISCONNECT:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case PLAY_EVT_PLAY_END:
                ivPlay.setImageResource(R.drawable.iv_play);
                showControlLayout();
                break;
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {

    }

    /**
     * 延时显示操作按钮
     */
    private void delayShowControl() {
        handler.removeMessages(HANDLER_DELAY);
        Message msg = handler.obtainMessage(HANDLER_DELAY);
        msg.what = HANDLER_DELAY;
        handler.sendMessageDelayed(msg, 5000);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_DELAY:
                    if (isPlaying) {
                        if (ivPlay.getVisibility() == View.VISIBLE) {
                            hideControlLayout();
                        }
                    }else {
                        showControlLayout();
                    }
                    break;

                default:
                    break;
            }

        }
    };

    private void showControlLayout() {
        reBottom.setVisibility(View.VISIBLE);
        ivPlay.setVisibility(View.VISIBLE);
    }

    private void hideControlLayout() {
        reBottom.setVisibility(View.GONE);
        ivPlay.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTXCloudVideoView != null) {
            mTXCloudVideoView.onResume();
        }
        if (mTXVodPlayer != null) {
            mTXVodPlayer.resume();
            delayShowControl();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTXCloudVideoView != null) {
            mTXCloudVideoView.onPause();
        }
        if (mTXVodPlayer != null) {
            mTXVodPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTXCloudVideoView != null) {
            mTXCloudVideoView.onDestroy();
        }
        if (mTXVodPlayer != null) {
            mTXVodPlayer.stopPlay(true);
        }
    }

    /**
     * 初始化listview
     */
    private void initListView() {
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new CommentAdapter(mContext, mList);
        mListView.setAdapter(mAdapter);
    }

    /**
     * 提交访问次数
     */
    private void OkHttpPlayCount(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                    }
                });
            }
        }).start();
    }

    /**
     * 获取评论列表
     */
    private void OkHttpCommentList(final String url) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("wid", data.getVideoId());
        builder.add("page", page + "");
        builder.add("pagesize", pageSize + "");
        builder.add("appid", CONST.APPID);
        final RequestBody body = builder.build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            return;
                        }
                        final String result = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(result)) {
                                    try {
                                        JSONObject object = new JSONObject(result);
                                        if (object != null) {
                                            if (!object.isNull("status")) {
                                                int status = object.getInt("status");
                                                if (status == 1) {//成功
                                                    if (!object.isNull("info")) {
                                                        JSONArray array = object.getJSONArray("info");
                                                        int length = array.length();
                                                        if (length <= 0) {
                                                            return;
                                                        }
                                                        mList.clear();
                                                        for (int i = 0; i < array.length(); i++) {
                                                            JSONObject obj = array.getJSONObject(i);
                                                            PhotoDto dto = new PhotoDto();
                                                            if (!obj.isNull("create_time")) {
                                                                dto.createTime = obj.getString("create_time");
                                                            }
                                                            if (!obj.isNull("username")) {
                                                                dto.userName = obj.getString("username");
                                                            }
                                                            if (!obj.isNull("comment")) {
                                                                dto.comment = EmojiMapUtil.replaceCheatSheetEmojis(obj.getString("comment"));
                                                            }
                                                            if (!obj.isNull("photo")) {
                                                                dto.portraitUrl = obj.getString("photo");
                                                            }
                                                            mList.add(dto);
                                                        }
                                                    }

                                                    tvCommentCount.setText("评论" + "（" + mList.size() + "）");
                                                    if (mAdapter != null) {
                                                        mAdapter.notifyDataSetChanged();
                                                    }

                                                } else {
                                                    //失败
                                                    if (!object.isNull("msg")) {
                                                        String msg = object.getString("msg");
                                                        if (msg != null) {
                                                            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    /**
     * 提交评论
     */
    private void OkHttpSubmitComment(final String url) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("token", MyApplication.TOKEN);
        builder.add("wid", data.videoId);
        builder.add("comment", EmojiMapUtil.replaceUnicodeEmojis(etComment.getText().toString()));
        final RequestBody body = builder.build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            return;
                        }
                        final String result = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(result)) {
                                    try {
                                        JSONObject object = new JSONObject(result);
                                        if (object != null) {
                                            if (!object.isNull("status")) {
                                                int status = object.getInt("status");
                                                if (status == 1) {//成功
                                                    clearContent();
                                                    OkHttpCommentList(CONST.GET_WORK_COMMENT_URL);
                                                } else {
                                                    //失败
                                                    if (!object.isNull("msg")) {
                                                        String msg = object.getString("msg");
                                                        if (msg != null) {
                                                            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    /**
     * 点赞
     */
    private void OkHttpPraise(final String url) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("token", MyApplication.TOKEN);
        builder.add("id", data.videoId);
        final RequestBody body = builder.build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            return;
                        }
                        final String result = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(result)) {
                                    try {
                                        JSONObject object = new JSONObject(result);
                                        if (object != null) {
                                            if (!object.isNull("status")) {
                                                int status = object.getInt("status");
                                                if (status == 1) {//成功
                                                    //保存点赞状态
                                                    SharedPreferences sharedPreferences = getSharedPreferences(data.videoId, Context.MODE_PRIVATE);
                                                    Editor editor = sharedPreferences.edit();
                                                    editor.putBoolean("praiseState", true);
                                                    editor.commit();
                                                    ivPraise.setImageResource(R.drawable.iv_like);
                                                } else {
                                                    //失败
                                                    if (!object.isNull("msg")) {
                                                        String msg = object.getString("msg");
                                                        if (msg != null) {
                                                            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    /**
     * 下载视频对话框
     */
    private void dialogDownload() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_download, null);
        TextView tvSd = (TextView) view.findViewById(R.id.tvSd);
        TextView tvHd = (TextView) view.findViewById(R.id.tvHd);
        TextView tvFhd = (TextView) view.findViewById(R.id.tvFhd);

        final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
        dialog.setContentView(view);
        dialog.show();

        tvSd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (data != null) {
                    downloadVideo(data.sd);
                }
            }
        });
        tvHd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (data != null) {
                    downloadVideo(data.hd);
                }
            }
        });
        tvFhd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (data != null) {
                    downloadVideo(data.fhd);
                }
            }
        });
    }

    /**
     * 下载视频
     * @param videoUrl
     */
    private void downloadVideo(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl)) {
            return;
        }
        DownloadManager dManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(videoUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        // 设置下载路径和文件名
        String filename = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);//获取文件名称
        File files = new File(CONST.DOWNLOAD_ADDR);
        if (!files.exists()) {
            files.mkdirs();
        }
        request.setDestinationInExternalPublicDir(files.getAbsolutePath(), filename);
        request.setDescription(filename);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");
        // 设置为可被媒体扫描器找到
        request.allowScanningByMediaScanner();
        // 设置为可见和可管理
        request.setVisibleInDownloadsUi(true);
        long refernece = dManager.enqueue(request);
//		// 把当前下载的ID保存起来
//		SharedPreferences sPreferences = mContext.getSharedPreferences("downloadplato", 0);
//		sPreferences.edit().putLong("plato", refernece).commit();

    }

    /**
     * 隐藏虚拟键盘
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (etComment != null) {
            CommonUtil.hideInputSoft(etComment, mContext);
        }
        return super.onTouchEvent(event);
    }

    private void exit() {
        if (configuration == null) {
            finish();
        } else {
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                finish();
            } else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                exit();
                break;
            case R.id.video_view:
                if (reBottom.getVisibility() == View.VISIBLE) {
                    hideControlLayout();
                }else {
                    showControlLayout();
                    delayShowControl();
                }
                break;
            case R.id.ivClear:
                clearContent();
                break;
            case R.id.ivInFull:
                if (configuration == null) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else {
                    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                }
                break;
            case R.id.ivPlay:
                startPlay();
                break;
            case R.id.tvSubmit:
                if (!TextUtils.isEmpty(etComment.getText().toString())) {
                    CommonUtil.hideInputSoft(etComment, mContext);
                    OkHttpSubmitComment(CONST.COMMENT_WORD_URL);
                }
                break;
            case R.id.ivPraise:
                if (praiseState) {
                    return;
                } else {
                    OkHttpPraise(CONST.PRAISE_WORK_URL);
                }
                break;
            case R.id.ivShare:
                if (data != null) {
                    CommonUtil.share(OnlineVideoActivity.this, data.title, data.title, data.imgUrl, CONST.WEB + data.getVideoId() + CONST.WEB_SUFFIX);
                }
                break;
            case R.id.ivDownload:
                dialogDownload();
                break;

            default:
                break;
        }
    }

}
