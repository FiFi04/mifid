package pl.rg.emails.mapper;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;
import pl.rg.Email;
import pl.rg.EmailDto;
import pl.rg.emails.impl.EmailImpl;
import pl.rg.emails.model.EmailModel;
import pl.rg.utils.pageAndSort.MifidPageMapper;
import pl.rg.utils.repository.MifidPage;

@Mapper
public abstract class EmailMapper implements MifidPageMapper {

  public static final EmailMapper INSTANCE = Mappers.getMapper(EmailMapper.class);

  @Named("recipientArrayToText")
  String recipientArrayToText(String[] recipient) {
    if (recipient == null) {
      return "";
    }
    return Arrays.stream(recipient)
        .map(Object::toString)
        .collect(Collectors.joining("; "));
  }

  public abstract EmailModel domainToEmailModel(Email email);

  public abstract Email emailModelToDomain(EmailModel emailModel);

  public abstract Email dtoToDomain(EmailDto emailDto);

  @Mapping(source = "recipient", target = "recipientAsText", qualifiedByName = "recipientArrayToText")
  @Mapping(source = "recipientCc", target = "recipientCcAsText", qualifiedByName = "recipientArrayToText")
  public abstract EmailDto domainToDto(Email emailModel);

  @ObjectFactory
  public Email createEmail() {
    return new EmailImpl();
  }

  public abstract MifidPage<Email> emailModelPageToEmailPage(MifidPage<EmailModel> emailModelPage);

  public abstract MifidPage<EmailDto> emailPageToEmailDtoPage(MifidPage<Email> emailPage);
}