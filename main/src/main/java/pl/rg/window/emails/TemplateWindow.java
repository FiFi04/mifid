package pl.rg.window.emails;


import java.awt.BorderLayout;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import pl.rg.window.WindowUtils;

public class TemplateWindow extends JFrame implements WindowUtils {

  private EmailWindowModel emailWindowModel;

  private JTextArea templateArea;

  private Map<String, String> templates;

  public TemplateWindow(EmailWindowModel emailWindowModel) {
    this.emailWindowModel = emailWindowModel;
    setTitle("Szablony emaili");
    setSize(500, 300);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);

    JButton changedButton = new JButton("Zapisz zmiany");
    JButton cancelButton = new JButton("Anuluj");

    JPanel panel = new JPanel(new BorderLayout());
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(changedButton);
    buttonPanel.add(cancelButton);

    DefaultListModel<String> templatesListModel = new DefaultListModel<>();
    templates = emailWindowModel.getEmailModuleController().loadTemplates();

    for (String template : templates.keySet()) {
      templatesListModel.addElement(template);
    }

    JList<String> templatesList = new JList<>(templatesListModel);
    templatesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane templateScrollPane = new JScrollPane(templatesList);
    panel.add(templateScrollPane, BorderLayout.WEST);

    templateArea = new JTextArea(15, 40);
    templateArea.setLineWrap(true);
    templateArea.setWrapStyleWord(true);
    JScrollPane templateAreaScrollPane = new JScrollPane(templateArea);
    panel.add(templateAreaScrollPane, BorderLayout.CENTER);

    panel.add(buttonPanel, BorderLayout.SOUTH);
    add(panel);

    templatesList.addListSelectionListener(e -> {
      String selectedTemplate = templatesList.getSelectedValue();
      String templateBody = templates.get(selectedTemplate);
      templateArea.setText(templateBody);
    });

    changedButton.addActionListener(e -> {
      String selectedTemplate = templatesList.getSelectedValue();
      String templateBody = templateArea.getText();
      emailWindowModel.getEmailModuleController().updateTemplate(templateBody, selectedTemplate);
      JOptionPane.showMessageDialog(new JFrame(), "Szablon został zaktualizowany");
    });

    cancelButton.addActionListener(e -> {
      dispose();
    });
  }
}