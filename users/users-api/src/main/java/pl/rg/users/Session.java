package pl.rg.users;

public interface Session {

  void startSession(String currentUser);

  void updateSession();

  void endSession();
}
