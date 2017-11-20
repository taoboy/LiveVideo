package com.hf.live.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;
import com.hf.live.stickygridheaders.StickyGridHeadersSimpleAdapter;
import com.hf.live.util.CommonUtil;

import net.tsz.afinal.FinalBitmap;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 本地未上传视频、图片
 */

public class MyNotUploadAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter{

	private Context mContext = null;
	private List<PhotoDto> mArrayList = null;
	private LayoutInflater mInflater = null;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
	private int width = 0;

	public MyNotUploadAdapter(Context context, List<PhotoDto> mArrayList) {
		this.mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
	}

	private HeaderViewHolder mHeaderHolder = null;

	private class HeaderViewHolder {
		TextView tvDate;
		TextView tvPosition;
	}

	@Override
	public long getHeaderId(int position) {
		return mArrayList.get(position).getSection();
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			mHeaderHolder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.sticky_grid_header, null);
			mHeaderHolder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
			mHeaderHolder.tvPosition = (TextView) convertView.findViewById(R.id.tvPosition);
			convertView.setTag(mHeaderHolder);
		} else {
			mHeaderHolder = (HeaderViewHolder) convertView.getTag();
		}

		PhotoDto dto = mArrayList.get(position);
		try {
			mHeaderHolder.tvDate.setText(sdf2.format(sdf1.parse(dto.workTime)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (!TextUtils.isEmpty(dto.city) && !TextUtils.isEmpty(dto.dis)) {
			mHeaderHolder.tvPosition.setText(dto.city+" "+dto.dis);
		}

		return convertView;
	}
	
	private ViewHolder mHolder = null;
	
	private class ViewHolder {
		ImageView imageView;
		ImageView ivVideo;
	}

	@Override
	public int getCount() {
		return mArrayList.size();
	}

	@Override
	public Object getItem(int position) {
		return mArrayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			mHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.sticky_grid_item, null);
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.ivVideo = (ImageView) convertView.findViewById(R.id.ivVideo);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		PhotoDto dto = mArrayList.get(position);
		if (TextUtils.equals(dto.workstype, "imgs")) {
			mHolder.ivVideo.setVisibility(View.INVISIBLE);
			if (!TextUtils.isEmpty(dto.imgUrl)) {
				FinalBitmap finalBitmap = FinalBitmap.create(mContext);
				finalBitmap.display(mHolder.imageView, dto.imgUrl, null, 0);
				ViewGroup.LayoutParams params = mHolder.imageView.getLayoutParams();
				params.width = width/4;
				params.height = width/4;
				mHolder.imageView.setLayoutParams(params);
			}
		}else {
			mHolder.ivVideo.setVisibility(View.VISIBLE);
			if (!TextUtils.isEmpty(dto.videoUrl)) {
				File file = CommonUtil.getLocalThumbnail(dto.workTime);
				if (file != null && file.exists()) {//本地缩略图存在就使用本地
					Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
					if (bitmap != null) {
						mHolder.imageView.setImageBitmap(bitmap);
						ViewGroup.LayoutParams params = mHolder.imageView.getLayoutParams();
						params.width = width/4;
						params.height = width/4;
						mHolder.imageView.setLayoutParams(params);
					}
				}else {//本地缩略图呗删除就根据视频获取缩略图
					downloadPortrait(dto.videoUrl, width/4, width/4, MediaStore.Video.Thumbnails.MINI_KIND, mHolder.imageView);
					ViewGroup.LayoutParams params = mHolder.imageView.getLayoutParams();
					params.width = width/4;
					params.height = width/4;
					mHolder.imageView.setLayoutParams(params);
				}
			}
		}

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
