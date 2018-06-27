package com.hf.live.activity;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.hf.live.view.MyDialog;

public class BaseActivity extends Activity{

	private Context mContext = null;
	private MyDialog mDialog = null;
	public static boolean isShowNavigationBar = true;//是否显示导航栏

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerNavigationBar();
		mContext = this;
	}
	
	/**
	 * 初始化dialog
	 */
	public void showDialog() {
		if (mDialog == null) {
			mDialog = new MyDialog(mContext);
		}
		mDialog.show();
	}
	public void cancelDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	/**
	 * 注册导航栏监听
	 */
	private void registerNavigationBar() {
		getContentResolver().registerContentObserver(Settings.Global.getUriFor("navigationbar_is_min"), true, mNavigationStatusObserver);
		int navigationBarIsMin = Settings.Global.getInt(getContentResolver(), "navigationbar_is_min", 0);
		if (navigationBarIsMin == 1) {
			//导航键隐藏了
			isShowNavigationBar = false;
		} else {
			//导航键显示了
			isShowNavigationBar = true;
		}
	}

	private ContentObserver mNavigationStatusObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			int navigationBarIsMin = Settings.Global.getInt(getContentResolver(), "navigationbar_is_min", 0);
			if (navigationBarIsMin == 1) {
				//导航键隐藏了
				isShowNavigationBar = false;
			} else {
				//导航键显示了
				isShowNavigationBar = true;
			}
			if (navigationListener != null) {
				navigationListener.showNavigation(isShowNavigationBar);
			}
		}
	};


	public interface NavigationListener {
		void showNavigation(boolean show);
	}

	private NavigationListener navigationListener;

	public NavigationListener getNavigationListener() {
		return navigationListener;
	}

	public void setNavigationListener(NavigationListener navigationListener) {
		this.navigationListener = navigationListener;
	}
	
}
