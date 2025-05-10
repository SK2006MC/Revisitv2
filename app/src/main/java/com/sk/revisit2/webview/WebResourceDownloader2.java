package com.sk.revisit2.webview;

import android.net.Uri;
import android.webkit.WebResourceRequest;

import com.sk.revisit2.MyUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WebResourceDownloader2 {
	private final ExecutorService executorService;
	private final MyUtils myUtils;
	private final int WRITE_BUFFER;
	private final OkHttpClient client;

	public WebResourceDownloader2(ExecutorService executorService, MyUtils utils, int writeBuffer) {
		this.executorService = executorService;
		this.myUtils = utils;
		this.WRITE_BUFFER = writeBuffer;
		this.client = new OkHttpClient.Builder()
				.connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
				.readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
				.build();
	}

	public void download(WebResourceRequest request) {
		executorService.execute(() -> downloadInternal(request));
	}

	private void downloadInternal(WebResourceRequest request) {
		Uri uri = request.getUrl();
		String url = uri.toString();

		String filePath = myUtils.buildLocalPath(uri);
		if (filePath == null) {
			LogError("Could not build local path for: " + url, null);
			return;
		}

		File file = myUtils.prepareFile(filePath);

		if (file == null) {
			LogError("Failed to prepare file for download: " + url + " at " + filePath, null);
			return;
		}

		if (file.exists() && !MyUtils.shouldUpdate) {
			return;
		}

		Request.Builder requestBuilder = new Request.Builder()
				.url(url)
				.get();

		if (request.getRequestHeaders() != null) {
			request.getRequestHeaders().forEach(requestBuilder::addHeader);
		}

		Request okHttpRequest = requestBuilder.build();

		Call call = client.newCall(okHttpRequest);
		try (Response response = call.execute()) {
			if (response.isSuccessful()) {
				ResponseBody body = response.body();
				if (body != null) {
					try (InputStream inputStream = body.byteStream();
					     FileOutputStream outputStream = new FileOutputStream(file)) {

						byte[] buffer = new byte[WRITE_BUFFER];
						int bytesRead;
						while ((bytesRead = inputStream.read(buffer)) != -1) {
							outputStream.write(buffer, 0, bytesRead);
						}
						outputStream.flush();

						String mimeType = body.contentType() != null ? Objects.requireNonNull(body.contentType()).toString() : null;
						String encoding = response.header("Content-Encoding");
						Headers responseHeaders = response.headers();
						Map<String, List<String>> headersMap = responseHeaders.toMultimap();

						myUtils.saveMimeType(filePath, mimeType);
						myUtils.saveEncoding(filePath, encoding);
						myUtils.saveHeaders(filePath, headersMap);
					}
				}
			} else {
				LogError("Failed to download " + url + ", response code: " + response.code() + " " + response.message(), null);
			}
		} catch (Exception e) {
			LogError("Error downloading " + url, e);
		}
	}

	void LogError(String msg, Exception e) {
		Log.e("WebResourceDownloader", msg, e);
	}
}
