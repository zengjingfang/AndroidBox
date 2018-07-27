### 安装

### 启动观察

BaseActivity.java

	@Override
    protected void onDestroy() {
        RefWatcher refWatcher = LeakCanaryWatcher.getRefWatcher();
        if (refWatcher != null) {
            refWatcher.watch(this);
        }
    }

RefWatch.java

  	public void watch(Object watchedReference, String referenceName) {
    	if (this == DISABLED) {
     	 return;
    	}
    	checkNotNull(watchedReference, "watchedReference");
    	checkNotNull(referenceName, "referenceName");
    	final long watchStartNanoTime = System.nanoTime();
    	String key = UUID.randomUUID().toString();
    	retainedKeys.add(key);
    	final KeyedWeakReference reference =
        	new KeyedWeakReference(watchedReference, key, referenceName, queue);

    	ensureGoneAsync(watchStartNanoTime, reference);
  	}

  	private void ensureGoneAsync(final long watchStartNanoTime, final KeyedWeakReference reference) {
    	watchExecutor.execute(new Retryable() {
      	@Override public Retryable.Result run() {
        	return ensureGone(reference, watchStartNanoTime);
      		}
    	});
  	}

   Retryable.Result ensureGone(final KeyedWeakReference reference, final long watchStartNanoTime) {
    long gcStartNanoTime = System.nanoTime();
    long watchDurationMs = NANOSECONDS.toMillis(gcStartNanoTime - watchStartNanoTime);

    removeWeaklyReachableReferences();

    if (debuggerControl.isDebuggerAttached()) {
      // The debugger can create false leaks.
      return RETRY;
    }
    if (gone(reference)) {
      return DONE;
    }
    // GC一下
    gcTrigger.runGc();
    // 去掉弱引用
    removeWeaklyReachableReferences();
    if (!gone(reference)) {
      long startDumpHeap = System.nanoTime();
      long gcDurationMs = NANOSECONDS.toMillis(startDumpHeap - gcStartNanoTime);
      // 获得dumheap文件
      File heapDumpFile = heapDumper.dumpHeap();
      if (heapDumpFile == RETRY_LATER) {
        // Could not dump the heap.
        return RETRY;
      }
      long heapDumpDurationMs = NANOSECONDS.toMillis(System.nanoTime() - startDumpHeap);
      // 分析dumpHeap文件
	  heapdumpListener.analyze(
          new HeapDump(heapDumpFile, reference.key, reference.name, excludedRefs, watchDurationMs,
              gcDurationMs, heapDumpDurationMs));
    }
    return DONE;
  }

