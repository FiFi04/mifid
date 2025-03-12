package pl.rg;

public interface EmailModuleFactory {

  String INSTANCE_METHOD_NAME = "getInstance";

  EmailModuleApi getInstance();
}