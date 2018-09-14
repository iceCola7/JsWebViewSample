# JsWebViewSample
Android 和 JS 互调实践

### 一、JS 调用 Android 方法

###### 1.允许 WebView 加载 JS
`webView.getSettings().setJavaScriptEnabled(true);`

###### 2.编写 JS 接口
``` 
public class JsInterface {
    private static final String TAG = "JsInterface";
    private JsBridge jsBridge;
    public JsInterface(JsBridge jsBridge) {
        this.jsBridge = jsBridge;
    }
    /**
     * 这个方法由 JS 调用， 不在主线程执行
     */
    @JavascriptInterface
    public void callAndroid(String value) {
        Log.i(TAG, "value = " + value);
        jsBridge.setTextValue(value);
    }
}
```

###### 3.给 WebView 添加 JS 接口
```
// 此处的 launcher 可以自定义，最终是 JS 中要使用的对象
webView.addJavascriptInterface(new JsInterface(this), "launcher"); 
```

###### 4. JS 代码中调用 Java 方法
```
// 判断 launcher 对象是否存在
if (window.launcher){ 
    // 此处的 launcher 要和 第3步中定义的 launcher 保持一致
    // JS 调用 Android 的方法
    launcher.callAndroid(str);
}else{
    alert("launcher not found!");
}
```

### 二、 Android 调用 JS 方法

###### 1.编写 JS 方法
```
var callJS = function(str){
    inputEle.value = str;
}
```

###### 2.使用 webView.loadUrl() 调用 JS 方法
```
// Android 调用 JS 方法
webView.loadUrl("javascript:if(window.callJS){window.callJS('" + str + "');}");
```
