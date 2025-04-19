package com.sk.revisit2.log;

import android.webkit.WebResourceRequest;

import com.sk.revisit2.utils.Utils;

import java.util.concurrent.ExecutorService;

public class LoggerManager implements AutoCloseable {
	private final FileLogger logger;
	private final FileLogger urlLogger;
	private final FileLogger reqLogger;

	public LoggerManager(String rootPath, ExecutorService executorService) {
		this.logger = new FileLogger(rootPath + "log.txt", executorService);
		this.urlLogger = new FileLogger(rootPath + "urls.txt", executorService);
		this.reqLogger = new FileLogger(rootPath + "req.txt", executorService);
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

	public FileLogger getLogger(LType type) {
		switch (type) {
			case LOG:
				return logger;
			case REQ:
				return reqLogger;
			case URL:
				return urlLogger;
		}
		return null;
	}

	public enum LType {
		URL, REQ, LOG
	}
}