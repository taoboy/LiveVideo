package com.hf.live.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.adapter.ScoreRankAdapter;
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
import okhttp3.Request;
import okhttp3.Response;

/**
 * 积分排行
 */

public class ScoreRankActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext = null;
    private LinearLayout llBack = null;
    private TextView tvTitle = null;
    private ListView listView = null;
    private ScoreRankAdapter mAdapter = null;
    private List<PhotoDto> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_rank);
        mContext = this;
        showDialog();
        initWidget();
        initListView();
    }

    private void initWidget() {
        llBack = (LinearLayout) findViewById(R.id.llBack);
        llBack.setOnClickListener(this);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText("积分排行");

        String url = "http://channellive2.tianqi.cn/weather/work/getTopScore20Users";
        OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    return;
                }
                String result = response.body().string();
                if (!TextUtils.isEmpty(result)) {
                    try {
                        JSONArray array = new JSONArray(result);
                        mList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject itemObj = array.getJSONObject(i);
                            PhotoDto dto = new PhotoDto();
                            if (!itemObj.isNull("username")) {
                                dto.userName = itemObj.getString("username");
                            }
                            if (!itemObj.isNull("phonenumber")) {
                                dto.phoneNumber = itemObj.getString("phonenumber");
                            }
                            if (!itemObj.isNull("nickname")) {
                                dto.nickName = itemObj.getString("nickname");
                            }
                            if (!itemObj.isNull("points")) {
                                dto.score = itemObj.getString("points");
                            }
                            if (!itemObj.isNull("uid")) {
                                dto.uid = itemObj.getString("uid");
                            }
                            if (!itemObj.isNull("photo")) {
                                dto.portraitUrl = itemObj.getString("photo");
                            }
                            if (!itemObj.isNull("mail")) {
                                dto.mail = itemObj.getString("mail");
                            }
                            if (!itemObj.isNull("department")) {
                                dto.unit = itemObj.getString("department");
                            }
                            mList.add(dto);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mList.size() > 0 && mAdapter != null) {
                                    mAdapter.notifyDataSetChanged();
                                }
                                cancelDialog();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void initListView() {
        listView = (ListView) findViewById(R.id.listView);
        mAdapter = new ScoreRankAdapter(mContext, mList);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PhotoDto dto = mList.get(position);
                Intent intent = new Intent(mContext, OtherInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("data", dto);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
        }
    }
}
