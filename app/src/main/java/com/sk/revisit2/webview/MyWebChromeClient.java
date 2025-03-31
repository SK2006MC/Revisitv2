package com.sk.revisit2.webview;

import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class MyWebChromeClient extends WebChromeClient {

	@Override
	public void onProgressChanged(WebView arg0, int arg1) {
		super.onProgressChanged(arg0, arg1);
	}

	@Override
	public void getVisitedHistory(ValueCallback<String[]> arg0) {
		super.getVisitedHistory(arg0);
	}
}