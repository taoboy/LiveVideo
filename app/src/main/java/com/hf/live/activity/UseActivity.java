package com.hf.live.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hf.live.R;

/**
 * 使用说明
 * @author shawn_sun
 *
 */

@SuppressLint("SimpleDateFormat")
public class UseActivity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private WebView webView = null;
	private WebSettings webSettings = null;
	private String url = "http://tianjinqixiang.tianqi.cn/navpage/";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_use);
		initWidget();
		initWebView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("使用说明");
	}
	
	/**
	 * 初始化webview
	 */
	@SuppressLint("SetJavaScriptEnabled") 
	private void initWebView() {
		webView = (WebView) findViewById(R.id.webView);
		webSettings = webView.getSettings();
		
		//支持javascript
		webSettings.setJavaScriptEnabled(true); 
		// 设置可以支持缩放 
		webSettings.setSupportZoom(true); 
		// 设置出现缩放工具 
		webSettings.setBuiltInZoomControls(false);
		//扩大比例的缩放
		webSettings.setUseWideViewPort(true);
		//自适应屏幕
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webSettings.setLoadWithOverviewMode(true);
		webView.setBackgroundColor(getResources().getColor(R.color.transparent));
		webView.loadUrl(url);
		
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
//				if (title != null) {
//					tvTitle.setText(title);
//				}
			}
		});
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String itemUrl) {
				url = itemUrl;
				webView.loadUrl(url);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (webView != null) {
			webView.reload();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView != null && webView.canGoBack()) {
				webView.goBack();
				return true;
			}else {
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
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
