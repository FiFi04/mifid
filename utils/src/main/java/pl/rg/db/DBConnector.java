package pl.rg.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnector {

    public static final String APP_PROPERTIES = "app.properties";
    private static DBConnector dbConnector;

    private Connection connection;

    private DBConnector() {
        this.connection = initializeConnection();
    }

    public static DBConnector getInstance() {
        if (dbConnector == null) {
            dbConnector = new DBConnector();
        }
        return dbConnector;
    }

    public Connection getConnection() {
        return connection;
    }

    private Connection initializeConnection() {
        try {
            Properties properties = getProperties();
            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    private Properties getProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = DBConnector.class.getClassLoader().getResourceAsStream(
            APP_PROPERTIES)) {
            properties.load(inputStream);
        }
        return properties;
    }
}
