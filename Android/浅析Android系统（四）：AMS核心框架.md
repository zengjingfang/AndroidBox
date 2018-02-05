# 前言
本文并未深入的去探讨AMS整个框架的各个细节原理，仅从框架的角度出发，理清楚了AMS框架内AMS、AMN、AMP，APT、APN、ATP,以及ActvityThread、mH、Instrumention、Application等各个角色之间的关系，已解答我们一直存在心里的疑惑，适合于应用App开发者阅读。AMS框架能够顺利运转，是基于Binder通信机制之上的，前期需要先了解Binder。本人水平有限，理解有误的地方，敬请批评指正。

# 一、开机init系统环境
Android系统底层基于Linux Kernel, 当Kernel启动过程会创建init进程, 该进程是uoyou用户空间的鼻祖, init进程会启动servicemanager(binder服务管家), Zygote进程(Java进程的鼻祖). Zygote进程会创建 system_ server进程以及各种app进程.【摘自：Gityuan.com】
故，Zygote进程是所有进程的父进程，包括system_server进程。至于SM本篇就不再说明了。
![](http://ww1.sinaimg.cn/large/aea705afgy1fo5xi04h8yj20u00i2aau.jpg)


由上图，我们可了解到当我们开启启动系统的时候，一切从系统底层的init开始，构建出了zygote进程、SM进程。zygote负责fork新的进程，如system_server,launcher以及我们平时开发的各种所谓的app。而SM是构建并管理了FrameWorke层的各种Service,其中最重要的就是我们本篇要说的ActivityServiceManger，同时还负责了不同进程之前相互Binder通信。明白了这两个角色间的如何分工与合作就非常重要。

# 二、AMS核心框架图
![](http://ww1.sinaimg.cn/large/aea705afgy1fo5xgfla5dj20nb0dujrv.jpg)
由上述，我们假设机器已经启动，并且Zygote和SM都准备好了，System_server进程也由Zygote构建好了。通常，我们首先要看到的就是桌面Lancher，这个Lancher实际就是一个新的App进程。对照下图，大概解释下Lancher进程进来时要做什么。并且理解下这个框架图。

+ 1、App的进程通过Zygote进程fork起来，一切从ActivityThread.main（）方法开始。
+ 2、App的ActivityThread继续调用attch()方法，将Lancher的ApplicationThread关联到AMS,这样，System_Server端可以AMS调用ATP(ApplicationTheadProxy)（Binder机制的客户端）,并且通过Binder联通了App端的ApplicationThread(Binder机制的服务端)。这样，下图中的AcitvityManagerService(AMS)->ApplicationThreadProxy(ATP)->ApplicationThead(APT)通道就打通了。
+ 3、由上述2中的通道，ASM发出了bindApplication的指令，APT收到并调用了bindApplication（）方法；
+ 4、APT需要通过mH这个handle消息队列，最终掉起ActivityThead的handleBindApplication()方法；
+ 5、执行handleBindApplication()，根据AMS带回来的的信息进行App的部分构建操作。
	+ 构建出mInstrumentation这个App管家婆；
	+ make 一个 我们应用App中经常看到的Application；
	+ 把这个Application交给mInstrumentation这个管家婆，并且回调Application的onCreate（）；

到现在为止，我们可以理解为桌面的App已经起来了，只是还没有界面。暂且忽略掉桌面Lancher这个App的界面加载过程，我们假设已经起来了，这个时候在桌面点击一个App图标，打开了另外一个App。

+ 1、Lancher的Activity调用了startActivity;
+ 2、Lancher的mInstrumentation管家婆调用了execStartActivity(),这样就到了下图的Instrumentation这里。
+ 3、通过Instrumentation->AMS->ATP,ApplicationThreadProxy调用了scheduleLancherActivity()方法；
+ 4、AMS端查看目标activity是否为当前进程的Activity，或者目标activity的进程已经起来了;
	+ 否：要先fork目标activity的进程；
	+ 是：APT响应scheduleLancherActivity();
+ 5、APT->mH->ActivityThread,ActivityThread调用handleLancherActivity()方法;
+ 6、接着调用ActivityThread.performLancherActivity(),构建出Activity实例，mInstrumentation回调Activity的onCreate方法。
+ 7、添加该activity到ArrayMap<IBinder,ActivityClientRecord>到mActivities队列里面。

![](http://ww1.sinaimg.cn/large/aea705afgy1fo5xeu0l29j21311eegp7.jpg)


# 三、AMS核心代码解析

### ActivityThead.main()

![](/pic/start_app_process.jpg)

	 public static void main(String[] args) {	
        Process.setArgV0("<pre-initialized>");

        Looper.prepareMainLooper();//准备MainLooper

        ActivityThread thread = new ActivityThread();
		// attcha方法
        thread.attach(false);

        if (sMainThreadHandler == null) {
            sMainThreadHandler = thread.getHandler();
        }
	}

### ActivityThead.attch()


 		private void attach(boolean system) {
                
            android.ddm.DdmHandleAppName.setAppName("<pre-initialized>",
                                                    UserHandle.myUserId());
            RuntimeInit.setApplicationObject(mAppThread.asBinder());
            final IActivityManager mgr = ActivityManagerNative.getDefault();
            try {
				//将这个一个App的ApplicationThread绑定到AMS
				//准备好了System_server进程的AMS-ATP-Binder-APT这一段路的通信！！！
                mgr.attachApplication(mAppThread);
            } catch (RemoteException ex) {
                // Ignore
            }
            // Watch for getting close to heap limit.
       }   
        
### ActivityThead.handleBindApplication()

	private void handleBindApplication(AppBindData data){
		final ContextImpl appContext = ContextImpl.createAppContext(this, data.info);
		 if (data.instrumentationName != null) {
				 LoadedApk pi = getPackageInfo(instrApp, data.compatInfo,
                    appContext.getClassLoader(), false, true, false);
                 ContextImpl instrContext = ContextImpl.createAppContext(this, pi);
			     try {
                	java.lang.ClassLoader cl = instrContext.getClassLoader();
					// 根据AMS响应回来的的信息，构建出mInstrumentation这个管家婆
                	mInstrumentation = (Instrumentation)
                    cl.loadClass(data.instrumentationName.getClassName()).newInstance();
            }
		 }
		//make 一个 我们应用App中经常看到的Application
 		Application app = data.info.makeApplication(data.restrictedBackupMode, null);
		// 把这个Application交给mInstrumentation这个管家婆，并且回调Application的onCreate（）
	    mInstrumentation.callApplicationOnCreate(app);
	}


###  ActivityThead.performLaunchActivity()

	private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
       
        ActivityInfo aInfo = r.activityInfo;
        if (r.packageInfo == null) {
            r.packageInfo = getPackageInfo(aInfo.applicationInfo, r.compatInfo,
                    Context.CONTEXT_INCLUDE_CODE);
        }
        Activity activity = null;
        try {
            java.lang.ClassLoader cl = r.packageInfo.getClassLoader();
			// new出该activity的实例
            activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);
        } catch (Exception e) {
             // "Unable to instantiate activity "
            }
        }	
		Application app = r.packageInfo.makeApplication(false, mInstrumentation);

		// 创建了BaseContext
		Context appContext = createBaseContextForActivity(r, activity);
		// attch该activity
		activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor);
		// 回调Activity的onCreate方法
		 mInstrumentation.callActivityOnCreate(activity, r.state);
		 r.activity = activity;
		//将activity加入到ActivityThead的ArrayMap<IBinder,ActivityClientRecord>队列
		mActivities.put(r.token, r);
	}


# 小结

本文主要梳理了AMS、AMN、AMP，APT、APN、ATP,以及ActvityThread、mH、Instrumention、Application，这几个角色的职责，以及每个角色之间的关系，从而梳理出了AMS的核心框架图。其中有非常重要的两个Binder接口类，首先是IActivityManager,他的Client端在普通的App这边，server端在system_ server这边，这样就实现了App->System_ Server的通信，同样的IApplicationThread刚好反了过来，实现了system _server->App 之间的通信。如此，便实现了System _server和App之间的相互通信。而ApplicationThread到ActivityThread中间用了mH这个Handle保证了消息的有序进行，并且ActvitityThread找了mInstrumention这个管家婆帮忙管事，减少了自己的代码负担。理解了整个框架如何进行通信的，那么如何进行四大组件生命周期的管理就是在这条AMS通信框架下的具体应用实现了。


## LancherMode

+ Standard:标准模式，启动一个就New一个Activity实例；
+ SingTop:栈顶单列模式
	+ 栈顶有：就不New一个Activity实例，但是会糊掉onNewIntent()方法。
	+ 栈顶没有，同Standard模式；
+ SingTask：单例模式，系统中只能创建一个唯一的Activity实例；
	+ 已存在：Task列表前的都出栈，该Activity实例带到栈顶，同时回调onNewIntent()方法；
	+ 不存在：New一个Activity实例；
		+ 该Activity实例的taskAffinity属性和当前task一致，则加入到当前task;
		+ 该Activity实例的taskAffinity属性和当前task不一致，则启动新的task,并加入；

+ SingleInstance：高度单列模式，Activity具有系统唯一性，同时位于一个独立的task中；

## Acivity属性

+ allowTaskReparenting:



## AMS
		