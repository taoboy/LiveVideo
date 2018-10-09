package com.hf.live.common;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MyApplication extends Application{

	private static MyApplication instance;
	private static Map<String,Activity> destoryMap = new HashMap<>();

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		initUmeng();
		getVideoWallResource();
	}

	public static MyApplication getApplication() {
		return instance;
	}

	/**
	 * 初始化umeng
	 */
	private void initUmeng() {
		//umeng分享
		UMConfigure.init(this, "55a136d967e58e167a0019c3", "umeng", UMConfigure.DEVICE_TYPE_PHONE, "");
		PlatformConfig.setWeixin("wxde36f1bc838263b2", "29e733030c77dbda77784fc7d880dff5");
		PlatformConfig.setQQZone("1104765826", "diELThajoUq2TWUa");
		PlatformConfig.setSinaWeibo("3038972811", "fee238ac7337be352aac2042a3bb017b", "http://sns.whalecloud.com/sina2/callback");
		UMConfigure.setLogEnabled(false);
	}

	/**
	 * 获取照片墙数据源
	 */
	private void getVideoWallResource() {
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
	public static String OLDUSERNAME = "";//手机号
	public static String USERNAME = "";//手机号
	public static String GROUPID = "";//用户组id
	public static String TOKEN = "";//token
	public static String UID = "";
	public static String POINTS = "";//积分
	public static String PHOTO = "";//头像地址
	public static String NICKNAME = "";//昵称
	public static String MAIL = "";//邮箱
	public static String UNIT = "";//单位名称

	//活动
	public static String TYPE = "";//用户类型,1是普通用户，2是活动报名
	public static String COLLEGE = "";//活动报名学校
	public static String MAJOR = "";//活动报名专业
	public static String VOTES = "";//投票数
	public static String CODE = "";//报名人员编号

	public static String USERINFO = "userInfo";//userInfo sharedPreferance名称
	public static class UserInfo {
		public static final String oldUserName = "oldUserName";
		public static final String userName = "uName";
		public static final String groupId = "groupId";
		public static final String token = "token";
		public static final String uid = "uid";
		public static final String points = "points";
		public static final String photo = "photo";
		public static final String nickName = "nickName";
		public static final String mail = "mail";
		public static final String unit = "unit";

		//活动
		public static final String type = "type";
		public static final String college = "college";
		public static final String major = "major";
		public static final String votes = "votes";
		public static final String code = "code";
	}

	/**
	 * 清除用户信息
	 */
	public static void clearUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.apply();
		TOKEN = "";
		UID = "";
		OLDUSERNAME = "";
		USERNAME = "";
		NICKNAME = "";
		GROUPID = "";
		POINTS = "";
		PHOTO = "";
		MAIL = "";
		UNIT = "";

		//活动
		TYPE = "";
		COLLEGE = "";
		MAJOR = "";
		VOTES = "";
		CODE = "";
	}

	/**
	 * 保存用户信息
	 */
	public static void saveUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(UserInfo.token, TOKEN);
		editor.putString(UserInfo.uid, UID);
		editor.putString(UserInfo.oldUserName, OLDUSERNAME);
		editor.putString(UserInfo.userName, USERNAME);
		editor.putString(UserInfo.nickName, NICKNAME);
		editor.putString(UserInfo.groupId, GROUPID);
		editor.putString(UserInfo.points, POINTS);
		editor.putString(UserInfo.photo, PHOTO);
		editor.putString(UserInfo.mail, MAIL);
		editor.putString(UserInfo.unit, UNIT);

		//活动
		editor.putString(UserInfo.type, TYPE);
		editor.putString(UserInfo.college, COLLEGE);
		editor.putString(UserInfo.major, MAJOR);
		editor.putString(UserInfo.votes, VOTES);
		editor.putString(UserInfo.code, CODE);
		editor.apply();
	}

	/**
	 * 获取用户信息
	 */
	public static void getUserInfo(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
		TOKEN = sharedPreferences.getString(UserInfo.token, "");
		UID = sharedPreferences.getString(UserInfo.uid, "");
		OLDUSERNAME = sharedPreferences.getString(UserInfo.oldUserName, "");
		USERNAME = sharedPreferences.getString(UserInfo.userName, "");
		NICKNAME = sharedPreferences.getString(UserInfo.nickName, "");
		GROUPID = sharedPreferences.getString(UserInfo.groupId, "");
		POINTS = sharedPreferences.getString(UserInfo.points, "");
		PHOTO = sharedPreferences.getString(UserInfo.photo, "");
		MAIL = sharedPreferences.getString(UserInfo.mail, "");
		UNIT = sharedPreferences.getString(UserInfo.unit, "");

		//活动
		TYPE = sharedPreferences.getString(UserInfo.type, "");
		COLLEGE = sharedPreferences.getString(UserInfo.college, "");
		MAJOR = sharedPreferences.getString(UserInfo.major, "");
		VOTES = sharedPreferences.getString(UserInfo.votes, "");
		CODE = sharedPreferences.getString(UserInfo.code, "");
	}

}
