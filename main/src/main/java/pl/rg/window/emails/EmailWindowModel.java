package pl.rg.window.emails;

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
import pl.rg.EmailDto;
import pl.rg.EmailModuleController;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;
import pl.rg.window.AbstractWindow;

@Getter
public class EmailWindowModel extends AbstractWindow {

  public static final String RESEND_BUTTON = "Wyślij ponownie";

  private final String[] buttonNames = {"Wyślij nowego maila", "Szablony", "Szukaj",
      RESEND_BUTTON};

  private EmailModuleController emailModuleController;

  public EmailWindowModel(JTable mainTable, EmailModuleController emailModuleController,
      JPanel searchPanel, JComboBox<String> sortColumnComboBox,
      JComboBox<Integer> pageNumberComboBox) {
    this.mainTable = mainTable;
    this.emailModuleController = emailModuleController;
    this.searchPanel = searchPanel;
    this.sortColumnComboBox = sortColumnComboBox;
    this.pageNumberComboBox = pageNumberComboBox;
    createActions();
  }

  @Override
  public String[] getSearchColumns() {
    return EmailColumn.getSearchColumns();
  }

  @Override
  public String[] getColumnNames() {
    return EmailColumn.getColumnNames();
  }

  @Override
  protected String[] getButtonNames() {
    return buttonNames;
  }

  @Override
  protected void createActions() {
    actions = new ArrayList<>();

    ActionListener sendAction = e -> {
      EmailWindow emailWindow = new EmailWindow(this);
      emailWindow.setVisible(true);
    };

    ActionListener resendAction = e -> {
      int selectedRow = mainTable.getSelectedRow();
      if (selectedRow < 0) {
        JOptionPane.showMessageDialog(new JFrame(),
            "Nie wybrano żadnego emaila do ponownego wysłania");
        return;
      }
      int id = (int) mainTable.getValueAt(selectedRow, 0);
      emailModuleController.resendEmail(id);
      JOptionPane.showMessageDialog(new JFrame(), "Email ponownie wysłany");
    };

    ActionListener searchAction = e -> {
      updateTable();
    };

    ActionListener templatesAction = e -> {
      TemplateWindow templateWindow = new TemplateWindow(this);
      templateWindow.setVisible(true);
    };

    actions.add(sendAction);
    actions.add(templatesAction);
    actions.add(searchAction);
    actions.add(resendAction);

    if (actions.size() != buttonNames.length) {
      throw logger.logAndThrowRuntimeException(LogLevel.DEBUG,
          new ApplicationException("M23SD", "Brak implementacji wszystkich akcji!"));
    }
  }

  @Override
  public void addSortAndPageActions() {
    this.sortColumnComboBox.addActionListener(e -> {
      updateTable();
    });

    this.pageNumberComboBox.addActionListener(e -> {
      updateTable();
    });
  }

  private void updateTable() {
    HashMap<String, String> fieldValues = getFieldsValues(searchPanel, this,
        EmailColumn::getDbColumnByName);
    List<Filter> filters = getFilters(fieldValues);
    Page page = getPage(EmailColumn::getDbColumnByName);
    DefaultTableModel tableUpdate = getUpdatedTable(filters, page);
    mainTable.setModel(tableUpdate);
  }

  @Override
  protected DefaultTableModel getUpdatedTable(List<Filter> filters, Page page) {
    DefaultTableModel tableModel = new DefaultTableModel(getColumnNames(), 0);
    tableModel.setRowCount(0);
    MifidPage<EmailDto> mifidPage = emailModuleController.getPage(filters, page);
    CURRENT_PAGES = mifidPage.getTotalPage();
    List<EmailDto> emailsData = mifidPage.getLimitedObjects();
    for (EmailDto email : emailsData) {
      tableModel.addRow(new Object[]{
          email.getId(),
          email.getSubject(),
          email.getBody(),
          email.getSender(),
          email.getRecipientAsText(),
          email.getRecipientCcAsText(),
          email.getStatus(),
          email.getErrorMessage(),
          email.getSentAttempts()
      });
    }
    return tableModel;
  }
}