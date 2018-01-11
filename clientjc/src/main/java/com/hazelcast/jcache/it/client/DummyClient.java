package com.hazelcast.jcache.it.client;

import com.google.gson.Gson;
import com.hazelcast.cache.ICache;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.concurrent.TimeUnit;

import static spark.Spark.get;

public class DummyClient {

    private static Logger logger = LoggerFactory.getLogger(DummyClient.class);
    private static CacheService service;

    public static void main(String[] args) throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        initialize();
        startServer();
    }

    private static void startServer() {
        get("/stats", (req, res) -> new Gson().toJson(service.runOnEmpty()));
    }

    private static void initialize() {
        logger.info("Initializing service");

        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        MutableConfiguration<String, String> config =
                new MutableConfiguration<String, String>()
                        .setTypes(String.class, String.class)
                        .setStatisticsEnabled(true);


        String cacheName = RandomStringUtils.randomAlphabetic(42);
        ICache<String, String> cache = (ICache<String, String>) cacheManager.createCache(cacheName, config);
        service = new CacheService(cache);
        logger.info("Initialized cache");
    }
}
