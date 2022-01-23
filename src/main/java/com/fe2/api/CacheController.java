package com.fe2.api;

import com.fe2.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Date;
import java.util.Optional;

@RestController
public class CacheController {

	@Autowired
    private Configuration configuration;
	
    /*
    Example: http://localhost:8080/cache/clear?olderthanhours=1
     */
    @GetMapping("/cache/clear")
    public ResponseEntity<String> cacheDelete(@RequestParam(value = "olderthanhours") Optional<Integer> olderThanHours)
    {
    	if(configuration.isCacheEnabled())
    	{
    		try {
            	File[] allFiles = new File(configuration.getCacheFolder()).listFiles();
                if (allFiles != null) {
                    for (File file : allFiles) {
                    	if(olderThanHours.isPresent())
                    	{
                    		if((new Date().getTime() - file.lastModified()) > (olderThanHours.get() * 60 * 60 * 1000))
                    		{
                    			file.delete();
                    		}
                    	}
                    	else
                    	{
                    		file.delete();
                    	}                    	                      
                    }
                }
    		} catch (Exception e) {
    			return ResponseEntity.internalServerError().build();
    		}
    	}
    	
    	return ResponseEntity.ok("OK");
    }
}
