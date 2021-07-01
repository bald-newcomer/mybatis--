package org.apache.ibatis;

public class HRecord {
  /**
   * ！！！！！！！！！！
   * mybatis为ORM(对象关系映射)框架
   * 用于实现面向对象编程语言里不同类型系统的数据之间的转换******重点（可见org.apache.ibatis.executor.resultSet->ResultSetWrapper方法）
   * 该方法中三次遍历，获取三个字段信息,此过程中实现类型转化
   * 例：classesNames=String columnNames=username jdbcType=VARCHAR  即String与VARCHAR转化（ORM框架核心精髓）
   * mybatis核心：
   */


  /**
   * mybatis mappers加载有4种方式
   * 1.resource
   * 2.url
   * 3.class
   * 4.package（优先级最高） 源码见XMLConfigBuilder.java类中mapperElement方法有优先级识别顺序
   * 4种加载方式可进入config文件中<mappers></mappers>标签中查看
   */

  /**
   * mybatis在获取到sql语句后需要通过执行器来执行该sql操作数据库（mybatis顶级执行器Executor,与线程执行器同名，但不同包下，作用也不同）
   * mybatis一级缓存默认开启，源码见Configuration文件中cacheEnable=true
   * 其中执行器有3种（可见org.apache.ibatis.session->ExecutorType）
   * 1.SIMPLE(简单) 来一次访问一次
   * 2.REUSE（复用） 复用访问数据库链接
   * 3.BATCH（批量操作）一次性操作多数
   * 默认使用SIMPLE
   */

  /**
   * mybatis缓存
   * 缓存的作用：自己看
   * 不足：修改数据库数据后，会出现数据一致性问题
   * 缓存的使用：（详见baseExecutor类）缓存会生成CacheKey key，相当于一条sql,此后的查询会拿此key在localCache中查找数据
   *  若localCache中不存在此key,sql执行前会将此key存入localCache
   *
   *
   */

  /**
   * mybatis主流程
   * 1).获取数据库源（driver url username password）
   * 2).获取执行语句 （select insert update delete）
   * 3).通过执行语句操作数据库 （connection prepareStatement resultSet）
   */

  /**
   * 主流程1)
   * Mybatis获取数据库源-源码过程
   * 1.org.apache.ibatis.session.SqlSessionFactoryBuilder.build（inputStream）config配置信息转流，构建SlqSessionFactory
   * 2.org.apache.ibatis.builder.xml.XMLConfigBuilder.parse 解析xml文件
   * 3.org.apache.ibatis.builder.xml.XMLConfigBuilder.parseConfiguration 解析Configuration标签
   * 4.org.apache.ibatis.builder.xml.XMLConfigBuilder.environmentElement 解析environment标签
   * 5.org.apache.ibatis.builder.xml.XMLConfigBuilder.dataSourceElement 解析dateSource标签
   * 6.org.apache.ibatis.session.Configuration.setEnvironment 获取连接参数并赋值给全局配置，（openSession会对执行器初始化：后期sql的执行会get该全局配置）
   *   且org.apache.ibatis.mapping中的Environment类中参数与config文件中的标签所对应
   */

  /**
   * 主流程2)
   * sql执行语句的获取及解析-源码过程 （select insert update delete）
   * 1.org.apache.ibatis.session.SqlSessionFactoryBuilder.build（inputStream）config配置信息转流，构建SlqSessionFactory
   * 2.org.apache.ibatis.builder.xml.XMLConfigBuilder.parse 解析xml文件
   * 3.org.apache.ibatis.builder.xml.XMLConfigBuilder.parseConfiguration 解析Configuration标签
   * 4.org.apache.ibatis.builder.xml.XMLConfigBuilder.mapperElement 解析mappers获取标签下内容
   * 5.org.apache.ibatis.builder.xml.XMLMapperBuilder.configurationElement 解析获取select标签下具体sql及相关信息
   * 6.org.apache.ibatis.builder.xml.XMLStatementBuilder.parseStatementNode 解析标签下属性内容赋值给临时变量
   * 7.org.apache.ibatis.session.Configuration.addMappedStatement 最终将由上一步解析所获得的标签下属性内容赋值给全局配置（openSession会对执行器初始化：后期sql的执行会get该全局配置）
   *   且通过selectOne->（org.apache.ibatis.session.defaults）selectList中configuration.getMappedElement(statement)获取之前解析到的sql语句
   */

  /**
   * 主流程3）
   * 通过sql执行语句操作数据库-源码过程
   * 1.org.apache.ibatis.session.DefaultSqlSessionFactory.openSession 创建SqlSession
   * 2.org.apache.ibatis.session.Configuration.newExecutor(org.apache.ibatis.transaction,org.apache.ibatis.session)
   * 3.org.apache.ibatis.executor.SimpleExecutor 创建执行器，若未设置，默认创建Simple执行器
   * 4.org.apache.ibatis.session.defaults.DefaultSqlSession.selectOne(java.lang.String,java.lang.Object) 
   * 5.org.apache.ibatis.session.defaults.DefaultSqlSession.selectList(java.lang.String,java.lang.Object)
   * 6.org.apache.ibatis.executor.CacheExecutor.query(org.apache.ibatis.mapping.MappedStatement,java.lang.Object,org.apache.ibatis.session.RowBounds,org.apache.ibatis.session.ResultHandler)
   * 7.org.apache.ibatis.executor.CacheExecutor.query(org.apache.ibatis.mapping.MappedStatement,java.lang.Object,org.apache.ibatis.session.RowBounds,org.apache.ibatis.session.ResultHandler,org.apache.ibatis.cache.CacheKey,org.apache.ibatis.mapping.BoundSql)
   * 8.org.apache.ibatis.executor.BaseExecutor.queryFromDatabase
   * 9.org.apache.ibatis.executor.SimpleExecutor.doQuery
   * 10.org.apache.ibatis.executor.statement.PreparedStatementHandler.query
   * 11.org.apache.ibatis.executor.resultSet.DefaultResultSetHandler.handleResultSets
   *
   */




}
