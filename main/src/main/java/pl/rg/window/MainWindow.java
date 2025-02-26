package pl.rg.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import pl.rg.EmailModuleController;
import pl.rg.main.AppContainer;
import pl.rg.users.UserModuleController;
import pl.rg.utils.enums.EmailStatus;
import pl.rg.window.emails.EmailWindowModel;
import pl.rg.window.users.UserWindowModel;

public class MainWindow extends JFrame {

  private JButton usersButton;
  private JButton emailsButton;
  private JButton logoutButton;
  private JTable mainTable;

  private AbstractWindow currentTableWindow;

  private JComboBox<String> sortColumnComboBox;

  private JComboBox<Integer> pageNumberComboBox;

  private UserWindowModel userWindowModel;

  private EmailWindowModel emailWindowModel;

  private MainWindow() {
    setTitle("Ankieta Mifid");
    setSize(900, 550);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout(10, 10));

    UserModuleController userModuleController = (UserModuleController) AppContainer.getContainer()
        .get("userModuleController");
    EmailModuleController emailModuleController = (EmailModuleController) AppContainer.getContainer()
        .get("emailModuleController");

    add(createLeftPanel(), BorderLayout.WEST);
    JPanel centerPanel = createCenterPanel();
    JPanel searchPanel = createSearchPanel();
    centerPanel.add(searchPanel, BorderLayout.NORTH);
    add(wrapCenterPanel(centerPanel), BorderLayout.CENTER);
    JPanel sortPagePanel = sortPagePanel();
    add(sortPagePanel, BorderLayout.SOUTH);
    JPanel rightPanel = createRightPanel();
    add(rightPanel, BorderLayout.EAST);
    addButtonActions(searchPanel, rightPanel, sortPagePanel, userModuleController);
    addTableSelectionListener();

    userWindowModel = new UserWindowModel(mainTable, userModuleController, searchPanel,
        sortColumnComboBox, pageNumberComboBox);

    emailWindowModel = new EmailWindowModel(mainTable, emailModuleController, searchPanel,
        sortColumnComboBox, pageNumberComboBox);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        userModuleController.logOut();
        dispose();
      }
    });
  }

  public static void display() {
    SwingUtilities.invokeLater(() -> {
      MainWindow window = new MainWindow();
      window.setVisible(true);
    });
  }

  private void addButtonActions(JPanel searchPanel, JPanel rightPanel, JPanel sortPagePanel,
      UserModuleController userModuleController) {
    usersButton.addActionListener(
        e -> loadView(userWindowModel, searchPanel, rightPanel, sortPagePanel));

    emailsButton.addActionListener(
        e -> loadView(emailWindowModel, searchPanel, rightPanel, sortPagePanel));

    logoutButton.addActionListener(e -> {
      userModuleController.logOut();
      dispose();
    });
  }

  private void loadView(AbstractWindow window, JPanel searchPanel, JPanel rightPanel,
      JPanel sortPagePanel) {
    DefaultTableModel windowModel = window.loadData();
    mainTable.setModel(windowModel);
    window.updateSearchPanel(searchPanel);
    window.updateSortAndPage(sortPagePanel);
    window.updateRightPanel(rightPanel);
    window.addSortAndPageActions();
    currentTableWindow = window;
  }

  private JPanel sortPagePanel() {
    JPanel sortPagePanel = new JPanel(
        new FlowLayout(FlowLayout.CENTER));
    JLabel sortLabel = new JLabel("Sortuj po kolumnie:");
    sortColumnComboBox = new JComboBox<>();
    JLabel pageLabel = new JLabel("Numer strony:");
    pageNumberComboBox = new JComboBox<>();

    sortPagePanel.add(sortLabel);
    sortPagePanel.add(sortColumnComboBox);
    sortPagePanel.add(Box.createHorizontalStrut(20));
    sortPagePanel.add(pageLabel);
    sortPagePanel.add(pageNumberComboBox);

    return sortPagePanel;
  }

  private JPanel createRightPanel() {
    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
    rightPanel.setPreferredSize(new Dimension(130, 450));
    rightPanel.setBorder(
        BorderFactory.createEmptyBorder(10, 10, 10, 10));

    rightPanel.add(Box.createVerticalGlue());

    return rightPanel;
  }

  private JPanel wrapCenterPanel(JPanel centerPanel) {
    JPanel centerWrapper = new JPanel(new BorderLayout());
    centerWrapper.setBorder(
        BorderFactory.createEmptyBorder(0, 20, 0, 20));
    centerWrapper.add(centerPanel, BorderLayout.CENTER);

    return centerWrapper;
  }

  private JPanel createSearchPanel() {
    // Panel wyszukiwania
    JPanel searchPanel = new JPanel();
    searchPanel.setLayout(new GridLayout(1, 4, 5, 5));
    searchPanel.setBorder(
        BorderFactory.createEmptyBorder(5, 5, 5, 5));

    return searchPanel;
  }

  private JPanel createCenterPanel() {
    // Tabela na środku
    mainTable = new JTable();
    JScrollPane scrollPane = new JScrollPane(mainTable);
    scrollPane.setPreferredSize(new Dimension(620, 330));

    // Panel z tabelą i panelem wyszukiwania
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BorderLayout(0, 10));
    centerPanel.setPreferredSize(new Dimension(640, 450));
    centerPanel.add(scrollPane, BorderLayout.CENTER);

    return centerPanel;
  }

  private JPanel createLeftPanel() {
    JPanel leftPanel = new JPanel();
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
    leftPanel.setPreferredSize(new Dimension(130, 450));
    leftPanel.setBorder(
        BorderFactory.createEmptyBorder(10, 10, 10, 10));

    usersButton = new JButton("Użytkownicy");
    emailsButton = new JButton("Maile");
    logoutButton = new JButton("Wyloguj");

    Dimension buttonSize = new Dimension(120, 25);
    usersButton.setMaximumSize(buttonSize);
    emailsButton.setMaximumSize(buttonSize);
    logoutButton.setMaximumSize(buttonSize);

    leftPanel.add(Box.createVerticalStrut(10));
    leftPanel.add(usersButton);
    leftPanel.add(Box.createVerticalStrut(10));
    leftPanel.add(emailsButton);
    leftPanel.add(Box.createVerticalStrut(10));
    leftPanel.add(logoutButton);
    leftPanel.add(Box.createVerticalGlue());

    return leftPanel;
  }

  private void addTableSelectionListener() {
    mainTable.getSelectionModel().addListSelectionListener(e -> {
      int selectedRow = mainTable.getSelectedRow();
      if (selectedRow != -1 && currentTableWindow instanceof UserWindowModel) {
        String blockedStatus = (String) mainTable.getValueAt(selectedRow, 5);
        userWindowModel.changeButtonVisibility(UserWindowModel.UNBLOCK_BUTTON,
            "Tak".equalsIgnoreCase(blockedStatus));
      } else if (selectedRow != -1 && currentTableWindow instanceof EmailWindowModel) {
        String sentStatus = mainTable.getValueAt(selectedRow, 6).toString();
        emailWindowModel.changeButtonVisibility(EmailWindowModel.RESEND_BUTTON,
            EmailStatus.ERROR.toString().equalsIgnoreCase(sentStatus));
      }
    });
  }
}