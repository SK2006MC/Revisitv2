package com.sk.revisit2.webview;

import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.sk.revisit2.MyUtils;
import com.sk.revisit2.components.UrlMonitor;
import com.sk.revisit2.managers.WebResourceManager;
import com.sk.revisit2.models.UrlItem;

public class MyWebViewClient extends WebViewClient {

	final String TAG = MyWebViewClient.class.getSimpleName();
	final MyUtils myUtils;
	WebResourceManager resourceManager;
	LinearProgressIndicator progressBar;
	private UrlMonitor urlMonitor;

	public MyWebViewClient(@NonNull MyUtils myUtils) {
		this.myUtils = myUtils;
		this.resourceManager = new WebResourceManager(myUtils);
	}

	public void setUrlMonitor(UrlMonitor urlMonitor) {
		this.urlMonitor = urlMonitor;
	}

	public void setProgressBar(LinearProgressIndicator progressBar) {
		this.progressBar = progressBar;
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {
		if (urlMonitor != null) {
			urlMonitor.addUrlToMonitor(request.getUrl().toString(), request.getMethod());
		}

		WebResourceResponse response = resourceManager.getResponse(webView, request);
		if (urlMonitor != null) {
			UrlItem.Status status;
			if (response == null) {
				status = UrlItem.Status.IGNORED;
			} else if (response.getStatusCode() == 200) {
				status = UrlItem.Status.LOADED_REMOTE;
			} else {
				status = UrlItem.Status.ERROR;
			}
			try{
				urlMonitor.updateUrlStatus(request.getUrl().toString(), status,
					response != null ? Long.parseLong(response.getResponseHeaders().getOrDefault("content-length", "-1")) : 0, 100);
			}catch(Exception e){
				Log.e(TAG,e.toString());
			}
		}
		return response;
	}
}