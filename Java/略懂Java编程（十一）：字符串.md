
# 一、不可变性

### 1、String作为方法参数

每当我们把String类型的字符串对象作为参数的方法时，实际是重新copy了一份引用，而这份引用指向的对象其实一直待在单一的物理位置上，我们可以理解为是一个全新的对象。

### 2、String的“+”和 StringBuilder

 + String的"+"和“+=”是Java仅有的两个重载操作符；
 + 在执行String的“+”操作时，内部会自动构建一个StringBuilder，并进行append操作；
 + 如果在一个循环中进行“+”操作，每次循环都会new一个StringBuilder，不免有些浪费资源。如果在循环体外部自己构建一个StringBuilder，有利于资源使用；

### 3、String操作

当String操作需要改变其内容时，String方法会返回一个新的String对象。如果没有改变，返回指向的还是原来的对象，以节约存储开销。

### 4、常量池优化
当String对象的值时相同的时候，在常量池中实际用的是一个，对外可能是各自不同的引用。这样做的目的是节约内存空间。所以，通过通过上述intern（）方法就可以返回对象在常量池中的引用。

![](https://docs.google.com/drawings/d/e/2PACX-1vT1PkapazDAJ_66J-LoyRRubf4vpy7ffvUevKfrUDKDwFBaGRleV0OVttVOC-fYUYqoSU1EBqm3SoLC/pub?w=655&h=316)

### 5、 String不可变的原因
+ 安全性：如我们经常使用的url、文件名、文件地址等等都是一个字符串，如果可变就会变的很危险；
+ 线程共享：多线程环境下，共享变得方便些；
+ HashMap等的key一般是String字符串，存入key的时候hashcode计算一次就好，因为String不可变，这样有利于提供访问速度；

# 二、正则表达式

### 1、符号
+ -？ >>> 含有“-”；
+ -？\\d+ >>> 可能有一个负号，后面跟着一位或多位数字；

### 2、Java接口
+ String.split()：将字符串从正则表达式匹配的地方切开；
+ replace()
+ intern（）方法：为每一个字符串序列生成有且仅有一个String引用。



### 3、用途
+ 查询
+ 替换
+ 扫描

# 三、String、StringBuilder、StringBuffer对比


+ String：字符串常量，线程不安全，执行效率低，适用于操作少、数据少的场景；
+ StringBuilder:字符串变量，线程不安全，执行效率高，适用于单线程操作多、数据多的场景；
+ StringBuffer:字符串变量，线程安全，执行效率一般，适用于多线程操作多、数据多的场景；


