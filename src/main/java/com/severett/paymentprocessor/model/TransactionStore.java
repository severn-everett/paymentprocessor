package com.severett.paymentprocessor.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class TransactionStore implements ITransactionStore {
    
    private static final String SUM_STAT = "sum";
    private static final String AVG_STAT = "avg";
    private static final String MAX_STAT = "max";
    private static final String MIN_STAT = "min";
    private static final String COUNT_STAT = "count";
    
    private double sum = 0.0;
    private long count = 0L;
    
    private final PriorityQueue<Double> maxQueue;
    private final PriorityQueue<Double> minQueue;
    private final PriorityQueue<Transaction> timestampQueue;
    
    public TransactionStore() {
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
        final Runnable trimQueueTask = new Runnable() {
            @Override
            public void run() {
                trimQueue();
            }
        };

        executorService.scheduleAtFixedRate(new Runnable() {
            private final ExecutorService executor = Executors.newSingleThreadExecutor();
            private Future<?> lastExecution;
            @Override
            public void run() {
                if ((lastExecution != null) && (!lastExecution.isDone())) {
                    return;
                }
                lastExecution = executor.submit(trimQueueTask);
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }
    
    @Override
    public void addTransaction(Transaction transaction) {
        new Thread(() -> {
            addToStats(transaction);
        }).start();
    }
    
    private void addToStats(Transaction transaction) {
        synchronized (this) {
            timestampQueue.add(transaction);
            double amt = transaction.getAmt();
            maxQueue.add(amt);
            minQueue.add(amt);
            sum += amt;
            count += 1;
        }
    }
    
    @Override
    public Map<String, Object> getTransactionStats() {
        synchronized (this) {
            Map<String, Object> statsMap = new HashMap<>();
            // Add the "sum" statistic
            statsMap.put(SUM_STAT, sum);
            // Add the "avg" statistic (default to 0.0)
            double avg = count > 0 ? sum / (double) count : 0.0;
            statsMap.put(AVG_STAT, avg);
            // Add the "max" statistic (default to 0.0)
            double max = !maxQueue.isEmpty() ? maxQueue.peek() : 0.0;
            statsMap.put(MAX_STAT, max);
            // Add the "min" statistic (default to 0.0)
            double min = !minQueue.isEmpty() ? minQueue.peek() : 0.0;
            statsMap.put(MIN_STAT, min);
            // Add the "count" statistic
            statsMap.put(COUNT_STAT, count);
            return statsMap;
        }
    }
    
    private void trimQueue() {
        synchronized (this) {
            Instant minuteBefore = Instant.now().minusSeconds(60L);
            boolean reachedMinuteBefore = false;
            while (!reachedMinuteBefore) {
                Transaction oldest = timestampQueue.peek();
                if ((oldest != null) && (oldest.getTimestamp().compareTo(minuteBefore) < 0)) {
                    timestampQueue.poll();
                    double amt = oldest.getAmt();
                    sum -= amt;
                    maxQueue.remove(amt);
                    minQueue.remove(amt);
                    count -= 1;
                } else {
                    reachedMinuteBefore = true;
                }
            }
        }
    }
    
}
