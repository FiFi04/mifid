package pl.rg.utils.repository.paging;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page {

  private int from;

  private int to;

  private List<Order> orders;
}
