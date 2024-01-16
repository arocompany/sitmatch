package com.nex.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class DataSourceConfig {
    private final InitService initService;

//    @Value("${spring.datasource.driver-class-name}")
//    private String dbDriverClassName;
//    @Value("${spring.datasource.url}")
//    private String dbJdbcUrl;
//
//    @Value("${spring.datasource.username}")
//    private String dbUsername;
//
//    @Value("${spring.datasource.password}")
//    private String dbPassword;
//
//    @Bean(name = "dataSource")
//    public DataSource dataSource() {
//        log.info("dataSource init --- start");
//        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
//        dataSourceBuilder.driverClassName(dbDriverClassName);
//        dataSourceBuilder.url(dbJdbcUrl);
//        dataSourceBuilder.username(dbUsername);
//        dataSourceBuilder.password(dbPassword);
//        log.info("dataSource init --- end");
//        return dataSourceBuilder.build();
//    }

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        log.info("dataSource init -- start");
        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(configData.getDriverClassName());
        dataSource.setUrl(configData.getUrl());
        dataSource.setUsername(configData.getUserName());
        dataSource.setPassword(configData.getPassword());
        log.info("dataSource init -- end");
        return dataSource;
    }

//    @Bean
//    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
//        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
//        sessionFactory.setDataSource(dataSource());
//
//        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        sessionFactory.setConfigLocation(resolver.getResource("classpath:/mybatis-config.xml"));
//
//        sessionFactory.setMapperLocations(resolver.getResources("classpath:/mappers/*.xml"));
//        return sessionFactory.getObject();
//    }
//
//    @Bean
//    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) throws Exception {
//        final SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
//        return sqlSessionTemplate;
//    }
//
//    @Bean
//    public DataSourceTransactionManager transactionManager() {
//        DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource());
//        return manager;
//    }
}
