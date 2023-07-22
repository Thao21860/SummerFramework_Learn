package org.example.test;

import org.example.annotation.ComponentScan;
import org.example.annotation.Configuration;
import org.example.annotation.Import;
import org.example.jdbc.JdbcConfig;

@ComponentScan
@Configuration
@Import({ConfigT1.class,ConfigT2.class, JdbcConfig.class})
public class ConfigMain {
}
