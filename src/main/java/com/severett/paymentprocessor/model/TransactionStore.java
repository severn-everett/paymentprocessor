package com.severett.paymentprocessor.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public class TransactionStore {
    
    private static final String SUM_STAT = "sum";
    private static final String AVG_STAT = "avg";
    private static final String MAX_STAT = "max";
    private static final String MIN_STAT = "min";
    private static final String COUNT_STAT = "count";
    
    private final Semaphore semaphore;
    
    private double sum = 0.0;
    private long count = 0L;
    
    private final PriorityQueue<Double> maxQueue;
    private final PriorityQueue<Double> minQueue;
    private final PriorityQueue<Transaction> timestampQueue;
    
    public TransactionStore() {
        semaphore = new Semaphore(1, true);
        maxQueue = new PriorityQueue<>((t1, t2) -> {
            return t1.compareTo(t2);
        });
        minQueue = new PriorityQueue<>((t1, t2) -> {
            return t2.compareTo(t1);
        });
        timestampQueue = new PriorityQueue<>((t1, t2) -> {
            return t1.getTimestamp().compareTo(t2.getTimestamp());
        });
    }
    
    public boolean addTransaction(Transaction transaction) {
        try {
            semaphore.acquire();
            timestampQueue.add(transaction);
            double amt = transaction.getAmt();
            maxQueue.add(amt);
            minQueue.add(amt);
            sum += amt;
            count += 1;
            return true;
        } catch (InterruptedException ie) {
            System.err.println("ERROR: " + ie);
            return false;
        } finally {
            semaphore.release();
        }
    }
    
    public Optional<Map<String, String>> getTransactionStats() {
        try {
            semaphore.acquire();
            
            Map<String, String> statsMap = new HashMap<>();
            // Add the "sum" statistic
            statsMap.put(SUM_STAT, Double.toString(sum));
            // Add the "avg" statistic (default to 0.0)
            double avg = count > 0 ? sum / (double) count : 0.0;
            statsMap.put(AVG_STAT, Double.toString(avg));
            // Add the "max" statistic (default to 0.0)
            double max = !maxQueue.isEmpty() ? maxQueue.peek() : 0.0;
            statsMap.put(MAX_STAT, Double.toString(max));
            // Add the "min" statistic (default to 0.0)
            double min = !minQueue.isEmpty() ? minQueue.peek() : 0.0;
            statsMap.put(MIN_STAT, Double.toString(min));
            // Add the "count" statistic
            statsMap.put(COUNT_STAT, Long.toString(count));
            return Optional.of(statsMap);
        } catch (InterruptedException ie) {
            System.err.println("ERROR: " + ie);
            return Optional.empty();
        } finally {
            semaphore.release();
        }
    }
    
    private void trimQueue() {
        try {
            semaphore.acquire();
            Instant minuteBefore = Instant.now().minusMillis(60000L);
            boolean reachedMinuteBefore = false;
            while (!reachedMinuteBefore) {
                Transaction oldest = timestampQueue.peek();
                if ((oldest != null) && (oldest.getTimestamp().compareTo(minuteBefore) < 0)) {
                    timestampQueue.poll();
                    double amt = oldest.getAmt();
                    maxQueue.remove(amt);
                    minQueue.remove(amt);
                    count -= 1;
                } else {
                    reachedMinuteBefore = true;
                }
            }
        } catch (InterruptedException ie) {
            System.err.println("ERROR: " + ie);
        } finally {
            semaphore.release();
        }
    }
    
}
