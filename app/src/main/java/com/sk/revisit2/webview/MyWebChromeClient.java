package com.sk.revisit2.webview;

import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class MyWebChromeClient extends WebChromeClient {
	LinearProgressIndicator progressBar;

	@Override
	public void onProgressChanged(WebView arg0, int arg1) {
		super.onProgressChanged(arg0, arg1);
		if (progressBar != null) {
			progressBar.setProgress(arg1);
		}
	}

	public void setProgressBar(LinearProgressIndicator progressBar){
		this.progressBar = progressBar;
	}

	@Override
	public void getVisitedHistory(ValueCallback<String[]> arg0) {
		super.getVisitedHistory(arg0);
	}
}