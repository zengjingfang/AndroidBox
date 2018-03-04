# 前言：
使用AndroidStudio已经有一段时间了，感受到了其中的优势，但是时间一直很紧，还有许多强大的功能没有用到，亟待一步步的学习使用。最近接了个大点的项目，原先开发人员使用的是Eclipse，依赖的project比较多，依赖关系复杂，按照之前的老办法（使用Eclipse导出为gradle），然后根据build报错逐个改之，但是现在发现当我run的时候可以成功，然而build是是失败的。而且，run的时候不断的报一些错误，实在影响开发还有我的心情。老大建议我最好重新配置，不然会是一个无底洞，改不来的。遵从老大的建议，特地花了点时间来配置，在此中间遇到了许多问题，还好有老大指点，而且有写依赖project老大已做修改并上传到了maven仓库，所以纠结了好几晚寝食难安的日子，终于跑通了。好吧，有点小小的激动和啰嗦，其实回过头来才发现踩了许多的坑，走了很多弯路。本篇主要讲一些方法策略、技巧以及几个坑的解决方案。

>适用类型：Eclipse项目，依赖项目众多，依赖关系复杂，转为androidstudio，按照以下步骤走，一定高效。

# 策略

## 步骤一：Eclipse导出、快速获取到依赖关系（build.gradle中）

将项目在Eclipse上跑通，有些有源码的jar包，如果后期的开发需要修改，最好在这个时候全部依赖源码project。Eclipse编译成功后，Export为gradle版本，此时一定要记得查看（坑一：有时候Eclipse并没有将所有的项目导出为gradle）。查看的文件主要是跟目录下的settings.gradle,build.gradle,以及每个moudle（eclipse中的project）的build.gradle文件。

##  步骤二：使用百度脑图等工具，理清楚依赖关系

打开[百度脑图](http://naotu.baidu.com/)，新建“思维导图"，然后遍历每个moudle下的build.gradle中的dependencies依赖的moudle，被依赖的作为主moudle的下一级，这样不管你有多少个moudle，一个个敲上去，依赖关系一目了然。（之前没有做思维导图搞头搞晕，花了一两小时一个个敲上去，后边查起来方便，明显是脑子笨，记不住）。

## 步骤三：按照AndroidStudio默认的工程目录调整文件结构

调整src、res、aidl、assets、libs、manifest.xml等文件如下结构

+ .idea
+ moudle
  - build
 -  libs
 - src
	 + main
		 - aidl
		 - java
			 + com
				 -  baidu
				 + .............(java源码）
		 - res
		 - AndroidManifest
 - build.gradle
+ build.gradle
+ settings.gradle
+ config.gradle

## 步骤四：修改build.gradle,修复依赖关系

记住一条，依赖相同名称的lib只能使用一模一样的，不管是jar、aar和依赖的moudle，原因就是出现相同的包名的Java文件时，AS不知道该使用哪一个。所以在这一步，建议将那些不改的lib或者依赖的第三方lib全部依赖maven仓库上的，公司有私有的maven仓库，所以有许多lib老大之前已上传，做起了快多了。强烈建议将libs中的jar或者aar释放出来，不然多个moudle如果依赖相同包名的lib,改一个就得全部替换。

## 步骤五：优化依赖以及Gradle统一依赖管理

如果A依赖B、B依赖C同时A依赖C，那么A依赖C完全可以去掉，这个在Eclipse项目里有许多的重复的。
Gradle统一依赖管理还是请参考大神的博客，里边很详细，配置一下再也不担心依赖冲突的问题啦！
[Gradle依赖的统一管理——stormzhang](http://mp.weixin.qq.com/s?__biz=MzA4NTQwNDcyMA==&mid=402733201&idx=1&sn=052e12818fe937e28ef08331535a179e&scene=1&srcid=0319de63hkT90KaCxN6t432J#wechat_redirect)

# 踩过的坑

## 坑一：R文件不存在

这个问题好坑爹，无法生成R文件，moudle里边的res资源无法使用，res资源经查也没有问题，直接编译不过。clean、Invalidate Caches 、Restart 、Google加终极大法重启电脑都没用，后来找张总给我支了一招，在settings.gradle注释掉其他的moudle，单个构建就OK啦。（现在想想，应该是gradle构建的问题，删除掉.idea也许管用）

## 坑二：本地依赖和maven仓库的依赖冲突
报错如下：

```
app\build\intermediates\res\merged\debug\drawable-mdpi-v4\xxx.png: error：Duplicate file.
appbuild\intermediates\res\merged\debug\drawable-mdpi\xxx.png: error：Original is here.The version qualifier may be implied.
```

看字面意思很容易理解是文件冲突了，但是就是想不通怎么回事，会冒出个drawable-mdpi-v,后来想了想是不是重maven仓库了下载下来的，直接打开C盘底下的.gradle下的缓存文件，果然有这么个文件。可是并没有配置该依赖，所以分析只能是某个moudle下的lib里边有这个依赖，这个时候可以先分享可能引用到该文件的lib,然后在settings.gradle文件中采取注释掉不相关的，逐个排除，可以快速定位。

## 坑三：依赖的lib冲突
报错如下：

```
Unknown source file: UNEXPECTED TOP-LEVEL EXCEPTION:
Unknown source file: com.android.dex.DexEception:Multiple dex files define Lcom.xxx.xxx.MainAcitivity;
Unknown source file: at com.android .......................

```

如步骤四中所说，记死一条，在androidstudio的环境下相同包名的moudle、jar、aar等，只能存在一个，否则会冲突。这里就是提示com.xxx.xxx这个包名的moudle、jar、aar之类的出现了不一样的，所以在步骤二中理清楚依赖关系很重要。知道错了，但是找到错也需要时间。

# 技巧

## 技巧一：

使用百度脑图等工具，画出依赖关系，一看就懂，一搜就找到，随时同步自己的build.gradle中的配置，依赖的lib项目版本号等。

## 技巧二：

灵活使用setting.gradle,比较大的项目编译一次很花时间，我们可以选择注释不相干的moudle配置，单个构建，由下至上逐个构建，这个既节约时间也可以快速定位问题。

## 技巧三：

如果在AndroidStudio自带的Terminal构建会使电脑很卡，如果是clean再run就慢的要死。使用windows直接gradle build会好一点，如果可以再服务器上编译那就可以把构建工作丢给服务器，构建的时候还可以做其他的事情，不然电脑卡死浪费时间不说，直接影响心情，哈哈。

## 技巧四：

这不是技巧，而是我们常常忘记了我们的终极大法，那就是clean、Invalidate Caches 、Restart 、加重启电脑。还有个就是删除根目录的.idea文件，重新打开项目文件。此法请在实在想不通，已经绝望的时候使用。

# 结语
只有掉到坑里爬起来回过头才知道那个坑就不是个坑，而自己从一个傻逼进阶成更傻逼，现在能跑起来必须感谢张总在关键时候指点一二救我一命。本人菜鸟一个，开年第一篇技术博客，写的这么逗逼这么水。不喜勿喷，至少我的自尊心很强，内心很脆弱。跪求大神们指点赐教。
