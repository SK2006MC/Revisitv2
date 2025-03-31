package com.sk.revisit2.net;

import android.net.Uri;
import android.webkit.WebResourceRequest;

import com.sk.revisit2.utils.FileSystemManager;
import com.sk.revisit2.log.LoggerHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class NetworkManager {
	private static final int CONNECT_TIMEOUT = 15000;
	private static final int READ_TIMEOUT = 15000;

	private final FileSystemManager fileManager;
	private final LoggerHelper logger;
	private String TAG = NetworkManager.class.getSimpleName();

	public NetworkManager(FileSystemManager fileManager, LoggerHelper logger) {
		this.fileManager = fileManager;
		this.logger = logger;
	}

	public void download(WebResourceRequest request) {
		Uri uri = request.getUrl();
		String url = uri.toString();
		String filePath = fileManager.buildLocalPath(uri);

		if (filePath == null) {
			logger.log("NetworkManager Could not build local path for: " + url);
			return;
		}

		HttpURLConnection connection = null;
		try {
			URL urlObj = new URL(url);
			connection = (HttpURLConnection) urlObj.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(CONNECT_TIMEOUT);
			connection.setReadTimeout(READ_TIMEOUT);

			// Set request headers
			if (request.getRequestHeaders() != null) {
				for (Map.Entry<String, String> header : request.getRequestHeaders().entrySet()) {
					connection.setRequestProperty(header.getKey(), header.getValue());
				}
			}

			connection.connect();

			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				try (InputStream inputStream = connection.getInputStream();
				     FileOutputStream outputStream = new FileOutputStream(filePath)) {

					byte[] buffer = new byte[8192];
					int bytesRead;
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}

					// Save metadata
					saveResponseMetadata(filePath, connection);
					logger.log("NetworkManager Downloaded: " + url);
				}
			} else {
				logger.log("NetworkManager Download failed for " + url +
						", response code: " + connection.getResponseCode());
				fileManager.deleteFile(filePath);
			}
		} catch (Exception e) {
			logger.log("NetworkManager Error downloading " + url + ": " + e.getMessage());
			fileManager.deleteFile(filePath);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private void saveResponseMetadata(String filePath, HttpURLConnection connection) {
		String mimeType = connection.getContentType();
		String encoding = connection.getContentEncoding();

		if (mimeType != null) {
			fileManager.writeStringToFile(filePath + ".mime", mimeType);
		}
		if (encoding != null) {
			fileManager.writeStringToFile(filePath + ".enc", encoding);
		}
	}

	public boolean isNetworkAvailable() {
		try {
			URL url = new URL("https://www.google.com");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(3000);
			connection.connect();
			return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
		} catch (IOException e) {
			return false;
		}
	}
}