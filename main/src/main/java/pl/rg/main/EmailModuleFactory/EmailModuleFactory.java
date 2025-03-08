package pl.rg.main.EmailModuleFactory;

import pl.rg.EmailModuleApi;

public interface EmailModuleFactory {
  String INSTANCE_METHOD_NAME = "getInstance";

  EmailModuleApi getInstance();
}