package n26.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Random;
import org.junit.Test;

public class StatisticsTest {

    Random random = new Random();

    @Test
    public void testStatisticsInitialization() {
        Statistics statisticsToTest = new Statistics();
        Statistics expected = new Statistics();

        expected.setMin(Double.MAX_VALUE);
        expected.setMax(Double.MIN_VALUE);
        expected.setAvg(0);
        expected.setCount(0);
        expected.setSum(0);
        expected.setLastTimestamp(0);

        assertThat(statisticsToTest).isEqualTo(expected);
    }

    @Test
    public void testAddStatistics_twoNonEmptyStatistics() {
        Statistics firstStats = createRandomizedStats();
        Statistics secondStats = createRandomizedStats();

        Statistics initialStats = firstStats.toBuilder().build();
        firstStats.addStatistics(secondStats);

        verifyStatsAddition(initialStats, secondStats, firstStats);
    }

    @Test
    public void testAddStatistics_twoEmptyStatistics() {
        Statistics emptyStats = new Statistics();
        Statistics someOtherEmptyStats = new Statistics();

        emptyStats.addStatistics(someOtherEmptyStats);
        assertThat(emptyStats).isEqualTo(someOtherEmptyStats);
    }

    @Test
    public void testAddStatistics_firstStatisticsEmpty() {
        Statistics emptyStats = new Statistics();
        Statistics secondStats = createRandomizedStats();

        Statistics initialStats = emptyStats.toBuilder().build();
        emptyStats.addStatistics(secondStats);

        verifyStatsAddition(initialStats, secondStats, emptyStats);
    }

    @Test
    public void testAddStatistics_SecondStatisticsEmpty() {
        Statistics firstStats = createRandomizedStats();
        Statistics emptyStats = new Statistics();

        Statistics initialStats = firstStats.toBuilder().build();
        firstStats.addStatistics(emptyStats);

        verifyStatsAddition(initialStats, emptyStats, firstStats);
    }

    @Test
    public void testAddTransaction_statisticsEmpty() {
        Statistics emptyStats = new Statistics();
        Transaction transaction = createCurrentTransaction();

        Statistics initialStats = emptyStats.toBuilder().build();
        emptyStats.addTransaction(transaction);

        verifyTransactionAddition(initialStats, transaction, emptyStats);
    }

    @Test
    public void testAddTransaction_statisticsNonEmpty() {
        Statistics nonEmptyStats = createRandomizedStats();
        Transaction transaction = createCurrentTransaction();

        Statistics initialStats = nonEmptyStats.toBuilder().build();
        nonEmptyStats.addTransaction(transaction);

        verifyTransactionAddition(initialStats, transaction, nonEmptyStats);
    }

    @Test
    public void testAddTransaction_transactionIsNotLatest() {
        Statistics nonEmptyStats = createRandomizedStats();

        Transaction transaction = createCurrentTransaction();
        nonEmptyStats.setLastTimestamp(transaction.getTimestamp() + 1);

        Statistics initialStats = nonEmptyStats.toBuilder().build();
        nonEmptyStats.addTransaction(transaction);

        verifyTransactionAddition(initialStats, transaction, nonEmptyStats);
    }

    @Test
    public void testAddTransaction_transactionAmountIsNegative() {
        Statistics nonEmptyStats = createRandomizedStats();
        Transaction transaction = new Transaction(Instant.now().toEpochMilli(), -2.4);

        Statistics initialStats = nonEmptyStats.toBuilder().build();
        nonEmptyStats.addTransaction(transaction);

        verifyTransactionAddition(initialStats, transaction, nonEmptyStats);
    }

    private Transaction createCurrentTransaction() {
        return new Transaction(Instant.now().toEpochMilli(), random.nextDouble());
    }

    private Statistics createRandomizedStats() {
        Statistics randomStats = new Statistics();

        randomStats.setMin(random.nextDouble());
        randomStats.setMax(random.nextDouble());
        randomStats.setAvg(random.nextDouble());
        randomStats.setCount(Math.abs(random.nextLong()));
        randomStats.setSum(random.nextDouble());
        randomStats.setLastTimestamp(Math.abs(random.nextLong()));

        return randomStats;
    }

    private void verifyStatsAddition(Statistics firstStats, Statistics secondStats, Statistics sum) {
        assertThat(sum.getLastTimestamp())
                .isEqualTo(Long.max(firstStats.getLastTimestamp(), secondStats.getLastTimestamp()));
        assertThat(sum.getMax()).isEqualTo(Double.max(firstStats.getMax(), secondStats.getMax()));
        assertThat(sum.getMin()).isEqualTo(Double.min(firstStats.getMin(), secondStats.getMin()));
        assertThat(sum.getSum()).isEqualTo(firstStats.getSum() + secondStats.getSum());
        assertThat(sum.getCount()).isEqualTo(firstStats.getCount() + secondStats.getCount());
        assertThat(sum.getAvg()).isEqualTo(
                (firstStats.getCount() * firstStats.getAvg() + secondStats.getCount() * secondStats.getAvg()) / (
                        firstStats.getCount() + secondStats.getCount()));
    }

    private void verifyTransactionAddition(Statistics originalStats, Transaction transaction, Statistics updatedStats) {
        assertThat(updatedStats.getLastTimestamp())
                .isEqualTo(Long.max(originalStats.getLastTimestamp(), transaction.getTimestamp()));
        assertThat(updatedStats.getMax()).isEqualTo(Double.max(originalStats.getMax(), transaction.getAmount()));
        assertThat(updatedStats.getMin()).isEqualTo(Double.min(originalStats.getMin(), transaction.getAmount()));
        assertThat(updatedStats.getSum()).isEqualTo(originalStats.getSum() + transaction.getAmount());
        assertThat(updatedStats.getCount()).isEqualTo(originalStats.getCount() + 1);
        assertThat(updatedStats.getAvg()).isEqualTo(
                (originalStats.getCount() * originalStats.getAvg() + transaction.getAmount()) / (
                        originalStats.getCount() + 1));
    }

}
