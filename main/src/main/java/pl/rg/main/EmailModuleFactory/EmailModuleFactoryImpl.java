package pl.rg.main.EmailModuleFactory;

import pl.rg.EmailModuleApi;
import pl.rg.emails.impl.EmailModuleImpl;
import pl.rg.emails.impl.EmailModuleMockImpl;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.db.PropertiesUtils;

@Service
public class EmailModuleFactoryImpl implements EmailModuleFactory {

  private EmailModuleMockImpl emailModuleMock;

  private EmailModuleImpl emailModule;

  private boolean emailMock;

  @Override
  public EmailModuleApi createModule() {
    if (emailModuleMock == null || emailModule == null) {
      emailModuleMock = new EmailModuleMockImpl();
      emailModule = new EmailModuleImpl();
    }
    emailMock = Boolean.parseBoolean(
        PropertiesUtils.getProperty(PropertiesUtils.EMAIL_MOCK).toLowerCase());
    if (emailMock) {
      return emailModuleMock;
    } else {
      return emailModule;
    }
  }
}