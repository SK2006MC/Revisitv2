package com.sk.revisit2.utils;

import android.webkit.MimeTypeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Utils {

	public static String headersToJson(Map<String, String> headers) {
		JSONObject json = new JSONObject();
		try {
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					json.put(entry.getKey(), entry.getValue());
				}
			}
		} catch (JSONException e) {
			return "{}";
		}
		return json.toString();
	}

	public static JSONObject responseHeadersToJson(Map<String, List<String>> headersMap) {
		JSONObject json = new JSONObject();
		try {
			if (headersMap != null) {
				for (Map.Entry<String, List<String>> entry : headersMap.entrySet()) {
					if (entry.getValue() != null && !entry.getValue().isEmpty()) {
						json.put(entry.getKey(), entry.getValue().get(0));
					}
				}
			}
		} catch (JSONException e) {
			return new JSONObject();
		}
		return json;
	}

	public static Map<String, String> jsonToHeaders(String jsonString) {
		Map<String, String> headers = new HashMap<>();
		try {
			JSONObject json = new JSONObject(jsonString);
			Iterator<String> keys = json.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				headers.put(key, json.getString(key));
			}
		} catch (JSONException e) {
			return headers;
		}
		return headers;
	}

	public static String getFileExtension(String filename) {
		if (filename == null) {
			return "";
		}
		int lastDot = filename.lastIndexOf('.');
		return lastDot == -1 ? "" : filename.substring(lastDot + 1);
	}

	public static String getMimeTypeFromUrl(String url) {
		String extension = getFileExtension(url);
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
	}
}