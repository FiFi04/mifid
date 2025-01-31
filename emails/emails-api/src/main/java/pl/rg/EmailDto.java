package pl.rg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pl.rg.utils.annotation.Validate;
import pl.rg.utils.enums.EmailStatus;
import pl.rg.utils.validator.enums.PossibleNull;
import pl.rg.utils.validator.enums.ValidatorCase;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmailDto {

  private Integer id;

  private String subject;

  private String body;

  private String sender;

  @Validate(validatorCase = ValidatorCase.TEXT, message = "Nieprawidłowy format email", format = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
  private String[] recipient;

  @Validate(validatorCase = ValidatorCase.TEXT, message = "Nieprawidłowy format email", format = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", possibleNull = PossibleNull.YES)
  private String[] recipientCc;

  private EmailStatus status;

  private String errorMessage;

  private int sentAttempts;

  private String recipientAsText;

  private String recipientCcAsText;

  public EmailDto(String subject, String body, String[] recipient) {
    this.subject = subject;
    this.body = body;
    this.recipient = recipient;
  }

  public EmailDto(String subject, String body, String[] recipient, String[] recipientCc) {
    this.subject = subject;
    this.body = body;
    this.recipient = recipient;
    this.recipientCc = recipientCc;
  }
}