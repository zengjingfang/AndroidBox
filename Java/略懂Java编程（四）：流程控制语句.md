第四章  流程控制语句

##1、while do & do while

这两者的区别在于，执行的先后顺序 ，比如我们的条件是i<5,如果当前i=5，while do 中，先while后do，显然不成立，所以就不能do啦，但是先do后while的话，前一次的判断while条件成立，下一次循环就可以do,而不管do之后得到的i。

##2、for

+ 构成：初始化initialization，布尔表达式Boolean-expression,步进表达式step，循环体；
+ 执行顺序：首次会初始化，接下来 Boolean-expression ，执行循环体，一次循环结束，执行步进step，接着Boolean运算，为true则执行下一次循环，为false则循环结束。
+ 注意：初始化initialization，布尔表达式Boolean-expression,步进表达式step都可以为空，但是“；”不可以少。

##3、 break & continue
+ 一般的break跳出当前的循环；
+ 带标签的break跳出标签所指的循环；
+ 一般的continue会退回最内层循环的开头处，并继续执行；
+ 带标签的continue会退回到标签所指的循环的开头处，并继续执行，紧接着执行标签后边的循环。

