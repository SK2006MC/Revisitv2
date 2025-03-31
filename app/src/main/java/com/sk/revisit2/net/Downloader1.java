package com.sk.revisit2.net;

import android.webkit.WebResourceRequest;

import java.io.File;

public class Downloader1 {

	DownloadCallback downloadCallback;

	interface DownloadCallback{
		void onStart();
		void onProgress();
		void onFinish();
	}

	void download(File path, WebResourceRequest request){
		downloadCallback.onStart();
		downloadCallback.onProgress();
		downloadCallback.onFinish();
	}
}
