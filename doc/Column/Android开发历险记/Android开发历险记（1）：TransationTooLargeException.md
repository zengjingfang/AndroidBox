### 一、问题背景

发现APP在启动时随机出现TransactionTooLargeException,但是查看APP的业务代码通过getPushInfo的

	com.xtc.watch W/IM-Core-IMInternal: {IMInternal$1.onFailure-78} 
                                                                   
    android.os.TransactionTooLargeException
		                                                                      
		at android.os.BinderProxy.transactNative(Native Method)
		                                                                       
		at android.os.BinderProxy.transact(Binder.java:504)
		                                                                      
		at com.xtc.im.core.push.IPushService$Stub$Proxy.getPushInfo(IPushService.java:258)
		                                                                       
		at com.xtc.im.core.app.bridge.AidlInterceptor.getPushInfo(AidlInterceptor.java:196)
		                                                                       
		at com.xtc.im.core.app.bridge.AidlInterceptor.dispatcherRequest(AidlInterceptor.java:134)
		                                                                       
		at com.xtc.im.core.app.bridge.AidlInterceptor.intercept(AidlInterceptor.java:77)
		                                                                       
		at com.xtc.im.core.common.task.RealInterceptorChain.proceed(RealInterceptorChain.java:40)
		                                                                       
		at com.xtc.im.core.app.bridge.AidlRealCall.getResponseWithInterceptorChain(AidlRealCall.java:54)
		                                                                       
		at com.xtc.im.core.common.task.RealCall$AsyncCall.run(RealCall.java:115)
		                                                                       
		at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1112)
		                                                                       
		at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:587)
		                                                                       
		at java.lang.Thread.run(Thread.java:818)


### 二、源码跟踪


#####1、[Binder.java](https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/os/Binder.java)

	public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
	        Binder.checkParcel(this, code, data, "Unreasonably large binder buffer");
	        if (mWarnOnBlocking && ((flags & FLAG_ONEWAY) == 0)) {
	            // For now, avoid spamming the log by disabling after we've logged
	            // about this interface at least once
	            mWarnOnBlocking = false;
	            Log.w(Binder.TAG, "Outgoing transactions from this process must be FLAG_ONEWAY",
	                    new Throwable());
	        }
	        final boolean tracingEnabled = Binder.isTracingEnabled();
	        if (tracingEnabled) {
	            final Throwable tr = new Throwable();
	            Binder.getTransactionTracker().addTrace(tr);
	            StackTraceElement stackTraceElement = tr.getStackTrace()[1];
	            Trace.traceBegin(Trace.TRACE_TAG_ALWAYS,
	                    stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName());
	        }
	        try {
	            return transactNative(code, data, reply, flags);
	        } finally {
	            if (tracingEnabled) {
	                Trace.traceEnd(Trace.TRACE_TAG_ALWAYS);
	            }
	        }
	    }


	public static final boolean CHECK_PARCEL_SIZE = false;
 
	static void checkParcel(IBinder obj, int code, Parcel parcel, String msg) {
	        if (CHECK_PARCEL_SIZE && parcel.dataSize() >= 800*1024) {
	            // Trying to send > 800k, this is way too much
	            StringBuilder sb = new StringBuilder();
	            sb.append(msg);
	            sb.append(": on ");
	            sb.append(obj);
	            sb.append(" calling ");
	            sb.append(code);
	            sb.append(" size ");
	            sb.append(parcel.dataSize());
	            sb.append(" (data: ");
	            parcel.setDataPosition(0);
	            sb.append(parcel.readInt());
	            sb.append(", ");
	            sb.append(parcel.readInt());
	            sb.append(", ");
	            sb.append(parcel.readInt());
	            sb.append(")");
	            Slog.wtfStack(TAG, sb.toString());
	        }
	    }

	public native boolean transactNative(int code, Parcel data, Parcel reply,
            int flags) throws RemoteException;


#### 2、[core / jni / android_ util_ Binder.cpp](https://android.googlesource.com/platform/frameworks/base/+/master/core/jni/android_util_Binder.cpp)

		static const JNINativeMethod gBinderProxyMethods[] = {
		     /* name, signature, funcPtr */
		    {"pingBinder",          "()Z", (void*)android_os_BinderProxy_pingBinder},
		    {"isBinderAlive",       "()Z", (void*)android_os_BinderProxy_isBinderAlive},
		    {"getInterfaceDescriptor", "()Ljava/lang/String;", (void*)android_os_BinderProxy_getInterfaceDescriptor},
		    {"transactNative",      "(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z", (void*)android_os_BinderProxy_transact},
		    {"linkToDeath",         "(Landroid/os/IBinder$DeathRecipient;I)V", (void*)android_os_BinderProxy_linkToDeath},
		    {"unlinkToDeath",       "(Landroid/os/IBinder$DeathRecipient;I)Z", (void*)android_os_BinderProxy_unlinkToDeath},
		    {"getNativeFinalizer",  "()J", (void*)android_os_BinderProxy_getNativeFinalizer},
		};

####
		static jboolean android_os_BinderProxy_transact(JNIEnv* env, jobject obj,
		        jint code, jobject dataObj, jobject replyObj, jint flags) // throws RemoteException
		{
		    if (dataObj == NULL) {
		        jniThrowNullPointerException(env, NULL);
		        return JNI_FALSE;
		    }
		    Parcel* data = parcelForJavaObject(env, dataObj);
		    if (data == NULL) {
		        return JNI_FALSE;
		    }
		    Parcel* reply = parcelForJavaObject(env, replyObj);
		    if (reply == NULL && replyObj != NULL) {
		        return JNI_FALSE;
		    }
		    IBinder* target = getBPNativeData(env, obj)->mObject.get();
		    if (target == NULL) {
		        jniThrowException(env, "java/lang/IllegalStateException", "Binder has been finalized!");
		        return JNI_FALSE;
		    }
		    ALOGV("Java code calling transact on %p in Java object %p with code %" PRId32 "\n",
		            target, obj, code);
		    bool time_binder_calls;
		    int64_t start_millis;
		    if (kEnableBinderSample) {
		        // Only log the binder call duration for things on the Java-level main thread.
		        // But if we don't
		        time_binder_calls = should_time_binder_calls();
		        if (time_binder_calls) {
		            start_millis = uptimeMillis();
		        }
		    }
		    //printf("Transact from Java code to %p sending: ", target); data->print();
		   // ！！！ 此处调用的 transact 调用binder驱动进行数据传输，底层不再研究了。1M的限制就在此处。
		    status_t err = target->transact(code, *data, reply, flags);

		    //if (reply) printf("Transact from Java code to %p received: ", target); reply->print();
		    if (kEnableBinderSample) {
		        if (time_binder_calls) {
		            conditionally_log_binder_call(start_millis, target, code);
		        }
		    }
		    if (err == NO_ERROR) {
		        return JNI_TRUE;
		    } else if (err == UNKNOWN_TRANSACTION) {
		        return JNI_FALSE;
		    }
		    signalExceptionForError(env, obj, err, true /*canThrowRemoteException*/, data->dataSize());
		    return JNI_FALSE;
		}



		void signalExceptionForError(JNIEnv* env, jobject obj, status_t err,
		        bool canThrowRemoteException, int parcelSize)
		{
		    switch (err) {
		        // 省略部分代码
		        case DEAD_OBJECT:
		            // DeadObjectException is a checked exception, only throw from certain methods.
		            jniThrowException(env, canThrowRemoteException
		                    ? "android/os/DeadObjectException"
		                            : "java/lang/RuntimeException", NULL);
		            break;
		        case UNKNOWN_TRANSACTION:
		            jniThrowException(env, "java/lang/RuntimeException", "Unknown transaction code");
		            break;
		        case FAILED_TRANSACTION: {
		            ALOGE("!!! FAILED BINDER TRANSACTION !!!  (parcel size = %d)", parcelSize);
		            const char* exceptionToThrow;
		            char msg[128];
		            // TransactionTooLargeException is a checked exception, only throw from certain methods.
		            // FIXME: Transaction too large is the most common reason for FAILED_TRANSACTION
		            //        but it is not the only one.  The Binder driver can return BR_FAILED_REPLY
		            //        for other reasons also, such as if the transaction is malformed or
		            //        refers to an FD that has been closed.  We should change the driver
		            //        to enable us to distinguish these cases in the future.
		           //  200K 的限制
		            if (canThrowRemoteException && parcelSize > 200*1024) {
		                // bona fide large payload
		                exceptionToThrow = "android/os/TransactionTooLargeException";
		                snprintf(msg, sizeof(msg)-1, "data parcel size %d bytes", parcelSize);
		            } else {
		                // Heuristic: a payload smaller than this threshold "shouldn't" be too
		                // big, so it's probably some other, more subtle problem.  In practice
		                // it seems to always mean that the remote process died while the binder
		                // transaction was already in flight.
		                exceptionToThrow = (canThrowRemoteException)
		                        ? "android/os/DeadObjectException"
		                        : "java/lang/RuntimeException";
		                snprintf(msg, sizeof(msg)-1,
		                        "Transaction failed on small parcel; remote process probably died");
		            }
		            jniThrowException(env, exceptionToThrow, msg);
		        } break;
		        case FDS_NOT_ALLOWED:
		            jniThrowException(env, "java/lang/RuntimeException",
		                    "Not allowed to write file descriptors here");
		            break;
		      // 省略部分代码
		    }
		}
		}


> 最终最后代码发现

+  200K的限制是5.0以上版本增加的
+  FIXME注释：
TransactionTooLargeException is a checked exception, only throw from certain methods.
 FIXME: Transaction too large is the most common reason for FAILED_TRANSACTION
but it is not the only one.  The Binder driver can return BR_FAILED_REPLY
for other reasons also, such as if the transaction is malformed or
 refers to an FD that has been closed.  We should change the driver
  to enable us to distinguish these cases in the future.
+ 1 M 的限制还是在更底层的地方

 		status_t err = target->transact(code, *data, reply, flags)
	Binder方式在进程间交换数据时，有一个 transation 的缓冲区，这个是一个进程所共有的，大约1M左右。超出就会抛出异常。

### 三、搜索资料

When you get this exception in your application, please analyze your code.

1. Are you exchanging lot of data between your services and application?
2. Using intents to share huge data, (for example, the user selects huge number of files 
from gallery share press share, the URIs of the selected files will be transferred using intents)
3. receiving bitmap files from service
4. waiting for android to respond back with huge data (for example, getInstalledApplications() 
when the user installed lot of applications)
5. using applyBatch() with lot of operations pending


The Binder transaction buffer has a limited fixed size, currently 1Mb, which is shared by all transactions in progress for the process. Consequently this exception can be thrown when there are many transactions in progress even when most of the individual transactions are of moderate size.

### 四、分析结论

+ 问题原因

	+ 初始化Push进程时会异步通过AIDL接口获取getPushInfo()，大小不可能超出1M;
	+ 但是初始化时会遍历所有的APP的ApplicationInfo，这里通过binder传递data.
Android的binder机制要求同一个应用binder传递的大小不能超过1M,这里虽然掉用的单个getPushInfo不会很大，但是可能因为总和超过1M而导致的抛出TransactionTooLargeException。


	        try {
	            metaData = packageManager.getApplicationInfo(pkgName, PackageManager.GET_META_DATA).metaData;
	        } catch (PackageManager.NameNotFoundException e) {
	            LogUtil.w(TAG, e);
	        }

+ 问题解决方案

针对手机来说，APP不需要遍历所有的APP的meteData，所以仅获取自己的APP的就好。

+ 这个问题应该如何避免
	+ 有跨进程传输的数据不可过大
	+ 调用系统接口应该慎重，尤其是packageManager.getApplicationInfo（），packageManager.getPackageInfo（），切记不要在循环里面调用

### 五、扩展知识

+ PackageInfo 和 ApplicationInfo 的区别
	+ PackageInfo contains ApplicationInfo (PackageInfo.applicationInfo).
	+ Package info is all your info from your manifest file, ApplicaitonInfo is the info from the <application> tag in your manifest.
	+ 所以，特别注意，ApplicationInfo如果已经满足要求就用这个，传输的数据小，消耗更小。
+ 合理使用getPackageManager().getPackageInfo(packageName,
            flag)后面这个flag参数
    + flag=GET_ACTIVITIES时，5.0版本存在一个坑，7.0后修改。数据上存在ApplicationInfo的冗余，导致传输的数据过大，所以慎用。


### 六、参考资料

+ [Android TransactionTooLargeException 解析，思考与监控方案](
http://blog.csdn.net/self_study/article/details/60136277)
+ [TransactionTooLargeException官方文档](https://developer.android.com/reference/android/os/TransactionTooLargeException.html)
+ [difference-between-applicationinfo-and-packageinfo](https://stackoverflow.com/questions/11409669/difference-between-applicationinfo-and-packageinfo)
+ [ 由一条TransactionTooLargeException看binder传输过程中数据冗余的坑](http://blog.csdn.net/gqlovelj/article/details/79386334)