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
package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SqlSourceBuilderTest {

  private static Configuration configuration;
  private static SqlSourceBuilder sqlSourceBuilder;
  private final String sqlFromXml = "\t\n\n  SELECT * \n        FROM user\n \t        WHERE user_id = 1\n\t  ";

  @BeforeEach
  void setUp() {
    configuration = new Configuration();

    sqlSourceBuilder = new SqlSourceBuilder(configuration);
  }

  @Test
  void testShrinkWhitespacesInSqlIsFalse() {
    //采用SqlSourceBuilder中的configuration，typeAliasRegistry，typeHandlerRegistry 解析sql
    SqlSource sqlSource = sqlSourceBuilder.parse(sqlFromXml, null, null);
    //解析完后是最中的BoundSql
    BoundSql boundSql = sqlSource.getBoundSql(null);
    String actual = boundSql.getSql();
    Assertions.assertEquals(sqlFromXml, actual);
  }

  @Test
  void testShrinkWhitespacesInSqlIsTrue() {
    //该设置会严格去除空格
    configuration.setShrinkWhitespacesInSql(true);
    SqlSource sqlSource = sqlSourceBuilder.parse(sqlFromXml, null, null);
    BoundSql boundSql = sqlSource.getBoundSql(null);
    //在 sqlSourceBuilder 中 采用的是 StaticSqlSource 直接返回的是 sqlSourceBuilder 解析的sql 没有添加分页等信息
    String actual = boundSql.getSql();

    String shrankWhitespacesInSql = "SELECT * FROM user WHERE user_id = 1";
    Assertions.assertEquals(shrankWhitespacesInSql, actual);
  }
}
