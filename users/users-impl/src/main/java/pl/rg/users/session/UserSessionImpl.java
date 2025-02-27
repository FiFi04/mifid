package pl.rg.users.session;

import static pl.rg.utils.db.PropertiesUtils.SESSION_MAXTIME;

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
public class UserSessionImpl implements UserSession {

  private static UserSession session;

  private final int sessionMaxTimeInSeconds = Integer.parseInt(
      PropertiesUtils.getProperty(SESSION_MAXTIME));

  private SessionModel activeSession;

  private LocalTime startTimeCounter;

  private UserSessionImpl() {
  }

  public static UserSession getInstance() {
    if (session == null) {
      session = new UserSessionImpl();
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