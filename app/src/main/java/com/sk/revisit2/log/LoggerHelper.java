package com.sk.revisit2.log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;

public class LoggerHelper {

	final String TAG = LoggerHelper.class.getSimpleName();
	BufferedWriter writer;
	final ExecutorService executorService;

	public LoggerHelper(String filePath, ExecutorService executorService) {
		this.executorService = executorService;
		try {
			writer = new BufferedWriter(new FileWriter(filePath, true));
		} catch (Exception e) {
			Log.e(TAG, e);
		}
	}

	public void log(String msg) {
		executorService.execute(() -> logInner(msg));
	}

	public void logInner(String msg) {
		try {
			writer.write(msg);
			writer.newLine();
			writer.flush();
		} catch (Exception e) {
			Log.e(TAG, e);
		}
	}

	public void close() {
		try {
			writer.close();
		} catch (Exception e) {
			Log.e(TAG, e);
		}
	}
}