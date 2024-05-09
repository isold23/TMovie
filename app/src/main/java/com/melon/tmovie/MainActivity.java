package com.melon.tmovie;


import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.content.pm.ActivityInfo;
import android.view.WindowManager;
import android.webkit.*;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.jetbrains.annotations.NotNull;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.melon.tmovie.update.APPUpdate;


public class MainActivity extends AppCompatActivity {
    private FrameLayout mFrameLayout;
    private WebView mWebView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private APPUpdate appUpdate = new APPUpdate();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        setContentView(R.layout.activity_main);
        mWebView = findViewById(R.id.webview);
        mFrameLayout = findViewById(R.id.mFrameLayout);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        mWebView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWebView.getSettings().setSafeBrowsingEnabled(false);
        }

        mWebView.getSettings().setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }

        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setDomStorageEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.getSettings().setAllowFileAccess(true);
                view.loadUrl(url);
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                WebResourceResponse response = super.shouldInterceptRequest(view, request);
                if (response != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        response.setResponseHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"));
                    }
                }
                return response;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                //Log.d("WebView", consoleMessage.message());
                return true;
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                if (mCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                mCustomView = view;
                mFrameLayout.addView(mCustomView);
                mCustomViewCallback = callback;
                mWebView.setVisibility(View.GONE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }

            public void onHideCustomView() {
                mWebView.setVisibility(View.VISIBLE);
                if (mCustomView == null) {
                    return;
                }
                mCustomView.setVisibility(View.GONE);
                mFrameLayout.removeView(mCustomView);
                mCustomViewCallback.onCustomViewHidden();
                mCustomView = null;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                super.onHideCustomView();
            }
        });

        mWebView.setOnTouchListener(new View.OnTouchListener() {
            private float startX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        Log.d("view", "MotionEvent.ACTION_DOWN");
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        Log.d("view", "MotionEvent.ACTION_UP");
                        if (endX - startX > 100) {
                            if(mWebView.canGoBack()) {
                                mWebView.goBack();
                                Log.d("view", "MotionEvent.goBack");
                            }
                        } else if (startX - endX > 100) {
                            if(mWebView.canGoForward()) {
                                mWebView.goForward();
                                Log.d("view", "MotionEvent.goForward");
                            }
                        }
                        break;
                }
                return false;
            }
        });


        mWebView.loadUrl("http://tv.hzdianyue.com/");

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mWebView == null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    return;
                }
                // 在 UI 线程上执行 WebView 刷新
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.reload();
                    }
                });
            }
        });

        mSwipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(@NonNull SwipeRefreshLayout swipeRefreshLayout, @Nullable View view) {
                // 原生安卓使用getScrollY()来判断在Y轴的距离
                return mWebView.getScrollY() > 0;
                //使用的X5内核的webview需要调用getWebScrollY()来判断在Y轴的距离
                //return mWebView.getWebScrollY() > 0;
            }
        });
        request_permissions();

    }

    @Override
    public void onConfigurationChanged(@NotNull Configuration config) {
        super.onConfigurationChanged(config);
        switch (config.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }
/*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        String TAG = "key";
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:     //确定键enter
            case KeyEvent.KEYCODE_DPAD_CENTER:
                Log.d(TAG, "enter--->");
                break;

            case KeyEvent.KEYCODE_BACK:    //返回键
                Log.d(TAG,"back--->");
                return true;   //这里由于break会退出，所以我们自己要处理掉 不返回上一层

            case KeyEvent.KEYCODE_SETTINGS: //设置键
                Log.d(TAG, "setting--->");
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:   //向下键

                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "down--->");
                }

                break;

            case KeyEvent.KEYCODE_DPAD_UP:   //向上键
                Log.d(TAG, "up--->");

                break;

            case KeyEvent.KEYCODE_0:   //数字键0
                Log.d(TAG, "0--->");

                break;
            case KeyEvent.KEYCODE_DPAD_LEFT: //向左键
                Log.d(TAG, "left--->");
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:  //向右键
                Log.d(TAG, "right--->");
                break;

            case KeyEvent.KEYCODE_INFO:    //info键
                Log.d(TAG, "info--->");

                break;

            case KeyEvent.KEYCODE_PAGE_DOWN:     //向上翻页键
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                Log.d(TAG, "page down--->");
                break;


            case KeyEvent.KEYCODE_PAGE_UP:     //向下翻页键
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                Log.d(TAG, "page up--->");

                break;

            case KeyEvent.KEYCODE_VOLUME_UP:   //调大声音键
                Log.d(TAG, "voice up--->");

                break;

            case KeyEvent.KEYCODE_VOLUME_DOWN: //降低声音键
                Log.d(TAG, "voice down--->");

                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE: //禁用声音
                Log.d(TAG, "voice mute--->");
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    */

    private void request_permissions() {
        // 创建一个权限列表，把需要使用而没用授权的的权限存放在这里
        List<String> permissionList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        // 如果列表为空，就是全部权限都获取了，不用再次获取了。不为空就去申请权限
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionList.toArray(new String[permissionList.size()]), 1002);
        }
    }
    // 请求权限回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean ret = true;
        switch (requestCode) {
            case 1002:
                // 1002请求码对应的是申请多个权限
                if (grantResults.length > 0) {
                    // 因为是多个权限，所以需要一个循环获取每个权限的获取情况
                    for (int i = 0; i < grantResults.length; i++) {
                        // PERMISSION_DENIED 这个值代表是没有授权，我们可以把被拒绝授权的权限显示出来
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                            ret = false;
                        }
                    }
                }
                break;
        }
        if(ret) {
            appUpdate.update(MainActivity.this);
        }
    }
}