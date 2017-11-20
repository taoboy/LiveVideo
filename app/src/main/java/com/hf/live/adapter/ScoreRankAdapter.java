package com.hf.live.adapter;

/**
 * 积分排行
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;

import java.util.ArrayList;
import java.util.List;

public class ScoreRankAdapter extends BaseAdapter{
	
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<>();

	private final class ViewHolder{
		TextView tvNum;
		TextView tvScore;
		TextView tvUserName;
	}
	
	private ViewHolder mHolder = null;
	
	public ScoreRankAdapter(Context context, List<PhotoDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_score_rank, null);
			mHolder = new ViewHolder();
			mHolder.tvNum = (TextView) convertView.findViewById(R.id.tvNum);
			mHolder.tvScore = (TextView) convertView.findViewById(R.id.tvScore);
			mHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		PhotoDto dto = mArrayList.get(position);
		
		mHolder.tvNum.setText(position+1+"");
		
		if (!TextUtils.isEmpty(dto.score)) {
			mHolder.tvScore.setText(dto.score+"分");
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
		
		if (position == 0 || position == 1 || position == 2) {
			mHolder.tvNum.setBackgroundResource(R.drawable.bg_rank_top3);
		}else {
			mHolder.tvNum.setBackgroundResource(R.drawable.bg_rank_bottom3);
		}
		
		return convertView;
	}

}
