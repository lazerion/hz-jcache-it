package com.hazelcast.jcache.it.client;


import com.hazelcast.cache.CacheStatistics;
import com.hazelcast.cache.ICache;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.stream.IntStream;

class CacheService {
    private final ICache<String, String> cache;

    CacheService(ICache<String, String> cache) {
        this.cache = cache;
    }

    CacheStatistics runOnEmpty() {
        IntStream.range(0, 100).forEach(it -> {
            final String key = RandomStringUtils.randomAlphabetic(42);
            final String value = RandomStringUtils.randomAlphabetic(42);
            cache.putIfAbsent(key, value);
            final String actual = cache.get(key);
            if (!StringUtils.equals(actual, value)) {
                throw new RuntimeException("key value mismatch");
            }
            cache.remove(key);
        });
        cache.clear();
        return cache.getLocalCacheStatistics();
    }

    String ensureOpen() {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        MutableConfiguration<String, String> config =
                new MutableConfiguration<String, String>()
                        .setTypes(String.class, String.class);

        String cacheName = RandomStringUtils.randomAlphabetic(42);
        cacheManager.createCache(cacheName, config);

        cacheManager.close();

        try {
            cacheManager.getCacheNames();
        } catch (IllegalStateException ex) {
            return "OK";
        }

        throw new RuntimeException("Ensure Open is not working!");
    }
}
