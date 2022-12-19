package com.fe2.service;

import com.fe2.configuration.CachingConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
public class ImageRetriever {

    @Cacheable(value=CachingConfig.IMAGES, sync=true)
    public byte[] downloadImage(final URL inputUrl) throws IOException
    {
        System.out.println("Calling " + inputUrl.toString());
        try(InputStream in = inputUrl.openStream()) {
            byte[] result = StreamUtils.copyToByteArray(in);
            return result;
        }
    }
}
