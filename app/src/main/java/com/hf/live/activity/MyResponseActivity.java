package com.hf.live.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hf.live.R;

/**
 * 免责申明
 */

public class MyResponseActivity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private WebView webView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_response);
		initWidget();
		initWebView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.my_response));
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
	}
	
	/**
	 * 初始化webview
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView() {
		webView = (WebView) findViewById(R.id.webView);
        WebSettings webSetting = webView.getSettings();     
        //支持javascript
        webSetting.setJavaScriptEnabled(true); 
  		// 设置可以支持缩放 
        webSetting.setSupportZoom(true); 
  		// 设置出现缩放工具 
        webSetting.setBuiltInZoomControls(false);
  		//扩大比例的缩放
        webSetting.setUseWideViewPort(true);
  		//自适应屏幕
        webSetting.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webSetting.setLoadWithOverviewMode(true);
        
        webView.setBackgroundColor(0); // 设置背景色
        webView.loadUrl("file:///android_asset/response.html");
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}
	
}
