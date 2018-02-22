package n26.model;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

@RequiredArgsConstructor
@Getter
public class Transaction {

    private final long timestamp;
    private final double amount;

    @Override
    public String toString() {
        return String.format("{'amount':{},'timestamp':{}}", amount, timestamp);
    }
}
