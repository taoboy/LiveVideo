package com.hf.live.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;

import net.tsz.afinal.FinalBitmap;

import java.util.List;

/**
 * 对视频、图片评论
 */

public class CommentAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList;
	
	private final class ViewHolder{
		ImageView ivPortrait;
		TextView tvUserName,tvTime,tvComment;
	}
	
	private ViewHolder mHolder = null;
	
	public CommentAdapter(Context context, List<PhotoDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_comment, null);
			mHolder = new ViewHolder();
			mHolder.ivPortrait = (ImageView) convertView.findViewById(R.id.ivPortrait);
			mHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
			mHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
			mHolder.tvComment = (TextView) convertView.findViewById(R.id.tvComment);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		PhotoDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.userName)) {
			mHolder.tvUserName.setText(dto.userName);
		}
		if (!TextUtils.isEmpty(dto.createTime)) {
			mHolder.tvTime.setText(dto.createTime);
		}
		if (!TextUtils.isEmpty(dto.comment)) {
			mHolder.tvComment.setText(dto.comment);
		}

		if (!TextUtils.isEmpty(dto.portraitUrl)) {
			LayoutParams lp = mHolder.ivPortrait.getLayoutParams();
			int width = lp.width;
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.ivPortrait, dto.portraitUrl, null, width);
		}else {
			mHolder.ivPortrait.setImageResource(R.drawable.iv_portrait);
		}

		return convertView;
	}
	
}
