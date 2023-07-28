package org.example.transaction;

import jakarta.annotation.Nullable;

import java.sql.Connection;
// 获取当前事务连接的工具
public class TransactionUtils {
    @Nullable
    public static Connection getCurrentConnection() {
        TransactionStatus status = DataSourceTransactionManager.transactionStatus.get();
        return status == null ? null : status.connection;
    }
}
