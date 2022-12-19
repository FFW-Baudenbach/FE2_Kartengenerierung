package com.fe2.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
public class CachingConfig {
    public static final String IMAGES = "IMAGES";

    @Autowired
    private com.fe2.configuration.Configuration configuration;

    @Bean
    public CacheManager cacheManager() {
        if (!configuration.isCachingEnabled())
            return new NoOpCacheManager();
        var manager = new ConcurrentMapCacheManager(IMAGES);
        manager.setAllowNullValues(false);
        return manager;
    }

    @CacheEvict(allEntries = true, value = {IMAGES})
    @Scheduled(fixedDelay = 60, timeUnit = TimeUnit.MINUTES, initialDelay = 60)
    public void reportCacheEvict() {
        if (configuration.isCachingEnabled())
            System.out.println("Flushing Cache");
    }

}