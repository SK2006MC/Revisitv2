package com.sk.revisit2.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class FileUtils {
	private static final String TAG = FileUtils.class.getSimpleName();

	public static void prepareDirectory(File dir) {
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				return;
			}
		}
	}

	public static File prepareFile(String path) {
		File file = new File(path);
		try {
			if (file.exists()) {
				return file.isFile() ? file : null;
			}

			File parent = file.getParentFile();
			if (parent != null && !parent.exists()){
				if(!parent.mkdirs()){
					return null;
				}
			}

			return file.createNewFile() ? file : null;
		} catch (IOException e) {
			return null;
		}
	}

	public static void writeStringToFile(String filePath, String content) {
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
			writer.write(content);
		} catch (IOException e) {
			Log.e(TAG, e.toString(), e);
		}
	}

	public static String readStringFromFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			return null;
		}

		StringBuilder content = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
			}
		} catch (IOException e) {
			Log.e(TAG, e.toString(), e);
			return null;
		}
		return content.toString();
	}

	public static void deleteFile(File file) {
		if (file != null && file.exists()) {
			if (!file.delete()) {
				Log.e(TAG, "err deleting file");
			}
		}
	}

	public static void deleteFile(String filePath) {
		deleteFile(new File(filePath));
	}

	public static boolean fileExists(String path) {
		File file = new File(path);
		return file.exists() && file.isFile();
	}

	public static boolean directoryExists(String path) {
		File dir = new File(path);
		return dir.exists() && dir.isDirectory();
	}

	public static String getFileExtension(String filename) {
		if (filename == null) {
			return "";
		}
		int lastDot = filename.lastIndexOf('.');
		return lastDot == -1 ? "" : filename.substring(lastDot + 1);
	}

}