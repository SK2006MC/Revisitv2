package com.sk.revisit2.log;

import android.webkit.WebResourceRequest;

import com.sk.revisit2.utils.Utils;

import java.util.concurrent.ExecutorService;

public class LoggerManager implements AutoCloseable {
	private final LoggerHelper logger;
	private final LoggerHelper urlLogger;
	private final LoggerHelper reqLogger;

	public LoggerManager(String rootPath, ExecutorService executorService) {
		this.logger = new LoggerHelper(rootPath + "log.txt", executorService);
		this.urlLogger = new LoggerHelper(rootPath + "urls.txt", executorService);
		this.reqLogger = new LoggerHelper(rootPath + "req.txt", executorService);
	}

	public void log(String tag, String message) {
		logger.log(tag + ": " + message);
	}

	public void logUrl(String url) {
		urlLogger.log(url);
	}

	public void logRequest(WebResourceRequest req) {
		reqLogger.log(req.getUrl().toString() + "\t|\t" + Utils.headersToJson(req.getRequestHeaders()));
	}

	@Override
	public void close() {
		logger.close();
		urlLogger.close();
		reqLogger.close();
	}

	public LoggerHelper getLogger(LType type) {
		switch (type){
			case LOG: return logger;
			case REQ: return reqLogger;
			case URL: return urlLogger;
		}
		return null;
	}

	public enum LType {
		URL,REQ,LOG
	}
}