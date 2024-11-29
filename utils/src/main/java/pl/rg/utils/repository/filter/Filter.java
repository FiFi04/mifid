package pl.rg.utils.repository.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Filter {

  private String column;

  private Object[] value;

  private FilterDataType filterData;

  private FilterSearchType filterSearch;

  private FilterConditionType filterCondition = FilterConditionType.AND;

  public Filter(String column, Object[] value, FilterDataType filterData,
      FilterSearchType filterSearch) {
    this.column = column;
    this.value = value;
    this.filterData = filterData;
    this.filterSearch = filterSearch;
  }

  public Filter(String column, Object[] value, FilterSearchType filterSearch) {
    this.column = column;
    this.value = value;
    this.filterSearch = filterSearch;
  }
}