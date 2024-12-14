package pl.rg.window;

import java.util.Arrays;
import java.util.Optional;

public interface DataEnumColumn {

  static String[] getColumnNames(DataEnumColumn[] values) {
    return Arrays.stream(values)
        .map(DataEnumColumn::getName)
        .toArray(String[]::new);
  }

  static String getNameByJavaAttribute(String javaAttribute, DataEnumColumn[] values) {
    return Arrays.stream(values)
        .filter(v -> v.getJavaAttribute().equals(javaAttribute))
        .findFirst().get().getName();
  }

  static Optional<String> getDbColumnByName(String name, DataEnumColumn[] values) {
    return Arrays.stream(values)
        .filter(column -> column.getName().equals(name))
        .map(DataEnumColumn::getDbColumn)
        .findFirst();
  }

  String getName();
  boolean isVisibility();
  String getDbColumn();
  String getJavaAttribute();
}
