package pl.rg.window;

import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import pl.rg.main.AppContainer;
import pl.rg.users.UserModuleController;
import pl.rg.utils.exception.ApplicationException;

public class LoginWindow extends JFrame {

  public LoginWindow(UserModuleController controller) throws HeadlessException {
    setTitle("Logowanie użytkownika");
    setSize(300, 200);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    JTextField usernameField = new JTextField(15);
    JPasswordField passwordField = new JPasswordField(15);
    JButton loginButton = new JButton("Zaloguj");

    ActionListener loginAction = e ->
    {
      try {
        controller.logIn(usernameField.getText(), String.valueOf(passwordField.getPassword()));
        dispose();
        MainWindow.display();
      } catch (ApplicationException exception) {
        JOptionPane.showMessageDialog(null, exception.getMessage(), "Błąd!",
            JOptionPane.ERROR_MESSAGE);
      }
    };

    passwordField.addActionListener(loginAction);
    loginButton.addActionListener(loginAction);

    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(6, 1));
    panel.add(new JLabel("Nazwa użytkownika:"));
    panel.add(usernameField);
    panel.add(new JLabel("Hasło:"));
    panel.add(passwordField);
    panel.add(loginButton);
    panel.setBorder(
        BorderFactory.createEmptyBorder(5, 5, 5, 5));

    add(panel);
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      LoginWindow loginWindow = new LoginWindow(
          (UserModuleController) AppContainer.getContainer().get("userModuleController"));
      loginWindow.setVisible(true);
    });
  }
}
