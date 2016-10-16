前言：这里是使用AS的基本设置，适合新入手的朋友阅读，将这里介绍的设置完基本使用无忧啦。

1、setting介绍
-----------

 - 点击菜单栏：File | settings    
 -  快捷方式：ctrl+art+s   
 - 注意：我们可以在基本设置头部的搜索框直接输入你要设置的关键字直接进入

![这里写图片描述](http://7xoq4d.com1.z0.glb.clouddn.com/asImage%203.png?attname=&e=1448790863&token=dDQYTry5zke_2YwAfs6GWn4lbjAOS7r4b3iCX-Kc:2mUky4wGSZJggKkPKcJoxgl3F0w)

2、设置主题样式、字体大小
-------------

  File | settings |Appearance&Behavior|Appearance  如上图中，在右侧UI Options下方
  Theme 点击选择，如Darlua就是我们经典的样式。
  下方可以继续设置字体大小 颜色
 

3、 设置keymap 快捷方式
----------------

File | settings |Keymap 设置
右侧 下来选择eclipse 这样大部分的快捷方式都会和Eclipse相同。
之后有许多快捷方式需要添加修改都在其下方选择操作，后期再对快捷方式详细介绍。

4、 编程字体设置
---------

 - 此部分会修改编辑器的字体，包含所有的文件显示的字体。Settings --> Editor --> Colors & Fonts -->Font 。默认系统显示的 Scheme 为 Defualt ，你是不能编辑的，你需要点击右侧的 Save As...，保存一份自己的设置，并在当中设置。之后，在 Editor Font 中即可设置字体。Show only monospaced fonts
   表示只显示等宽字体，一般来说，编程等宽字体使用较多，且效果较好。
 - Settings --> Editor --> Colors & Fonts
   中可以还可以设置字体的颜色，你可以根据你要设置的对象进行选择设置，同时你也可以从网络上下载字体颜色设置包导入。

5、代码格式设置
--------

如果你想设置你的代码格式化时显示的样式，你可以这么设置。Settings --> Code Style 。同样的， Scheme 中默认的配置，你无法修改，你需要创建一份自己的配置。

6、默认文件编码
--------

无论是你个人开发，还是在项目组中团队开发，都需要统一你的文件编码。出于字符兼容的问题，建议使用 utf-8 。中国的 Windows 电脑，默认的字符编码为 GBK 。Settings --> File Encodings 。建议将 IDE Encoding 、 Project Encoding 、 Properties Fiels 都设置成统一的编码。

7、去掉Android Studio编辑区域中部竖线
--------------------------

这条线是用以提醒程序员，一行的代码长度最好不要超过这条线。如果你不想显示这条线，可以这么设置。Settings --> Editor --> Appearance ，取消勾选 Show right margin (configured in Code Style options) 。

8、插件
----

Android Studio和Eclipse一样，都是支持插件的。Android Studio默认自带了一些插件，如果你不使用某些插件，你可以禁用它。Settings --> Plugins ，右侧会显示出已经安装的插件列表。取消勾选即可禁用插件。 
建议禁用的插件（基本不需要用到）
VS Integration ： CVS 版本控制系统，用不到。
Google Cloud Tools For Android Studio ： Google云 用不到。
Google Login ： Google账号登录。
hg4idea ： Mercurial 版本控制系统，用不到。

9、检查更新
------

Android Studio支持自动检查更新。之前尚未发布正式版时，一周有时会有几次更新。你可以设置检查的类型，用以控制更新类型。Settings --> Updates 。勾选 Check for updates in channel ，即开通了自动检查更新。你可以禁用自动检查更新。右侧的列表，是更新通道。
Stable Channel ： 正式版本通道，只会获取最新的正式版本。
Beta Channel ： 测试版本通道，只会获取最新的测试版本。
Dev Channel ： 开发发布通道，只会获取最新的开发版本。
Canary Channel ： 预览发布通道，只会获取最新的预览版本。
以上4个通道中， Stable Channel 最稳定，问题相对较少， Canary Channel 能获得最新版本，问题相对较多。

10、自动导入（import）。
----------------

当你从其他地方复制了一段代码到Android Studio中，默认的Android Studio不会自动导入这段代码中使用到的类的引用。你可以这么设置。Settings --> Editor --> Auto Import ，勾选 Add unambiguous improts on the fly 。

11、SDK设置
--------

File --> Other Settings --> Default Project Structure

12、打开一直停留在 Fetching AndroidStudio Component information
-------------------------------------------------------

这是在检查你的 Android SDK 。有人会在这里卡上很长时间，很大的原因就是：网络连接有问题。可以通过配置hosts 的方式来解决。如果检查需要更新，则需要你进行安装 。解决方案：在Android Studio安装目录下的 bin 目录下，找到 idea.properties 文件，在文件最后追加disable.android.first.run=true 。

13、Logcat颜色设置
-------------

Settings --> Editor --> colors&fonts -->AndroidLogcat 
可以对每个级别的进行设置

14、显示行号
-------

File-->Setting--> Editor --> General -->Appearance，勾选Show line numbers来设置

15、显示空格
-------

可通过File-->Setting--> Editor --> General -->Appearance，勾选Show whitespaces来设置。

16、鼠标悬浮显示doc
------------

Settings->IDE Settings->Editor->Show quick doc on mouse move

17、修改内存、使用更流畅
-------------

在android studio目录下找到：studio64.exe.vmoptions文件，修改的参数（-Xmx750m)，将默认参数修改为2048MB（-Xmx20480m）。既将内存增加到2G，这里可以根据自己的电脑进行合理的配置，如果觉得还是不够流畅，可以改得更高。
注意：设置完毕后需要重启AS，File --> Invalidate catches /Restart
此操作可以清除缓存然后再重启

18、代码提示 原eclipse中的 art+/
------------------------

Keymap --> [右下方]others --> Class Name Completion，快捷键是Ctrl+Alt+Space（空格键）。
可以自行修改。

19、设置成员变量常用的前缀 m
----------------

![这里写图片描述](http://7xoq4d.com1.z0.glb.clouddn.com/asImage%204.png?attname=&e=1448797207&token=dDQYTry5zke_2YwAfs6GWn4lbjAOS7r4b3iCX-Kc:uJc6dg5TNZZCwgQooWd149fDyFY)

20、注释设置
-------

Settings --> Editor -->File and Code Templates  -->Class
[点击参考详细介绍](http://jingyan.baidu.com/article/e6c8503c7195b7e54f1a1898.html)

21、编译错误是提醒颜色样式设置 error 右侧栏哪些红色小块
--------------------------------

![这里写图片描述](http://7xoq4d.com1.z0.glb.clouddn.com/asImage%205.png?attname=&e=1448798083&token=dDQYTry5zke_2YwAfs6GWn4lbjAOS7r4b3iCX-Kc:3bFNj-VQ19B1IHCIKDRwTylKMKs)

22、Logcat的console中，显示”no debuggable applications”
-------------------------------------------------

 Tools --> Android --> Enable ADB Integration 点击会有打钩，表示已选择
 

23、导入jar文件
----------

AS并不能如Eclipse那样直接复制就可以啦，还需要按照规矩导入到你的Moudle的lib下。
第一步：复制你需要添加的jar，并将其黏贴到app/src/main/libs文件夹下;
第二步：选中右击粘贴的jar文件，选中菜单中的 Add as library ,然后再下拉选择对应的Moudle。

24、导入arr文件
----------

[请点击参考：AndroidStudio快速入门三：aar和jar 生成和导入项目](http://blog.csdn.net/jf_1994/article/details/50084349)


25、删除Moudle
-----------

AS相对Eclipse而言，保护措施做得更加严格
删除一个Module，直接在IDE中选中Module后按Delete是删不掉的，需要先右键project-->Open Module Settings-->在弹出面板的左侧Modules一栏中选中要删除的Module-->点击面板左上角的“-”符号-->点击OK后回到IDE，然后选中要删掉的Module，按Delte快捷键删掉即可；