package com.fe2.service;

import com.fe2.configuration.Configuration;
import com.fe2.helper.FileHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class MapGenerator {

    @Autowired
    private UrlBuilder builder;

    @Autowired
    private Configuration configuration;



    public ResponseEntity<Object> generateMap(final String endpoint, final double lat, final double lng, Optional<String> size, Optional<String> identifier)
    {
    	// Germany: Latitude from 47.40724 to 54.9079 and longitude from 5.98815 to 14.98853.
        if (lat < lng || lat < 47 || lat > 54 || lng < 5 || lng > 14)
            return generateErrorResponse("ERROR: Input seems strange - did you confound latitude and longitude?");
    	
    	Path cacheFile = null;
    	byte[] image = null;
    	
    	String filename = endpoint;
        if (identifier.isPresent()) {
            filename += "_" + identifier.get();
        }
    	    	
    	if (configuration.isCacheEnabled())
    	{    		
        	cacheFile = FileHelper.getFullCacheOutputFilePath(configuration.getCacheFolder(), endpoint + lat + lng + size.orElse("") + identifier.orElse(""), configuration.getOutputFormat());
        	
        	if(FileHelper.checkIfFileExists(cacheFile))
        	{       		
        		try {
					image = Files.readAllBytes(cacheFile);
				} catch (Exception e) {
					return generateErrorResponse("ERROR: Reading file from cache: " + e.getMessage());
				}
        		
        		try {
                    if (configuration.isImageStoringEnabled()) {
                        Path outputFile = FileHelper.getFullOutputFilePath(configuration.getOutputFolder(), filename, configuration.getOutputFormat());
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
    	}

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
        
        try(InputStream in = url.openStream()) {
            image = StreamUtils.copyToByteArray(in);
        }
        catch (Exception e) {
            return generateErrorResponse("ERROR: Exception downloading image: " + e.getMessage());
        }

        try {
            if (configuration.isCacheEnabled() && !FileHelper.checkIfFileExists(cacheFile)) {
                FileHelper.writeToFile(image, cacheFile);
            }
        }
        catch (Exception e) {
            return generateErrorResponse("ERROR: Exception storing image in cache: " + e.getMessage());
        }
        
        try {
            if (configuration.isImageStoringEnabled()) {
                Path outputFile = FileHelper.getFullOutputFilePath(configuration.getOutputFolder(), filename, configuration.getOutputFormat());
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

    public ResponseEntity<Object> generateMap(final MultiValueMap<String, String> parameters, boolean showHydrants, boolean showRoute, boolean showPois)
    {
    	Path cacheFile = null;
    	byte[] image = null;
    	    	
    	if (configuration.isCacheEnabled())
    	{
    		String parametersAsString = "";
    		for(String key : parameters.keySet()){
    			parametersAsString += key + parameters.getFirst(key);
    		}
        	
        	cacheFile = FileHelper.getFullCacheOutputFilePath(configuration.getCacheFolder(), parametersAsString + showHydrants + showRoute + showPois, configuration.getOutputFormat());
        	
        	if(FileHelper.checkIfFileExists(cacheFile))
        	{       		
        		try {
					image = Files.readAllBytes(cacheFile);
				} catch (Exception e) {
					return generateErrorResponse("ERROR: Reading file from cache: " + e.getMessage());
				}

                return ResponseEntity
                        .ok()
                        .contentType(FileHelper.getMediaType(configuration.getOutputFormat()))
                        .body(image);
        	}        	
    	}
    	
        URL url;
        try {
            url = builder.generateGenericMapUrl(parameters, showHydrants, showRoute, showPois);
        }
        catch (Exception e) {
            return generateErrorResponse("ERROR: Exception generating URL: " + e.getMessage());
        }

        try(InputStream in = url.openStream()) {
            image = StreamUtils.copyToByteArray(in);
        }
        catch (Exception e) {
            return generateErrorResponse("ERROR: Exception downloading image: " + e.getMessage());
        }
        
        try {
            if (configuration.isCacheEnabled() && !FileHelper.checkIfFileExists(cacheFile)) {
                FileHelper.writeToFile(image, cacheFile);
            }
        }
        catch (Exception e) {
            return generateErrorResponse("ERROR: Exception storing image in cache: " + e.getMessage());
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
