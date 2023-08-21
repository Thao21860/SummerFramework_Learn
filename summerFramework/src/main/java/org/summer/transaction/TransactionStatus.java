package org.summer.transaction;

import java.sql.Connection;

// 记录事务状态
public class TransactionStatus {
    final Connection connection;

    public TransactionStatus(Connection connection) {
        this.connection = connection;
    }
}
