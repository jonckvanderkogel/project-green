package project.green.simulation;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.green.kafka.payments.PaymentEvent;
import project.green.repository.PaymentTransactionRepository;
import project.green.service.MessageProducerService;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.function.Supplier;

import static project.green.simulation.PaymentEventSupport.generatePaymentEvent;

@Slf4j
@RequiredArgsConstructor
public class SimulationService {
    private final MessageProducerService messageProducerService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private static final SplittableRandom RANDOM = new SplittableRandom();
    private static final FundsStorage FUNDS_STORAGE = new FundsStorage();

    private static final List<Tuple2<String, String>> CUSTOMERS = List.of(
            Tuple.of("NL24INGB8196349335", "Cleo Simonis"),
            Tuple.of("NL86INGB5693818835", "Abe Abshire"),
            Tuple.of("NL71INGB0382125101", "H. van Dijk"),
            Tuple.of("NL75INGB0794085193", "Allen Johnston"),
            Tuple.of("NL53INGB1464897916", "Norman Waters"),
            Tuple.of("NL94INGB9585178749", "Jarrod Gleichner"),
            Tuple.of("NL16INGB5893562010", "Zachery Hane III"),
            Tuple.of("NL08INGB5896083184", "Nana Mraz"),
            Tuple.of("NL30INGB3130212954", "Khalilah Hilpert"),
            Tuple.of("NL96INGB9223471630", "Ariel Braun")
    );

    private Disposable disposableSimulation;

    @PostConstruct
    public void init() {
        initalizeFundsStorage();
        log.info(FUNDS_STORAGE.printDatabase());
//        runSimulation();
    }

    public void start() {
        if (disposableSimulation.isDisposed()) {
            runSimulation();
        }
    }

    public void stop() {
        disposableSimulation.dispose();
    }

    public void generateAndSendPaymentEventForAccount(String account, int numberOfTransactions) {
        generatePaymentEventForAccount(account, numberOfTransactions)
                .flatMap(messageProducerService::sendMessage)
                .subscribe();
    }

    private Mono<Tuple2<String, String>> resolveAccount(String account) {
        Optional<Tuple2<String, String>> tupleMaybe = CUSTOMERS.stream()
                .filter(t -> t._1().equals(account))
                .findFirst();

        return tupleMaybe
                .map(Mono::just)
                .orElseGet(() -> Mono.error(new IllegalArgumentException("Account not present")));
    }

    private Flux<PaymentEvent> generatePaymentEventForAccount(String account, int numberOfTransactions) {
        return Flux.range(0, numberOfTransactions)
                .flatMap(unused -> resolveAccount(account))
                .map(t -> generatePaymentEvent(t._1(), t._2()))
                .map(event -> FUNDS_STORAGE.checkBalanceSufficient(event.getFromAccount(), event.getValue()) ? event : PaymentEventSupport.swapFromTo(event))
                .doOnNext(e -> updateFundsStorage(e::getFromAccount, e::getToAccount, e::getValue));
    }

    private void initalizeFundsStorage() {
        paymentTransactionRepository
                .findAll()
                .filter(pt -> CUSTOMERS.stream().anyMatch(t -> t._1().equals(pt.getPerspectiveAccount())))
                .doOnNext(pt -> updateFundsStorage(pt::getPerspectiveAccount, pt::getValue, pt.getPerspectiveAccount().equals(pt.getFromAccount())))
                .blockLast();
    }

    private void updateFundsStorage(Supplier<String> accountSupplier, Supplier<Long> valueSupplier, boolean debit) {
        FUNDS_STORAGE.updateBalance(accountSupplier.get(), debit ? valueSupplier.get() * -1 : valueSupplier.get());
    }

    private void runSimulation() {
        this.disposableSimulation = generatePaymentEvents()
                .flatMap(messageProducerService::sendMessage)
                .subscribe();
    }

    public Flux<PaymentEvent> generatePaymentEvents() {
        return Flux.interval(Duration.ofSeconds(5))
                .map(ignored -> CUSTOMERS.get(RANDOM.nextInt(0, CUSTOMERS.size())))
                .map(tuple -> generatePaymentEvent(tuple._1(), tuple._2()))
                .map(event -> FUNDS_STORAGE.checkBalanceSufficient(event.getFromAccount(), event.getValue()) ? event : PaymentEventSupport.swapFromTo(event))
                .doOnNext(e -> updateFundsStorage(e::getFromAccount, e::getToAccount, e::getValue));
    }

    private void updateFundsStorage(Supplier<String> fromSupplier, Supplier<String> toSupplier, Supplier<Long> valueSupplier) {
        FUNDS_STORAGE.updateBalance(toSupplier.get(), valueSupplier.get());
        FUNDS_STORAGE.updateBalance(fromSupplier.get(), valueSupplier.get() * -1);
    }
}
