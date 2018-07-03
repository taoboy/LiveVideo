package com.hf.live.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.dto.SwitchDto;
import com.hf.live.util.AutoUpdateUtil;
import com.hf.live.util.CommonUtil;
import com.hf.live.util.DataCleanManager;
import com.smartapi.pn.client.NotificationService;

import java.io.File;

/**
 * 设置
 */

public class MySettingActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private LinearLayout llBack = null;
	private TextView tvTitle,tvLocalSave,tvLocalCache,tvDataResource,tvVersion,tvLogout;
	private LinearLayout llPushNews,llLocalSave,llLocalCache,llSwitch,llVersion ;
	private ImageView ivPushNews = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_setting);
		mContext = this;
		initWidget();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.my_setting));
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		llPushNews = (LinearLayout) findViewById(R.id.llPushNews);
		llPushNews.setOnClickListener(this);
		llLocalSave = (LinearLayout) findViewById(R.id.llLocalSave);
		llLocalSave.setOnClickListener(this);
		llLocalCache = (LinearLayout) findViewById(R.id.llLocalCache);
		llLocalCache.setOnClickListener(this);
		llSwitch = (LinearLayout) findViewById(R.id.llSwitch);
		llSwitch.setOnClickListener(this);
		ivPushNews = (ImageView) findViewById(R.id.ivPushNews);
		tvLocalSave = (TextView) findViewById(R.id.tvLocalSave);
		tvLocalCache = (TextView) findViewById(R.id.tvLocalCache);
		tvDataResource = (TextView) findViewById(R.id.tvDataResource);
		llVersion = (LinearLayout) findViewById(R.id.llVersion);
		llVersion.setOnClickListener(this);
		tvVersion = (TextView) findViewById(R.id.tvVersion);
		tvLogout = (TextView) findViewById(R.id.tvLogout);
		tvLogout.setOnClickListener(this);
		
		SharedPreferences sharedPreferences = getSharedPreferences("PushInfo", Context.MODE_PRIVATE);
		if (sharedPreferences.getBoolean("pushState", true)) {
			ivPushNews.setBackgroundResource(R.drawable.setting_checkbox_on);
		}else {
			ivPushNews.setBackgroundResource(R.drawable.setting_checkbox_off);
		}
		
		try {
			String localSave = DataCleanManager.getLocalSaveSize(mContext);
			if (localSave != null) {
				tvLocalSave.setText(localSave);
			}
			String cache = DataCleanManager.getCacheSize(mContext);
			if (cache != null) {
				tvLocalCache.setText(cache);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		SharedPreferences sp = getSharedPreferences("DATASOURCE", Context.MODE_PRIVATE);
		int size = sp.getInt("size", 0);
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				SwitchDto dto = new SwitchDto();
				dto.name = sp.getString("name"+i, "");
				dto.appid = sp.getString("appid"+i, "");
				dto.isSelected = sp.getBoolean("isSelected"+i, false);
				if (dto.isSelected) {
					tvDataResource.setText(dto.name);
					break;
				}
			}
		}else {
			tvDataResource.setText(CONST.SOURCENAME);
		}

		tvVersion.setText(CommonUtil.getVersion(mContext));
		
	}
	
	/**
	 * 删除对话框
	 * @param message 标题
	 * @param content 内容
	 * @param flag 0删除本地存储，1删除缓存
	 */
	private void deleteDialog(String message, String content, final int flag) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		TextView tvContent = (TextView) view.findViewById(R.id.tvContent);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
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
				if (flag == 0) {
					DataCleanManager.clearLocalSave(mContext);
					try {
						String localSave = DataCleanManager.getLocalSaveSize(mContext);
						if (localSave != null) {
							tvLocalSave.setText(localSave);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else {
					DataCleanManager.clearCache(mContext);
					try {
						String cache = DataCleanManager.getCacheSize(mContext);
						if (cache != null) {
							tvLocalCache.setText(cache);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				dialog.dismiss();
			}
		});
	}
	
	/**
	 * 退出登录对话框
	 * @param message 标题
	 */
	private void logoutDialog(String message) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_delete, null);
		TextView tvMessage = (TextView) view.findViewById(R.id.tvMessage);
		LinearLayout llNegative = (LinearLayout) view.findViewById(R.id.llNegative);
		LinearLayout llPositive = (LinearLayout) view.findViewById(R.id.llPositive);
		
		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.setContentView(view);
		dialog.show();
		
		tvMessage.setText(message);
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
				//清除sharedPreferance里保存的用户信息
				MyApplication.clearUserInfo(mContext);

				//删除本地保存的头像
				File file = new File(CONST.PORTRAIT_ADDR);
				if (file.exists()) {
					file.delete();
				}
				
				file = new File(CONST.OLD_PORTRAIT_ADDR);
				if (file.exists()) {
					file.delete();
				}
				
				MyApplication.destoryActivity("MainActivity");
				MyApplication.destoryActivity("PersonCenterActivity");
				startActivity(new Intent(mContext, LoginActivity.class));
				finish();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;
		case R.id.llPushNews:
			SharedPreferences sharedPreferences = getSharedPreferences("PushInfo", Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			if (sharedPreferences.getBoolean("pushState", true)) {
				editor.putBoolean("pushState", false);
				editor.commit();
				ivPushNews.setBackgroundResource(R.drawable.setting_checkbox_off);
				Intent intent = new Intent(mContext, NotificationService.class);
				stopService(intent);
			}else {
				editor.putBoolean("pushState", true);
				editor.commit();
				ivPushNews.setBackgroundResource(R.drawable.setting_checkbox_on);
				Intent intent = new Intent(mContext, NotificationService.class);
				startService(intent);
			}
			break;
		case R.id.llLocalSave:
			deleteDialog(getString(R.string.sure_delete), getString(R.string.local_content), 0);
			break;
		case R.id.llLocalCache:
			deleteDialog(getString(R.string.sure_delete), getString(R.string.cache_content), 1);
			break;
		case R.id.llSwitch:
			startActivityForResult(new Intent(mContext, SwitchResourceActivity.class), 2);
			break;
		case R.id.llVersion:
			AutoUpdateUtil.checkUpdate(MySettingActivity.this, mContext, "51", getString(R.string.app_name), false);
			break;
		case R.id.tvLogout:
			logoutDialog(getString(R.string.sure_logout));
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
			case 2:
				tvDataResource.setText(CONST.SOURCENAME);
				break;

			default:
				break;
			}
		}
	}
	
}
