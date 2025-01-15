package pl.rg.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailTemplateType {
  NEW_ACCOUNT ("Dodanie konta"),
  RESET_PASSWORD ("Reset hasła");

  private String name;
}
