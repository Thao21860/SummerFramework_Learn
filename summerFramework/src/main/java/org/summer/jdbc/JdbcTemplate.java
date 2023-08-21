package org.summer.jdbc;

import org.summer.exception.DataAccessException;
import org.summer.transaction.TransactionUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class JdbcTemplate {
    final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    /*
    read
     */
    public Number queryForNumber(String sql, Object...args) throws DataAccessException {
        return queryForObject(sql, NumberRawMapper.instance,args);
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
                    // 最初光标位于第一行之前，初始也需要调用next。
                    // row 是 rs 维护的一个光标，指向当前项
                    list.add(rowMapper.mapRow(rs,rs.getRow()));
                }
            }
            return list;
        });
    }
    @SuppressWarnings("unchecked")
    public <T> T queryForObject(String sql, Class<T> clazz, Object...args) throws DataAccessException{
        if (clazz == String.class){
            return (T) queryForObject(sql, StringRowMapper.instance,args);
        }
        if (clazz == Boolean.class){
            return (T) queryForObject(sql, BooleanRowMapper.instance, args);
        }
        if (clazz.isAssignableFrom(Number.class) || clazz.isPrimitive()){
            return (T) queryForObject(sql, NumberRawMapper.instance, args);
        }
        return queryForObject(sql, new BeanRowMapper<>(clazz), args);
    }
    // 实现queryObject
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException{
        return execute(preparedStatementCreator(sql,args), (PreparedStatement ps) -> {
            T t = null;
            try(ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    if (t == null){
                        t = rowMapper.mapRow(rs, rs.getRow());
                    }else {
                        throw new DataAccessException("multi result found");
                    }
                }
                if (t == null){
                    throw new DataAccessException("empty result found");
                }
            }
            return t;
        });
    }
    /*
    write
     */
    //实现update
    public int update(String sql, Object... args){
        return execute(
                preparedStatementCreator(sql, args),
                // 实现 ConnectionCallback中的函数式接口，调用ps的executeUpdate方法
                PreparedStatement::executeUpdate);
    }

    public Number updateAndReturnGeneratedKey(String sql, Object...args){
        return execute((Connection con) -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            bindArgs(ps,args);
            return ps;
            },
            (PreparedStatement ps) -> {
                int n = ps.executeUpdate();
                if (n == 0){
                    throw new DataAccessException("0 rows inserted");
                }
                if (n > 1){
                    throw new DataAccessException("multi rows inserted");
                }
                return n;
                // TODO:resultSet.next 总是返回false
//                try(ResultSet keys = ps.getGeneratedKeys()){
//                    while (keys.next()){
//                        return (Number) keys.getObject(1);
//                    }
//                }
//                throw new DataAccessException(" should not reach here");
            });
    }

    // 实现creator中的createPreparedStatement，即获取connection返回preparedStatement
    // creator 对sql语句进行预编译
    private PreparedStatementCreator preparedStatementCreator(String sql, Object... args){
        return (Connection con) -> {
            PreparedStatement ps = con.prepareStatement(sql);
            bindArgs(ps,args);
            return ps;
        };
    }
    // 对sql语句中的？赋值
    private void bindArgs(PreparedStatement ps, Object... args) throws SQLException{
        for (int i = 0; i < args.length; i++){
            ps.setObject(i+1,args[i]);
        }
    }

    // action 为函数，需要实现
    public <T> T execute(ConnectionCallback<T> action) throws DataAccessException{
        // 有事务时使用当前事务连接
        Connection current = TransactionUtils.getCurrentConnection();
        if (current != null) {
            try {
                return action.doInConnection(current);
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
        // 无事务使用dataSource获取新连接
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
        ResultSetMetaData meta = rs.getMetaData();
        int columns = meta.getColumnCount();
        String[] ss = new String[columns];
        for (int i = 1; i <= columns; i++){
            ss[i - 1] = rs.getString(i);
        }
        return Arrays.toString(ss);
//        return rs.getString(1);
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



