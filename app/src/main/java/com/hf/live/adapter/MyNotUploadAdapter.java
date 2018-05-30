package com.hf.live.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

	private Context mContext;
	private List<PhotoDto> mArrayList;
	private LayoutInflater mInflater;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
	private int width;
	private RelativeLayout.LayoutParams params;

	public MyNotUploadAdapter(Context context, List<PhotoDto> mArrayList) {
		this.mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		params = new RelativeLayout.LayoutParams(width/4, width/4-10);
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
			convertView = mInflater.inflate(R.layout.adapter_my_upload_header, null);
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
			convertView = mInflater.inflate(R.layout.adapter_my_upload_content, null);
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
				if (params != null) {
					mHolder.imageView.setLayoutParams(params);
				}
			}
		}else {
			mHolder.ivVideo.setVisibility(View.VISIBLE);
			if (!TextUtils.isEmpty(dto.videoUrl)) {
				File file = CommonUtil.getLocalThumbnail(dto.workTime);
				if (file != null && file.exists()) {//本地缩略图存在就使用本地
					Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
					if (bitmap != null) {
						mHolder.imageView.setImageBitmap(bitmap);
						if (params != null) {
							mHolder.imageView.setLayoutParams(params);
						}
					}
				}else {//本地缩略图呗删除就根据视频获取缩略图
					CommonUtil.videoThumbnail(dto.videoUrl, width/4, width/4, MediaStore.Video.Thumbnails.MICRO_KIND, mHolder.imageView);
					if (params != null) {
						mHolder.imageView.setLayoutParams(params);
					}
				}
			}
		}

		return convertView;
	}

}
