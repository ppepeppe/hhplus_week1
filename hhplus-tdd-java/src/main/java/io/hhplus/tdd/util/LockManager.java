package io.hhplus.tdd.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

@Component
public class LockManager {
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    public void lock(Long userId) {
        locks.computeIfAbsent(userId, k -> new ReentrantLock()).lock();
    }

    public void unlock(Long userId) {
        ReentrantLock lock = locks.get(userId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
