package com.hf.live.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.adapter.SearchHistoryAdapter;
import com.hf.live.adapter.VideoWallAdapter;
import com.hf.live.common.CONST;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 搜索视频
 */

public class SearchVideoActivity extends BaseActivity implements View.OnClickListener{

    private Context mContext;
    private LinearLayout llBack;
    private EditText etSearch;
    private ImageView ivClear;
    private TextView tvSearch;
    private LinearLayout llHistory;
    private ListView listView1 = null;
    private SearchHistoryAdapter adapter1 = null;
    private List<PhotoDto> list1 = new ArrayList<>();
    private ListView listView2 = null;
    private VideoWallAdapter adapter2 = null;
    private List<PhotoDto> list2 = new ArrayList<>();
    private int page = 1;
    private int pageSize = 20;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_video);
        mContext = this;
        initWidget();
        initListView1();
        initListView2();
    }
    
    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvSearch = (TextView) findViewById(R.id.tvSearch);
        tvSearch.setOnClickListener(this);
        ivClear = (ImageView) findViewById(R.id.ivClear);
        ivClear.setOnClickListener(this);
        llHistory = (LinearLayout) findViewById(R.id.llHistory);
        etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(etSearch.getText().toString())) {
                    ivClear.setVisibility(View.GONE);
                    tvSearch.setTextColor(getResources().getColor(R.color.text_color4));
                }else {
                    ivClear.setVisibility(View.VISIBLE);
                    tvSearch.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });
    }

    /**
     * 初始化listview
     */
    private void initListView1() {
        readHistory();

        listView1 = (ListView) findViewById(R.id.listView1);
        adapter1 = new SearchHistoryAdapter(mContext, list1);
        listView1.setAdapter(adapter1);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                PhotoDto dto = list1.get(arg2);
                if (!TextUtils.isEmpty(dto.history)) {
                    etSearch.setText(dto.history);
                    etSearch.setSelection(dto.history.length());
                    search();
                }
            }
        });
        listView1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteDialog("确定要删除该条历史搜索？", position);
                return true;
            }
        });
    }

    /**
     * 删除对话框
     * @param message 标题
     */
    private void deleteDialog(String message, final int position) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_delete, null);
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
        LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

        final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
        dialog.setContentView(view);
        dialog.show();

        tvMessage.setText(message);
        llNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });

        llPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                list1.remove(position);
                if (adapter1 != null) {
                    adapter1.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * 初始化listview
     */
    private void initListView2() {
        listView2 = (ListView) findViewById(R.id.listView2);
        adapter2 = new VideoWallAdapter(mContext, list2);
        listView2.setAdapter(adapter2);
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                PhotoDto dto = list2.get(arg2);
                Intent intent = new Intent();
                if (dto.getWorkstype().equals("imgs")) {
                    intent.setClass(mContext, OnlinePictureActivity.class);
                }else {
                    intent.setClass(mContext, OnlineVideoActivity.class);
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable("data", dto);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        listView2.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition() == view.getCount() - 1) {
                    page += 1;
                    OkHttpVideoList(CONST.GET_VIDEO_PIC_URL, etSearch.getText().toString());
                }
            }
            @Override
            public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
            }
        });
    }

    /**
     * 获取视频列表
     */
    private void OkHttpVideoList(final String url, String search) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("appid", CONST.APPID);
        builder.add("page", page+"");
        builder.add("pagesize", pageSize+"");
        if (!TextUtils.isEmpty(search)) {
            builder.add("search", search);
        }
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
                                                int status  = object.getInt("status");
                                                if (status == 1) {//成功
                                                    if (!object.isNull("info")) {
                                                        JSONArray array = object.getJSONArray("info");
                                                        for (int i = 0; i < array.length(); i++) {
                                                            JSONObject obj = array.getJSONObject(i);
                                                            PhotoDto dto = new PhotoDto();
                                                            if (!obj.isNull("id")) {
                                                                dto.videoId = obj.getString("id");
                                                            }
                                                            if (!obj.isNull("title")) {
                                                                dto.title = obj.getString("title");
                                                            }
                                                            if (!obj.isNull("content")) {
                                                                dto.content = obj.getString("content");
                                                            }
                                                            if (!obj.isNull("create_time")) {
                                                                dto.createTime = obj.getString("create_time");
                                                            }
                                                            if (!obj.isNull("location")) {
                                                                dto.location = obj.getString("location");
                                                            }
                                                            if (!obj.isNull("citycode")) {
                                                                dto.adcode = obj.getString("citycode");
                                                            }
                                                            if (!obj.isNull("nickname")) {
                                                                dto.nickName = obj.getString("nickname");
                                                            }
                                                            if (!obj.isNull("username")) {
                                                                dto.userName = obj.getString("username");
                                                            }
                                                            if (!obj.isNull("phonenumber")) {
                                                                dto.phoneNumber = obj.getString("phonenumber");
                                                            }
                                                            if (!obj.isNull("praise")) {
                                                                dto.praiseCount = obj.getString("praise");
                                                            }
                                                            if (!obj.isNull("browsecount")) {
                                                                dto.playCount = obj.getString("browsecount");
                                                            }
                                                            if (!obj.isNull("comments")) {
                                                                dto.commentCount = obj.getString("comments");
                                                            }
                                                            if (!obj.isNull("work_time")) {
                                                                dto.workTime = obj.getString("work_time");
                                                            }
                                                            if (!obj.isNull("workstype")) {
                                                                dto.workstype = obj.getString("workstype");
                                                            }
                                                            if (!obj.isNull("weather_flag")) {
                                                                dto.weatherFlag = obj.getString("weather_flag");
                                                            }
                                                            if (!obj.isNull("other_flags")) {
                                                                dto.otherFlag = obj.getString("other_flags");
                                                            }
                                                            if (!obj.isNull("worksinfo")) {
                                                                JSONObject workObj = new JSONObject(obj.getString("worksinfo"));
                                                                //视频
                                                                if (!workObj.isNull("video")) {
                                                                    JSONObject video = workObj.getJSONObject("video");
                                                                    if (!video.isNull("ORG")) {//腾讯云结构解析
                                                                        JSONObject ORG = video.getJSONObject("ORG");
                                                                        if (!ORG.isNull("url")) {
                                                                            dto.videoUrl = ORG.getString("url");
                                                                        }
                                                                        if (!video.isNull("SD")) {
                                                                            JSONObject SD = video.getJSONObject("SD");
                                                                            if (!SD.isNull("url")) {
                                                                                dto.sd = SD.getString("url");
                                                                            }
                                                                        }
                                                                        if (!video.isNull("HD")) {
                                                                            JSONObject HD = video.getJSONObject("HD");
                                                                            if (!HD.isNull("url")) {
                                                                                dto.hd = HD.getString("url");
                                                                                dto.videoUrl = HD.getString("url");
                                                                            }
                                                                        }
                                                                        if (!video.isNull("FHD")) {
                                                                            JSONObject FHD = video.getJSONObject("FHD");
                                                                            if (!FHD.isNull("url")) {
                                                                                dto.fhd = FHD.getString("url");
                                                                            }
                                                                        }
                                                                    }else {
                                                                        dto.videoUrl = video.getString("url");
                                                                    }
                                                                }
                                                                if (!workObj.isNull("thumbnail")) {
                                                                    JSONObject imgObj = new JSONObject(workObj.getString("thumbnail"));
                                                                    if (!imgObj.isNull("url")) {
                                                                        dto.imgUrl = imgObj.getString("url");
                                                                    }
                                                                }

                                                                //上传的图片地址，最多9张
                                                                List<String> urlList = new ArrayList<>();
                                                                if (!workObj.isNull("imgs1")) {
                                                                    JSONObject imgObj = new JSONObject(workObj.getString("imgs1"));
                                                                    if (!imgObj.isNull("url")) {
                                                                        urlList.add(imgObj.getString("url"));
                                                                        dto.imgUrl = imgObj.getString("url");
                                                                    }
                                                                }
                                                                if (!workObj.isNull("imgs2")) {
                                                                    JSONObject imgObj = new JSONObject(workObj.getString("imgs2"));
                                                                    if (!imgObj.isNull("url")) {
                                                                        urlList.add(imgObj.getString("url"));
                                                                    }
                                                                }
                                                                if (!workObj.isNull("imgs3")) {
                                                                    JSONObject imgObj = new JSONObject(workObj.getString("imgs3"));
                                                                    if (!imgObj.isNull("url")) {
                                                                        urlList.add(imgObj.getString("url"));
                                                                    }
                                                                }
                                                                if (!workObj.isNull("imgs4")) {
                                                                    JSONObject imgObj = new JSONObject(workObj.getString("imgs4"));
                                                                    if (!imgObj.isNull("url")) {
                                                                        urlList.add(imgObj.getString("url"));
                                                                    }
                                                                }
                                                                if (!workObj.isNull("imgs5")) {
                                                                    JSONObject imgObj = new JSONObject(workObj.getString("imgs5"));
                                                                    if (!imgObj.isNull("url")) {
                                                                        urlList.add(imgObj.getString("url"));
                                                                    }
                                                                }
                                                                if (!workObj.isNull("imgs6")) {
                                                                    JSONObject imgObj = new JSONObject(workObj.getString("imgs6"));
                                                                    if (!imgObj.isNull("url")) {
                                                                        urlList.add(imgObj.getString("url"));
                                                                    }
                                                                }
                                                                if (!workObj.isNull("imgs7")) {
                                                                    JSONObject imgObj = new JSONObject(workObj.getString("imgs7"));
                                                                    if (!imgObj.isNull("url")) {
                                                                        urlList.add(imgObj.getString("url"));
                                                                    }
                                                                }
                                                                if (!workObj.isNull("imgs8")) {
                                                                    JSONObject imgObj = new JSONObject(workObj.getString("imgs8"));
                                                                    if (!imgObj.isNull("url")) {
                                                                        urlList.add(imgObj.getString("url"));
                                                                    }
                                                                }
                                                                if (!workObj.isNull("imgs9")) {
                                                                    JSONObject imgObj = new JSONObject(workObj.getString("imgs9"));
                                                                    if (!imgObj.isNull("url")) {
                                                                        urlList.add(imgObj.getString("url"));
                                                                    }
                                                                }
                                                                dto.urlList.addAll(urlList);
                                                            }

                                                            if (!TextUtils.isEmpty(dto.workTime)) {
                                                                list2.add(dto);
                                                            }
                                                        }
                                                    }

                                                    if (list2.isEmpty()) {
                                                        Toast.makeText(mContext, "没有符合条件的数据！", Toast.LENGTH_LONG).show();
                                                    }

                                                    if (list2.size() > 0 && adapter2 != null) {
                                                        adapter2.notifyDataSetChanged();
                                                        listView2.setVisibility(View.VISIBLE);
                                                        llHistory.setVisibility(View.GONE);
                                                    }
                                                    cancelDialog();

                                                }else {
                                                    //失败
                                                    if (!object.isNull("msg")) {
                                                        String msg = object.getString("msg");
                                                        if (!TextUtils.isEmpty(msg)) {
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

    private void search() {
        if (!TextUtils.isEmpty(etSearch.getText().toString())) {
            saveHistory();
            showDialog();
            list2.clear();
            page = 1;
            OkHttpVideoList(CONST.GET_VIDEO_PIC_URL, etSearch.getText().toString());
        }
    }

    private void readHistory() {
        list1.clear();
        SharedPreferences sharedPreferences = getSharedPreferences("HISTORY", Context.MODE_PRIVATE);
        String keyword = sharedPreferences.getString("keyword", "");
        if (!TextUtils.isEmpty(keyword)) {
            String[] values = keyword.split(",");
            for (int i = 0; i < values.length; i++) {
                PhotoDto dto = new PhotoDto();
                dto.history = values[i];
                list1.add(dto);
            }
        }

        if (adapter1 != null) {
            adapter1.notifyDataSetChanged();
        }
    }

    private void saveHistory() {
        String keyword = "";
        if (!TextUtils.isEmpty(etSearch.getText().toString())) {
            keyword = etSearch.getText().toString()+",";
        }

        for (int i = 0; i < list1.size(); i++) {
            PhotoDto dto = list1.get(i);
            if (TextUtils.equals(keyword, dto.history+",")) {
                list1.remove(i);
                break;
            }
        }

        for (int i = 0; i < list1.size(); i++) {
            PhotoDto dto = list1.get(i);
            keyword += dto.history+",";
        }

        SharedPreferences sharedPreferences = getSharedPreferences("HISTORY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("keyword", keyword);
        editor.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveHistory();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                saveHistory();
                finish();
                break;
            case R.id.ivClear:
                etSearch.setText("");
                page = 1;
                list2.clear();
                if (adapter2 != null) {
                    adapter2.notifyDataSetChanged();
                    listView2.setVisibility(View.GONE);
                }
                llHistory.setVisibility(View.VISIBLE);
                readHistory();
                break;
            case R.id.tvSearch:
                search();
                break;
        }
    }
    
}
