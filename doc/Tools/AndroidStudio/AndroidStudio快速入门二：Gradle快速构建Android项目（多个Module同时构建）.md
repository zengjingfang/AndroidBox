【开篇语】小编菜鸟一只，初入AndroidStudio,只是想搞清楚项目从Eclipse导入到AndroidStudio的过程中是怎么回事。所以简单的看了下，gradle用法还有很多，其中原理还待深入了解。

一、Gradle准备
----------

 1. 安装好AS后，有默认的Gradle,但是建议使用自己的Gradle,本人下载的 gradle-2.5-all版本；
 2. 配置环境变量：
         GRADLE_HOME ==>>D:\Program Files\Android\Android Studio\gradle\gradle-2.5-all\gradle-2.5\bin;
        PATH ==>> %GRADLE_HOME%;
 3. 检验 ：
  运行 gradle -v    如图  既是配置成功
    ![gradle配置成功](http://img.blog.csdn.net/20151104213925614)

二、演示项目的项目结构
-----------
```
|—— demo：
   |—— libproject:
   |       |- project1
   |       |- project2
   |       |- project3
   |       |- project4
   |       |- project5
   |       |- project6
   |—— project:
           |- project0
```

>      project0 为主工程，project0 依赖项目 project1,project2,project3,project4
>      project4  依赖 project5，project6

三、settings.gradle配置
-----------------

> 一个多项目构建必须在根项目的根目录下包含settings.gradle文件，因为它指明了那些包含在多项目构建中的项目。

项目文件根目录 demo下

```
|—— demo：
   |—— settings.gradle
```

```
include ':project:project0'
include ':project:project1'
include ':project:project2'
include ':project:project3'
include ':project:project4'
include ':project:project5'
include ':project:project5'
include ':project:project6
```

> 此处的配置先后顺序是否有影响还待进一步验证（尝试更换了顺序，均可以构建成功，似乎会根据依赖关系自动分配顺序），欢迎朋友们指导。
> 记住主工程的项目project0以及被依赖的项目一定要在此配置，否则会出现错误，错误详情请
> [点击：Eclipse转AndroidStuido天坑](http://blog.csdn.net/jf_1994/article/details/49645175)


四、build.gradle配置
----------------

     

 - 项目文件根目录 demo下配置一个全局的build.gradle

>  如果需要在多项目构建的所有项目中加入公用的配置或行为，我们可以将这项配置加入到根项目的build.gradle文件中(使用allprojects)

```
|—— demo：
   |—— build.gradle
```

 

```
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {//仓库
        jcenter() 
    }
    dependencies {//依赖
        classpath 'com.android.tools.build:gradle:1.3.0' //！！！注意：2.5版本的gradle为1.3.0
    }
}
subprojects{
    //add common configuration for sub-projects here 
}
allprojects {
    //Add configuration here
}

```
   - 每个module根目录下配置一个build.gradle

```
|—— demo：
   |—— libproject:
           |- project4
                 |- build.gradle
  
```

```
apply plugin: 'com.android.library' //需要的插件

dependencies {
    compile fileTree(dir: 'libs', include: '*.jar') //依赖libs下的所有jar包
    compile project(':libproject:project5')  // project4 依赖 project5
    compile project(':libproject:project6')  // project4 依赖 project6
}

android {
    compileSdkVersion 17                    //SDK版本 注意：被依赖的project5|6 不能高于project4
    buildToolsVersion "23.0.1"
    
    lintOptions {
        abortOnError false
    }

    sourceSets {                           //资源设置
        main {
            manifest.srcFile 'AndroidManifest.xml'
            java.srcDirs = ['src']
            resources.srcDirs = ['src']
            aidl.srcDirs = ['src']
            renderscript.srcDirs = ['src']
            res.srcDirs = ['res']
            assets.srcDirs = ['assets']
            jniLibs.srcDirs = ['libs']           //注意：引用libs文件夹中的so，需要该属性
        }

        // Move the tests to tests/java, tests/res, etc...
        instrumentTest.setRoot('tests')

        // Move the build types to build-types/<type>
        // For instance, build-types/debug/java, build-types/debug/AndroidManifest.xml, ...
        // This moves them out of them default location under src/<type>/... which would
        // conflict with src/ being used by the main source set.
        // Adding new build types or product flavors should be accompanied
        // by a similar customization.
        debug.setRoot('build-types/debug')
        release.setRoot('build-types/release')
    }
}

```

五、构建项目、执行gradle命令
-----------------

 - 构建项目一: 在项目根目录demo下执行如下doc命令

```
gradle projects 
```
![构建中](http://img.blog.csdn.net/20151110203839976)
```
> gradle projects
:projects
 
 
------------------------------------------------------------
Root project
------------------------------------------------------------
 
Root project 'demo'
+--- Project ':libproject'
      --- Project ':project1'
      --- Project ':project2'
      --- Project ':project3'
      --- Project ':project4'
	  --- Project ':project5'
      --- Project ':project6'
\--- Project ':project'
     \--- Project ':project0'
      
 
To see a list of the tasks of a project, run gradle <project-path>:tasks
For example, try running gradle :app:tasks
 
BUILD SUCCESSFUL
```
 - 构建项目二: 在项目根目录demo下执行如下doc命令

```
gradle build
```
此刻有点像Ant编译一样，耐心等待
![这里写图片描述](http://img.blog.csdn.net/20151110205057447)

now, build succesful, 最喜欢看到这个句啦！
此时查看项目目录，发生了变化

```
|—— demo：
   |——.gradle
   |—— libproject:
   |       |- project1
   |       |     |-build.gradle
   |       |
   |       |- project2
   |       |- project3
   |       |- project4    //文件太多 不一一写出
   |       |- project5
   |       |- project6
   |
   |—— project:
   |       |- project0
   |			  |-build.gradle
   |			  |—build
   |				  |—outputs
   |					  |-apk
   |						  |-project0.apk
   |						  |- ...
   |			  |-.......文件太多 不一一写出啦
   |—— gradle
   |       |——wrapper
   |	        |——gradle-wrapper.jar
   |	        |——gradle-wrapper.properties
   |—— build 
   |—— build.gradle
   |—— settings.gradle
```

 - 清除构建：在项目根目录demo下执行如下doc命令

```
gradle clean
```
此操作类似ant 编译中的ant clean 命令，会清除构建生成的文件，方便下一次构建。

> 推荐参考：
> 
> [Gradle入门](http://blog.jobbole.com/71999/) 有源码实践操作，理解起来很方便。

曾尝试在在项目根目录下的build.gradle把所有的子项目的配置也写在这里，执行错误，报错：manifest.srcFile 'AndroidManifest.xml' 这里的路径有问题，还待进一步解决，期待各位朋友指导！

