package pl.rg.users.session;

import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import pl.rg.users.UserSession;
import pl.rg.users.model.SessionModel;
import pl.rg.utils.db.PropertiesUtils;
import pl.rg.utils.repository.MifidGeneral;

@Data
@SuperBuilder
public class SessionImpl implements UserSession {

  private static UserSession session;

  private SessionModel activeSession;

  private LocalTime startTimeCounter;

  private final int sessionMaxTimeInSeconds = Integer.parseInt(
      PropertiesUtils.getProperty("session.maxTime"));

  private SessionImpl() {
  }

  public static UserSession getInstance() {
    if (session == null) {
      session = new SessionImpl();
    }
    return session;
  }

  @Override
  public String getActiveSessionUsername() {
    return activeSession.getUser();
  }

  @Override
  public String getActiveSessionToken() {
    return activeSession.getToken();
  }

  @Override
  public void setActiveSessionLogoutTime(LocalDateTime logoutTime) {
    this.activeSession.setLogoutTime(logoutTime);
  }

  @Override
  public void setActiveSession(MifidGeneral activeSession) {
    this.activeSession = (SessionModel) activeSession;
  }
}