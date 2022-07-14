package project.green.simulation;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.green.kafka.payments.PaymentEvent;
import project.green.repository.PaymentTransactionRepository;
import project.green.service.MessageProducerService;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
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
        Tuple.of("NL71INGB0382125101", "Quinton Dicki Jr."),
        Tuple.of("NL75INGB0794085193", "Allen Johnston"),
        Tuple.of("NL53INGB1464897916", "Norman Waters"),
        Tuple.of("NL94INGB9585178749", "Jarrod Gleichner"),
        Tuple.of("NL16INGB5893562010", "Zachery Hane III"),
        Tuple.of("NL08INGB5896083184", "Nana Mraz"),
        Tuple.of("NL30INGB3130212954", "Khalilah Hilpert"),
        Tuple.of("NL96INGB9223471630", "Ariel Braun")
    );

    @PostConstruct
    public void init() {
        initalizeFundsStorage();
        log.info(FUNDS_STORAGE.printDatabase());
        runSimulation();
    }

    private void initalizeFundsStorage() {
        paymentTransactionRepository
            .findAll()
            .filter(pt -> CUSTOMERS.stream().anyMatch(t -> t._1().equals(pt.getPerspectiveAccount())))
            .doOnNext(pt -> updateFundsStorage(pt::getPerspectiveAccount, pt::getValue, pt.getPerspectiveAccount().equals(pt.getFromAccount())))
            .blockLast();
    }

    private void updateFundsStorage(Supplier<String> accountSupplier, Supplier<Double> valueSupplier, boolean debit) {
        FUNDS_STORAGE.updateBalance(accountSupplier.get(), debit ? valueSupplier.get() * -1 : valueSupplier.get());
    }

    private void runSimulation() {
        generatePaymentEvents()
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

    private void updateFundsStorage(Supplier<String> fromSupplier, Supplier<String> toSupplier, Supplier<Double> valueSupplier) {
        Tuple2<String, Integer> relevantAccountAndSign = determineRelevantAccountAndSign(fromSupplier, toSupplier);
        FUNDS_STORAGE.updateBalance(relevantAccountAndSign._1(), valueSupplier.get() * relevantAccountAndSign._2());
    }

    private Tuple2<String, Integer> determineRelevantAccountAndSign(Supplier<String> fromSupplier, Supplier<String> toSupplier) {
        return CUSTOMERS
            .stream()
            .filter(account -> account._1().equals(fromSupplier.get()))
            .map(t -> new Tuple2<>(t._1(), -1))
            .findFirst()
            .orElseGet(() -> new Tuple2<>(toSupplier.get(), 1));
    }
}
