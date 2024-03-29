package project.green.service;

import reactor.core.publisher.Mono;

import javax.validation.Validation;
import javax.validation.Validator;

import static java.util.stream.Collectors.joining;

public class ValidationService {
    private final static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> Mono<T> validate(T arg) {
        var validationResult = VALIDATOR.validate(arg);

        return validationResult.isEmpty() ? Mono.just(arg) : Mono.error(
            new InputValidationException(
                validationResult
                    .stream()
                    .map(r -> String.format("Field %s, constraint: %s, actual value: %s.", r.getPropertyPath().toString(), r.getMessage(), r.getInvalidValue()))
                    .collect(joining(" "))
            )
        );
    }
}
