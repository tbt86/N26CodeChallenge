package n26.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import n26.model.InvalidTimestampException;
import n26.model.Statistics;
import n26.model.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TransactionStoreTest {

    Clock mockedClock;

    Instant now;

    ThreadLocalRandom random = ThreadLocalRandom.current();

    TransactionStore transactionStore;

    @Before
    public void setup() {
        now = Instant.now();
        // Create a mocked clock so we can control time by setting now to any timestamp
        mockedClock = mock(Clock.class);
        Mockito.doAnswer((invocation) -> now).when(mockedClock).instant();

        transactionStore = new TransactionStore(mockedClock);
    }

    @Test
    public void testGetStatistics_noTransactionsAtAll() {
        Statistics statistics = transactionStore.getStatistics(Instant.now());

        assertThat(statistics.getCount()).isZero();
    }

    @Test
    public void testGetStatistics_noTransactionsInLastMinute() {
        transactionStore.addTransaction(new Transaction(now.toEpochMilli(), 5.0));

        advanceTimeBySeconds(61);
        Statistics statistics = transactionStore.getStatistics(now);

        assertThat(statistics.getCount()).isZero();
    }

    @Test
    public void testGetStatistics_noTransactionsForSomeIntervals() {
        int addedTransactions = 0;

        for (int i = 0; i < 60; i++) {
            if (i % 3 > 0) {
                addTransaction(transactionStore, i);
                addedTransactions++;
            }

            advanceTimeBySeconds(1);
        }

        Statistics statistics = transactionStore.getStatistics(now);

        verifyStats(addedTransactions, statistics, 1.0, 59.0);
    }

    @Test
    public void testGetStatistics_staleTransactionsForSomeIntervals() {
        int addedTransactions = 0;

        for (int i = 0; i < 60; i++) {
            addTransaction(transactionStore, i);
            advanceTimeBySeconds(1);
        }

        for (int i = 0; i < 60; i++) {
            if (i % 3 > 0) {
                addTransaction(transactionStore, i);
                addedTransactions++;
            }

            advanceTimeBySeconds(1);
        }

        Statistics statistics = transactionStore.getStatistics(now);

        verifyStats(addedTransactions, statistics, 1.0, 59.0);
    }

    @Test
    public void testGetStatistics_transactionsAvailableForAllIntervals() {
        for (int i = 0; i < 60; i++) {
            addTransaction(transactionStore, i);
            advanceTimeBySeconds(1);
        }

        Statistics statistics = transactionStore.getStatistics(now);

        verifyStats(60, statistics, 0.0, 59.0);
    }

    @Test
    public void testGetStatistics_multipleTransactionsPerInterval() {
        int addedTransactions = 0;

        for (int i = 0; i < 60; i++) {
            int transactionsThisSecond = random.nextInt(1, 10);
            for (int k = 0; k < transactionsThisSecond; k++) {
                addTransaction(transactionStore, i);
                addedTransactions++;
            }
            advanceTimeBySeconds(1);
        }

        Statistics statistics = transactionStore.getStatistics(now);

        verifyStats(addedTransactions, statistics, 0.0, 59.0);
    }

    @Test
    public void testGetStatistics_maxWasOneMinuteAgo() {
        transactionStore.addTransaction(new Transaction(now.toEpochMilli(), 1000.0));

        for (int i = 1; i < 60; i++) {
            addTransaction(transactionStore, i);

            advanceTimeBySeconds(1);
        }

        advanceTimeByMillis(999);

        Statistics statistics = transactionStore.getStatistics(now);
        verifyStats(60, statistics, 1.0, 1000.0);

        transactionStore.addTransaction(new Transaction(now.toEpochMilli(), 500.0));

        statistics = transactionStore.getStatistics(now);
        //still in currentBucket
        verifyStats(60, statistics, 1.0, 1000.0);
        advanceTimeBySeconds(1);
        statistics = transactionStore.getStatistics(now);
        //first two element replaced by currentBucket
        verifyStats(59, statistics, 2.0, 500);
    }

    @Test
    public void testGetStatistics_maxWasInCurrentSecond() {
        transactionStore.addTransaction(new Transaction(now.toEpochMilli(), 1000.0));

        for (int i = 1; i <= 60; i++) {
            addTransaction(transactionStore, i);

            advanceTimeBySeconds(1);
        }

        transactionStore.addTransaction(new Transaction(now.toEpochMilli(), 1500.0));

        Statistics statistics = transactionStore.getStatistics(now);
        // still in currentBucket
        verifyStats(61, statistics, 1.0, 1000);
        advanceTimeBySeconds(1);
        statistics = transactionStore.getStatistics(now);
        // currentBucket cleared
        verifyStats(60, statistics, 2.0, 1500);
    }

    @Test(expected = InvalidTimestampException.class)
    public void testAddTransaction_fromTheFuture() {
        Instant now = Instant.now();
        transactionStore.addTransaction(new Transaction(now.plus(1, ChronoUnit.SECONDS).toEpochMilli(), 5.0));
    }

    @Test(expected = InvalidTimestampException.class)
    public void testAddTransaction_olderThanOneMinute() {
        transactionStore.addTransaction(new Transaction(now.minus(61, ChronoUnit.SECONDS).toEpochMilli(), 5.0));
    }

    @Test
    public void testAddTransaction_correctTimestamp() {
        transactionStore.addTransaction(new Transaction(now.minus(1, ChronoUnit.SECONDS).toEpochMilli(), 5.0));
        Statistics statistics = transactionStore.getStatistics(now);
        verifyStats(1, statistics, 5.0, 5.0);
    }

    @Test
    public void testAddTransaction_outofOrderTimestamp() {
        transactionStore.addTransaction(new Transaction(now.minus(5, ChronoUnit.SECONDS).toEpochMilli(), 5.0));
        transactionStore.addTransaction(new Transaction(now.minus(1, ChronoUnit.SECONDS).toEpochMilli(), 2.0));
        advanceTimeBySeconds(5);
        transactionStore.addTransaction(new Transaction(now.minus(1, ChronoUnit.SECONDS).toEpochMilli(), 17.0));
        Statistics statistics = transactionStore.getStatistics(now);
        verifyStats(3, statistics, 2.0, 17.0);
    }

    private void verifyStats(final int expectedCount, final Statistics statistics, final double expectedMin,
            final double expectedMax) {
        assertThat(statistics.getCount()).isEqualTo(expectedCount);
        assertThat(statistics.getMin()).isEqualTo(expectedMin);
        assertThat(statistics.getMax()).isEqualTo(expectedMax);
        assertThat(Math.round(statistics.getSum())).isEqualTo(Math.round(statistics.getAvg() * statistics.getCount()));
    }

    private void advanceTimeBySeconds(final int amountToAdd) {
        now = now.plus(amountToAdd, ChronoUnit.SECONDS);
    }

    private void advanceTimeByMillis(final int amountToAdd) {
        now = now.plus(amountToAdd, ChronoUnit.MILLIS);
    }

    private void addTransaction(final TransactionStore transactionStore, final int i) {
        long randomOffsetMs = random.nextLong((now.toEpochMilli() / 1000) * 1000, now.toEpochMilli());
        transactionStore.addTransaction(new Transaction(randomOffsetMs, i));
    }

}
