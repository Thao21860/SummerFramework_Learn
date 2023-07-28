package org.example.test.postPorcessor;

import org.example.annotation.AutoWired;
import org.example.annotation.Component;
import org.example.annotation.Transactional;
import org.example.exception.DataAccessException;
import org.example.ioc.ApplicationContext;
import org.example.ioc.ApplicationContextUtils;
import org.example.jdbc.JdbcTemplate;

import java.math.BigDecimal;

@Component
@Transactional
public class TransactionalTest {
    @AutoWired
    JdbcTemplate template;
    public void add(){
        int res = template.updateAndReturnGeneratedKey("insert into account (id,name,balance) values (?,?,?) ",5,"xixixi",new BigDecimal(2000)).intValue();
        System.out.println(res);
//        throw new DataAccessException();
    }

    public void update(){
        int res = template.updateAndReturnGeneratedKey("update account set `name`=?,balance=? where id=? ","xixixi",new BigDecimal(20),4).intValue();
        System.out.println(res);
    }
}
