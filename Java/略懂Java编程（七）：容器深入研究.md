# 一、数据结构

### 1、数组（Array)

+ 相同元素所组成的集合
+ 分配一块连续的内存存储，利用元素**索引**找到内存地址
+ 一维、二维、多维

### 2、堆栈(Stak)
+ 串列形式，只允许在一端进行数据的输入和输出
+ 操作:
	+ 推入（push):将元素堆叠到顶端，总数加一
	+ 弹出（pop)：输出顶端元素，总数减一
+ 特点：**后入先出**
### 3、队列（Queue)
+ 线性表，通常用链表或者数组来实现
+ 操作：
	+ 添加：只允许从后端进行插入操作
	+ 删除：只允许从前端进行删除操作
+ 特点：先入先出
+ 分类：
	+ 单链队列
	+ 循环队列
	+ 阵列队列
### 4、链表（Linked List)
+ 线性表，不按照线性顺序存储，而是每个节点存储了（上）下一个节点的指针
+ 分类：
	+ 单向链表
	+ 双向链表
	+ 循环链表
	+ 块状链表
+ 应用：实现队列
### 5、树（Tree)
+ 特点：
	+ 每个节点有零个或者多个子节点
	+ 没有父节点的节点叫做根节点
	+ 每一个非根节点有且只有一个父节点
	+ 除了跟节点外，每个子节点可以分为不交叉的子树
+ 二叉树： 每个节点最多含有两个子树的树称为二叉树
+ 红黑树：
	+ 每个节点是红的或者是黑的
	+ 根节点是黑的
	+ 每个叶节点（NIL）是黑的
	+ 如果一个节点是红的，则它的两个儿子都是黑的
	+ 对于每一个节点，从这个节点出发到它的子孙节点，每条路径包含了相同数目的黑节点

### 6、堆（Heap)
+ 一个树的数组对象，解决在队列中某些较短的任务需要等待很长时间才得以结束，或者不短小，但是具有重要性作业，应该具备有一定优先权。堆就是为了解决这类问题而设计的数据结构。

### 7、散列表（Hash)
+ 也叫哈希表，通过键（Key)直接访问内存存储位置的数据结构。查找速度快。

# 二、Java常用集合

![](https://docs.google.com/drawings/d/e/2PACX-1vSavWCQ31k5Gfhxsiy5Hj7D9edZXGZkmhLt3K1Zk2Z08uR3qIwrEOAtv964hxsfb-g1lOTdFAi_yY1M/pub?w=419&h=253)
### 1、List

+ subList():截取部分元素
+ retainAll()：取两个List的交集部分
+ removeAll()：移除所有元素
+ set()：设置某一项元素
+ toArray()：转换成数组
+ asList()：内部变成一个不可变长度的数组
+ **ArrayList,适用于大量随机访问**;
+ **LinkedList,适用于中间插入或者删除元素**；
### 2、Iterator

> **遍历一个序列对象，不需要关注序列的类型结构。**

+ 通过iterator()返回一个Iterator对象，并准备好返回第一个元素；
+ next()获得序列中下一个元素；
+ hasNext()判断是否还存在元素；
+ remove（）将迭代器返回的元素清除；

> ListIterator是Iterator的子类，仅适用于List

+ listIterator(n) 可以直接到索引为n的位置

### 3、Stack

+ 后入先出
+ peek()：返回栈顶元素但不删除
+ pop():返回栈顶元素的同时移除这个元素
+ LinkedList具有直接实现栈的方法

### 4、Set

+ Set不保存重复的元素；
+ Set具有和Collection完全一样的功能，无自己其他特性；
+ Set是基于对象的值来确定归属性的；
+ HashSet乱序**散列**的，TreeSet存储到**红-黑**数据结构中，LinkedHashSet使用了**链表**结构；

### 5、Map

+ 对象映射到其他对象
+ Map与数组和其他的Collection一样，可以很容易地扩展到多维
+ Map可以返回他的键Set(通过KeySet（）方法)，他的值Collection,或者它的键值对Set

#### 6、Queue

+ 典型的**先进先出（FIFO）**的容器，LinkedList提供支持；
+ offer():将一个元素插入到队尾，或者返回false;
+ peek()和element（）不移除，返回队头；
+ poll()移除并返回队头，若队列为null，则返回Null；
+ remove（）移除并返回队头.若队列为null，则抛出NoSuchElementExcption()异常；

#### 7、PriorityQueue

+ 优先级队列声明下一个弹出的元素是最需要的元素
+ offer(),插入一个对象时，被排序（默认自然顺序，或者通过Comparator来修改排序）
+ peek()、poll()、remove(),获取到的是队列中优先级最高的元素

### 8、Collection和Iterator

+ 方法可以接受一个Collection参数，这样可以应用于任何实现了Collection接口的类
+ Collection继承了Iterator，所以实现了Collection也就必然提供了Iterator（）方法
+ 生成Iterator是将队列与消费的方法连接在一起耦合度最少的方式

### 9、Foreach与迭代器

+ foreach语句可以用于任何数组或者其他任何Iterable，但是并不说明数组肯定也是一个Iterable，任何自动包装也不会自动发生；
+ 适配器方法惯用法：当你有一个接口需要另一个接口时，编写一个适配器就可以解决问题，如：用适配器构建返回自定义的Iterator对象；
+ Arrays,asList(某个数组等)，没有新建一个应用，修改的话会修改掉原始数组的引用；

### 小结：
+ HashXXX:散列，查找快，大量随机访问
+ TreeXXX:红-黑， 固定顺序
+ LinkedXXX:链表，插入快，从中间插入元素频繁


# 三、使用对比

### Vector和ArrayList

+ Vector同步，线程安全的，效率相对低
+ ArrayList不同步，非线程安全的，效率相对高
+ 内部都是通过数组（Array)来实现，内部数组长度不够时，Vector扩展增长原来的1倍，ArrayList扩展增长原来的50%，资源占用超出需要，集合中保存大量的数据时，Vector更优（设置集合初始化大小）。

### ArrayList 和 LinkedList

+ ArrayList基于动态数组（Array)实现的，LinkedList基于**链表**实现的；
+ 随机访问查下操作，LinkedList优于ArrayList;
+ 新增删除操作，ArrayList优于LinkedList(大批量随机插入删除数据时Linked速度明显快,ArrayList需要移动插入点之后的数据）；

### HashMap 和 TreeMap

+ HashMap 通过hashcode对内部进行快速访问；
+ TreeMap 中所有的元素都保持一个固定的顺序，需要得到有序的结果则使用TreeMap;

### HashTable 和 HashMap

+ HashTable 线程安全的，HashMap 线程不安全的；
+ HashMap 的key\Value 可以为NULL;

### HashMap 、SparseArray 和 ArrayMap
+ HashMap： 数组+链表存储方式，当空间不够时，进行扩容，新的空间会是原来的两倍，如果存储大批量的数据，非常消耗资源；
+ SparseArray： key 和 value 分别是一个数组存储，key存储int类型。对key进行二分法排序，并通过二分法查询，查询时速度比HashMap快，数据量大的收速度也不明显了。
	+ SparseArray代替HashMap的条件
	+ 数据量不大，千级以内；
	+ Key必须为int类型
+ ArrayMap：内部是使用两个数组进行数据存储，一个数组记录key的hash值，另外一个数组记录Value值，其他和SparseArray类似，只是Key可以不为int。应用场景和SparseArray的一样，如果在数据量比较大的情况下，那么它的性能将退化至少50%。


