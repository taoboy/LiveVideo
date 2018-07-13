package com.hf.live.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.hf.live.dto.PhotoDto;

import net.tsz.afinal.FinalBitmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 视频墙
 */

public class VideoWallAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList;
	private int width;
	private Bitmap seatBitmap;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private RelativeLayout.LayoutParams params;
	
	private final class ViewHolder{
		ImageView imageView,ivVideo,ivPortrait;
		TextView tvAddress,tvTime,tvUserName,tvPraise,tvComment,tvTitle;
	}
	
	private ViewHolder mHolder = null;
	
	public VideoWallAdapter(Context context, List<PhotoDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();

		seatBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.iv_seat_bitmap);
		params = new RelativeLayout.LayoutParams(width, width*9/16);
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
			convertView = mInflater.inflate(R.layout.adapter_video_wall, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.ivVideo = (ImageView) convertView.findViewById(R.id.ivVideo);
			mHolder.ivPortrait = (ImageView) convertView.findViewById(R.id.ivPortrait);
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

		try {
			PhotoDto dto = mArrayList.get(position);

			if (!TextUtils.isEmpty(dto.imgUrl)) {
				FinalBitmap finalBitmap = FinalBitmap.create(mContext);
				finalBitmap.display(mHolder.imageView, dto.imgUrl, seatBitmap, seatBitmap, null, 0);
				mHolder.imageView.setLayoutParams(params);
			}

			if (!TextUtils.isEmpty(dto.location)) {
				mHolder.tvAddress.setText(dto.location);
			}else {
				mHolder.tvAddress.setText(mContext.getString(R.string.no_location));
			}

			if (!TextUtils.isEmpty(dto.portraitUrl)) {
				ViewGroup.LayoutParams lp = mHolder.ivPortrait.getLayoutParams();
				int width = lp.width;
				FinalBitmap finalBitmap = FinalBitmap.create(mContext);
				finalBitmap.display(mHolder.ivPortrait, dto.portraitUrl, null, width);
			}else {
				mHolder.ivPortrait.setImageResource(R.drawable.iv_portrait);
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

			if (!TextUtils.isEmpty(dto.title)) {
				mHolder.tvTitle.setText(dto.title);
			}

			if (!TextUtils.isEmpty(dto.praiseCount)) {
				mHolder.tvPraise.setText(dto.praiseCount);
			}

			if (!TextUtils.isEmpty(dto.commentCount)) {
				mHolder.tvComment.setText(dto.commentCount);
			}

			if (!TextUtils.isEmpty(dto.workTime)) {
				try {
					long second = 1, minute = 60, hour = 3600, day = 86400, week = 604800, month = 2592000, year = 31530000;
					long currentTime = new Date().getTime();
					long workTime = sdf1.parse(dto.workTime).getTime();
					long time = (currentTime-workTime)/1000;//单位秒
					String timeString = "";
					if (time <= minute) {
						timeString = time/second+"秒前";
					}else if (time > minute && time <= hour) {
						timeString = time/minute+"分钟前";
					}else if (time > hour && time <= day) {
						timeString = time/hour+"小时前";
					}else if (time > day && time <= week) {
						timeString = time/day+"天前";
					}else if (time > week && time <= month) {
						timeString = time/week+"星期前";
					}else if (time > month && time <= year) {
						timeString = time/month+"个月前";
					}else if (time > year) {
						timeString = time/year+"年前";
					}
					mHolder.tvTime.setText(timeString);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}else {
				mHolder.tvTime.setText("");
			}

			if (dto.getWorkstype().equals("imgs")) {
				mHolder.ivVideo.setVisibility(View.INVISIBLE);
			}else {
				mHolder.ivVideo.setVisibility(View.VISIBLE);
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}

		return convertView;
	}

}
