package com.sk.revisit2.webview;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceRequest;

import com.sk.revisit2.MyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WebResourceDownloader2 {
	private static final int DEFAULT_TIMEOUT_SECONDS = 15;
	private final ExecutorService executorService;
	private final MyUtils myUtils;
	private final int writeBuffer;
	private final OkHttpClient client;

	public WebResourceDownloader2(ExecutorService executorService, MyUtils utils, int writeBuffer) {
		this.executorService = Objects.requireNonNull(executorService, "executorService cannot be null");
		this.myUtils = Objects.requireNonNull(utils, "utils cannot be null");
		this.writeBuffer = writeBuffer;
		this.client = new OkHttpClient.Builder()
				.connectTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
				.readTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
				.build();
	}

	public void download(WebResourceRequest request) {
		if (request == null) {
			logError("Request cannot be null", null);
			return;
		}
		executorService.execute(() -> downloadInternal(request));
	}

	private void downloadInternal(WebResourceRequest request) {
		Uri uri = request.getUrl();
		String url = uri.toString();

		String filePath = myUtils.buildLocalPath(uri);
		if (filePath == null) {
			logError("Could not build local path for: " + url, null);
			return;
		}

		File file = myUtils.prepareFile(filePath);
		if (file == null) {
			logError("Failed to prepare file for download: " + url + " at " + filePath, null);
			return;
		}

		if (file.exists() && !MyUtils.shouldUpdate) {
			return;
		}

		Request.Builder requestBuilder = new Request.Builder()
				.url(url)
				.get();

		Map<String, String> headers = request.getRequestHeaders();
		if (headers != null) {
			headers.forEach(requestBuilder::addHeader);
		}

		try (Response response = client.newCall(requestBuilder.build()).execute()) {
			if (!response.isSuccessful()) {
				logError("Failed to download " + url + ", response code: " + response.code() + " " + response.message(), null);
				return;
			}

			ResponseBody body = response.body();
			if (body == null) {
				logError("Response body is null for: " + url, null);
				return;
			}

			try (InputStream inputStream = body.byteStream();
				 FileOutputStream outputStream = new FileOutputStream(file)) {
				
				byte[] buffer = new byte[writeBuffer];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				outputStream.flush();

				saveResponseMetadata(filePath, body, response);
			}
		} catch (Exception e) {
			logError("Error downloading " + url, e);
			// Clean up partial download
			if (file.exists()) {
				file.delete();
			}
		}
	}

	private void saveResponseMetadata(String filePath, ResponseBody body, Response response) {
		String mimeType = body.contentType() != null ? body.contentType().toString() : null;
		String encoding = response.header("Content-Encoding");
		Map<String, List<String>> headersMap = response.headers().toMultimap();

		myUtils.saveMimeType(filePath, mimeType);
		myUtils.saveEncoding(filePath, encoding);
		myUtils.saveHeaders(filePath, headersMap);
	}

	private void logError(String msg, Exception e) {
		Log.e("WebResourceDownloader", msg, e);
	}
}
