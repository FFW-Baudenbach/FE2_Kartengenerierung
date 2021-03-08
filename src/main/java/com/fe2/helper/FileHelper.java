package com.fe2.helper;

import org.springframework.boot.actuate.health.Health;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelper {

    public static Path getFullOutputFilePath(final String outputFolder, final String outputFileNamePrefix, final String outputFormat)
    {
        return Paths.get(outputFolder, outputFileNamePrefix + getFileEnding(outputFormat));
    }

    public static String getFileEnding(final String outputFormat)
    {
        if (outputFormat.startsWith("png"))
            return ".png";
        if (outputFormat.startsWith("gif"))
            return ".gif";
        if (outputFormat.startsWith("jpg"))
            return ".jpg";

        throw new IllegalArgumentException("Unsupported image format");
    }

    public static MediaType getMediaType(final String outputFormat)
    {
        if (outputFormat.startsWith("png"))
            return MediaType.IMAGE_PNG;
        if (outputFormat.startsWith("gif"))
            return MediaType.IMAGE_GIF;
        if (outputFormat.startsWith("jpg"))
            return MediaType.IMAGE_JPEG;

        throw new IllegalArgumentException("Unsupported image format");
    }

    public static void writeToFile(byte[] image, Path targetPath) throws IOException {
        if (Files.exists(targetPath)) {
            Files.delete(targetPath);
        }
        Files.createFile(targetPath);
        Files.write(targetPath, image);
    }

    public static Health canReadWriteDirectory(final Path dir) {
        if (!Files.exists(dir))
            return Health.down().withDetail(dir.toString(), "Directory does not exist").build();
        if (!Files.isDirectory(dir))
            return Health.down().withDetail(dir.toString(), "Not a directory").build();
        if (!Files.isReadable(dir))
            return Health.down().withDetail(dir.toString(), "Directory is not readable").build();
        if (!Files.isWritable(dir))
            return Health.down().withDetail(dir.toString(), "Directory is not writable").build();

        return Health.up().withDetail(dir.toString(), "OK").build();
    }
}
