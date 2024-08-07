package pl.rg.validator.impl;

import pl.rg.validator.api.Validator;

import java.util.Arrays;

public abstract class BaseValidator implements Validator {
    protected boolean hasRightFormat(String string, int lenght) {
        string = removeInvalidChars(string);
        return string.length() == lenght && containsOnlyDigits(string);
    }

    protected boolean containsOnlyDigits(String string) {
        return string.matches("[0-9]+");
    }

    private String removeInvalidChars(String string) {
        return string.replaceAll("[^0-9]", "");
    }

    protected int computeWeights(String string, int[] weights) {
        int sum = 0;
        int weightValue;
        for (int i = 0; i < weights.length; i++) {
            int value = Character.getNumericValue(string.charAt(i));
            if (string.length() == IdentificationNumberValidator.CARD_NUMBER_LENGTH) {
                if ((weightValue = value * weights[i]) > 9) {
                    String[] weightSplit = String.valueOf(weightValue).split("");
                    sum += Arrays.stream(weightSplit)
                            .mapToInt(Integer::valueOf)
                            .sum();
                } else {
                    sum += value * weights[i];
                }
            } else {
                sum += value * weights[i];
            }
        }
        int lastNumber;
        if (string.length() == IdentificationNumberValidator.PESEL_LENGTH) {
            lastNumber = sum % 10;
            return lastNumber == 0 ? 0 : 10 - lastNumber;
        } else if (string.length() == IdentificationNumberValidator.NIP_LENGTH) {
            return sum % 11;
        } else if (string.length() == IdentificationNumberValidator.CARD_NUMBER_LENGTH) {
            return sum % 10;
        } else {
            lastNumber = sum % 11;
            return lastNumber == 10 ? 0 : lastNumber;
        }
    }
}
