package org.example.scan.jdbc;

import org.example.config.PropertyResolver;
import org.example.ioc.AnnotationConfigApplicationContext;
import org.example.ioc.ApplicationContext;
import org.example.ioc.ApplicationContextUtils;
import org.example.test.ConfigMain;
import org.example.test.ConfigT1;
import org.example.utils.YamlUtils;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

public class JdbcTest {
    @Before
    public void before() throws IOException, URISyntaxException, ClassNotFoundException {
        Properties properties = new Properties();
        properties.load(ClassLoader.getSystemResourceAsStream("test.properties"));
        Map<String, Object> config = YamlUtils.loadYamlAsPlainMap();
        properties.putAll(config);
        PropertyResolver pr = new PropertyResolver(properties);
        ApplicationContextUtils.setApplicationContext(new AnnotationConfigApplicationContext(ConfigMain.class, pr));
    }

    @Test
    public void getDataSource() throws SQLException {
        ApplicationContext context = ApplicationContextUtils.getRequiredApplicationContext();
        DataSource dataSource = context.getBean("dataSource");
        System.out.println(dataSource);
        PreparedStatement ps = dataSource.getConnection().prepareStatement("select * from account ", Statement.RETURN_GENERATED_KEYS);
//        ps.setObject(1,1);
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            String ss = rs.getString(1);
            System.out.println(ss);
        }


    }

    @Test
    public void testUpdate() throws SQLException {
        ApplicationContext context = ApplicationContextUtils.getRequiredApplicationContext();
        DataSource dataSource = context.getBean("dataSource");
        PreparedStatement ps = dataSource.getConnection().prepareStatement("UPDATE `user` SET `name` = 'xxxx' WHERE id=1",Statement.RETURN_GENERATED_KEYS);
//        ps.setObject(1,"xxxx");
//        ps.setObject(2,1);
        int n = ps.executeUpdate();
        System.out.println(n);
        System.out.println(ps.getGeneratedKeys());
        ResultSet rs = ps.getGeneratedKeys();
        System.out.println(rs.next());
        while(rs.next()){
            String ss = rs.getString(1);
            System.out.println(ss);
        }
    }





}
