package org.example.test.jdbc;

import java.math.BigDecimal;

public class User {
    public int id;

    public String name;
    public BigDecimal balance;

    public User(int id, String name, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public User() {
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }
}
