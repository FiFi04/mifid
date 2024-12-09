package pl.rg.window;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.exception.ValidationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.filter.FilterSearchType;
import pl.rg.window.users.UserColumn;

public interface WindowUtils {

  Logger loggerInstance = LoggerImpl.getInstance();

  default String getTextFieldValue(JPanel panel, String labelName) {
    for (int i = 0; i < panel.getComponentCount(); i++) {
      if (panel.getComponent(i) instanceof JLabel label && label.getText().equals(labelName)) {
        if (i + 1 < panel.getComponentCount() && panel.getComponent(
            i + 1) instanceof JTextField textField) {
          return textField.getText().trim();
        }
      }
    }
    throw loggerInstance.logAndThrowRuntimeException(LogLevel.DEBUG,
        new ApplicationException("M26BP", "Brak pola tekstowego dla: " + labelName));
  }

  default void showValidationMessage(ValidationException e) {
    String[] messageSplited = e.getMessage().split(":");
    Map<String, String> constraintsMap = e.getConstraintsMap();
    String message = constraintsMap.entrySet().stream().map(c -> {
      String nameColumnName = DataEnumColumn.getNameByJavaAttribute(c.getKey(),
          UserColumn.values());
      return "Pole " + nameColumnName + ": " + c.getValue();
    }).collect(Collectors.joining("\n"));

    JOptionPane.showMessageDialog(new JFrame(), message, messageSplited[0],
        JOptionPane.WARNING_MESSAGE);
  }

  default HashMap<String, String> getFieldsValues(JPanel searchPanel, AbstractWindow window) {
    return Arrays.stream(window.getColumnNames())
        .collect(Collectors.toMap(
            columnName -> DataEnumColumn.getDbColumnByName(columnName, UserColumn.values()).get(),
            columnName -> getTextFieldValue(searchPanel, columnName),
            (existing, newValue) -> existing,
            HashMap::new
        ));
  }

  default List<Filter> getFilters(HashMap<String, String> fieldValues) {
    return fieldValues.entrySet().stream()
        .filter(textField -> !textField.getValue().isBlank())
        .map(textField -> new Filter(textField.getKey(), new Object[]{textField.getValue()},
            FilterSearchType.MATCH))
        .toList();
  }
}