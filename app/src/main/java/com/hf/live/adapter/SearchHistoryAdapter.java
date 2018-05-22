package com.hf.live.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;

import java.util.List;

/**
 * 历史搜索
 */

public class SearchHistoryAdapter extends BaseAdapter{

	private Context mContext;
	private LayoutInflater mInflater;
	private List<PhotoDto> mArrayList;

	private final class ViewHolder{
		TextView tvHistory;
	}

	private ViewHolder mHolder = null;

	public SearchHistoryAdapter(Context context, List<PhotoDto> mArrayList) {
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
			convertView = mInflater.inflate(R.layout.adapter_search_history, null);
			mHolder = new ViewHolder();
			mHolder.tvHistory = (TextView) convertView.findViewById(R.id.tvHistory);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		try {
			PhotoDto dto = mArrayList.get(position);

			if (!TextUtils.isEmpty(dto.history)) {
				mHolder.tvHistory.setText(dto.history);
			}

		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}

		return convertView;
	}

}
