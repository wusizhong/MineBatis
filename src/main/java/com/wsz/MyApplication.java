package com.wsz;

import com.mybatis.MapperProxyFactory;

import java.util.List;

public class MyApplication {
    public static void main(String[] args) {
        UserMapper userMapper = MapperProxyFactory.getMapper(UserMapper.class);
        User result = userMapper.getUserById(1);

        System.out.println(result);
    }
}
