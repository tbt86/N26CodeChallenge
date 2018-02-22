package n26.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED) // protected for unit testing, shouldn't be used otherwise
@EqualsAndHashCode
@Builder(toBuilder = true)
@AllArgsConstructor
public class Statistics {

    private double max;
    private double min;
    private double avg;
    private double sum;
    private long count;
    private long lastTimestamp;

    public Statistics() {
        init();
    }

    private void init() {
        max = Double.MIN_VALUE;
        min = Double.MAX_VALUE;
        avg = 0;
        sum = 0;
        count = 0;
        lastTimestamp = 0;
    }

    public void reset() {
        init();
    }

    public void addTransaction(Transaction transaction) {
        lastTimestamp = Long.max(transaction.getTimestamp(), lastTimestamp);

        if (count == 0) {
            min = transaction.getAmount();
            max = transaction.getAmount();
        } else {
            min = Double.min(min, transaction.getAmount());
            max = Double.max(max, transaction.getAmount());
        }

        avg = (count * avg + transaction.getAmount()) / (count + 1);
        count++;
        sum += transaction.getAmount();
    }

    public void addStatistics(Statistics other) {
        if (other.getCount() <= 0) {
            return;
        }

        lastTimestamp = Long.max(other.getLastTimestamp(), lastTimestamp);
        min = Double.min(min, other.getMin());
        max = Double.max(max, other.getMax());

        sum += other.getSum();
        avg = (count * avg + other.getCount() * other.getAvg()) / (count + other.getCount());
        count += other.getCount();
    }

}
