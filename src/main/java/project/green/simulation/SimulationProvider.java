package project.green.simulation;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.springframework.stereotype.Component;
import project.green.kafka.payments.PaymentEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.SplittableRandom;

import static project.green.simulation.PaymentEventSupport.generatePaymentEvent;

@Component
public class SimulationProvider {
    private static final SplittableRandom RANDOM = new SplittableRandom();

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

    public Flux<PaymentEvent> generatePaymentEvents() {
        return Flux.interval(Duration.ofSeconds(5))
            .map(ignored -> CUSTOMERS.get(RANDOM.nextInt(0, CUSTOMERS.size())))
            .map(tuple -> generatePaymentEvent(tuple._1(), tuple._2()));
    }
}
