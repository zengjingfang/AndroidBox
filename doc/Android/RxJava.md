
#  一、使用

### 1、创建一个被观察者Observable

	 Observable<String> observable1 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onStart();
                subscriber.onNext("NNn");

            }
        });

### 2、创建一个观察者Subscriber
        Subscriber<String> stringSubscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {


            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {

            }
        };

### 3、安排观察者和被观察者的执行线程，以及两者之间的订阅关系
		
        Scheduler ioScheduler = Schedulers.io();
        Observable<String> observable2 = observable1.subscribeOn(ioScheduler);
        
        Scheduler mainScheduler = AndroidSchedulers.mainThread();
        Observable<String> observable3 = observable2.observeOn(mainScheduler);
        
        observer3.subscribe(stringSubscriber);


# 二、简单分析

#### Observable.create()

	public static <T> Observable<T> create(OnSubscribe<T> f) {
        return new Observable<T>(RxJavaHooks.onCreate(f));
    }

#### Observable.new

	final OnSubscribe<T> onSubscribe;

	protected Observable(OnSubscribe<T> f) {
		//通过构造将外部的Onsubscribe传给Observer对象
        this.onSubscribe = f;
    }

#### Observable.subscribe

	 public final Subscription subscribe(Subscriber<? super T> subscriber) {
        return Observable.subscribe(subscriber, this);
    }

#### Observale.subscribe

	static <T> Subscription subscribe(Subscriber<? super T> subscriber, Observable<T> observable) {
     // validate and proceed
        if (subscriber == null) {
            throw new IllegalArgumentException("subscriber can not be null");
        }
        if (observable.onSubscribe == null) {
            throw new IllegalStateException("onSubscribe function can not be null.");
        }

        // new Subscriber so onStart it
		// 回调了subscribe的onStart方法
        subscriber.onStart();

        // if not already wrapped
        if (!(subscriber instanceof SafeSubscriber)) {
            // assign to `observer` so we return the protected version
            subscriber = new SafeSubscriber<T>(subscriber);
        }

        // The code below is exactly the same an unsafeSubscribe but not used because it would
        // add a significant depth to already huge call stacks.
        try {
            // allow the hook to intercept and/or decorate
			// 回调了最初构造里面传入的OnSubScribe的call方法
            RxJavaHooks.onObservableStart(observable, observable.onSubscribe).call(subscriber);
            return RxJavaHooks.onObservableReturn(subscriber);
        } catch (Throwable e) {
            // special handling for certain Throwable/Error/Exception types
            Exceptions.throwIfFatal(e);
            // in case the subscriber can't listen to exceptions anymore
            if (subscriber.isUnsubscribed()) {
                RxJavaHooks.onError(RxJavaHooks.onObservableError(e));
            } else {
                // if an unhandled error occurs executing the onSubscribe we will propagate it
                try {
                    subscriber.onError(RxJavaHooks.onObservableError(e));
                } catch (Throwable e2) {
                    Exceptions.throwIfFatal(e2);
                    // if this happens it means the onError itself failed (perhaps an invalid function implementation)
                    // so we are unable to propagate the error correctly and will just throw
                    RuntimeException r = new OnErrorFailedException("Error occurred attempting to subscribe [" + e.getMessage() + "] and then again while trying to pass to onError.", e2);
                    // TODO could the hook be the cause of the error in the on error handling.
                    RxJavaHooks.onObservableError(r);
                    // TODO why aren't we throwing the hook's return value.
                    throw r; // NOPMD
                }
            }
            return Subscriptions.unsubscribed();
        }
    }

### 小结
![](https://docs.google.com/drawings/d/e/2PACX-1vT85nt1Ur9apqnVhtyZ7n7D_yk1Yc68R7km_O3fiKop3Icw6-VYD_rlJNaoXkHXhCXdv7jB1K7T6_6D/pub?w=744&h=401)

如上图：

+ 第一步：构建一个 Observable,准备好OnSubscribe实现，并内含了 OnSubscribe 的接口，这个 OnSubscribe 等待Subscriber接口传过来；

+ 第二步：构建一个Subscriber的实现;
+ 第三步：Observable 执行subscribe方法，把subscribe接口最终传给Observale的OnSubscribe，并回调call
+ 第四步：OnSubscribe的call传回了subscriber的接口，这样我们在这里调用subscriber的接口回调，在外部的subscriber做自己的实现，达成了一个订阅的关系。

> 这里设计的巧妙之处就在于，把实现都公布在外面，由我们开发者来实现。内部则吧观察者的subscriber接口，通过OnSubscribe的接口回调传到了被观察者。外部被观察者需要干什么也通过了OnSubscribe的实现来进行自己的操作。
> 
> 所以，我们可以这样来理解这其中的三个角色：
> Observable: 被观察者
> Subscriber: 观察者
> OnSubscribe： 将观察者的接口传回被观察者的帮运工


# 三、线程分析


## 1、Schedulers.io()

#### [1.1]Schedulers.io
    public static Scheduler io() {
		// 首先经过[1.2] [1.3] 准备好ioScheduler
		// 然后 [1.4] 设置
        return RxJavaHooks.onIOScheduler(getInstance().ioScheduler);
    }
#### [1.2] Schedulers.getInstance
	 private static Schedulers getInstance() {
        for (;;) {
            Schedulers current = INSTANCE.get();
            if (current != null) {
                return current;
            }
			// 为null new 一个单列
            current = new Schedulers();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            } else {
                current.shutdownInstance();
            }
        }
    }

#### [1.3]Schedulers.newInstance

 	private Schedulers() {

        @SuppressWarnings("deprecation")
        RxJavaSchedulersHook hook = RxJavaPlugins.getInstance().getSchedulersHook();
		
        // 初始化 computationScheduler
        Scheduler c = hook.getComputationScheduler();
        if (c != null) {
            computationScheduler = c;
        } else {
            computationScheduler = RxJavaSchedulersHook.createComputationScheduler();
        }
		
		// 初始化 ioScheduler
        Scheduler io = hook.getIOScheduler();
        if (io != null) {
            ioScheduler = io;
        } else {
            ioScheduler = RxJavaSchedulersHook.createIoScheduler();
        }
		
		// 初始化 newThreadScheduler
        Scheduler nt = hook.getNewThreadScheduler();
        if (nt != null) {
            newThreadScheduler = nt;
        } else {
            newThreadScheduler = RxJavaSchedulersHook.createNewThreadScheduler();
        }
    }


#### [1.4] RxJavaSchedulersHook.createIoScheduler

    public static Scheduler createIoScheduler() {
        return createIoScheduler(new RxThreadFactory("RxIoScheduler-"));
    }

#### [1.5] RxJavaSchedulersHook.createIoScheduler

    public static Scheduler createIoScheduler(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException("threadFactory == null");
        }
        return new CachedThreadScheduler(threadFactory);
    }

#### [1.6]CachedThreadScheduler.newInstance

    public CachedThreadScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.pool = new AtomicReference<CachedWorkerPool>(NONE);
		// 调用[1.7]
        start();
    }

#### [1.7]CachedThreadScheduler.start
	
	private static final long KEEP_ALIVE_TIME = 60;

    @Override
    public void start() {
        CachedWorkerPool update =
            new CachedWorkerPool(threadFactory, KEEP_ALIVE_TIME, KEEP_ALIVE_UNIT);
        if (!pool.compareAndSet(NONE, update)) {
            update.shutdown();
        }
    }

#### [1.8]CachedThreadScheduler.CachedWorkerPool

        CachedWorkerPool(final ThreadFactory threadFactory, long keepAliveTime, TimeUnit unit) {
            this.threadFactory = threadFactory;
            this.keepAliveTime = unit != null ? unit.toNanos(keepAliveTime) : 0L;
            this.expiringWorkerQueue = new ConcurrentLinkedQueue<ThreadWorker>();
            this.allWorkers = new CompositeSubscription();

            ScheduledExecutorService evictor = null;
            Future<?> task = null;
            if (unit != null) {
				// 发现是在这个地方new了一个线程池
                evictor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
                    @Override public Thread newThread(Runnable r) {
						// 这么做是为了给本线程池命名来作为标识
                        Thread thread = threadFactory.newThread(r);
                        thread.setName(thread.getName() + " (Evictor)");
                        return thread;
                    }
                });
                NewThreadWorker.tryEnableCancelPolicy(evictor);
                task = evictor.scheduleWithFixedDelay(
                        new Runnable() {
                            @Override
                            public void run() {
                                evictExpiredWorkers();
                            }
                        }, this.keepAliveTime, this.keepAliveTime, TimeUnit.NANOSECONDS
                );
            }
            evictorService = evictor;
            evictorTask = task;
        }

#### [1.9]RxJavaHooks.onIOScheduler

    public static Scheduler onIOScheduler(Scheduler scheduler) {
        Func1<Scheduler, Scheduler> f = onIOScheduler;
        if (f != null) {
			// 返回外部设置的的ioscheduler
            return f.call(scheduler);
        }
		// 返回自己
        return scheduler;
    }


### 2、subscribeOn(Schedulers.io())

#### [2.0] Observable.subscribeOn

    public final Observable<T> subscribeOn(Scheduler scheduler) {
        if (this instanceof ScalarSynchronousObservable) {
			//ScalarSynchronousObservable 特殊处理
            return ((ScalarSynchronousObservable<T>)this).scalarScheduleOn(scheduler);
        }
		// 先[2.1] new 出 OperatorSubscribeOn 对象，传入了当前 observable 和 scheduler
        return create(new OperatorSubscribeOn<T>(this, scheduler));

    }

#### [2.1] OperatorSubscribeOn.new

	// 通过构造传入了 observable 和 scheduler
    public OperatorSubscribeOn(Observable<T> source, Scheduler scheduler) {
        this.scheduler = scheduler;
        this.source = source;
    }

### [2.2]Observable.create

	// 再用 新的 OperatorSubscribeOn （实际也是个OnSubscribe ）再新new一个Observable返回
    public static <T> Observable<T> create(OnSubscribe<T> f) {
		// 
        return new Observable<T>(RxJavaHooks.onCreate(f));
    }

### [2.3]RxJavaHooks.onCreate

    public static <T> Observable.OnSubscribe<T> onCreate(Observable.OnSubscribe<T> onSubscribe) {
        Func1<Observable.OnSubscribe, Observable.OnSubscribe> f = onObservableCreate;
        if (f != null) {
			// Hook to call when an Observable is created.
            return f.call(onSubscribe);
        }
        return onSubscribe;
    }

+ 构造了一个OperatorSubscribeOn

+ 这个 OperatorSubscribeOn 包含了原始构建好的 observable 和 scheduler

+ 重新构造了一个新的 observable，内含了 OperatorSubscribeOn

+ 返回新的observable

### 3、AndroidSchedulers.mainThread()

#### [3.1]AndroidSchedulers.mainThread
    public static Scheduler mainThread() {
        return getInstance().mainThreadScheduler;
    }
#### [3.2]AndroidSchedulers.getInstance
    private static AndroidSchedulers getInstance() {
        for (;;) {
            AndroidSchedulers current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new AndroidSchedulers();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }
### [3.3]AndroidSchedulers.newInstance
    private AndroidSchedulers() {
        RxAndroidSchedulersHook hook = RxAndroidPlugins.getInstance().getSchedulersHook();

        Scheduler main = hook.getMainThreadScheduler();
        if (main != null) {
            mainThreadScheduler = main;
        } else {
            mainThreadScheduler = new LooperScheduler(Looper.getMainLooper());
        }
    }


### 4、observeOn(AndroidSchedulers.mainThread())


#### [4.1]Observable.observeOn
    public final Observable<T> observeOn(Scheduler scheduler) {
        return observeOn(scheduler, RxRingBuffer.SIZE);
    }

#### [4.2]Observable.observeOn
    public final Observable<T> observeOn(Scheduler scheduler, int bufferSize) {
        return observeOn(scheduler, false, bufferSize);
    }

#### [4.3]observeOn.observeOn
    public final Observable<T> observeOn(Scheduler scheduler, boolean delayError, int bufferSize) {
        if (this instanceof ScalarSynchronousObservable) {
            return ((ScalarSynchronousObservable<T>)this).scalarScheduleOn(scheduler);
        }
        return lift(new OperatorObserveOn<T>(scheduler, delayError, bufferSize));
    }

#### [4.4]

    public final <R> Observable<R> lift(final Operator<? extends R, ? super T> operator) {
		// 这里要注意了  onSubscribe 是当前observable 的 onSubscribe ,也就是前面构建出来的那个 OperatorSubscribeOn 
        return create(new OnSubscribeLift<T, R>(onSubscribe, operator));
    }

#### [4.5] OnSubscribeLift

    public OnSubscribeLift(OnSubscribe<T> parent, Operator<? extends R, ? super T> operator) {
        this.parent = parent;
        this.operator = operator;
    }

	@Override
    public void call(Subscriber<? super R> o) {
        try {
            Subscriber<? super T> st = RxJavaHooks.onObservableLift(operator).call(o);
            try {
                // new Subscriber created and being subscribed with so 'onStart' it
                st.onStart();
                parent.call(st);
            } catch (Throwable e) {
                // localized capture of errors rather than it skipping all operators
                // and ending up in the try/catch of the subscribe method which then
                // prevents onErrorResumeNext and other similar approaches to error handling
                Exceptions.throwIfFatal(e);
                st.onError(e);
            }
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // if the lift function failed all we can do is pass the error to the final Subscriber
            // as we don't have the operator available to us
            o.onError(e);
        }
    }

#### [4.6]Observable.create 

	// 同 [2.2]
    public static <T> Observable<T> create(OnSubscribe<T> f) {
        return new Observable<T>(RxJavaHooks.onCreate(f));
    }


+ 构建一个新的 OperatorObserveOn，内含了 AndroidSchedulers.mainThread 的scheduler
+ 构建一个新的 OnSubscribeLift（实现 OnSubscribe 接口），内含上面3中的OperatorSubscribeOn  和上面的 OperatorObserveOn
+ 构建一个新的 Observable ，他的 OnSubscribe 就是上面的 OnSubscribeLift


### 

#### [5.1]Observale3.subscribe

	static <T> Subscription subscribe(Subscriber<? super T> subscriber, Observable<T> observable) {
     // validate and proceed
        if (subscriber == null) {
            throw new IllegalArgumentException("subscriber can not be null");
        }
        if (observable.onSubscribe == null) {
            throw new IllegalStateException("onSubscribe function can not be null.");
        }

        // new Subscriber so onStart it
		// 回调了subscribe的onStart方法
        subscriber.onStart();

        // if not already wrapped
        if (!(subscriber instanceof SafeSubscriber)) {
            // assign to `observer` so we return the protected version
            subscriber = new SafeSubscriber<T>(subscriber);
        }

        // The code below is exactly the same an unsafeSubscribe but not used because it would
        // add a significant depth to already huge call stacks.
        try {
            // allow the hook to intercept and/or decorate
			// 回调了最初构造里面传入的OnSubScribe的call方法
            RxJavaHooks.onObservableStart(observable, observable.onSubscribe).call(subscriber);
            return RxJavaHooks.onObservableReturn(subscriber);
        } catch (Throwable e) {
            // special handling for certain Throwable/Error/Exception types
            Exceptions.throwIfFatal(e);
            // in case the subscriber can't listen to exceptions anymore
            if (subscriber.isUnsubscribed()) {
                RxJavaHooks.onError(RxJavaHooks.onObservableError(e));
            } else {
                // if an unhandled error occurs executing the onSubscribe we will propagate it
                try {
                    subscriber.onError(RxJavaHooks.onObservableError(e));
                } catch (Throwable e2) {
                    Exceptions.throwIfFatal(e2);
                    // if this happens it means the onError itself failed (perhaps an invalid function implementation)
                    // so we are unable to propagate the error correctly and will just throw
                    RuntimeException r = new OnErrorFailedException("Error occurred attempting to subscribe [" + e.getMessage() + "] and then again while trying to pass to onError.", e2);
                    // TODO could the hook be the cause of the error in the on error handling.
                    RxJavaHooks.onObservableError(r);
                    // TODO why aren't we throwing the hook's return value.
                    throw r; // NOPMD
                }
            }
            return Subscriptions.unsubscribed();
        }
    }


#### [5.2]OnSubscribeLift.call
	//由[4.4]可知
    // operator = OperatorObserveOn
	// parent   = OperatorSubscribeOn
    @Override
    public void call(Subscriber<? super R> o) {
        try {
			// [5.3]OperatorObserveOn 的 call回调，这里就是把subscriber 先给到 OperatorObserverOn
			// 如果没有被hook，返回的还是OperatorObserverOn 这个 subscriber 
            Subscriber<? super T> st = RxJavaHooks.onObservableLift(operator).call(o);
			
            try {
                // new Subscriber created and being subscribed with so 'onStart' it
                st.onStart();
				// [5.4]OperatorObserveOn 在这里call回调,然后把感刚刚这个新的subscriber回调过去
                parent.call(st);
            } catch (Throwable e) {
                // localized capture of errors rather than it skipping all operators
                // and ending up in the try/catch of the subscribe method which then
                // prevents onErrorResumeNext and other similar approaches to error handling
                Exceptions.throwIfFatal(e);
                st.onError(e);
            }
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            // if the lift function failed all we can do is pass the error to the final Subscriber
            // as we don't have the operator available to us
            o.onError(e);
        }
    }


### [5.3]OperatorObserveOn.call

	// scheduler = mainScheduler
    // child = subscriber 
	@Override
    public Subscriber<? super T> call(Subscriber<? super T> child) {
        if (scheduler instanceof ImmediateScheduler) {
            // avoid overhead, execute directly
            return child;
        } else if (scheduler instanceof TrampolineScheduler) {
            // avoid overhead, execute directly
            return child;
        } else {
			// 将外部的subscriber 以及准备好的 mainScheduler 包装生成一个新的Subscriber
            ObserveOnSubscriber<T> parent = new ObserveOnSubscriber<T>(scheduler, child, delayError, bufferSize);
            parent.init();
            return parent;
        }
    }


### [5.3.1] ObserveOnSubscriber.new

	public ObserveOnSubscriber(Scheduler scheduler, Subscriber<? super T> child, boolean delayError, int bufferSize) {
            this.child = child;
            this.recursiveScheduler = scheduler.createWorker();
            this.delayError = delayError;
            this.on = NotificationLite.instance();
            int calculatedSize = (bufferSize > 0) ? bufferSize : RxRingBuffer.SIZE;
            // this formula calculates the 75% of the bufferSize, rounded up to the next integer
            this.limit = calculatedSize - (calculatedSize >> 2);
            if (UnsafeAccess.isUnsafeAvailable()) {
                queue = new SpscArrayQueue<Object>(calculatedSize);
            } else {
                queue = new SpscAtomicArrayQueue<Object>(calculatedSize);
            }
            // signal that this is an async operator capable of receiving this many
            request(calculatedSize);
        }

### [5.3.2]ObserveOnSubscriber.next
	
		// 来看看这个ObserveOnSubscriber.next的实现
	    @Override
        public void onNext(final T t) {
            if (isUnsubscribed() || finished) {
                return;
            }
            if (!queue.offer(on.next(t))) {
                onError(new MissingBackpressureException());
                return;
            }
			// 调用这个方法
            schedule();
        }

### [5.3.3]ObserveOnSubscriber.next
		
		// recursiveScheduler = mainSchedule
	    protected void schedule() {
            if (counter.getAndIncrement() == 0) {
				//这样，就在开始准备好的mainSchedule上跑
                recursiveScheduler.schedule(this);
            }
        }
#### [5.4]OperatorSubscribeOn.call
	
	// scheduler = ioScheduler
	// subscriber = ObserveOnSubscriber
	// source = Observable(1)
 	@Override
    public void call(final Subscriber<? super T> subscriber) {
        final Worker inner = scheduler.createWorker();
        subscriber.add(inner);

        inner.schedule(new Action0() {
            @Override
            public void call() {
                final Thread t = Thread.currentThread();

                Subscriber<T> s = new Subscriber<T>(subscriber) {
                    @Override
                    public void onNext(T t) {
						// onNext事件
                        subscriber.onNext(t);
                    }

                    @Override
                    public void onError(Throwable e) {
                        try {
                            subscriber.onError(e);
                        } finally {
                            inner.unsubscribe();
                        }
                    }

                    @Override
                    public void onCompleted() {
                        try {
                            subscriber.onCompleted();
                        } finally {
                            inner.unsubscribe();
                        }
                    }

                    @Override
                    public void setProducer(final Producer p) {
                        subscriber.setProducer(new Producer() {
                            @Override
                            public void request(final long n) {
                                if (t == Thread.currentThread()) {
                                    p.request(n);
                                } else {
                                    inner.schedule(new Action0() {
                                        @Override
                                        public void call() {
                                            p.request(n);
                                        }
                                    });
                                }
                            }
                        });
                    }
                };
				// source = Observable(1) 
				// s 是跑在ioScheduler上的subscriber
				// 看重点，看重点，终于call到内部去了
                source.unsafeSubscribe(s);
            }
        });
    }

#### [5.5]Observable.unsafeSubscribe

    public final Subscription unsafeSubscribe(Subscriber<? super T> subscriber) {
        try {
            // new Subscriber so onStart it
            subscriber.onStart();
            // allow the hook to intercept and/or decorate
			// 终于回到这个地方，调用了onSubscribe的call
            RxJavaHooks.onObservableStart(this, onSubscribe).call(subscriber);
            return RxJavaHooks.onObservableReturn(subscriber);
        } catch (Throwable e) {
            // special handling for certain Throwable/Error/Exception types
            Exceptions.throwIfFatal(e);
            // if an unhandled error occurs executing the onSubscribe we will propagate it
            try {
                subscriber.onError(RxJavaHooks.onObservableError(e));
            } catch (Throwable e2) {
                Exceptions.throwIfFatal(e2);
                // if this happens it means the onError itself failed (perhaps an invalid function implementation)
                // so we are unable to propagate the error correctly and will just throw
                RuntimeException r = new OnErrorFailedException("Error occurred attempting to subscribe [" + e.getMessage() + "] and then again while trying to pass to onError.", e2);
                // TODO could the hook be the cause of the error in the on error handling.
                RxJavaHooks.onObservableError(r);
                // TODO why aren't we throwing the hook's return value.
                throw r; // NOPMD
            }
            return Subscriptions.unsubscribed();
        }
    }


# 图解

![](https://docs.google.com/drawings/d/e/2PACX-1vSg-A3VUPZzJJQKRQ4PBIkla_H3pcuoQndfcTYHOLWYMcOkXri5FPYRO5BhWo6KRicKhjwzei8a6ogV/pub?w=959&h=818)

		
        Scheduler ioScheduler = Schedulers.io();
        Observable<String> observable2 = observable1.subscribeOn(ioScheduler);
        
        Scheduler mainScheduler = AndroidSchedulers.mainThread();
        Observable<String> observable3 = observer2.observeOn(mainScheduler);
        
        observable3.subscribe(subscriber);

如上图以及代码：

### 一、准备好 observable和subscriber,把自己放到对应的Scheduler上

+ 1、构建出：observable1
+ 2、构建出：ioScheduler
+ 3、构建出：observer2= observable1.subscribeOn(ioScheduler)
	+ 构建出OperatorSubscribeOn,包含了 observale1和ioSchduler
	+ 构建Observable2,并把上一步中的OperatorSubscribeOn作为自己的OnSubscribe
+ 4、构建出：mainScheduler
+ 5、构建出：observable3 = observer2.observeOn(mainScheduler)
	+ 构建出：OperatorObserveOn,内部包含了mainScheduler
	+ 构建出：OnSubscribeLift（实现 OnSubscribe 接口），内含上面3中的OperatorSubscribeOn 和上面的 OperatorObserveOn
	+ 构建出：Observable3，他的 OnSubscribe 就是上面的 OnSubscribeLift

### 二、订阅

+ 6、传入subscriber,observable3.subscribe(subscriber)
	+ OnSubscribeLift.call(subscriber)
	+ 构建出：ObserveOnSubscriber=OperatorObserveOn.call(subscriber),在mainSchedule跑
	+ OperatorSubscribeOn.call(ObserveOnSubscriber)
	+ 构建出：s = new Subscriber<T>(subscriber)，在ioSchedule上跑
	+ 最后：source.unsafeSubscribe(s)，这里的source就是最初的 observable1
	+ 最最后：observable1.call(s)

### 三、执行

+ 7、调用 s.next()
	+ 外部调用s的接口
	+ s的实现会在ioSchdule上跑，代码见[5.4]
	+ 然后转为ObserveOnSubscriber.next,代码见[5.4]
	+ ObserveOnSubscriber 这个被安排在mainSchedule上实现
	+ ObserveOnSubscriber 这个被安排在ioSchedule上跑
	

# 总结

其实，核心还是怎么发Subscriber的接口传递到Observable,然后让他回调回来。最终还是个回调。


# 线程

#### CachedThreadScheduler

    @Override
    public Worker createWorker() {
		// 用io的线程池
        return new EventLoopWorker(pool.get());
    }


#### CachedThreadScheduler.EventLoopWorker

	static final class EventLoopWorker extends Scheduler.Worker implements Action0 {
        private final CompositeSubscription innerSubscription = new CompositeSubscription();
        private final CachedWorkerPool pool;
        private final ThreadWorker threadWorker;
        final AtomicBoolean once;

        EventLoopWorker(CachedWorkerPool pool) {
            this.pool = pool;
            this.once = new AtomicBoolean();
            this.threadWorker = pool.get();
        }

        @Override
        public void unsubscribe() {
            if (once.compareAndSet(false, true)) {
                // unsubscribe should be idempotent, so only do this once

                // Release the worker _after_ the previous action (if any) has completed
                threadWorker.schedule(this);
            }
            innerSubscription.unsubscribe();
        }

        @Override
        public void call() {
            pool.release(threadWorker);
        }

        @Override
        public boolean isUnsubscribed() {
            return innerSubscription.isUnsubscribed();
        }

        @Override
        public Subscription schedule(Action0 action) {
            return schedule(action, 0, null);
        }

        @Override
        public Subscription schedule(final Action0 action, long delayTime, TimeUnit unit) {
            if (innerSubscription.isUnsubscribed()) {
                // don't schedule, we are unsubscribed
                return Subscriptions.unsubscribed();
            }

            ScheduledAction s = threadWorker.scheduleActual(new Action0() {
                @Override
                public void call() {
                    if (isUnsubscribed()) {
                        return;
                    }
					// 最后回调回去了
                    action.call();
                }
            }, delayTime, unit);
            innerSubscription.add(s);
            s.addParent(innerSubscription);
            return s;
        }
    }
