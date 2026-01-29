package dev.mastern.plugins.hypeup.database.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.mastern.plugins.hypeup.HypeUp;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    
    private final HypeUp plugin;
    private HikariDataSource dataSource;
    private final String type;
    
    public DatabaseConnection(HypeUp plugin) {
        this.plugin = plugin;
        this.type = plugin.getConfig().getString("database.type", "SQLITE");
    }
    
    public void connect() {
        HikariConfig config = new HikariConfig();
        
        if (type.equalsIgnoreCase("MYSQL")) {
            setupMySQL(config);
        } else {
            setupSQLite(config);
        }
        
        dataSource = new HikariDataSource(config);
    }
    
    private void setupMySQL(HikariConfig config) {
        FileConfiguration cfg = plugin.getConfig();
        
        String host = cfg.getString("database.mysql.host", "localhost");
        int port = cfg.getInt("database.mysql.port", 3306);
        String database = cfg.getString("database.mysql.database", "hypeup");
        String username = cfg.getString("database.mysql.username", "root");
        String password = cfg.getString("database.mysql.password", "");
        boolean useSSL = cfg.getBoolean("database.mysql.use-ssl", false);
        
        // Pool settings
        int maxPoolSize = cfg.getInt("database.mysql.pool.maximum-pool-size", 10);
        int minIdle = cfg.getInt("database.mysql.pool.minimum-idle", 2);
        long connectionTimeout = cfg.getLong("database.mysql.pool.connection-timeout", 30000);
        long idleTimeout = cfg.getLong("database.mysql.pool.idle-timeout", 600000);
        long maxLifetime = cfg.getLong("database.mysql.pool.max-lifetime", 1800000);
        long leakDetectionThreshold = cfg.getLong("database.mysql.pool.leak-detection-threshold", 60000);
        
        String jdbcUrl = "jdbc:mariadb://" + host + ":" + port + "/" + database 
            + "?useSSL=" + useSSL 
            + "&cachePrepStmts=true"
            + "&prepStmtCacheSize=250"
            + "&prepStmtCacheSqlLimit=2048"
            + "&useServerPrepStmts=true"
            + "&rewriteBatchedStatements=true"
            + "&cacheResultSetMetadata=true"
            + "&elideSetAutoCommits=true"
            + "&useLocalSessionState=true"
            + "&alwaysSendSetIsolation=false"
            + "&enableQueryTimeouts=false";
        
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);
        config.setValidationTimeout(5000);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("socketTimeout", "60000");
        config.setDriverClassName("org.mariadb.jdbc.Driver");
    }
    
    private void setupSQLite(HikariConfig config) {
        FileConfiguration cfg = plugin.getConfig();
        String filename = cfg.getString("database.sqlite.file", "hypeup.db");
        
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File dbFile = new File(dataFolder, filename);
        config.setJdbcUrl("jdbc:h2:" + dbFile.getAbsolutePath() + ";MODE=MySQL;CACHE_SIZE=8192;LOCK_MODE=1");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(1);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1800000);
        config.addDataSourceProperty("cachePrepStmts", "true");
    }
    
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
