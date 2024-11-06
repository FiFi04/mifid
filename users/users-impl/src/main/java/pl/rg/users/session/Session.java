package pl.rg.users.session;

import java.time.LocalTime;
import lombok.Data;
import pl.rg.users.model.SessionModel;
import pl.rg.utils.db.PropertiesUtils;

@Data
public class Session {

  private static Session session;

  private SessionModel activeSession;

  private LocalTime startTimeCounter;

  private final int sessionMaxTimeInSeconds = Integer.parseInt(
      PropertiesUtils.getProperty("session.maxTime"));

  private Session() {
  }

  public static Session getInstance() {
    if (session == null) {
      session = new Session();
    }
    return session;
  }
}