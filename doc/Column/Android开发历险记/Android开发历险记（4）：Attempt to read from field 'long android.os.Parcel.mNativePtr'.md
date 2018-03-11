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


#### 三、分析结论


根据上述的源码追踪分析，极有可能是在bindservice的时候进程意外死亡或者进行binder通信时内存被回收等情况，导致了这个parcel为null，抛出了异常。处理就是在APP业务代码中bindservice操作时进行一次“try catch”,保证程序不崩溃。如果出现该情况，也就意味着bindService失败，是否要进一步处理根据环境而定。

