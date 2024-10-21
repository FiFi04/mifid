package pl.rg.users.window;

import java.awt.GridLayout;
import java.awt.HeadlessException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pl.rg.users.UserModuleController;

public class UserForm extends JFrame {

  JButton buttonSave = new JButton("Zapisz");
  JButton buttonCancel = new JButton("Anuluj");
  JLabel labelName = new JLabel("Imię:");
  JTextField fieldName = new JTextField();
  JLabel labelLastname = new JLabel("Nazwisko:");
  JTextField fieldLastName = new JTextField();
  JLabel labelEmail = new JLabel("E-mail:");
  JTextField fieldEmail = new JTextField();

  public UserForm(UserModuleController controller) throws HeadlessException {
    setTitle("User Form");
    setSize(300, 200);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(4, 2));

    panel.add(labelName);
    panel.add(fieldName);
    panel.add(labelLastname);
    panel.add(fieldLastName);
    panel.add(labelEmail);
    panel.add(fieldEmail);
    panel.add(buttonSave);
    panel.add(buttonCancel);

    add(panel);
    buttonCancel.addActionListener(e -> dispose());

    buttonSave.addActionListener(e -> {
      // Pobieranie wartości z pól tekstowych
      String imie = fieldName.getText();
      String nazwisko = fieldLastName.getText();
      String email = fieldEmail.getText();

      controller.createUser(imie, nazwisko, email);
    });
  }
}
