# IO流
程序操作有两种基本类型，CPU的操作和IO的操作，其中大部分时间都花在了IO上面，不管事本地磁盘，还是网络连接。流，简单的说是一个连续的字节序列。输入流，是读取这个字节序列。而输出流，则是构建这个序列。
![](http://upload-images.jianshu.io/upload_images/1752522-adc85fb95f884363.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
### 一、字节流和字符流
+ 字节流
![](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/image002.png)
![](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/image004.png)
+ 字符流
![](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/image006.png)
![](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/image008.png)

### 二、NIO（jdk1.4之后）

+ 目标：快速移动大量数据
#### 方式一：通道和缓冲器

+ 通道要么从缓冲器获取数据，要么向缓冲器发送数据
+ 唯一直接和通道交互的缓冲器是ByteBuffer

+ ByteBuffer
	+ 转换数据：ByteBuffer只容纳普通的字节，如果需要转为字符。要么在输入时进行编码，要么输出时进行编码。
	+ 获取基本类型：ByteBuffer虽然存的是字节，但是还是可以产生基本类型数据的。ByteBuffer.asShortBuffer()

+　视图缓冲器

#### 方式二：内存映射文件

+ 内存映射文件允许我们创建或者修改那些因为太大而不能放入内存的文件
+ 只有一部分存入内存，文件的其他部分被交换出去

### 三、文件加锁（jdk1.4之后）

+ tryLock(): 非阻塞，当拿不到锁（当其他进程已持锁，且不共享）则直接返回
+ lock():阻塞，对文件有独占权。拿不到则等待，除非调用线程中断或者调用通道关闭
+ release():释放锁，Java虚拟机会自动释放锁

### 四、序列化

+ 对象网：不仅保存了对象的“全景图”，还能追踪到对象内包含的所有引用，并保存那些对象。
+ Externalizable:
	+ 继承了Serializable,同时增添了writeExternal和readExternal。
	+ 默认构造器都会被调用（与Serializable不同，其以二进制位来构造），然后调用readExtenal()等，所以注意要声明为public的。
+ transient：变量加上这个关键字，则表示这个类虽然实现了Serializable,但是不使用默认的序列化，自己来处理。故也不会保存到磁盘。
+ 安全问题：
	+ 每次运行后，对象将会处于不同的内存地址
	+ 如果想保存系统状态，最安全的做法就是将其作为“原子”操作进行序列化
### 五、性能调优

+ 尽可能减少IO访问次数
+ 尽可能减少IO传输的大小
+ 尽可能使用缓存，而不是磁盘
+ 网络传输尽可能在传输前转为字节码，因为字符到字节码的编码很耗时
+ 
### 五、参考资料
+ [Java深度历险（八）——Java I/O](http://www.infoq.com/cn/articles/cf-java-i-o/)
+ [深入分析 Java I/O 的工作机制](https://www.ibm.com/developerworks/cn/java/j-lo-javaio/index.html)















	
	