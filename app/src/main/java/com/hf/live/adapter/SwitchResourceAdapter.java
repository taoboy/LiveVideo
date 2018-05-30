package com.hf.live.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.SwitchDto;

import java.util.List;

/**
 * 切换数据源
 */

public class SwitchResourceAdapter extends BaseAdapter{
	
	private Context mContext;
	private LayoutInflater mInflater;
	private List<SwitchDto> mArrayList;
	
	private final class ViewHolder{
		TextView tvName;
		ImageView imageView;
	}
	
	private ViewHolder mHolder = null;
	
	public SwitchResourceAdapter(Context context, List<SwitchDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_switch_resource, null);
			mHolder = new ViewHolder();
			mHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		
		SwitchDto dto = mArrayList.get(position);
		mHolder.tvName.setText(dto.name);
		if (dto.isSelected) {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.white));
			mHolder.imageView.setVisibility(View.VISIBLE);
		}else {
			mHolder.tvName.setTextColor(mContext.getResources().getColor(R.color.text_color2));
			mHolder.imageView.setVisibility(View.GONE);
		}
		
		return convertView;
	}
	
}
