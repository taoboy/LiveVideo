package com.hf.live.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.tsz.afinal.FinalBitmap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.adapter.MyViewPagerAdapter;
import com.hf.live.adapter.OnlinePictureAdapter;
import com.hf.live.adapter.VideoAdapter;
import com.hf.live.common.CONST;
import com.hf.live.dto.PhotoDto;
import com.hf.live.R;
import com.hf.live.util.CommonUtil;
import com.hf.live.util.CustomHttpClient;
import com.hf.live.util.EmojiMapUtil;
import com.hf.live.util.OkHttpUtil;
import com.hf.live.view.PhotoView;

/**
 * 在线预览图片
 */

public class OnlinePictureActivity extends BaseActivity implements OnClickListener{
	
	private Context mContext = null;
	private GridView mGridView = null;
	private OnlinePictureAdapter gridAdapter = null;
	private ViewPager mViewPager = null;
	private MyViewPagerAdapter pagerAdapter = null;
	private ImageView[] imageArray = null;//装载图片的数组
	private ImageView[] ivTips = null;//装载点的数组
	private ViewGroup viewGroup = null;
	private RelativeLayout rePager = null;
	private LinearLayout llBack = null;//返回按钮
	private PhotoDto data = null;
	private List<String> urlList = new ArrayList<>();//存放图片的list
	
	private ListView mListView = null;
	private VideoAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int page = 1;
	private int pageSize = 1000;
	private boolean praiseState = false;//点赞状态
	private LinearLayout llListView = null;
	private TextView tvPosition = null;//地址信息
	private TextView tvTitle = null;//标题
	private TextView tvTime = null;//时间
	private TextView tvCommentCount = null;//评论次数
	private LinearLayout llSubmit = null;
	private EditText etComment = null;
	private TextView tvSubmit = null;
	private ImageView ivComment = null;//评论
	private ImageView ivPraise = null;//点赞
	private ImageView ivShare = null;//分享
	private RelativeLayout reOperate = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_online_picture);
		mContext = this;
		initWidget();
		initGridView();
		initViewPager();
		initListView();
	}
	
	/**
	 * 初始化控件
	 */
	private void initWidget() {
		rePager = (RelativeLayout) findViewById(R.id.rePager);
		viewGroup = (ViewGroup) findViewById(R.id.viewGroup);
		llBack = (LinearLayout) findViewById(R.id.llBack);
		llBack.setOnClickListener(this);
		llListView = (LinearLayout) findViewById(R.id.llListView);
		tvPosition = (TextView) findViewById(R.id.tvPosition);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvCommentCount = (TextView) findViewById(R.id.tvCommentCount);
		ivComment = (ImageView) findViewById(R.id.ivComment);
		ivComment.setOnClickListener(this);
		ivPraise = (ImageView) findViewById(R.id.ivPraise);
		ivPraise.setOnClickListener(this);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		llSubmit = (LinearLayout) findViewById(R.id.llSubmit);
		etComment = (EditText) findViewById(R.id.etComment);
		tvSubmit = (TextView) findViewById(R.id.tvSubmit);
		tvSubmit.setOnClickListener(this);
		reOperate = (RelativeLayout) findViewById(R.id.reOperate);
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				urlList.clear();
				urlList = data.getUrlList();
				
				tvPosition.setText(data.getLocation());
				tvTitle.setText(data.getTitle());
				tvTime.setText(data.getWorkTime());
				tvCommentCount.setText(getString(R.string.comment) + "（"+data.getCommentCount()+"）");
				
				//获取点赞状态
				SharedPreferences sharedPreferences = getSharedPreferences(data.getVideoId(), Context.MODE_PRIVATE);
				if (sharedPreferences.getBoolean("praiseState", false)) {
					praiseState = true;
					ivPraise.setImageResource(R.drawable.iv_like);
				}else {
					praiseState = false;
					ivPraise.setImageResource(R.drawable.iv_unlike);
				}

				//获取评论列表
				OkHttpCommentList(CONST.GET_WORK_COMMENT_URL);
			}
		}
	}
	
	/**
	 * 初始化gridview
	 */
	private void initGridView() {
		mGridView = (GridView) findViewById(R.id.gridView);
		gridAdapter = new OnlinePictureAdapter(mContext, urlList);
		mGridView.setAdapter(gridAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				mGridView.setVisibility(View.GONE);
				llListView.setVisibility(View.GONE);
				reOperate.setVisibility(View.GONE);
				llSubmit.setVisibility(View.GONE);
				rePager.setVisibility(View.VISIBLE);
				if (mViewPager != null) {
					mViewPager.setCurrentItem(arg2);
				}
			}
		});
	}
	
	/**
	 * 初始化viewPager
	 */
	private void initViewPager() {
		imageArray = new ImageView[urlList.size()];
		for (int i = 0; i < urlList.size(); i++) {
			ImageView image = new ImageView(mContext);
			FinalBitmap finalBitmap = FinalBitmap.create(mContext);
			finalBitmap.display(image, urlList.get(i), null, 0);
			imageArray[i] = image;
		}
		
		ivTips = new ImageView[urlList.size()];
		viewGroup.removeAllViews();
		for (int i = 0; i < urlList.size(); i++) {
			ImageView imageView = new ImageView(mContext);
			imageView.setLayoutParams(new LayoutParams(5, 5));  
			ivTips[i] = imageView;  
			if(i == 0){  
				ivTips[i].setBackgroundResource(R.drawable.point_white);  
			}else{  
				ivTips[i].setBackgroundResource(R.drawable.point_gray);  
			}  
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));  
			layoutParams.leftMargin = 10;  
			layoutParams.rightMargin = 10;  
			viewGroup.addView(imageView, layoutParams);  
		}
		
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		pagerAdapter = new MyViewPagerAdapter(imageArray);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				for (int i = 0; i < urlList.size(); i++) {
					if(i == arg0){  
						ivTips[i].setBackgroundResource(R.drawable.point_white);  
					}else{  
						ivTips[i].setBackgroundResource(R.drawable.point_gray);  
					} 
					
					View childAt = mViewPager.getChildAt(i);
                    try {
                        if (childAt != null && childAt instanceof PhotoView) {
                        	PhotoView  photoView = (PhotoView) childAt;//得到viewPager里面的页面
                        	PhotoViewAttacher mAttacher = new PhotoViewAttacher(photoView);//把得到的photoView放到这个负责变形的类当中
                            mAttacher.getDisplayMatrix().reset();//得到这个页面的显示状态，然后重置为默认状态
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
				}
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new VideoAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
	}

	/**
	 * 获取评论列表
	 */
	private void OkHttpCommentList(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("wid", data.getVideoId());
		builder.add("page", page+"");
		builder.add("pagesize", pageSize+"");
		builder.add("appid", CONST.APPID);
		RequestBody body = builder.build();
		OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject object = new JSONObject(result);
						if (object != null) {
							if (!object.isNull("status")) {
								int status  = object.getInt("status");
								if (status == 1) {//成功
									if (!object.isNull("info")) {
										JSONArray array = object.getJSONArray("info");
										int length = array.length();
										if (length <= 0) {
											return;
										}
										mList.clear();
										for (int i = 0; i < array.length(); i++) {
											JSONObject obj = array.getJSONObject(i);
											PhotoDto dto = new PhotoDto();
											if (!obj.isNull("create_time")) {
												dto.createTime = obj.getString("create_time");
											}
											if (!obj.isNull("username")) {
												dto.userName = obj.getString("username");
											}
											if (!obj.isNull("comment")) {
												dto.comment = EmojiMapUtil.replaceCheatSheetEmojis(obj.getString("comment"));
											}
											if (!obj.isNull("photo")) {
												dto.portraitUrl = obj.getString("photo");
											}
											mList.add(dto);
										}
									}

									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											tvCommentCount.setText(getString(R.string.comment) + "（"+mList.size()+"）");
											if (mList.size() > 0 && mAdapter != null) {
												mAdapter.notifyDataSetChanged();
											}
										}
									});

								}else {
									//失败
									if (!object.isNull("msg")) {
										final String msg = object.getString("msg");
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												if (msg != null) {
													Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
												}
											}
										});

									}
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * 提交评论
	 */
	private void OkHttpSubmitComment(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", TOKEN);
		builder.add("wid", data.videoId);
		builder.add("comment", EmojiMapUtil.replaceUnicodeEmojis(etComment.getText().toString()));
		RequestBody body = builder.build();
		OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
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
						if (!TextUtils.isEmpty(result)) {
							try {
								JSONObject object = new JSONObject(result);
								if (object != null) {
									if (!object.isNull("status")) {
										int status  = object.getInt("status");
										if (status == 1) {//成功
											etComment.setText("");
											OkHttpCommentList(CONST.GET_WORK_COMMENT_URL);
										}else {
											//失败
											if (!object.isNull("msg")) {
												String msg = object.getString("msg");
												if (msg != null) {
													Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
												}
											}
										}
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				});
			}
		});
	}
	
	/**
	 * 隐藏虚拟键盘
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (etComment != null) {
			CommonUtil.hideInputSoft(etComment, mContext);
		}
		if (llSubmit != null) {
			llSubmit.setVisibility(View.GONE);
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 点赞
	 */
	private void OkHttpPraise(String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", TOKEN);
		builder.add("id", data.videoId);
		RequestBody body = builder.build();
		OkHttpUtil.enqueue(new Request.Builder().post(body).url(url).build(), new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {

			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					return;
				}
				String result = response.body().string();
				if (result != null) {
					try {
						JSONObject object = new JSONObject(result);
						if (object != null) {
							if (!object.isNull("status")) {
								int status  = object.getInt("status");
								if (status == 1) {//成功
									//保存点赞状态
									SharedPreferences sharedPreferences = getSharedPreferences(data.videoId, Context.MODE_PRIVATE);
									Editor editor = sharedPreferences.edit();
									editor.putBoolean("praiseState", true);
									editor.commit();
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											ivPraise.setImageResource(R.drawable.iv_like);
										}
									});
								}else {
									//失败
									if (!object.isNull("msg")) {
										final String msg = object.getString("msg");
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												if (msg != null) {
													Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
												}
											}
										});
									}
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	private void commentAnimation(boolean flag, final LinearLayout llLayout) {
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation = null;
		if (flag == false) {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 0, 
					Animation.RELATIVE_TO_SELF, 1f, 
					Animation.RELATIVE_TO_SELF, 0);
		}else {
			animation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,0f,
					Animation.RELATIVE_TO_SELF,1.0f);
		}
		animation.setDuration(200);
		animationSet.addAnimation(animation);
		animationSet.setFillAfter(true);
		llLayout.startAnimation(animationSet);
		animationSet.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			@Override
			public void onAnimationEnd(Animation arg0) {
				llLayout.clearAnimation();
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mGridView.getVisibility() == View.GONE || llSubmit.getVisibility() == View.VISIBLE) {
			if (mGridView.getVisibility() == View.GONE) {
				mGridView.setVisibility(View.VISIBLE);
				llListView.setVisibility(View.VISIBLE);
				reOperate.setVisibility(View.VISIBLE);
				rePager.setVisibility(View.GONE);
			}
			if (llSubmit.getVisibility() == View.VISIBLE) {
				commentAnimation(true, llSubmit);
				llSubmit.setVisibility(View.GONE);
			}
			return false;
		}else {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llBack:
			if (mGridView.getVisibility() == View.GONE || llSubmit.getVisibility() == View.VISIBLE) {
				if (mGridView.getVisibility() == View.GONE) {
					mGridView.setVisibility(View.VISIBLE);
					llListView.setVisibility(View.VISIBLE);
					reOperate.setVisibility(View.VISIBLE);
					rePager.setVisibility(View.GONE);
				}
				if (llSubmit.getVisibility() == View.VISIBLE) {
					commentAnimation(true, llSubmit);
					llSubmit.setVisibility(View.GONE);
				}
			}else {
				finish();
			}
			break;
		case R.id.tvSubmit:
			if (!TextUtils.isEmpty(etComment.getText().toString())) {
				CommonUtil.hideInputSoft(etComment, mContext);
				OkHttpSubmitComment(CONST.COMMENT_WORD_URL);
			}
			break;
		case R.id.ivComment:
			if (TOKEN != null) {
				if (llSubmit.getVisibility() == View.GONE) {
					commentAnimation(false, llSubmit);
					llSubmit.setVisibility(View.VISIBLE);
				}else {
					commentAnimation(true, llSubmit);
					llSubmit.setVisibility(View.GONE);
				}
			}else {
				Intent intent = new Intent(mContext, LoginActivity.class);
				startActivityForResult(intent, 0);
			}
			break;
		case R.id.ivPraise:
			if (praiseState) {
				return;
			}else {
				OkHttpPraise(CONST.PRAISE_WORK_URL);
			}
			break;
		case R.id.ivShare:
			CommonUtil.share(OnlinePictureActivity.this, data.title, data.title, data.imgUrl, CONST.WEB+data.getVideoId()+CONST.WEB_SUFFIX);
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 0:
				llSubmit.setVisibility(View.VISIBLE);
				break;

			default:
				break;
			}
		}
	}
	
}
