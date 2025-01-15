package pl.rg.emails.impl;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.Email;
import pl.rg.utils.enums.EmailStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmailDomain implements Email {

  private Integer id;

  private String subject;

  private String body;

  private String sender;

  private String[] recipient;

  private String[] recipientCc;

  private EmailStatus status;

  private String errorMessage;

  private int sentAttempts;

  private LocalDateTime sentTime;

  public EmailDomain(String subject, String body, String sender, String[] recipient,
      String[] recipientCc, EmailStatus status, String errorMessage, int sentAttempts,
      LocalDateTime sentTime) {
    this.subject = subject;
    this.body = body;
    this.sender = sender;
    this.recipient = recipient;
    this.recipientCc = recipientCc;
    this.status = status;
    this.errorMessage = errorMessage;
    this.sentAttempts = sentAttempts;
    this.sentTime = sentTime;
  }

  public EmailDomain(String subject, String body, String[] recipient) {
    this.subject = subject;
    this.body = body;
    this.recipient = recipient;
  }

  public EmailDomain(String subject, String body, String[] recipient, String[] recipientCc) {
    this.subject = subject;
    this.body = body;
    this.recipient = recipient;
    this.recipientCc = recipientCc;
  }
}