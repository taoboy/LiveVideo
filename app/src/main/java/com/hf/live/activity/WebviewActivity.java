package com.hf.live.activity;

import android.os.Bundle;
import android.text.TextUtils;
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
 * 普通网页
 */

public class WebviewActivity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;
	private TextView tvTitle = null;
	private WebView webView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		initWidget();
		initWebView();
	}
	
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText("风云即拍");
	}
	
	/**
	 * 初始化webview
	 */
	private void initWebView() {
		String url = getIntent().getStringExtra("url");
		if (TextUtils.isEmpty(url)) {
			return;
		}

		webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();     
        //支持javascript
  		webSettings.setJavaScriptEnabled(true); 
  		// 设置可以支持缩放 
  		webSettings.setSupportZoom(true); 
  		// 设置可以支持缩放 
  		webSettings.setBuiltInZoomControls(true);
  		// 隐藏缩放控件
  		webSettings.setDisplayZoomControls(false);
  		//扩大比例的缩放
  		webSettings.setUseWideViewPort(true);
  		//自适应屏幕
  		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
  		webSettings.setLoadWithOverviewMode(true);
  		webView.setBackgroundColor(0); // 设置背景色
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
				webView.loadUrl(itemUrl);
				return true;
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}
		});
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
