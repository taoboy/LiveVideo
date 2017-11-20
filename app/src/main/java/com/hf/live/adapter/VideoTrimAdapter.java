package com.hf.live.adapter;

/**
 * 视频剪辑
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class VideoTrimAdapter extends BaseAdapter {

	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<>();
	private int imageWidth = 0;

	private final class ViewHolder{
		ImageView imageView;
		ImageView imageView1;
		TextView tvAlbumName;
	}

	private ViewHolder mHolder = null;

	@SuppressWarnings("deprecation")
	public VideoTrimAdapter(Context context, List<PhotoDto> mArrayList, int imageWidth) {
		mContext = context;
		this.imageWidth = imageWidth;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			convertView = mInflater.inflate(R.layout.adapter_video_trim, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.imageView1 = (ImageView) convertView.findViewById(R.id.imageView1);
			mHolder.tvAlbumName = (TextView) convertView.findViewById(R.id.tvAlbumName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		PhotoDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.videoUrl)) {
			downloadPortrait(dto.videoUrl, imageWidth, imageWidth, MediaStore.Video.Thumbnails.MINI_KIND, mHolder.imageView);
			LayoutParams params = mHolder.imageView.getLayoutParams();
			params.width = imageWidth;
			params.height = imageWidth;
			mHolder.imageView.setLayoutParams(params);
		}
		
		if (dto.isSelected) {
			mHolder.imageView1.setVisibility(View.VISIBLE);
		}else {
			mHolder.imageView1.setVisibility(View.INVISIBLE);
		}

//		if (!TextUtils.isEmpty(dto.imageName)) {
//			mHolder.tvAlbumName.setText(dto.imageName);
//		}
		
		return convertView;
	}

	/**
	 * 下载头像保存在本地
	 */
	private void downloadPortrait(String imgUrl, int width, int height, int kind, final ImageView imageView) {
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
