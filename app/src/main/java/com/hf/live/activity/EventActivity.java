package com.hf.live.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.squareup.picasso.Picasso;

/**
 * 气象活动
 */

public class EventActivity extends BaseActivity implements View.OnClickListener {

    private TextView tvUpload, tvCheck;
    private LinearLayout llIntro;
    private ImageView ivLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        initWidget();
    }

    private void initWidget() {
        tvUpload = (TextView) findViewById(R.id.tvUpload);
        tvUpload.setOnClickListener(this);
        tvCheck = (TextView) findViewById(R.id.tvCheck);
        tvCheck.setOnClickListener(this);
        llIntro = (LinearLayout) findViewById(R.id.llIntro);
        llIntro.setOnClickListener(this);
        ivLogo = (ImageView) findViewById(R.id.ivLogo);

        if (getIntent().hasExtra("logoUrl")) {
            String logoUrl = getIntent().getStringExtra("logoUrl");
            if (!TextUtils.isEmpty(logoUrl)) {
                Picasso.with(this).load(logoUrl).into(ivLogo);
            }
        }

        if (getIntent().hasExtra("showUrl")) {
            String showUrl = getIntent().getStringExtra("showUrl");
            if (!TextUtils.isEmpty(showUrl)) {
                tvCheck.setVisibility(View.VISIBLE);
                tvCheck.setTag(showUrl);
            }else {
                tvCheck.setVisibility(View.GONE);
            }
        }
    }

    private void selectDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_delete, null);
        TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        TextView tvNegtive = (TextView) view.findViewById(R.id.tvNegtive);
        TextView tvPositive = (TextView) view.findViewById(R.id.tvPositive);
        LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
        LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);

        tvNegtive.setText("拍摄");
        tvPositive.setText("相册");

        final Dialog dialog = new Dialog(this, R.style.CustomProgressDialog);
        dialog.setContentView(view);
        dialog.show();

        tvMessage.setText("选择上传方式");
        llNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                Intent intent = new Intent(EventActivity.this, VideoRecordActivity.class);
                intent.putExtra("appid", "26");//活动专用频道
                startActivity(intent);
            }
        });

        llPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                Intent intent = new Intent(EventActivity.this, SelectVideoActivity.class);
                intent.putExtra("appid", "26");//活动专用频道
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.tvUpload:
                selectDialog();
                break;
            case R.id.tvCheck:
                intent = new Intent(this, WebviewActivity2.class);
                intent.putExtra("url", tvCheck.getTag()+"");
                startActivity(intent);
                break;
            case R.id.llIntro:
                intent = new Intent(this, WebviewActivity2.class);
                intent.putExtra("url", "http://channellive2.tianqi.cn/Public/htmls/zhuboinfo.html");
                startActivity(intent);
                break;
        }
    }
}
