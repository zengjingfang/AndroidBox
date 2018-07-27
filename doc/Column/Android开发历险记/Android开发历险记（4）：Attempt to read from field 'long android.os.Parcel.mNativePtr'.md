### 一、问题背景

后台崩溃异常，如下

	#Wed Feb 07 13:05:56 GMT+08:00 2018
	STACK_TRACE=java.lang.RuntimeException: Unable to bind to service com.xtc.im.core.push.PushService@33d1d88a with Intent { act=com.xtc.im.core.push.PushService flg=0x20 pkg=com.xtc.watch cmp=com.xtc.watch/com.xtc.im.core.push.PushService (has extras) }: java.lang.NullPointerException: Attempt to read from field 'long android.os.Parcel.mNativePtr' on a null object reference
	 android.app.ActivityThread.handleBindService(ActivityThread.java:2880)
	 android.app.ActivityThread.access$2000(ActivityThread.java:175)
	 android.app.ActivityThread$H.handleMessage(ActivityThread.java:1457)
	 android.os.Handler.dispatchMessage(Handler.java:102)
	 android.os.Looper.loop(Looper.java:135)
	 android.app.ActivityThread.main(ActivityThread.java:5418)
	 java.lang.reflect.Method.invoke(Native Method)
	 java.lang.reflect.Method.invoke(Method.java:372)
	 com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:1037)
	 com.android.internal.os.ZygoteInit.main(ZygoteInit.java:832)
	Caused by: java.lang.NullPointerException: Attempt to read from field 'long android.os.Parcel.mNativePtr' on a null object reference
	 android.os.Parcel.appendFrom(Parcel.java:446)
	 android.os.BaseBundle.writeToParcelInner(BaseBundle.java:1300)
	 android.os.Bundle.writeToParcel(Bundle.java:1034)
	 android.os.Parcel.writeBundle(Parcel.java:697)
	 android.content.Intent.writeToParcel(Intent.java:7734)
	 android.app.ActivityManagerProxy.publishService(ActivityManagerNative.java:3676)
	 android.app.ActivityThread.handleBindService(ActivityThread.java:2868)


### 二、源码追踪

#### 1、ActivityThread.java

    private void handleBindService(BindServiceData data) {
        Service s = mServices.get(data.token);
        if (DEBUG_SERVICE)
            Slog.v(TAG, "handleBindService s=" + s + " rebind=" + data.rebind);
        if (s != null) {
            try {
                data.intent.setExtrasClassLoader(s.getClassLoader());
                data.intent.prepareToEnterProcess();
                try {
                    if (!data.rebind) {
                        IBinder binder = s.onBind(data.intent);
                        ActivityManagerNative.getDefault().publishService(
                                data.token, data.intent, binder);
                    } else {
                        s.onRebind(data.intent);
                        ActivityManagerNative.getDefault().serviceDoneExecuting(
                                data.token, SERVICE_DONE_EXECUTING_ANON, 0, 0);
                    }
                    ensureJitEnabled();
                } catch (RemoteException ex) {
                }
            } catch (Exception e) {
                if (!mInstrumentation.onException(s, e)) {
                   // 可以观察到异常是在此处抛出
                    throw new RuntimeException(
                            "Unable to bind to service " + s
                            + " with " + data.intent + ": " + e.toString(), e);
                }
            }
        }
    }


#### 2、[Parcel.java](https://android.googlesource.com/platform/frameworks/base/+/0e2d281/core/java/android/os/Parcel.java)

	public final void appendFrom(Parcel parcel, int offset, int length) {
	// 由于 parcel.mNativePtr中 parcel为null，所以抛出了NullPointerException
	nativeAppendFrom(mNativePtr, parcel.mNativePtr, offset, length);
	}


#### 三、问题分析

出现该问题原因是在startService(intent)中的intent，使用了putExtra传输数据，但是所传的key偏长，或者说数据过大，导致系统底层出现了异常，抛出了NullPointerException，最终导致RuntimeException，所以真正的解决办法是在bindservice动作的intent里面不通过putExtra传输参数，如下注释后面两行代码。经过版本验证修改了没有出现该异常。

	    intent.setPackage(context.getPackageName());
        intent.setComponent(new ComponentName(hostPkg, PushService.class.getName()));
        intent.setAction(PushService.ACTION);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		// 去掉，验证bindService出现的异常
        //intent.putExtra(EXTRA_PLATFORM, platform);
        //intent.putExtra(EXTRA_HOST_PKG_NAME, hostPkg);
        