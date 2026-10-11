package com.zxyy.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/** 按 key 互斥的进程内锁；只保留正在使用或等待中的锁。 */
public final class KeyedLock {
    private final ConcurrentHashMap<String, LockEntry> locks = new ConcurrentHashMap<>();

    public <T> T withLock(String key, Supplier<T> action) {
        LockEntry entry = locks.compute(key, (ignored, current) -> {
            LockEntry retained = current == null ? new LockEntry() : current;
            // 持有者和等待者都计数，不能在仍有线程等待时移除锁。
            retained.users++;
            return retained;
        });
        entry.lock.lock();
        try {
            return action.get();
        } finally {
            entry.lock.unlock();
            locks.compute(key, (ignored, current) -> {
                // 同一个 key 的计数增减都在 compute 中原子执行。
                entry.users--;
                return entry.users == 0 ? null : entry;
            });
        }
    }

    private static final class LockEntry {
        private final ReentrantLock lock = new ReentrantLock();
        private int users;
    }
}
