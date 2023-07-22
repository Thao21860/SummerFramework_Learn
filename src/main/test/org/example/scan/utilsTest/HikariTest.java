package org.example.scan.utilsTest;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HikariTest {
    public static void main(String[] args) throws SQLException {
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://192.168.201.100:3306/mytest?characterEncoding=utf-8";
        String username = "root";
        String password = "root";

        HikariConfig config = new HikariConfig();
        config.setAutoCommit(false);
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(2000);
        DataSource dataSource = new HikariDataSource(config);
        Connection connection = dataSource.getConnection();
        System.out.println(connection);
        PreparedStatement ps = connection.prepareStatement("select * from account");
        ResultSet rs = ps.executeQuery();
        System.out.println(rs);
        rs.getString(1);
    }
}
