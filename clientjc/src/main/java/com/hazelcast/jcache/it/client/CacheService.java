package com.hazelcast.jcache.it.client;


import com.hazelcast.cache.CacheStatistics;
import com.hazelcast.cache.ICache;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.IntStream;

public class CacheService {
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
}
