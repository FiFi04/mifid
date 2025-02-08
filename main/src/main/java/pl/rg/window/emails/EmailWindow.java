package pl.rg.window.emails;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import pl.rg.utils.exception.ValidationException;
import pl.rg.window.WindowUtils;

public class EmailWindow extends JFrame implements WindowUtils {

  private JTextArea emailBodyField;

  public EmailWindow(EmailWindowModel emailWindowModel) {
    setTitle("Nowy emaila");
    setSize(500, 300);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JButton sendButton = new JButton("Wyślij");
    JButton cancelButton = new JButton("Anuluj");

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);

    int row = 0;

    for (EmailColumn emailColumn : EmailColumn.values()) {
      if (emailColumn.isVisibility()) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel(emailColumn.getName()), gbc);
        gbc.gridx = 1;
        if (emailColumn.getName().equals(EmailColumn.BODY.getName())) {
          setEmailBodyField(gbc, panel);
        } else {
          JTextField textField = new JTextField();
          gbc.fill = GridBagConstraints.HORIZONTAL;
          gbc.weightx = 1.0;
          gbc.weighty = 0;
          panel.add(textField, gbc);
        }
        row++;
      }
    }

    setButtons(gbc, row);
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(sendButton);
    buttonPanel.add(cancelButton);
    panel.add(buttonPanel, gbc);

    add(panel);

    sendButton.addActionListener(e -> {
      String subject = getTextFieldValue(panel, EmailColumn.SUBJECT.getName());
      String body = getTextFieldValue(panel, EmailColumn.BODY.getName());
      String recipient = getTextFieldValue(panel, EmailColumn.RECIPIENT.getName());
      String[] recipientArray = recipient.split("; ");
      String recipientCc = getTextFieldValue(panel, EmailColumn.RECIPIENT_CC.getName());
      String[] recipientCcArray = recipientCc.split("; ");
      send(emailWindowModel, subject, body, recipientArray, recipientCcArray);
    });

    cancelButton.addActionListener(e -> {
      dispose();
    });

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        emailWindowModel.refreshTable();
      }
    });
  }

  private void setButtons(GridBagConstraints gbc, int row) {
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
  }

  private void setEmailBodyField(GridBagConstraints gbc, JPanel panel) {
    emailBodyField = new JTextArea(5, 20);
    emailBodyField.setLineWrap(true);
    emailBodyField.setWrapStyleWord(true);
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    panel.add(emailBodyField, gbc);
  }

  private void send(EmailWindowModel emailWindowModel, String subject, String body,
      String[] recipient, String[] recipientCc) {
    try {
      if (recipientCc == null || recipientCc.length == 0) {
        emailWindowModel.getEmailModuleController()
            .sendEmail(subject, body, recipient, recipientCc);
      } else {
        emailWindowModel.getEmailModuleController()
            .sendEmail(subject, body, recipient, recipientCc);
      }

      JOptionPane.showMessageDialog(this, "Email został wysłany");
      dispose();
    } catch (ValidationException ex) {
      emailWindowModel.showValidationMessage(ex);
    } catch (RuntimeException ex) {
      JOptionPane.showMessageDialog(this, "Błąd podczas wysyłki maila. Mail nie został wysłany.",
          "Błąd wysyłki", JOptionPane.ERROR_MESSAGE);
    }
  }
}