package pl.rg.utils.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import pl.rg.utils.db.DBConnector;
import pl.rg.utils.repository.filter.Filter;
import pl.rg.utils.repository.paging.Page;

public interface Repository<T extends MifidGeneral<E>, E> {

  String NO_METHOD_MESSAGE = "Brak metody o podanej sygnaturze";

  String INVOCATION_EXCEPTION_MESSAGE = "Wystąpił wyjątek podczas wykonywania metody";

  String INSTANTIATION_EXCEPTION_MESSAGE = "Nie można utworzyć obiektu";

  String NO_ACCESS_MESSAGE = "Brak dostepu do metody";

  String NO_CLASS_MESSAGE = "Brak klasy o podanej nazwie";

  String DB_READ_EXCEPTION_MESSAGE = "Błąd odczytu z bazy danych";

  String DB_DELETE_EXCEPTION_MESSAGE = "Błąd podczas usuwania obiektu z bazy danych";

  String GENERAL_EXCEPTION_MESSAGE = "Wystąpił niespodziewany błąd";

  String DB_ROLLBACK_MESSAGE = "Wycofano zmiany z bazy danych";

  String DB_ROLLBACK_EXCEPTION_MESSAGE = "Błąd podczas wycowywania zmian z bazy danych";

  String DB_SAVE_MESSAGE = "Zapisano w bazie danych";

  String DB_SAVE_EXCEPTION_MESSAGE = "Błąd zapisu do bazy danych";

  String DB_COMPARE_EXCEPTION_MESSAGE = "Błąd porównywania wartości w bazie danych";

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

  List<T> findAll(List<Filter> filters);

  MifidPage<T> findAll(List<Filter> filters, Page page);
}