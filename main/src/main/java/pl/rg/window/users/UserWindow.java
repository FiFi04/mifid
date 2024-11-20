package pl.rg.window.users;

import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

public class UserWindow extends JFrame {

  Logger logger = LoggerImpl.getInstance();

  public UserWindow(UserWindowModel userWindowModel) {
    setTitle("Dane użytkownika");
    setSize(300, 200);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JButton saveButton = new JButton("Zapisz");
    JButton cancelButton = new JButton("Anuluj");

    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(6, 1));

    for (UserColumn userColumn : UserColumn.values()) {
      if (userColumn.isVisibility()) {
        panel.add(new JLabel(userColumn.getName()));
        panel.add(userColumn.getName(), new JTextField());
      }
    }
    panel.add(saveButton);
    panel.add(cancelButton);
    panel.setBorder(
        BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(panel);

    saveButton.addActionListener(e -> {
      try {
        String firstName = getTextFieldValue(panel, "Imię");
        String lastName = getTextFieldValue(panel, "Nazwisko");
        String email = getTextFieldValue(panel, "Email");

        boolean userCreated = userWindowModel.getUserModuleController()
            .createUser(firstName, lastName, email);

        if (userCreated) {
          JOptionPane.showMessageDialog(this, "Użytkownik zapisany pomyślnie");
          dispose();
        } else {
          JOptionPane.showMessageDialog(this, "Nie udało się utworzyć użytkownika");
          dispose();
        }
      } catch (RuntimeException ex) {
        JOptionPane.showMessageDialog(this, "Błąd zapisu");
      }
    });

    cancelButton.addActionListener(e -> {
      dispose();
    });
  }

  private String getTextFieldValue(JPanel panel, String labelName) {
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