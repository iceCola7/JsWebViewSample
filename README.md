# JsWebViewSample（Android 和 JS 交互实践）

`Android` 与 `JS` 交互实际上是通过 `WebView` 互相调用方法：

- `Android` 去调用 `JS` 的代码；
- `JS` 去调用 `Android` 的代码。

## 一、JS 调用 Android 方法

#### 方法一：通过 WebView 的 addJavascriptInterface() 进行对象映射

> 优点：使用简单，仅将Android对象和JS对象映射即可

> 缺点：存在漏洞问题

###### 1）允许 WebView 加载 JS
`webView.getSettings().setJavaScriptEnabled(true);`

###### 2）编写 JS 接口
``` 
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
```

###### 3）给 WebView 添加 JS 接口
`webView.addJavascriptInterface(new JsInterface(this), "launcher");// 此处的 launcher 可以自定义，最终是 JS 中要使用的对象 `

###### 4）JS 代码中调用 Java 方法
```
if (window.launcher){ // 判断 launcher 对象是否存在
	// 此处的 launcher 要和 第3步中定义的 launcher 保持一致
    // JS 调用 Android 的方法
    launcher.callAndroid(str);
}else{
    alert("launcher not found!");
}
```

#### 方法二：通过 WebViewClient 的 shouldOverrideUrlLoading() 方法回调拦截 url 

> 优点：不存在方式一的漏洞；

> 缺点：JS获取Android方法的返回值复杂。 

###### 1）JS 代码中，约定协议
```
function callAndroid(){
    // 约定的 url 协议为：js://webview?arg1=111&arg2=222
    document.location = "js://webview?arg1="+inputEle.value+"&arg2=222";
}
```

###### 2）Android 代码中，通过设置 WebViewClient 对协议进行拦截处理

```
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
```

#### 方法三：通过 WebChromeClient 的 onJsAlert() 、 onJsConfirm() 、 onJsPrompt（）方法回调拦截 JS 对话框 alert() 、 confirm() 、 prompt（） 消息

> 处理方式和方法二差不多

###### 1）JS代码中，约定协议 
```
// 调用 prompt()
var result=prompt("js://prompt?arg1="+inputEle.value+"&arg2=222");
alert("prompt：" + result);
```

###### 2）Android 代码中，通过设置 WebChromeClient 对协议进行拦截处理
```
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
```

## 二、 Android 调用 JS 方法

#### 方法一： 通过 WebView 的 loadUrl()

###### 1）编写 JS 方法
```
var callJS = function(str){
    inputEle.value = str;
}
```

###### 2）使用 webView.loadUrl() 调用 JS 方法
```
// Android 调用 JS 方法
webView.loadUrl("javascript:if(window.callJS){window.callJS('" + str + "');}");
```

#### 方法二： 通过 WebView 的 evaluateJavascript()
> - 该方法比第一种方法效率更高，使用更简洁；
> - 该方法执行不会刷新页面，而第一种方法（ loadUrl ）则会；
> - Android 4.4 以后才能使用。

```
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    webView.evaluateJavascript("javascript:if(window.callJS){window.callJS('" + str + "');}", new ValueCallback<String>() {
        @Override
        public void onReceiveValue(String value) {
            Log.e("TAG", "--------->>" + value);
        }
    });
}
```
