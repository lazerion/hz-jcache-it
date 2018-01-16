package com.hazelcast.jcache.it.utils;


import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class Snapshot {
    @SerializedName("entries")
    private Map<String, String> entries;
    @SerializedName("name")
    private String name;
}
