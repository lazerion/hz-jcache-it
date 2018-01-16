package com.hazelcast.jcache.it.client;


import com.hazelcast.cache.CacheStatistics;
import com.hazelcast.cache.ICache;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.IntStream;

class CacheService {
    private final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final static int DEFAULT_SNAPSHOT_SIZE = 128;

    private final ICache<String, String> cache;
    private CacheManager manager;

    CacheService() {
        this.cache = createRandom();
    }

    private ICache<String, String> createRandom() {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        manager = cachingProvider.getCacheManager();

        MutableConfiguration<String, String> config =
                new MutableConfiguration<String, String>()
                        .setTypes(String.class, String.class)
                        .setStatisticsEnabled(true);


        String cacheName = RandomStringUtils.randomAlphabetic(42);
        return (ICache<String, String>) manager.createCache(cacheName, config);
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
        ICache random = createRandom();
        random.getCacheManager().close();
        try {
            random.getCacheManager().getCacheNames();
        } catch (IllegalStateException ex) {
            return "OK";
        }

        throw new RuntimeException("Ensure Open is not working!");
    }

    Snapshot snapshot() {
        ICache<String, String> random = createRandom();
        Set<String> keys = new LinkedHashSet<>();
        IntStream.range(0, DEFAULT_SNAPSHOT_SIZE).forEach(it -> {
            final String key = RandomStringUtils.randomAlphabetic(42);
            final String value = RandomStringUtils.randomAlphabetic(42);
            if (!random.putIfAbsent(key, value)) {
                throw new RuntimeException("unexpected operation result on cache");
            }
            keys.add(key);
        });
        return Snapshot.builder().entries(random.getAll(keys)).name(random.getName()).build();
    }

    String verify(Snapshot snapshot, String version) {
        if (snapshot.getEntries().size() != DEFAULT_SNAPSHOT_SIZE) {
            throw new RuntimeException("snapshot size mismatch");
        }

        logger.info("JCache version for verification {}", version);
        Cache<String, String> cache;
        if ("1.0".equals(version)){
            cache = manager.getCache(snapshot.getName(), String.class, String.class);
        } else if ("1.1".equals(version)){
            cache = manager.getCache(snapshot.getName());
        } else {
            throw new RuntimeException("Invalid jcache version");
        }

        snapshot.getEntries().entrySet().forEach(it -> {
            Object actual = cache.get(it.getKey());
            if (!actual.equals(it.getValue())) {
                throw new RuntimeException("key-value mismatch");
            }
        });

        return "OK";
    }
}
