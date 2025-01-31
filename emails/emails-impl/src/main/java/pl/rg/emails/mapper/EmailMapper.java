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
public interface EmailMapper extends MifidPageMapper {

  EmailMapper INSTANCE = Mappers.getMapper(EmailMapper.class);

  EmailModel domainToEmailModel(Email email);

  Email emailModelToDomain(EmailModel emailModel);

  Email dtoToDomain(EmailDto emailDto);

  @Mapping(source = "recipient", target = "recipientAsText", qualifiedByName = "recipientArrayToText")
  @Mapping(source = "recipientCc", target = "recipientCcAsText", qualifiedByName = "recipientArrayToText")
  EmailDto domainToDto(Email emailModel);

  @Named("recipientArrayToText")
  static String recipientArrayToText(String[] recipient) {
    if (recipient == null) {
      return "";
    }
    return Arrays.stream(recipient)
        .map(Object::toString)
        .collect(Collectors.joining("; "));
  }

  @ObjectFactory
  default Email createEmail() {
    return new EmailImpl();
  }

  MifidPage<Email> emailModelPageToEmailPage(MifidPage<EmailModel> emailModelPage);

  MifidPage<EmailDto> emailPageToEmailDtoPage(MifidPage<Email> emailPage);
}