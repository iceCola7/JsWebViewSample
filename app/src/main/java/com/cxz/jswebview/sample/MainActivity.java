package com.cxz.jswebview.sample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

        // 允许 WebView 加载 JS 代码
        webView.getSettings().setJavaScriptEnabled(true);

        // 给 WebView 添加 JS 接口
        webView.addJavascriptInterface(new JsInterface(this), "launcher");

        webView.loadUrl("file:///android_asset/index.html");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = editText.getText().toString();
                // Android 调用 JS 方法
                webView.loadUrl("javascript:if(window.callJS){window.callJS('" + str + "');}");
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
