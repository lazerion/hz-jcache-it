package com.hazelcast.jcache.it.utils;


import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ClientContainer {
    private final Logger logger = LoggerFactory.getLogger(ClientContainer.class);
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private final String baseUrl;
    private OkHttpClient client = new OkHttpClient();

    public ClientContainer() {
        baseUrl = "http://localhost:4567";
    }


    public Optional<CacheStats> statistics() {
        Request request = new Request.Builder()
                .url(String.format("%s/stats", baseUrl))
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                logger.error("error {}", response.message());
                return Optional.empty();
            }
            final String stats = response.body().string();
            return Optional.of(new Gson().fromJson(stats, CacheStats.class));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public boolean ensureOpenForGetCacheNames() {
        Request request = new Request.Builder()
                .url(String.format("%s/ensure-open", baseUrl))
                .build();

        return sendRequest(request);
    }

    public Snapshot snapshot() {
        Request request = new Request.Builder()
                .url(String.format("%s/snapshot", baseUrl))
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                return null;
            }
            final String snap = response.body().string();
            return new Gson().fromJson(snap, Snapshot.class);
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean verify(Snapshot snapshot, String version) {

        RequestBody body = RequestBody.create(JSON, new Gson().toJson(snapshot));
        Request request = new Request.Builder()
                .url(String.format("%s/snapshot/%s", baseUrl, version))
                .post(body)
                .build();

        return sendRequest(request);
    }

    private boolean sendRequest(Request request) {
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                logger.error("error {}", response.message());
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
