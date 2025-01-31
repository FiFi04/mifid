package pl.rg;

import java.util.List;
import java.util.Map;
import pl.rg.utils.repository.MifidPage;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;

public interface EmailModuleController {

  void sendEmail(String subject, String body, String[] recipient, String... recipientCC);

  void resendEmail(int emailID);

  List<EmailDto> getFiltered(List<Filter> filters);

  MifidPage getPage(List<Filter> filters, Page page);

  Map<String, String> loadTemplates();

  void updateTemplate(String newTemplateText, String fileName);
}