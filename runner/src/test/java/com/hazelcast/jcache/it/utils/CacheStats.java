package com.hazelcast.jcache.it.utils;


import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CacheStats {
    @SerializedName("puts")
    private int puts;
    @SerializedName("hits")
    private int hits;
    @SerializedName("misses")
    private int misses;
    @SerializedName("removals")
    private int removals;
}
