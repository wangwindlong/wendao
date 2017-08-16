package com.gzsll.jsbridge;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by sll on 2016/5/5.
 */
public class WVJBWebView extends WebView {

    private ArrayList<WVJBMessage> messageQueue = new ArrayList<>();
    private Map<String, WVJBResponseCallback> responseCallbacks = new HashMap<>();
    private Map<String, WVJBHandler> messageHandlers = new HashMap<>();
    private long uniqueId = 0;
    private MyJavascriptInterface myInterface = new MyJavascriptInterface();
    private String script;


    public interface WVJBResponseCallback {
        void callback(Object data);
    }

    public interface WVJBHandler {
        void request(Object data, WVJBResponseCallback callback);
    }

    public WVJBWebView(Context context) {
        super(context);
        init();
    }

    public WVJBWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WVJBWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        getSettings().setJavaScriptEnabled(true);
        addJavascriptInterface(myInterface, WVJBConstants.INTERFACE);
        setWebViewClient(new WVJBWebViewClient(this));
        // 开启DOM缓存，开启LocalStorage存储（html5的本地存储方式）
        getSettings().setDomStorageEnabled(true);
        getSettings().setDatabaseEnabled(true);
//        getSettings().setDatabasePath(getContext().getCacheDir().getAbsolutePath());
    }


    public void callHandler(String handlerName) {
        callHandler(handlerName, null, null);
    }

    public void callHandler(String handlerName, Object data) {
        callHandler(handlerName, data, null);
    }

    public void callHandler(String handlerName, Object data,
                            WVJBResponseCallback callback) {
        sendData(data, callback, handlerName);
    }

    public void registerHandler(String handlerName, WVJBHandler handler) {
        if (TextUtils.isEmpty(handlerName) || handler == null)
            return;
        messageHandlers.put(handlerName, handler);
    }


    private void sendData(Object data, WVJBResponseCallback callback,
                          String handlerName) {
        if (data == null && TextUtils.isEmpty(handlerName))
            return;
        WVJBMessage message = new WVJBMessage();
        if (data != null) {
            message.data = data;
        }
        if (callback != null) {
            String callbackId = "java_cb_" + (++uniqueId);
            responseCallbacks.put(callbackId, callback);
            message.callbackId = callbackId;
        }
        if (handlerName != null) {
            message.handlerName = handlerName;
        }
        queueMessage(message);
    }


    private void queueMessage(WVJBMessage message) {
        if (messageQueue != null) {
            messageQueue.add(message);
        } else {
            dispatchMessage(message);
        }
    }

    public void dispatchMessage(WVJBMessage message) {
        String messageJSON = doubleEscapeString(message2Json(message).toString());
        executeJavascript("WebViewJavascriptBridge._handleMessageFromJava('"
                + messageJSON + "');");
    }


    private String doubleEscapeString(String javascript) {
        String result;
        result = javascript.replace("\\", "\\\\");
        result = result.replace("\"", "\\\"");
        result = result.replace("\'", "\\\'");
        result = result.replace("\n", "\\n");
        result = result.replace("\r", "\\r");
        result = result.replace("\f", "\\f");
        return result;
    }

    private JSONObject message2Json(WVJBMessage message) {
        JSONObject object = new JSONObject();
        try {
            if (message.callbackId != null) {
                object.put("callbackId", message.callbackId);
            }
            if (message.data != null) {
                object.put("data", message.data);
            }
            if (message.handlerName != null) {
                object.put("handlerName", message.handlerName);
            }
            if (message.responseId != null) {
                object.put("responseId", message.responseId);
            }
            if (message.responseData != null) {
                object.put("responseData", message.responseData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }


    public void flushMessageQueue() {
        String script = "WebViewJavascriptBridge._fetchQueue()";
        executeJavascript(script, new JavascriptCallback() {
            public void onReceiveValue(String messageQueueString) {
                if (!TextUtils.isEmpty(messageQueueString)) {
                    processMessageQueue(messageQueueString);
                }
            }
        });
    }

    private void processMessageQueue(String messageQueueString) {
            if(TextUtils.isEmpty(messageQueueString)){
                return;
            }
            try {
                JSONArray messages = new JSONArray(messageQueueString);
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject jo = messages.getJSONObject(i);
                    WVJBMessage message = json2Message(jo);
                    if (message.responseId != null) {
                        WVJBResponseCallback responseCallback = responseCallbacks
                                .remove(message.responseId);
                        if (responseCallback != null) {
                            responseCallback.callback(message.responseData);
                        }
                    } else {
                        WVJBResponseCallback responseCallback = null;
                        if (message.callbackId != null) {
                            final String callbackId = message.callbackId;
                            responseCallback = new WVJBResponseCallback() {
                                @Override
                                public void callback(Object data) {
                                    WVJBMessage msg = new WVJBMessage();
                                    msg.responseId = callbackId;
                                    msg.responseData = data;
                                    queueMessage(msg);
                                }
                            };
                        }

                        WVJBHandler handler = messageHandlers.get(message.handlerName);

                        if (handler != null) {
                            handler.request(message.data, responseCallback);
                        } else {
                            Log.e(WVJBConstants.TAG, "No handler for message from JS:" + message.handlerName);
                        }
                    }
                }
            }catch (JSONException exception){
                exception.printStackTrace();
            }

    }

    private WVJBMessage json2Message(JSONObject object) {
        WVJBMessage message = new WVJBMessage();
        try {
            if (object.has("callbackId")) {
                message.callbackId = object.getString("callbackId");
            }
            if (object.has("data")) {
                message.data = object.get("data");
            }
            if (object.has("handlerName")) {
                message.handlerName = object.getString("handlerName");
            }
            if (object.has("responseId")) {
                message.responseId = object.getString("responseId");
            }
            if (object.has("responseData")) {
                message.responseData = object.get("responseData");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }


    public void injectJavascriptFile() {
        try {
            if (TextUtils.isEmpty(script)) {
                InputStream in = getResources().getAssets().open("WebViewJavascriptBridge.js");
                script = convertStreamToString(in);
            }
            executeJavascript(script);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (messageQueue != null) {
            for (WVJBMessage message : messageQueue) {
                dispatchMessage(message);
            }
            messageQueue = null;
        }
    }


    private String convertStreamToString(InputStream is) {
        String s = "";
        try {
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            if (scanner.hasNext()) s = scanner.next();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    private void executeJavascript(String script) {
        executeJavascript(script, null);
    }

    private void executeJavascript(final String script,
                                  final JavascriptCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    if (callback != null) {
                        if (value != null && value.startsWith("\"")
                                && value.endsWith("\"")) {
                            value = value.substring(1, value.length() - 1)
                                    .replaceAll("\\\\", "");
                        }
                        callback.onReceiveValue(value);
                    }
                }
            });
        } else {
            if (callback != null) {
                myInterface.addCallback(++uniqueId + "", callback);
                post(new Runnable() {
                    @Override
                    public void run() {
                        loadUrl("javascript:window." + WVJBConstants.INTERFACE
                                + ".onResultForScript(" + uniqueId + "," + script + ")");
                    }
                });
            } else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        loadUrl("javascript:" + script);
                    }
                });
            }
        }
    }


    private class MyJavascriptInterface {
        Map<String, JavascriptCallback> map = new HashMap<>();

        public void addCallback(String key, JavascriptCallback callback) {
            map.put(key, callback);
        }

        @JavascriptInterface
        public void onResultForScript(String key, String value) {
            JavascriptCallback callback = map.remove(key);
            if (callback != null)
                callback.onReceiveValue(value);
        }
    }

    public interface JavascriptCallback {
        void onReceiveValue(String value);
    }




}
