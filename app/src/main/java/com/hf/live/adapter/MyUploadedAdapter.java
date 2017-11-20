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
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;
import com.hf.live.stickygridheaders.StickyGridHeadersSimpleAdapter;

import net.tsz.afinal.FinalBitmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 已上传
 */

public class MyUploadedAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {

	private Context mContext = null;
	private List<PhotoDto> mArrayList = null;
	private LayoutInflater mInflater = null;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
	private int width = 0;

	public MyUploadedAdapter(Context context, List<PhotoDto> mArrayList) {
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
			mHeaderHolder.tvDate.setText(sdf2.format(sdf1.parse(dto.getWorkTime())));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		mHeaderHolder.tvPosition.setText(dto.location);

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
		if (!TextUtils.isEmpty(dto.imgUrl)) {
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.imgUrl, null, 0);
			LayoutParams params = mHolder.imageView.getLayoutParams();
			params.width = width/4;
			params.height = width/4;
			mHolder.imageView.setLayoutParams(params);
		}
		if (dto.getWorkstype().equals("imgs")) {
			mHolder.ivVideo.setVisibility(View.INVISIBLE);
		}else {
			mHolder.ivVideo.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

}
