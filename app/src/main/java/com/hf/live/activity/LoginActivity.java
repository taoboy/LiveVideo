package com.hf.live.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hf.live.R;
import com.hf.live.common.CONST;
import com.hf.live.common.MyApplication;
import com.hf.live.util.CommonUtil;
import com.hf.live.util.OkHttpUtil;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 登录页面
 */

public class LoginActivity extends BaseActivity implements OnClickListener {

    private Context mContext = null;
    private EditText etUserName, etPwd;
    private int seconds = 60;
    private Timer timer = null;
    private TextView tvSend, tvLogin = null;
    private ImageView ivSina, ivQQ, ivWechat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
        initWidget();
    }

    /**
     * 初始化控件
     */
    private void initWidget() {
        etUserName = (EditText) findViewById(R.id.etUserName);
        etPwd = (EditText) findViewById(R.id.etPwd);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(this);
        tvSend = (TextView) findViewById(R.id.tvSend);
        tvSend.setOnClickListener(this);
        ivSina = (ImageView) findViewById(R.id.ivSina);
        ivSina.setOnClickListener(this);
        ivQQ = (ImageView) findViewById(R.id.ivQQ);
        ivQQ.setOnClickListener(this);
        ivWechat = (ImageView) findViewById(R.id.ivWechat);
        ivWechat.setOnClickListener(this);
    }

    /**
     * 验证手机号码
     */
    private boolean checkMobileInfo() {
        if (TextUtils.isEmpty(etUserName.getText().toString())) {
            Toast.makeText(mContext, "请输入手机号码", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 获取验证码
     */
    private void OkHttpCode(final String url) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("phonenumber", etUserName.getText().toString().trim());
        final RequestBody body = builder.build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(url).post(body).build(), new Callback() {
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
                                        JSONObject obj = new JSONObject(result);
                                        if (!obj.isNull("status")) {
                                            if (TextUtils.equals(obj.getString("status"), "301")) {//成功发送验证码
                                                //发送验证码成功
                                                etPwd.setFocusable(true);
                                                etPwd.setFocusableInTouchMode(true);
                                                etPwd.requestFocus();
                                            } else {//发送验证码失败
                                                if (!obj.isNull("msg")) {
                                                    resetTimer();
                                                    Toast.makeText(mContext, obj.getString("msg"), Toast.LENGTH_SHORT).show();
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

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 101:
                    if (seconds <= 0) {
                        resetTimer();
                    } else {
                        tvSend.setText(seconds-- + "s");
                    }
                    break;

                default:
                    break;
            }
        }

        ;
    };

    /**
     * 验证登录信息
     */
    private boolean checkInfo() {
        if (TextUtils.isEmpty(etUserName.getText().toString())) {
            Toast.makeText(mContext, "请输入手机号码", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etPwd.getText().toString())) {
            Toast.makeText(mContext, "请输入手机验证码", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 登录
     */
    private void OkHttpLogin(final String url) {
        FormBody.Builder builder = new FormBody.Builder();
        builder.add("phonenumber", etUserName.getText().toString().trim());
        builder.add("vcode", etPwd.getText().toString().trim());
        final RequestBody body = builder.build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtil.enqueue(new Request.Builder().url(url).post(body).build(), new Callback() {
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
                                                        JSONObject obj = object.getJSONObject("info");
                                                        if (!obj.isNull("token")) {
                                                            MyApplication.TOKEN = obj.getString("token");
                                                        }
                                                        if (!obj.isNull("uid")) {
                                                            MyApplication.UID = obj.getString("uid");
                                                        }
                                                        if (!obj.isNull("phonenumber")) {
                                                            MyApplication.USERNAME = obj.getString("phonenumber");
                                                        }
                                                        if (!obj.isNull("username")) {
                                                            MyApplication.OLDUSERNAME = obj.getString("username");
                                                        }
                                                        if (!obj.isNull("nickname")) {
                                                            MyApplication.NICKNAME = obj.getString("nickname");
                                                        }
                                                        if (!obj.isNull("mail")) {
                                                            MyApplication.MAIL = obj.getString("mail");
                                                        }
                                                        if (!obj.isNull("department")) {
                                                            MyApplication.UNIT = obj.getString("department");
                                                        }
                                                        if (!obj.isNull("groupid")) {
                                                            MyApplication.GROUPID = obj.getString("groupid");
                                                        }
                                                        if (!obj.isNull("points")) {
                                                            MyApplication.POINTS = obj.getString("points");
                                                        }
                                                        if (!obj.isNull("photo")) {
                                                            MyApplication.PHOTO = obj.getString("photo");
                                                            if (!TextUtils.isEmpty(MyApplication.PHOTO)) {
                                                                CommonUtil.OkHttpLoadPortrait(LoginActivity.this, MyApplication.PHOTO);
                                                            }
                                                        }

                                                        //活动
                                                        if (!obj.isNull("type")) {
                                                            MyApplication.TYPE = obj.getString("type");
                                                        }
                                                        if (!obj.isNull("college")) {
                                                            MyApplication.COLLEGE = obj.getString("college");
                                                        }
                                                        if (!obj.isNull("major")) {
                                                            MyApplication.MAJOR = obj.getString("major");
                                                        }
                                                        if (!obj.isNull("votes")) {
                                                            MyApplication.VOTES = obj.getString("votes");
                                                        }
                                                        if (!obj.isNull("code")) {
                                                            MyApplication.CODE = obj.getString("code");
                                                        }

                                                        MyApplication.saveUserInfo(mContext);

                                                        cancelDialog();
                                                        resetTimer();
                                                        startActivity(new Intent(mContext, MainActivity2.class));
                                                        finish();

                                                    }
                                                } else if (status == 400) {//选择新用户或者老用户
                                                    if (!object.isNull("info")) {
                                                        JSONObject obj = object.getJSONObject("info");
                                                        if (!obj.isNull("token")) {
                                                            MyApplication.TOKEN = obj.getString("token");
                                                        }
                                                        if (!obj.isNull("phonenumber")) {
                                                            MyApplication.USERNAME = obj.getString("phonenumber");
                                                        }

                                                        cancelDialog();
                                                        resetTimer();
                                                        startActivity(new Intent(mContext, SelectUserActivity.class));

                                                    }
                                                } else {
                                                    //失败
                                                    if (!object.isNull("msg")) {
                                                        final String msg = object.getString("msg");
                                                        if (msg != null) {
                                                            cancelDialog();
                                                            resetTimer();
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
     * 重置计时器
     */
    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        seconds = 60;
        tvSend.setText("获取验证码");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetTimer();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvSend:
                if (checkMobileInfo()) {
                    if (timer == null) {
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(101);
                            }
                        }, 0, 1000);
                    }
                    OkHttpCode("http://channellive2.tianqi.cn/Weather/User/Login3Sendcode");
                }

                break;
            case R.id.tvLogin:
                if (checkInfo()) {
                    showDialog();
                    OkHttpLogin("http://channellive2.tianqi.cn/Weather/User/Login3");
                }
                break;
            case R.id.ivSina:
                UMShareAPI.get(mContext).getPlatformInfo(this, SHARE_MEDIA.SINA, new UMAuthListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {

                        Toast.makeText(mContext, "start", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {

                        Log.e("data", map + "");
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                        Toast.makeText(mContext, "error", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {
                        Toast.makeText(mContext, "cancel", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.ivQQ:
                UMShareAPI.get(mContext).getPlatformInfo(this, SHARE_MEDIA.QQ, new UMAuthListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {

                        Toast.makeText(mContext, "start", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {

                        Log.e("data", map + "");
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                        Toast.makeText(mContext, "error", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {
                        Toast.makeText(mContext, "cancel", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.ivWechat:
                UMShareAPI.get(mContext).getPlatformInfo(this, SHARE_MEDIA.WEIXIN, new UMAuthListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {

                        Toast.makeText(mContext, "start", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {

                        Log.e("data", map + "");
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                        Toast.makeText(mContext, "error", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {
                        Toast.makeText(mContext, "cancel", Toast.LENGTH_SHORT).show();
                    }
                });
                break;


            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

}
