package pl.rg.utils.validator.impl;

import pl.rg.utils.enums.DocumentType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DocumentNumberValidator extends BaseValidator {
    protected static final int[] ID_DOCUMENT_WEIGHTS = {7, 3, 1, 7, 3, 1, 7, 3};

    private static final String ID_CARD_REGEX = "[A-Z]{3}\\d{6}";

    private static final String PASSPORT_REGEX = "[A-Z]{2}\\d{7}";

    @Override
    public boolean valid(String value, Field field) {
        DocumentType documentType = null;
        if (value.matches(ID_CARD_REGEX)) {
            documentType = null;
        } else if (value.matches(PASSPORT_REGEX)) {
            documentType = null;
        }
        if (documentType == null) {
            return false;
        }
        switch (documentType) {
            case ID_CARD -> {
                if (!value.matches(ID_CARD_REGEX) ||
                        !isIdDocControlNumberCorrect(value, ID_DOCUMENT_WEIGHTS)) {
                    return false;
                }
            }
            case PASSPORT -> {
                if (!value.matches(PASSPORT_REGEX) ||
                        !isIdDocControlNumberCorrect(value, ID_DOCUMENT_WEIGHTS)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isIdDocControlNumberCorrect(String docNumber, int[] weights) {
        List<String> personalDocumentValues = Arrays.asList(docNumber.split(""));
        String docNumberWithoutCtrlNum = "";
        int controlNumberIndex;
        boolean isIdCard = docNumber.matches(ID_CARD_REGEX);
        if (isIdCard) {
            controlNumberIndex = 3;
            docNumberWithoutCtrlNum = docNumber.substring(0, 3) + docNumber.substring(4);
        } else {
            controlNumberIndex = 2;
            docNumberWithoutCtrlNum = docNumber.substring(0, 2) + docNumber.substring(3);
        }
        char[] charArray = docNumberWithoutCtrlNum.toCharArray();
        List<Integer> controlNumbers = new ArrayList<>();
        for (int i = 0; i < charArray.length; i++) {
            controlNumbers.add(Character.getNumericValue(charArray[i]) * weights[i]);
        }
        int sum = controlNumbers.stream()
                .mapToInt(Integer::intValue)
                .sum();
        return sum % 10 == Integer.parseInt(personalDocumentValues.get(controlNumberIndex));
    }
}
