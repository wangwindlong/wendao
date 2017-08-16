package com.gzsll.jsbridge;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by sll on 2016/3/1.
 */
public class WVJBWebViewClient extends WebViewClient {

    private WVJBWebView mWVJBWebView;


    public WVJBWebViewClient(WVJBWebView wvjbWebView) {
        mWVJBWebView = wvjbWebView;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith(WVJBConstants.SCHEME)) {
            if (url.indexOf(WVJBConstants.BRIDGE_LOADED) > 0) {
                mWVJBWebView.injectJavascriptFile();
            } else if (url.indexOf(WVJBConstants.MESSAGE) > 0) {
                mWVJBWebView.flushMessageQueue();
            } else {
                Logger.d("UnkownMessage:" + url);
            }
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

}