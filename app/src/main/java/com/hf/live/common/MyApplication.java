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
}
