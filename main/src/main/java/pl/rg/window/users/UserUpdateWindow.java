package pl.rg.window.users;

import java.awt.GridLayout;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pl.rg.users.UserDto;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

public class UserUpdateWindow extends JFrame {

  Logger logger = LoggerImpl.getInstance();

  public UserUpdateWindow(UserWindowModel userWindowModel) {
    setTitle("Dane użytkownika");
    setSize(300, 200);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JButton updateButton = new JButton("Aktualizuj");
    JButton cancelButton = new JButton("Anuluj");

    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(6, 1));

    for (UserColumn userColumn : UserColumn.values()) {
      if (userColumn == UserColumn.ID) {
        panel.add(new JLabel("Id aktualnego użytkownika"));
        panel.add(userColumn.getName(), new JTextField());
      }
      if (userColumn.isVisibility()) {
        panel.add(new JLabel(userColumn.getName()));
        panel.add(userColumn.getName(), new JTextField());
      }
    }
    panel.add(updateButton);
    panel.add(cancelButton);
    panel.setBorder(
        BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(panel);

    updateButton.addActionListener(e -> {
      try {
        int id = Integer.parseInt(getTextFieldValue(panel, "Id aktualnego użytkownika"));
        String firstName = getTextFieldValue(panel, "Imię");
        String lastName = getTextFieldValue(panel, "Nazwisko");
        String email = getTextFieldValue(panel, "Email");

        Optional<UserDto> user = userWindowModel.getUserModuleController().getUser(id);
        UserDto userDto = user.get();
        if (!firstName.isEmpty()) {
          userDto.setFirstName(firstName);
        }
        if (!lastName.isEmpty()) {
          userDto.setFirstName(firstName);
        }
        if (!email.isEmpty()) {
          userDto.setFirstName(firstName);
        }
        userWindowModel.getUserModuleController().updateUser(user.get());
        JOptionPane.showMessageDialog(this, "Dane użytkownika zaktualizowane");
        dispose();
      } catch (RuntimeException ex) {
        if (ex.getMessage().startsWith("U32GH")) {
          JOptionPane.showMessageDialog(this, "Brak użytkownika o podanym ID");
        } else {
          JOptionPane.showMessageDialog(this, "Błąd aktualizacji");
        }
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
