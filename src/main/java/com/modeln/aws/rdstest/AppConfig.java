package com.modeln.aws.rdstest;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@ComponentScan("com.modeln")
@PropertySource("classpath:database.properties")
public class AppConfig {

  private final static String URL = "url";
  private final static String USER = "dbuser";
  private final static String DRIVER = "driver";
  private final static String PASSWORD = "dbpassword";

  @Autowired
  Environment environment;

  @Bean
  DataSource dataSource() {
    DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
    driverManagerDataSource.setUrl(System.getenv(URL));
    driverManagerDataSource.setUsername(System.getenv(USER));
    driverManagerDataSource.setPassword(System.getenv(PASSWORD));
    driverManagerDataSource.setDriverClassName(environment.getProperty(DRIVER));
    return driverManagerDataSource;
  }

  @Bean
  NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
    NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource());
    
    return template;
  }
}