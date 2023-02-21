package project.green.simulation;


import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@RequiredArgsConstructor
public class FundsStorage {
    private final Map<String, Long> database = new HashMap<>();

    public Boolean checkBalanceSufficient(String accountNr, Long amountRequested) {
        Long currentFunds = database.computeIfAbsent(accountNr, i -> 0L);

        return currentFunds >= amountRequested ? TRUE : FALSE;
    }

    public String printDatabase() {
        return database
            .entrySet()
            .stream()
            .map(e -> String.format("Account: %s, balance: %2$,.2f", e.getKey(), (double) e.getValue()/100))
            .collect(Collectors.joining("\n"));
    }

    public void updateBalance(String accountNr, Long amount) {
        database.merge(accountNr, amount, Long::sum);
    }
}
