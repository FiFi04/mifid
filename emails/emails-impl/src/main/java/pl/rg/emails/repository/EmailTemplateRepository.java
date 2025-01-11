package pl.rg.emails.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import pl.rg.emails.model.EmailTemplateModel;
import pl.rg.utils.annotation.Repository;
import pl.rg.utils.exception.RepositoryException;
import pl.rg.utils.logger.LogLevel;
import pl.rg.utils.repository.MifidRepository;

@Repository
public class EmailTemplateRepository extends MifidRepository<EmailTemplateModel, Integer> {

  public Optional<EmailTemplateModel> findByTitle(String title) {
    String query = "SELECT id " + "FROM %s WHERE %s = '" + title + "'";
    try (Statement statement = getDBConnector().getConnection().createStatement()) {
      String completeQuery = String.format(query, EmailTemplateModel.TABLE_NAME,
          EmailTemplateModel.TITLE);
      logger.logSql(LogLevel.INFO, completeQuery);
      ResultSet resultSet = statement.executeQuery(completeQuery);
      if (resultSet.next()) {
        int emailTemplateId = resultSet.getInt(1);
        return findById(emailTemplateId);
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw logger.logAndThrowRepositoryException(LogLevel.DEBUG,
          new RepositoryException(DB_COMPARE_EXCEPTION_MESSAGE));
    }
  }
}
