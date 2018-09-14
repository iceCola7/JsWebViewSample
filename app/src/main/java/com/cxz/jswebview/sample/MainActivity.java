package com.cxz.jswebview.sample;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements JsBridge {


    private WebView webView;
    private TextView tv_result;
    private EditText editText;
    private Button button;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        initViews();

    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initViews() {
        webView = findViewById(R.id.webView);
        tv_result = findViewById(R.id.tv_result);
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);

        WebSettings settings = webView.getSettings();

        // 允许 WebView 加载 JS 代码
        settings.setJavaScriptEnabled(true);
        // 允许 JS 弹窗
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 给 WebView 添加 JS 接口
        // 此处的 launcher 可以自定义，最终是 JS 中要使用的对象
        webView.addJavascriptInterface(new JsInterface(this), "launcher");

        webView.loadUrl("file:///android_asset/index.html");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = editText.getText().toString();
                // Android 调用 JS 方法
                webView.loadUrl("javascript:if(window.callJS){window.callJS('" + str + "');}");

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    webView.evaluateJavascript("javascript:if(window.callJS){window.callJS('" + str + "');}", new ValueCallback<String>() {
//                        @Override
//                        public void onReceiveValue(String value) {
//                            Log.e("TAG", "--------->>" + value);
//                        }
//                    });
//                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                // 例如：url = "js://webview?arg1=111&arg2=222"
                Uri uri = Uri.parse(url);
                // 如果url的协议 = 预先约定的 js 协议
                if (uri.getScheme().equals("js")) {
                    // 拦截url,下面JS开始调用Android需要的方法
                    if (uri.getAuthority().equals("webview")) {
                        // 执行JS所需要调用的逻辑
                        Log.e("TAG", "JS 调用了 Android 的方法");
                        Set<String> collection = uri.getQueryParameterNames();
                        Iterator<String> it = collection.iterator();
                        String result = "";
                        while (it.hasNext()) {
                            result += uri.getQueryParameter(it.next()) + ",";
                        }
                        tv_result.setText(result);
                    }
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                // 例如：url = "js://webview?arg1=111&arg2=222"
                Uri uri = Uri.parse(message);
                Log.e("TAG", "----onJsPrompt--->>" + url + "," + message);
                // 如果url的协议 = 预先约定的 js 协议
                if (uri.getScheme().equals("js")) {
                    // 拦截url,下面JS开始调用Android需要的方法
                    if (uri.getAuthority().equals("prompt")) {
                        // 执行JS所需要调用的逻辑
                        Log.e("TAG", "JS 调用了 Android 的方法");
                        Set<String> collection = uri.getQueryParameterNames();
                        Iterator<String> it = collection.iterator();
                        String result2 = "";
                        while (it.hasNext()) {
                            result2 += uri.getQueryParameter(it.next()) + ",";
                        }
                        tv_result.setText(result2);
                    }
                    return true;
                }
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.e("TAG", "----onJsAlert--->>" + url+ "," + message);
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                Log.e("TAG", "----onJsConfirm--->>" + url+ "," + message);
                return super.onJsConfirm(view, url, message, result);
            }
        });

    }

    @Override
    public void setTextValue(final String value) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                tv_result.setText(value);
            }
        });
    }
}
