package com.hf.live.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.util.CommonUtil;

/**
 * 关于
 */

public class MyAboutActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvAddress = null;//网址
	private TextView tvVersion = null;//版本号
	private TextView tvHotline = null;//热线
	private ImageView ivLogo = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_about);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.my_about));
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvAddress = (TextView) findViewById(R.id.tvAddress);
		tvAddress.setText(getString(R.string.web_addr));
		tvAddress.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		tvAddress.getPaint().setAntiAlias(true);
		tvAddress.setOnClickListener(this);
		tvVersion = (TextView) findViewById(R.id.tvVersion);
		tvHotline = (TextView) findViewById(R.id.tvHotline);
		tvHotline.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		tvHotline.getPaint().setAntiAlias(true);
		tvHotline.setOnClickListener(this);
		ivLogo = (ImageView) findViewById(R.id.ivLogo);
		
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		if (bitmap != null) {
			Bitmap newBitmap = CommonUtil.getRoundedCornerBitmap(bitmap, 10);
			if (newBitmap != null) {
				ivLogo.setImageBitmap(newBitmap);
			}
		}
		
		tvVersion.setText(CommonUtil.getVersion(mContext));
	}
	
	/**
	 * 拨打电话对话框
	 * @param message
	 * @param content
	 */
	private void dialDialog(final String message, final String content) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.delete_dialog, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		TextView tvPositive = (TextView) view.findViewById(R.id.tvPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvPositive.setText(getString(R.string.dial_phone));
		tvMessage.setText(message);
		tvContent.setText(content);
		tvContent.setVisibility(View.VISIBLE);
		
		llNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		
		llPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:" + message));
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
		case R.id.tvAddress:
			Intent intentAddr = new Intent(mContext, MyAboutDetailActivity.class);
			intentAddr.putExtra("web", tvAddress.getText().toString());
			startActivity(intentAddr);
			break;
		case R.id.tvHotline:
			dialDialog(getString(R.string.service_hotline2), getString(R.string.hotline_num));
			break;

		default:
			break;
		}
	}
	
}
