package com.neis.db;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private DBConnection() {}

    static {
        try {
            // MariaDB JDBC Driver 로딩
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MariaDB JDBC Driver를 찾을 수 없습니다. pom.xml 의 mariadb-java-client 의존성을 확인하세요.", e);
        }
    }

    public static Connection getConnection(ServletContext ctx) throws SQLException {
        String url = DBConfig.getJdbcUrl(ctx);
        String user = DBConfig.getUser(ctx);
        String pass = DBConfig.getPassword(ctx);
        return DriverManager.getConnection(url, user, pass);
    }
}


