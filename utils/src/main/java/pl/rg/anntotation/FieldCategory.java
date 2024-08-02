package pl.rg.anntotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldCategory {

    boolean dbColumn() default false;

    boolean dbField() default false;

    boolean fetchField() default false;
}
