package org.example.jdbc;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.annotation.Bean;
import org.example.annotation.Configuration;
import org.example.annotation.Value;

import javax.sql.DataSource;

@Configuration
public class JdbcConfig {
    @Bean
    public DataSource dataSource(@Value("${summer.datasource.driver-class-name}") String driver,
                          @Value("${summer.datasource.url}") String url,
                          @Value("${summer.datasource.username}") String username,
                          @Value("${summer.datasource.password}") String password,
                          @Value("${summer.datasource.max-pool-size}") int maxPoolSize,
                          @Value("${summer.datasource.min-pool-size}") int minPoolSize,
                          @Value("${summer.datasource.time-out}") int timeOut){
        HikariConfig config = new HikariConfig();
        config.setAutoCommit(false);
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minPoolSize);
        config.setConnectionTimeout(timeOut);
        return new HikariDataSource(config);
    }
}
