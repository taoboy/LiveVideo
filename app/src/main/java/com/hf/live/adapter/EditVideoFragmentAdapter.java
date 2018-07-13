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
import com.hf.live.stickygridheaders.StickyGridHeadersSimpleAdapter;
import com.hf.live.util.CommonUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class EditVideoFragmentAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList;
	private int imageWidth;
	private RelativeLayout.LayoutParams params;

	private HeaderViewHolder mHeaderHolder = null;

	private class HeaderViewHolder {
		TextView tvSection;
	}

	@Override
	public long getHeaderId(int position) {
		return mArrayList.get(position).getSection();
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			mHeaderHolder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.adapter_select_video_title, null);
			mHeaderHolder.tvSection = (TextView) convertView.findViewById(R.id.tvSection);
			convertView.setTag(mHeaderHolder);
		} else {
			mHeaderHolder = (HeaderViewHolder) convertView.getTag();
		}

		PhotoDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.sectionName)) {
			mHeaderHolder.tvSection.setText(dto.sectionName);
		}

		return convertView;
	}

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
		params = new RelativeLayout.LayoutParams(imageWidth/4, imageWidth/4);
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
			convertView = mInflater.inflate(R.layout.adapter_select_video_content, null);
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
			if (!TextUtils.isEmpty(imgPath)) {
				File file = new File(imgPath);
				if (file.exists()) {
					Picasso.with(mContext).load(file).centerCrop().resize(200, 200).into(mHolder.imageView);
				}
			}else {
				CommonUtil.videoThumbnail(dto.videoUrl, imageWidth/4, imageWidth/4, MediaStore.Video.Thumbnails.MINI_KIND, mHolder.imageView);
			}
		}
		mHolder.imageView.setLayoutParams(params);

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
