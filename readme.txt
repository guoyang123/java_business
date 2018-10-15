v1.0
项目开发采用四层架构：
 view->视图层
 controller -->控制层 ,接受前端的输入并调用service层。
 service-->业务逻辑层 处理业务逻辑并调用dao层。
 dao-->持久层  操作数据库    MyBatis
 接口隔离原则 ---》扩展
  Dao接口和Dao的实现类
  service接口和service实现类

  vo -->view object (value object)


  db--->pojo (123434234234)--->vo  --> 2018-10-11 21:23:32

  md5+salt(盐值)

