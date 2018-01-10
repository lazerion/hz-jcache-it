package com.hazelcast.jcache.it.client;

import com.google.gson.Gson;
import com.hazelcast.cache.ICache;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static spark.Spark.get;

public class DummyClient {
    enum State {
        WAIT,
        READY
    }

    private static Logger logger = LoggerFactory.getLogger(DummyClient.class);
    private static ICache<String, String> cache;
    private static State state = State.WAIT;

    public static void main(String[] args) throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        initialize();
        startServer();
        startClient();
    }

    private static void startClient() throws InterruptedException {
        logger.info("Starting client ...");
        state = State.WAIT;
        while (true) {
            logger.info("Running ICache...");
            IntStream.range(0, 100).forEach(it -> {
                final String key = RandomStringUtils.randomAlphabetic(42);
                final String value = RandomStringUtils.randomAlphabetic(42);
                cache.putIfAbsent(key, value);
                final String actual = cache.get(key);
                if (!actual.equals(value)) {
                    logger.error("Value mismatch");
                }
            });

            cache.clear();
            state = State.READY;
            TimeUnit.SECONDS.sleep(1);
        }
    }

    private static void startServer() {
        logger.info("Starting server ...");
        get("/stats", (req, res) -> {
            logger.info("Querying statistics...");
            if (State.READY != state) {
                throw new RuntimeException("Stats are not ready");
            }
            return new Gson().toJson(cache.getLocalCacheStatistics());
        });
    }

    private static void initialize() {
        logger.info("Initializing cache");

        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();
        // TODO below does not work try it again
//        MutableConfiguration<String, String> config =
//                new MutableConfiguration<String, String>()
//                        .setTypes(String.class, String.class)
//                        .setStatisticsEnabled(true);


        String cacheName = RandomStringUtils.randomAlphabetic(42);
        cacheManager.enableStatistics(cacheName, true);
        cache = (ICache<String, String>) cacheManager.getCache(cacheName, String.class, String.class);
        logger.info("Initialized cache");
    }
}
