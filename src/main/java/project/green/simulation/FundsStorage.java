package project.green.simulation;


import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@RequiredArgsConstructor
public class FundsStorage {
    private final Map<String, Double> database = new HashMap<>();

    public Boolean checkBalanceSufficient(String accountNr, Double amountRequested) {
        Double currentFunds = database.computeIfAbsent(accountNr, i -> 0d);

        return currentFunds >= amountRequested ? TRUE : FALSE;
    }

    public String printDatabase() {
        return database
            .entrySet()
            .stream()
            .map(e -> String.format("Account: %s, balance: %2$,.2f", e.getKey(), e.getValue()))
            .collect(Collectors.joining("\n"));
    }

    public void updateBalance(String accountNr, Double amount) {
        database.merge(accountNr, amount, Double::sum);
    }
}
