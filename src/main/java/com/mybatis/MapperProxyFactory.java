package com.mybatis;

import com.wsz.User;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class MapperProxyFactory {
    private static Map<Class, TypeHandler> typeHandlerMap = new HashMap<>();

    static {
        typeHandlerMap.put(String.class, new StringTypeHandler());
        typeHandlerMap.put(Integer.class, new IntegerTypeHandler());

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static <T> T getMapper(Class<T> mapper) {
        Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{mapper}, new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //解析sql-->执行sql-->封装结果
                //JDBC
                //1、创建数据库连接
                Connection connection = getConnection();

                Select annotation = method.getAnnotation(Select.class);
                String sql = annotation.value();
                System.out.println(sql);

                //参数名：参数值
                //"name": "wsz"
                //"age": 18
                //获取参数名和值的映射
                Map<String, Object> paramValueMap = new HashMap<>();
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    String name = parameter.getAnnotation(Param.class).value();
                    paramValueMap.put(name, args[i]);
                    paramValueMap.put(parameter.getName(), args[i]);
                }

                ParameterMappingTokenHandler tokenHandler = new ParameterMappingTokenHandler();
                GenericTokenParser parser = new GenericTokenParser("#{", "}", tokenHandler);
                String parseSql = parser.parse(sql);
                //获取参数名
                List<ParameterMapping> parameterMappings = tokenHandler.getParameterMappings();

                //2、构造PreparedStatement
                PreparedStatement preparedStatement = connection.prepareStatement(parseSql);
                for (int i = 0; i < parameterMappings.size(); i++) {
                    String property = parameterMappings.get(i).getProperty();
                    Object value = paramValueMap.get(property);
                    Class<?> type = value.getClass();
                    typeHandlerMap.get(type).setParameter(preparedStatement, i + 1, value);
                }

                //3、执行SQL
                ResultSet resultSet = preparedStatement.executeQuery();
                //4、封装结果
                //获取返回值类型
                Class resultType = null;
                Type genericReturnType = method.getGenericReturnType();
                if (genericReturnType instanceof Class) {
                    //不是泛型
                    resultType = (Class) genericReturnType;
                } else if (genericReturnType instanceof ParameterizedType) {
                    //是泛型
                    Type[] actualTypeArguments = ((ParameterizedType) genericReturnType).getActualTypeArguments();
                    resultType = (Class) actualTypeArguments[0];
                }

                //获取列名
                ResultSetMetaData metaData = resultSet.getMetaData();
                List<String> columnNames = new ArrayList<>();
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    columnNames.add(metaData.getColumnName(i + 1));
                }
                //获取列名和set方法的映射
                Map<String, Method> setMethodMap = new HashMap<>();
                for (Method declaredMethod : resultType.getDeclaredMethods()) {
                    if (declaredMethod.getName().startsWith("set")) {
                        String propertyName = declaredMethod.getName().substring(3);
                        propertyName = propertyName.substring(0, 1).toLowerCase(Locale.ROOT) + propertyName.substring(1);
                        setMethodMap.put(propertyName, declaredMethod);
                    }
                }
                Object result;
                List<Object> list = new ArrayList<>();
                while (resultSet.next()) {
                    Object instance = resultType.newInstance();
                    for (int i = 0; i < columnNames.size(); i++) {
                        String columnName = columnNames.get(i);
                        Method setMethod = setMethodMap.get(columnName);
                        Class<?> clazz = setMethod.getParameterTypes()[0];
                        TypeHandler typeHandler = typeHandlerMap.get(clazz);
                        setMethod.invoke(instance, typeHandler.getResult(resultSet, columnName));
                    }
                    list.add(instance);
                }

                if(method.getReturnType().equals(List.class)) {
                    result = list;
                }else {
                    result = list.get(0);
                }


                //5、关闭连接
                connection.close();
                return result;
            }
        });

        return (T) proxyInstance;
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/test?serverTimezone=GMT%2B8", "root", "root");
    }

}
