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
	
**解析property元素的子元素**
* BeanDefinitionParserDelegate---BeanDefinitionParserDelegate
	* Spring配置文件中，对<property>元素中配置的Array、List、Set、Map、Prop等各种集合子元素都通过上述方法解析，生成对应的数据对象
		* 如果ManagedList、ManagedSet等，这些Managed类是Spring对象BeanDefinition的数据封装，对集合数据类型的具体解析有各自的解析方法实现，解析方法的命名非常规范。
		
**解析list元素的子元素**
* BeanDefinitionParserDelegate---parseListElement

经过对Spring Bean定义资源文件转换的Document对象中的元素层层解析，Spring IoC现在已经将XML性质定义的Bean定义资源文件转换为Spring IoC所识别的数据结构---BeanDefinition，它是Bean定义资源文件中配置POJO对象在Spring IoC容器中的映射，可以通过AbstractBeanDefinition为入口，看到了IoC容器进行索引、查询和操作

通过Spring IoC容器对Bean定义资源的解析后，IoC容器大致完成了管理Bean对象的准备工作，即初始化过程，但是最为重要的依赖注入还没有发生，现在在IoC容器中BeanDefinition存储的只是一些静态信息，接下来需要向同期注册Bean定义信息才能全部完成IoC容器的初始化过程。

继续跟踪程序的执行顺序，接下来会到分析DefaultBeanDefinitionDocumentReader对Bean定义转换的Document对象解析的流程中，在其parseDefaultElement方法中完成对Document对象的解析后得到BeanDefinition的BeanDefinitionHolder对象，然后在processBeanDefinition方法里调用BeanDefinitionReaderUtils的registerBeanDefinition向IoC容器注册解析的Bean。

**解析过后的BeanDefinition在IoC容器中的注册**
* BeanDefinitionReaderUtils---registerBeanDefinition
	* 当调用BeanDefinitionReaderUtils向IoC容器注册解析的BeanDefinition时，真正完成注册功能的是DefaultListableBeanFactory


**DefaultListableBeanFactory 向 IOC 容器注册解析后的 BeanDefinition**
* 使用一个HashMap的集合对象存放IoC容器中注册解析的BeanDefinitio，向容器注册的主要源码
* registerBeanDefinition方法向IoC容器注册

至此，Bean定义资源文件中配置的Bean被解析过后，已经注册到IoC容器中，被容器管理起来，真正完成了IoC容器初始化所做的全部工作。现在IoC容器中已经建立了整个Bean的配置信息，这些BeanDefinition信息已经可以使用，并且可以被检索，IoC容器的作用就是对这些注册的Bean定义信息进行处理和维护。这些注册的Bean定义信息是IoC容器控制反转的基础，正是有了这些注册的数据，容器才可以进行依赖注入。

<hr>

**总结**

**IoC容器初始化**
* 初始化的入口在容器实现中的refresh()调用完成
* 对bean定义载入IoC容器使用的方法是loadBeanDefinition
> 其中大致过程如下：通过ResourceLoader来完成资源文件位置的定位，DefaultResourceLoader是默认的实现，同时上下文本身就给出了ResourceLoader的实现，可以从类路径、文件系统、URL等方式来定位资源位置。如果是XmlBeanFactory作为IoC容器，那么需要为它指定bean定义的资源，也就是说bean定义文件时通过抽象成Resource来被IoC容器处理的，容器通过BeanDefinitionReader来完成定义信息的解析和Bean信息的注册，往往使用的是XmlBeanDefinitionReader来解析bean的xml定义文件，实际的处理过程是委托给BeanDefinitionParserDelegate来完成的，从而得到bean的定义信息，这些信息在Spring中使用BeanDefinition对象来表示，这个名字可以联想到loadBeanDefinition，RegisterBeanDefinition这些相关方法。都是为处理BeanDefinition服务的，容器解析得到BeanDefinition IoC以后，需要把它在IoC容器中注册，这由IoC实现BeanDefinitionRegistry接口来实现。注册过程就是在IoC容器内部维护一个HashMap来保存得到BeanDefinnition的过程。这个HashMap是IoC容器持有bean信息的场所，以后对bean的操作都是围绕这个HashMap来实现的

* 然后就可以通过BeanFactory和ApplicationContext来享受Spring IoC的服务了。在使用IoC容器的时候，除了少量的粘合代码，绝大多数以正确IoC风格标写的应用程序代码完全不用关心如何到达工厂，因为容器将把这些对象与容器管理的其他对象钩在一起。基本的策略是把工厂放到已知的地方，最好是放在对预期使用的上下文有意义的地方，以及代码将实际需要访问工厂的地方。Spring本身提供了对声明式载入web应用程序用法的应用程序上下文，并将其存储在ServletContext中的框架实现。


**在使用SpringIoC容器的时候还需要区别两个概念**
* BeanFactory
	* IoC容器的编程抽象
		* 比如ApplicationContext、XmlBeanFactory等，这些都是容器的具体表现，需要使用什么的容器由用户决定，但Spring提供了丰富的选择。
* FactoryBean
	* 只是一个可以在IoC容器中被管理的一个bean，是对各种处理裹层和资源使用的抽象，而不返回FactoryBean本身，可以把它堪称是一个抽象工厂，对它的调用返回的是工厂生产的产品，当使用容器中的FactoryBean的时候，该容器不会返回FactoryBean本身，而是返回其生成的对象。Spring包括了大部分的通用资源和服务访问抽象的FactoryBean的实现（下方列出），这些都可以看成是具体的工厂，堪称是Spring建立好的工厂。也就是说Spring通过使用抽象工厂模式准备了一系列工厂来生产一些特定的对象，免除手工重复的工作，要使用时只需要在IoC容器里配置好就能很方便的使用了
		* 对JNDI查询的处理
		* 对代理对象的处理
		* 对事务性代理的处理
		* 对RMI代理的处理
		* ...


### IoC容器的依赖注入

**依赖注入发生的时间**
当Spring IoC容器完成了Bean定义资源的定位、载入和解析注册以后，IoC容器中已经管理类Bean定义的相关数据，但是此时IoC容器还没有对所管理的Bean进行依赖注入，依赖注入在以下两种情况发生：
* 用户第一次通过getBean方法向IoC容器获取Bean时，IoC容器触发依赖注入
* 当用户在Bean定义资源中为<Bean>元素配置了lazy-init属性，即让容器在解析注册Beean定义时进行预实例化，触发依赖注入。
BeanFactory接口定义了Spring IoC容器的基本功能规范。BeanFactory接口定义了几个getBean方法，就是用户向IoC容器索取管理的Bean的方法。具体实现在AbstractBeanFactory中。

**AbstractBeanFactory通过getBean向IoC容器获取被管理的Bean**
* AbstractBeanFactory---doGetBean

根据Bean定义方式不同，采取不同的创建Bean实例对象的策略
* 单例(Singleton)
	* 则容器在创建之前先从缓存中查找
		* 确保整个容器中只存在一个实例对象
* 原型(Prototype)
	* 则容器每次都会创建一个新的实例对象
* 其他
	* 扩展为指定其生命周期范围
	
具体的Bean实例对象的创建过程由实现了ObjectFactory接口的匿名内部类的createBean方法完成。ObjectFactory使用了委派模式，具体的Bean实例创建过程交由其实现类AbstractAutowireCapableBeanFactory完成。
	





















































