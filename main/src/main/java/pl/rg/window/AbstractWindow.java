package pl.rg.window;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Order;
import pl.rg.utils.repository.paging.OrderType;
import pl.rg.utils.repository.paging.Page;
import pl.rg.window.emails.EmailWindowModel;
import pl.rg.window.users.UserWindowModel;

public abstract class AbstractWindow implements WindowUtils {

  public static int PAGE_SIZE = 10;

  public static int CURRENT_PAGES;

  public static HashMap<String, JButton> buttons = new HashMap<>();

  protected String[] buttonNames;

  protected List<ActionListener> actions;

  protected JTable mainTable;

  protected JPanel searchPanel;

  protected JComboBox<String> sortColumnComboBox;

  protected JComboBox<Integer> pageNumberComboBox;

  public AbstractWindow() {
    createActions();
  }

  protected Logger logger = LoggerImpl.getInstance();

  public abstract String[] getSearchColumns();

  public abstract String[] getColumnNames();

  protected abstract String[] getButtonNames();

  protected abstract void createActions();

  protected abstract DefaultTableModel getUpdatedTable(List<Filter> filters, Page page);

  protected DefaultTableModel loadData() {
    Page page = new Page();
    page.setFrom(0);
    page.setTo(AbstractWindow.PAGE_SIZE);
    page.setOrders(List.of(new Order("id", OrderType.ASC)));
    return getUpdatedTable(null, page);
  }

  public void refreshTable() {
    DefaultTableModel data = loadData();
    mainTable.setModel(data);
  }

  protected List<ActionListener> getActions() {
    return actions;
  }

  public ActionListener getMethodByAction(String action) {
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
    for (String column : getSearchColumns()) {
      searchPanel.add(new JLabel(column));
      searchPanel.add(column, new JTextField());
    }
    searchPanel.revalidate();
    searchPanel.repaint();
  }

  public void updateSortAndPage(JPanel sortPagePanel) {
    sortPagePanel.removeAll();
    JLabel sortLabel = new JLabel("Sortuj po kolumnie:");
    sortColumnComboBox = new JComboBox<>();
    JLabel pageLabel = new JLabel("Numer strony:");
    pageNumberComboBox = new JComboBox<>();
    for (String column : getColumnNames()) {
      sortColumnComboBox.addItem(column);
    }
    for (int i = 1; i <= CURRENT_PAGES; i++) {
      pageNumberComboBox.addItem(i);
    }
    sortPagePanel.add(sortLabel);
    sortPagePanel.add(sortColumnComboBox);
    sortPagePanel.add(Box.createHorizontalStrut(20));
    sortPagePanel.add(pageLabel);
    sortPagePanel.add(pageNumberComboBox);

    sortPagePanel.revalidate();
    sortPagePanel.repaint();
  }

  public void updateRightPanel(JPanel rightPanel) {
    rightPanel.removeAll();
    buttonNames = getButtonNames();
    Dimension buttonSize = new Dimension(120, 25);
    for (String button : buttonNames) {
      rightPanel.add(Box.createVerticalStrut(10));
      JButton jButton = new JButton(button);
      jButton.setMaximumSize(buttonSize);
      rightPanel.add(button, jButton);
      jButton.addActionListener(getMethodByAction(button));
      buttons.put(button, jButton);
      if (button.equals(UserWindowModel.UNBLOCK_BUTTON)) {
        jButton.setVisible(false);
      }
      if (button.equals(EmailWindowModel.RESEND_BUTTON)) {
        jButton.setVisible(false);
      }
    }

    rightPanel.revalidate();
    rightPanel.repaint();
  }
}