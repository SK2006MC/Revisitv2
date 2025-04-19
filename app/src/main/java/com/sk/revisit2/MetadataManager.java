package com.sk.revisit2;

import android.net.Uri;
import android.util.Log;

import com.sk.revisit2.log.FileLogger;
import com.sk.revisit2.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MetadataManager {
	private static final String TAG = MetadataManager.class.getSimpleName();
	private static final String META_MIME_SUFFIX = ".mime";
	private static final String META_HEADERS_SUFFIX = ".head";
	private static final String META_ENCODING_SUFFIX = ".enc";

	private final FileLogger logger;
	private final FileUtils fileManager;

	public MetadataManager(FileLogger logger, FileUtils fileManager) {
		this.logger = logger;
		this.fileManager = fileManager;
	}

	public void saveMimeType(String baseFilePath, String mimeType) {
		if (mimeType != null && !mimeType.isEmpty()) {
			String actualMime = mimeType.split(";")[0].trim();
			if (!actualMime.isEmpty()) {
				fileManager.writeStringToFile(baseFilePath + META_MIME_SUFFIX, actualMime);
			} else {
				logger.log(TAG + ": Extracted empty mime type from: " + mimeType);
			}
		} else {
			logger.log(TAG + ": No Content-Type header received for: " + baseFilePath);
			String guessedMime = guessMimeFromUrl(Uri.parse(baseFilePath));
			if (guessedMime != null) {
				logger.log(TAG + ": Guessed MIME type: " + guessedMime + " for " + baseFilePath);
				fileManager.writeStringToFile(baseFilePath + META_MIME_SUFFIX, guessedMime);
			}
		}
	}

	public void saveEncoding(String baseFilePath, String encoding) {
		if (encoding != null && !encoding.isEmpty()) {
			fileManager.writeStringToFile(baseFilePath + META_ENCODING_SUFFIX, encoding);
		}
	}

	public void saveHeaders(String baseFilePath, Map<String, List<String>> headersMap) {
		if (headersMap != null && !headersMap.isEmpty()) {
			JSONObject jsonHeaders = responseHeadersToJson(headersMap);
			if (jsonHeaders != null) {
				fileManager.writeStringToFile(baseFilePath + META_HEADERS_SUFFIX, jsonHeaders.toString());
			}
		}
	}

	public String getMimeTypeFromMeta(String baseFilePath) {
		return fileManager.readStringFromFile(baseFilePath + META_MIME_SUFFIX);
	}

	public String getEncodingFromMeta(String baseFilePath) {
		return fileManager.readStringFromFile(baseFilePath + META_ENCODING_SUFFIX);
	}

	public Map<String, String> getHeaders(String baseFilePath) {
		String filePath = baseFilePath + META_HEADERS_SUFFIX;
		String jsonString = fileManager.readStringFromFile(filePath);
		if (jsonString != null && !jsonString.isEmpty()) {
			return jsonToHeaders(jsonString);
		}
		return null;
	}

	public void deleteMetadata(String baseFilePath) {
		fileManager.deleteFile(baseFilePath + META_MIME_SUFFIX);
		fileManager.deleteFile(baseFilePath + META_HEADERS_SUFFIX);
		fileManager.deleteFile(baseFilePath + META_ENCODING_SUFFIX);
		logger.log(TAG + ": Deleted metadata for: " + baseFilePath);
	}

	public String guessMimeFromUrl(Uri url) {
		String fileExtension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(url.toString());
		if (fileExtension != null) {
			return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
		}
		return null;
	}

	public void deleteFileAndMetadata(File contentFile) {
		if (contentFile == null) {
			return;
		}
		String basePath = contentFile.getAbsolutePath();
		fileManager.deleteFile(contentFile);
		deleteMetadata(basePath);
		logger.log(TAG + ": Deleted file and metadata for: " + basePath);
	}

	private JSONObject responseHeadersToJson(Map<String, List<String>> headersMap) {
		JSONObject jsonHeaders = new JSONObject();
		if (headersMap != null) {
			for (Map.Entry<String, List<String>> entry : headersMap.entrySet()) {
				String key = entry.getKey();
				List<String> values = entry.getValue();
				if (key != null && values != null && !values.isEmpty()) {
					try {
						jsonHeaders.put(key, values.get(0));
					} catch (JSONException e) {
						Log.e(TAG, "JSONException while converting response headers to JSON", e);
						return null;
					}
				}
			}
		}
		return jsonHeaders;
	}

	private Map<String, String> jsonToHeaders(String jsonString) {
		Map<String, String> headers = new java.util.HashMap<>();
		if (jsonString == null || jsonString.isEmpty()) {
			return headers;
		}
		try {
			JSONObject jsonHeaders = new JSONObject(jsonString);
			java.util.Iterator<String> keys = jsonHeaders.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				Object valueObj = jsonHeaders.opt(key);
				if (valueObj != null && valueObj != JSONObject.NULL) {
					headers.put(key, valueObj.toString());
				} else {
					headers.put(key, null);
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "JSONException while parsing headers from JSON: " + jsonString, e);
		}
		return headers;
	}
} 