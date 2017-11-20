package com.hf.live.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hf.live.R;
import com.hf.live.fragment.NotUploadFragment;
import com.hf.live.fragment.UploadedFragment;
import com.hf.live.view.MainViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的上传
 */

public class MyUploadActivity extends BaseActivity implements OnClickListener{
	
	private LinearLayout llBack = null;//返回按钮
	private TextView tvTitle = null;
	private LinearLayout llUploadYes = null;//已上传
	private TextView tvUploadYes = null;
	private LinearLayout llUploadNo = null;//未上传
	private TextView tvUploadNo = null;
	private MainViewPager viewPager;
	private List<Fragment> fragments = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_upload);
		initWidget();
		initViewPager();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(getString(R.string.my_upload));
		llUploadYes = (LinearLayout) findViewById(R.id.llUploadYes);
		llUploadYes.setOnClickListener(new MyOnClickListener(0));
		llUploadNo = (LinearLayout) findViewById(R.id.llUploadNo);
		llUploadNo.setOnClickListener(new MyOnClickListener(1));
		tvUploadYes = (TextView) findViewById(R.id.tvUploadYes);
		tvUploadNo = (TextView) findViewById(R.id.tvUploadNo);
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		Fragment fragment1 = new UploadedFragment();
		fragments.add(fragment1);
		Fragment fragment2 = new NotUploadFragment();
		fragments.add(fragment2);
			
		viewPager = (MainViewPager) findViewById(R.id.viewPager);
		viewPager.setSlipping(true);//设置ViewPager是否可以滑动
		viewPager.setOffscreenPageLimit(fragments.size());
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		viewPager.setAdapter(new MyPagerAdapter());
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			if (arg0 == 0) {
				llUploadYes.setBackgroundResource(R.drawable.red_bg);
				llUploadNo.setBackgroundResource(R.drawable.white_bg);
				tvUploadYes.setTextColor(getResources().getColor(R.color.text_color1));
				tvUploadNo.setTextColor(getResources().getColor(R.color.black));
			}else if (arg0 == 1) {
				llUploadYes.setBackgroundResource(R.drawable.white_bg);
				llUploadNo.setBackgroundResource(R.drawable.red_bg);
				tvUploadYes.setTextColor(getResources().getColor(R.color.black));
				tvUploadNo.setTextColor(getResources().getColor(R.color.text_color1));
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
	private class MyOnClickListener implements View.OnClickListener {
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
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			finish();
			break;

		default:
			break;
		}
	}

}
