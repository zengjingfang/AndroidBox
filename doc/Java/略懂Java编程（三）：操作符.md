### 1、while do & do while

这两者的区别在于，执行的先后顺序 ，比如我们的条件是i<5,如果当前i=5，while do 中，先while后do，显然不成立，所以就不能do啦，但是先do后while的话，前一次的判断while条件成立，下一次循环就可以do,而不管do之后得到的i。

### 2、for

#### for ++
+ 构成：初始化initialization，布尔表达式Boolean-expression,步进表达式step，循环体；
+ 执行顺序：
	+ 首次会初始化
	+ 接下来 Boolean-expression ，
	+ 执行循环体，一次循环结束，执行步进step，
	+ 接着Boolean运算，
		+ 为true则执行下一次循环，
		+ 为false则循环结束。
+ 注意：初始化initialization，布尔表达式Boolean-expression,步进表达式step都可以为空，但是“；”不可以少。
+ 执行顺序示意图
![](http://ww1.sinaimg.cn/large/aea705afgy1fp3e8n2s7bj20cj06fdfz.jpg)

#### for++ 和 for each 的区别

+ for++ 每次进行布尔判断时，要获取一次list的count边界值，相对消耗性能
+ for each 实际使用到了List的迭代器，每次迭代通过hasNext做判断是否有下一个，所以性能优
+ for each 在遍历过程中，不能remove子项，因为迭代器不允许有remove操作，否则抛出ConcurrentModificationException

### 3、 break & continue
+ 一般的break跳出当前的循环；
+ 带标签的break跳出标签所指的循环；
+ 一般的continue会退回最内层循环的开头处，并继续执行；
+ 带标签的continue会退回到标签所指的循环的开头处，并继续执行，紧接着执行标签后边的循环。


### 4、this


### 5、