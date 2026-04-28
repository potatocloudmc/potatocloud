package net.potatocloud.node.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public final class MySQLHandler {
    private final HikariConfig config = new HikariConfig();
    private HikariDataSource dataSource;

    @SneakyThrows
    public MySQLHandler() {
        config.setMaximumPoolSize(20);
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setJdbcUrl(Optional.ofNullable(System.getenv("MARIADB_HOST")).orElse("jdbc:mariadb://localhost:3306/potatocloud"));
        config.setUsername("root");
        config.setPoolName("potatocloud-Hikari");
        config.setPassword(Optional.ofNullable(System.getenv("MARIADB_PASSWORD")).orElseThrow(() -> new SQLException("root password missing")));
        config.setAutoCommit(false);
        //connect();
    }

    private synchronized void connect() {
        if (dataSource != null && !dataSource.isClosed()) return;
        dataSource = new HikariDataSource(config);
    }

    public synchronized void close() {
        if (dataSource == null) return;
        try {
            dataSource.close();
        } finally {
            dataSource = null;
        }
    }

    public Connection getConnection() throws SQLException {
        HikariDataSource ds = this.dataSource;
        if (ds == null) throw new SQLException("MySQLHandler is not connected (dataSource == null)");
        return ds.getConnection();
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }
}
