package pl.rg.emails.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.utils.enums.EmailStatus;
import pl.rg.utils.annotation.FieldCategory;
import pl.rg.utils.repository.MifidGeneral;


@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmailModel extends MifidGeneral<Integer> {

  @FieldCategory(dbColumn = true)
  public static final String SUBJECT = "subject";

  @FieldCategory(dbColumn = true)
  public static final String BODY = "body";

  @FieldCategory(dbColumn = true)
  public static final String SENDER = "sender";

  @FieldCategory(dbColumn = true)
  public static final String RECIPIENT = "recipient";

  @FieldCategory(dbColumn = true)
  public static final String RECIPIENT_CC = "recipient_cc";

  @FieldCategory(dbColumn = true)
  public static final String STATUS = "status";

  @FieldCategory(dbColumn = true)
  public static final String ERROR_MESSAGE = "error_message";

  @FieldCategory(dbColumn = true)
  public static final String SENT_ATTEMPTS = "sent_attempts";

  @FieldCategory(dbColumn = true)
  public static final String SENT_TIME = "sent_time";

  public static final String TABLE_NAME = "email";

  @FieldCategory(dbField = true)
  private String subject;

  @FieldCategory(dbField = true)
  private String body;

  @FieldCategory(dbField = true)
  private String sender;

  @FieldCategory(dbField = true)
  private String[] recipient;

  @FieldCategory(dbField = true)
  private String[] recipientCc;

  @FieldCategory(dbField = true)
  private EmailStatus status;

  @FieldCategory(dbField = true)
  private String errorMessage;

  @FieldCategory(dbField = true)
  private int sentAttempts;

  @FieldCategory(dbField = true)
  private LocalDateTime sentTime;

  public EmailModel(String subject, String body, String sender, String[] recipient,
      EmailStatus status,  String errorMessage, int sentAttempts, LocalDateTime sentTime) {
    this.subject = subject;
    this.body = body;
    this.sender = sender;
    this.recipient = recipient;
    this.status = status;
    this.errorMessage = errorMessage;
    this.sentAttempts = sentAttempts;
    this.sentTime = sentTime;
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }
}