/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.autoconstructor;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * sqlSessionFactory:创建sqlSession，内部管理着一个configuration
 */
class AutoConstructorTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/autoconstructor/mybatis-config.xml")) {

      //manage a myBatis configuration
      //建立sqlSessionFactory，读取xml配置文件存入配置，生命周期内有效
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/autoconstructor/CreateDB.sql");
  }

  /**
   * sqlSession.getMapper()
   * 使用 SqlSession 创建相关接口的代理对象
   * <p>
   * 获取对应mapper过程(该过程使用了动态代理)
   * Mapper接口被到注册到了MapperRegistry中，放在其名为knowMappers 的HashMap属性中
   * 在MapperRegistry类的addMapper()方法中，knownMappers.put(type, new MapperProxyFactory<T>(type));
   * 相当于把：诸如BlogMapper 之类的Mapper接口被添加到了MapperRegistry 中的一个HashMap中。并以 Mapper 接口的 Class 对象作为 Key
   * 以一个携带Mapper接口作为属性的MapperProxyFactory 实例作为value
   */
  @Test
  void fullyPopulatedSubject() {
    //通过sqlSessionFactory.openSession()，创建Sqlsession
    //Sqlsession对应着一次数据库会话，可以执行多次sql，当一旦关闭了Sqlsession就需要重新创建它
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      //根据条件查询获取数据库数据
      final Object subject = mapper.getSubject(1);
      assertNotNull(subject);
    }
  }

  /**
   * 报错信息：
   * PrimitiveSubject with invalid types (int,String,int,int,int,boolean,Date)
   * or values
   * (2,b,10,null,45,true,Wed Jun 30 01:27:53 CST 2021).
   * <p>
   * 参数 null 不能赋值给 int类型，所以要用包装类
   * <p>
   */
  @Test
  void primitiveSubjects() {

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      assertThrows(PersistenceException.class, mapper::getSubjects);
    }
  }

  @Test
  void annotatedSubject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      verifySubjects(mapper.getAnnotatedSubjects());
    }
  }

  @Test
  void badSubject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      assertThrows(PersistenceException.class, mapper::getBadSubjects);
    }
  }

  @Test
  void extensiveSubject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      verifySubjects(mapper.getExtensiveSubjects());
    }
  }

  private void verifySubjects(final List<?> subjects) {
    assertNotNull(subjects);
    Assertions.assertThat(subjects.size()).isEqualTo(3);
  }
}
