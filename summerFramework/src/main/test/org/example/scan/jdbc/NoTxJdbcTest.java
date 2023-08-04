package org.example.scan.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NoTxJdbcTest {
    DataSource dataSource = null;
    public NoTxJdbcTest(){
        HikariConfig config = new HikariConfig();
        // 此时置为false，以后的代码一定要手动提交才能生效
        config.setAutoCommit(false);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/test?characterEncoding=utf-8");
        config.setUsername("root");
        config.setPassword("root");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(2000);
        this.dataSource = new HikariDataSource(config);
    }
    @Test
    public void test1() throws SQLException {
        Connection connection = this.dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement("UPDATE `user` SET `name` = 'xjx' WHERE id=1");
        int n = ps.executeUpdate();
        System.out.println(n);

    }

}
