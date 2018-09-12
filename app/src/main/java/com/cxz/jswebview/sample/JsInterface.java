package com.cxz.jswebview.sample;

import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * @author chenxz
 * @date 2018/9/12
 * @desc
 */
public class JsInterface {

    private static final String TAG = "JsInterface";

    private JsBridge jsBridge;

    public JsInterface(JsBridge jsBridge) {
        this.jsBridge = jsBridge;
    }

    /**
     * 这个方法由 JS 调用， 不在主线程执行
     *
     * @param value
     */
    @JavascriptInterface
    public void callAndroid(String value) {
        Log.i(TAG, "value = " + value);
        jsBridge.setTextValue(value);
    }

}
