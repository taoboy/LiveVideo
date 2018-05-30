package com.hf.live.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.hf.live.R;

public class MyDialog extends Dialog {

	private Context mContext;
	private ImageView imageView;
	private TextView tvPercent;

	public MyDialog(Context context) {
		super(context);
		mContext = context;
	}

	public void setStyle(int featureId) {
		requestWindowFeature(featureId);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawableResource(R.color.transparent);
		setContentView(R.layout.layout_loading);
		initWidget();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		imageView = (ImageView) findViewById(R.id.imageView);
		tvPercent = (TextView) findViewById(R.id.tvPercent);

		Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.round_animation);
		imageView.startAnimation(animation);
	}

	public void setPercent(int percent) {
		tvPercent.setText(percent+"%");
	}

}
