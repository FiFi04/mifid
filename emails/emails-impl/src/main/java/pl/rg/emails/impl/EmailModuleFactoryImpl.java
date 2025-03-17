package pl.rg.emails.impl;

import pl.rg.EmailModuleApi;
import pl.rg.EmailModuleFactory;
import pl.rg.utils.annotation.Autowire;
import pl.rg.utils.annotation.Service;
import pl.rg.utils.db.PropertiesUtils;

@Service
public class EmailModuleFactoryImpl implements EmailModuleFactory {

  @Autowire
  private EmailModuleMockImpl emailModuleMock;

  @Autowire
  private EmailModuleImpl emailModule;

  @Override
  public EmailModuleApi getInstance() {
    boolean emailMock = PropertiesUtils.getBooleanProperty(PropertiesUtils.EMAIL_MOCK);
    if (emailMock) {
      return emailModuleMock;
    } else {
      return emailModule;
    }
  }
}