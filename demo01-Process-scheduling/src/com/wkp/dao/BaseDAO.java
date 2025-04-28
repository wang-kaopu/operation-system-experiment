package com.wkp.dao;

import com.wkp.util.JDBCUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;


public abstract class BaseDAO {
    public int executeUpdate(String sql, Object... args) throws SQLException {
        // 1. 获取连接
        Connection connection = JDBCUtils.getConnection();
        // 2. 编写preparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        // 3. 为占位符赋值
        int len = args.length;
        for (int i = 1; i <= len; ++i) {
            preparedStatement.setObject(i, args[i - 1]);
        }
        int executed = 0;
        try {
            // 4. 执行executeUpdate
            executed = preparedStatement.executeUpdate();
            // 5. 根据是否处于事务状态，考虑回收连接
            if (connection.getAutoCommit()) {
                JDBCUtils.freeConnection();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            // 6. 返回
            return executed;
        }
    }

    public <T> List<T> executeQuery(Class<T> clazz, String sql, Object... args) throws SQLException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        // 1. 获取链接
        Connection connection = JDBCUtils.getConnection();
        // 2. 创建PreparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        // 3. 为占位符赋值
        int len = args.length;
        for (int i = 1; i <= len; ++i) {
            preparedStatement.setObject(i, args[i - 1]);
        }
        // 4. 执行查询
        ResultSet resultSet = preparedStatement.executeQuery();
        // 6. 解析结果集
        ArrayList<T> list = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int count = metaData.getColumnCount();
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        while (resultSet.next()) {
            T t = constructor.newInstance();
            for (int i = 1; i <= count; ++i) {
                Object value = resultSet.getObject(i);
                String columnLabel = metaData.getColumnLabel(i);
                Field field = clazz.getDeclaredField(columnLabel);
                field.setAccessible(true);
                field.set(t, value);
            }
            list.add(t);
        }
        // 5. 考虑是否回收连接
        if (connection.getAutoCommit()) {
            JDBCUtils.freeConnection();
        }
        return list;
    }
}
