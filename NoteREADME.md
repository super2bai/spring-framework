### 什么是IOC/DI

**IOC Inversion of Control  控制反转**
就是把代码里面需要实现的对象创建、依赖的代码，反转给容器来实现
那么必然需要创建一个容器，同时需要一种描述来让容器直到需要创建的对象与对象的关系
这个描述最具体表现就是可配置的文件

**DI Dependency Injection  依赖注入**
就是指对象是被动接收依赖类贰不是自己主动去找
换句话说就是指对象不是从容器中查找它依赖的类，而是在容器实例话的时候主动将它依赖的类注入给它

**对象和对象关系怎么表示:**
* XML
* properties文件
* 等语义化配置文件表示

**描述对象关系的文件存放在:**
* classpath
* filesystem
* URL网络资源
* servletContext等

### Spring IOC体系结构

**BeanFactory**
* 只对IOC容器的基本行为作了定义，不关心如何加载
* Spring Bean的创建是典型的工厂模式，这一些列的Bean工厂
* 也即IOC容器为开发者管理对象间的依赖关系提供了很多便利和基础服务
* BeanFactory作为最顶层的一个接口类,它定义了IOC容器的基本功能规范，子类有：
	*  ListableBeanFactory(可列表)、
	*  HierarchicalBeanFactory(有继承关系)
	*  AutowaireCapableBeanFactory(定义Bean的自动装配规则)
* 最终实现是DefaultListableBeanFactory
* 这四个接口共同定义了Bean的集合、之间的关系和行为
* 如何生产对象，要看IOC容器的实现
	* XmlBeanFactory
		* 最基本的IOC容器的实现
		* 读取XML文件定义的BeanDefinition
	* ClasspathXmlApplicationContext
		* 高级的IOC容器
		* 提供IOC容器的基本功能
		* 附加服务
			* 支持信息源，可以实现国际化
			* 访问资源
			* 支持应用事件
			
**BeanDefinition**
* Bean对象在Spring实现中是以BeanDefinition来描述的
* Bean的解析主要就是对Spring配置见的解析
	
	
### IOC容器的初始化
**IOC容器的初始化**
* BeanDefinition的Resource定位
* BeanDefinition的Resource载入
* BeanDefinition的Resource注册
> ApplicationContext允许上下文嵌套，通过保持父上下文可以维持一个上下文体系。对于bean的查找可以在这个上下文体系中发生，首先检查当前上下文，其次是父上下文，逐级向上，这样为不同的Spring应用提供了一个共享的bean定义环境。


**IOC容器的创建过程**
* XmlBeanFactory


	//根据 Xml 配置文件创建 Resource 资源对象，该对象中包含了 BeanDefinition 的信息 ClassPathResource resource =new ClassPathResource("application-context.xml");
	//创建 DefaultListableBeanFactory
	DefaultListableBeanFactory factory =new DefaultListableBeanFactory();
	//创建 XmlBeanDefinitionReader 读取器，用于载入 BeanDefinition。之所以需要 BeanFactory 作为参数，是因为会 将读取的信息回调配置给 factory
	XmlBeanDefinitionReader reader =new XmlBeanDefinitionReader(factory);
	//XmlBeanDefinitionReader 执行载入 BeanDefinition 的方法，最后会完成 Bean 的载入和注册。完成后 Bean 就成功 的放置到 IOC 容器当中，以后我们就可以从中取得 Bean 来使用
	reader.loadBeanDefinitions(resource);
	
* FileSystemXmlApplicationContext	