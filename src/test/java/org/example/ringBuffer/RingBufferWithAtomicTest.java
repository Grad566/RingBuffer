package org.example.ringBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class RingBufferWithAtomicTest {
    private RingBufferWithAtomic<Integer> ringBuffer;

    @BeforeEach
    public void setUp() {
        ringBuffer = new RingBufferWithAtomic<>(5);
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
    public void testConcurrentPutAndGet() throws InterruptedException {
        int numProducers = 3;
        int numConsumers = 3;
        int itemsPerProducer = 10;

        ExecutorService executor = Executors.newFixedThreadPool(numProducers + numConsumers);
        CountDownLatch latch = new CountDownLatch(numProducers + numConsumers);

        // Producers
        for (int i = 0; i < numProducers; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < itemsPerProducer; j++) {
                        ringBuffer.put(j);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Consumers
        for (int i = 0; i < numConsumers; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < itemsPerProducer; j++) {
                        ringBuffer.get();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertTrue(ringBuffer.isEmpty());
    }

}