package com.hf.live.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.hf.live.manager.SystemStatusManager;
import com.hf.live.R;
import com.hf.live.util.CommonUtil;
import com.hf.live.view.MyDialog;

public class BaseActivity extends Activity{

	private Context mContext = null;
	private MyDialog mDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		CommonUtil.getUserInfo(mContext);
		setTranslucentStatus();
	}
	
	/**
	 * 设置状态栏背景状态
	 */
	@SuppressLint("InlinedApi") 
	private void setTranslucentStatus() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window win = getWindow();
			WindowManager.LayoutParams winParams = win.getAttributes();
			final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
			winParams.flags |= bits;
			win.setAttributes(winParams);
		}
		SystemStatusManager tintManager = new SystemStatusManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(R.drawable.bg_title_bar);// 状态栏无背景
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

	//本地保存用户信息参数
	public static String OLDUSERNAME = null;//手机号
	public static String USERNAME = null;//手机号
	public static String GROUPID = null;//用户组id
	public static String TOKEN = null;//token
	public static String POINTS = null;//积分
	public static String PHOTO = null;//头像地址
	public static String NICKNAME = null;//昵称
	public static String MAIL = null;//邮箱
	public static String UNIT = null;//单位名称
	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static class UserInfo {
		public static final String oldUserName = "oldUserName";
		public static final String userName = "uName";
		public static final String groupId = "groupId";
		public static final String token = "token";
		public static final String points = "points";
		public static final String photo = "photo";
		public static final String nickName = "nickName";
		public static final String mail = "mail";
		public static final String unit = "unit";
	}

	/**
	 * 清除用户信息
	 */
	public void clearUserInfo() {
		SharedPreferences sharedPreferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
		TOKEN = null;
		OLDUSERNAME = null;
		USERNAME = null;
		NICKNAME = null;
		GROUPID = null;
		POINTS = null;
		PHOTO = null;
		MAIL = null;
		UNIT = null;
	}
	
}
