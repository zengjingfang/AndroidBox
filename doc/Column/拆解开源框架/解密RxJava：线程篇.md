###  一、computationScheduler  >>> EventLoopsScheduler

EventLoopsScheduler.java


	// 可以通过 System.getProperty 来配置 value 的 key
    static final String KEY_MAX_THREADS = "rx.scheduler.max-computation-threads";
   
    static final int MAX_THREADS;
    static {
        int maxThreads = Integer.getInteger(KEY_MAX_THREADS, 0);
        int cpuCount = Runtime.getRuntime().availableProcessors();
        int max;
        if (maxThreads <= 0 || maxThreads > cpuCount) {
            max = cpuCount;// 默认为CPU个数，不可超过CPU个数
        } else {
            max = maxThreads;
        }
        MAX_THREADS = max;
    }

    public EventLoopsScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.pool = new AtomicReference<FixedSchedulerPool>(NONE);
        start();
    }

    public void start() {
        // 构建FixedSchedulerPool
        FixedSchedulerPool update = new FixedSchedulerPool(threadFactory, MAX_THREADS);
        if (!pool.compareAndSet(NONE, update)) {
            update.shutdown();
        }
    }

	static final class FixedSchedulerPool {
        final int cores;

        final PoolWorker[] eventLoops;
        long n;

        FixedSchedulerPool(ThreadFactory threadFactory, int maxThreads) {
            // initialize event loops
            this.cores = maxThreads;
            this.eventLoops = new PoolWorker[maxThreads];  // 这里新建了一个线程池组
            for (int i = 0; i < maxThreads; i++) {
                // new出每个线程池
                this.eventLoops[i] = new PoolWorker(threadFactory);
            }
        }

        public PoolWorker getEventLoop() {
            int c = cores;
            if (c == 0) {
                return SHUTDOWN_WORKER;
            }
            // simple round robin, improvements to come
			// 依次从里边取线程
            return eventLoops[(int)(n++ % c)];
        }

        public void shutdown() {
            for (PoolWorker w : eventLoops) {
                w.unsubscribe();
            }
        }
    }

    static final class PoolWorker extends NewThreadWorker {
        PoolWorker(ThreadFactory threadFactory) {
            super(threadFactory);
        }
    }


NewThreadWorker.java


    public NewThreadWorker(ThreadFactory threadFactory) {

		// new一个核心线程数为1，最大线程数为Integer.MAX_VALUE，Queue 为 DelayedWorkQueue 的线程池组
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1, threadFactory);

        // Java 7+: cancelled future tasks can be removed from the executor thus avoiding memory leak
        boolean cancelSupported = tryEnableCancelPolicy(exec);
       
	   if (!cancelSupported && exec instanceof ScheduledThreadPoolExecutor) {
            registerExecutor((ScheduledThreadPoolExecutor)exec);
        }
        executor = exec;
    }


### io >>> CachedThreadScheduler

CachedThreadScheduler.java

    public CachedThreadScheduler(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        this.pool = new AtomicReference<CachedWorkerPool>(NONE);
        start();
    }

    @Override
    public void start() {
        CachedWorkerPool update =
            new CachedWorkerPool(threadFactory, KEEP_ALIVE_TIME, KEEP_ALIVE_UNIT);
        if (!pool.compareAndSet(NONE, update)) {
            update.shutdown();
        }
    }


	static final class CachedWorkerPool {
        private final ThreadFactory threadFactory;
        private final long keepAliveTime;
        private final ConcurrentLinkedQueue<ThreadWorker> expiringWorkerQueue;
        private final CompositeSubscription allWorkers;
        private final ScheduledExecutorService evictorService;
        private final Future<?> evictorTask;

        CachedWorkerPool(final ThreadFactory threadFactory, long keepAliveTime, TimeUnit unit) {
            this.threadFactory = threadFactory;
            this.keepAliveTime = unit != null ? unit.toNanos(keepAliveTime) : 0L;
            this.expiringWorkerQueue = new ConcurrentLinkedQueue<ThreadWorker>();
            this.allWorkers = new CompositeSubscription();

            ScheduledExecutorService evictor = null;
            Future<?> task = null;
            if (unit != null) {

				// 核心线程数为1，最大线程数为nteger.MAX_VALUE
                evictor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
                    @Override public Thread newThread(Runnable r) {
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

        ThreadWorker get() {
            if (allWorkers.isUnsubscribed()) {
                return SHUTDOWN_THREADWORKER;
            }
            while (!expiringWorkerQueue.isEmpty()) {
                ThreadWorker threadWorker = expiringWorkerQueue.poll();
                if (threadWorker != null) {
                    return threadWorker;
                }
            }

            // No cached worker found, so create a new one.、
			// 构建 ThreadWorker 并加入到缓存 allWorkers 中
            ThreadWorker w = new ThreadWorker(threadFactory);
            allWorkers.add(w);
            return w;
        }
	static final class ThreadWorker extends NewThreadWorker {
        private long expirationTime;

        ThreadWorker(ThreadFactory threadFactory) {
            super(threadFactory);
            this.expirationTime = 0L;
        }
	}