package com.hf.live.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * 选择图片
 */

public class SelectPictureAdapter extends BaseAdapter {

	private static Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList;
	private int width;
	private RelativeLayout.LayoutParams params;

	private final class ViewHolder{
		ImageView imageView;
		ImageView imageView1;
	}

	private ViewHolder mHolder = null;

	public SelectPictureAdapter(Context context, List<PhotoDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
		params = new RelativeLayout.LayoutParams(width/4, width/4);
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
			convertView = mInflater.inflate(R.layout.adapter_select_picture, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.imageView1 = (ImageView) convertView.findViewById(R.id.imageView1);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		mHolder.imageView1.setTag(position);
		
		PhotoDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.imgUrl)) {
			File file = new File(dto.imgUrl);
			if (file.exists()) {
				Picasso.with(mContext).load(file).centerCrop().resize(200, 200).into(mHolder.imageView);
				mHolder.imageView.setLayoutParams(params);
			}
		}
		
		if (dto.isSelected) {
			mHolder.imageView1.setVisibility(View.VISIBLE);
		}else {
			mHolder.imageView1.setVisibility(View.INVISIBLE);
		}
		
		return convertView;
	}

}
