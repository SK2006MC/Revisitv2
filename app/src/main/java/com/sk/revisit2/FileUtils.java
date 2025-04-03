package com.sk.revisit2;

import android.util.Log;
import java.io.*;
import java.nio.charset.StandardCharsets;
import com.sk.revisit2.log.LoggerHelper;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();
    private final LoggerHelper logger;
    private final int WRITE_BUFFER = 8 * 1024;

    public FileUtils(LoggerHelper logger) {
        this.logger = logger;
    }

    public void prepareDirectory(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + dir.getAbsolutePath());
                return;
            }
        }
        dir.isDirectory();
    }

    public File prepareFile(String path) {
        File file = new File(path);
        try {
            if (file.exists()) {
                if (file.isFile()) {
                    return file;
                } else {
                    logger.log(TAG + ": ERROR: Path exists but is a directory: " + path);
                    return null;
                }
            }

            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    logger.log(TAG + ": ERROR: Failed to create parent directories for: " + parent.getAbsolutePath());
                    return null;
                }
            }

            if (file.createNewFile()) {
                logger.log(TAG + ": Created file: " + file.getAbsolutePath());
                return file;
            } else {
                if (file.exists() && file.isFile()) {
                    return file;
                }
                logger.log(TAG + ": ERROR: Failed to create file: " + file.getAbsolutePath());
                return null;
            }
        } catch (Exception e) {
            logger.log(TAG + ": ERROR: Error preparing file: " + path + " - " + e.getMessage());
            return null;
        }
    }

    public void writeStringToFile(String filePath, String content) {
        File file = new File(filePath);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(content);
        } catch (IOException e) {
            logger.log(TAG + ": ERROR: Failed to write metadata file: " + filePath + " - " + e.getMessage());
        }
    }

    public String readStringFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile() || file.length() == 0) {
            return null;
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            logger.log(TAG + ": ERROR: Error reading metadata file: " + filePath + " - " + e.getMessage());
            return null;
        }
        String result = content.toString().trim();
        return result.isEmpty() ? null : result;
    }

    public String getString(File file, String encoding) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        String effectiveEncoding = encoding;
        if (effectiveEncoding == null || effectiveEncoding.isEmpty()) {
            effectiveEncoding = StandardCharsets.UTF_8.name();
            logger.log(TAG + ": Using default encoding UTF-8 for file: " + file.getPath());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), effectiveEncoding))) {
            char[] buffer = new char[WRITE_BUFFER];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, charsRead);
            }
            return builder.toString();
        } catch (IOException e) {
            logger.log(TAG + ": ERROR: Error reading string from file: " + file.getPath() + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            logger.log(TAG + ": ERROR: Error processing file: " + file.getPath() + " - " + e.getMessage());
            return null;
        }
    }

    public String getString(File file) {
        return getString(file, null);
    }

    public void deleteFile(File file) {
        if (file == null) {
            return;
        }
        if (file.exists()) {
            if (!file.delete()) {
                logger.log(TAG + ": ERROR: Failed to delete file: " + file.getAbsolutePath());
            }
        }
    }
} 