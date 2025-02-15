package pl.rg.window.users;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import lombok.Getter;
import pl.rg.users.UserDto;
import pl.rg.users.UserModuleController;
import pl.rg.users.model.UserModel;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Order;
import pl.rg.utils.repository.paging.OrderType;
import pl.rg.utils.repository.paging.Page;
import pl.rg.window.AbstractWindow;
import pl.rg.window.DataEnumColumn;

@Getter
public class UserWindowModel extends AbstractWindow {

  public static final String UNBLOCK_BUTTON = "Odblokuj";

  private static final String[] buttonNames = {"Dodaj", "Edytuj", "Usuń", "Szukaj",
      "Resetuj hasło", UNBLOCK_BUTTON};

  private List<ActionListener> actions;

  private JTable mainTable;

  private UserModuleController userModuleController;

  private JPanel searchPanel;

  private JComboBox<String> sortColumnComboBox;

  private JComboBox<Integer> pageNumberComboBox;

  private Object[] options = {"Tak", "Nie"};

  public UserWindowModel(JTable mainTable, UserModuleController userModuleController,
      JPanel searchPanel, JComboBox<String> sortColumnComboBox,
      JComboBox<Integer> pageNumberComboBox) {
    this.mainTable = mainTable;
    this.userModuleController = userModuleController;
    this.searchPanel = searchPanel;
    this.sortColumnComboBox = sortColumnComboBox;
    this.pageNumberComboBox = pageNumberComboBox;
    addTableSelectionListener();
  }

  public DefaultTableModel loadUserData() {
    Page page = new Page();
    page.setFrom(0);
    page.setTo(AbstractWindow.PAGE_SIZE);
    page.setOrders(List.of(new Order(UserModel.ID, OrderType.ASC)));
    updateSortAndPage(sortColumnComboBox, pageNumberComboBox);
    return getUpdatedTable(null, page);
  }

  public void refreshTable() {
    DefaultTableModel userData = loadUserData();
    mainTable.setModel(userData);
  }

  @Override
  public String[] getButtonNames() {
    return buttonNames;
  }

  @Override
  protected void createActions() {
    actions = new ArrayList<>();

    ActionListener addAction = e -> {
      UserWindow userWindow = new UserWindow(this, false, null);
      userWindow.setVisible(true);
    };

    ActionListener editAction = e -> {
      int selectedRow = mainTable.getSelectedRow();
      if (selectedRow < 0) {
        JOptionPane.showMessageDialog(new JFrame(), "Nie wybrano żadnego użytkownika do edycji");
        return;
      }
      int id = (int) mainTable.getValueAt(selectedRow, 0);
      UserWindow userWindow = new UserWindow(this, true, id);
      userWindow.setVisible(true);
    };

    ActionListener deleteAction = e -> {
      int selectedRow = mainTable.getSelectedRow();
      if (selectedRow < 0) {
        JOptionPane.showMessageDialog(new JFrame(), "Nie wybrano żadnego użytkownika do edycji");
        return;
      }

      int option = getOptionFromOptionDialog("Czy na pewno chcesz usunąć wybranego użytkownika?",
          "Usunięcie użytkownika");
      if (option == JOptionPane.YES_OPTION) {
        Integer id = (Integer) mainTable.getValueAt(selectedRow, 0);
        userModuleController.deleteUser(id);
        refreshTable();
      }
    };

    ActionListener searchAction = e -> {
      updateTable();
    };

    ActionListener resetPasswordAction = e -> {
    };

    ActionListener unblockUser = e -> {
      int selectedRow = mainTable.getSelectedRow();
      int option = getOptionFromOptionDialog(
          "Czy na pewno chcesz odblokować wybranego użytkownika?", "Odbkolowanie użytkownika");
      if (option == JOptionPane.YES_OPTION) {
        Integer id = (Integer) mainTable.getValueAt(selectedRow, 0);
        String userName = userModuleController.getUser(id).get().getUserName();
        getUserModuleController().resetLoginAttempts(userName);
        refreshTable();
      }
    };

    actions.add(addAction);
    actions.add(editAction);
    actions.add(deleteAction);
    actions.add(searchAction);
    actions.add(resetPasswordAction);
    actions.add(unblockUser);

    if (actions.size() != buttonNames.length) {
      throw logger.logAndThrowRuntimeException(LogLevel.DEBUG,
          new ApplicationException("M23SD", "Brak implementacji wszystkich akcji!"));
    }
  }

  public void addSortAndPageActions() {
    this.sortColumnComboBox.addActionListener(e -> {
      updateTable();
    });

    this.pageNumberComboBox.addActionListener(e -> {
      updateTable();
    });
  }

  @Override
  protected List<ActionListener> getActions() {
    return actions;
  }

  @Override
  public String[] getColumnNames() {
    return DataEnumColumn.getColumnNames(UserColumn.values());
  }

  private void updateTable() {
    HashMap<String, String> fieldValues = getFieldsValues(searchPanel, this);
    List<Filter> filters = getFilters(fieldValues);
    String sortColumn = (String) sortColumnComboBox.getSelectedItem();
    int pageNumber = (int) pageNumberComboBox.getSelectedItem();
    Page page = new Page();
    page.setFrom((pageNumber - 1) * AbstractWindow.PAGE_SIZE);
    page.setTo(pageNumber * AbstractWindow.PAGE_SIZE);
    page.setOrders(
        List.of(new Order(DataEnumColumn.getDbColumnByName(sortColumn, UserColumn.values()).get(),
            OrderType.ASC)));
    DefaultTableModel tableUpdate = getUpdatedTable(filters, page);
    mainTable.setModel(tableUpdate);
  }

  private DefaultTableModel getUpdatedTable(List<Filter> filters, Page page) {
    DefaultTableModel tableModel = new DefaultTableModel(getColumnNames(), 0);
    tableModel.setRowCount(0);
    MifidPage<UserDto> mifidPage = userModuleController.getPage(filters, page);
    CURRENT_PAGES = mifidPage.getTotalPage();
    List<UserDto> usersData = mifidPage.getLimitedObjects();
    for (UserDto user : usersData) {
      tableModel.addRow(new Object[]{
          user.getId(),
          user.getUserName(),
          user.getFirstName(),
          user.getLastName(),
          user.getEmail(),
          user.getBlocked()
      });
    }
    return tableModel;
  }

  private void addTableSelectionListener() {
    mainTable.getSelectionModel().addListSelectionListener(e -> {
      if (mainTable.getSelectedRow() != -1) {
        updateUnblockButtonVisibility();
      }
    });
  }

  private void updateUnblockButtonVisibility() {
    int selectedRow = mainTable.getSelectedRow();
    String blockedStatus = (String) mainTable.getValueAt(selectedRow, 5);
    buttons.get(UNBLOCK_BUTTON).setVisible("Tak".equalsIgnoreCase(blockedStatus));
  }
}