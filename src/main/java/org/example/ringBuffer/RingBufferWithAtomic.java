package org.example.ringBuffer;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class RingBufferWithAtomic<T> {
    private final T[] buffer;
    private final AtomicInteger head;
    private final AtomicInteger tail;
    private final Semaphore empty;
    private final Semaphore full;
    private final AtomicInteger count;

    @SuppressWarnings("unchecked")
    public RingBufferWithAtomic(int capacity) {
        buffer = (T[]) new Object[capacity];
        head = new AtomicInteger(0);
        tail = new AtomicInteger(0);
        empty = new Semaphore(capacity);
        full = new Semaphore(0);
        count = new AtomicInteger(0);
    }

    public void put(T item) throws InterruptedException {
        empty.acquire();
        int currentTail;
        do {
            currentTail = tail.get();
        } while (!tail.compareAndSet(currentTail, (currentTail + 1) % buffer.length));

        buffer[currentTail] = item;
        count.getAndIncrement();
        full.release();
        System.out.println(Arrays.toString(buffer));
    }

    public T get() throws InterruptedException {
        full.acquire();
        int currentHead;
        do {
            currentHead = head.get();
        } while (!head.compareAndSet(currentHead, (currentHead + 1) % buffer.length));

        T item = buffer[currentHead];
        count.getAndDecrement();
        empty.release();
        return item;
    }

    public int size() {
        return count.get();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isFull() {
        return size() == buffer.length;
    }
}
