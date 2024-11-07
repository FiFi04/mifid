package pl.rg.utils.repository.paging;

import lombok.Data;

@Data
public class Order {

  private String column;

  private OrderType orderType;
}
