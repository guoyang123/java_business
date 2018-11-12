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

递归查询
    id    名称       parentid
    1    电子产品      0
    2    手机          1
    3     家电         1
    4     彩电         3
    5     冰箱         3
    6     小米手机     2
    7     小米8        6
    8      mi8-32G     7
    查询电子产品的所有类别?categoryId=1
    step1:查询出点子产品类
    step2: 查询电子产品的子类 List<categoryid> 2,3
    step3:查询categorid=2类别的子类   List   6
    step4:查询categoryid=3类别的子类  List 4,5
    。。。


















