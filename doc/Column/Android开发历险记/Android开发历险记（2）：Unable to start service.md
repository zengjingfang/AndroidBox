### 一、问题背景

崩溃后台看到这个异常，实际查找代码并没有什么异常情况，只是简单的区startService

	java.lang.SecurityException: Unable to start service Intent { act=com.xxx.im.app.revive.pushinfo cat=[com.xxx.im] flg=0x20 pkg=com.xtc.watch cmp=com.xtc.watch/com.xxx.im.core.app.bridge.AppReviveService (has extras) }: Unable to launch app com.xtc.watch/10273 for service Intent { act=com.xxx.im.app.revive.pushinfo cat=[com.xxx.im] pkg=com.xtc.watch cmp=com.xtc.watch/com.xxx.im.core.app.bridge.AppReviveService }: user 0 is restricted
		at android.app.ContextImpl.startServiceCommon(ContextImpl.java:1769)
		at android.app.ContextImpl.startService(ContextImpl.java:1742)
		at android.content.ContextWrapper.startService(ContextWrapper.java:527)

### 二、源码追踪

#### ContextImpl.java

    private ComponentName startServiceCommon(Intent service, UserHandle user) {
        try {
            validateServiceIntent(service);
            service.prepareToLeaveProcess();
            ComponentName cn = ActivityManagerNative.getDefault().startService(
                mMainThread.getApplicationThread(), service,
                service.resolveTypeIfNeeded(getContentResolver()), user.getIdentifier());
            if (cn != null) {
                if (cn.getPackageName().equals("!")) {
                    throw new SecurityException(
                            "Not allowed to start service " + service
                            + " without permission " + cn.getClassName());
                } else if (cn.getPackageName().equals("!!")) {
                    // 异常抛出的位置
                    throw new SecurityException(
                            "Unable to start service " + service
                            + ": " + cn.getClassName());
                }
            }
            return cn;
        } catch (RemoteException e) {
            return null;
        }
    }


##### ActivityManagerService.java

    @Override
    public ComponentName startService(IApplicationThread caller, Intent service,
            String resolvedType, int userId) {
        enforceNotIsolatedCaller("startService");
        // Refuse possible leaked file descriptors
        if (service != null && service.hasFileDescriptors() == true) {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
        if (DEBUG_SERVICE)
            Slog.v(TAG, "startService: " + service + " type=" + resolvedType);
        synchronized(this) {
            final int callingPid = Binder.getCallingPid();
            final int callingUid = Binder.getCallingUid();
            final long origId = Binder.clearCallingIdentity();
            ComponentName res = mServices.startServiceLocked(caller, service,
                    resolvedType, callingPid, callingUid, userId);
            Binder.restoreCallingIdentity(origId);
            return res;
        }
    }


#### ActiveServices.java

 	ComponentName startServiceInnerLocked(ServiceMap smap, Intent service,
            ServiceRecord r, boolean callerFg, boolean addToStarting) {
        ProcessStats.ServiceState stracker = r.getTracker();
        if (stracker != null) {
            stracker.setStarted(true, mAm.mProcessStats.getMemFactorLocked(), r.lastActivity);
        }
        r.callStart = false;
        synchronized (r.stats.getBatteryStats()) {
            r.stats.startRunningLocked();
        }
      // error 发生的位置
        String error = bringUpServiceLocked(r, service.getFlags(), callerFg, false);
        if (error != null) {
           // error 发生的位置
            return new ComponentName("!!", error);
        }

        if (r.startRequested && addToStarting) {
            boolean first = smap.mStartingBackground.size() == 0;
            smap.mStartingBackground.add(r);
            r.startingBgTimeout = SystemClock.uptimeMillis() + BG_START_TIMEOUT;
            if (DEBUG_DELAYED_SERVICE) {
                RuntimeException here = new RuntimeException("here");
                here.fillInStackTrace();
                Slog.v(TAG, "Starting background (first=" + first + "): " + r, here);
            } else if (DEBUG_DELAYED_STARTS) {
                Slog.v(TAG, "Starting background (first=" + first + "): " + r);
            }
            if (first) {
                smap.rescheduleDelayedStarts();
            }
        } else if (callerFg) {
            smap.ensureNotStartingBackground(r);
        }

        return r.name;
    }


#### ActiveServices.java

	private final String bringUpServiceLocked(ServiceRecord r,
            int intentFlags, boolean execInFg, boolean whileRestarting) {
        //Slog.i(TAG, "Bring up service:");
        //r.dump("  ");

        if (r.app != null && r.app.thread != null) {
            sendServiceArgsLocked(r, execInFg, false);
            return null;
        }

        if (!whileRestarting && r.restartDelay > 0) {
            // If waiting for a restart, then do nothing.
            return null;
        }

        if (DEBUG_SERVICE) Slog.v(TAG, "Bringing up " + r + " " + r.intent);

        // We are now bringing the service up, so no longer in the
        // restarting state.
        if (mRestartingServices.remove(r)) {
            clearRestartingIfNeededLocked(r);
        }

        // Make sure this service is no longer considered delayed, we are starting it now.
        if (r.delayed) {
            if (DEBUG_DELAYED_STARTS) Slog.v(TAG, "REM FR DELAY LIST (bring up): " + r);
            getServiceMap(r.userId).mDelayedStartList.remove(r);
            r.delayed = false;
        }

        // Make sure that the user who owns this service is started.  If not,
        // we don't want to allow it to run.
       // 确保要启动的serveice的APP已经启动了，如果没有启动则不能启动service
      //  启动失败信息  is stopped
        if (mAm.mStartedUsers.get(r.userId) == null) {
            String msg = "Unable to launch app "
                    + r.appInfo.packageName + "/"
                    + r.appInfo.uid + " for service "
                    + r.intent.getIntent() + ": user " + r.userId + " is stopped";
            Slog.w(TAG, msg);
            bringDownServiceLocked(r);
            return msg;
        }

        // Service is now being launched, its package can't be stopped.
        try {
            AppGlobals.getPackageManager().setPackageStoppedState(
                    r.packageName, false, r.userId);
        } catch (RemoteException e) {
        } catch (IllegalArgumentException e) {
            Slog.w(TAG, "Failed trying to unstop package "
                    + r.packageName + ": " + e);
        }

        final boolean isolated = (r.serviceInfo.flags&ServiceInfo.FLAG_ISOLATED_PROCESS) != 0;
        final String procName = r.processName;
        ProcessRecord app;

        if (!isolated) {
            app = mAm.getProcessRecordLocked(procName, r.appInfo.uid, false);
            if (DEBUG_MU) Slog.v(TAG_MU, "bringUpServiceLocked: appInfo.uid=" + r.appInfo.uid
                        + " app=" + app);
            if (app != null && app.thread != null) {
                try {
                    app.addPackage(r.appInfo.packageName, r.appInfo.versionCode, mAm.mProcessStats);
                    realStartServiceLocked(r, app, execInFg);
                    return null;
                } catch (RemoteException e) {
                    Slog.w(TAG, "Exception when starting service " + r.shortName, e);
                }

                // If a dead object exception was thrown -- fall through to
                // restart the application.
            }
        } else {
            // If this service runs in an isolated process, then each time
            // we call startProcessLocked() we will get a new isolated
            // process, starting another process if we are currently waiting
            // for a previous process to come up.  To deal with this, we store
            // in the service any current isolated process it is running in or
            // waiting to have come up.
            app = r.isolatedProc;
        }
        // 已经开始启动service，但是APP挂了，先把service加入到记录，然后等到APP起来再继续
        // Not running -- get it started, and enqueue this service record
        // to be executed when the app comes up.
        if (app == null) {
            if ((app=mAm.startProcessLocked(procName, r.appInfo, true, intentFlags,
                    "service", r.name, false, isolated, false)) == null) {
               // 启动失败信息  process is bad
                String msg = "Unable to launch app "
                        + r.appInfo.packageName + "/"
                        + r.appInfo.uid + " for service "
                        + r.intent.getIntent() + ": process is bad";
                Slog.w(TAG, msg);
                bringDownServiceLocked(r);
                return msg;
            }
            if (isolated) {
                r.isolatedProc = app;
            }
        }

        if (!mPendingServices.contains(r)) {
            mPendingServices.add(r);
        }

        if (r.delayedStop) {
            // Oh and hey we've already been asked to stop!
            r.delayedStop = false;
            if (r.startRequested) {
                if (DEBUG_DELAYED_STARTS) Slog.v(TAG, "Applying delayed stop (in bring up): " + r);
                stopServiceLocked(r);
            }
        }

        return null;
    }

> 根据上述代码，还是没有找到：user 0 is restricted 的位置，全盘搜索也没有看到。

### 三、资料搜索


[security-exception-unable-to-start-service-user-0-is-restricted](https://stackoverflow.com/questions/38764497/security-exception-unable-to-start-service-user-0-is-restricted)

回答如下：
Please see post from OPPO: http://bbs.coloros.com/thread-174655-3-1.html

Below is some translation from the post: After auto screen off for a while, the system will start battery management module, it will forbid any app start up. but there is a bug, it should force stop the app instead throw exception.

From developer side, they give a solution: use "try catch" when starting the service.

问题原因是：启动service的时候被强杀了，处理方法是在启动service的地方加 “try catch”。

### 四、分析结论

+ 问题原因：这个是OPPO机器系统的一个坑，再锁屏时启动了电池管理模块，直接禁止了拉起任何APP。
+ 处理方案：

	  	private void startServiceSafely(Intent intent) {
        try {
            ComponentName componentName = context.startService(intent);
            if (componentName == null) {
                LogUtil.w(TAG, "start service failed.");
            }else {
                LogUtil.d(TAG,"start service success.");
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Start revive service error: " + e);
        }
     	}

### 五、扩展知识

+ StartService有哪些坑
	+ 存在ComponentName componentName = context.startService(intent)返回是OK的，但是Service实际没有起来，做好容错处理。
	+ 待补充
+ BindService有哪些坑
	+ 调用了bindService如果要再次绑定，需要先unBindService,然后才可绑定
	+ onServiceConnected(ComponentName name, IBinder service)这个回调是在主线程回调的，存在绑定成功但是回调没有回来的情况。

### 六、参考资料

+ [security-exception-unable-to-start-service-user-0-is-restricted](https://stackoverflow.com/questions/38764497/security-exception-unable-to-start-service-user-0-is-restricted)


