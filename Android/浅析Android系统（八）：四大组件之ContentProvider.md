# ContentProvider

## 关键流程

+ 准备： 通过System-Server拿到目标Provider-Process中已经Publish的IContentProvider
	+ 若该Provider-Process不存在，则先启动该进程，该进程的Application在回调onCreate之前会调用T.InstallContentProviders()安装Providers。然后会在在System-Server进程Publish。(1>2>3>4>5[APT.bindApplication()]>6>7>8>9>11>3>10)
	
	
	+ 若该Provider-Process存在，但是在System-Server中该Provider的ContentProviderRecord不存在，则要通知Provider-Process进行AT.ScheduleInstallProvider()安装该Provider,并且返回AMS,通知AMS进行Publish该Provider。(1>2>3>4>5[APT.scheduleInstallProvider()]>6>7>8>9>11>3>10)

	+ 若该Provider-Process存在，且System-Server中该Provider的ContentProviderRecord存在，且已Publish，则直接返回。(1>2>3>4>11>3>10)
	
+ 安装： Client _Process 进程安装上一步中获取到的Provider的IContentProvider接口，进行计数操作。
+ 使用： 此时，IContentProvider已经OK,Client-Process通过CPP(ContentProiverProxy)和Proivder-Process进程通过Binder机制交互，完成操作。

## 流程图



## 核心代码

### 准备：ActivityManagerService.getContentProviderImpl()

	private final ContentProviderHolder getContentProviderImpl(IApplicationThread caller,
        String name, IBinder token, boolean stable, int userId) {
    ContentProviderRecord cpr;
    ContentProviderConnection conn = null;
    ProviderInfo cpi = null;

    synchronized(this) {
        //获取调用者的进程记录ProcessRecord；
        ProcessRecord r = getRecordForAppLocked(caller);
        //从AMS中查询相应的ContentProviderRecord
        cpr = mProviderMap.getProviderByName(name, userId);
        ...
        boolean providerRunning = cpr != null;

        
        if (providerRunning) {
            // 目标provider已存在的情况 
			cpi = cpr.info;
            //当允许运行在调用者进程且已发布，则直接返回
            if (r != null && cpr.canRunHere(r)) {
                ContentProviderHolder holder = cpr.newHolder(null);
                holder.provider = null;
                return holder;
            }

            final long origId = Binder.clearCallingIdentity();
            //增加引用计数
            conn = incProviderCountLocked(r, cpr, token, stable);
            if (conn != null && (conn.stableCount+conn.unstableCount) == 1) {
                if (cpr.proc != null && r.setAdj <= ProcessList.PERCEPTIBLE_APP_ADJ) {
                    //更新进程LRU队列
                    updateLruProcessLocked(cpr.proc, false, null);
                }
            }

            if (cpr.proc != null) {
                boolean success = updateOomAdjLocked(cpr.proc); //更新进程
                if (!success) {
                    //provider进程被杀,则减少引用计数 
                    boolean lastRef = decProviderCountLocked(conn, cpr, token, stable);
                    appDiedLocked(cpr.proc);
                    if (!lastRef) {
                        return null;
                    }
                    providerRunning = false;
                    conn = null;
                }
            }
        }

        if (!providerRunning) {
             // 目标provider不存在的情况

			//当provider并没有处于mLaunchingProviders队列，则启动它
            if (i >= N) {
                try {
                     
                    //查询进程记录ProcessRecord
                    ProcessRecord proc = getProcessRecordLocked(
                            cpi.processName, cpr.appInfo.uid, false);

                    if (proc != null && proc.thread != null) {
                        if (!proc.pubProviders.containsKey(cpi.name)) {
                            proc.pubProviders.put(cpi.name, cpr);
                            //启动provider进程启动并发布provider !!!!!!!!!
                            proc.thread.scheduleInstallProvider(cpi); 
                        }
                    } else {
                        // 启动进程!!!!
                        proc = startProcessLocked(cpi.processName,
                                cpr.appInfo, false, 0, "content provider",
                                new ComponentName(cpi.applicationInfo.packageName,
                                        cpi.name), false, false, false);
                        if (proc == null) {
                            return null;
                        }
                    }
                    cpr.launchingApp = proc;
                    //将cpr添加到mLaunchingProviders
                    mLaunchingProviders.add(cpr);
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            }
        }
    }
  
    synchronized (cpr) {
        while (cpr.provider == null) {
             //循环等待provider发布完成
        }
    }

    return cpr != null ? cpr.newHolder(conn) : null;
	}

### 发布 ActivityManagerService.publishContentProviders()

	/**
     * 1、进程发布，构建出ContentProviderRecord对象
     * 2、将该provider添加到mProviderMap
   	 * 3、移除provider发布超时的消息
   	 * 4、唤醒客户端的wait等待方法
     */
	public final void publishContentProviders(IApplicationThread caller,
       List<ContentProviderHolder> providers) {
   		if (providers == null) {
       return;
   		}

   	synchronized (this) {
       final ProcessRecord r = getRecordForAppLocked(caller);
       if (r == null) {
           throw new SecurityException(...);
       }

       final long origId = Binder.clearCallingIdentity();
       final int N = providers.size();
       for (int i = 0; i < N; i++) {
           ContentProviderHolder src = providers.get(i);
           if (src == null || src.info == null || src.provider == null) {
               continue;
           }
			// 进程发布，构建出ContentProviderRecord对象
           ContentProviderRecord dst = r.pubProviders.get(src.info.name);
           if (dst != null) {
               ComponentName comp = new ComponentName(dst.info.packageName, dst.info.name);
               //将该provider添加到mProviderMap
               mProviderMap.putProviderByClass(comp, dst);
               String names[] = dst.info.authority.split(";");
               for (int j = 0; j < names.length; j++) {
                   mProviderMap.putProviderByName(names[j], dst);
               }

               int launchingCount = mLaunchingProviders.size();
               int j;
               boolean wasInLaunchingProviders = false;
               for (j = 0; j < launchingCount; j++) {
                   if (mLaunchingProviders.get(j) == dst) {
                       //将该provider移除mLaunchingProviders队列
                       mLaunchingProviders.remove(j);
                       wasInLaunchingProviders = true;
                       j--;
                       launchingCount--;
                   }
               }
               if (wasInLaunchingProviders) {
					移除provider发布超时的消息
                   mHandler.removeMessages(CONTENT_PROVIDER_PUBLISH_TIMEOUT_MSG, r);
               }
               synchronized (dst) {
                   dst.provider = src.provider;
                   dst.proc = r;
                   //唤醒客户端的wait等待方法
                   dst.notifyAll();
               }
               updateOomAdjLocked(r);
           }
       }
       Binder.restoreCallingIdentity(origId);
   	}    
	}


### 安装：

	/**
     * 1、获取到IContentProvider接口
   	 * 2、Attach该IcontentProvider,回调ContentProvider.onCreate()
   	 * 3、查取或者构建ProviderClientRecord
     */
    private IActivityManager.ContentProviderHolder installProvider(Context context,
            IActivityManager.ContentProviderHolder holder, ProviderInfo info,
            boolean noisy, boolean noReleaseNeeded, boolean stable) {
        ContentProvider localProvider = null;
        IContentProvider provider;
        if (holder == null || holder.provider == null) {
            ...
            Context c = null;
            ....

            try {
                final java.lang.ClassLoader cl = c.getClassLoader();
                localProvider = (ContentProvider)cl.
                    loadClass(info.name).newInstance();
				
				// 获取到IContentProvider接口
                provider = localProvider.getIContentProvider();

                if (provider == null) {
                    return null;
                }
               
                // XXX Need to create the correct context for this provider.
				// 将本ContentProvider关联到Context,并回调ContentProvider.onCreate()
                localProvider.attachInfo(c, info);
            } catch (java.lang.Exception e) {
                if (!mInstrumentation.onException(null, e)) {
                    throw new RuntimeException(
                            "Unable to get provider " + info.name
                            + ": " + e.toString(), e);
                }
                return null;
            }
        } else {
            provider = holder.provider;
            if (DEBUG_PROVIDER) Slog.v(TAG, "Installing external provider " + info.authority + ": "
                    + info.name);
        }

        IActivityManager.ContentProviderHolder retHolder;

        synchronized (mProviderMap) {
     
            IBinder jBinder = provider.asBinder();
            if (localProvider != null) {
                ComponentName cname = new ComponentName(info.packageName, info.name);
                ProviderClientRecord pr = mLocalProvidersByName.get(cname);
                if (pr != null) {
                   
                    provider = pr.mProvider;
                } else {
                    holder = new IActivityManager.ContentProviderHolder(info);
                    holder.provider = provider;
                    holder.noReleaseNeeded = true;
                    pr = installProviderAuthoritiesLocked(provider, localProvider, holder);
                    mLocalProviders.put(jBinder, pr);
                    mLocalProvidersByName.put(cname, pr);
                }
                retHolder = pr.mHolder;
            } else {
                ProviderRefCount prc = mProviderRefCountMap.get(jBinder);
                if (prc != null) {
                 
                    if (!noReleaseNeeded) {
                        incProviderRefLocked(prc, stable);
                        try {
							//AMS释放
                            ActivityManagerNative.getDefault().removeContentProvider(
                                    holder.connection, stable);
                        } catch (RemoteException e) {
                            //do nothing content provider object is dead any way
                        }







                    }
                } else {

					// 构建ProviderClientRecord
                    ProviderClientRecord client = installProviderAuthoritiesLocked(
                            provider, localProvider, holder);
                   
                    mProviderRefCountMap.put(jBinder, prc);
                }
                retHolder = prc.holder;
            }
        }

        return retHolder;
    }

### ContentProvider.attachInfo()

	private void attachInfo(Context context, ProviderInfo info, boolean testing) {
        mNoPerms = testing;

        /*
         * Only allow it to be set once, so after the content service gives
         * this to us clients can't change it.
         */
        if (mContext == null) {
            mContext = context;
            if (context != null) {
                mTransport.mAppOpsManager = (AppOpsManager) context.getSystemService(
                        Context.APP_OPS_SERVICE);
            }
            mMyUid = Process.myUid();
            if (info != null) {
                setReadPermission(info.readPermission);
                setWritePermission(info.writePermission);
                setPathPermissions(info.pathPermissions);
                mExported = info.exported;
                mSingleUser = (info.flags & ProviderInfo.FLAG_SINGLE_USER) != 0;
                setAuthorities(info.authority);
            }
            ContentProvider.this.onCreate();
        }
    }
