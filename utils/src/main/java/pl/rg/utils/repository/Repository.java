package pl.rg.utils.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import pl.rg.utils.db.DBConnector;

public interface Repository<T extends MifidGeneral<E>, E> {

  default DBConnector getDBConnector() {
    return DBConnector.getInstance();
  }

  List<T> findAll() throws SQLException;

  Optional<T> findById(E id) throws SQLException;

  Optional<T> findFetchById(E id) throws SQLException;

  void deleteAll(Iterable<T> objects) throws SQLException;

  void deleteById(E id) throws SQLException;

  void save(T object) throws SQLException;

  T saveAndFlush(T object) throws SQLException;
}
