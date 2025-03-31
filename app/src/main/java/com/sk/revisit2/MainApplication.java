package com.sk.revisit2;

import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import com.sk.revisit2.log.LoggerManager;
import com.sk.revisit2.net.NetworkManager;
import com.sk.revisit2.utils.FileSystemManager;
import com.sk.revisit2.utils.ResourceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainApplication implements AutoCloseable {
	private FileSystemManager fileManager;
	private NetworkManager networkManager;
	private ResourceManager resourceManager;
	private LoggerManager loggerManager;
	private ExecutorService executorService;

	public void initialize(Context context, String rootPath) {
		executorService = Executors.newFixedThreadPool(4);
		this.fileManager = new FileSystemManager(rootPath, executorService);
		this.loggerManager = new LoggerManager(rootPath, executorService);
		this.networkManager = new NetworkManager(fileManager, loggerManager.getLogger(LoggerManager.LType.LOG));
		this.resourceManager = new ResourceManager(fileManager, loggerManager.getLogger(LoggerManager.LType.LOG));
	}

	// Manager access methods
	public FileSystemManager getFileManager() {
		return fileManager;
	}

	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	public LoggerManager getLoggerManager() {
		return loggerManager;
	}

	// Convenience methods
	public void downloadResource(WebResourceRequest request) {
		networkManager.download(request);
	}

	public WebResourceResponse getLocalResource(WebResourceRequest request) {
		return resourceManager.getLocalResponse(request);
	}

	public void log(String tag, String message) {
		loggerManager.log(tag, message);
	}

	@Override
	public void close() {
		if (loggerManager != null) {
			loggerManager.close();
		}
		if (fileManager != null) {
			fileManager.close();
		}
		if (executorService != null) {
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
}