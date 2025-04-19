package com.sk.revisit2.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.WebSettings;

import androidx.preference.PreferenceManager;

public class WebPreferenceManager {
	private static final String KEY_JAVASCRIPT_ENABLED = "javascript_enabled";
	private static final String KEY_DOM_STORAGE_ENABLED = "dom_storage_enabled";
	private static final String KEY_DATABASE_ENABLED = "database_enabled";
	private static final String KEY_SUPPORT_ZOOM = "support_zoom";
	private static final String KEY_SAFE_BROWSING = "safe_browsing";
	private static final String KEY_MULTIPLE_WINDOWS = "multiple_windows";
	private static final String KEY_MEDIA_PLAYBACK_GESTURE = "media_playback_gesture";
	private static final String KEY_MIXED_CONTENT_MODE = "mixed_content_mode";

	private final SharedPreferences sharedPreferences;

	public WebPreferenceManager(Context context) {
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public boolean isJavaScriptEnabled() {
		return sharedPreferences.getBoolean(KEY_JAVASCRIPT_ENABLED, true);
	}

	public boolean isDomStorageEnabled() {
		return sharedPreferences.getBoolean(KEY_DOM_STORAGE_ENABLED, true);
	}

	public boolean isDatabaseEnabled() {
		return sharedPreferences.getBoolean(KEY_DATABASE_ENABLED, true);
	}

	public boolean isSupportZoomEnabled() {
		return sharedPreferences.getBoolean(KEY_SUPPORT_ZOOM, true);
	}

	public boolean isSafeBrowsingEnabled() {
		return sharedPreferences.getBoolean(KEY_SAFE_BROWSING, false);
	}

	public boolean isMultipleWindowsEnabled() {
		return sharedPreferences.getBoolean(KEY_MULTIPLE_WINDOWS, true);
	}

	public boolean isMediaPlaybackGestureRequired() {
		return sharedPreferences.getBoolean(KEY_MEDIA_PLAYBACK_GESTURE, true);
	}

	public int getMixedContentMode() {
		String value = sharedPreferences.getString(KEY_MIXED_CONTENT_MODE, "0");
		return Integer.parseInt(value);
	}

	/**
	 * Applies all preferences to the given WebSettings
	 */
	public void applyToWebSettings(WebSettings settings) {
		settings.setJavaScriptEnabled(isJavaScriptEnabled());
		settings.setDomStorageEnabled(isDomStorageEnabled());
		settings.setDatabaseEnabled(isDatabaseEnabled());
		settings.setSupportZoom(isSupportZoomEnabled());
		settings.setSafeBrowsingEnabled(isSafeBrowsingEnabled());
		settings.setSupportMultipleWindows(isMultipleWindowsEnabled());
		settings.setMediaPlaybackRequiresUserGesture(isMediaPlaybackGestureRequired());
		settings.setMixedContentMode(getMixedContentMode());
	}

	/**
	 * Register a listener for preference changes
	 */
	public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
	}

	/**
	 * Unregister a preference change listener
	 */
	public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
	}
} 