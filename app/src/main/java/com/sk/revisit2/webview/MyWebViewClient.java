package com.sk.revisit2.webview;

import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.sk.revisit2.MyUtils;
import com.sk.revisit2.managers.WebResourceManager;

public class MyWebViewClient extends WebViewClient {

	final String TAG = MyWebViewClient.class.getSimpleName();
	final MyUtils myUtils;
	WebResourceManager resourceManager;
	LinearProgressIndicator progressBar;

	public MyWebViewClient(@NonNull MyUtils myUtils) {
		this.myUtils = myUtils;
		this.resourceManager = new WebResourceManager(myUtils);
	}

	public void setProgressBar(LinearProgressIndicator progressBar) {
		this.progressBar = progressBar;
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {
		return resourceManager.getResponse(webView, request);
	}

}