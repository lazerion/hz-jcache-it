package com.hazelcast.jcache.it.utils;


import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class ClientContainer {
    private final Logger logger = LoggerFactory.getLogger(ClientContainer.class);

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
}
