package com.hf.live.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.dto.PhotoDto;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


public class CommonUtil {
	
	/** 
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素) 
     */  
    public static float dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return dpValue * scale;
    }  
  
    /** 
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp 
     */  
    public static float px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return pxValue / scale;
    } 
    
	/**
	 * 校验用户名是否正确,只能有字母，数字和下划线
	 */
	public static boolean isUser(String str) {
		String format = "^[\\w+s]{6,30}$";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
	}
	
	/**
	 * 校验密码是否正确,只能有字母，数字和下划线
	 */
	public static boolean isPwd(String str) {
		String format = "^[\\w+s]{6,15}$";
        Pattern pattern = Pattern.compile(format);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
	}
	
	/**
	 * 验证是否是邮箱
	 * 
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}
	
	/**
	 * 验证是否是手机号
	 * @param phoneNumber
	 * @return
	 */
	public static boolean isPhoneNumberValid(String phoneNumber) {
		boolean isValid = false;
		/*
		 * 可接受的电话格式有：
		 */
		String expression = "^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$";
		CharSequence inputStr = phoneNumber;
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(inputStr);

		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}
	
	/**
	 * 设置摄像头预览方向
	 * @param cameraId
	 * @param camera
	 * @return
	 */
	public static int setCameraDisplayOrientation (Activity activity, int cameraId, Camera camera) {
		CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo (cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;   // compensate the mirror
		} else {
			result = ( info.orientation - degrees + 360) % 360;
		}
		return result;
	}
	
	/**
	 * 设置摄像头保存视频方向
	 * @param cameraId
	 * @param camera
	 * @return
	 */
	public static int setCameraVideoOrientation (Activity activity, int cameraId, Camera camera) {
		CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo (cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
//			result = (360 - result) % 360;   // compensate the mirror
		} else {
			result = ( info.orientation - degrees + 360) % 360;
		}
		return result;
	}
	
	/**
	 * 读取图片属性：旋转的角度
	 * @param path 图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {    
       int degree  = 0;    
       try {    
           ExifInterface exifInterface = new ExifInterface(path);    
           int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);    
           switch (orientation) {    
           case ExifInterface.ORIENTATION_ROTATE_90:    
               degree = 90;    
               break;    
           case ExifInterface.ORIENTATION_ROTATE_180:    
               degree = 180;    
               break;    
           case ExifInterface.ORIENTATION_ROTATE_270:    
               degree = 270;    
               break;    
           }    
       } catch (IOException e) {    
           e.printStackTrace();    
       }    
       return degree;    
   }
	
	/**
	 * 隐藏虚拟键盘
	 * @param editText 输入框
	 * @param context 上下文
	 */
	public static void hideInputSoft(EditText editText, Context context) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}
	
	/**
	 * 转换图片成圆形
	 * 
	 * @param bitmap
	 *            传入Bitmap对象
	 * @return
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}
		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}
	
	/**
     * 获取网落图片资源 
     * @param url
     * @return
     */
	public static Bitmap getHttpBitmap(String url) {
		URL myFileURL;
		Bitmap bitmap = null;
		try {
			myFileURL = new URL(url);
			// 获得连接
			HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
			// 设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
			conn.setConnectTimeout(6000);
			// 连接设置获得数据流
			conn.setDoInput(true);
			// 不使用缓存
			conn.setUseCaches(false);
			// 这句可有可无，没有影响
			conn.connect();
			// 得到数据流
			InputStream is = conn.getInputStream();
			// 解析得到图片
			bitmap = BitmapFactory.decodeStream(is);
			// 关闭数据流
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/**
	 * 转换图片成六边形
	 * @return
	 */
	public static Bitmap getHexagonShape(Bitmap bitmap) {
		int targetWidth = bitmap.getWidth();
		int targetHeight = bitmap.getHeight();
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);

		float radius = targetHeight / 2;
		float triangleHeight = (float) (Math.sqrt(3) * radius / 2);
		float centerX = targetWidth / 2;
		float centerY = targetHeight / 2;
		
		Canvas canvas = new Canvas(targetBitmap);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG|Paint.ANTI_ALIAS_FLAG));
		Path path = new Path();
		path.moveTo(centerX, centerY + radius);
		path.lineTo(centerX - triangleHeight, centerY + radius / 2);
		path.lineTo(centerX - triangleHeight, centerY - radius / 2);
		path.lineTo(centerX, centerY - radius);
		path.lineTo(centerX + triangleHeight, centerY - radius / 2);
		path.lineTo(centerX + triangleHeight, centerY + radius / 2);
		path.moveTo(centerX, centerY + radius);
		canvas.clipPath(path);
		canvas.drawBitmap(bitmap, new Rect(0, 0, targetWidth, targetHeight), new Rect(0, 0, targetWidth, targetHeight), null);
		return targetBitmap;
	}

	/**
	 * 把本地的drawable转换成六边形图片
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}
	
	/**
	 * 获取圆角图片
	 * @param bitmap
	 * @param corner
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int corner) {
		try {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()));
			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(Color.BLACK);
			canvas.drawRoundRect(rectF, corner, corner, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

			final Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			canvas.drawBitmap(bitmap, src, rect, paint);
			bitmap.recycle();
			return output;
		} catch (Exception e) {
			return bitmap;
		}
	}
	
	/**
	 * 格式化时间
	 * @param miss
	 * @return
	 */
	public static String formatMiss(int miss) {
		String hh = miss / 3600 > 9 ? miss / 3600 + "" : "0" + miss / 3600;
		String mm = (miss % 3600) / 60 > 9 ? (miss % 3600) / 60 + "" : "0" + (miss % 3600) / 60;
		String ss = (miss % 3600) % 60 > 9 ? (miss % 3600) % 60 + "" : "0" + (miss % 3600) % 60;
		return hh + ":" + mm + ":" + ss;
	}
	
	/**
	 * 格式化时间
	 * @param miss
	 * @return
	 */
	public static String formatMiss2(int miss) {
		if (miss == 0) {
			return "00:00";
		}
		String mm = miss / 60 >= 1 ? "0" + miss / 60 : "0" + miss / 60;
		String ss = miss % 60 > 9 ? miss % 60 + "" : "0" + miss % 60;
		return mm + ":" + ss;
	}
	
	/**
	 * 获取版本号
	 * @return 当前应用的版本号
	 */
	public static String getVersion(Context context) {
	    try {
	        PackageManager manager = context.getPackageManager();
	        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
	        return info.versionName;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "";
	    }
	}

	/**
	 * 分享功能
	 * @param activity
	 */
	public static void share(final Activity activity, final String title, final String content, final String imgUrl, final String url) {
		UMShareConfig config = new UMShareConfig();
		config.setSinaAuthType(UMShareConfig.AUTH_TYPE_WEBVIEW);//只进行网页授权
		UMShareAPI.get(activity).setShareConfig(config);

		ShareAction panelAction = new ShareAction(activity);
		panelAction.setDisplayList(SHARE_MEDIA.WEIXIN,SHARE_MEDIA.WEIXIN_CIRCLE,SHARE_MEDIA.QQ,SHARE_MEDIA.SINA);
		panelAction.setShareboardclickCallback(new ShareBoardlistener() {
			@Override
			public void onclick(SnsPlatform arg0, SHARE_MEDIA arg1) {
				ShareAction sAction = new ShareAction(activity);
				sAction.setPlatform(arg1);
				UMWeb web = new UMWeb(url);
				web.setTitle(title);//标题
				if (!TextUtils.isEmpty(imgUrl)) {
					web.setThumb(new UMImage(activity, imgUrl));  //缩略图
				}else {
					web.setThumb(new UMImage(activity, R.drawable.ic_launcher));
				}
				web.setDescription(content);
				sAction.withMedia(web);
				sAction.share();
			}
		});
		panelAction.open();
	}

	/**
	 * 获取视频的缩略图
	 * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 * @param videoPath 视频的路径
	 * @param kind 参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	public static String getVideoThumbnail(final String videoPath, final int kind) {
		File files = new File(CONST.THUMBNAIL_ADDR);
		if (!files.exists()) {
			files.mkdirs();
		}
		File file = new File(videoPath);
		if (!file.exists()) {
			return null;
		}
		String fileName = file.getName();
		if (fileName.endsWith(CONST.VIDEOTYPE)) {
			fileName = fileName.replace(CONST.VIDEOTYPE, CONST.IMGTYPE);
		}
		final String imgPath = files.getAbsolutePath()+File.separator+fileName;
		if (!new File(imgPath).exists()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						FileOutputStream fos = new FileOutputStream(imgPath);
						Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
						if (bitmap != null && fos != null) {
							bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
							if (bitmap != null && !bitmap.isRecycled()) {
								bitmap.recycle();
							}
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		return imgPath;
	}

	public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		// 获取视频的缩略图
		Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 获取视频缩略图
	 */
	public static void videoThumbnail(String imgUrl, int width, int height, int kind, final ImageView imageView) {
		AsynLoadTask task = new AsynLoadTask(new AsynLoadCompleteListener() {
			@Override
			public void loadComplete(Bitmap bitmap) {
				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}, imgUrl, width, height, kind);
		task.execute();
	}

	private interface AsynLoadCompleteListener {
		void loadComplete(Bitmap bitmap);
	}

	private static class AsynLoadTask extends AsyncTask<Void, Bitmap, Bitmap> {

		private String imgUrl;
		private int width, height;
		private int kind;
		private AsynLoadCompleteListener completeListener;

		private AsynLoadTask(AsynLoadCompleteListener completeListener, String imgUrl, int width, int height, int kind) {
			this.imgUrl = imgUrl;
			this.width = width;
			this.height = height;
			this.kind = kind;
			this.completeListener = completeListener;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Bitmap... values) {
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			return CommonUtil.getVideoThumbnail(imgUrl, width, height, kind);
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (completeListener != null) {
				completeListener.loadComplete(bitmap);
			}
		}
	}

	/**
	 * 根据时间戳获取缩略图文件
	 * @param time
	 * @return
	 */
	public static File getLocalThumbnail(String time) {
		File thumbnailFile = null;
		File picFiles = new File(CONST.THUMBNAIL_ADDR);
		String[] picArray = picFiles.list();
		if (picArray != null && picArray.length > 0) {
			for (int i = 0; i < picArray.length; i++) {
				File localFile = picFiles.listFiles()[i];
				if (localFile.exists()) {
					String fileName = localFile.getName();
					String workTime = fileName.substring(0, fileName.length()-4);
					if (TextUtils.equals(time, workTime)) {
						thumbnailFile = new File(localFile.getPath());
						break;
					}
				}
			}
		}
		return thumbnailFile;
	}

	/**
	 * 保存视频、图片信息
	 * @param context
	 * @param time
	 * @param type
	 * @param pro
	 * @param city
	 * @param dis
	 * @param street
	 */
	public static void saveVideoInfo(Context context, String time, String type, String pro, String city, String dis, String street) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(time, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("time", time);
		editor.putString("type", type);
		editor.putString("pro", pro);
		editor.putString("city", city);
		editor.putString("dis", dis);
		editor.putString("street", street);
		editor.commit();
	}

	/**
	 * 获取保存的视频、图片信息
	 * @param context
	 * @param time
	 * @return
	 */
	public static PhotoDto getVideoInfo(Context context, String time) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(time, Context.MODE_PRIVATE);
		String workTime = sharedPreferences.getString("time", null);
		if (!TextUtils.isEmpty(workTime)) {
			PhotoDto dto = new PhotoDto();
			dto.workTime = sharedPreferences.getString("time", null);
			dto.workstype = sharedPreferences.getString("type", null);
			dto.pro = sharedPreferences.getString("pro", null);
			dto.city = sharedPreferences.getString("city", null);
			dto.dis = sharedPreferences.getString("dis", null);
			dto.street = sharedPreferences.getString("street", null);
			return dto;
		}
		return null;
	}

	/**
	 * 获取天气标签
	 * @param f
	 * @return
	 */
	public static String getWeatherFlag(String f) {
		String flag = "";
		if (TextUtils.isEmpty(f)) {
			return flag;
		}
		if (TextUtils.equals(f, "wt01")) {
			flag = "雪";
		}else if (TextUtils.equals(f, "wt02")) {
			flag = "雨";
		}else if (TextUtils.equals(f, "wt03")) {
			flag = "冰雹";
		}else if (TextUtils.equals(f, "wt04")) {
			flag = "晴";
		}else if (TextUtils.equals(f, "wt05")) {
			flag = "霾";
		}else if (TextUtils.equals(f, "wt06")) {
			flag = "大风";
		}else if (TextUtils.equals(f, "wt07")) {
			flag = "沙尘";
		}
		return flag;
	}

	/**
	 * 获取其它标签
	 * @param f
	 * @return
	 */
	public static String getOtherFlag(String f) {
		String flag = "";
		if (TextUtils.isEmpty(f)) {
			return flag;
		}
		if (TextUtils.equals(f, "et01")) {
			flag = "自然灾害";
		}else if (TextUtils.equals(f, "et02")) {
			flag = "事故灾害";
		}else if (TextUtils.equals(f, "et03")) {
			flag = "公共卫生";
		}else if (TextUtils.equals(f, "et04")) {
			flag = "社会安全";
		}
		return flag;
	}

	/**
	 * 获取所有本地视频文件信息
	 * @return
	 */
	public static List<PhotoDto> getAllLocalVideos(Context context) {
		List<PhotoDto> list = new ArrayList<>();
		if (context != null) {
			Cursor cursor = context.getContentResolver().query(
					MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null,
					null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
					String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
					String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM));
					String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ARTIST));
					String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
					String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
					String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
					long duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
					long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));

					if (mimeType.endsWith("mp4")) {
						PhotoDto dto = new PhotoDto();
						dto.imageName = title;
						dto.videoUrl = path;
						dto.duration = duration;
						list.add(0, dto);
					}
				}
				cursor.close();
			}
		}

		return list;
	}

	/**
	 * 获取所有本地图片文件信息
	 * @return
	 */
	public static List<PhotoDto> getAllLocalImages(Context context) {
		List<PhotoDto> list = new ArrayList<>();
		if (context != null) {
			Cursor cursor = context.getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null,
					null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
					String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE));
					String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
					String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
					String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
					long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));

					PhotoDto dto = new PhotoDto();
					dto.imageName = title;
					dto.imgUrl = path;
					list.add(0, dto);
				}
				cursor.close();
			}
		}

		return list;
	}

	/**
	 * 下载头像
	 * @param imgUrl
	 */
	public static void OkHttpLoadPortrait(final Activity activity, final String imgUrl) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(imgUrl).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final byte[] bytes = response.body().bytes();
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
								try {
									File files = new File(CONST.SDCARD_PATH);
									if (!files.exists()) {
										files.mkdirs();
									}
									FileOutputStream fos = new FileOutputStream(CONST.PORTRAIT_ADDR);
									if (bitmap != null && fos != null) {
										bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
										if (bitmap != null && !bitmap.isRecycled()) {
											bitmap.recycle();
										}
									}
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}
							}
						});
					}
				});
			}
		}).start();
	}

}
