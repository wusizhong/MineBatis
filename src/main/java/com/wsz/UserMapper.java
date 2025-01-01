package com.wsz;

import com.mybatis.Param;
import com.mybatis.Select;

import java.util.List;

public interface UserMapper {
    @Select("select * from user where name = #{name} and age = #{age}")
    public List<User> getUserList(@Param("name") String name, @Param("age") int age);

    @Select("select * from user where name = #{name}")
    public List<User> getUserListByName(@Param("name") String name);

    @Select("select * from user where id = #{id}")
    public User getUserById(@Param("id") Integer id);
}
