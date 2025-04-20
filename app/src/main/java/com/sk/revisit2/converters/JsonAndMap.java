package com.sk.revisit2.converters;

import androidx.annotation.Nullable;

import com.sk.revisit2.log.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class JsonAndMap {

	public static String TAG = JsonAndMap.class.getSimpleName();

	public static Map<String, String> jsonToHeaders(String jsonString) {
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

	@Nullable
	public static JSONObject headersToJson(Map<String, List<String>> headersMap) {
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
}
