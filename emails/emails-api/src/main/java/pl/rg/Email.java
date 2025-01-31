package pl.rg;

import java.time.LocalDateTime;
import pl.rg.utils.enums.EmailStatus;

public interface Email {

  Integer getId();

  void setId(Integer id);

  String getSubject();

  void setSubject(String subject);

  String getBody();

  void setBody(String body);

  String getSender();

  void setSender(String sender);

  String[] getRecipient();

  void setRecipient(String[] recipient);

  String[] getRecipientCc();

  void setRecipientCc(String[] recipientCc);

  EmailStatus getStatus();

  void setStatus(EmailStatus status);

  String getErrorMessage();

  void setErrorMessage(String errorMessage);

  int getSentAttempts();

  void setSentAttempts(int sentAttempts);

  LocalDateTime getSentTime();

  void setSentTime(LocalDateTime sentTime);
}