package com.sk.revisit2.utils;

import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncodingUtils {
	private static final String TAG = EncodingUtils.class.getSimpleName();

	public static String encodeToB64(String url) {
		return Base64.encodeToString(url.getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
	}

	public static String hash(String url) {
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
			Log.e(TAG, ": ERROR: SHA-256 algorithm not available - " + e.getMessage());
			return encodeToB64(url);
		}
	}

	public static String guessEncodingFromFile(File file) {
		try (InputStream fis = new FileInputStream(file)) {
			byte[] bom = new byte[3];
			int bytesRead = fis.read(bom, 0, 3);
			if (bytesRead == 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
//				Log.d(TAG, ": Detected UTF-8 BOM for file " + file.getAbsolutePath());
				return StandardCharsets.UTF_8.name();
			}
		} catch (IOException e) {
//			Log.e(TAG, ": ERROR: Error checking BOM for file " + file.getAbsolutePath() + " - " + e.getMessage());
		}

//		Log.d(TAG, ": Defaulting to UTF-8 encoding for file " + file.getAbsolutePath());
		return StandardCharsets.UTF_8.name();
	}
} 