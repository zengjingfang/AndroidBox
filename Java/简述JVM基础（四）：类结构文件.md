# 一、程序存储格式

+ 统一的程序存储格式：不同平台的虚拟机于所有平台都**统一使用程序存储格式——字节码（ByteCode)**;
+ Java虚拟机不关心Class文件的来源，而只和“Class文件"这种二进制文件格式关联，也就是说Java虚拟机只认识“Class"文件；
+ Java编译器可以把Java程序代码编译成虚拟机所需要的Class文件；

# 二、Class文件结构
 
Class文件是以8个字节为单位的二进制流，紧凑排列，中间没有空隙；如果想查看一个Class文件除了通过winHex编译器看到字节码，也可以通过javap -verbose xxx.Class 输出字节码内容,这样看起来比较直观。

### 1、基本类型

+ 无符号数：
	+ 定义：基本的数据类型，u1、u8表示1个字节，8个字节。
	+ 使用：可以用来描述数字、索引、引用，utf-8格式的字符串；

+ 表：
	+ 定义：多个无符号数和其他表组成的复合数据类型；通常以“_info” 结尾。
	+ 使用：描述有层次关系的复合结构数据；


### 2、魔数与版本

+ 魔数：每个Class文件的头4个字节，唯一作用就是确定这个文件是否能被一个虚拟机接受的Class文件；
+ 次版本号：紧接着魔数后面的第5和第6个字节；
+ 主版本号：第7和第8个字节代表主版本号，比如说50对应的就是JDK1.6.
+ 可以使用十六进制编译器WinHex打开一个Class文件瞅瞅；

### 3、常量池

版本号之后紧跟的就是常量池入口，可以理解为Class文件之中的资源仓库；

+ 常量池容量计数器：u2类型，代表本Class文件有N-1个常量（因为是从1开始技术的）；
	+ 0项常量：不引用任何一个常量池项目
+ 常量池放置的内容每一项都是一个表，主要分两类
	+ 字面量：文本字符串、final常量值等；
	+ 符号引用
		+ 类和接口的全限定名
		+ 字段的名称和描述符
		+ 方法的名称和描述符

+ length：
	+ 定义：UTF-8编码的字符串长度是多少个字节；
	+ 65535限制：Class文件中方法、字段等都需要引用CONSTANT_ Utf8_ info型常量的length为u2类型，最大为65535.如果某个变量或者方法名超过了64K，那么这个length容不下了，当然也就无法编译了。


### 4、访问标志(access_flags)

常量池之后紧跟的就是访问标志。主要包括了这个Class是类or接口，是不是public,是不是abstract类型，是不是final类型。

### 5、类索引、父类索引于接口类集合

+ java.lang.Object类索引为0；
+ 类的索引其实就是描述了这个Class的extends和implements的关系；

### 6、字段表集合(field_info)

+ 用于描述定义的变量，依次包括了访问标志（access_ flags)、名称索引（name_ index)、描述索引（descriptor_ index)、属性表集合（attributes)。
+ 描述的信息如下：
	+ 作用域：public、private、protect
	+ 实例or类变量：static
	+ 可变性：final
	+ 并发可见性：volatile
	+ 是否可序列化：transient
	+ 字段数据类型：基本类型、对象、数组等
	+ 字段名称；

+ 字段表集合原则
	+ 1、不会列出超类or父类或者父接口继承而来的字段；
	+ 2、有可能列出原本Java代码中不存在的字段（内部类会自动添加指向外部类实例的字段，才能引用到外部类）；
	+ 3、Java语言中字段是无法重载的；

### 7、方法表集合

和字段表集合差不多，方法表集合用来描述Class文件中的方法，但是访问标志和属性表集合和字段表集合有所区别；

+ 访问标志：
	+ volatile、transient关键字不可以修饰方法，方法表中少了这两种标志；
	+ synchronized、native、strictfp和abstract可以修复方法，故方法表增加了这些对应的标志；
+ Code属性：
	+ 方法体中的代码放在了“Code”属性里面了；

+ 方法表集合原则
	+ 方法没有重写（Override)，父类的信息不会写到子类的方法表中；
	+ 编译器有可能自动添加一些方法，典型的如类构造器的“< clinit >”、方法&实例构造器的“< init >“方法；
	+ 重载（Overload)一个方法，需要添加一个特征签名，特征签名就是一个方法中各个参数在常量池中的字段符号引用的集合；

### 8、属性表集合（attribute_info)

上述那些表需要携带自己的某些属性，来描述自己的特殊环境信息，比如InnderClasses、LineNumberTable、Code之类的；


+ Code （用语描述代码）
	+ max_stack:操作栈深度最大值，JVM运行时根据这个值来分配栈帧（Stack Frame)中的操作栈深度；
	+ max_locals:代表了局部变量表所需要的存储空间。
		+ **Slot:虚拟机为局部变量分配内存的最小单位**
			+ byte、char、float、int、short、boolean、returnAddress 长度少于32位，占1个slot
			+ double、long 64位，占2个slot
		+ 当代码超出一个局部变量的作用域时，这个局部变量所占用的slot可以被其他的局部变量所使用

	+ code_length:字节码长度
	+ code:存储字节码指令
	+ 65535限制：虚拟机规定了一个方法不允许超过65535条字节码，否则编译不通过；
	+ 执行：执行过程中的数据交换、方法调用等操作都是基于栈（操作栈）的；
	+ this关键字：在实例方法中通常可以有个this关键字来引用当前对象的变量，这是因为Java编译时在局部变量表中自动增加了这个（this)局部变量。

+ LineNumberTable：描述Java的源码行号和字节码行号；
+ LocalVariableTable:描述局部变量表中的变量与Java源码中定义的变量之间的关系；

# 三、字节码指令

### 1、字节码组成
+ 操作码（Opcode）:i（助记符）代表int类型数据操作....等等；
+ 操作数 (Operands)：永远都是一个数组类型的对象；
> Java虚拟机采用面向操作数栈而不是寄存器的架构，字节码指令集是一种指令集架构。放弃了操作数对齐，省略了填充的符号和间隔。

### 2、加载和存储指令

将数据在帧栈中将局部变量表和操作数栈之间来回传输。

+ 将一个局部变量加载到操作栈；
+ 将一个数值从操作数栈存储到局部变量表；
+ 将一个常量加载到操作数栈；
+ 扩充局部变量表的访问索引的指令；


### 3、运算指令

+ 将两个操作数栈上的值进行某种特定运算，并把结果重新存入到操作栈顶；
+ Java没有直接支持byte、short、char、boolean类型，都转为int类型进行运算，使用int的指令代替；


### 4、类型转换指令

+ 宽化转换
	+ int到long、float、double
	+ long到float、double
	+ float到double
+ 窄化转换
	+ 必须显示的声明转换
	+ 有溢出或者丢精的情况，但不会抛出异常

### 5、同步指令

+ Java虚拟机支持方法级同步和方法内部一段指令序列同步，这两种同步都是通过“管程”来支持；
+ 执行线程就要求先成功持有“管程”，然后才能执行方法，最后方法执行完成后，才释放“管程”。
+ Java虚拟机通过monitorenter和monitorexit两个指令配对使用，另外编译器会自动增加一个异常处理器。当出现异常时，这个异常处理器能够捕获到所有的异常，并且释放“管程”，monitorexit指令响应。这样的话，保证了monitorenter和monitorexit总是成对出现的。


# 四、代码举例

###1、Java文件：

	package com.xxx.ccc;

	public final class InitConfig {
    	public static final InitConfig BFCACCOUNT = new InitConfig(0, "aaa", "AAA");
    	private int mIndex;
    	private String mData;
    	private String mDescribe;

    	private InitConfig(int indexFlag, String data, String describe) {
        	this.mIndex = indexFlag;
        	this.mData = data;
        	this.mDescribe = describe;
    	}

   		public String getmData() {
        	return this.mData;
    	}
}


### 2、Class 文件：

 	Last modified 2017-7-4; size 1050 bytes
 	MD5 checksum 2beb0c10f91b793c3570edcf2d1eff78
	Compiled from "InitConfig.java"
	public final class com.xxx.xxx.InitConfig
 	minor version: 0  //次版本号
 	major version: 51 //主版本号
 	flags: ACC_PUBLIC, ACC_FINAL, ACC_SUPER  //访问标志
	Constant pool: //常量池
 	 #1 = Methodref          #14.#41        // java/lang/Object."<init>":()V
 	 #2 = Fieldref           #5.#42         // com/xxx/xxx/InitConfig.mIndex:I
  	 #6 = Class              #46            // com/xxx/xxx/common/constant/ConstData
  	 #7 = String             #47            // aaa
	 #23 = Utf8               <init>
 	 #24 = Utf8               (ILjava/lang/String;Ljava/lang/String;)V
     #25 = Utf8               Code
     #26 = Utf8               LineNumberTable  //Java的源码行号和字节码行号
 	 #27 = Utf8               LocalVariableTable //局部变量表中的变量与Java源码中定义的变量之间的关系
 	 #28 = Utf8               this
 	 #32 = Utf8               getmData
 	 #33 = Utf8               ()Ljava/lang/String;
 	 #37 = Utf8               <clinit>
 	 #38 = Utf8               ()V
 	 #40 = Utf8               InitConfig.java
 	 #41 = NameAndType        #23:#38        // "<init>":()V
 	 #45 = Utf8               com/xxx/xxx/InitConfig
 	 #46 = Utf8               com/xxx/xxx/common/constant/ConstData
 	 #53 = NameAndType        #17:#16        // SEAACCOUNT:Lcom/xxx/ccc/InitConfig;
 	 #54 = Utf8               java/lang/Object

	public static final com.xxx.xxx BFCACCOUNT;
   		descriptor: Lcom/xxx/xxx/InitConfig;
   		flags: ACC_PUBLIC, ACC_STATIC, ACC_FINAL 

 	public java.lang.String getmData();
   		descriptor: ()Ljava/lang/String;
   		flags: ACC_PUBLIC
  		Code:
    		 stack=1, locals=1, args_size=1
       		 0: aload_0
       		 1: getfield      #3                  // Field mData:Ljava/lang/String;
        	 4: areturn
     	     LineNumberTable: //Java的源码行号和字节码行号
       	     line 36: 0
     		 LocalVariableTable: //局部变量表中的变量与Java源码中定义的变量之间的关系
       		 Start  Length  Slot  Name   Signature
           		0       5     0   this   Lcom/xxx/xxx/InitConfig;  //方法里面默认增加了个this

# 小结

为什么说一些”非Java"语言也是可以在JVM上跑，这是因为JVM只认识Class文件，所以如果某某语言最终编译出的文件是Class文件，那么对于JVM来说没有什么区别，但是得按照Class文件的结构来，不然也无法正常执行。Class定义了许多特定的基本类型和表结构，通过魔数让JVM认识该文件，版本号保证可以在要求的JDK版本上运行，在常量池中定义好常量，访问标志位确定访问权限。索引集合方便与外界的class保持联系，字段表保存我们定义好的变量，方法表存储方法的信息，属性表存储了上述各种表的一些属性。其中记住slot为局部变量分配内存的最小单位，当程序超出作用域的时候，slot可以被其他替换使用。到这里，仅仅是代码最静态的存储的格式，程序要运行起来。还需要操作指令，也是由字节码存储，包括操作码和操作数。有加载存储、运算、类型转换、同步指令。