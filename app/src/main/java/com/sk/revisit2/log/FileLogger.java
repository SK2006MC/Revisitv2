package com.sk.revisit2.log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.ExecutorService;
import android.util.Log;


public class FileLogger {

	final String TAG = FileLogger.class.getSimpleName();
	final ExecutorService executorService;
	BufferedWriter writer;

	public FileLogger(String filePath, ExecutorService executorService) {
		this.executorService = executorService;
		try {
			writer = new BufferedWriter(new FileWriter(filePath, true));
		} catch (Exception e) {
			Log.e(TAG, e.toString(),e);
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
			Log.e(TAG, e.toString(),e);
		}
	}

	public void close() {
		try {
			writer.close();
		} catch (Exception e) {
			Log.e(TAG, e.toString(),e);
		}
	}
}