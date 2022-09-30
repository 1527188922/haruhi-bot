package com.haruhi.bot.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CacheMap<K,V> {

    Cache<K,V> cache;

    public CacheMap(long expireTime, TimeUnit timeUnit, long maximumSize){
        cache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(expireTime, timeUnit)
                .build();
    }

    public V get(K key) {
        return cache.getIfPresent(key);
    }

    public V get(K key, Function<K, V> function) {
        return cache.get(key, function);
    }

    public void put(K key, V value) {
        cache.put(key, value);
    }

    public void remove(K key) {
        cache.invalidate(key);
    }

    public void removeAll() {
        cache.invalidateAll();
    }

    public CacheStats stats() {
        return cache.stats();
    }

    public long size() {
        return cache.estimatedSize();
    }
}
