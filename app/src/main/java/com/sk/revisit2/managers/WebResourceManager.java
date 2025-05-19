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
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Executors;
public class WebResourceManager {

	String TAG = this.getClass().getSimpleName();
	MyUtils myUtils;
	WebLogger loggerManager;

	public WebResourceManager(MyUtils myUtils) {
		this.myUtils = myUtils;
		this.loggerManager = new WebLogger(myUtils.getRootPath(), Executors.newSingleThreadExecutor());
	}

	public WebResourceResponse getResponse(WebView webView, @NonNull WebResourceRequest request) {
		Uri uri = request.getUrl();
		String url = uri.toString();

		if (!URLUtil.isNetworkUrl(url)) {
			loggerManager.Log( "Non network url: " + url);
			return null;
		}

		if (request.isRedirect()) {
			loggerManager.Log( "Redirect: " + url);
			//return null;
		}

		if (!request.getMethod().equals("GET")) {
			loggerManager.Log( "Not GET Method: " + request.getMethod() + ':' + url);
			return null;
		}

		String localFilePath = myUtils.buildLocalPath(uri);
		loggerManager.logUrl(uri + " -> " + localFilePath);
		File localFile = new File(localFilePath);

		if (localFile.exists()) {
			if (MyUtils.shouldUpdate && MyUtils.isNetWorkAvailable) {
				myUtils.download(request);
				loggerManager.Log( "updating local resource: "+url);
			}
			loggerManager.Log( "loading from local: "+url);
			return loadFromLocal(localFile);
		} else {
			loggerManager.Log( "need to download: "+url);
			if (MyUtils.isNetWorkAvailable) {
				loggerManager.Log("Downloading: "+url);
				download(request);
				return loadFromLocal(localFile);
			} else {
				loggerManager.logRequest(String.valueOf(request));
				return new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream("no off file".getBytes()));
			}
		}
	}

	private void download(WebResourceRequest request){
		myUtils.download(request);
	}

	@NonNull
	private WebResourceResponse loadFromLocal(@NonNull File file) {
		WebResourceResponse response;
		String localFile = file.getAbsolutePath();
		String mimeType = getMimeType(localFile);
		String encoding = getEncoding(localFile);
		//Map<String, String> headers = getHeaders(localFile);
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
		} catch (Exception e) {
			inputStream = new ByteArrayInputStream(e.toString().getBytes());
			loggerManager.Log(e.toString());
		}
		response = new WebResourceResponse(mimeType, encoding, inputStream);
		//response.setResponseHeaders(headers);
		//response.setStatusCodeAndReasonPhrase(200,"OK");
		loggerManager.logLocalPath(localFile);
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

	String getEncoding(String file) {
		String encoding = myUtils.getEncodingFromMeta(file);
		if (encoding == null) {
			encoding = "UTF-8";
		}
		return encoding;
	}
}
