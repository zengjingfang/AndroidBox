# 并发的解决的问题：
+ 速度

+ 设计可管理性
###更快的执行

+ 并发可以提高在单处理器上的程序性能；

+ 实现并发最直接的方式是在操作系统级别使用**进程**；
    + 周期性的将CPU从一个进程切换到另一个进程；
    + 但是JAVA使用的并发系统会共享内存和I/O这样的资源，所以必须要协调不同驱动线程任务对这些资源的使用；

###线程机制
+ 机制：切分CPU时间，子线程都能分配到一定的时间执行任务；
+ 好处：CPU的个数可以任意扩展，与实际的线程代码运行无关；

### Thread
+ 让步：Thread.yield():在run()方法中调用Thread.yield()方法是对**线程调度器**的一种建议；
    + 线程调度器：Java线程机制的一部分，可以将CPU从一个线程转移给另一个线程；
    + **暗示**可以交给别的同样优先级的线程运行；
    + 可以看到任务换进换出的证据；
+ 执行：run():导出Runnable类的时候必须要有一个run()方法，这个方法不会产生任何内在的线程能力，要实现线程行为，必须显示的将一个任务附着到线程上；
+ 优先级
    + 设置优先级：在run()方法的开头处，Thread.currentThread().setPriority(int priority) ；
    + 实际原理：提高频率（实际优先分配时间片）；
    + 常用级别:MAX_PRIORITY,NORM_PRIORITY,MIN_PRIORITY;

+ 后台线程（daemon）
    + 定义：程序在运行的过程中在后台提供的一种通用服务的线程，这种线程并不能成为程序不可获取的部分；
    + 理解：如果非后台线程死了，那么后台线程也就会被杀死；
    + 设置：在启动（start())方法之前，调用threadSymbol(实现了runnable接口的一个线程实例).setDaemon(true);
    + 判断：isDaemon();

+ 加入一个线程（join)
    + 定义：如果一个线程在另一个线程t上调用t.join()，那么次线程将被挂起，知道目标线程t结束后才恢复；
    + 理解：有A和B两个线程，如果A线程在run()方法中，调用了B.join(),意思就A线程跑在路上的时候，把B线程的任务给加进来了，那么就只能等着B跑完然后A接着跑。但是呢,如果想强制中断也可以啊。直接掉B.interrupt()方法中断掉B线程，但是记得try-catch；
    + 


# 并发使用注意事项


## 第66条：同步访问共享的可变数据

+ Synchronized可以保证在同一时刻，只有一个线程可以执行某一个方法，或者某一个代码块。
	+ 阻止一个线程看到对象处于不一致的状态之中（进不去）；
	+ 进入同步方法、块后看到同一个锁保护之前的所有修改效果（进去之后，看到的是同一个锁最后的修改）；


+ volatile修饰符不执行互斥访问，可以保证任何一个线程都将看到最近刚刚被写入的值；
	+ 安全性失败（safety failure）如果第二个线程在第一个线程读和写之间进行，会取到第一个线程修改写之前的旧值，导致错误；
		+ 建议使用 AtomicLong 等；
+ 原则：不共享可变数据，只共享不可变数据（每个读和写的操作都要执行同步），或者不共享（将可变数据限制在单个线程中）！

## 第67条：避免过度同步

+ 外来方法：在同步区域内部调用设计成要被覆盖的方法，或者以对象形势提供的方法；
+ 死锁：一个线程尝试去获取已经被锁定的对象，造成的程序阻塞；
	+ 在同步区域内部调用外来方法，容易造成死锁；
	+ 同一个线程再次重复拿锁，不会造成死锁，但是可能会有活性失败或者安全性失败；
+ 开放调用：在同步区域之外调用外来方法；
+ 应该尽量在同步区域内部做最少的工作；

##  第70条：线程安全性的文档化
+ 不可变的（immutable）：这个类的实例是不可变的，不需要外部进行同步处理，例如String,Long;
+ 无条件的线程安全（Unconditionally thread-safe):这个类的实例是可变的，但是内部有足够的同步处理，所以外部不需要进行同步。例如Random,ConcurrentHashMap;
	+ 使用私有锁对象来代替同步的方法；
+ 有条件的线程安全（Conditionally thread-safe):有些方法需要外部同步；
	+ 注明那个方法调用需要外部同步；
	+ 注明在执行这些序列的时候要获得哪把锁；
+ 非线程安全（not thread-safe):这个类的实例是可变的，外部并发使用必现要进行同步处理；

## 第71条：慎用延迟初始化

+ 对于实例域，使用双重检查模式(double-check idiom)；
+ 对于静态域，则使用 lazy initialization hodler class idiom;
+ 对于可以接受重复初始化的实例域，考虑使用单重检查模式（single-check idiom；


# 线程池基础知识

# 线程池

##简介

###线程池的创建

```
new  ThreadPoolExecutor(
int corePoolSize,
int maximumPoolSize,
long keepAliveTime,
TimeUnit milliseconds,
BlockingQueue<Runnable> runnableTaskQueue,
ThreadFactory threadFactory,
RejectedExecutionHandler handler);
```

+ corePoolSize（线程池基本大小）

这是一个判断该线程池是否要新建一个新的线程的标准值，如果当前线程池中的线程数小于corePoolSize，
则在接到新的请求任务时新建一个线程来处理，反之则把任务放到
BlockingQueue中，由线程池中空的线程从BlockingQueue中取出并处理；

+ maximumPoolSize（线程池最大大小）

线程池最大的线程数，当大于该值的时候则让RejectedExecutionHandler拒绝处理；

+ keepAliveTime
当线程池中大于corePoolSize的时候，部分多余的空线程会等待keepAliveTime时间，如果没有请求处理超过该时间则自行销毁；

+ BlockingQueue（任务队列）
保存等待任务执行的阻塞队列，有以下几种：
   + ArrayBlockingQueue:数组队列，先进先出；
   + LinkedBlockingQueue:链表队列，先进先出，吞吐量大于ArrayBlockingQueue;
   + SynchronousQueue:一个不存储元素的队列，每插入一个任务必须等到另一个线程调用移除操作，否则处于阻塞状态；吞吐量高于LinkedBlockingQueue,newCachedThreadPool使用的就是这个队列；
   + PriorityBlockingQueue:一个优先级无限阻塞的队列；

+ RejectedExecutionHandler（饱和策略）
  当线程池处于饱和状态下，对于提交的新任务必须要有一种策略来处理；

###提交任务

+ execute(new Runnable(){} )
  没有返回值

         threadsPool.execute(new Runnable() {
              @Override
              public void run() {
                  // TODO Auto-generated method stub
              }
          });
+ commit(new callable(){})  返回future对象

              Future future = mThreadPoolExecutor.submit(new Callable() {
                  @Override
                  public Object call() throws Exception {
                      return null;
                  }
              });
              --------------------------------------------------

              try {
                  Object o = future.get();//阻塞 直到结果准备就绪
              } catch (InterruptedException e) {
                  //中断异常
                  e.printStackTrace();
              } catch (ExecutionException e) {
                  //无法执行异常
                  e.printStackTrace();
              } finally {
                  //关闭线程池
                  mThreadPoolExecutor.shutdown();
              }
          }
###关闭任务
+ 原理
  遍历所有的线程，逐个调用线程的interrupt方法来中断线程;
+ shutdown
  执行shutdown后，遍历线程池中所有的线程，将状态修改为SHUTDOWN状态，然后中断**正在执行任务**的线程；
+ shutdownNow
  执行shutdownNow后，遍历线程池中所有的线程，将所有线程的状态修改为STOP状态，然后尝试停止**所有的线程任务**。

###工作原理

+ 工作流程示意图

![工作示意图](http://ww1.sinaimg.cn/large/aea705afgw1f5qupoct0ij20dw085gmc.jpg)

+ 源码分析

+ 工作线程

###合理配置线程池

+ 任务特性
    + 任务的性质：CPU密集型任务，IO密集型的任务，混合型任务；
    + 任务的优先级：高，中，低；
    + 任务的执行时间：长、中、短；
    + 任务的依赖性，是否依赖其他系统资源，比如数据库连接。

+ 配置意见

    + CPU密集型建议使用线程数尽可能少的线程池，IO密集型任务由于线程并不是一直在工作，所以建议使用线程数较多的线程池；
    + 优先级不同任务可以使用PriorityBlockingProcessors任务队列来处理，让优先级更高的来处理（！注意：如果一直是优先级搞的任务在处理，则优先级低的任务可能无法得到处理；
    + 时间不同的任务可以交给不同规模的线程池来处理，也可以交给优先级队列，让时间短的任务先执行；
    + 依赖数据库连接池的任务，由于提交SQL后需要等待返回结果，所以等待的时间越长，CPU空闲的时间越长，为了提高CPU的利用率，可以通过增大线程池的线程数。
    + 建议使用**有界队列**，这样更加稳定安全，防止撑爆内存（如果线程一直处于**阻塞**状态，新的任务来的时候就会新建新的线程，直到内存不足）。

###线程池的监控

+ 部分线程池的属性参数
    + taskCount:线程池需要执行的任务数量；
    + completedTaskCont: 已经完成的任务数量；
    + largestPoolSize: 线程池曾经创建过的最大线程数，可以看看是否大于最大线程数，是否满过；
    + getPoolSize: 线程池的线程数量，**线程池不销毁，池中的线程不会自动销毁**；
    + getAliveCount:活动状态的线程数。

+ 通过扩展线程池进行监控
    + 继承线程池；
    + 重写beforeExecute,afterExecute和terminated方法。

###常用线程池

+ FixedThreadPool 定长并发线程池
    + 源码

            public static ExecutorService newFixedThreadPool(int nThreads) {
                return new ThreadPoolExecutor(nThreads, nThreads,
                                          0L, TimeUnit.MILLISECONDS,
                                          new LinkedBlockingQueue<Runnable>());
            }
    + 特点：
    可以创建固定的线程数，当线程数达到corePoolSize的时候，再添加任务就放到LinkedBlockingQueue这个无界的队列里边，等待空闲的线程来取任务执行任务，空闲线程不会被回收；

+ SingleThreadExecutor 顺序执行线程池

    + 源码

            public static ExecutorService newSingleThreadExecutor() {
                 return new FinalizableDelegatedExecutorService
                                    (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
            }
    + 特点
    corePoolSize为1，既最多只能有一个线程工作，当其他任务来的时候都放到LinkedBlockingQueue任务队列里边，等待线程工作完后，再从队列里边取，逐个执行，相当于顺序执行；

+ CachedThreadPool “无限”容量可缓存线程池
    + 源码

            public static ExecutorService newCachedThreadPool() {
                    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
            }
    + 特点
        + SynchronousQueue是一个没有容量的阻塞队列，每个插入必须有对应的移除操作；
        + 如果没有线程去从SynchronousQueue从事移除的工作，那么就会新建一个线程来执行任务，如果一直这么下去，就可以能因为创建过多的线程耗尽CPU资源；
        + 如果空闲的线程等待时间超过60秒，而且没有新的任务，会自动被回收。
+ ScheduledThreadPool 定时线程池



##参考资料：

   + 《JAVA编程思想》
   + 《EffectiveJava》
   + JDK1.6源码
   + [聊聊并发——方腾飞](http://www.infoq.com/cn/articles/java-threadPool)
   + [Java中常见的线程池](http://blog.csdn.net/u010723709/article/details/50391948)
