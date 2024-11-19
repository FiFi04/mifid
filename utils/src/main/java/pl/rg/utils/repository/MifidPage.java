package pl.rg.utils.repository;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MifidPage<T> {

  private int totalObjects;

  private int totalPage;

  private int objectFrom;

  private int objectTo;

  private List<T> limitedObjects;

  private boolean hasObjects() {
    return limitedObjects != null && !limitedObjects.isEmpty();
  }
}
