package com.mybatis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface TypeHandler<T> {
    void setParameter(PreparedStatement ps, int i, T value) throws SQLException;
    T getResult(ResultSet rs, String columnName) throws SQLException;
}
