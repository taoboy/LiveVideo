package com.hf.live.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取相册视频
 */

public class SelectAlbumVideoAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList;
	private int width;
	private RelativeLayout.LayoutParams params;

	private final class ViewHolder{
		ImageView imageView;
		TextView tvAlbumName;
	}

	private ViewHolder mHolder = null;

	public SelectAlbumVideoAdapter(Context context, List<PhotoDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		params = new RelativeLayout.LayoutParams(width/2, width/2);
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_select_album_video, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvAlbumName = (TextView) convertView.findViewById(R.id.tvAlbumName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		final PhotoDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.albumCover)) {
			videoThumbnail(dto.albumCover, width/2, width/2, MediaStore.Video.Thumbnails.MINI_KIND, mHolder.imageView);
			if (params != null) {
				mHolder.imageView.setLayoutParams(params);
			}
		}

		if (!TextUtils.isEmpty(dto.albumName)) {
			mHolder.tvAlbumName.setText(dto.albumName);
		}

		return convertView;
	}

	/**
	 * 下载头像保存在本地
	 */
	private void videoThumbnail(String imgUrl, int width, int height, int kind, final ImageView imageView) {
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

	private class AsynLoadTask extends AsyncTask<Void, Bitmap, Bitmap> {

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

}
