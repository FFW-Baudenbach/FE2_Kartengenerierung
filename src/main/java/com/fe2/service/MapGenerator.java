package com.fe2.service;

import com.fe2.configuration.Configuration;
import com.fe2.helper.FileHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class MapGenerator {

    @Autowired
    private UrlBuilder builder;

    @Autowired
    private Configuration configuration;

    public ResponseEntity<Object> generateMap(final String endpoint, final double lat, final double lng, Optional<String> size)
    {
        // Germany: Latitude from 47.40724 to 54.9079 and longitude from 5.98815 to 14.98853.
        if (lat < lng || lat < 47 || lat > 54 || lng < 5 || lng > 14)
            return generateErrorResponse("ERROR: Input seems strange - did you confound latitude and longitude?");

        String sizeParam = size.orElse("640x640");

        URL url;
        try {
            switch (endpoint) {
                case "overview":
                    url = builder.generateOverviewRoadmapUrl(lat, lng, sizeParam);
                    break;
                case "detail":
                    url = builder.generateDetailHybridUrl(lat, lng, sizeParam);
                    break;
                case "route":
                    url = builder.generateRouteRoadmapUrl(lat, lng, sizeParam);
                    break;
                default:
                    throw new IllegalArgumentException(endpoint + " not supported!");
            }
        }
        catch (Exception e) {
            return generateErrorResponse("ERROR: Exception generating URL: " + e.getMessage());
        }

        byte[] image;
        try(InputStream in = url.openStream()) {
            image = StreamUtils.copyToByteArray(in);
        }
        catch (Exception e) {
            return generateErrorResponse("ERROR: Exception downloading image: " + e.getMessage());
        }

        try {
            if (configuration.isImageStoringEnabled()) {
                Path outputFile = FileHelper.getFullOutputFilePath(configuration.getOutputFolder(), endpoint, configuration.getOutputFormat());
                FileHelper.writeToFile(image, outputFile);
            }
        }
        catch (Exception e) {
            return generateErrorResponse("ERROR: Exception storing image: " + e.getMessage());
        }

        return ResponseEntity
                .ok()
                .contentType(FileHelper.getMediaType(configuration.getOutputFormat()))
                .body(image);
    }

    private ResponseEntity<Object> generateErrorResponse(final String message)
    {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body(message);
    }

}
