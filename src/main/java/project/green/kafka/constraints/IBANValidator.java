package project.green.kafka.constraints;

import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IBANValidator implements ConstraintValidator<ValidIBAN, String> {
    private static final IBANCheckDigit IBANChecker = new IBANCheckDigit();

    @Override
    public void initialize(ValidIBAN constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return IBANChecker.isValid(value);
    }
}
