package org.example.ringBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RingBufferTest {
    private RingBuffer<Integer> ringBuffer;

    @BeforeEach
    public void setUp() {
        ringBuffer = new RingBuffer<>(5);
    }

    @Test
    public void testPutAndGet() throws InterruptedException {
        ringBuffer.put(1);
        ringBuffer.put(2);
        ringBuffer.put(3);

        assertEquals(3, ringBuffer.size());
        assertEquals(1, ringBuffer.get());
        assertEquals(2, ringBuffer.get());
        assertEquals(1, ringBuffer.size());
    }

    @Test
    public void testIsEmptyAndIsFull() throws InterruptedException {
        assertTrue(ringBuffer.isEmpty());
        assertFalse(ringBuffer.isFull());

        for (int i = 0; i < 5; i++) {
            ringBuffer.put(i);
        }

        assertFalse(ringBuffer.isEmpty());
        assertTrue(ringBuffer.isFull());
    }

    @Test
    public void testWith2Threads() throws InterruptedException {
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {

            CountDownLatch latch = new CountDownLatch(2);
            executor.submit(() -> {
                try {
                    latch.await();
                    for (int i = 0; i < 15; i++) {
                        ringBuffer.put(i);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            executor.submit(() -> {
                try {
                    latch.await();
                    for (int i = 0; i < 15; i++) {
                        ringBuffer.get();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            latch.countDown();
            latch.countDown();

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            assertTrue(ringBuffer.isEmpty());
        }
    }

}