前言：一般大一点的项目都会依赖多个项目或者第三方库，在使用Eclispe开发时，我们经常使用到 jar 包，但是接触到AndroidStudio后我们有了更好的打包方式，即aar。

>阅读之前，如果你对Gradle构建没有了解，请先阅读[Gradle构建基础](http://blog.csdn.net/jf_1994/article/details/49764123)，方便本篇内容的理解。

###一、aar和jar的生成###
- 当改项目的build.gradle的配置中，插件为 com.android.library时：

    apply plugin: 'com.android.library'

- 可以认为该Moulde为一个依赖工程，编译该Moudle会自动生成 aar 和 jar.

- 生成的目录位置：

    **jar: library/build/intermediates/bundles/debug(release)/classes.jar**

    **aar: library/build/outputs/aar/demo.aar**


###二、arr和jar的区别###

- jar:仅打包了class文件和配置清单文件，其res等资源文件并没有进来  
- arr:打包了所有的class、res等资源文件

###三、arr包在AndroidStudio中使用###

- 复制demo.aar到libs目录下
- 更改build.gradle配置



    repositories {
    flatDir {
    dirs 'libs'
    }
    }
    dependencies {
       	 compile(name:'demo', ext:'aar')
    }
 

   

- 重新编译一次，查看项目地址 ”\build\intermediates\exploded-aar\“ 你会发现下面多了一个文件夹 librarydemo 打开后能看见里边包含了一个 ”classes.jar“ 文件与一些 资源文件和”R.txt“文件 。


![](http://7xoq4d.com1.z0.glb.clouddn.com/asaar.png?attname=&e=1448781124&token=dDQYTry5zke_2YwAfs6GWn4lbjAOS7r4b3iCX-Kc:6kIa5jhw47KoG_VgnJYXAD_QVtI)