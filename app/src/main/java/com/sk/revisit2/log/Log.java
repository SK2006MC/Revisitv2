package com.sk.revisit2.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Log {
	static final List<String[]> logs = new ArrayList<>();

	public static void e(String tag, String msg, Exception e) {
		logs.add(new String[]{tag, msg, e.toString()});
	}

	public static void e(String tag, String msg) {
		logs.add(new String[]{tag, msg});
	}

	public static void e(String tag, Exception e) {
		logs.add(new String[]{tag, e.toString()});
	}

	public static void i(String tag, String msg) {
		logs.add(new String[]{tag, msg});
	}

	public static void d(String tag, String msg) {
		logs.add(new String[]{tag, msg});
	}

	public static void saveLog(File logFile) throws Exception {
		BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
		for (String[] log : logs) {
			writer.write(Arrays.toString(log));
			writer.newLine();
			writer.flush();
		}
		writer.close();
	}
}