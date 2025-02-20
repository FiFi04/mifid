package pl.rg.users;

import java.time.LocalDateTime;
import java.time.LocalTime;
import pl.rg.utils.repository.MifidGeneral;

public interface UserSession {

  String getActiveSessionUsername();

  String getActiveSessionToken();

  void setActiveSessionLogoutTime(LocalDateTime logoutTime);

  MifidGeneral getActiveSession();

  void setActiveSession(MifidGeneral activeSession);

  LocalTime getStartTimeCounter();

  void setStartTimeCounter(LocalTime startTimeCounter);

  int getSessionMaxTimeInSeconds();
}
