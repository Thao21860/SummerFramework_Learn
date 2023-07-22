package org.example.jdbc;

import org.example.exception.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JdbcTemplate {
    final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    //实现update
    public int update(String sql, Object... args){
        return execute(
                preparedStatementCreator(sql, args),
                // 实现 ConnectionCallback中的函数式接口，调用ps的executeUpdate方法
                PreparedStatement::executeUpdate);
    }
    // 实现QueryList
    public <T> List<T> queryForList(String sql, Class<T> clazz,Object... args){
        return queryForList(sql,new BeanRowMapper<>(clazz),args);
    }
    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper,Object... args){
        return execute(preparedStatementCreator(sql,args),(PreparedStatement ps) -> {
            List<T> list = new ArrayList<>();
            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    // row 是 rs 维护的一个光标，指向当前项
                    list.add(rowMapper.mapRow(rs,rs.getRow()));
                }
            }
            return list;
        });
    }
    // 实现creator中的createPreparedStatement，即获取connection返回preparedStatement
    private PreparedStatementCreator preparedStatementCreator(String sql, Object... args){
        return (Connection con) -> {
            PreparedStatement ps = con.prepareStatement(sql);
            bindArgs(ps,args);
            return ps;
        };
    }
    // 写入参数
    private void bindArgs(PreparedStatement ps, Object... args) throws SQLException{
        for (int i = 0; i < args.length; i++){
            ps.setObject(i+1,args[i]);
        }
    }

    // action 为函数，需要实现
    public <T> T execute(ConnectionCallback<T> action){
        try(Connection connection = this.dataSource.getConnection()){
            return action.doInConnection(connection);
        }catch (SQLException e){
            throw new DataAccessException(e);
        }
    }
    // 先通过creator回调获取ps再执行connection回调
    public <T> T execute( PreparedStatementCreator psc, PreparedStatementCallback<T> action){
        return execute((Connection connection) -> {
            try (PreparedStatement ps = psc.createPreparedStatement(connection)){
                return action.doInConnection(ps);
            }
        });
    }
}

class StringRowMapper implements RowMapper<String> {
    static StringRowMapper instance = new StringRowMapper();

    @Override
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString(1);
    }
}

class BooleanRowMapper implements RowMapper<Boolean> {
    static BooleanRowMapper instance = new BooleanRowMapper();

    @Override
    public Boolean mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getBoolean(1);
    }
}

class NumberRawMapper implements RowMapper<Number> {
    static NumberRawMapper instance = new NumberRawMapper();

    @Override
    public Number mapRow(ResultSet rs, int rowNum) throws SQLException {
        return (Number) rs.getObject(1);
    }
}



