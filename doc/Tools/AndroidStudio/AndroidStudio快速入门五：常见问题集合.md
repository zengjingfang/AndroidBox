前言:本篇主要收集在使用as中碰见的一些问题，方便查阅，也分享给朋友们。

1、 Gradle Sync Failed
---------------------

问题: error:project with path’:libproject:project1’ could not be found in project ‘:project 2’. 

分析：这里是因为在主工程的settings.gradle 文件中没有配置 project1 
解决：增加project1的配置，[详情请点击](http://blog.csdn.net/jf_1994/article/details/49645175)

2、cant resolve symbol
---------------------

问题：import的包均有问题，clean的过程中好了，但是clean完了之后又是这样，所有的工程均是如此，不是R文件的错误，代码可以编译成功，程序可以运行，但就是“全线飘红”，如图：
![这里写图片描述](http://7xoq4d.com1.z0.glb.clouddn.com/as%7BD5BA87FE-358A-40C6-B5D0-0EF8F149C703%7D.png?attname=&e=1449665056&token=dDQYTry5zke_2YwAfs6GWn4lbjAOS7r4b3iCX-Kc:LHOqjBcSPJ33ZQIAKfBf-on1J1U)

分析：由于电脑突然断电，as的缓存文件出现错误
解决：File -->Invalidate Catches /Restart 重启

3、Error:Execution failed for task ':app:dexDebug'.
--------------------------------------------------

Error:Execution failed for task ':app:dexDebug'.
com.android.ide.common.process.ProcessException: org.gradle.process.internal.ExecException: Process 'command 'C:\Program Files\Java\jdk1.8.0\bin\java.exe'' finished with non-zero exit value 2

分析：查看其它log内容，会提示有多余的包冲突，比如support.v4包
解决：删除多余的jar包

4、Unable to load class 'org.codehaus.groovy.runtime.StringGroovyMethords'
------------------------------------------------------------------------

问题：Gradle版本不对应
解决：
\gradle\wrapper\gradle-wrapper.properties文件

```
distributionUrl=https\://services.gradle.org/distributions/gradle-2.5-all.zip
```
和build.gradle 文件 版本号对应
distributionUrl=https\://services.gradle.org/distributions/gradle-2.5-all.zip

5、Error:Execution failed for task ':project:demo:processDebugManifest'.
------------------------------------------------------------------------

 - 问题1：

>Error:Execution failed for task ':project:PrimaryListen:processDebugManifest'.
Manifest merger failed with multiple errors, see logs

分析：AS相对Eclipse更加严格，demo主工程的Manifest.xml 中的Application 配置与 依赖工程的Manifest.xml 中的Application 配置相冲突

解决：删除所有依赖工程的Manifest.xml 中的Application 配置

- 问题2：

> Error:Execution failed for task ':project:demo:processDebugManifest'.
> Manifest merger failed : uses-sdk:minSdkVersion 8 cannot be smaller
> than version 14 declared in library
> [demoRoot.libproject.demo1:unspecified] 
> E:\---demoRoot\project\demo\build\intermediates\exploded-aar\demoRoot.libproject.demo1\unspecified\AndroidManifest.xml
> Suggestion: use tools:overrideLibrary="com.demo.demojava" to force
> usage


分析：被依赖工程的版本号不能高于主工程的版本号
解决：修改版本号 minSdkVersion
<uses-sdk
android:minSdkVersion="8"
android:targetSdkVersion="16" />
<uses-sdk
android:minSdkVersion="15"
android:targetSdkVersion="16" />

6、Not recognizing known sRGB profile that has been edited
---------------------------------------------------------

问题：
AAPT err(1745145340): 
E:\******\res\drawable-mdpi\addpressed.png: libpng warning: iCCP: Not recognizing known sRGB profile that has been edited
问题解析：
图片格式的原因，有一些网友是非png格式的图片（例如jpg格式等）而错误地采用了png为后缀。
详细解析：http://my.oschina.net/1pei/blog/479162

解决方案一：
使用图片格式转换工具 再次转换为png，覆盖源文件即可，注意，压缩选择为 0
小编使用的工具：http://cdn1.mydown.yesky.com/5666bdbd/70d8d5c8c48f50e9108175dee1808273/soft/201501/pconverter_setup_4.8.3.rar


解决方案二：
buildToolsVersion "23.0.1"
--->
buildToolsVersion "19.1.0"

7、Your project path contains non-ASCII characters
-------------------------------------------------

问题：
![这里写图片描述](http://7xoq4d.com1.z0.glb.clouddn.com/as120804.jpg?attname=&e=1449665575&token=dDQYTry5zke_2YwAfs6GWn4lbjAOS7r4b3iCX-Kc:oxUMH5qPJQ1O7RHdMG_Z8szq0HM)

分析：文件路径有中文
解决：修改路径为英文

8、Error:Execution failed for task ':app:dexDebug'.
--------------------------------------------------

问题：
Error:Execution failed for task ':app:dexDebug'.
 com.android.ide.common.process.ProcessException: org.gradle.process.internal.ExecException: Process 'command 'C:\Program Files\Java\jdk1.8.0\bin\java.exe'' finished with non-zero exit value 2
分析：重复的jar包（相同的包名，但是文件不同（版本不同等））
解决：删除不需要的jar包

**详解：**

> 当多个项目有依赖关系，且使用了相同的Jar包，显然，这个时候我们不可能删除其中的jar包，但是一直报这个错，其实在AndroidStudio中，如果有相同jar包名的jar包，默认只会选用其中一个，但是这仅仅是包名相同，文件也相同（如support-v4.jar，如果版本相同，就不会存在冲突，如果不同，则会冲突报错）。
> 
> 如果你是从Eclispe转过来之类的操作，构建AndroidStudio项目内部的情况无从得知，如果还是报错，可以使用资源管理器搜索该jar包，删除构建生成的（gradle clean 命令或者执行clean操作不一定能够完全删除），仅留下源文件，再次build就可以啦。



9、非法字符: '\ufeff' 解决方案|错误: 需要class, interface或enum
-------------------------------------------------

分析：
Eclipse可以智能的把UTF-8+BOM文件转为普通的UTF-8文件，Android Studio还没有这个功能，所以使用Android Studio编译UTF-8+BOM编码的文件时会出现” 非法字符: '\ufeff' “之类的错误

解决：
使用notepad++或者其他编辑工具 格式转换为   UTF-8BOM格式编码


10、 lintOptions false
---------------------

分析：
AndroidStudio编译条件过于严格，需要屏蔽掉lint检查

```
 lintOptions {
      abortOnError false
  }
```

11、Missing one of the key attributes 'action#name,category#name' on element intent-filter at AndroidManifest.xml
------------------------------------------------------------------------

分析：AndroidStudio编译条件相对严格，必须要有 name的配置

```
<intent-filter 
    android:priority="90">
               
 </intent-filter>
```

修改为

```
<intent-filter 
	  android:priority="90">
      <action android:name="HEHEHE" />
 </intent-filter>
```