package pl.rg.window.users;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lombok.Getter;
import lombok.Setter;
import pl.rg.users.UserDto;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.exception.RepositoryException;
import pl.rg.utils.exception.ValidationException;
import pl.rg.window.MainWindow;

@Getter
@Setter
public class UserWindow extends JFrame {

  private boolean edit;

  private int userId;

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
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(panel);

    saveButton.addActionListener(e -> {
      String firstName = MainWindow.getTextFieldValue(panel, UserColumn.NAME.getName());
      String lastName = MainWindow.getTextFieldValue(panel, UserColumn.SURNAME.getName());
      String email = MainWindow.getTextFieldValue(panel, UserColumn.EMAIL.getName());
      if (!edit) {
        save(userWindowModel, firstName, lastName, email);
      } else {
        update(userWindowModel, firstName, lastName, email);
      }
    });

    cancelButton.addActionListener(e -> {
      dispose();
    });

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        userWindowModel.refreshTable();
      }
    });
  }

  private void update(UserWindowModel userWindowModel, String firstName, String lastName,
      String email) {
    try {
      Optional<UserDto> user = userWindowModel.getUserModuleController().getUser(userId);
      UserDto userDto = user.get();
      if (!firstName.isBlank()) {
        userDto.setFirstName(firstName);
      }
      if (!lastName.isBlank()) {
        userDto.setLastName(lastName);
      }
      if (!email.isBlank()) {
        userDto.setEmail(email);
      }
      userWindowModel.getUserModuleController().updateUser(userDto);
      JOptionPane.showMessageDialog(this, "Dane użytkownika zaktualizowane");
      dispose();
    } catch (ApplicationException | ValidationException ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage());
    } catch (RepositoryException ex1) {
      JOptionPane.showMessageDialog(this, "Błąd aktualizacji w bazie danych");
    }
  }

  private void save(UserWindowModel userWindowModel, String firstName, String lastName,
      String email) {
    try {
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
  }
}
