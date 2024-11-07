package pl.rg.window.users;

import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UserWindow extends JFrame {

  public UserWindow(UserWindowModel userWindowModel) {
    setTitle("Dane użytkownika");
    setSize(300, 200);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JButton saveButton = new JButton("Zapisz");
    JButton cancelButton = new JButton("Anuluj");

    saveButton.addActionListener(e ->
    {
      // todo
    });

    cancelButton.addActionListener(e -> {
      dispose();
    });

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
  }
}
