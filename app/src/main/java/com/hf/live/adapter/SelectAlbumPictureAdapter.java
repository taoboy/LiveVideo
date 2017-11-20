package com.hf.live.adapter;

/**
 * 选择相册图片、多选
 */

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.dto.PhotoDto;

import net.tsz.afinal.FinalBitmap;

import java.util.ArrayList;
import java.util.List;

public class SelectAlbumPictureAdapter extends BaseAdapter {

	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private List<PhotoDto> mArrayList = new ArrayList<PhotoDto>();
	private int width;

	private final class ViewHolder{
		ImageView imageView;
		TextView tvAlbumName;
	}

	private ViewHolder mHolder = null;

	@SuppressWarnings("deprecation")
	public SelectAlbumPictureAdapter(Context context, List<PhotoDto> mArrayList) {
		mContext = context;
		this.mArrayList = mArrayList;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		width = wm.getDefaultDisplay().getWidth();
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
			convertView = mInflater.inflate(R.layout.adapter_select_album_picture, null);
			mHolder = new ViewHolder();
			mHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
			mHolder.tvAlbumName = (TextView) convertView.findViewById(R.id.tvAlbumName);
			convertView.setTag(mHolder);
		}else {
			mHolder = (ViewHolder) convertView.getTag();
		}

		PhotoDto dto = mArrayList.get(position);
		if (!TextUtils.isEmpty(dto.albumCover)) {
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(mHolder.imageView, dto.albumCover, null, 0);
			ViewGroup.LayoutParams params = mHolder.imageView.getLayoutParams();
			params.width = width/2;
			params.height = width/2;
			mHolder.imageView.setLayoutParams(params);
		}

		if (!TextUtils.isEmpty(dto.albumName)) {
			mHolder.tvAlbumName.setText(dto.albumName);
		}

		return convertView;
	}

}
