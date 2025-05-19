package com.sk.revisit2.managers;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sk.revisit2.converters.JsonAndMap;
import com.sk.revisit2.utils.FileUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;

public class UrlMetaManager {
	private static final String TAG = UrlMetaManager.class.getSimpleName();
	private static final String META_MIME_SUFFIX = ".mime";
	private static final String META_HEADERS_SUFFIX = ".head";
	private static final String META_ENCODING_SUFFIX = ".enc";

	public static void saveMimeType(String baseFilePath, String mimeType) {
		if (mimeType != null && !mimeType.isEmpty()) {
			String actualMime = mimeType.split(";")[0].trim();
			if (!actualMime.isEmpty()) {
				FileUtils.writeStringToFile(baseFilePath + META_MIME_SUFFIX, actualMime);
			} else {
				Log(TAG + ": Extracted empty mime type from: " + mimeType);
			}
		} else {
			Log(TAG + ": No Content-Type header received for: " + baseFilePath);
			String guessedMime = guessMimeFromUri(Uri.parse(baseFilePath));
			if (guessedMime != null) {
				Log(TAG + ": Guessed MIME type: " + guessedMime + " for " + baseFilePath);
				FileUtils.writeStringToFile(baseFilePath + META_MIME_SUFFIX, guessedMime);
			}
		}
	}

	private static void Log(String msg) {
		Log.i(TAG, msg);
	}

	public static void saveEncoding(String baseFilePath, String encoding) {
		if (encoding != null && !encoding.isEmpty()) {
			FileUtils.writeStringToFile(baseFilePath + META_ENCODING_SUFFIX, encoding);
		}
	}

	public static void saveHeaders(String baseFilePath, Map<String, List<String>> headersMap) {
		if (headersMap != null && !headersMap.isEmpty()) {
			JSONObject jsonHeaders = JsonAndMap.headersToJson(headersMap);
			if (jsonHeaders != null) {
				FileUtils.writeStringToFile(baseFilePath + META_HEADERS_SUFFIX, jsonHeaders.toString());
			}
		}
	}

	public static String getMimeTypeFromMeta(String baseFilePath) {
		return FileUtils.readStringFromFile(baseFilePath + META_MIME_SUFFIX);
	}

	public static String getEncodingFromMeta(String baseFilePath) {
		return FileUtils.readStringFromFile(baseFilePath + META_ENCODING_SUFFIX);
	}

	public static Map<String, String> getHeadersFromMeta(String baseFilePath) {
		String filePath = baseFilePath + META_HEADERS_SUFFIX;
		String jsonString = FileUtils.readStringFromFile(filePath);
		if (jsonString != null && !jsonString.isEmpty()) {
			return JsonAndMap.jsonToHeaders(jsonString);
		}
		return null;
	}

	public static void deleteMetadata(String baseFilePath) {
		FileUtils.deleteFile(baseFilePath + META_MIME_SUFFIX);
		FileUtils.deleteFile(baseFilePath + META_HEADERS_SUFFIX);
		FileUtils.deleteFile(baseFilePath + META_ENCODING_SUFFIX);
		Log("Deleted metadata for: " + baseFilePath);
	}

	@Nullable
	public static String guessMimeFromUri(@NonNull Uri url) {
		String fileExtension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(url.toString());
		if (fileExtension != null) {
			return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
		}
		return null;
	}

	public static void deleteFileAndMetadata(File contentFile) {
		if (contentFile == null) {
			return;
		}
		String basePath = contentFile.getAbsolutePath();
		FileUtils.deleteFile(contentFile);
		deleteMetadata(basePath);
		Log(TAG + ": Deleted file and metadata for: " + basePath);
	}
}