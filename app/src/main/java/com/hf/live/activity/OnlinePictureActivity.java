package com.hf.live.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import com.hf.live.R;
import com.hf.live.adapter.CommentAdapter;
import com.hf.live.adapter.MyViewPagerAdapter;
import com.hf.live.adapter.OnlinePictureAdapter;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.dto.PhotoDto;
import com.hf.live.util.CommonUtil;
import com.hf.live.util.EmojiMapUtil;
import com.hf.live.util.OkHttpUtil;
import com.hf.live.view.PhotoView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.senab.photoview.PhotoViewAttacher;

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
	private PhotoDto data = null;
	private List<String> urlList = new ArrayList<>();//存放图片的list

	private TextView tvTitle, tvContent, tvPositon, tvDate, tvCommentCount, tvSubmit, tvWeatherFlag, tvOtherFlag, tvUserName, tvPlayCount, tvPraiseCount;
	private ImageView ivPortrait, ivPraise, ivShare, ivClear;
	private EditText etComment = null;
	private boolean praiseState = false;//点赞状态
	private LinearLayout llListView = null;
	private LinearLayout llSubmit = null;

	//评论
	private ListView mListView = null;
	private CommentAdapter mAdapter = null;
	private List<PhotoDto> mList = new ArrayList<>();
	private int page = 1;
	private int pageSize = 1000;

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
		llListView = (LinearLayout) findViewById(R.id.llListView);
		ivPortrait = (ImageView) findViewById(R.id.ivPortrait);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvPlayCount = (TextView) findViewById(R.id.tvPlayCount);
		tvPraiseCount = (TextView) findViewById(R.id.tvPraiseCount);
		tvPositon = (TextView) findViewById(R.id.tvPosition);
		tvDate = (TextView) findViewById(R.id.tvDate);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvContent = (TextView) findViewById(R.id.tvContent);
		tvCommentCount = (TextView) findViewById(R.id.tvCommentCount);
		ivPraise = (ImageView) findViewById(R.id.ivPraise);
		ivPraise.setOnClickListener(this);
		ivShare = (ImageView) findViewById(R.id.ivShare);
		ivShare.setOnClickListener(this);
		llSubmit = (LinearLayout) findViewById(R.id.llSubmit);
		etComment = (EditText) findViewById(R.id.etComment);
		etComment.addTextChangedListener(watcher);
		tvSubmit = (TextView) findViewById(R.id.tvSubmit);
		tvSubmit.setOnClickListener(this);
		tvWeatherFlag = (TextView) findViewById(R.id.tvWeatherFlag);
		tvOtherFlag = (TextView) findViewById(R.id.tvOtherFlag);
		ivClear = (ImageView) findViewById(R.id.ivClear);
		ivClear.setOnClickListener(this);
		
		if (getIntent().hasExtra("data")) {
			data = getIntent().getExtras().getParcelable("data");
			if (data != null) {
				urlList.clear();
				urlList.addAll(data.urlList);

				if (!TextUtils.isEmpty(data.nickName)) {
					tvUserName.setText(data.nickName);
				} else if (!TextUtils.isEmpty(data.userName)) {
					tvUserName.setText(data.userName);
				} else if (!TextUtils.isEmpty(data.phoneNumber)) {
					tvUserName.setText(data.phoneNumber);
				}

				if (!TextUtils.isEmpty(data.playCount)) {
					tvPlayCount.setText(data.playCount + "次浏览");
				}

				if (!TextUtils.isEmpty(data.praiseCount)) {
					tvPraiseCount.setText(data.praiseCount);
				}

				//获取点赞状态
				SharedPreferences sharedPreferences = getSharedPreferences(data.getVideoId(), Context.MODE_PRIVATE);
				if (sharedPreferences.getBoolean("praiseState", false)) {
					praiseState = true;
					ivPraise.setImageResource(R.drawable.iv_like);
				} else {
					praiseState = false;
					ivPraise.setImageResource(R.drawable.iv_unlike);
				}

				if (!TextUtils.isEmpty(data.title)) {
					tvTitle.setText(data.title);
				}

				if (!TextUtils.isEmpty(data.content)) {
					tvContent.setText(data.content);
					tvContent.setVisibility(View.VISIBLE);
				}

				String weatherFlag = CommonUtil.getWeatherFlag(data.weatherFlag);
				if (!TextUtils.isEmpty(weatherFlag)) {
					tvWeatherFlag.setText(weatherFlag);
					tvWeatherFlag.setBackgroundResource(R.drawable.corner_flag);
					tvWeatherFlag.setVisibility(View.VISIBLE);
				}
				String otherFlag = CommonUtil.getOtherFlag(data.otherFlag);
				if (!TextUtils.isEmpty(otherFlag)) {
					tvOtherFlag.setText(otherFlag);
					tvOtherFlag.setBackgroundResource(R.drawable.corner_flag);
					tvOtherFlag.setVisibility(View.VISIBLE);
				}

				if (!TextUtils.isEmpty(data.location)) {
					tvPositon.setText("拍摄地点：" + data.location);
				}

				if (!TextUtils.isEmpty(data.workTime)) {
					tvDate.setText("拍摄时间：" + data.workTime);
				}

				if (!TextUtils.isEmpty(data.commentCount)) {
					tvCommentCount.setText("评论" + "（" + data.commentCount + "）");
				}

				if (!TextUtils.isEmpty(data.videoId)) {
					//提交访问次数
					OkHttpPlayCount("http://channellive2.tianqi.cn/weather/work/fyjp_browsecount/resourceid/" + data.videoId);
				}

				//获取评论列表
				OkHttpCommentList(CONST.GET_WORK_COMMENT_URL);
			}
		}
	}

	/**
	 * 评论监听
	 */
	private TextWatcher watcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			if (!TextUtils.isEmpty(etComment.getText().toString())) {
				ivClear.setVisibility(View.VISIBLE);
				tvSubmit.setVisibility(View.VISIBLE);
			} else {
				ivClear.setVisibility(View.GONE);
				tvSubmit.setVisibility(View.GONE);
			}
		}
	};

	/**
	 * 清空输入内容
	 */
	private void clearContent() {
		if (etComment != null) {
			etComment.setText("");
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
				llSubmit.setVisibility(View.GONE);
				rePager.setVisibility(View.VISIBLE);
				if (mViewPager != null) {
					mViewPager.setCurrentItem(arg2);
				}
				CommonUtil.hideInputSoft(etComment, mContext);
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
			Picasso.with(mContext).load(urlList.get(i)).into(image);
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
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}
	
	/**
	 * 初始化listview
	 */
	private void initListView() {
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new CommentAdapter(mContext, mList);
		mListView.setAdapter(mAdapter);
	}

	/**
	 * 提交访问次数
	 */
	private void OkHttpPlayCount(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpUtil.enqueue(new Request.Builder().url(url).build(), new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
					}
					@Override
					public void onResponse(Call call, Response response) throws IOException {
					}
				});
			}
		}).start();
	}

	/**
	 * 获取评论列表
	 */
	private void OkHttpCommentList(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("wid", data.getVideoId());
		builder.add("page", page + "");
		builder.add("pagesize", pageSize + "");
		builder.add("appid", CONST.APPID);
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
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
												int status = object.getInt("status");
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

													tvCommentCount.setText("评论" + "（" + mList.size() + "）");
													if (mAdapter != null) {
														mAdapter.notifyDataSetChanged();
													}

												} else {
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
		}).start();
	}

	/**
	 * 提交评论
	 */
	private void OkHttpSubmitComment(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", MyApplication.TOKEN);
		builder.add("wid", data.videoId);
		builder.add("comment", EmojiMapUtil.replaceUnicodeEmojis(etComment.getText().toString()));
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
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
												int status = object.getInt("status");
												if (status == 1) {//成功
													clearContent();
													OkHttpCommentList(CONST.GET_WORK_COMMENT_URL);
												} else {
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
		}).start();
	}

	/**
	 * 点赞
	 */
	private void OkHttpPraise(final String url) {
		FormBody.Builder builder = new FormBody.Builder();
		builder.add("token", MyApplication.TOKEN);
		builder.add("id", data.videoId);
		final RequestBody body = builder.build();
		new Thread(new Runnable() {
			@Override
			public void run() {
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
												int status = object.getInt("status");
												if (status == 1) {//成功
													//保存点赞状态
													SharedPreferences sharedPreferences = getSharedPreferences(data.videoId, Context.MODE_PRIVATE);
													Editor editor = sharedPreferences.edit();
													editor.putBoolean("praiseState", true);
													editor.commit();
													ivPraise.setImageResource(R.drawable.iv_like);
												} else {
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
		}).start();
	}
	
	/**
	 * 隐藏虚拟键盘
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (etComment != null) {
			CommonUtil.hideInputSoft(etComment, mContext);
		}
		return super.onTouchEvent(event);
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
		if (mGridView.getVisibility() == View.GONE) {
			mGridView.setVisibility(View.VISIBLE);
			llListView.setVisibility(View.VISIBLE);
			llSubmit.setVisibility(View.VISIBLE);
			rePager.setVisibility(View.GONE);
		}else {
			finish();
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ivClear:
			clearContent();
			break;
		case R.id.tvSubmit:
			if (!TextUtils.isEmpty(etComment.getText().toString())) {
				CommonUtil.hideInputSoft(etComment, mContext);
				OkHttpSubmitComment(CONST.COMMENT_WORD_URL);
			}
			break;
		case R.id.ivPraise:
			if (praiseState) {
				return;
			} else {
				OkHttpPraise(CONST.PRAISE_WORK_URL);
			}
			break;
		case R.id.ivShare:
			if (data != null) {
				CommonUtil.share(OnlinePictureActivity.this, data.title, data.title, data.imgUrl, CONST.WEB + data.videoId + CONST.WEB_SUFFIX);
			}
			break;

		default:
			break;
		}
	}
	
}
