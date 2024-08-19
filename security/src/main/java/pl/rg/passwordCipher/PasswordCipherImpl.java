package pl.rg.passwordCipher;

import pl.rg.db.DBConnector;
import pl.rg.db.PropertiesUtils;
import pl.rg.logger.Logger;
import pl.rg.logger.LoggerImpl;

import javax.crypto.Cipher;
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

public class PasswordCipherImpl implements PasswordCipher {

    private static PasswordCipherImpl passwordCipher;

    private final static String KEY_TABLE_NAME = "public_key";

    private final static String PUBLIC_KEY_COLUMN = "key_hash";

    private Logger logger = LoggerImpl.getInstance();

    private String privateKeyDirectory;

    DBConnector dbConnector = DBConnector.getInstance();

    private PasswordCipherImpl() {
        initializePasswordCipher();
    }

    private void initializePasswordCipher() {
        privateKeyDirectory = PropertiesUtils.getProperty("privateKey.directory", PropertiesUtils.PROPERTIES_FILE_SECURITY);
        generateRsaKeyPair();
    }

    public static PasswordCipherImpl getInstance() {
        if (passwordCipher == null) {
            passwordCipher = new PasswordCipherImpl();
        }
        return passwordCipher;
    }

    public Optional<String> encryptPassword(String password) {
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            Optional<PublicKey> publicKey = getPublicKey();
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey.orElseThrow());
            return Optional.of(Base64.getEncoder().encodeToString(encryptCipher.doFinal(password.getBytes())));
        } catch (GeneralSecurityException e) {
            logger.logAndThrowException("Błąd podczas szyfrowania hasła: ", e);
            return Optional.empty();
        }
    }

    public Optional<String> decryptPassword(String encryptedPassword) {
        try {
            Cipher decryptCipher = Cipher.getInstance("RSA");
            Optional<PrivateKey> privateKey = getPrivateKey();
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey.orElseThrow());
            return Optional.of(new String(decryptCipher.doFinal(Base64.getDecoder().decode(encryptedPassword))));
        } catch (GeneralSecurityException e) {
            logger.logAndThrowException("Błąd podczas odszyfrowania hasła: ", e);
            return Optional.empty();
        }
    }

    private void generateRsaKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            savePublicKey(keyPair.getPublic());
            savePrivateKey(keyPair.getPrivate());
        } catch (NoSuchAlgorithmException e) {
            logger.logAndThrowException("Błąd podczas tworzenia kluczy: ", e);
        }
    }

    private Optional<PrivateKey> getPrivateKey() {
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

    private Optional<PublicKey> getPublicKey() {
        try (Statement statement = dbConnector.getConnection().createStatement()) {
            String query = "SELECT * FROM " + KEY_TABLE_NAME;
            logger.log(query);
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                String publicKeyStr = resultSet.getString(PUBLIC_KEY_COLUMN);
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

    private boolean savePublicKey(PublicKey publicKey) {
        try (Statement statement = dbConnector.getConnection().createStatement()) {
            Optional<PublicKey> publicKeyOptional = getPublicKey();
            String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            if (publicKeyOptional.isPresent()) {
                String updateQuery = "UPDATE %s SET %s = '%s' WHERE id = 1";
                String completeUpdateQuery = String.format(updateQuery, KEY_TABLE_NAME, PUBLIC_KEY_COLUMN, publicKeyStr);
                statement.executeUpdate(completeUpdateQuery);
                logger.log("Klucz został zaktualizowany w bazie danych.");
                return true;
            }
            String insertKeyQuery = "INSERT INTO %s (id, %s) VALUES (1, '%s')";
            String completeInsertQuery = String.format(insertKeyQuery, KEY_TABLE_NAME, PUBLIC_KEY_COLUMN, publicKeyStr);
            logger.log(completeInsertQuery);
            statement.executeUpdate(completeInsertQuery);
            logger.log("Klucz został dodany do bazy danych");
            return true;
        } catch (SQLException e) {
            logger.logAndThrowException("Błąd zapisu klucza do bazy danych: ", e);
            return false;
        }
    }

    private boolean savePrivateKey(PrivateKey privateKey) {
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
