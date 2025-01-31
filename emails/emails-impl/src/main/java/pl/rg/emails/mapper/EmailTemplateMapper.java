package pl.rg.emails.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;
import pl.rg.EmailTemplate;
import pl.rg.EmailTemplateDto;
import pl.rg.emails.impl.EmailTemplateImpl;
import pl.rg.emails.model.EmailTemplateModel;

@Mapper
public interface EmailTemplateMapper {

  EmailTemplateMapper INSTANCE = Mappers.getMapper(EmailTemplateMapper.class);

  EmailTemplateModel domainToModel(EmailTemplate emailTemplate);

  EmailTemplate modelToDomain(EmailTemplateModel emailTemplateModel);

  EmailTemplate dtoToDomain(EmailTemplateDto emailTemplateDto);

  @ObjectFactory
  default EmailTemplate createEmailTemplate() {
    return new EmailTemplateImpl();
  }
}