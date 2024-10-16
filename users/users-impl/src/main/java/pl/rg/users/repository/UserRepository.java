package pl.rg.users.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import pl.rg.users.model.UserModel;
import pl.rg.utils.annotation.Repository;
import pl.rg.utils.repository.MifidRepository;

@Repository
public class UserRepository extends MifidRepository<UserModel, Integer> {

  public boolean containsUsername(String value) {
    String tableName = "user";
    String columnName = "user_name";
    String query = "SELECT EXISTS (SELECT %s FROM %s WHERE %s = '" + value + "')";
    try (Statement statement = getDBConnector().getConnection().createStatement()) {
      String completeQuery = String.format(query, columnName, tableName, columnName);
      logger.log(completeQuery);
      ResultSet resultSet = statement.executeQuery(completeQuery);
      resultSet.next();
      return resultSet.getBoolean(1);
    } catch (SQLException e) {
      logger.logAndThrowRepositoryException("Błąd porównywania wartości w bazie danych: ", e);
      return false;
    }
  }
}
