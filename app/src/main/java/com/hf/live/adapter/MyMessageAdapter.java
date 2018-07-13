package com.hf.live.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;
import com.squareup.picasso.Picasso;

import net.tsz.afinal.FinalBitmap;

import java.util.List;

/**
 * 我的消息
 */

public class MyMessageAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList ;
	
	private final class ViewHolder{
		ImageView ivPortrait,imageView,ivVideo;
		TextView tvUserName,tvDate,tvContent,tvScore,tvPosition,tvTitle;
		RelativeLayout rePortrait;
		LinearLayout llVideo;
	}
	
	private ViewHolder mHolder = null;
	
	public MyMessageAdapter(Context context, List<PhotoDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_my_message, null);
			mHolder = new ViewHolder();
			mHolder.ivPortrait = (ImageView) convertView.findViewById(R.id.ivPortrait);
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.ivVideo = (ImageView) convertView.findViewById(R.id.ivVideo);
			mHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
			mHolder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
			mHolder.tvContent = (TextView) convertView.findViewById(R.id.tvContent);
			mHolder.tvScore = (TextView) convertView.findViewById(R.id.tvScore);
			mHolder.tvPosition = (TextView) convertView.findViewById(R.id.tvPosition);
			mHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			mHolder.rePortrait = (RelativeLayout) convertView.findViewById(R.id.rePortrait);
			mHolder.llVideo = (LinearLayout) convertView.findViewById(R.id.llVideo);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		PhotoDto dto = mArrayList.get(position);

		LayoutParams lp = mHolder.ivPortrait.getLayoutParams();
		int width = lp.width;
		if (!TextUtils.isEmpty(dto.portraitUrl)) {
			FinalBitmap portraitBitmap = FinalBitmap.create(mContext);
			portraitBitmap.display(mHolder.ivPortrait, dto.portraitUrl, null, width);
		}

		if (!TextUtils.isEmpty(dto.nickName)) {
			mHolder.tvUserName.setText(dto.nickName);
		}else if (!TextUtils.isEmpty(dto.userName)) {
			mHolder.tvUserName.setText(dto.userName);
		}else if (!TextUtils.isEmpty(dto.phoneNumber)) {
			if (dto.phoneNumber.length() >= 7) {
				mHolder.tvUserName.setText(dto.phoneNumber.replace(dto.phoneNumber.substring(3, 7), "****"));
			}else {
				mHolder.tvUserName.setText(dto.phoneNumber);
			}
		}

		if (!TextUtils.isEmpty(dto.msgContent)) {
			mHolder.tvContent.setText(dto.msgContent);
		}

		if (!TextUtils.isEmpty(dto.workTime)) {
			mHolder.tvDate.setText(dto.workTime);
		}

		if (!TextUtils.isEmpty(dto.getLocation())) {
			mHolder.tvPosition.setText(dto.getLocation());
		}else {
			mHolder.tvPosition.setText(mContext.getString(R.string.no_location));
		}

		if (!TextUtils.isEmpty(dto.title)) {
			mHolder.tvTitle.setText(dto.title);
		}

		if (!TextUtils.isEmpty(dto.score)) {
			mHolder.tvScore.setText("+"+dto.score);
			if (dto.getScore().equals("0")) {
				mHolder.tvScore.setVisibility(View.INVISIBLE);
				mHolder.llVideo.setVisibility(View.VISIBLE);
				mHolder.rePortrait.setVisibility(View.VISIBLE);
			}else {
				mHolder.tvScore.setVisibility(View.VISIBLE);
				mHolder.llVideo.setVisibility(View.GONE);
				mHolder.rePortrait.setVisibility(View.GONE);
			}
		}

		if (dto.workstype.equals("imgs")) {
			mHolder.ivVideo.setVisibility(View.INVISIBLE);
		}else {
			mHolder.ivVideo.setVisibility(View.VISIBLE);
		}

		if (!TextUtils.isEmpty(dto.imgUrl)) {
			Picasso.with(mContext).load(dto.imgUrl).centerCrop().resize(360, 240).into(mHolder.imageView);
		}else {
			mHolder.imageView.setImageResource(R.drawable.iv_seat_bitmap);
		}

		return convertView;
	}

}
