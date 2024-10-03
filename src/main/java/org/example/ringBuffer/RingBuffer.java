package org.example.ringBuffer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RingBuffer<T> {
    private static final int DEFAULT_CAPACITY = 16;
    private final T[] buffer;
    private int readerIndex;
    private int writerIndex;
    private int count;
    private final Lock lock;
    private final Condition notFull;
    private final Condition notEmpty;

    public RingBuffer(int capacity) {
        capacity = capacity < 1 ? DEFAULT_CAPACITY : capacity;
        buffer = (T[]) new Object[capacity];
        readerIndex = 0;
        writerIndex = 0;
        count = 0;
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
    }

    public void put(T obj) throws InterruptedException {
        lock.lock();
        try {
            while (count == buffer.length) {
                notFull.await();
            }

            buffer[writerIndex] = obj;
            writerIndex = (writerIndex + 1) % buffer.length;
            count++;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T get() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) {
                notEmpty.await();
            }

            T obj = buffer[readerIndex];
            readerIndex = (readerIndex + 1) % buffer.length;
            count--;
            notFull.signal();

            return obj;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return count == 0;
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull() {
        lock.lock();
        try {
            return count == buffer.length;
        } finally {
            lock.unlock();
        }
    }
}
