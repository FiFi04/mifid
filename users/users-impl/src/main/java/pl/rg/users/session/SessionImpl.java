package pl.rg.users.session;

import java.time.LocalTime;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import pl.rg.users.UserSession;
import pl.rg.users.model.SessionModel;
import pl.rg.utils.db.PropertiesUtils;

@Data
@SuperBuilder
public class SessionImpl implements UserSession {

  private static SessionImpl session;

  private SessionModel activeSession;

  private LocalTime startTimeCounter;

  private final int sessionMaxTimeInSeconds = Integer.parseInt(
      PropertiesUtils.getProperty("session.maxTime"));

  private SessionImpl() {
  }

  public static SessionImpl getInstance() {
    if (session == null) {
      session = new SessionImpl();
    }
    return session;
  }

  @Override
  public String getCurrentSessionUsername() {
    return activeSession.getUser();
  }
}