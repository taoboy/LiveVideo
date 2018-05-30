package com.hf.live.common;

import android.os.Environment;

import com.hf.live.R;

public class CONST {

	public static String SHOWGUIDE = "show_guide";
	public static String VERSION = "version";
	public static String APPID = "0";
	public static String SOURCENAME = "气象频道";
	public static String noValue = "--";
	
	//接口地址
	public static String LOGIN_URL = "http://channellive2.tianqi.cn/Weather/User/Login3";//登陆接口
	public static String REGISTER_URL = "http://channellive2.tianqi.cn/weather/user/Register";//注册接口
	public static String MODIFY_USERINFO_URL = "http://channellive2.tianqi.cn/weather/user/update";//修改用户信息接口
	public static String UPLOAD_VIDEO_PIC_URL = "http://channellive2.tianqi.cn/weather/Tensent/upload";//上传视频、图片接口
	public static String GET_VIDEO_PIC_URL = "http://channellive2.tianqi.cn/weather/work/getwork";//获取上传视频、图片接口
	public static String GET_CHECK_LIST = "http://channellive2.tianqi.cn/weather/work/getallwork";//获取视频审核列表
	public static String CHECK_VIDEO = "http://channellive2.tianqi.cn/Weather/work/workCheck";//视频审核
	public static String GET_MY_UPLOAD_URL = "http://channellive2.tianqi.cn/weather/work/getmywork";//获取我的上传作品接口
	public static String GET_MY_MESSAGE_URL = "http://channellive2.tianqi.cn/weather/message/newmessage";//获取用户消息接口
	public static String GET_MY_MESSAGE_COUNT_URL = "http://channellive2.tianqi.cn/weather/message/newcount";//获取用户消息个数接口
	public static String GET_WORK_DETAIL_URL = "http://channellive2.tianqi.cn/weather/work/getworkinfo";//获取某个作品详情接口
	public static String GET_WORK_COMMENT_URL = "http://channellive2.tianqi.cn/weather/comment/getcomment";//获取视频评论信息接口
	public static String COMMENT_WORD_URL = "http://channellive2.tianqi.cn/weather/comment/savecomment";//作品评论提交接口
	public static String PRAISE_WORK_URL = "http://channellive2.tianqi.cn/weather/work/praise";//对某个作品点赞
	public static String GET_SCORE_URL = "http://channellive2.tianqi.cn/weather/Pointsrecord/getrecord";//查询积分接口
	public static String EXCHANGE_SCORE_URL = "http://channellive2.tianqi.cn/weather/Pointsexchange/saveRecord";//积分兑换接口
	public static String EXCHANGE_SCORE_AUTHORITY_URL = "http://channellive2.tianqi.cn/weather/Pointsexchange/check";//是否可以兑换积分权限接口
	
	//广播
//	public static String REFRESH_USERINFO = "refresh_userinfo";//刷新个人信息
	public static String REFRESH_UPLOAD = "refresh_upload";//刷新已上传的图片、视频信息
	public static String REFRESH_NOTUPLOAD = "refresh_notupload";//刷新未上传的图片、视频信息

	//下拉刷新progresBar四种颜色
	public static final int color1 = R.color.refresh_color1;
	public static final int color2 = R.color.refresh_color2;
	public static final int color3 = R.color.refresh_color3;
	public static final int color4 = R.color.refresh_color4;
	
	//通用
	public static String SDCARD_PATH = Environment.getExternalStorageDirectory()+"/FYJP";
	public static String PORTRAIT_ADDR = SDCARD_PATH + "/portrait.png";//头像保存的路径
	public static String OLD_PORTRAIT_ADDR = SDCARD_PATH + "/oldportrait.png";//头像保存的路径
	public static String VIDEO_ADDR = SDCARD_PATH + "/video";//拍摄视频保存的路径
	public static String TRIMPATH = SDCARD_PATH + "/trim/";//裁剪后视频文件夹路径
	public static String DOWNLOAD_ADDR = SDCARD_PATH + "/download";//下载视频保存的路径
	public static String MERGEPATH = SDCARD_PATH + "/merge/";//合并裁剪后的视频文件夹路径
	public static String THUMBNAIL_ADDR = SDCARD_PATH + "/thumbnail/";//缩略图保存的路径
	public static String PICTURE_ADDR = SDCARD_PATH + "/picture/";//拍照保存的路径
	public static String VIDEOTYPE = ".mp4";//mp4格式播放视频要快，比.3gp速度快很多
	public static String IMGTYPE = ".jpg";
	public static int TIME = 120;//视频录制时间限定为120秒
	public static int standarH = 608;//当视频高度太高时，给一个定值
//	public static String WEB = "http://channellive2.tianqi.cn/showVideo.html?url=";//分享视频时，网页需要加载播放器
	public static String WEB = "http://channellive2.tianqi.cn/weather/work/fxlink/id/";//分享视频时，网页需要加载播放器
	public static String WEB_SUFFIX = ".html";
	
}
