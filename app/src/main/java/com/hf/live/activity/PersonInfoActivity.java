package com.hf.live.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.view.CircleImageView;
import com.scene.net.Net;

import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 修改个人信息
 */

public class PersonInfoActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private TextView tvPhone = null;
	private TextView tvScore = null;
	private LinearLayout llPortrait = null;
	private CircleImageView ivPortrait = null;
	private LinearLayout llNickName = null;
	private TextView tvNickName = null;
	private LinearLayout llMail = null;
	private TextView tvMail = null;
	private LinearLayout llUnit = null;
	private TextView tvUnit = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person_info);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("个人信息");
		tvScore = (TextView) findViewById(R.id.tvScore);
		tvPhone = (TextView) findViewById(R.id.tvPhone);
		llPortrait = (LinearLayout) findViewById(R.id.llPortrait);
		llPortrait.setOnClickListener(this);
		ivPortrait = (CircleImageView) findViewById(R.id.ivPortrait);
		llNickName = (LinearLayout) findViewById(R.id.llNickName);
		llNickName.setOnClickListener(this);
		tvNickName = (TextView) findViewById(R.id.tvNickName);
		llMail = (LinearLayout) findViewById(R.id.llMail);
		llMail.setOnClickListener(this);
		tvMail = (TextView) findViewById(R.id.tvMail);
		llUnit = (LinearLayout) findViewById(R.id.llUnit);
		llUnit.setOnClickListener(this);
		tvUnit = (TextView) findViewById(R.id.tvUnit);
		
		getPortrait();
		if (!TextUtils.isEmpty(MyApplication.NICKNAME)) {
			tvNickName.setText(MyApplication.NICKNAME);
		}
		if (!TextUtils.isEmpty(MyApplication.USERNAME)) {
			tvPhone.setText(MyApplication.USERNAME);
		}
		if (!TextUtils.isEmpty(MyApplication.MAIL)) {
			tvMail.setText(MyApplication.MAIL);
		}
		if (!TextUtils.isEmpty(MyApplication.POINTS)) {
			tvScore.setText(MyApplication.POINTS);
		}
		if (!TextUtils.isEmpty(MyApplication.UNIT)) {
			tvUnit.setText(MyApplication.UNIT);
		}
	}
	
	/**
	 * 获取相册
	 */
	private void getAlbum() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.putExtra("crop", "false");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
        startActivityForResult(intent, 0);
	}
	
	/**
	 * 上传图片
	 * @param url 接口地址
	 */
	private void uploadPortrait(String url) {
		AjaxParams params = new AjaxParams();
		params.put("token", MyApplication.TOKEN);
		
		try {
			params.put("photo", new File(CONST.PORTRAIT_ADDR));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		Net.post(url, params, new AjaxCallBack<String>() {
			@Override
			public void onSuccess(String t) {
				super.onSuccess(t);
				getPortrait();
			}

			@Override
			public void onLoading(long count, long current) {
				super.onLoading(count, current);
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				super.onFailure(t, errorNo, strMsg);
				Toast.makeText(mContext, getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	/**
	 * 获取头像
	 */
	private void getPortrait() {
		Bitmap bitmap = BitmapFactory.decodeFile(CONST.PORTRAIT_ADDR);
		if (bitmap != null) {
			ivPortrait.setImageBitmap(bitmap);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_OK);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.llPortrait:
			getAlbum();
			break;
		case R.id.llNickName:
			Intent intent = new Intent(mContext, ModifyInfoActivity.class);
			intent.putExtra("title", "昵称");
			intent.putExtra("content", MyApplication.NICKNAME);
			startActivityForResult(intent, 1);
			break;
		case R.id.llMail:
			intent = new Intent(mContext, ModifyInfoActivity.class);
			intent.putExtra("title", "邮箱");
			intent.putExtra("content", MyApplication.MAIL);
			startActivityForResult(intent, 2);
			break;
		case R.id.llUnit:
			intent = new Intent(mContext, ModifyInfoActivity.class);
			intent.putExtra("title", "单位名称");
			intent.putExtra("content", MyApplication.UNIT);
			startActivityForResult(intent, 3);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 0:
				if (data == null) {
					return;
				}

				Bitmap bitmap = null;
				Uri uri = data.getData();
				FileOutputStream fos = null;
				if (uri == null) {
					bitmap = data.getParcelableExtra("data");
				}else {
					try {
						ContentResolver resolver = getContentResolver();
						bitmap = MediaStore.Images.Media.getBitmap(resolver, uri);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
					}
				}
				
				try {
					File files = new File(CONST.SDCARD_PATH);
					if (!files.exists()) {
						files.mkdirs();
					}
					
					fos = new FileOutputStream(CONST.PORTRAIT_ADDR);
					if (bitmap != null && fos != null) {
						bitmap.compress(CompressFormat.PNG, 100, fos);
						
						if (bitmap != null && !bitmap.isRecycled()) {
							bitmap.recycle();
							bitmap = null;
						}
						
						uploadPortrait(CONST.MODIFY_USERINFO_URL);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				break;
			case 1:
				if (!TextUtils.isEmpty(MyApplication.NICKNAME)) {
					tvNickName.setText(MyApplication.NICKNAME);
				}
				break;
			case 2:
				if (!TextUtils.isEmpty(MyApplication.MAIL)) {
					tvMail.setText(MyApplication.MAIL);
				}
				break;
			case 3:
				if (!TextUtils.isEmpty(MyApplication.UNIT)) {
					tvUnit.setText(MyApplication.UNIT);
				}
				break;

			default:
				break;
			}
		}
	}
	
}
