package com.sk.revisit2.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Log {
	
	static String E="E",I="I",D="D";
	
	static final List<String[]> logs = new ArrayList<>();

	public static void e(String tag, String msg, Exception e) {
		logs.add(new String[]{E,tag, msg, e.toString()});
	}

	public static void e(String tag, String msg) {
		logs.add(new String[]{E,tag, msg});
	}

	public static void e(String tag, Exception e) {
		logs.add(new String[]{E,tag, e.toString()});
	}

	public static void i(String tag, String msg) {
		logs.add(new String[]{I,tag, msg});
	}

	public static void d(String tag, String msg) {
		logs.add(new String[]{D,tag, msg});
	}

	public static void saveLog(File logFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(logFile,true));
		for (String[] log : logs) {
			writer.write(Arrays.toString(log));
			writer.newLine();
			writer.flush();
		}
		writer.close();
	}
}