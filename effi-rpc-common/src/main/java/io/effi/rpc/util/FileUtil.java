package io.effi.rpc.util;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import static java.lang.String.format;

/**
 * Provides for file operations.
 */
public final class FileUtil {

    private FileUtil() {
    }

    /**
     * Writes the content to the file.
     */
    public static void writeLineFile(CharSequence content, File targetFile) {
        try {
            if (StringUtil.isBlank(content)) {
                return;
            }
            if (!targetFile.exists()) {
                createFileWithDir(targetFile.getAbsolutePath());
            }
            try (RandomAccessFile file = new RandomAccessFile(targetFile, "rw");
                 FileChannel channel = file.getChannel(); FileLock ignored = channel.lock()) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = file.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                String fileContents = sb.toString();
                if (!fileContents.contains(content)) {
                    fileContents += content + "\n";
                }
                file.setLength(0);
                file.write(fileContents.getBytes());
            }
        } catch (Exception e) {
            throw new RuntimeException("Write file is failed", e);
        }
    }

    /**
     * Creates File with dir.
     */
    public static void createFileWithDir(String filePath) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        mkdir(parentDir);
        try {
            boolean success = file.createNewFile();
            if (!success) {
                throw new RuntimeException(format("Create file: %s is not success", filePath));
            }
        } catch (Exception e) {
            throw new RuntimeException(format("Create file: %s is failed", filePath), e);
        }
    }

    /**
     * Converts input stream to byte array.
     */
    public static byte[] toBytes(InputStream inputStream) throws IOException {
        try (inputStream; ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }

    /**
     * Creates dir.
     */
    private static void mkdir(File directory) {
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                throw new RuntimeException(format("Create dir: %s is not success", directory.getPath()));
            }
        }
    }
}

