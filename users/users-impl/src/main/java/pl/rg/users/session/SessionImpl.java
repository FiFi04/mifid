package pl.rg.users.session;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;
import lombok.Data;
import pl.rg.users.Session;
import pl.rg.users.model.SessionModel;
import pl.rg.users.repository.SessionRepository;
import pl.rg.utils.db.PropertiesUtils;
import pl.rg.utils.exception.ApplicationException;
import pl.rg.utils.logger.Logger;
import pl.rg.utils.logger.LoggerImpl;

@Data
public class SessionImpl implements Session {

  private static SessionImpl session;

  private SessionRepository sessionRepository;

  private SessionModel activeSession;

  private LocalTime startTimeCounter;

  private Logger logger = LoggerImpl.getInstance();

  private int sessionMaxTimeInSeconds = Integer.parseInt(
      PropertiesUtils.getProperty("session.maxTime"));

  private SessionImpl() {
  }

  public static SessionImpl getInstance() {
    if (session == null) {
      session = new SessionImpl();
    }
    return session;
  }

  public void startSession(String currentUser) {
    sessionRepository = new SessionRepository();
    startTimeCounter = LocalTime.now();
    activeSession = new SessionModel(currentUser, generateToken(), LocalDateTime.now(), null);
    logger.log("Rozpoczęto sesję " + activeSession.getToken());
  }

  public void updateSession() {
    LocalTime currentTime = LocalTime.now();
    Duration actionDuration = Duration.between(startTimeCounter, currentTime);
    if (actionDuration.getSeconds() < sessionMaxTimeInSeconds) {
      startTimeCounter = currentTime;
    } else {
      endSession();
      throw logger.logAndThrowRuntimeException(new ApplicationException("U33SE",
          "Wylogowano z powodu zbyt długiego czasu nieaktywności"));
    }
  }

  public void endSession() {
    activeSession.setLogoutTime(LocalDateTime.now());
    sessionRepository.save(activeSession);
    logger.log("Zakończono sesję " + activeSession.getToken());
    activeSession = null;
  }

  private String generateToken() {
    int tokenLength = 20;
    String tokenChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    StringBuilder token = new StringBuilder();
    Random random = new Random();
    for (int i = 0; i < tokenLength; i++) {
      token.append(tokenChars.charAt(random.nextInt(tokenChars.length())));
    }
    return token.toString();
  }
}