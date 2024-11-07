package pl.rg.utils.repository;

import java.util.List;
import lombok.Data;

@Data
public class MifidPage<T extends MifidGeneral<E>, E> {

  private int totalObjects;

  private int totalPage;

  private int objectFrom;

  private int objectTo;

  private List<T> limitedObjects;

  private boolean hasObjects() {
    // todo implement
    return false;
  }
}
