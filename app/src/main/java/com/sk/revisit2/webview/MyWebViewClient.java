package com.sk.revisit2.webview;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.sk.revisit2.MyUtils;
import com.sk.revisit2.log.Log;
import com.sk.revisit2.managers.WebResourceManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

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
		return resourceManager.getResponse(webView,request);
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView arg0, WebResourceRequest arg1) {
		return super.shouldOverrideUrlLoading(arg0, arg1);
	}

	@Override
	public void onPageFinished(WebView view, String url) {
		super.onPageFinished(view, url);
		if (progressBar != null) {
			progressBar.setProgress(100);
			progressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		super.onPageStarted(view, url, favicon);
		if (progressBar != null) {
			progressBar.setProgress(0);
			progressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onLoadResource(WebView arg0, String arg1) {
		super.onLoadResource(arg0, arg1);
	}

}