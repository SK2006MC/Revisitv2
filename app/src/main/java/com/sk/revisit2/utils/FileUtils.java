package com.sk.revisit2.utils;

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

public class FileUtils implements AutoCloseable {
	private final String TAG = FileUtils.class.getSimpleName();
	private final ExecutorService executorService;

	public FileUtils(String rootPath, ExecutorService executorService) {
		rootPath = normalizePath(rootPath);
		prepareDirectory(new File(rootPath));
		this.executorService = executorService;
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
				Log.e(TAG, e);
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
					Log.e(TAG, "err deleting file");
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

	public String getString(File file, String encoding) {
		if (file == null || !file.exists() || !file.isFile()) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		String effectiveEncoding = encoding;
		if (effectiveEncoding == null || effectiveEncoding.isEmpty()) {
			effectiveEncoding = StandardCharsets.UTF_8.name();
			Log.d(TAG, ": Using default encoding UTF-8 for file: " + file.getPath());
		}

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), effectiveEncoding))) {
			char[] buffer = new char[1024];
			int charsRead;
			while ((charsRead = reader.read(buffer)) != -1) {
				builder.append(buffer, 0, charsRead);
			}
			return builder.toString();
		} catch (IOException e) {
			Log.e(TAG, ": ERROR: Error reading string from file: " + file.getPath() + " - " + e.getMessage());
			return null;
		} catch (Exception e) {
			Log.e(TAG, ": ERROR: Error processing file: " + file.getPath() + " - " + e.getMessage());
			return null;
		}
	}

	public String getString(File file) {
		return getString(file, null);
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
}