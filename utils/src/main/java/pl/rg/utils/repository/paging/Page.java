package pl.rg.utils.repository.paging;

import java.util.List;
import lombok.Data;

@Data
public class Page {

  private int from;

  private int to;

  private List<Order> orders;
}
