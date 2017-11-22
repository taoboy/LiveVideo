package com.hf.live.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyApplication extends Application{
	
	private static Map<String,Activity> destoryMap = new HashMap<String, Activity>();

	@Override
	public void onCreate() {
		super.onCreate();

		//获取视频墙选中的数据源信息
		SharedPreferences sp = getSharedPreferences("DATASOURCE", Context.MODE_PRIVATE);
		int size = sp.getInt("size", 0);
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				boolean isSelected = sp.getBoolean("isSelected"+i, false);
				if (isSelected) {
					CONST.APPID = sp.getString("appid"+i, "");
					CONST.SOURCENAME = sp.getString("name"+i, "");
					break;
				}
			}
		}
	}

	{
		//umeng分享的平台注册
		PlatformConfig.setWeixin("wxde36f1bc838263b2", "29e733030c77dbda77784fc7d880dff5");
		PlatformConfig.setQQZone("1104765826", "diELThajoUq2TWUa");
		Config.DEBUG = false;
	}
	
	/**
     * 添加到销毁队列
     * @param activity 要销毁的activity
     */
    public static void addDestoryActivity(Activity activity,String activityName) {
        destoryMap.put(activityName,activity);
    }
    
	/**
	*销毁指定Activity
	*/
    public static void destoryActivity(String activityName) {
       Set<String> keySet=destoryMap.keySet();
        for (String key:keySet){
            destoryMap.get(key).finish();
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
	public static void clearUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
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

	/**
	 * 保存用户信息
	 */
	public static void saveUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(UserInfo.token, TOKEN);
		editor.putString(UserInfo.oldUserName, OLDUSERNAME);
		editor.putString(UserInfo.userName, USERNAME);
		editor.putString(UserInfo.nickName, NICKNAME);
		editor.putString(UserInfo.groupId, GROUPID);
		editor.putString(UserInfo.points, POINTS);
		editor.putString(UserInfo.photo, PHOTO);
		editor.putString(UserInfo.mail, MAIL);
		editor.putString(UserInfo.unit, UNIT);
		editor.commit();
	}

	/**
	 * 获取用户信息
	 */
	public static void getUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		TOKEN = sharedPreferences.getString(UserInfo.token, null);
		OLDUSERNAME = sharedPreferences.getString(UserInfo.oldUserName, null);
		USERNAME = sharedPreferences.getString(UserInfo.userName, null);
		NICKNAME = sharedPreferences.getString(UserInfo.nickName, null);
		GROUPID = sharedPreferences.getString(UserInfo.groupId, null);
		POINTS = sharedPreferences.getString(UserInfo.points, null);
		PHOTO = sharedPreferences.getString(UserInfo.photo, null);
		MAIL = sharedPreferences.getString(UserInfo.mail, null);
		UNIT = sharedPreferences.getString(UserInfo.unit, null);
	}

}
