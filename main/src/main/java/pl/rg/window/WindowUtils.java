package pl.rg.window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

public interface WindowUtils {

  Logger logger = LoggerImpl.getInstance();

  default String getTextFieldValue(JPanel panel, String labelName) {
    for (int i = 0; i < panel.getComponentCount(); i++) {
      if (panel.getComponent(i) instanceof JLabel label && label.getText().equals(labelName)) {
        if (i + 1 < panel.getComponentCount() && panel.getComponent(
            i + 1) instanceof JTextField textField) {
          return textField.getText().trim();
        }
      }
    }
    throw logger.logAndThrowRuntimeException(LogLevel.DEBUG,
        new ApplicationException("M26BP", "Brak pola tekstowego dla: " + labelName));
  }
}
