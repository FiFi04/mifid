package pl.rg.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import pl.rg.utils.validator.enums.PossibleNull;
import pl.rg.utils.validator.enums.ValidatorCase;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validate {

  ValidatorCase validatorCase();

  String message();

  String format() default ".*";

  int maxTextLength() default 45;

  PossibleNull possibleNull() default PossibleNull.NO;
}
