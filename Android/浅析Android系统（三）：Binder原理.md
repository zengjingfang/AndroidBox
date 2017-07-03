# 前言

Binder部分参见了《Android开发艺术探索》以及GitYuan的博客[Binder系列](http://gityuan.com/2015/10/31/binder-prepare/)

# Binder前置知识
### Binder类比
+ Binder驱动 --> 路由器
+ ServicManager ---> DNS
+ Binder Client --->客户端
+ Binder Server --->服务器

其中，SM在Binder通信的过程中的唯一标志永远为0。

# 智能指针

+ 引用计数  对象都有属于自己的“计数”，每增加一个引用，计数就会曾加。
+ 强指针sp 增加强引用，也会增加弱引用。减少同。
+ 弱指针wp 增加弱引用，不会增加强引用
+ 对象释放  减少弱强引用的时候会进行判断，如果减少后强引用为0，则删除对象，既释放。

# 数据传输载体 Parcel

+ 问题：int型的数值可以通过不断复制到目标进程，但是如果是一个对象的话就不行，我们拿到的是对象的引用，但是不同进程间的地址分配是不一样的，如果我们这个时候传内存地址就不起效果了。
+ Parcel,是一种数据载体，用于承载希望通过IBinder发送的的相关信息（包括数据和对象引用）。
+ 可以将Parcel理解为集装箱，同时不仅只是一个集装箱，还负责打包和组装的任务。既，写入和写出。同时需要注意的是，写入方和写出方所使用的协议必须是完全一致的。
+ Parcel在最前面的参数表明了数据的大小，写入时能够动态的申请所需要的内存空间。
+ WriteInplace用于确认即将写入的数据的起始和结束位置，并做好padding工作。
+ Serializable是Java的序列化接口，使用简单，但是I/O操作多开销大；
+ Parcelable是Android的序列化接口，使用麻烦，但是效率高。

# Binder原理分析

+ Binder存在的意义

由于进程间的内存不是共享的，当我们在进行跨进程通信的时候，不能直接传递内存地址进行通信，前面也提到了，不同的进程的地址分配是不一样的，这是单个进程内部的事情。但是Binder操作的是内核空间的内存，这块内存对于上层用户空间而言就是共享内存了。通过共享内存这第三方，我们对其进行文件的读写操作，就完成了进程间的通信了。参照图（引自gityuan.com）如下：
![](http://gityuan.com/images/binder/prepare/binder_interprocess_communication.png)

+ Binder原理

挑选了这张图来对Binder原理进行浅层次的分析，图片引自（gityuan.com)。

![](http://gityuan.com/images/binder/java_binder/java_binder.jpg)

+ Binder驱动与协议

	+ binder_ open :为用户创建一个自己的binder_proc实体，之后用户对binder设备的操作都用这个对象为基础；
	+ binder_ mmap :内存映射，将进程A的内存空间某内存映射到物理内存某个位置，进程B需要A的数据时，只需要copy一次既让B也映射到同样的物理内存地址就可以了。
	+ binder_ioctl :Binder驱动里真正干活的人，执行读写（BINDER_ WRITE _READ）等命令，实现应用进程和Binder驱动之间的交互。

+ SM

ServiceManger是binder服务的大管家，他管理了所有的xxxServiceManager,比如ActivityManager,我们可以通过addService来注册服务，通过getService来获取服务。由图可知，这些操作都要通过底层的Binder驱动完成，才能做到跨进程。我们可以理解ServiceManager运行在系统进程中。SM的设计遵循了C/S框架，而SM则是"IP"为0的一个站点，我们将ServiceManager所在的进程称为服务端进程，其他进程称为client进程。
	
+ 
