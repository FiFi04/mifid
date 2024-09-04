package pl.rg.repository;

import pl.rg.repository.model.PublicKeyHashModel;
import pl.rg.db.PropertiesUtils;
import pl.rg.logger.Logger;
import pl.rg.logger.LoggerImpl;
import pl.rg.annotation.Repository;

import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Optional;

import static pl.rg.repository.Repository.dbConnector;

@Repository
public class PasswordRepository {

    private String privateKeyDirectory = PropertiesUtils.getProperty("privateKey.directory");

    private Logger logger = LoggerImpl.getInstance();

    public Optional<PrivateKey> getPrivateKey() {
        File keyFile = new File(privateKeyDirectory);
        try {
            byte[] privateKeyBytes = Files.readAllBytes(keyFile.toPath());
            byte[] decodedPrivateKey = Base64.getDecoder().decode(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);
            return Optional.of(keyFactory.generatePrivate(privateKeySpec));
        } catch (IOException e) {
            logger.logAndThrowException("Błąd odczytu klucza z pliku: ", e);
        } catch (GeneralSecurityException e) {
            logger.logAndThrowException("Błąd odszyfrowania klucza z pliku: ", e);
        }
        return Optional.empty();
    }

    public Optional<PublicKey> getPublicKey() {
        try (Statement statement = dbConnector.getConnection().createStatement()) {
            String query = "SELECT * FROM " + PublicKeyHashModel.TABLE_NAME;
            logger.log(query);
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                String publicKeyStr = resultSet.getString(PublicKeyHashModel.KEY_HASH);
                byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                return Optional.of(keyFactory.generatePublic(publicKeySpec));
            }
        } catch (SQLException e) {
            logger.logAndThrowException("Błąd odczytu klucza z bazy : ", e);
        } catch (GeneralSecurityException e) {
            logger.logAndThrowException("Błąd odszyfrowania klucza z bazy : ", e);
        }
        return Optional.empty();
    }

    public boolean savePublicKey(PublicKey publicKey) {
        try (Statement statement = dbConnector.getConnection().createStatement()) {
            Optional<PublicKey> publicKeyOptional = getPublicKey();
            String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            if (publicKeyOptional.isPresent()) {
                String updateQuery = "UPDATE %s SET %s = '%s' WHERE id = 1";
                String completeUpdateQuery = String.format(updateQuery, PublicKeyHashModel.TABLE_NAME, PublicKeyHashModel.KEY_HASH, publicKeyStr);
                statement.executeUpdate(completeUpdateQuery);
                logger.log("Klucz został zaktualizowany w bazie danych.");
                return true;
            }
            String insertKeyQuery = "INSERT INTO %s (id, %s) VALUES (1, '%s')";
            String completeInsertQuery = String.format(insertKeyQuery, PublicKeyHashModel.TABLE_NAME, PublicKeyHashModel.KEY_HASH, publicKeyStr);
            logger.log(completeInsertQuery);
            statement.executeUpdate(completeInsertQuery);
            logger.log("Klucz został dodany do bazy danych");
            return true;
        } catch (SQLException e) {
            logger.logAndThrowException("Błąd zapisu klucza do bazy danych: ", e);
            return false;
        }
    }

    public boolean savePrivateKey(PrivateKey privateKey) {
        File keyFile = new File(privateKeyDirectory);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(keyFile))) {
            String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            writer.write(privateKeyStr);
            return true;
        } catch (IOException e) {
            logger.logAndThrowException("Błąd zapisu klucza do pliku: ", e);
            return false;
        }
    }
}
