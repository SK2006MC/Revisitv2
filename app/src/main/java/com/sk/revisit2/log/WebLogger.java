package com.sk.revisit2.log;

import java.util.concurrent.ExecutorService;

public class WebLogger implements AutoCloseable {

    private final FileLogger urlLogger;
    private final FileLogger reqLogger;
    private final FileLogger localPathLogger;
	private final FileLogger webLogger;

    public WebLogger(String rootPath,ExecutorService executorService){
        urlLogger = new FileLogger(rootPath+"/Urls.txt",executorService);
        reqLogger = new FileLogger(rootPath+"/Requests.txt",executorService);
		localPathLogger = new FileLogger(rootPath+"/LocalPaths.txt",executorService);
		webLogger =  new FileLogger(rootPath+"/Web.txt",executorService);
    }

    public void logUrl(String url){
		urlLogger.log(url);
	}

    public void logRequest(String request){
		reqLogger.log(request);
	}

    public void logLocalPath(String localPath){
		localPathLogger.log(localPath);
	}

    @Override
    public void close(){
        urlLogger.close();
        reqLogger.close();
        localPathLogger.close();
    }

	public void Log(String string) {
		webLogger.log(string);
	}
}