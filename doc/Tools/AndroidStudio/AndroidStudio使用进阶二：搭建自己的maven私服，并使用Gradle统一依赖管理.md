# 前言： #

最近我们老大组织了我们软件团队开了一个小会，说了一些存在的问题，平时在技术上的交流还是比较少的，尤其是在不同的项目之间的开发人员，然而经过这次会议我突然发现，我的缺陷不仅是在基础的能力上，还有一点就是对新的技术，编程思想知之甚少,就用low来形容吧。所以即使再忙也不能把自己陷在敲代码的体力活中，有时候真得跳出来多看看，了解一些主流的技术,使用一些工具，既节约时间，而且可以写出一份优秀的代码。

# 问题： #

+ 将Android项目从Eclipse转到AndroidStudio后，我们所有的依赖，jar，aar包等必须要保持一致，不然就会冲突构建失败。
+ 如果依赖的module太多，构建一个project时每个依赖的module都需要构建，比较费时，然后我也不想去copy已经编译好的aar，因为有时候需要修改依赖的module，需要打开源码，修改后需要上传新版本的aar等等。

#方案：#

参考了大神们Gradle统一依赖的管理方式，并再次加工，将所有的依赖放到一个config.gradle的配置文件里边。同时写好依赖maven私库和源码的module的配置，这样就可以在源码和aar之间自由切换。

准备工作：使用Nexus Repository搭建maven私服

搭建maven私服很简单，可以参考[ Android 项目部署之Nexus私服搭建和应用](http://blog.csdn.net/l2show/article/details/48653949),公司已经搭好了maven私库，但是毕竟是公共的，不是那么自由，所以我就搭在本地了。下面是主要的一些配置。

Maven的配置文件：D:\xxx\Maven\apache-maven-3.3.9\conf\settings.xml

      <localRepository>E:\***\repository</localRepository> //本地仓库地址，先行创建好文件夹


	  //nexus服务
      <server>     
      	<id>my-nexus-releases</id>     
      	<username>admin</username>     
      	<password>admin123</password>     
   	  </server>     
  	  <server>     
       	<id>my-nexus-snapshot</id>     
      	<username>admin</username>     
      	<password>admin123</password>     
      </server>  
 
	 //镜像

	 <mirror>        
	     <id>nexus</id>         
	     <url>http://localhost:8081/nexus/content/groups/public/</url>        
	     <mirrorOf>*</mirrorOf>        
	   </mirror>  

	//nexus仓库配置  
  
 	 <profile>
      <id>nexusProfile</id>
      <repositories>
        <repository>
          <id>nexus</id>
          <name>Nexus Repository</name>
          <url>http://xxx.xx.xx.xx:8081/nexus/content/groups/public/</url>
          <layout>default</layout>
          <releases> 
			<enabled>true</enabled>
          </releases>
          <snapshots>
          <enabled>true</enabled>
            </snapshots>
        </repository>
      </repositories>
    </profile>

    //激活
	 <activeProfiles>
	    <activeProfile>nexusProfile</activeProfile>
	  </activeProfiles>

# 演示项目： #

假设app为主工程，依赖的lib3,lib2,而lib2依赖了lib1。 

## settings.gradle 配置 ##

	include ':lib1'
	include ':lib2'
	include ':lib3'
	include ':app'

## maven私库配置，根目录下的build.gradle中的配置 ##

    apply from:"config.gradle" //配置统一依赖管理文件

	buildscript {
	    repositories {
	        jcenter()     
	    }
	    dependencies {
	        classpath 'com.android.tools.build:gradle:1.3.0'
	    }
	}
	
	allprojects {
	    repositories {
	        jcenter()	   
	        maven { url MAVEN_URL }//配置maven私服
	    }
	}

## 全局变量，根目录下的gradle.properties ##

	MAVEN_URL= http://xxx.xx.xx.xx:8081/nexus/content/repositories
	MAVEN_SNAPSHOT_URL = http://xxx.xx.xx.xx:8081/nexus/content/repositories/thirdparty-snapshot/
	#对应maven的groupId值
	GROUP=group
	#登录nexus oss的用户名
	NEXUS_USERNAME=admin
	#登录nexus oss的密码
	NEXUS_PASSWORD=admin123
	# groupid
	GROUP_ID = group
	# type
	TYPE = aar
	# description
	DESCRIPTION = dependences lib
`

## app的build.gradle ##

	dependencies {
	    compile fileTree(dir: 'libs', include: ['*.jar'])
	    compile rootProject.ext.dependencies["lib2"]
	    compile rootProject.ext.dependencies["lib3"]
	}

## lib2的build.gradle ##

	dependencies {
		 compile fileTree(dir: 'libs', include: ['*.jar'])
		 compile rootProject.ext.dependencies["lib1"]
	}

## 自动配置上传aar到maven私库的配置 ##

    uploadArchives {
    configuration = configurations.archives
    repositories {
        mavenDeployer {
            snapshotRepository(url: MAVEN_SNAPSHOT_URL) {
                authentication(userName: NEXUS_USERNAME, password: NEXUS_PASSWORD)
            }

            repository(url: MAVEN_URL) {
                authentication(userName: NEXUS_USERNAME, password: NEXUS_PASSWORD)
            }

            pom.project {
                version rootProject.ext.uploadArchives["lib1"]//版本号
                artifactId 'lib1'
                groupId GROUP_ID
                packaging TYPE
                description DESCRIPTION
            }
        }
    }
	}

## 统一依赖config.gradle ##

	// 依赖全局控制  源码工程和maven私库的aar自由控制,比如现在 lib2依赖lib1的aar 只要注释下方的源码依赖，打开上面的maven依赖即可

	dependencies = [
		"lib1"		: 'group:lib1:1.0.0+',
		//"lib2"		: 'group:lib2:1.0.0+',
		//"lib3"		: 'group:lib3:1.0.0+'，

 		// "lib1" : project(':lib1'),//lib2(我在后边注明依赖lib1的工程lib2,依赖关系一目了然）
		"lib2" : project(':lib2'),//app
		"lib3" : project(':lib3'),//app

    ]

	//上传时修改aar版本号

	 uploadArchives = [
 		 lib1                 : "1.0.0",
		 lib1                 : "1.0.0",
		 lib1                 : "1.0.0"
	 ]

# 注意： #

+ 这么搞有个问题，如果一个lib2依赖的是lib1,那么此时构建生成的aar,会将依赖lib1的aar版本打包一起上传到maven，所以我在版本号后边增加了“+"号，永远保持依赖最新的。

+ 如果lib1打开了源码，那么依赖了lib1的module也要打开源码，逐层上推。

# 请教： #

**我将所有的依赖都放到config.gradle中，一是为了方便管理，二是希望能够实现每个module可以在源码和aar之间自由的切换，但是第二点并没有达到最理想的效果，求大神们给出更好的方案。**

# 结语： #

这篇博客还是上周写了一大半，中间有些事情耽搁了，一直拖到现在。今晚和一个老同学聊天，我问道她一个问题：站在女生的角度来说，你希望找的男朋友是事业有成的呢？还是愿意找个一无所有的一起创造的呢？她的回答是：其实重点是会照顾人，对女朋友好，当然事业有成最好咯，但往往很少啦，如果不是，至少要是个潜力股，还有，如果真的在乎那个女孩，**就算她愿意陪你吃苦，你也是不愿意她为你吃苦的**。我觉得说的很真实，所以，还是加油吧。现在办公室就我一个人，打开音响可以放心的把音量调高一点，这样算是给自己一点放松吧。这段一直关注stormzhang的公众号AndroidDeveloper,里边的一篇：我到底有多么拼命，的确和他比确实弱爆了。或许就如stormzhang所说的，作为一个初级程序员，我的生活就是工作。前段曾为发一个版本搞了个通宵，然而发现最近有些急躁或者疲倦了，当然我也会及时的调整自己，我觉得保持持续的激情就是我们所说的坚持吧。这段老说自己很忙，但是我真的很忙吗？我只是希望忙的时候多注意一些方法，要有效率，要有成果。

