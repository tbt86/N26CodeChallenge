package n26.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import n26.model.InvalidTimestampException;
import n26.model.Statistics;
import n26.model.Transaction;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionStore {

    private final int BUCKET_SIZE_MS = 1000;
    private final int STATISTICS_WINDOW_SECONDS = 60;

    // Keeping statistics separate for each bucket
    // Bucket sizes are defined in MS by {@link TransactionStore#BUCKET_SIZE_MS}
    // Number of entries in the map is defined by
    // STATISTICS_WINDOW_SECONDS * 1000 / BUCKET_SIZE_MS
    private final Map<Long, Statistics> statisticsMap = new HashMap<>();

    // The clock used to retrieve the current time
    private final Clock clock;

    // This keeps track of the statistics for the current second, e.g. if the time is 13:22:42.543, it will contain
    // transactions from 13:22:42.000 up to the current time
    private Statistics currentBucket;

    // The index of the current bucket, which is defined by getBucketIndex()
    private long currentBucketIndex;

    public TransactionStore() {
        this(Clock.systemUTC());
    }

    public TransactionStore(Clock clock) {
        this.clock = clock;
        currentBucket = new Statistics();
        currentBucketIndex = getBucketIndex(Instant.now(clock).toEpochMilli());
    }

    /**
     * Adds a transaction to the current statistics
     * @param transaction the transaction to add to the statistics
     * @throws InvalidTimestampException if transaction is from the future or too old
     */
    public void addTransaction(Transaction transaction) {
        validateTransactionTimestamp(transaction);

        final long index = getBucketIndex(transaction.getTimestamp());

        if (index == currentBucketIndex) {
            currentBucket.addTransaction(transaction);
        } else if (isInFutureBucket(transaction.getTimestamp())) {
            commitCurrentBucket(currentBucketIndex);
            currentBucketIndex = index;
            currentBucket.addTransaction(transaction);
        } else {
            addToStatisticsMap(transaction, index);
        }

    }

    /**
     * Gets the total statistics for the last {@link TransactionStore#STATISTICS_WINDOW_SECONDS} seconds
     * Total statistics exclude the transactions from the current second
     * Only transactions from the last {@link TransactionStore#STATISTICS_WINDOW_SECONDS} seconds up to the last
     * full second (like 13:45:24.000) are included
     * @param now the time when the statistics got requested
     * @return total statistics for all transactions from the past {@link TransactionStore#STATISTICS_WINDOW_SECONDS} seconds
     */
    public Statistics getStatistics(Instant now) {
        // Check if we need to add the current bucket to the statistics
        if (isInFutureBucket(now.toEpochMilli())) {
            commitCurrentBucket(currentBucketIndex);
        }

        final Statistics totalStats = new Statistics();
        for (final Statistics s : statisticsMap.values()) {
            final Instant lastTransactionTime = Instant.ofEpochMilli(s.getLastTimestamp());
            // Check if the statistics belong in the desired statistics time window
            if (ChronoUnit.SECONDS.between(lastTransactionTime, now) <= STATISTICS_WINDOW_SECONDS) {
                totalStats.addStatistics(s);
            }
        }

        return totalStats;
    }

    private void addToStatisticsMap(final Transaction transaction, final long interval) {
        Statistics stats = statisticsMap.get(interval);

        if (stats == null) {
            stats = new Statistics();
            statisticsMap.put(interval, stats);
        } else if (ChronoUnit.SECONDS.between(Instant.ofEpochMilli(stats.getLastTimestamp()),
                Instant.ofEpochMilli(transaction.getTimestamp())) > 1) {
            stats.reset();
        }

        stats.addTransaction(transaction);
    }

    private void commitCurrentBucket(final long interval) {
        if (currentBucket.getCount() > 0) {
            statisticsMap.put(interval, currentBucket.toBuilder().build());
            currentBucket.reset();
        }
    }

    private long getBucketIndex(final long timestamp) {
        return (timestamp / BUCKET_SIZE_MS) % STATISTICS_WINDOW_SECONDS;
    }

    private void validateTransactionTimestamp(final Transaction transaction) {
        final Instant now = Instant.now(clock);
        final Instant transactionTime = Instant.ofEpochMilli(transaction.getTimestamp());
        final Instant cutOffTime = now.minus(STATISTICS_WINDOW_SECONDS, ChronoUnit.SECONDS);

        if (transactionTime.isAfter(now)) {
            log.info("Discarding transaction from the future. Timestamp was {} and now is {}",
                    transaction.getTimestamp(), now.toEpochMilli());
            throw new InvalidTimestampException(
                    "Transaction timestamp '" + transactionTime.toString() + "' is in the future.");
        } else if (transactionTime.isBefore(cutOffTime)) {
            log.info("Discarding outdated transaction. Timestamp was {} and now is {}", transaction.getTimestamp(),
                    now.toEpochMilli());
            throw new InvalidTimestampException(
                    "Transaction timestamp '" + transactionTime.toString() + "' is older than 60 seconds.");
        }
    }

    private boolean isInFutureBucket(Long timestamp) {
        return timestamp / BUCKET_SIZE_MS > currentBucket.getLastTimestamp() / BUCKET_SIZE_MS;
    }

}
