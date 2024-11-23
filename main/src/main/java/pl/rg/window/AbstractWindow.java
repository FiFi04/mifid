package pl.rg.window;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

public abstract class AbstractWindow {

  public AbstractWindow() {
    createActions();
  }

  protected Logger logger = LoggerImpl.getInstance();

  protected abstract String[] getColumnNames();

  protected abstract String[] getButtonNames();

  protected abstract void createActions();

  protected abstract List<ActionListener> getActions();

  public ActionListener getMethodByAction(String action) {
    String[] buttonNames = getButtonNames();
    for (int i = 0; i < buttonNames.length; i++) {
      if (buttonNames[i].equals(action)) {
        return getActions().get(i);
      }
    }

    throw logger.logAndThrowRuntimeException(LogLevel.DEBUG,
        new ApplicationException("M45FV", "Nie znaleziono akcji dla podanego przycisku"));
  }

  public void updateSearchPanel(JPanel searchPanel) {
    searchPanel.removeAll();

    for (String column : getColumnNames()) {
      searchPanel.add(new JLabel(column));
      searchPanel.add(column, new JTextField());
    }
    searchPanel.revalidate();
    searchPanel.repaint();
  }

  public void updateSortAndPage(JComboBox<String> sortColumnComboBox,
      JComboBox<Integer> pageNumberComboBox) {
    sortColumnComboBox.removeAllItems();
    for (String column : getColumnNames()) {
      sortColumnComboBox.addItem(column);
    }

    // todo pages from repo
    int page = 20;
    for (int i = 1; i <= page; i++) {
      pageNumberComboBox.addItem(i);
    }
  }

  public void updateRightPanel(JPanel rightPanel) {
    rightPanel.removeAll();
    Dimension buttonSize = new Dimension(120, 25);
    for (String button : getButtonNames()) {
      rightPanel.add(Box.createVerticalStrut(10));
      JButton jButton = new JButton(button);
      jButton.setMaximumSize(buttonSize);
      rightPanel.add(button, jButton);
      jButton.addActionListener(getMethodByAction(button));
    }

    rightPanel.revalidate();
    rightPanel.repaint();
  }
}
