一、AndroidStudio安装
-----------------

     关于安装的问题就不多说啦，下载点击安装，默认就是；
     需要注意的就是在安装后会出现：
    一直停留在fetching Android sdk compoment information界面，或者最终 fetching failed的情况；
    解决方法：关闭AS，找到安装目录下的 ***Bin\idea.properties 文件，在最后一行添加以下代码
     　 disable.android.first.run=true

二、Gradle准备
----------

 1. 安装好AS后，有默认的Gradle,但是建议使用自己的Gradle,本人下载的 gradle-2.5-all版本；
 2. 配置环境变量：
         GRADLE_HOME ==>>D:\Program Files\Android\Android Studio\gradle\gradle-2.5-all\gradle-2.5\bin;
        PATH ==>> %GRADLE_HOME%;
 3. 检验 ：
  运行 gradle -v    如图  既是配置成功
    ![gradle配置成功](http://img.blog.csdn.net/20151104213925614)

三、Eclipse导出项目
-------------

   

 - 1、 保证项目编译运行正常，选中主工程，点击File/Export  选择Android/Generare Gradle build
   files.如图

     ![这里写图片描述](http://img.blog.csdn.net/20151104215118953)
     选择所有的项目 Next Next Finish 
     ![这里写图片描述](http://img.blog.csdn.net/20151104215251033)
	 **这个地方注意 如果已经导出过一次 需要勾选**
	 ![这里写图片描述](http://img.blog.csdn.net/20151104220318801)
	 如此项目便导出完成
	 【注意】仔细查看每个工程是否成功导出，如果没有，请再次导出。判断依据是该工程下是否有生成build.gradle文件等。
	 需要修改两个地方

 - 2、项目根目录下的（与.gradle平级）的build.gradle文件；

	 
    因为gradle2.5版本对应的是 1.3.0 暂且这么理解吧
```
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:**gradle:1.3.0'**
    }
}
```

 - 3、项目根目录\gradle\wrapper 下的gradle-wrapper.properties文件

修改为安装版本对应的distributionUrl=https\://services.gradle.org/distributions/gradle-2.5-all.zip
```
#Tue Nov 03 09:29:30 CST 2015
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-2.5-all.zip
```

 - 4、查看 项目根目录下的 settings.gradle文件

该项目的工程目录
lib  lib1
libproject project1
                 project2
                 project3
                 project4
                 project5
                 project6
project     mainproject
【天坑】小编在这里遇到一个天坑，由于我的Eclipse有问题，并不能一次性把项目导出完；
【查看】settings.gradle 会发现有部分的成功导出的项目。

```
include ':lib:lib1'

include ':libproject:project2'
include ':libproject:project3'
include ':libproject:project4'
```
【添加】手动添加其他没能一次导出成功的工程，如project5，project6.

 - 5、导入到AS  打开首页，选择Import project (Eclipse ----)

![这里写图片描述](http://img.blog.csdn.net/20151104222608003)
输入项目根目录、选中主工程下的build.gradle文件
![这里写图片描述](http://img.blog.csdn.net/20151105221751169)
经过漫长的等待，如图
![这里写图片描述](http://img.blog.csdn.net/20151105221836068)
终于导入啦

 - 6、【回到神坑】出现了 Gradle Sync Failed

error:project with path':libproject:project1' could not be found in project ':project 2'.
其中：project2是依赖project1的
![这里写图片描述](http://img.blog.csdn.net/20151105222004132)
按照提示 点击了open file
![这里写图片描述](http://img.blog.csdn.net/20151105222050391)
作为一只菜鸟，还以为是project1的问题，其实不然
在【4】中已经有分析其原因了，修改如下，将 project1 添加到 settings.gradle 中

```
include ':libproject:project1'
```
感谢大神的搭救，回过头来才发现自己的错误。
于是，Gradle Sync Successful

 - 7、选择主工程 build -》clean project 进行编译

由于android studio 对代码比较严格，安装提示一步步进行修改就可以啦。
如：
删除依赖工程的AndroidManifest.xml中 Application的配置
删除重复的string.xml的内容
。。。。
于是乎，you are successful！
更多问题请留言。


> 方便真正理解AndroidStudio的构建过程，多个项目间的依赖关系如何构建，请阅读[AndroidStudio快速入门二：Gradle快速构建Android项目（多个Module同时构建）](http://blog.csdn.net/jf_1994/article/details/49764123)，一定会让你豁然开朗。



重点推荐：AndroidStudio 与Gradle：http://blog.csdn.net/jf_1994/article/details/49764123