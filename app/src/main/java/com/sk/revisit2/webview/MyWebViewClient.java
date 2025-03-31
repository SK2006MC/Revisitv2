package com.sk.revisit2.webview;

import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;

import androidx.annotation.NonNull;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.sk.revisit2.log.Log;
import com.sk.revisit2.MyUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MyWebViewClient extends WebViewClient {

	final MyUtils myUtils;
	final String TAG = MyWebViewClient.class.getSimpleName();
	LinearProgressIndicator progressBar;

	public MyWebViewClient(@NonNull MyUtils myUtils) {
		this.myUtils = myUtils;
	}

	public void setProgressBar(LinearProgressIndicator progressBar) {
		this.progressBar = progressBar;
	}

	@Override
	public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {
		Uri uri = request.getUrl();
		if (!request.getMethod().equals("GET")) {
			return null;
		}

		String localFilePath = myUtils.buildLocalPath(uri);
		myUtils.logUrl(uri + " -> " + localFilePath);
		File localFile = new File(localFilePath);

		if (localFile.exists()) {
			if (MyUtils.shouldUpdate) {
				myUtils.download(request);
				Log.i(TAG, "updating from local");
			}
			Log.i(TAG, "loading from local");
			return loadFromLocal(localFile);
		} else {
			Log.i(TAG, "need to download");
			if (MyUtils.isNetWorkAvailable) {
				myUtils.download(request);
				return loadFromLocal(localFile);
			}
			myUtils.logReq(request);
		}
		return new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream("no off file".getBytes()));
	}

	WebResourceResponse loadFromLocal(File file) {
		WebResourceResponse response;
		String localFile = file.getAbsolutePath();
		String mimeType = getMimeType(localFile);
		String encoding = getEncoding(file);
		//Map<String,String> headers = getHeaders(localFile);
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
		} catch (Exception e) {
			inputStream = new ByteArrayInputStream("err".getBytes());
			Log.e(TAG, " ", e);
		}
		response = new WebResourceResponse(mimeType, encoding, inputStream);
		//response.setResponseHeaders(headers);
		return response;
	}

	String getMimeType(String file) {
		String mimeType = myUtils.getMimeTypeFromMeta(file);
		if (mimeType == null) {
			//myUtils.createMeteMimeType()
			mimeType = "application/oclet-stream";
		}
		return mimeType;
	}

	String getEncoding(File file) {
		String encoding = myUtils.guessEncodingFromFile(file);
		if (encoding == null) {
			encoding = "UTF-8";
		}
		return encoding;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView arg0, WebResourceRequest arg1) {
		log1("shouldOverrideUrlLoading",arg1.getUrl().toString());
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
		log1("onLoadResource",arg1);
	}

	void log1(String funcName,String msg){
		myUtils.Log(TAG,funcName+" -> "+msg);
	}
}