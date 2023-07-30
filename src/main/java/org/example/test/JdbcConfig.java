package org.example.test;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.annotation.AutoWired;
import org.example.annotation.Bean;
import org.example.annotation.Configuration;
import org.example.annotation.Value;
import org.example.jdbc.JdbcTemplate;
import org.example.transaction.DataSourceTransactionManager;
import org.example.transaction.PlatformTransactionManager;
import org.example.transaction.TransactionalBeanPostProcessor;

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
    @Bean
    public JdbcTemplate jdbcTemplate(@AutoWired DataSource dataSource){
        return new JdbcTemplate(dataSource);
    }
    @Bean
    public TransactionalBeanPostProcessor transactionalBeanPostProcessor(){
        return new TransactionalBeanPostProcessor();
    }
    @Bean
    public PlatformTransactionManager platformTransactionManager(@AutoWired DataSource dataSource){
        return new DataSourceTransactionManager(dataSource);
    }

}
