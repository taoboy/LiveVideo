package com.hf.live.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.activity.OtherInfoActivity;
import com.hf.live.dto.PhotoDto;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * 视频审核
 */

public class MyCheckAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList;
	
	private final class ViewHolder{
		ImageView imageView,ivVideo;
		TextView tvTitle,tvUserName,tvStatus;
	}
	
	private ViewHolder mHolder = null;
	
	public MyCheckAdapter(Context context, List<PhotoDto> mArrayList) {
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
	public int getViewTypeCount() {
		// menu type count
		return 3;
	}
	
	@Override
	public int getItemViewType(int position) {
		// current menu type
		PhotoDto data = mArrayList.get(position);
		if (TextUtils.equals(data.status, "1")) {//未审核
			return 0;
		}else if (TextUtils.equals(data.status, "2")) {//审核通过
			return 1;
		}else if (TextUtils.equals(data.status, "3")) {//审核拒绝
			return 2;
		}
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.adapter_my_check, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.ivVideo = (ImageView) convertView.findViewById(R.id.ivVideo);
			mHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.tvStatus = (TextView) convertView.findViewById(R.id.tvStatus);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		final PhotoDto dto = mArrayList.get(position);

		if (!TextUtils.isEmpty(dto.title)) {
			mHolder.tvTitle.setText(dto.title);
		}

		if (!TextUtils.isEmpty(dto.nickName)) {
			mHolder.tvUserName.setText(dto.nickName);
		}else if (!TextUtils.isEmpty(dto.nickName)) {
			mHolder.tvUserName.setText(dto.nickName);
		}else if (!TextUtils.isEmpty(dto.phoneNumber)) {
			mHolder.tvUserName.setText(dto.phoneNumber);
		}
		mHolder.tvUserName.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

		if (TextUtils.equals(dto.status, "1")) {//未审核
			mHolder.tvStatus.setText(mContext.getString(R.string.not_check));
			mHolder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.yellow));
		}else if (TextUtils.equals(dto.status, "2")) {//审核通过
			mHolder.tvStatus.setText(mContext.getString(R.string.pass_check));
			mHolder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.green));
		}else if (TextUtils.equals(dto.status, "3")) {//审核拒绝
			mHolder.tvStatus.setText(mContext.getString(R.string.refuse_check));
			mHolder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.red));
		}

		if (!TextUtils.isEmpty(dto.imgUrl)) {
			Picasso.with(mContext).load(dto.imgUrl).centerCrop().resize(360, 240).error(R.drawable.iv_seat_bitmap).into(mHolder.imageView);
		}else {
			mHolder.imageView.setImageResource(R.drawable.iv_seat_bitmap);
		}

		if (dto.workstype.equals("imgs")) {
			mHolder.ivVideo.setVisibility(View.INVISIBLE);
		}else {
			mHolder.ivVideo.setVisibility(View.VISIBLE);
		}

		mHolder.tvUserName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, OtherInfoActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("data", dto);
				intent.putExtras(bundle);
				mContext.startActivity(intent);
			}
		});
		
		return convertView;
	}

}
