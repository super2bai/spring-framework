### 一、什么是IOC/DI

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

### 二、Spring IOC体系结构

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
	
	
### 三、IoC容器的初始化
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


### 四、IoC容器的依赖注入

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
	
**AbstractAutowireCapableBeanFactory创建Bean实例对象**
AbstractAutowireCapableBeanFactory类实现了ObjectFactoryj接口，创建容器指定的的Bean实例对象，同时还对创建的Bean实例对象进行初始化处理。
* AbstractAutowireCapableBeanFactory---createBean
具体的依赖注入实现在：
* .createBeanInstance
	* 生成Bean所包含的java实例对象
* .populateBean
	* 对Bean属性的依赖注入进行管理

**createBeanInstance方法创建Bean的java实例对象**
容器初始化生成Bean所包含的Java实例对象
* AbstractAutowireCapableBeanFactory---createBeanInstance
* 对使用工厂方法和自动装配特性的Bean的实例化比较清除，调用相应的工厂方法或者参数匹配的构造方法即可完成实例化对象的工作，但是对于最常使用的默认无参构造方法就需要使用相应的初始化策略(JDK的反射机制或者CGLIB)来进行初始化了，在方法getInstantiationStrategy().instantiate中就具体实现类使用初始策略实例化对象。

**SimpleInstantiationStrategy 类使用默认的无参构造方法创建 Bean 实例化对象**
容器初始化生成Bean所包含的Java实例对象
* SimpleInstantiationStrategy---instantiate
* Bean的方法是否被覆盖
	* 被覆盖
		* 使用JDK的反射机制进行实例化
	* 无
		* CGLIB进行实例化
* CGLIB是一个常用的字节码生成器的类库，它提供了一系列API实现java字节码的生成和转换功能。JDK的动态代理只能针对接口，如果一个类没有实现任何接口，要对其进行动态代理只能使用CGLIB.

**populateBean方法对Bean属性的依赖注入**
生成对象后，Spring IoC容器是如何将Bean的属性依赖关系注入Bean实例对象中并设置好。
* AbstractAutowireCapableBeanFactory---populateBean
* AbstractAutowireCapableBeanFactory---applyPropertyValues
* 对属性的注入过程分以下两种情况
	* 属性值不需要类型转换时，不需要解析属性值，直接准备进行依赖注入
	* 属性值需要进行类型转换时，如对其他对象的引用等
		* 首先需要解析属性值
		* 然后对解析后的属性值进行依赖注入
对属性值的解析是在BeanDefinitionValueResolver类的resolveValueIfNecessary方法中进行的，对属性值的依赖注入是通过bw.setPropertyValues方法实现的，在分析属性值的依赖注入之前，分析以下对属性值的解析过程。

**BeanDefinitionValueResolver解析属性值**
当容器在对属性进行依赖注入时，如果发现属性值需要进行类型转换，如属性值是容器中另一个Bean实例对象的引用，则容器首先需要根据属性值解析出所医用的对象，然后才能将该引用对象注入到目标实例对象的属性上去，对属性进行解析的由resolveValueIfNecessary方法实现
* BeanDefinitionValueResolver---resolveValueIfNecessary
* BeanDefinitionValueResolver---resolveReference
* BeanDefinitionValueResolver---resolveManagedArray
* BeanDefinitionValueResolver---resolveManagedList
* BeanDefinitionValueResolver---resolveManagedSet
* BeanDefinitionValueResolver---resolveManagedMap
通过上面的代码分析，明白了Spring是如何将引用类型，内部类以及集合类型等属性进行解析的，属性值解析完成后就可以进行依赖注入了，依赖注入的过程就是Bean对象实例设置到它所依赖的Bean对象舒心上去，依赖注入是通过bw.setPropertyValues方法实现的，该方法也使用了委托模式，在BeanWrapper接口中至少定义了方法声明，依赖注入的具体实现交由其实现类BeanWrapperImpl来完成。

**BeanWrapperImpl对Bean属性的依赖注入**
BeanWrapperImpl类主要是对容器中完成初始化的Bean实例对象进行属性的依赖注入，即把Bean对象设置到它所依赖的另一个Bean的属性中去。
* AbstractNestablePropertyAccessor---setPropertyValue
* Spring IoC容器是如何将属性的值注入到Bean实例对象中
	* 集合类型的属性
		* 将其属性值解析为目标类型的集合后直接赋值给属性
	* 非集合类型的属性
		* 大量使用了JDk的反射和内省机制，通过属性的getter方法获取指定属性注入以前的值
		* 调用属性的setter方法为属性设置注入后的值
		
至此Spring IoC容器对Bean定义资源文件的定位，载入、解析和依赖注入已经全部分析完毕，现在Spring IoC容器中管理了一系列靠依赖关系联系起来的Bean，程序不需要应用自己手动创建所需的对象，Spring IoC容器会在使用的时候自动创建，并且注入好相关的依赖，这就是Spring核心功能的控制反转和依赖注入的相关功能

### 五、IoC容器的高级特性
* 介绍
之前介绍了Spring Ioc容器对Bean资源的定位，读取和解析过程，同时也清楚了当用户通过getBean方向向IoC容器获取被管理的Bean时，IoC容器对Bean进行的初始化和依赖注入的过程，这些是Spring IoC容器的基本功能特性。

Spring IoC还有一些高级特性，如使用lazy-init属性对Bean预初始化、FactoryBean产生或者修饰Bean对象的生成、IoC容器初始化Bean过程中使用BeanPostProcessor后置处理器对Bean生命周期事件管理和IoC容器的autowiring自动装配功能等。

* Spring IoC容器的lazy-init属性实现预实例化
通过前面对IoC容器的实现和工作原理分析，知道了IoC容器的初始化过程就是对Bean定义资源的定位、载入和注册，此时容器对Bean的依赖注入并没有发生，依赖注入主要是在应用程序第一次向容器索取Bean时，通过getBean方法的调用完成。

当Bean定义资源的 Bean元素中配置了lazy-init属性时，容器将会在初始化的时候对所配置的Bean进行预实例化，Bean的依赖注入在容器初始化的时候已经完成。这样，当应用程序第一次向容器索取被管理的Bean时，就不用再初始化和对Bean进行依赖注入了，直接从容器中获取已经完成依赖注入的现成的Bean，可以提高应用第一次向容器获取Bean的性能。
	* refresh
		* AbstractApplicationContext---refresh()
		* 先从IoC容器的初始化过程开始，通过之前的分析，知道IoC容器读入已经定位的Bean定义资源是从refresh方法开始的。
	* finishBeanFactoryInitialization处理预实例化Bean
		* AbstractApplicationContext---finishBeanFactoryInitialization
	* DefaultListableBeanFactory 对配置 lazy-init 属性单态 Bean 的预实例化
		* DefaultListableBeanFactory---preInstantiateSingletons
		* 如果设置了lazy-init属性，则容器在完成Bean定义的注册之后，会通过getBean方法，触发对指定Bean的初始化和依赖注入过程，这样当应用第一次向容器索取所需的Bean时，ring 起不再需要对Bean进行初始化和依赖注入，直接从已经完成实例化和依赖注入的Bean中取一个现成的Bean，这样就提高了第一次获取Bean的性能。

* FactoryBean的实现
	* 背景知识
		* BeanFactory
			* Bean工厂，是一个工厂
			* Spring IoC容器的最顶层接口
			*  管理Bean
				* 实例化、定位、配置应用程序中的对象及建立这些对象见的依赖
		* FactoryBean
			* 工厂Bean，是一个Bean
			* 产生其他Bean实例
			* 通常情况下，这种bean没有什么特别的要求，仅需要提供一个工厂方法，该方法用来返回其他bean实例
			* 通常情况下，bean无需自己实现工厂模式，Spring容器担任工厂角色
			* 少数情况下，容器中的bean本身就是工厂，其作用是产生其他bean实例
	当用户使用容器本身时，可以使用转义字符“&”来得到Factory本身，以区别通过FactoryBean产生的实例对象和FactoryBean对象本身。
		如果myJndiObject是一个FactoryBean，使用&myJndiObject得到的是myJndiObject对象，而不是myJndiObject产生出来的对象。 
	* FactoryBean
	* AbstractBeanFactory---doGetBean
		* 调用FactoryBean			
		* AbstractBeanFactory---getObjectForBeanInstance
			* Dereference（解引用）
			* 在C/C++中应用比较多的术语，"*"是解引用符号，而"&"是引用符号
			* 解引用是指变量指向的是所引用对象的本身数据，而不是引用对象的内存地址
	* AbstractBeanFactory生产Bean实例对象
		* FactoryBeanRegistrySupport---getObjectFromFactoryBean
		* FactoryBeanRegistrySupport---doGetObjectFromFactoryBean
		* BeanFactory接口调用其实现类的getObject方法来实现创建Bean实例对象的功能	
	* 工厂Bean的实现类getObject方法创建Bean实例对象
		* FactoryBean的实现类非常多
			* Proxy
			* RMI
			* JNDI
			* ServletContextFactoryBean
			* ...
		* FactoryBean接口为Spring容器提供了一个很好的封装机制，具体的getObject有不同的实现类根据不同的实现政策来具体技工，分析一个最简单的AnnotationTestBeanFactory
			* AnnotationTestBeanFactory
			* 其他的Proxy、RMI、JNDI等等，都是根据相应的策略提供getObject的实现

* BeanPostProcessor后置处理器的实现
BeanPostProcessor后置处理器是Spring IoC容器经常使用到的一个特性，这个Bean后置处理器是一个监听器，可以监听容器触发的Bean生命周期事件。后置处理器向容器注册以后，容器中管理的Bean就具备了接收IoC容器事件回调的能力。
BeanPostProcessor的使用非常简单，只需要提供一个实现接口BeanPostProcessor的实现类，然后在Bean的配置文件中设置即可。
	* BeanPostProcessor
		* 这两个回调的入口都是和容器管理的Bean的生命周期事件紧密相关，可以为用户提供在Spring IoC容器初始化Bean过程中自定义的处理操作
	* AbstractAutowireCapableBeanFactory类对容器生成的Bean添加后置处理器
		* AbstractAutowireCapableBeanFactory---doCreateBean
			* BeanPostProcessor后置处理器的调用发生在Spring IoC容器完成对Bean实例对象的创建和属性的依赖注入完成之后，当应用程序第一次调用getBean方法（lazy-init预实例化除外）向Spring IoC容器索取指定Bean时触发Spring IoC容器创建Bean实例对象并进行依赖注入的过程，其中真正实现创建Bean对象并进行依赖注入的方法是AbstractAutowireCapableBeanFactory类的doCreateBean
	* AbstractAutowireCapableBeanFactory---initializeBean
		* 为容器产生的Bean实例对象添加BeanPostProcessor后置处理器的入口
		* AbstractAutowireCapableBeanFactory---applyBeanPostProcessorsAfterInitialization
			* BeanPostProcessor是一个接口，其初始化前的操作方法和初始化后的操作放啊均委托其实现子类来实现
				* AOP面向切面编程的注册通知适配器
				* Bean对象的数据校验
				* Bean继承属性/方法的合并
	* AdvisorAdapterRegistrationManager 
		* 其他的BeanPostProcessor接口实现类也类似，都是对Bean对象使用到的一些特性进行处理，或者向IoC容器中注册，为创建的Bean实例对象做一些自定义的功能增加，这些操作是容器初始化Bean时自动触发的，不需要人为的干预
			
* Spring IoC容器autowiring实现原理
	* Spring IoC容器提供了两种管理Bean依赖关系的方式
		* 显式管理
			* 通过BeanDefinition的属性值和构造方法实现Bean依赖关系管理
		* autowiring
			* Spring IoC容器的依赖自动装配功能，不需要对Bean属性的依赖关系做显式的声明，只需要在配置好autowiring属性，IoC容器会自动使用反射查找属性的类型和名称，然后基于属性的类型或者名称来自动装配容器中管理的Bean，从而自动的完成依赖注入
	* AbstractAutoWireCapableBeanFactory对Bean实例进行属性依赖注入
		* AbstractAutoWireCapableBeanFactory---populateBean
	* Spring IoC容器根据Bean名称或者类型进行autowiring自动依赖注入
		* AbstractAutoWireCapableBeanFactory---autowireByName
		* AbstractAutoWireCapableBeanFactory---autowireByType
		* 可以看出来通过属性名进行自动依赖注入的相对比通过属性类型进行自动依赖注入要稍微简单一些，但是真正实现属性注入的是DefaultSingletonBeanRegistry类的registerDependentBean
	* DefaultSingletonBeanRegistry的registerDependentBean方法对属性注入
		* DefaultSingletonBeanRegistry---registerDependentBean
通过对autowire的源码分析，可以看出，autowire的实现过程：
* 对Bean的属性调用getBean方法，完成依赖Bean的初始化和依赖注入
* 将依赖Bean的属性引用设置到被依赖的Bean上
* 将依赖Bean的名称和被依赖Bean的名称存储在IoC容器的集合中

Spring IoC容器的autowire属性自动依赖注入是一个很方便的特性，可以简化开发时的配置，但也存在不足：
* Bean的依赖关系在配置文件中无法很清楚的看出来，对于维护造成一定困难
* 由于自动依赖注入是Spring容器自动执行的，容器是不会智能判断的，如果配置不当，将带来无法预料的后果，所以自动依赖注入特性在使用时还是综合考虑
	
		

### 六、Spring AOP设计原理及具体实践
**SpringAOP应用示例**
* AOP(Aspect Oriented Programming)面向切面编程
	* 可以通过预编译方式和运行期动态代理实现在不修改源代码的情况下给程序动态统一添加功能的一种技术
	* AOP设计模式追求的是调用者和被调用者之间的解耦

* 相关概念
	* Aspect切面
		* 官方的抽象定义为“一个关注点的模块化，这个关注点可能会横切多个对象”
		* “切面“在ApplicationContext中<aop:aspect>来配置
	* Joinpoint连接点
		* 程序执行过程中的某一行为
			* 某个方法的调用
			* 某个方法抛出异常等行为
	* Advice通知
		* “切面”对于某个“连接点”所产生的动作
		* 一个“切面”可以包含多个“Advice”
	* Pointcut切入点
		* 匹配连接点的断言
		* 在AOP中通知和一个切入点表达式关联
		* 切面中的所有通知所关注的连接点，都由切入点表达式来决定
	* Target Object目标对象
		* 被一个或多个切面所通知的对象
		* 在实际运行时，Spring AOP采用代理实现，实际AOP操作的是TargetObject的代理对象
	* AOP Proxy AOP代理
		* 在Spring AOP中由两种代理方式
			* JDK动态代理
			* CGLIB代理
		* 默认情况下，TargetObject实现了接口时，则采用JDK动态代理，反之，采用CGLIB代理
		* 强制使用CGLIB大力需要将<aop:config>的proxy-target-class属性设置为true
		
* Advice通知 类型
	* Before advice 前置通知
		* 在某连接点JointPoint之前执行的通知，但这个通知不能阻止连接点前的执行
		* ApplicationContext中在<aop:aspect>里面使用<aop:before>
	* After advice 后置通知
		* 在某连接点退出的时候执行的通知(不论是正常返回还是异常退出)
		* ApplicationContext中在<aop:aspect>里面使用<aop:after>
	* After return advice 返回后通知
		* 在某连接点正常完成后执行的通知，不包括抛出异常的情况
		* ApplicationContext中在<aop:aspect>里面使用<after-returning>
	* Around advice环绕通知
		* 包围一个连接点的通知
		* 类似Web中Servlet规范中的Filter的doFilter方法
		* 可以在方法的调用前后完成自定义的行为，也可以选择不执行
		* ApplicationContext中在<aop:aspect>里面使用<aop:around>
	* After throwing advice 抛出异常后通知
		* 在方法抛出异常退出时执行的通知
		* ApplicationContext中在<aop:aspect>里面使用<aop:after-throwing>
注：可以将多个通知应用到一个目标对象上，即可以将多个切面织入到同一目标对象

* 使用Spring AOP
	* 注解
		* XML文件中声明激活自动扫描组件功能，同时激活自动代理功能（来测试AOP的注解功能）
		* 为Aspect切面类添加注解
	* XML

* Srping AOP难点
	* 理解AOP的理念和相关概念
	* 灵活掌握和使用切入点表达式

* execution
	* execution(modifiers-pattern? ret-type-pattern declaring-type-pattern? name-pattern(param-pattern) throws-pattern?
		* modifiers-pattenr
			* 方法的操作权限
		* ret-type-pattern
			* 返回值
			* 必选
		* declaring-type-pattern
			* 方法所在的包
		* name-pattern
			* 方法名
			* 必选
		* parm-pattern
			* 参数名
		* throws-pattern
			异常

* 访问当前的连接点
	* 每个通知方法第一个参数都是JoinPoint。其实，在Spring中，任何通知方法都可以将第一个参数定义为org.aspect.lang.JoinPoint类型用以接受当前连接点对象。
	* JoinPoint接口提供了一系列有用的方法
		* getArgs()
			* 返回方法参数
		* getThis()
			* 返回代理对象
		* getTarget()
			* 返回目标
		* getSignature()
			* 返回正在被通知的方法相关信息
		* toString()
			* 打印出正在被通知的方法的有用信息
		
**SpringAOP设计原理及源码分析**
* Spring生成代理对象
	* JDkProxy(如果是接口)
	* Cglib(不是接口)
具体使用哪种方式由AopProxyFactory根据AdvisedSupport对象的配置来决定。默认的策略是如果目标类是接口，则使用JDkProxy，否则使用Cglib。

* JdkDynamicAopProxy


### 七、Spring JDBC设计原理及二次开发

































