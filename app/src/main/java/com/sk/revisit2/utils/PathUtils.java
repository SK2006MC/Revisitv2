package com.sk.revisit2.utils;

import java.io.File;

public class PathUtils {

	static String sep = File.separator;

	public static String normalizePathE(String path) {
		return path.endsWith(sep) ? path : path + sep;
	}


}
