package com.mybatis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegerTypeHandler implements TypeHandler<Integer>{
    @Override
    public void setParameter(PreparedStatement ps, int i, Integer value) throws SQLException {
        ps.setInt(i, value);
    }

    @Override
    public Integer getResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getInt(columnName);
    }
}
