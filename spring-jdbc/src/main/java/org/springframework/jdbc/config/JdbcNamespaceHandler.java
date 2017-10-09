/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.config;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@link NamespaceHandler} for JDBC configuration namespace.
 * @author Oliver Gierke
 * @author Dave Syer
 */
public class JdbcNamespaceHandler extends NamespaceHandlerSupport {

	/**
	 * NOTE<br>
	 * 2017-10-08<br>
	 * Spring JDBC设计原理及二次开发<br>
	 * 
	 * config模块<br>
	 * 
	 * 处理jdbc配置的命名空间
	 */
	@Override
	public void init() {
		// 解析<embedded-database>元素
		// 使用EmbeddedDatabaseFactoryBean创建一个BeanDefinition
		// 引用了org.w3c.dom
		registerBeanDefinitionParser("embedded-database", new EmbeddedDatabaseBeanDefinitionParser());
		registerBeanDefinitionParser("initialize-database", new InitializeDatabaseBeanDefinitionParser());
	}
}
