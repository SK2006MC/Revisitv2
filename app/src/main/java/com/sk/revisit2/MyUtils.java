package com.sk.revisit2;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceRequest;

import androidx.annotation.NonNull;

import android.util.Log;
import com.sk.revisit2.managers.UrlMetaManager;
import com.sk.revisit2.utils.EncodingUtils;
import com.sk.revisit2.utils.FileUtils;
import com.sk.revisit2.utils.PathUtils;
import com.sk.revisit2.webview.WebResourceDownloader2;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyUtils {

	private static final String TAG = MyUtils.class.getSimpleName();
	private static final int MAX_THREADS = 4;
	public static boolean isNetWorkAvailable = false, shouldUpdate = false;
	private final int WRITE_BUFFER = 8 * 1024;
	private final ExecutorService executorService;
	private final String rootPath;
	private final Context context;
	private final WebResourceDownloader2 webResourceDownloader;

	public MyUtils(Context context, String rootPath) {
		rootPath = PathUtils.normalizePathE(rootPath);
		this.context = context;
		this.rootPath = rootPath;
		this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
		this.webResourceDownloader = new WebResourceDownloader2(executorService, this, WRITE_BUFFER);
		FileUtils.prepareDirectory(new File(rootPath));
		Log("MyUtils initialized. Root path: " + rootPath);
	}

	public File prepareFile(String path) {
		return FileUtils.prepareFile(path);
	}

	public String buildLocalPath(Uri uri) {
		return buildLocalPath2(uri);
	}

	// public String buildLocalPath4(Uri uri){
	// 	String url = uri.getPath();
	// 	//check if the url is dynamic
	// 	assert url != null;
	// 	if(url.contains("=")){
	// 		return buildLocalPath2(uri);
	// 	}
	// }

	@NonNull
	private String buildBasePath(@NonNull Uri uri) {
		String authority = uri.getAuthority();
		String basePath = rootPath + authority + File.separator;
		FileUtils.prepareDirectory(new File(basePath));
		return basePath;
	}

	@NonNull
	private String buildLocalPath1(Uri uri) {
		return buildBasePath(uri) + EncodingUtils.encodeToB64(uri.toString());
	}

	@NonNull
	private String buildLocalPath2(@NonNull Uri uri) {
		String hashed = EncodingUtils.hash(uri.toString());
		if (hashed == null) {
			return buildLocalPath1(uri);
		}
		return buildBasePath(uri) + hashed;
	}

	public void download(WebResourceRequest request) {
		webResourceDownloader.download(request);
	}

	public void saveMimeType(String baseFilePath, String mimeType) {
		UrlMetaManager.saveMimeType(baseFilePath, mimeType);
	}

	public void saveEncoding(String baseFilePath, String encoding) {
		UrlMetaManager.saveEncoding(baseFilePath, encoding);
	}

	public void saveHeaders(String baseFilePath, Map<String, List<String>> headersMap) {
		UrlMetaManager.saveHeaders(baseFilePath, headersMap);
	}

	public String guessEncodingFromFile(File file) {
		return EncodingUtils.guessEncodingFromFile(file);
	}

	public String getMimeTypeFromMeta(String file) {
		return UrlMetaManager.getMimeTypeFromMeta(file);
	}

	public String getEncodingFromMeta(String file) {
		return UrlMetaManager.getEncodingFromMeta(file);
	}

	public Map<String, String> getHeadersFromMeta(String localFile) {
		return UrlMetaManager.getHeadersFromMeta(localFile);
	}

	private void Log(String msg) {
		Log.i(TAG, msg);
	}

	public void close() {
		Log("Closing MyUtils resources...");
		executorService.shutdown();
		try {
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Log("Error waiting for executor termination: " + e.getMessage());
		}
		Log("Executor service shut down.");
	}

	public String getRootPath() {
		return rootPath;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public Context getContext() {
		return context;
	}

	public WebResourceDownloader2 getWebResourceDownloader() {
		return webResourceDownloader;
	}
}
