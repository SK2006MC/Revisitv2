package com.sk.revisit2.managers;

import android.net.Uri;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sk.revisit2.MyUtils;
import com.sk.revisit2.log.Log;
import com.sk.revisit2.log.LoggerManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class WebResourceManager {

	String TAG = this.getClass().getSimpleName();
	MyUtils myUtils;
	LoggerManager loggerManager;

	public WebResourceManager(MyUtils myUtils) {
		this.myUtils = myUtils;
		this.loggerManager = myUtils.getLoggerManager();
	}

	public WebResourceResponse getResponse(WebView webView, @NonNull WebResourceRequest request) {
		Uri uri = request.getUrl();
		String url = uri.toString();

		if (!URLUtil.isNetworkUrl(url)) {
			Log.i(TAG, "Non network url: " + url);
			return null;
		}

		if (request.isRedirect()) {
			Log.i(TAG, "Redirected: " + url);
			return null;
		}

		if (!request.getMethod().equals("GET")) {
			Log.i(TAG, "Not GET Method: " + request.getMethod() + ':' + url);
			return null;
		}

		String localFilePath = myUtils.buildLocalPath(uri);
		loggerManager.logUrl(uri + " -> " + localFilePath);
		File localFile = new File(localFilePath);

		if (localFile.exists()) {
			if (MyUtils.shouldUpdate && MyUtils.isNetWorkAvailable) {
				myUtils.download(request);
				Log.i(TAG, "updating local resource");
			}
			Log.i(TAG, "loading from local");
			return loadFromLocal(localFile);
		} else {
			Log.i(TAG, "need to download");
			if (MyUtils.isNetWorkAvailable) {
				myUtils.download(request);
				return loadFromLocal(localFile);
			} else {
				loggerManager.logRequest(request);
				return new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream("no off file".getBytes()));
			}
		}
	}

	@NonNull
	private WebResourceResponse loadFromLocal(@NonNull File file) {
		WebResourceResponse response;
		String localFile = file.getAbsolutePath();
		String mimeType = getMimeType(localFile);
		String encoding = getEncoding(file);
		Map<String, String> headers = getHeaders(localFile);
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
		} catch (Exception e) {
			inputStream = new ByteArrayInputStream(e.toString().getBytes());
			Log.e(TAG, e);
		}
		response = new WebResourceResponse(mimeType, encoding, inputStream);
		response.setResponseHeaders(headers);
		//response.setStatusCodeAndReasonPhrase(200,"OK");
		return response;
	}

	@Nullable
	private Map<String, String> getHeaders(String localFile) {
		return myUtils.getHeadersFromMeta(localFile);
	}

	String getMimeType(String file) {
		String mimeType = myUtils.getMimeTypeFromMeta(file);
		if (mimeType == null) {
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
}
