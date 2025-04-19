package com.sk.revisit2.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.sk.revisit2.MyUtils;
import com.sk.revisit2.preferences.WebPreferenceManager;

public class MyWebView extends WebView {

	final Context context;
	public MyWebViewClient webViewClient;
	public MyWebChromeClient webChromeClient;
	public MyUtils myUtils;
	AttributeSet attributeSet;
	int defStyleAttr;
	private WebPreferenceManager preferenceManager;

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

	public void setProgressBar(LinearProgressIndicator progressBar) {
		webChromeClient.setProgressBar(progressBar);
		webViewClient.setProgressBar(progressBar);
	}

	@SuppressLint("SetJavaScriptEnabled")
	void init(Context context) {
		preferenceManager = new WebPreferenceManager(context);
		WebSettings webSettings = getSettings();

		webSettings.setAllowContentAccess(true);
		webSettings.setAllowFileAccess(true);
		webSettings.setAllowFileAccessFromFileURLs(true);
		webSettings.setAllowUniversalAccessFromFileURLs(true);
		webSettings.setOffscreenPreRaster(true);
		webSettings.setUseWideViewPort(true);

		preferenceManager.applyToWebSettings(webSettings);
		preferenceManager.registerOnSharedPreferenceChangeListener((prefs, key) -> {
			preferenceManager.applyToWebSettings(webSettings);
		});
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (preferenceManager != null) {
			preferenceManager.unregisterOnSharedPreferenceChangeListener(
					(prefs, key) -> preferenceManager.applyToWebSettings(getSettings())
			);
		}
	}
}