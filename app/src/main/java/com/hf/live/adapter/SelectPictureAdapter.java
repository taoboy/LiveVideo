package com.hf.live.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;

import net.tsz.afinal.FinalBitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择图片
 */

public class SelectPictureAdapter extends BaseAdapter {

	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<>();
	private int width = 0;

	private final class ViewHolder{
		ImageView imageView;
		ImageView imageView1;
	}

	private ViewHolder mHolder = null;

	@SuppressWarnings("deprecation")
	public SelectPictureAdapter(Context context, List<PhotoDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
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
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.imgUrl, null, 0);
			LayoutParams params = mHolder.imageView.getLayoutParams();
			params.width = width/4;
			params.height = width/4;
			mHolder.imageView.setLayoutParams(params);
		}
		
		if (dto.isSelected) {
			mHolder.imageView1.setVisibility(View.VISIBLE);
		}else {
			mHolder.imageView1.setVisibility(View.INVISIBLE);
		}
		
		return convertView;
	}

}
