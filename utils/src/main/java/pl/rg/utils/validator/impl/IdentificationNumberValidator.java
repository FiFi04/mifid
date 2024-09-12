package pl.rg.utils.validator.impl;

import pl.rg.utils.annotation.Validate;

import java.lang.reflect.Field;

public class IdentificationNumberValidator extends BaseValidator {
    protected static final int PESEL_LENGTH = 11;

    protected static final int NIP_LENGTH = 10;

    protected static final int REGON_LENGTH = 9;

    protected static final int CARD_NUMBER_LENGTH = 16;

    protected static final int[] NIP_WEIGHTS = {6, 5, 7, 2, 3, 4, 5, 6, 7};

    protected static final int[] REGON_WEIGHTS = {8, 9, 2, 3, 4, 5, 6, 7};

    protected static final int[] PESEL_WEIGHTS = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};

    protected static final int[] CARD_NUMBER_WEIGHTS = {2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1};

    @Override
    public boolean valid(String str, Field field) {
        boolean hasRightFormat = hasRightFormat(str, field.getAnnotation(Validate.class).maxTextLength());
        if (hasRightFormat) {
            int checksum = -1;
            if (str.length() == PESEL_LENGTH) {
                checksum = computeWeights(str, PESEL_WEIGHTS);
            } else if (str.length() == NIP_LENGTH) {
                checksum = computeWeights(str, NIP_WEIGHTS);
            } else if (str.length() == REGON_LENGTH) {
                checksum = computeWeights(str, REGON_WEIGHTS);
            } else if (str.length() == CARD_NUMBER_LENGTH) {
                checksum = computeWeights(str, CARD_NUMBER_WEIGHTS);
            }
            if (str.length() == CARD_NUMBER_LENGTH) {
                return checksum == 0;
            }
            return checksum == Character.getNumericValue(str.charAt(str.length() - 1));
        }
        return false;
    }
}
