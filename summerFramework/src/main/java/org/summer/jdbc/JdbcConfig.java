package org.summer.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.summer.annotation.AutoWired;
import org.summer.annotation.Bean;
import org.summer.annotation.Configuration;
import org.summer.annotation.Value;
import org.summer.transaction.DataSourceTransactionManager;
import org.summer.transaction.PlatformTransactionManager;
import org.summer.transaction.TransactionalBeanPostProcessor;

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
