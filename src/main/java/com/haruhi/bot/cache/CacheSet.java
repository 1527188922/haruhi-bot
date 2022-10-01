package com.haruhi.bot.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.concurrent.TimeUnit;

public class CacheSet<K> {

    CacheMap<K,Object> cacheMap;
    private final static Object OBJECT = new Object();

    /**
     * 有参构造
     * @param expireTime 过期时间
     * @param timeUnit 过期时间单位
     * @param maximumSize 缓存最大容量
     */
    public CacheSet(long expireTime, TimeUnit timeUnit, long maximumSize){
        cacheMap = new CacheMap<>(expireTime,timeUnit,maximumSize);
    }

    public boolean contains(K key) {
        return cacheMap.get(key) != null;
    }

    public void add(K key) {
        cacheMap.put(key, OBJECT);
    }

    public void remove(K key) {
        cacheMap.remove(key);
    }

    public void removeAll() {
        cacheMap.removeAll();
    }

    public CacheStats stats() {
        return cacheMap.stats();
    }

    public long size() {
        return cacheMap.size();
    }
}
