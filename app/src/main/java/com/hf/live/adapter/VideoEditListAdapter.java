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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;

import java.io.File;
import java.util.List;

/**
 * 视频编辑
 */

public class VideoEditListAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList;
	private int width;
	private LinearLayout.LayoutParams params;

	private final class ViewHolder{
		ImageView imageView;
		TextView tvDuration;
	}

	private ViewHolder mHolder = null;

	public VideoEditListAdapter(Context context, List<PhotoDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();

		params = new LinearLayout.LayoutParams(width*2/3, width*2/3*9/16);
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
			convertView = mInflater.inflate(R.layout.adapter_edit_list, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvDuration = (TextView) convertView.findViewById(R.id.tvDuration);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		try {
			PhotoDto dto = mArrayList.get(position);

			if (!TextUtils.isEmpty(dto.videoUrl)) {
				String imgPath = CommonUtil.getVideoThumbnail(dto.videoUrl, MediaStore.Video.Thumbnails.MINI_KIND);
				if (!TextUtils.isEmpty(imgPath) && new File(imgPath).exists()) {
					Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
					if (bitmap != null) {
						mHolder.imageView.setImageBitmap(bitmap);
					}
				}else {
					CommonUtil.videoThumbnail(dto.videoUrl, width*2/3, width*2/3*9/16, MediaStore.Video.Thumbnails.MINI_KIND, mHolder.imageView);
				}
			}
			if (params != null) {
				mHolder.imageView.setLayoutParams(params);
			}

			mHolder.tvDuration.setText(String.format("%02d:%02d", dto.duration/1000/60, dto.duration/1000%60));

		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}

		return convertView;
	}

}
