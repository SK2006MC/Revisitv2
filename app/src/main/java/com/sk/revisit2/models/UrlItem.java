package com.sk.revisit2.models;

public class UrlItem {
	private final String url;
	private final String method;
	private long size;
	private Status status;
	private int progress;

	public UrlItem(String url, String method) {
		this.url = url;
		this.method = method;
		this.size = 0;
		this.status = Status.LOADING;
		this.progress = 0;
	}

	public String getUrl() {
		return url;
	}

	public String getMethod() {
		return method;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public enum Status {
		IGNORED,
		LOADING,
		LOADED_LOCAL,
		LOADED_REMOTE,
		ERROR
	}
} 