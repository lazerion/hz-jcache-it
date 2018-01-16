package com.hazelcast.jcache.it.client;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static spark.Spark.get;
import static spark.Spark.post;

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
        get("/ensure-open", (req, res) -> service.ensureOpen());
        get("/snapshot", (req, res) -> new Gson().toJson(service.snapshot()));
        post("/snapshot/:version", (req, res) -> {
            String version = req.params(":version");
            Snapshot snapshot = new Gson().fromJson(req.body(), Snapshot.class);
            return service.verify(snapshot, version);
        });
    }

    private static void initialize() {
        logger.info("Initializing service");
        service = new CacheService();
        logger.info("Initialized cache");
    }
}
