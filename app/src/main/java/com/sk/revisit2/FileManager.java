package com.sk.revisit2;

import android.util.Log;
import com.sk.revisit2.log.LoggerHelper;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileManager {
    private static final String TAG = FileManager.class.getSimpleName();
    private final LoggerHelper logger;
    private final int writeBuffer;

    public FileManager(LoggerHelper logger, int writeBuffer) {
        this.logger = logger;
        this.writeBuffer = writeBuffer;
    }

    public void prepareDirectory(File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                logger.log(TAG + ": Failed to create directory: " + dir.getAbsolutePath());
            }
        }
    }

    public File prepareFile(String path) {
        File file = new File(path);
        prepareDirectory(file.getParentFile());
        return file;
    }

    public void writeStringToFile(String filePath, String content) {
        File file = prepareFile(filePath);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(content);
        } catch (IOException e) {
            logger.log(TAG + ": ERROR: Failed to write to file " + filePath + " - " + e.getMessage());
        }
    }

    public String readStringFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        } catch (IOException e) {
            logger.log(TAG + ": ERROR: Failed to read file " + filePath + " - " + e.getMessage());
            return null;
        }
    }

    public String getString(File file, String encoding) {
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), encoding))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        } catch (IOException e) {
            logger.log(TAG + ": ERROR: Failed to read file " + file.getAbsolutePath() + " - " + e.getMessage());
            return null;
        }
    }

    public String getString(File file) {
        return getString(file, StandardCharsets.UTF_8.name());
    }

    public void deleteFile(File file) {
        if (file != null && file.exists()) {
            if (!file.delete()) {
                logger.log(TAG + ": Failed to delete file: " + file.getAbsolutePath());
            }
        }
    }

    public void deleteFileAndMetadata(String baseFilePath, MetadataManager metadataManager) {
        deleteFile(new File(baseFilePath));
        metadataManager.deleteMetadata(baseFilePath);
    }
} 