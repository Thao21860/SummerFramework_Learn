package org.example.transaction;

import org.example.exception.TransactionException;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

// 执行事务的类
public class DataSourceTransactionManager implements InvocationHandler, PlatformTransactionManager {
    static final ThreadLocal<TransactionStatus> transactionStatus = new ThreadLocal<>();
    final DataSource dataSource;

    public DataSourceTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TransactionStatus status = transactionStatus.get();
        if (status == null){
            // 无事务
            try (Connection connection = dataSource.getConnection()){
                // 关闭自动提交
                final boolean autoCommit = connection.getAutoCommit();
                if (autoCommit){
                    connection.setAutoCommit(false);
                }
                try {
                    // ThreadLocal状态
                    transactionStatus.set(new TransactionStatus(connection));
                    // 调用业务方法
                    Object r = method.invoke(proxy, args);
                    // 提交事务
                    connection.commit();
                    return r;
                } catch (InvocationTargetException e) {
                    // 出错回滚事务
                    TransactionException te = new TransactionException(e.getCause());
                    try {
                        connection.rollback();
                    }catch (SQLException sqlE) {
                        te.addSuppressed(sqlE);
                    }
                throw te;
                } finally {
                    transactionStatus.remove();
                    if (autoCommit) {
                        connection.setAutoCommit(true);
                    }
                }
            }
        } else {
            // 已经有事务，加入当前事务
            return method.invoke(proxy,args);
        }
    }
}
