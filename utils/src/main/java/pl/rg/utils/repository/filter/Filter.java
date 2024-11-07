package pl.rg.utils.repository.filter;

import lombok.Getter;

@Getter
public class Filter {

  private String column;

  private Object[] value;

  private FilterDataType filterData;

  private FilterSearchType filterSearch;

  private FilterContitionType filterContition = FilterContitionType.AND;

}
