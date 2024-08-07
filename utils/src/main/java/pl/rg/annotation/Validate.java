package pl.rg.annotation;

import pl.rg.validator.enums.ValidatorCase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {

    ValidatorCase validatorCase();

    String message();

    String format() default ".*";

    int maxTextLength() default 30;
}
