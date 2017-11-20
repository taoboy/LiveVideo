package com.hf.live.adapter;

import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalBitmap;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;

public class VideoWallAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<PhotoDto>();
	
	private final class ViewHolder{
		ImageView imageView;
		ImageView ivVideo;
		TextView tvAddress;
		TextView tvTime;
		TextView tvUserName;
		TextView tvPraise;
		TextView tvComment;
		TextView tvTitle;
	}
	
	private ViewHolder mHolder = null;
	
	public VideoWallAdapter(Context context, List<PhotoDto> mArrayList) {
		mContext = context;
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
			convertView = mInflater.inflate(R.layout.video_wall_cell, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.ivVideo = (ImageView) convertView.findViewById(R.id.ivVideo);
			mHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			mHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
			mHolder.tvPraise = (TextView) convertView.findViewById(R.id.tvPraise);
			mHolder.tvComment = (TextView) convertView.findViewById(R.id.tvComment);
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		PhotoDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.getLocation())) {
			mHolder.tvAddress.setText(dto.getLocation());
		}else {
			mHolder.tvAddress.setText(mContext.getString(R.string.no_location));
		}
		
		if (!TextUtils.isEmpty(dto.nickName)) {
			mHolder.tvUserName.setText(dto.nickName);
		}else if (!TextUtils.isEmpty(dto.getUserName())) {
			mHolder.tvUserName.setText(dto.getUserName());
		}else if (!TextUtils.isEmpty(dto.phoneNumber)) {
			if (dto.phoneNumber.length() >= 7) {
				mHolder.tvUserName.setText(dto.phoneNumber.replace(dto.phoneNumber.substring(3, 7), "****"));
			}else {
				mHolder.tvUserName.setText(dto.phoneNumber);
			}
		}
		mHolder.tvPraise.setText(dto.getPraiseCount());
		mHolder.tvComment.setText(dto.getCommentCount());
		mHolder.tvTitle.setText(dto.getTitle());
		
		if (!TextUtils.isEmpty(dto.getWorkTime())) {
			mHolder.tvTime.setText(mContext.getResources().getString(R.string.cell_upload)+": "+dto.getWorkTime());
		}else {
			mHolder.tvTime.setText(mContext.getResources().getString(R.string.cell_upload)+": "+"--");
		}
		
		FinalBitmap finalBitmap = FinalBitmap.create(mContext);
		finalBitmap.display(mHolder.imageView, dto.imgUrl, null, 0);
		
		if (dto.getWorkstype().equals("imgs")) {
			mHolder.ivVideo.setVisibility(View.INVISIBLE);
		}else {
			mHolder.ivVideo.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

}
