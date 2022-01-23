package com.fe2.helper;

import org.springframework.boot.actuate.health.Health;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class FileHelper {

    public static Path getFullOutputFilePath(final String outputFolder, final String outputFileNamePrefix, final String outputFormat)
    {
        return Paths.get(outputFolder, outputFileNamePrefix + getFileEnding(outputFormat));
    }
    
    public static Path getFullCacheOutputFilePath(final String cacheFolder, final String parametersForHash, final String outputFormat)
    {
    	try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(parametersForHash.getBytes());
			return Paths.get(cacheFolder, bytesToHex(md.digest()) + getFileEnding(outputFormat));
		} catch (Exception e) {
			return Paths.get(cacheFolder, parametersForHash.hashCode() + getFileEnding(outputFormat));
		}
    }
    
    public static boolean checkIfFileExists(final Path pathToFile)
    {
    	File f = pathToFile.toFile();
    	return (f.exists() && !f.isDirectory()) ? true : false;
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
    
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
