package com.sk.revisit2.managers;

import android.net.Uri;
import android.webkit.URLUtil;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sk.revisit2.MyUtils;
import com.sk.revisit2.log.WebLogger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;

public class WebResourceManager {

	private static final String DEFAULT_ENCODING = "UTF-8";
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
	private static final String TEXT_HTML = "text/html";

	String TAG = this.getClass().getSimpleName();
	MyUtils myUtils;
	WebLogger webLogger;

	public WebResourceManager(MyUtils myUtils) {
		this.myUtils = myUtils;
		this.webLogger = new WebLogger(myUtils.getRootPath(), Executors.newSingleThreadExecutor());
	}

	public WebResourceResponse getResponse(WebView webView, @NonNull WebResourceRequest request) {
		try {
			Uri uri = request.getUrl();
			String url = uri.toString();

			if (!request.getMethod().equals("GET")) {
				webLogger.Log("Not GET Method: " + request.getMethod() + ':' + url);
				return null;
			}

			// Check if URL should be ignored
			if (shouldIgnoreUrl(url)) {
				webLogger.Log("Ignoring non-resource URL: " + url);
				return null;
			}

			if (!URLUtil.isNetworkUrl(url)) {
				webLogger.Log("Non network url: " + url);
				return null;
			}

			if (request.isRedirect()) {
				webLogger.Log("Redirect: " + url);
				//return null;
			}

			String localFilePath = myUtils.buildLocalPath(uri);
			webLogger.logUrl(uri + " -> " + localFilePath);
			File localFile = new File(localFilePath);

			if (localFile.exists()) {
				if (MyUtils.shouldUpdate && MyUtils.isNetWorkAvailable) {
					myUtils.download(request);
					webLogger.Log("updating local resource: " + url);
				}
				webLogger.Log("loading from local: " + url);
				return loadFromLocal(localFile);
			} else {
				webLogger.Log("need to download: " + url);
				if (MyUtils.isNetWorkAvailable) {
					webLogger.Log("Downloading: " + url);
					download(request);
					return loadFromLocal(localFile);
				} else {
					webLogger.logRequest(url);
					return new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream("No offline file available.Connect to internet once.".getBytes()));
				}
			}
		} catch (Exception e) {
			webLogger.Log("Error processing request: " + e.getMessage());
			return new WebResourceResponse(TEXT_HTML, DEFAULT_ENCODING, 
				new ByteArrayInputStream("Error processing request".getBytes()));
		}
	}

	private void download(WebResourceRequest request) {
		myUtils.download(request);
	}

	@NonNull
	private WebResourceResponse loadFromLocal(@NonNull File file) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			String mimeType = getMimeType(file.getAbsolutePath());
			String encoding = getEncoding(file.getAbsolutePath());
			Map<String, String> headers = getHeaders(file.getAbsolutePath());
			
			WebResourceResponse response = new WebResourceResponse(mimeType, encoding, inputStream);
			response.setResponseHeaders(headers);
			webLogger.logLocalPath(file.getAbsolutePath());
			return response;
		} catch (Exception e) {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ignored) {}
			}
			webLogger.Log(e.toString());
			return new WebResourceResponse(TEXT_HTML, DEFAULT_ENCODING, 
				new ByteArrayInputStream(e.toString().getBytes()));
		}
	}

	@Nullable
	private Map<String, String> getHeaders(String localFile) {
		Map<String, String> headers = myUtils.getHeadersFromMeta(localFile);
		return headers != null ? headers : Collections.emptyMap();
	}

	String getMimeType(String file) {
		String mimeType = myUtils.getMimeTypeFromMeta(file);
		if (mimeType == null) {
			mimeType = "application/oclet-stream";
		}
		return mimeType;
	}

	String getEncoding(String file) {
		String encoding = myUtils.getEncodingFromMeta(file);
		if (encoding == null) {
			encoding = "UTF-8";
		}
		return encoding;
	}

	private boolean shouldIgnoreUrl(String url) {
		// Ignore common non-resource URLs
		String lowerUrl = url.toLowerCase();
		
		// Ignore Google search suggestions
		if (lowerUrl.contains("suggest?") || lowerUrl.contains("complete/search")) {
			return true;
		}
		
		// Ignore common analytics and tracking URLs
		if (lowerUrl.contains("analytics") || lowerUrl.contains("tracking") || 
			lowerUrl.contains("pixel") || lowerUrl.contains("beacon")) {
			return true;
		}
		
		// Ignore common social media widgets
		if (lowerUrl.contains("facebook.com/plugins") || 
			lowerUrl.contains("twitter.com/intent") ||
			lowerUrl.contains("linkedin.com/share")) {
			return true;
		}
		
		// Ignore common ad-related URLs
		if (lowerUrl.contains("ads.") || lowerUrl.contains("advertising") || 
			lowerUrl.contains("doubleclick.net")) {
			return true;
		}
		
		// Ignore common API endpoints that don't need caching
		if (lowerUrl.contains("/api/") || lowerUrl.contains("/service/") || 
			lowerUrl.contains("/rest/")) {
			return true;
		}
		
		// Ignore URLs with specific query parameters that indicate non-resource content
		if (lowerUrl.contains("?q=") || lowerUrl.contains("&q=") || 
			lowerUrl.contains("?search=") || lowerUrl.contains("&search=")) {
			return true;
		}
		return false;
	}
}
