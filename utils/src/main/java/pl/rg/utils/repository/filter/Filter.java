package pl.rg.utils.repository.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Filter {

  private String column;

  private Object[] value;

  private FilterDataType filterData; //todo do zrobienia w przypadku błędów przy filtrowaniu

  private FilterSearchType filterSearch;

  private FilterConditionType filterCondition = FilterConditionType.AND;

  public Filter(String column, Object[] value, FilterSearchType filterSearch) {
    this.column = column;
    this.value = value;
    this.filterSearch = filterSearch;
  }

  public Filter(String column, Object[] value, FilterSearchType filterSearch,
      FilterConditionType filterCondition) {
    this.column = column;
    this.value = value;
    this.filterSearch = filterSearch;
    this.filterCondition = filterCondition;
  }
}