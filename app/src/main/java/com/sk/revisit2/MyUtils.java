package com.sk.revisit2;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sk.revisit2.log.LoggerHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyUtils {

	private static final String TAG = MyUtils.class.getSimpleName();
	private static final int MAX_THREADS = 4;
	// --- Metadata File Suffixes ---
	private static final String META_MIME_SUFFIX = ".mime";
	private static final String META_HEADERS_SUFFIX = ".head";
	private static final String META_ENCODING_SUFFIX = ".enc"; // Added for explicit encoding saving
	public static boolean isNetWorkAvailable = false, shouldUpdate = false;
	private final ExecutorService executorService;
	private final LoggerHelper logger, urlLogger, reqLogger;
	private final String rootPath;
	private final Context context;
	final int WRITE_BUFFER = 8 * 1024;
	private final Downloader downloader;

	public MyUtils(Context context, String rootPath) {
		// Ensure rootPath ends with a separator
		if (!rootPath.endsWith(File.separator)) {
			rootPath += File.separator;
		}
		this.context = context;
		this.rootPath = rootPath;
		executorService = Executors.newFixedThreadPool(MAX_THREADS);
		// Ensure log directories exist
		prepareDirectory(new File(rootPath));
		urlLogger = new LoggerHelper(rootPath + "urls.txt", executorService);
		logger = new LoggerHelper(rootPath + "log.txt", executorService);
		reqLogger = new LoggerHelper(rootPath + "req.txt", executorService);
		this.downloader = new Downloader(executorService, this, WRITE_BUFFER);
		Log("MyUtils initialized. Root path: " + rootPath); // Use instance logger
	}

	/**
	 * Converts request headers (Map<String, String>) to a JSON string.
	 *
	 * @param headers Request headers map.
	 * @return JSON string representation of headers, or empty JSON object "{}" on error/empty.
	 */
	public static String headersToJson(Map<String, String> headers) {
		JSONObject jsonHeaders = new JSONObject();
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				try {
					// Ensure key is not null, value can be null but JSON usually expects strings
					if (entry.getKey() != null) {
						jsonHeaders.put(entry.getKey(), entry.getValue() != null ? entry.getValue() : JSONObject.NULL);
					}
				} catch (JSONException e) {
					// Use android.util.Log here as this is a static method without instance logger access
					android.util.Log.e(TAG, "JSONException while converting request headers to JSON", e);
				}
			}
		}
		return jsonHeaders.toString();
	}

	/**
	 * Converts response headers (Map<String, List<String>>) from HttpURLConnection
	 * to a JSON object, typically storing only the first value for each header key.
	 * Ignores the null key often present which contains the status line.
	 *
	 * @param headersMap Response headers map from HttpURLConnection.getHeaderFields().
	 * @return JSONObject representation or null on error.
	 */
	public static JSONObject responseHeadersToJson(Map<String, List<String>> headersMap) {
		JSONObject jsonHeaders = new JSONObject();
		if (headersMap != null) {
			for (Map.Entry<String, List<String>> entry : headersMap.entrySet()) {
				String key = entry.getKey();
				List<String> values = entry.getValue();
				// Ignore the null key (status line) and empty value lists
				if (key != null && values != null && !values.isEmpty()) {
					try {
						// Store the first value, common practice for simple caching needs
						jsonHeaders.put(key, values.get(0));
					} catch (JSONException e) {
						android.util.Log.e(TAG, "JSONException while converting response headers to JSON", e);
						return null; // Indicate failure
					}
				}
			}
		}
		return jsonHeaders;
	}

	/**
	 * Convert JSON string back to a Map<String, String> of headers.
	 *
	 * @param jsonString JSON string representing headers.
	 * @return Map of headers, or an empty map if input is null/invalid.
	 */
	public static Map<String, String> jsonToHeaders(String jsonString) {
		Map<String, String> headers = new HashMap<>();
		if (jsonString == null || jsonString.isEmpty()) {
			return headers; // Return empty map for null/empty input
		}
		try {
			JSONObject jsonHeaders = new JSONObject(jsonString);
			Iterator<String> keys = jsonHeaders.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				// Get value, handle potential nulls stored as JSONObject.NULL
				Object valueObj = jsonHeaders.opt(key); // Use opt to avoid exception if key disappears
				if (valueObj != null && valueObj != JSONObject.NULL) {
					headers.put(key, valueObj.toString()); // Convert value to string
				} else {
					headers.put(key, null); // Store null if it was JSONObject.NULL or missing
				}
			}
		} catch (JSONException e) {
			// Use android.util.Log here as this is a static method
			android.util.Log.e(TAG, "JSONException while parsing headers from JSON: " + jsonString, e);
			// Return potentially partially filled map or empty map depending on desired robustness
		}
		return headers;
	}

	// Helper to ensure a directory exists
	public void prepareDirectory(File dir) {
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				Log.e(TAG, "Failed to create directory: " + dir.getAbsolutePath()); // Use static Log here as logger might not be ready
				return;
			}
		}
		dir.isDirectory();
	}

	public void logUrl(String url) {
		urlLogger.log(url);
	}

	// Changed Log to use instance logger
	public void Log(String msg) {
		logger.log(TAG + ": " + msg);
	}

	// Added Log method overload for easier logging
	public void Log(String tag, String msg) {
		logger.log(tag + ": " + msg);
	}

	// Added Error logging using instance logger
	public void LogError(String msg, Throwable e) {
		logger.log(TAG + ": ERROR: " + msg + (e != null ? " - " + e.getMessage() : ""));
		// Optionally log stack trace too, depending on LoggerHelper capability or verbosity needs
		// if (e != null) logger.log(android.util.Log.getStackTraceString(e));
	}

	public void logReq(WebResourceRequest req) {
		reqLogger.log(req.getUrl().toString() + "\t|\t" + headersToJson(req.getRequestHeaders())); // Pass headers map directly
	}

	public String encodeToB64(@NonNull String url) {
		return Base64.encodeToString(url.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
	}

	public String hash(String url) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashBytes = digest.digest(url.getBytes(StandardCharsets.UTF_8));

			StringBuilder hexString = new StringBuilder(2 * hashBytes.length);
			for (byte hashByte : hashBytes) {
				String hex = Integer.toHexString(0xff & hashByte);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			LogError("SHA-256 algorithm not available", e); // Use instance logger
			// Fallback or rethrow? Returning null might hide issues. Maybe use Base64 as fallback?
			// For now, return Base64 encoding as a less ideal alternative path name
			return encodeToB64(url);
		}
	}

	public File prepareFile(String path) {
		File file = new File(path);
		try {
			// Check if it exists and is a file (not a directory)
			if (file.exists()) {
				if (file.isFile()) {
					return file;
				} else {
					LogError("Path exists but is a directory: " + path, null);
					return null; // Path exists but is not a file
				}
			}


			File parent = file.getParentFile(); // Use getParentFile()
			if (parent != null && !parent.exists()) {
				if (!parent.mkdirs()) {
					LogError("Failed to create parent directories for: " + parent.getAbsolutePath(), null);
					return null;
				}
			}

			if (file.createNewFile()) {
				Log(TAG, "Created file: " + file.getAbsolutePath());
				return file;
			} else {
				// Check again if it exists now (maybe race condition?)
				if (file.exists() && file.isFile()) {
					return file;
				}
				LogError("Failed to create file: " + file.getAbsolutePath(), null);
				return null;
			}
		} catch (Exception e) {
			LogError("Error preparing file: " + path, e);
			return null;
		}
	}

	public String buildLocalPath(Uri uri) {
		return buildLocalPath2(uri); // Use hashing by default
	}

	String buildBasePath(Uri uri) {
		String authority = uri.getAuthority();
		if (authority == null || authority.isEmpty()) {
			authority = "no_authority"; // Handle cases like file:/// URIs
		}
		// Ensure the base directory exists when creating the path string
		String basePath = rootPath + authority + File.separator;
		prepareDirectory(new File(basePath)); // Ensure base dir exists
		return basePath;
	}

	// --- Metadata Saving Methods ---

	String buildLocalPath1(Uri uri) {
		return buildBasePath(uri) + encodeToB64(uri.toString());
	}

	String buildLocalPath2(Uri uri) {
		String hashed = hash(uri.toString());
		if (hashed == null) {
			// Fallback if hashing failed
			return buildLocalPath1(uri);
		}
		return buildBasePath(uri) + hashed;
	}

	public void download(WebResourceRequest request) {
		downloader.download(request);
	}

	public void saveMimeType(String baseFilePath, String mimeType) {
		if (mimeType != null && !mimeType.isEmpty()) {
			// Often includes charset, e.g., "text/html; charset=utf-8"
			// Extract only the MIME type part
			String actualMime = mimeType.split(";")[0].trim();
			if (!actualMime.isEmpty()) {
				writeStringToFile(baseFilePath + META_MIME_SUFFIX, actualMime);
			} else {
				Log(TAG, "Extracted empty mime type from: " + mimeType);
			}
		} else {
			Log(TAG, "No Content-Type header received for: " + baseFilePath);
			// Optionally try to guess from URL extension?
			String guessedMime = guessMimeFromUrl(baseFilePath); // Use original URL if possible, fallback to filepath
			if (guessedMime != null) {
				Log(TAG, "Guessed MIME type: " + guessedMime + " for " + baseFilePath);
				writeStringToFile(baseFilePath + META_MIME_SUFFIX, guessedMime);
			}
		}
	}

	// --- Metadata Reading Methods ---

	public void saveEncoding(String baseFilePath, String encoding) {
		if (encoding != null && !encoding.isEmpty()) {
			writeStringToFile(baseFilePath + META_ENCODING_SUFFIX, encoding);
		}
		// If no encoding header, we might rely on guessing later or default to UTF-8
	}

	public void saveHeaders(String baseFilePath, Map<String, List<String>> headersMap) {
		if (headersMap != null && !headersMap.isEmpty()) {
			JSONObject jsonHeaders = responseHeadersToJson(headersMap);
			if (jsonHeaders != null) {
				writeStringToFile(baseFilePath + META_HEADERS_SUFFIX, jsonHeaders.toString());
			}
		}
	}

	// Helper to write a string to a file
	public void writeStringToFile(String filePath, String content) {
		File file = new File(filePath);
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			writer.write(content);
			// Log(TAG,"Wrote metadata file: " + filePath); // Optional: can be verbose
		} catch (IOException e) {
			LogError("Failed to write metadata file: " + filePath, e);
		}
	}

	public String getMimeTypeFromMeta(String baseFilePath) {
		return readStringFromFile(baseFilePath + META_MIME_SUFFIX);
	}

	public String getEncodingFromMeta(String baseFilePath) {
		return readStringFromFile(baseFilePath + META_ENCODING_SUFFIX);
	}

	// Corrected getHeaders
	public Map<String, String> getHeaders(String baseFilePath) {
		String filePath = baseFilePath + META_HEADERS_SUFFIX;
		String jsonString = readStringFromFile(filePath);
		if (jsonString != null && !jsonString.isEmpty()) {
			return jsonToHeaders(jsonString); // Use the existing static method
		}
		return null; // Return null or empty map if no headers found
	}

	// Helper to read the first line (or whole content) from a small file
	public String readStringFromFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists() || !file.isFile() || file.length() == 0) {
			return null;
		}
		// For small metadata files, reading the whole content might be simpler
		StringBuilder content = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line;
			// For small metadata files, reading the whole content might be simpler
			// If expecting only the first line: return reader.readLine();
			// If the file might be multi-line JSON etc.:
			while ((line = reader.readLine()) != null) {
				content.append(line); // Append lines if necessary (e.g., for multi-line JSON, though headers are usually single line)
			}
		} catch (IOException e) {
			LogError("Error reading metadata file: " + filePath, e);
			return null;
		}
		String result = content.toString().trim();
		return result.isEmpty() ? null : result;
	}

	// --- Header Conversion ---

	/**
	 * Reads the entire content of a file into a String.
	 * Suitable for potentially larger files than metadata, but use with caution for very large files.
	 * Uses the provided or guessed encoding.
	 */
	public String getString(File file, String encoding) {
		if (file == null || !file.exists() || !file.isFile()) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		String effectiveEncoding = encoding;
		if (effectiveEncoding == null || effectiveEncoding.isEmpty()) {
			effectiveEncoding = guessEncodingFromFile(file); // Guess if not provided
		}
		if (effectiveEncoding == null) {
			effectiveEncoding = StandardCharsets.UTF_8.name(); // Default fallback
			Log(TAG, "Using default encoding UTF-8 for file: " + file.getPath());
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), effectiveEncoding))) {
			char[] buffer = new char[WRITE_BUFFER]; // Use char buffer for reader
			int charsRead;
			while ((charsRead = reader.read(buffer)) != -1) {
				builder.append(buffer, 0, charsRead);
			}
			return builder.toString();
		} catch (IOException e) {
			LogError("Error reading string from file: " + file.getPath(), e);
			return null; // Indicate failure
		} catch (Exception e) { // Catch potential unsupported encoding etc.
			LogError("Error processing file: " + file.getPath(), e);
			return null;
		}
	}

	// Overload getString to use default encoding guessing
	public String getString(File file) {
		return getString(file, null);
	}

	public String guessEncodingFromFile(File file) {
		// InputStreamReader's default constructor uses the system default encoding,
		// which might not be reliable. Detecting encoding accurately is complex.
		// Common approaches involve checking for BOM (Byte Order Mark) or using libraries.
		// For simplicity, we'll just return UTF-8 as a common default for web content,
		// or rely on the saved ".enc" file if available.
		String savedEncoding = getEncodingFromMeta(file.getAbsolutePath()); // Check if we saved it explicitly
		if (savedEncoding != null) {
			Log(TAG, "Using saved encoding: " + savedEncoding + " for file " + file.getAbsolutePath());
			return savedEncoding;
		}

		// Simple check for UTF-8 BOM
		try (InputStream fis = new FileInputStream(file)) {
			byte[] bom = new byte[3];
			int bytesRead = fis.read(bom, 0, 3);
			if (bytesRead == 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
				Log(TAG, "Detected UTF-8 BOM for file " + file.getAbsolutePath());
				return StandardCharsets.UTF_8.name();
			}
			// Add more BOM checks if needed (UTF-16 LE/BE etc.)
		} catch (IOException e) {
			LogError("Error checking BOM for file " + file.getAbsolutePath(), e);
		}

		// If no specific encoding saved or detected, default to UTF-8
		Log(TAG, "Defaulting to UTF-8 encoding for file " + file.getAbsolutePath());
		return StandardCharsets.UTF_8.name();
	}

	/**
	 * Deletes a file and its associated metadata files (.mime, .head, .enc).
	 *
	 * @param contentFile The main content file to delete.
	 */
	void deleteFileAndMetadata(File contentFile) {
		if (contentFile == null) {
			return;
		}
		String basePath = contentFile.getAbsolutePath();
		deleteFile(contentFile); // Delete the main file
		deleteFile(new File(basePath + META_MIME_SUFFIX));
		deleteFile(new File(basePath + META_HEADERS_SUFFIX));
		deleteFile(new File(basePath + META_ENCODING_SUFFIX));
		Log(TAG, "Deleted file and metadata for: " + basePath);
	}

	// Renamed original deleteFile for clarity
	void deleteSingleFile(File file) {
		if (file == null) {
			return;
		}
		if (file.exists()) {
			if (file.delete()) {
				// Log(TAG,"Deleted file: " + file.getAbsolutePath()); // Optional success log
			} else {
				LogError("Failed to delete file: " + file.getAbsolutePath(), null);
			}
		}
	}

	// Make the public deleteFile call the helper for consistency
	public void deleteFile(File file) {
		deleteSingleFile(file);
	}

	// --- Serving Cached Content ---

	/**
	 * Attempts to create a WebResourceResponse from cached data for the given request.
	 *
	 * @param request The original WebResourceRequest.
	 * @return A WebResourceResponse if the resource is found in cache with metadata, otherwise null.
	 */
	@Nullable // Indicate that null can be returned
	public WebResourceResponse getCachedResponse(WebResourceRequest request) {
		Uri uri = request.getUrl();
		String url = uri.toString();
		String filePath = buildLocalPath(uri);
		File contentFile = new File(filePath);

		if (!contentFile.exists() || !contentFile.isFile() || contentFile.length() == 0) {
			// Log(TAG,"Cache miss (file not found or empty): " + url);
			return null; // Not in cache or empty file
		}

		// --- Read Metadata ---
		String mimeType = getMimeTypeFromMeta(filePath);
		String encoding = getEncodingFromMeta(filePath); // Get explicitly saved encoding first
		Map<String, String> headers = getHeaders(filePath); // Get saved response headers

		// --- Determine MIME Type ---
		if (mimeType == null || mimeType.isEmpty()) {
			// If not saved, try guessing from URL (less reliable than Content-Type header)
			mimeType = guessMimeFromUrl(url);
			Log(TAG, "MIME type not found in metadata, guessed: " + mimeType + " for " + url);
			// If still null, WebView might make a default guess, or we could set a generic one
			if (mimeType == null) {
				mimeType = "application/octet-stream"; // A generic fallback
			}
		}

		// --- Determine Encoding ---
		if (encoding == null || encoding.isEmpty()) {
			// If not explicitly saved, try guessing from the file content
			// Note: Guessing encoding from content can be slow/unreliable.
			// Prefer relying on saved encoding or a sensible default like UTF-8 for text types.
			if (mimeType.startsWith("text/")) {
				encoding = guessEncodingFromFile(contentFile); // Guess for text types
				Log(TAG, "Encoding not found in metadata, guessed: " + encoding + " for " + url);
			}
			// If still null or not a text type, let WebView handle it or default to null/UTF-8
			if (encoding == null) {
				encoding = StandardCharsets.UTF_8.name(); // Default for text/* if guessing failed
				Log(TAG, "Using default encoding UTF-8 for text mime: " + mimeType);
			} else if (!mimeType.startsWith("text/")) {
				encoding = null; // Don't specify encoding for binary types
			}
		}

		// --- Prepare InputStream ---
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(contentFile);
		} catch (FileNotFoundException e) {
			LogError("Cached file disappeared unexpectedly: " + filePath, e);
			deleteFileAndMetadata(contentFile); // Clean up inconsistent state
			return null;
		} catch (SecurityException se) {
			LogError("Permission denied reading cached file: " + filePath, se);
			return null;
		}

		Log(TAG, "Cache hit: Serving " + url + " from " + filePath);

		// --- Construct WebResourceResponse ---
		return new WebResourceResponse(mimeType, encoding, inputStream);
	}

	/**
	 * Guesses the MIME type from the file extension in a URL.
	 *
	 * @param url The URL string.
	 * @return The guessed MIME type or null if unable to guess.
	 */
	@Nullable
	public String guessMimeFromUrl(String url) {
		String fileExtension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (fileExtension != null) {
			return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
		}
		return null;
	}


	public void close() {
		Log(TAG, "Closing MyUtils resources...");
		urlLogger.close();
		logger.close();
		reqLogger.close();
		executorService.shutdown();
		Log(TAG, "Executor service shut down.");
		// Consider using shutdownNow() or awaitTermination() if needed,
		// but simple shutdown is usually fine for background tasks.
	}
}