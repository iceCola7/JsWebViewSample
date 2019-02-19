package com.cxz.webview.sample;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.cxz.webview.sample.util.Base64Util;
import com.cxz.webview.sample.util.RealPathUtil;
import com.google.gson.JsonObject;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;

import io.reactivex.functions.Consumer;

public class SampleActivity extends AppCompatActivity implements JsBridge {

    private static final int REQUEST_PICK_IMAGE = 0x0001111;
    private WebView mWebView;

    private String pickPhotoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        mWebView = findViewById(R.id.webView);

        initWebView();

    }

    private void initWebView() {
        mWebView.setWebViewClient(mWebViewClient);
        WebSettings settings = mWebView.getSettings();
        // 允许 WebView 加载 JS 代码
        settings.setJavaScriptEnabled(true);
        // 允许 JS 弹窗
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        mWebView.addJavascriptInterface(new JsInterface(this), "launcher");

        mWebView.loadUrl("file:///android_asset/index1.html");
    }

    WebViewClient mWebViewClient = new WebViewClient() {
        //将约定好的空js文件替换为本地的
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            WebResourceResponse webResourceResponse = super.shouldInterceptRequest(view, url);
            if (url == null) {
                return webResourceResponse;
            }
            if (url.endsWith("native-app.js")) {
                try {
                    webResourceResponse = new WebResourceResponse("text/javascript",
                            "UTF-8", SampleActivity.this.getAssets().open("js-native.js"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return webResourceResponse;
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            WebResourceResponse webResourceResponse = super.shouldInterceptRequest(view, request);
            if (request == null) {
                return webResourceResponse;
            }
            String url = request.getUrl().toString();
            if (url != null && url.endsWith("native-app.js")) {
                try {
                    webResourceResponse = new WebResourceResponse("text/javascript",
                            "UTF-8", SampleActivity.this.getAssets().open("js-native.js"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return webResourceResponse;
        }
    };

    @Override
    public void goPickPhoto(final String funcName, String jsonStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pickPhotoName = funcName;
                new RxPermissions(SampleActivity.this)
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                if (aBoolean) {
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                    startActivityForResult(intent, REQUEST_PICK_IMAGE);
                                } else {
                                    Toast.makeText(SampleActivity.this, "请给予权限，谢谢", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            //系统相册选取完成
            Uri uri = data.getData();
            if (uri != null) {
                String filePath;
                if (!TextUtils.isEmpty(uri.toString()) && uri.toString().startsWith("file")) {
                    filePath = uri.getPath();
                } else {
                    filePath = RealPathUtil.getRealPathFromURI(this, uri);
                }
                String base64Image = Base64Util.encodeBase64ImageFile(filePath);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("image64", base64Image);
                jsonObject.addProperty("message", "图片获取成功");
                Log.d("SampleActivity", "jsonObject:" + jsonObject);
                mWebView.loadUrl("javascript:sdk_nativeCallback(\'" + pickPhotoName + "\',\'" + jsonObject + "\')");
            }
        }
    }
}
