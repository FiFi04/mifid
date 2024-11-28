package pl.rg.window.users;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import lombok.Getter;
import pl.rg.users.UserDto;
import pl.rg.users.UserModuleController;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.window.AbstractWindow;

@Getter
public class UserWindowModel extends AbstractWindow {

  private static final String[] buttonNames = {"Dodaj", "Edytuj", "Usuń", "Szukaj",
      "Resetuj hasło"};

  private List<ActionListener> actions;

  private JTable mainTable;

  private UserModuleController userModuleController;

  public UserWindowModel(JTable mainTable, UserModuleController userModuleController) {
    this.mainTable = mainTable;
    this.userModuleController = userModuleController;
  }

  public DefaultTableModel loadUserData() { // todo replace after implement search users method
    DefaultTableModel tableModel = new DefaultTableModel(getColumnNames(), 0);
    tableModel.setRowCount(0);
    List<UserDto> userData = userModuleController.getFiltered(null);
    for (UserDto user : userData) {
      tableModel.addRow(new Object[]{
          user.getId(),
          user.getUserName(),
          user.getFirstName(),
          user.getLastName(),
          user.getEmail()
      });
    }
//    Object[][] userData = { // todo replace after implement search users method
//        {1, "jankow", "Jan", "Kowalski", "jan.kowalski@example.com"},
//        {2, "annnow", "Anna", "Nowak", "anna.nowak@example.com"},
//        {3, "piowis", "Piotr", "Wiśniewski", "piotr.wisniewski@example.com"}
//    };
//    for (Object[] row : userData) {
//      tableModel.addRow(row);
//    }
    return tableModel;
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
      }
      int id = (int) mainTable.getValueAt(selectedRow, 0);
      UserWindow userWindow = new UserWindow(this, true, id);
      userWindow.setVisible(true);
    };

    ActionListener deleteAction = e -> {
      int selectedRow = mainTable.getSelectedRow();
      Integer id = (Integer) mainTable.getValueAt(selectedRow, 0);
      userModuleController.deleteUser(id);
      refreshTable();
    };

    ActionListener searchAction = e -> {
      // todo
    };

    ActionListener resetPasswordAction = e -> {
      // todo
    };

    actions.add(addAction);
    actions.add(editAction);
    actions.add(deleteAction);
    actions.add(searchAction);
    actions.add(resetPasswordAction);

    if (actions.size() != buttonNames.length) {
      throw logger.logAndThrowRuntimeException(LogLevel.DEBUG,
          new ApplicationException("M23SD", "Brak implementacji wszystkich akcji!"));
    }
  }

  @Override
  protected List<ActionListener> getActions() {
    return actions;
  }

  @Override
  public String[] getColumnNames() {
    return UserColumn.getColumnNames();
  }
}
