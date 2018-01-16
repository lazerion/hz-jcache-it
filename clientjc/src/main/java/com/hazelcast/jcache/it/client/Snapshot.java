package com.hazelcast.jcache.it.client;


import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
class Snapshot {
    @SerializedName("entries")
    private Map<String, String> entries;
    @SerializedName("name")
    private String name;
}
