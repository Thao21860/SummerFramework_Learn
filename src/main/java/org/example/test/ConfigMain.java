package org.example.test;

import org.example.annotation.ComponentScan;
import org.example.annotation.Configuration;
import org.example.annotation.Import;

@ComponentScan
@Configuration
@Import({ConfigT1.class,ConfigT2.class, JdbcConfig.class})
public class ConfigMain {
}
