package com.sk.revisit2;

import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import com.sk.revisit2.log.FileLogger;
import com.sk.revisit2.utils.EncodingUtils;
import com.sk.revisit2.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ResourceManager {
	private static final String TAG = ResourceManager.class.getSimpleName();
	private final String rootPath;
	private final MetadataManager metadataManager;
	private final EncodingUtils encodingUtils;
	private final FileLogger logger;
	private final FileUtils fileUtils;

	public ResourceManager(String rootPath, FileUtils fileUtils, MetadataManager metadataManager,
	                       EncodingUtils encodingUtils, FileLogger logger) {
		if (!rootPath.endsWith(File.separator)) {
			rootPath += File.separator;
		}
		this.rootPath = rootPath;
		this.fileUtils = fileUtils;
		this.metadataManager = metadataManager;
		this.encodingUtils = encodingUtils;
		this.logger = logger;
	}

	public String buildLocalPath(Uri uri) {
		return buildLocalPath2(uri);
	}

	private String buildBasePath(Uri uri) {
		String authority = uri.getAuthority();
		if (authority == null || authority.isEmpty()) {
			authority = "no_authority";
		}
		String basePath = rootPath + authority + File.separator;
		fileUtils.prepareDirectory(new File(basePath));
		return basePath;
	}

	private String buildLocalPath1(Uri uri) {
		return buildBasePath(uri) + encodingUtils.encodeToB64(uri.toString());
	}

	private String buildLocalPath2(Uri uri) {
		String hashed = encodingUtils.hash(uri.toString());
		if (hashed == null) {
			return buildLocalPath1(uri);
		}
		return buildBasePath(uri) + hashed;
	}

	public WebResourceResponse getResourceResponse(WebResourceRequest request) {
		Uri uri = request.getUrl();
		String url = uri.toString();
		String filePath = buildLocalPath(uri);
		File contentFile = new File(filePath);

		if (!contentFile.exists() || !contentFile.isFile() || contentFile.length() == 0) {
			return null;
		}

		String mimeType = metadataManager.getMimeTypeFromMeta(filePath);
		String encoding = metadataManager.getEncodingFromMeta(filePath);
		Map<String, String> headers = metadataManager.getHeaders(filePath);

		if (mimeType == null || mimeType.isEmpty()) {
			mimeType = metadataManager.guessMimeFromUrl(uri);
			logger.log(TAG + ": MIME type not found in metadata, guessed: " + mimeType + " for " + url);
			if (mimeType == null) {
				mimeType = "application/octet-stream";
			}
		}

		if (encoding == null || encoding.isEmpty()) {
			if (mimeType.startsWith("text/")) {
				encoding = encodingUtils.guessEncodingFromFile(contentFile);
				logger.log(TAG + ": Encoding not found in metadata, guessed: " + encoding + " for " + url);
			}
			if (encoding == null) {
				encoding = StandardCharsets.UTF_8.name();
				logger.log(TAG + ": Using default encoding UTF-8 for text mime: " + mimeType);
			} else if (!mimeType.startsWith("text/")) {
				encoding = null;
			}
		}

		InputStream inputStream;
		try {
			inputStream = new FileInputStream(contentFile);
		} catch (FileNotFoundException e) {
			logger.log(TAG + ": ERROR: Resource file disappeared unexpectedly: " + filePath + " - " + e.getMessage());
			metadataManager.deleteFileAndMetadata(contentFile);
			return null;
		} catch (SecurityException se) {
			logger.log(TAG + ": ERROR: Permission denied reading resource file: " + filePath + " - " + se.getMessage());
			return null;
		}

		logger.log(TAG + ": Serving resource: " + url + " from " + filePath);
		return new WebResourceResponse(mimeType, encoding, inputStream);
	}
} 