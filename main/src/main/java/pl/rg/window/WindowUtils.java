package pl.rg.window;

import java.util.HashMap;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.filter.FilterSearchType;

public interface WindowUtils {

  Logger loggerInstance = LoggerImpl.getInstance();

  Object[] options = {"Tak", "Nie"};

  default int getOptionFromOptionDialog(String message, String title) {
    return JOptionPane.showOptionDialog(
        new JFrame(),
        message,
        title,
        JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[1]
    );
  }

  default String getTextFieldValue(JPanel panel, String labelName) {
    for (int i = 0; i < panel.getComponentCount(); i++) {
      if (panel.getComponent(i) instanceof JLabel label && label.getText().equals(labelName)) {
        if (i + 1 < panel.getComponentCount() && panel.getComponent(
            i + 1) instanceof JTextComponent textField) {
          return textField.getText().trim();
        }
      }
    }
    throw loggerInstance.logAndThrowRuntimeException(LogLevel.DEBUG,
        new ApplicationException("M26BP", "Brak pola tekstowego dla: " + labelName));
  }

  default List<Filter> getFilters(HashMap<String, String> fieldValues) {
    return fieldValues.entrySet().stream()
        .filter(textField -> !textField.getValue().isBlank())
        .map(textField -> new Filter(textField.getKey(), new Object[]{textField.getValue()},
            FilterSearchType.MATCH))
        .toList();
  }
}