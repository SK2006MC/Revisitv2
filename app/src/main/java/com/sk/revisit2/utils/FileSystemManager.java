package com.sk.revisit2.utils;

import android.net.Uri;

import com.sk.revisit2.log.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class FileSystemManager implements AutoCloseable {
	private final String rootPath;
	private final ExecutorService executorService;
	private final String TAG = FileSystemManager.class.getSimpleName();

	public FileSystemManager(String rootPath, ExecutorService executorService) {
		this.rootPath = normalizePath(rootPath);
		this.executorService = executorService;
		prepareDirectory(new File(this.rootPath));
	}

	private String normalizePath(String path) {
		return path.endsWith(File.separator) ? path : path + File.separator;
	}

	public void prepareDirectory(File dir) {
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				return;
			}
		}
		dir.isDirectory();
	}

	public File prepareFile(String path) {
		File file = new File(path);
		try {
			if (file.exists()) {
				return file.isFile() ? file : null;
			}

			File parent = file.getParentFile();
			if (parent != null && !parent.exists() && !parent.mkdirs()) {
				return null;
			}

			return file.createNewFile() ? file : null;
		} catch (IOException e) {
			return null;
		}
	}

	public void writeStringToFile(String filePath, String content) {
		executorService.execute(() -> {
			try (BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
				writer.write(content);
			} catch (IOException e) {
				// Error handling could be added here
			}
		});
	}

	public String readStringFromFile(String filePath) {
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
			return null;
		}
		return content.toString();
	}

	public void deleteFile(File file) {
		if (file != null && file.exists()) {
			executorService.execute(() -> {
				if (!file.delete()) {
					Log.e(TAG,"err deleting file");
				}
			});
		}
	}

	public void deleteFile(String filePath) {
		deleteFile(new File(filePath));
	}

	public boolean fileExists(String path) {
		File file = new File(path);
		return file.exists() && file.isFile();
	}

	public boolean directoryExists(String path) {
		File dir = new File(path);
		return dir.exists() && dir.isDirectory();
	}

	@Override
	public void close() {
		executorService.shutdown();
		try {
			if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	public String buildLocalPath(Uri uri) {
		return "";
	}
}