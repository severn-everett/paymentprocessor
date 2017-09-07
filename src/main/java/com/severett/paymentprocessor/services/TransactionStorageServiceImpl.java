package com.severett.paymentprocessor.services;

import com.severett.paymentprocessor.model.Transaction;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TransactionStorageServiceImpl implements TransactionStorageService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionStorageServiceImpl.class);
    
    private static final String SUM_STAT = "sum";
    private static final String AVG_STAT = "avg";
    private static final String MAX_STAT = "max";
    private static final String MIN_STAT = "min";
    private static final String COUNT_STAT = "count";
    
    private long sum = 0L;
    private long transactionCount = 0L;
    
    private final PriorityQueue<Long> maxQueue;
    private final PriorityQueue<Long> minQueue;
    private final PriorityQueue<Transaction> timestampQueue;
    
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    
    public TransactionStorageServiceImpl() {
        maxQueue = new PriorityQueue<>((t1, t2) -> {
            return t2.compareTo(t1);
        });
        minQueue = new PriorityQueue<>((t1, t2) -> {
            return t1.compareTo(t2);
        });
        timestampQueue = new PriorityQueue<>((t1, t2) -> {
            return t1.getTimestamp().compareTo(t2.getTimestamp());
        });
        
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> trimQueue(), 1L, 1L, TimeUnit.SECONDS);
    }
    
    @Override
    public void addTransaction(Transaction transaction) {
        new Thread(() -> {
            addToStats(transaction);
        }).start();
    }
    
    private void addToStats(Transaction transaction) {
        try {
            writeLock.lock();
            timestampQueue.add(transaction);
            long count = transaction.getCount();
            maxQueue.add(count);
            minQueue.add(count);
            sum += count;
            transactionCount += 1;
        } finally {
            writeLock.unlock();
        }
    }
    
    @Override
    public Optional<Map<String, Object>> getTransactionStats() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Map<String, Object>> callable = () -> compileTransactionStats();
        Future<Map<String, Object>> future = executor.submit(callable);
        try {
            return Optional.of(future.get());
        } catch (Exception e) {
            LOGGER.error("Exception occurred when obtaining statistics: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    private Map<String, Object> compileTransactionStats() {
        try {
            readLock.lock();
            Map<String, Object> statsMap = new HashMap<>();
            // Add the "sum" statistic
            statsMap.put(SUM_STAT, sum);
            // Add the "avg" statistic (default to 0.0)
            double avg = transactionCount > 0 ? sum / (double) transactionCount : 0.0;
            statsMap.put(AVG_STAT, avg);
            // Add the "max" statistic (default to 0.0)
            long max = !maxQueue.isEmpty() ? maxQueue.peek() : 0L;
            statsMap.put(MAX_STAT, max);
            // Add the "min" statistic (default to 0.0)
            long min = !minQueue.isEmpty() ? minQueue.peek() : 0L;
            statsMap.put(MIN_STAT, min);
            // Add the "count" statistic
            statsMap.put(COUNT_STAT, transactionCount);
            return statsMap;
        } finally {
            readLock.unlock();
        }
    }
    
    private void trimQueue() {
        try {
            writeLock.lock();
            LOGGER.debug("Starting Trim Operation");
            Instant minuteBefore = Instant.now().minusSeconds(60L);
            boolean finishTrim = false;
            while (!finishTrim) {
                Transaction oldest = timestampQueue.peek();
                // Check for whether the queue is empty or if there are no expired transactions
                if ((oldest != null) && (oldest.getTimestamp().compareTo(minuteBefore) < 0)) {
                    timestampQueue.poll();
                    long count = oldest.getCount();
                    sum -= count;
                    maxQueue.remove(count);
                    minQueue.remove(count);
                    transactionCount -= 1;
                } else {
                    finishTrim = true;
                }
            }
        } finally {
            LOGGER.debug("Trim Operation Completed");
            writeLock.unlock();
        } 
    }
    
}
