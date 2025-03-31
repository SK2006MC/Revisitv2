package com.sk.revisit2.utils;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import com.sk.revisit2.log.LoggerHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceManager {
	private final FileSystemManager fileManager;
	private final LoggerHelper logger;

	public ResourceManager(FileSystemManager fileManager, LoggerHelper logger) {
		this.fileManager = fileManager;
		this.logger = logger;
	}

	public WebResourceResponse getLocalResponse(WebResourceRequest request) {
		Uri uri = request.getUrl();
		String filePath = buildLocalPath(uri);
		File contentFile = new File(filePath);

		if (!fileManager.fileExists(filePath)) {
			return null;
		}

		try {
			String mimeType = fileManager.readStringFromFile(filePath + ".mime");
			String encoding = fileManager.readStringFromFile(filePath + ".enc");
			String headersJson = fileManager.readStringFromFile(filePath + ".head");

			Map<String, String> responseHeaders = new HashMap<>();
			if (headersJson != null) {
				responseHeaders = Utils.jsonToHeaders(headersJson);
			}

			InputStream dataStream = new FileInputStream(contentFile);
			return new WebResourceResponse(
					mimeType,
					encoding,
					HttpURLConnection.HTTP_OK,
					"OK",
					responseHeaders,
					dataStream
			);
		} catch (Exception e) {
			logger.log("ResourceManager Error loading local response: " + e.getMessage());
			return null;
		}
	}

	public String buildLocalPath(Uri uri) {
		String authority = uri.getAuthority() != null ? uri.getAuthority() : "no_authority";
		String basePath =   authority + File.separator;
		fileManager.prepareDirectory(new File(basePath));
		return basePath + Utils.hash(uri.toString());
	}

	public void saveMetadata(String basePath, String mimeType, String encoding,
	                         Map<String, List<String>> headers) {
		if (mimeType != null) {
			fileManager.writeStringToFile(basePath + ".mime", mimeType);
		}
		if (encoding != null) {
			fileManager.writeStringToFile(basePath + ".enc", encoding);
		}
		if (headers != null) {
			String headersJson = Utils.responseHeadersToJson(headers).toString();
			fileManager.writeStringToFile(basePath + ".head", headersJson);
		}
	}

	public void deleteLocalResource(WebResourceRequest request) {
		String filePath = buildLocalPath(request.getUrl());
		fileManager.deleteFile(filePath);
		fileManager.deleteFile(filePath + ".mime");
		fileManager.deleteFile(filePath + ".enc");
		fileManager.deleteFile(filePath + ".head");
	}

	public boolean hasLocalResource(WebResourceRequest request) {
		String filePath = buildLocalPath(request.getUrl());
		return fileManager.fileExists(filePath);
	}
}