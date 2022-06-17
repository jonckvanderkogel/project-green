package project.green.kafka.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IBANValidator.class)
public @interface ValidIBAN {
    String message() default "Must be a valid IBAN";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
