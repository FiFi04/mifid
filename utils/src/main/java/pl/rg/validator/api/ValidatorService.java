package pl.rg.validator.api;

import java.util.Map;

public interface ValidatorService {
    Map<String, String> validateFields(Object object);

    Map<String, String> validateWithInnerFields(Object object);
}
