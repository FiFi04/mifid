package pl.rg.window.users;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import pl.rg.utils.exception.RepositoryException;
import pl.rg.utils.exception.ValidationException;
import pl.rg.window.WindowUtils;

@Getter
@Setter
public class UserWindow extends JFrame implements WindowUtils {

  private UserDto userDto;

  private UserWindowModel userWindowModel;

  public UserWindow(UserWindowModel userWindowModel, boolean edit, Integer userId) {
    this.userWindowModel = userWindowModel;
    setTitle("Dane użytkownika");
    setSize(300, 200);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JButton saveButton = new JButton("Zapisz");
    JButton cancelButton = new JButton("Anuluj");

    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(6, 1));

    if (edit) {
      userDto = userWindowModel.getUserModuleController().getUser(userId).get();
    }
    for (UserColumn userColumn : UserColumn.values()) {
      if (userColumn.isVisibility()) {
        panel.add(new JLabel(userColumn.getName()));
        if (edit) {
          JTextField textField = new JTextField();
          if (userColumn.getName().equals(UserColumn.NAME.getName())) {
            textField.setText(userDto.getFirstName());
          }
          if (userColumn.getName().equals(UserColumn.SURNAME.getName())) {
            textField.setText(userDto.getLastName());
          }
          if (userColumn.getName().equals(UserColumn.EMAIL.getName())) {
            textField.setText(userDto.getEmail());
          }
          panel.add(userColumn.getName(), textField);
        } else {
          panel.add(userColumn.getName(), new JTextField());
        }
      }
    }
    panel.add(saveButton);
    panel.add(cancelButton);
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(panel);

    saveButton.addActionListener(e -> {
      String firstName = getTextFieldValue(panel, UserColumn.NAME.getName());
      String lastName = getTextFieldValue(panel, UserColumn.SURNAME.getName());
      String email = getTextFieldValue(panel, UserColumn.EMAIL.getName());
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
      userDto.setFirstName(firstName);
      userDto.setLastName(lastName);
      userDto.setEmail(email);
      userWindowModel.getUserModuleController().updateUser(userDto);
      JOptionPane.showMessageDialog(this, "Dane użytkownika zaktualizowane");
      dispose();
    } catch (ValidationException ex) {
      userWindowModel.showValidationMessage(ex);
    } catch (RepositoryException ex1) {
      JOptionPane.showMessageDialog(this, "Błąd aktualizacji w bazie danych", "Błąd!",
          JOptionPane.ERROR_MESSAGE);
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
    } catch (ValidationException ex) {
      userWindowModel.showValidationMessage(ex);
    } catch (RepositoryException ex) {
      JOptionPane.showMessageDialog(this, "Błąd zapisu do bazy danych!", "Błąd!",
          JOptionPane.ERROR_MESSAGE);
    }
  }
}