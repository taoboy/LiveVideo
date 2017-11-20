package com.hf.live.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;
import com.hf.live.util.OkHttpUtil;
import com.hf.live.view.CircleImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 查看他人信息
 */

public class OtherInfoActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private CircleImageView ivPortrait = null;
	private TextView tvUserName = null;
	private TextView tvScore = null;
	private TextView tvNickName = null;
	private LinearLayout llPhone = null;
	private TextView tvPhone = null;
	private TextView tvMail = null;
	private TextView tvUnit = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_other_info);
		mContext = this;
		showDialog();
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvScore = (TextView) findViewById(R.id.tvScore);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		ivPortrait = (CircleImageView) findViewById(R.id.ivPortrait);
		tvNickName = (TextView) findViewById(R.id.tvNickName);
		tvMail = (TextView) findViewById(R.id.tvMail);
		tvUnit = (TextView) findViewById(R.id.tvUnit);
		llPhone = (LinearLayout) findViewById(R.id.llPhone);
		llPhone.setOnClickListener(this);
		tvPhone = (TextView) findViewById(R.id.tvPhone);

		PhotoDto data = getIntent().getExtras().getParcelable("data");
		if (data != null) {
			cancelDialog();
			if (!TextUtils.isEmpty(data.portraitUrl)) {
				downloadPortrait(data.portraitUrl);
			}

			if (!TextUtils.isEmpty(data.phoneNumber)) {
				tvUserName.setText(data.phoneNumber);
				tvPhone.setText(data.phoneNumber);
			}else {
				tvUserName.setText(data.userName);
			}

			if (!TextUtils.isEmpty(data.score)) {
				tvScore.setText(data.score);
			}

			if (!TextUtils.isEmpty(data.nickName)) {
				tvNickName.setText(data.nickName);
			}

			if (!TextUtils.isEmpty(data.mail)) {
				tvMail.setText(data.mail);
			}

			if (!TextUtils.isEmpty(data.unit)) {
				tvUnit.setText(data.unit);
			}
		}else {
			OkHttpOtherInfo();
		}
	}

	private void OkHttpOtherInfo() {
		String uid = getIntent().getStringExtra("uid");
		if (!TextUtils.isEmpty(uid)) {
			String url = "http://channellive2.tianqi.cn/weather/work/getUserById?uid="+uid;
			OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
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
									JSONObject obj = new JSONObject(result);
									if (!obj.isNull("photo")) {
										downloadPortrait(obj.getString("photo"));
									}

									String username = obj.getString("username");
									String phonenumber = obj.getString("phonenumber");
									if (!TextUtils.isEmpty(phonenumber)) {
										tvUserName.setText(phonenumber);
										tvPhone.setText(phonenumber);
									}else {
										tvUserName.setText(username);
									}

									if (!obj.isNull("points")) {
										tvScore.setText(obj.getString("points"));
									}

									if (!obj.isNull("nickname")) {
										tvNickName.setText(obj.getString("nickname"));
									}

									if (!obj.isNull("mail")) {
										tvMail.setText(obj.getString("mail"));
									}

									if (!obj.isNull("department")) {
										tvUnit.setText(obj.getString("department"));
									}
									cancelDialog();
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}
					});
				}
			});
		}else {
			cancelDialog();
		}
	}

	/**
	 * 下载头像保存在本地
	 */
	private void downloadPortrait(String imgUrl) {
		AsynLoadTask task = new AsynLoadTask(new AsynLoadCompleteListener() {
			@Override
			public void loadComplete(Bitmap bitmap) {
				if (bitmap != null) {
					ivPortrait.setImageBitmap(bitmap);
				}
			}
		}, imgUrl);
		task.execute();
	}

	private interface AsynLoadCompleteListener {
		void loadComplete(Bitmap bitmap);
	}

	private class AsynLoadTask extends AsyncTask<Void, Bitmap, Bitmap> {

		private String imgUrl;
		private AsynLoadCompleteListener completeListener;

		private AsynLoadTask(AsynLoadCompleteListener completeListener, String imgUrl) {
			this.imgUrl = imgUrl;
			this.completeListener = completeListener;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Bitmap... values) {
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bitmap = CommonUtil.getHttpBitmap(imgUrl);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (completeListener != null) {
				completeListener.loadComplete(bitmap);
			}
		}
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
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + message));
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
		case R.id.llPhone:
			String no = tvPhone.getText().toString();
			if (!TextUtils.isEmpty(no)) {
				dialDialog("拨打电话", no);
			}
			break;

		default:
			break;
		}
	}
	
}
