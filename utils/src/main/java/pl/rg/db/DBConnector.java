package pl.rg.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

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
            String url = PropertiesUtils.getProperty("db.url", PropertiesUtils.PROPERTIES_FILE_UTILS);
            String username = PropertiesUtils.getProperty("db.username", PropertiesUtils.PROPERTIES_FILE_UTILS);
            String password = PropertiesUtils.getProperty("db.password", PropertiesUtils.PROPERTIES_FILE_UTILS);
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }
}
