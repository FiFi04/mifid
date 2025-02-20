package pl.rg.window;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.exception.ValidationException;
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

  protected Map<String, JButton> buttons = new HashMap<>();

  protected List<ActionListener> actions;

  protected JTable mainTable;

  protected JPanel searchPanel;

  protected JComboBox<String> sortColumnComboBox;

  protected JComboBox<Integer> pageNumberComboBox;

  protected Logger logger = LoggerImpl.getInstance();

  public abstract String[] getSearchColumns();

  public abstract String[] getColumnNames();

  protected abstract String[] getButtonNames();

  public abstract void addSortAndPageActions();

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

  public void changeButtonVisibility(String buttonName, boolean visibility) {
    buttons.get(buttonName).setVisible(visibility);
  }

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
    Dimension buttonSize = new Dimension(120, 25);
    for (String button : getButtonNames()) {
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

  public void showValidationMessage(ValidationException e, Function<String, String> nameResolver) {
    String[] messageSplited = e.getMessage().split(":");
    Map<String, String> constraintsMap = e.getConstraintsMap();
    String message = constraintsMap.entrySet().stream()
        .map(c -> "Pole " + nameResolver.apply(c.getKey()) + ": " + c.getValue())
        .collect(Collectors.joining("\n"));
    JOptionPane.showMessageDialog(new JFrame(), message, messageSplited[0],
        JOptionPane.WARNING_MESSAGE);
  }

  public HashMap<String, String> getFieldsValues(JPanel searchPanel, AbstractWindow window,
      Function<String, String> nameResolver) {
    return Arrays.stream(window.getSearchColumns())
        .collect(Collectors.toMap(
            nameResolver::apply,
            columnName -> getTextFieldValue(searchPanel, columnName),
            (existing, newValue) -> existing,
            HashMap::new
        ));
  }

  public Page getPage(Function<String, String> nameResolver) {
    String sortColumn = (String) sortColumnComboBox.getSelectedItem();
    int pageNumber = (int) pageNumberComboBox.getSelectedItem();
    Page page = new Page();
    page.setFrom((pageNumber - 1) * AbstractWindow.PAGE_SIZE);
    page.setTo(pageNumber * AbstractWindow.PAGE_SIZE);
    page.setOrders(
        List.of(new Order(nameResolver.apply(sortColumn), OrderType.ASC)));
    return page;
  }
}