package com.hf.live.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.MyApplication;
import com.hf.live.fragment.EditVideoFragment;
import com.hf.live.fragment.VideoWallFragment;
import com.hf.live.util.AutoUpdateUtil;
import com.hf.live.util.OkHttpUtil;
import com.hf.live.view.MainViewPager;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 主界面
 * @author shawn_sun
 *
 */

public class MainActivity2 extends BaseActivity implements OnClickListener {

	private Context mContext = null;
	private LinearLayout llWall, llClip, llShot;
	private ImageView ivWall, ivClip, ivShot, ivEvent;
	private TextView tvWall, tvClip, tvShot;
	private MainViewPager viewPager = null;
	private List<Fragment> fragments = new ArrayList<>();
	private long mExitTime;//记录点击完返回按钮后的long型时间
	private int screenWidth, screenHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);
		MyApplication.addDestoryActivity(MainActivity2.this, "MainActivity2");
		mContext = this;
		initWidget();
		initViewPager();
	}

	/**
	 * 初始化控件
	 */
	private void initWidget() {
		AutoUpdateUtil.checkUpdate(MainActivity2.this, mContext, "51", getString(R.string.app_name), true);

		llWall = (LinearLayout) findViewById(R.id.llWall);
		llWall.setOnClickListener(new MyOnClickListener(0));
		llClip = (LinearLayout) findViewById(R.id.llClip);
		llClip.setOnClickListener(new MyOnClickListener(1));
		llShot = (LinearLayout) findViewById(R.id.llShot);
		llShot.setOnClickListener(this);
		ivWall = (ImageView) findViewById(R.id.ivWall);
		ivClip = (ImageView) findViewById(R.id.ivClip);
		ivShot = (ImageView) findViewById(R.id.ivShot);
		tvWall = (TextView) findViewById(R.id.tvWall);
		tvClip = (TextView) findViewById(R.id.tvClip);
		tvShot = (TextView) findViewById(R.id.tvShot);
		ivEvent = (ImageView) findViewById(R.id.ivEvent);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;

		OkHttpEvent("http://channellive2.tianqi.cn/weather/work/fyjp_zhubo_path");
	}

	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		Fragment fragment = new VideoWallFragment();
		fragments.add(fragment);
		fragment = new EditVideoFragment();
		fragments.add(fragment);

		viewPager = (MainViewPager) findViewById(R.id.viewPager);
		viewPager.setSlipping(false);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		viewPager.setAdapter(new MyPagerAdapter());
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			switch (arg0) {
				case 0:
					ivWall.setImageResource(R.drawable.iv_meizi_press);
					ivClip.setImageResource(R.drawable.iv_clip);
					tvWall.setTextColor(getResources().getColor(R.color.red));
					tvClip.setTextColor(getResources().getColor(R.color.text_color4));
					break;
				case 1:
					ivWall.setImageResource(R.drawable.iv_meizi);
					ivClip.setImageResource(R.drawable.iv_clip_press);
					tvWall.setTextColor(getResources().getColor(R.color.text_color4));
					tvClip.setTextColor(getResources().getColor(R.color.red));
					break;
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/**
	 * @ClassName: MyOnClickListener
	 * @Description: TODO头标点击监听
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:46:08
	 *
	 */
	private class MyOnClickListener implements OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			if (viewPager != null) {
				viewPager.setCurrentItem(index);
			}
		}
	};

	/**
	 * @ClassName: MyPagerAdapter
	 * @Description: TODO填充ViewPager的数据适配器
	 * @author Panyy
	 * @date 2013 2013年11月6日 下午2:37:47
	 *
	 */
	private class MyPagerAdapter extends PagerAdapter {
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(fragments.get(position).getView());
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = fragments.get(position);
			if (!fragment.isAdded()) { // 如果fragment还没有added
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.add(fragment, fragment.getClass().getSimpleName());
				ft.commit();
				/**
				 * 在用FragmentTransaction.commit()方法提交FragmentTransaction对象后
				 * 会在进程的主线程中,用异步的方式来执行。
				 * 如果想要立即执行这个等待中的操作,就要调用这个方法(只能在主线程中调用)。
				 * 要注意的是,所有的回调和相关的行为都会在这个调用中被执行完成,因此要仔细确认这个方法的调用位置。
				 */
				getFragmentManager().executePendingTransactions();
			}

			if (fragment.getView().getParent() == null) {
				container.addView(fragment.getView()); // 为viewpager增加布局
			}
			return fragment.getView();
		}
	}

	/**
	 * 拍摄对话框
	 */
	private void shotDialog1() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_shot1, null);

		ImageView ivClose = (ImageView) view.findViewById(R.id.ivClose);
		LinearLayout ll1 = (LinearLayout) view.findViewById(R.id.ll1);
		LinearLayout ll2 = (LinearLayout) view.findViewById(R.id.ll2);
		LinearLayout ll3 = (LinearLayout) view.findViewById(R.id.ll3);
		LinearLayout ll4 = (LinearLayout) view.findViewById(R.id.ll4);
		LinearLayout ll5 = (LinearLayout) view.findViewById(R.id.ll5);
		LinearLayout ll6 = (LinearLayout) view.findViewById(R.id.ll6);

		final Dialog dialog = new Dialog(mContext, R.style.CustomProgressDialog);
		dialog.getWindow().setGravity(Gravity.BOTTOM);
		dialog.setContentView(view);
		dialog.show();

		ivClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		//直播
		ll1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

		//推流
		ll2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, PushRtmpSettingActivity.class));
			}
		});

		//观看流直播
		ll3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, PullRtmpSettingActivity.class));
			}
		});

		//拍摄
		ll4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, VideoRecordActivity.class));
			}
		});

		//视频
		ll5.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, SelectVideoActivity.class));
			}
		});

		//照片
		ll6.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(mContext, SelectPictureActivity.class));
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(mContext, getString(R.string.confirm_exit)+getString(R.string.app_name), Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				finish();
			}
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.llShot:
				shotDialog1();
				break;
		default:
			break;
		}
	}

	/**
	 * 访问活动
	 * @param url
	 */
	@SuppressLint("ClickableViewAccessibility")
	private void OkHttpEvent(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {

					}

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						if (!response.isSuccessful()) {
							return;
						}
						final String result = response.body().string();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								try {
									JSONObject obj = new JSONObject(result);
									if (!obj.isNull("deadLineSatus")) {
										String deadLineSatus = obj.getString("deadLineSatus");
										if (!TextUtils.equals(deadLineSatus, "1")) {//活动截止，不显示入口
											return;
										}

										if (!obj.isNull("home")) {
											Picasso.with(mContext).load(obj.getString("home")).into(ivEvent);
											ivEvent.setVisibility(View.VISIBLE);
											final String showUrl = obj.getString("showUrl");
											final String logoUrl = obj.getString("logo");
//											ivEvent.setOnClickListener(new OnClickListener() {
//												@Override
//												public void onClick(View v) {
//													Intent intent = new Intent(mContext, EventActivity.class);
//													intent.putExtra("showUrl", showUrl);
//													intent.putExtra("logoUrl", logoUrl);
//													startActivity(intent);
//												}
//											});

											ivEvent.setOnTouchListener(new View.OnTouchListener() {
												int lastX, lastY;
                                                int dx, dy;
												@Override
												public boolean onTouch(View v, MotionEvent event) {
													int ea = event.getAction();
													switch (ea) {
														case MotionEvent.ACTION_DOWN:
															lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
															lastY = (int) event.getRawY();
															break;
														case MotionEvent.ACTION_MOVE:
															dx = (int) event.getRawX() - lastX;
															dy = (int) event.getRawY() - lastY;
															int l = v.getLeft() + dx;
															int b = v.getBottom() + dy;
															int r = v.getRight() + dx;
															int t = v.getTop() + dy;
															// 下面判断移动是否超出屏幕
															if (l < 0) {
																l = 0;
																r = l + v.getWidth();
															}
															if (t < 0) {
																t = 0;
																b = t + v.getHeight();
															}
															if (r > screenWidth) {
																r = screenWidth;
																l = r - v.getWidth();
															}
															if (b > screenHeight) {
																b = screenHeight;
																t = b - v.getHeight();
															}
															v.layout(l, t, r, b);
															lastX = (int) event.getRawX();
															lastY = (int) event.getRawY();
															v.postInvalidate();
															break;
														case MotionEvent.ACTION_UP:
															if (Math.abs(dx) == 0 && Math.abs(dy) == 0) {
																Intent intent = new Intent(mContext, EventActivity.class);
																intent.putExtra("showUrl", showUrl);
																intent.putExtra("logoUrl", logoUrl);
																startActivity(intent);
															}

															// 每次移动都要设置其layout，不然由于父布局可能嵌套listview，当父布局发生改变冲毁（如下拉刷新时）则移动的view会回到原来的位置
															RelativeLayout.LayoutParams lpFeedback = new RelativeLayout.LayoutParams(
																	RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
															lpFeedback.leftMargin = v.getLeft();
															lpFeedback.topMargin = v.getTop();
															lpFeedback.setMargins(v.getLeft(), v.getTop(), 0, 0);
															v.setLayoutParams(lpFeedback);
															break;
													}
													return true;
												}
											});

										}
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						});
					}
				});
			}
		}).start();
	}

}
