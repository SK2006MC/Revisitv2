package com.sk.revisit2.webview;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceRequest;

import com.sk.revisit2.MyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class WebResourceDownloader {
	private final ExecutorService executorService;
	private final MyUtils utils;
	private final int writeBuffer;

	public WebResourceDownloader(ExecutorService executorService, MyUtils utils, int writeBuffer) {
		this.executorService = executorService;
		this.utils = utils;
		this.writeBuffer = writeBuffer;
	}

	public void download(WebResourceRequest request) {
		executorService.execute(() -> downloadInternal(request));
	}

	private void downloadInternal(WebResourceRequest request) {
		Uri uri = request.getUrl();
		String url = uri.toString();
		String filePath = utils.buildLocalPath(uri);
		if (filePath == null) {
			LogError("Could not build local path for: " + url, null);
			return;
		}
		File file = utils.prepareFile(filePath);

		if (file == null) {
			LogError("Failed to prepare file for download: " + url + " at " + filePath, null);
			return;
		}

		if (file.exists() && file.length() > 0 && !MyUtils.shouldUpdate) {
			return;
		}

		HttpURLConnection connection = null;
		InputStream inputStream = null;
		FileOutputStream outputStream = null;

		try {
			URL urlObj = new URL(url);
			connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(15000);
			connection.setReadTimeout(15000);
			if (request.getRequestHeaders() != null) {
				request.getRequestHeaders().forEach(connection::setRequestProperty);
			}
			connection.connect();

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				inputStream = connection.getInputStream();
				outputStream = new FileOutputStream(file);

				byte[] buffer = new byte[writeBuffer];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				outputStream.flush();

				String mimeType = connection.getContentType();
				String encoding = connection.getContentEncoding();
				Map<String, List<String>> responseHeaders = connection.getHeaderFields();

				utils.saveMimeType(filePath, mimeType);
				utils.saveEncoding(filePath, encoding);
				utils.saveHeaders(filePath, responseHeaders);

			} else {
				LogError("Failed to download " + url + ", response code: " + responseCode + " " + connection.getResponseMessage(), null);
			}

		} catch (Exception e) {
			LogError("Error downloading " + url, e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) { /* ignore */ }
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) { /* ignore */ }
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	void LogError(String msg, Exception e) {
		Log.e("WebResourceDownloader", msg, e);
	}
} 