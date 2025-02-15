package pl.rg.window;

import java.util.Arrays;
import java.util.Optional;

public interface DataEnumColumn {

  String getJavaAttribute();

  String getName();

  String getDbColumn();

  static <E extends Enum<E> & DataEnumColumn> Optional<String> getNameByJavaAttribute(
      Class<E> enumType, String javaAttribute) {
    return Arrays.stream(enumType.getEnumConstants())
        .filter(e -> e.getJavaAttribute().equals(javaAttribute))
        .map(DataEnumColumn::getName)
        .findFirst();
  }

  static <E extends Enum<E> & DataEnumColumn> Optional<String> getDbColumnByName(
      Class<E> enumType, String name) {
    return Arrays.stream(enumType.getEnumConstants())
        .filter(column -> column.getName().equals(name))
        .map(DataEnumColumn::getDbColumn)
        .findFirst();
  }
}