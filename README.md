主要工作原理：
1. 通过动态代理创建mapper接口的代理对象
   - 创建数据库连接
   - 构造PreparedStatement
   - 执行sql
   - 封装结果
   - 关闭连接
   - 返回结果
2. 通过代理对象，调用对应的方法，去执行sql

重点：通过TypeHandler去构造PreparedStatement（根据参数类型设置值）和封装结果（根据列类型获取值）