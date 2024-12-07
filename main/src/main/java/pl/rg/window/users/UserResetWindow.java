package pl.rg.window.users;

import java.awt.GridLayout;
import java.awt.Label;
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
import pl.rg.window.WindowUtils;

@Getter
@Setter
public class UserResetWindow extends JFrame implements WindowUtils {

  private UserDto userDto;

  public UserResetWindow(UserWindowModel userWindowModel) {
    setTitle("Dane użytkownika");
    setSize(300, 100);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JButton resetButton = new JButton("Resetuj");
    JButton cancelButton = new JButton("Anuluj");

    JPanel panel = new JPanel();
    panel.setLayout(new GridLayout(2, 2));

    panel.add(new JLabel(UserColumn.LOGIN.getName()));
    panel.add(UserColumn.LOGIN.getName(), new JTextField());

    panel.add(resetButton);
    panel.add(cancelButton);
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(panel);

    resetButton.addActionListener(e -> {
      String username = getTextFieldValue(panel, UserColumn.LOGIN.getName());
      userWindowModel.getUserModuleController().resetLoginAttempts(username);
      JOptionPane.showMessageDialog(this, "Odblokowano użytkownika");
      dispose();
    });

    cancelButton.addActionListener(e -> {
      dispose();
    });
  }
}
