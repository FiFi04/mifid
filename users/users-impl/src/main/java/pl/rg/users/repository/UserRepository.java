package pl.rg.users.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import pl.rg.users.model.UserModel;
import pl.rg.utils.annotation.Repository;
import pl.rg.utils.exception.RepositoryException;
import pl.rg.utils.repository.MifidRepository;

@Repository
public class UserRepository extends MifidRepository<UserModel, Integer> {

  public boolean containsUsername(String value) {

    String query = "SELECT EXISTS (SELECT %s FROM %s WHERE %s = '" + value + "')";
    try (Statement statement = getDBConnector().getConnection().createStatement()) {
      String completeQuery = String.format(query, UserModel.USER_NAME, UserModel.TABLE_NAME,
          UserModel.USER_NAME);
      logger.log(completeQuery);
      ResultSet resultSet = statement.executeQuery(completeQuery);
      resultSet.next();
      return resultSet.getBoolean(1);
    } catch (SQLException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException(DB_COMPARE_EXCEPTION_MESSAGE));
    }
  }

  public Optional<UserModel> getByUsername(String username) {
    String query = "SELECT id " + "FROM %s WHERE %s = '" + username + "'";
    try (Statement statement = getDBConnector().getConnection().createStatement()) {
      String completeQuery = String.format(query, UserModel.TABLE_NAME, UserModel.USER_NAME);
      logger.log(completeQuery);
      ResultSet resultSet = statement.executeQuery(completeQuery);
      if (resultSet.next()) {
        int userId = resultSet.getInt(1);
        return findById(userId);
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw logger.logAndThrowRepositoryException(
          new RepositoryException(DB_COMPARE_EXCEPTION_MESSAGE));
    }
  }
}
