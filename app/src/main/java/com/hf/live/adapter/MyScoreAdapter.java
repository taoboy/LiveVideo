package com.hf.live.adapter;

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

import net.tsz.afinal.FinalBitmap;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的积分
 */

public class MyScoreAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<>();
	
	private final class ViewHolder{
		ImageView imageView;
		ImageView ivVideo;
		TextView tvDate;
		TextView tvContent;
		TextView tvScore;
		TextView tvPosition;
		TextView tvTime;
		TextView tvTitle;
	}
	
	private ViewHolder mHolder = null;
	
	public MyScoreAdapter(Context context, List<PhotoDto> mArrayList) {
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_my_score, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.ivVideo = (ImageView) convertView.findViewById(R.id.ivVideo);
			mHolder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
			mHolder.tvContent = (TextView) convertView.findViewById(R.id.tvContent);
			mHolder.tvScore = (TextView) convertView.findViewById(R.id.tvScore);
			mHolder.tvPosition = (TextView) convertView.findViewById(R.id.tvPosition);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		PhotoDto dto = mArrayList.get(position);
		mHolder.tvDate.setText(dto.getCreateTime());
		mHolder.tvContent.setText(dto.getScoreWhy());
		mHolder.tvScore.setText("+"+dto.getScore());
		if (!TextUtils.isEmpty(dto.getLocation())) {
			mHolder.tvPosition.setText(dto.getLocation());
		}else {
			mHolder.tvPosition.setText(mContext.getString(R.string.no_location));
		}
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
