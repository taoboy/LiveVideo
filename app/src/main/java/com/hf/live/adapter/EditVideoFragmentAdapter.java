package com.hf.live.adapter;

/**
 * 视频剪辑
 */

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;

import java.io.File;
import java.util.List;

public class EditVideoFragmentAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList;
	private int imageWidth;
	private RelativeLayout.LayoutParams params;

	private final class ViewHolder{
		ImageView imageView;
		ImageView imageView1;
		TextView tvAlbumName;
	}

	private ViewHolder mHolder = null;

	public EditVideoFragmentAdapter(Context context, List<PhotoDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		imageWidth = wm.getDefaultDisplay().getWidth();
		params = new RelativeLayout.LayoutParams(imageWidth/4, imageWidth/4-10);
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
			convertView = mInflater.inflate(R.layout.adapter_edit_video_fragment, null);
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
			String imgPath = CommonUtil.getVideoThumbnail(dto.videoUrl, MediaStore.Video.Thumbnails.MINI_KIND);
			if (!TextUtils.isEmpty(imgPath) && new File(imgPath).exists()) {
				Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
				if (bitmap != null) {
					mHolder.imageView.setImageBitmap(bitmap);
				}
			}else {
				CommonUtil.videoThumbnail(dto.videoUrl, imageWidth/4, imageWidth/4-10, MediaStore.Video.Thumbnails.MINI_KIND, mHolder.imageView);
			}
		}
		if (params != null) {
			mHolder.imageView.setLayoutParams(params);
		}

		if (dto.isSelected) {
			mHolder.imageView1.setImageResource(R.drawable.iv_grid_select);
		}else {
			mHolder.imageView1.setImageResource(R.drawable.iv_grid_unselect);
		}

		if (!TextUtils.isEmpty(dto.imageName)) {
			mHolder.tvAlbumName.setText(dto.imageName);
		}

		return convertView;
	}



}
