package com.hazelcast.jcache.it.utils;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ClientContainer {
    private final Logger logger = LoggerFactory.getLogger(ClientContainer.class);

    private final String baseUrl;
    private OkHttpClient client = new OkHttpClient();

    public ClientContainer(){
        baseUrl = "http://localhost:4567";
    }


    public String statistics() {
        Request request = new Request.Builder()
                .url(String.format("%s/stats", baseUrl))
                .build();

        try {
            Response response = client.newCall(request).execute();
            logger.info("response {}", response);
            return response.message();
        } catch (Exception ex){
            return "";
        }
    }
}
