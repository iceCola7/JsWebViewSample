package com.cxz.webview.sample;

import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * @author chenxz
 * @date 2018/9/12
 * @desc 编写 JS 接口类
 */
public class JsInterface {

    private static final String TAG = "JsInterface";

    private JsBridge jsBridge;

    public JsInterface(JsBridge jsBridge) {
        this.jsBridge = jsBridge;
    }

    /**
     * 这个方法由 JS 调用， 不在主线程执行   暴露给sdk的本地方法
     *
     * @param funcName
     * @param jsonStr
     */
    @JavascriptInterface
    public void native_launchFunc(final String funcName, final String jsonStr) {
        Log.e(TAG, "funcName::" + funcName + ",json::" + jsonStr);
        if (funcName.startsWith("pickPhoto")) {
            jsBridge.goPickPhoto(funcName, jsonStr);
        }
    }

}
