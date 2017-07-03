# 一、进程和线程
+ 进程：进程（process）是程序的一个运行实例；
+ 线程：线程（Thread）是CPU调度的一个基本单元；
+ Android：
	+ 四大组件只是进程中的一些“零件”；
	+ 应用程序启动后，至少创建一个主线程Activity	Thread,两个Binder线程；
	+ 同一个包中的组件将运行在相同的进程空间内；
	+ 不同的包中组件可以通过一定的方式（IPC)运行在同一个进程空间内；
# 二、Thread、Looper、Message、Hanlder直接的关系
### 1、一个线程Thread对应一个Looper
	 private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        sThreadLocal.set(new Looper(quitAllowed));
    }
	
  	private Looper(boolean quitAllowed) {
        mQueue = new MessageQueue(quitAllowed);
        mThread = Thread.currentThread();
    }

在一个线程里边Looper.prepare(),这个Looper就创建了一个MessageQueue()，同时指定Looper的线程为当前线程。

### 2、一个Looper对应一个MessageQueue


上述已足够说明

### 3、每个MessageQueue有N个Message

上述已足够说明

### 4、每个Messag最多指定一个Hanlder来处理

	public Handler(Callback callback, boolean async) {
        if (FIND_POTENTIAL_LEAKS) {
            final Class<? extends Handler> klass = getClass();
            if ((klass.isAnonymousClass() || klass.isMemberClass() || klass.isLocalClass()) &&
                    (klass.getModifiers() & Modifier.STATIC) == 0) {
                Log.w(TAG, "The following Handler class should be static or leaks might occur: " +
                    klass.getCanonicalName());
            }
        }

        mLooper = Looper.myLooper();//当前线程的mLooper
        if (mLooper == null) {
            throw new RuntimeException(
                "Can't create handler inside thread that has not called Looper.prepare()");
        }
        mQueue = mLooper.mQueue;//这个Looper的mQueue
        mCallback = callback;
        mAsynchronous = async;
    }

我们可以在一个线程Thread中new一个Hanlder，这个Hanlder会找到当前线程的mLooper，以及这个Looper的mQueue。

	 /**
     * Subclasses must implement this to receive messages.
     */
    public void handleMessage(Message msg) {
    }
	  /**
     * Handle system messages here.
     */
    public void dispatchMessage(Message msg) {
        if (msg.callback != null) {
            handleCallback(msg);
        } else {
            if (mCallback != null) {
                if (mCallback.handleMessage(msg)) {
                    return;
                }
            }
            handleMessage(msg);
        }
    }
上述为默认的分发执行Message的策略，当然也可以复写自定义。最终执行的就是handleMessage（msg）。

### Thread和Hanlder是一对多的关系

上述可知，我们可以在一个Thread里边new出多个Hanlder,这个Hanlder会找到当前的Thead公共的唯一的Looper以及这个Looper的唯一的MessageQueue,我们通过Hanlder将一些Message压入到MessageQueue里边。这个Looper会从Queue里边一个个取出Message，然后交给Hanlder来处理。

# 总结

我们可以将一个App看做一个应用程序，启动程序默认会启动一个进程，这个进程启动时就会zygoteInit启动一个主线程和两个binder线程以及我们自己new的一些子线程。其中这个主线程就是ActivityThread(Service也是寄存在ActivityThread之中的)。然后通过上述的Handler、Looper、MessageQueue，让程序活了起来。