package pl.rg;

public interface EmailModuleFactory {

  String INSTANCE_METHOD_NAME = "getInstance";

  String FACTORY_NAME = "emailModuleFactory";

  EmailModuleApi getInstance();
}