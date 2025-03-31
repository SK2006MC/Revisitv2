package com.sk.revisit2.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.sk.revisit2.MyUtils;

public class MyWebView extends WebView {

	final Context context;
	AttributeSet attributeSet;
	int defStyleAttr;
	MyWebViewClient webViewClient;
	MyWebChromeClient webChromeClient;
	MyUtils myUtils;

	public MyWebView(Context context) {
		super(context);
		this.context = context;
		init(context);
	}

	public MyWebView(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		this.context = context;
		this.attributeSet = attributeSet;
		init(context);
	}

	public MyWebView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
		this.attributeSet = attrs;
		this.defStyleAttr = defStyleAttr;
		init(context);
	}

	public void setMyUtils(@NonNull MyUtils utils) {
		this.myUtils = utils;
		webViewClient = new MyWebViewClient(myUtils);
		webChromeClient = new MyWebChromeClient();

		setWebChromeClient(webChromeClient);
		setWebViewClient(webViewClient);
	}

	@SuppressLint("SetJavaScriptEnabled")
	void init(Context context) {
		WebSettings webSettings = getSettings();
		webSettings.setAllowContentAccess(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowFileAccessFromFileURLs(true);
		webSettings.setAllowUniversalAccessFromFileURLs(true);
		//webSettings.setAppCacheEnabled(true);
		webSettings.setDatabaseEnabled(true);
		//webSettings.setBuiltInZoomControls(false);
		//webSettings.setDisplayZoomControls(false);
		webSettings.setDomStorageEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setJavaScriptEnabled(true);
		//webSettings.setLoadWithOverviewMode(false);
		//webSettings.setLoadsImagesAutomatically(false);
		webSettings.setMediaPlaybackRequiresUserGesture(true);
		webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		//webSettings.setNeedInitialFocus(false);
		webSettings.setOffscreenPreRaster(true);
		//webSettings.setRenderPriority();
		webSettings.setSafeBrowsingEnabled(false);
		webSettings.setSupportMultipleWindows(true);
		webSettings.setSupportZoom(true);
		webSettings.setUseWideViewPort(true);
		//webSettings.setUserAgentString();
	}
}