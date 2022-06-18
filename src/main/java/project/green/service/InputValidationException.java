package project.green.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class InputValidationException extends RuntimeException {
    private final String message;
}
