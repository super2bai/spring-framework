### 什么是IOC/DI

**IOC Inversion of Control  控制反转**
就是把代码里面需要实现的对象创建、依赖的代码，反转给容器来实现
那么必然需要创建一个容器，同时需要一种描述来让容器知道需要创建的对象与对象的关系
这个描述最具体表现就是可配置的文件

**DI Dependency Injection  依赖注入**
就是指对象是被动接收依赖类而不是自己主动去找
换句话说就是指对象不是从容器中查找它依赖的类，而是在容器实例化的时候主动将它依赖的类注入给它

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
	* 通过调用父类AbstractApplicationContext的refresh()启动整个IoC容器对Bean定义的载入过程
	* AbstractRefreshableApplicationContext 子类的 loadBeanDefinitions()
	* AbstractBeanDefinitionReader读取Bean定义资源 loadBeanDefinitions()
		* 调用资源加载器的获取资源方法resourceLoader.getResource(location),获取到要加载的资源
		* 真正执行加载功能是其子类XmlBeanDefinitionReader的loadBeanDefinition()
	* 资源加载器获取要读取的资源
		* XmlBeanDefinitionReader通过调用其父类DefaultResourceLoader的getResource方法获取要加载的资源	
		* 真正执行加载功能是其子类XmlBeanDefinitionReader的loadBeanDefinitions()
	* XmlBeanDefinitionReader加载bean资源
		* 通过源码分析，载入Bean定义资源文件的最后一步是将Bean定义资源转换为Document对象，该过程由documentLoader实现
	* DocumentLoader将Bean定义资源转换成Document对象
		* DefaultDocumentLoader的loadDocument方法
		* 该解析过程调用JavaEE标准的JAXP标准进行处理
		
至此，Sping IOC容器根据定位的Bean定义资源文件，将其加载读入并转换成为Document对象过程完成。
接下来要继续分析Spring IOC容器将载入的Bean定义资源文件转换为Document对象之后，是如何将其解析为Spring IOC管理的Bean对象并将其注册到容器中的

**XmlBeanDefinitionReader解析载入Bean定义资源文件**
* doLoadBeanDefinitions
* Bean定义资源的载入解析
	* 通过调用XML解析器将Bean定义资源文件转换得到Document对象，这一步是载入
		* 这些Document对象并没有按照Spring的Bean规则进行解析
	* 在完成通用的XML解析之后，按照Spring的Bean规则对Document对象进行解析
		* 接口BeanDefinitionDocumentReader的实现类DefaultBeanDefinitionDocumentReader实现的
		
**DefaultBeanDefinitionDocumentReader对Bean定义的Document对象解析**
* BeanDefinitionDocumentReader 接口通过 registerBeanDefinitions 方法调用其实现类 DefaultBeanDefinitionDocumentReader 对 Document 对象进行解析
	* <Import>---DefaultBeanDefinitionDocumentReader
	* <Alias>---DefaultBeanDefinitionDocumentReader
	* <Bean>---BeanDefinitionParserDelegate---parseBeanDefinitionElement

**BeanDefinitionParserDelegate 解析 Bean 定义资源文件中的<Bean>元素**
* BeanDefinitionParserDelegate
	* 配置文件中<Bean>元素中配置的属性就是通过该方法解析和设置到bean中的
	* 解析<Bean>元素过程中没有创建和实例化Bean对象，只是创建了Bean对象的定义类BeanDefinition
		* 将<Bean>元素中的配置信息设置到BeanDefinition中作为记录，当依赖注入时才使用这些记录信息创建和实例化具体的Bean对象
		
**BeanDefinitionParserDelegate 解析<property>元素**
* BeanDefinitionParserDelegate---parsePropertyElements
	* ref被封装为指向依赖对象的一个引用
	* value配置都会封装为一个字符串类型的对象
	* ref和value都通过"解析的数据类型属性值.setSource(extractSource(ele));"方法将属性值/引用与所引用的属性关联起来
	* 该方法的最后对于<property>元素的子元素通过parsePropertySubElement方法解析
	
**解析<property>子元素**
* BeanDefinitionParserDelegate---BeanDefinitionParserDelegate
	* Spring配置文件中，对<property>元素中配置的Array、List、Set、Map、Prop等各种集合子元素都通过上述方法解析，生成对应的数据对象
		* 如果ManagedList、ManagedSet等，这些Managed类是Spring对象BeanDefinition的数据封装，对集合数据类型的具体解析有各自的解析方法实现，解析方法的命名非常规范。
		
**解析<list>子元素**
* BeanDefinitionParserDelegate---parseListElement

经过对Spring Bean定义资源文件转换的Document对象中的元素层层解析，Spring IoC现在已经将XML性质定义的Bean定义资源文件转换为Spring IoC所识别的数据结构---BeanDefinition，它是Bean定义资源文件中配置POJO对象在Spring IoC容器中的映射，可以通过AbstractBeanDefinition为入口，看到了IoC容器进行索引、查询和操作

通过Spring IoC容器对Bean定义资源的解析后，IoC容器大致完成了管理Bean对象的准备工作，即初始化过程，但是最为重要的依赖注入还没有发生，现在在IoC容器中BeanDefinition存储的只是一些静态信息，接下来需要向同期注册Bean定义信息才能全部完成IoC容器的初始化过程。

**解析过后的BeanDefinition在IoC容器中的注册**






















