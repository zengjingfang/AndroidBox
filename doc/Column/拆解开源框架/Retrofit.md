# 一、初始化

###  1、MyHttp
	
	//外部创建一个Retrofit对象
    private Retrofit createRetrofit(String baseUrl, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
				// 添加自己的 请求 CallAdapter
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
				// 添加自己的 响应数据转换 ConverterFactory
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .baseUrl(baseUrl)
				.client(okHttpClient)
                .build();
    }
> Build设计模式
### 2、Retrofit.build

	 public Retrofit build() {
      if (baseUrl == null) {
        throw new IllegalStateException("Base URL required.");
      }

      okhttp3.Call.Factory callFactory = this.callFactory;
      if (callFactory == null) {
        callFactory = new OkHttpClient();
      }

      Executor callbackExecutor = this.callbackExecutor;
      if (callbackExecutor == null) {
        callbackExecutor = platform.defaultCallbackExecutor();
      }
	  // 准备CallAdapter.Factory
      // Make a defensive copy of the adapters and add the default Call adapter.
      List<CallAdapter.Factory> adapterFactories = new ArrayList<>(this.adapterFactories);
      adapterFactories.add(platform.defaultCallAdapterFactory(callbackExecutor));
	  // 准备Converter.Factory
      // Make a defensive copy of the converters.
      List<Converter.Factory> converterFactories = new ArrayList<>(this.converterFactories);
	  
      return new Retrofit(callFactory, baseUrl, converterFactories, adapterFactories,
          callbackExecutor, validateEagerly);
    }
# 二、使用
###
	public Observable<List<Account>> getAccountById(String accountId) {
        AccountHttpService accountHttpService = retrofit.create(AccountHttpService.class);
        Observable<HttpResponse<List<Account>>> observable = accountHttpService.getAccountById(mobileId);
        return observable.map(new HttpRxJavaCallback<List<Account>>())
                .subscribeOn(Schedulers.io());
    }

### Retrofit.create

	// 核心方法
	public <T> T create(final Class<T> service) {
    Utils.validateServiceInterface(service);
    if (validateEagerly) {
      eagerlyValidateMethods(service);
    }
	// 这里通过动态代理的技术生成接口的实现类
    return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
        new InvocationHandler() {
          private final Platform platform = Platform.get();

          @Override public Object invoke(Object proxy, Method method, Object... args)
              throws Throwable {
            // If the method is a method from Object then defer to normal invocation.
            if (method.getDeclaringClass() == Object.class) {
              return method.invoke(this, args);
            }
            if (platform.isDefaultMethod(method)) {
              return platform.invokeDefaultMethod(method, service, proxy, args);
            }
			// 1、构造ServiceMethod  这里解析了方法、注解等，最后实际成了一个请求方法
            ServiceMethod serviceMethod = loadServiceMethod(method);
			// 2、构造OkHttpCall  最后封装了okhttp
            OkHttpCall okHttpCall = new OkHttpCall<>(serviceMethod, args);
			// 3、对应的Adapter处理请求  用对应的callAdapter 执行了请求
            return serviceMethod.callAdapter.adapt(okHttpCall);
          }
        });
  }
### 1、构造ServiceMethod
###  Retrofit.loadServiceMethod

	ServiceMethod loadServiceMethod(Method method) {
    ServiceMethod result;
    synchronized (serviceMethodCache) {
      result = serviceMethodCache.get(method);
      if (result == null) {
        result = new ServiceMethod.Builder(this, method).build();
        serviceMethodCache.put(method, result);
      }
    }
    return result;
  }

### ServiceMethod.build

	public ServiceMethod build() {
	  // 创建 CallAdapter
      callAdapter = createCallAdapter();
      responseType = callAdapter.responseType();
      if (responseType == Response.class || responseType == okhttp3.Response.class) {
        throw methodError("'"
            + Utils.getRawType(responseType).getName()
            + "' is not a valid response body type. Did you mean ResponseBody?");
      }
	  // 创建 ResponseConverter
      responseConverter = createResponseConverter();

      for (Annotation annotation : methodAnnotations) {
		// 解析方法的注解参数
        parseMethodAnnotation(annotation);
      }

      // 省略代码......

      return new ServiceMethod<>(this);
    }

#### 1.1 构建CallAdapter

### ServiceMethod.createCallAdapter
	  private CallAdapter<?> createCallAdapter() {
      Type returnType = method.getGenericReturnType();
      if (Utils.hasUnresolvableType(returnType)) {
        throw methodError(
            "Method return type must not include a type variable or wildcard: %s", returnType);
      }
      if (returnType == void.class) {
        throw methodError("Service methods cannot return void.");
      }
      Annotation[] annotations = method.getAnnotations();
      try {
		// 根据返回类型 和 注解信息返回对应的callAdapter
        return retrofit.callAdapter(returnType, annotations);
      } catch (RuntimeException e) { // Wide exception range because factories are user code.
        throw methodError(e, "Unable to create call adapter for %s", returnType);
      }
    }
### Retrofit.nextCallAdapter

 	public CallAdapter<?> nextCallAdapter(CallAdapter.Factory skipPast, Type returnType,
      Annotation[] annotations) {
    checkNotNull(returnType, "returnType == null");
    checkNotNull(annotations, "annotations == null");

    int start = adapterFactories.indexOf(skipPast) + 1;
	
    for (int i = start, count = adapterFactories.size(); i < count; i++) {
	  // 从初始化时已有的adapterFactories里找，有就返回
      CallAdapter<?> adapter = adapterFactories.get(i).get(returnType, annotations, this);
      if (adapter != null) {
        return adapter;
      }
    }

### RxJavaCallAdapterFactory.getCallAdapter

	private CallAdapter<Observable<?>> getCallAdapter(Type returnType, Scheduler scheduler) {
    Type observableType = getParameterUpperBound(0, (ParameterizedType) returnType);
    Class<?> rawObservableType = getRawType(observableType);
    if (rawObservableType == Response.class) {
      if (!(observableType instanceof ParameterizedType)) {
        throw new IllegalStateException("Response must be parameterized"
            + " as Response<Foo> or Response<? extends Foo>");
      }
      Type responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
      return new ResponseCallAdapter(responseType, scheduler);
    }

    if (rawObservableType == Result.class) {
      if (!(observableType instanceof ParameterizedType)) {
        throw new IllegalStateException("Result must be parameterized"
            + " as Result<Foo> or Result<? extends Foo>");
      }
      Type responseType = getParameterUpperBound(0, (ParameterizedType) observableType);
      return new ResultCallAdapter(responseType, scheduler);
    }

    return new SimpleCallAdapter(observableType, scheduler);
  }

### RxJavaCallAdapterFactory.ResponseCallAdapter

	static final class ResponseCallAdapter implements CallAdapter<Observable<?>> {
    private final Type responseType;
    private final Scheduler scheduler;

    ResponseCallAdapter(Type responseType, Scheduler scheduler) {
      this.responseType = responseType;
      this.scheduler = scheduler;
    }

    @Override public Type responseType() {
      return responseType;
    }

    @Override public <R> Observable<Response<R>> adapt(Call<R> call) {
      Observable<Response<R>> observable = Observable.create(new CallOnSubscribe<>(call));
      if (scheduler != null) {
        return observable.subscribeOn(scheduler);
      }
      return observable;
    }
  }

### RxJavaCallAdapterFactory
	static final class CallOnSubscribe<T> implements Observable.OnSubscribe<Response<T>> {
    private final Call<T> originalCall;

    CallOnSubscribe(Call<T> originalCall) {
      this.originalCall = originalCall;
    }

    @Override public void call(final Subscriber<? super Response<T>> subscriber) {
      // Since Call is a one-shot type, clone it for each new subscriber.
      Call<T> call = originalCall.clone();

      // Wrap the call in a helper which handles both unsubscription and backpressure.
      RequestArbiter<T> requestArbiter = new RequestArbiter<>(call, subscriber);
      subscriber.add(requestArbiter);
      subscriber.setProducer(requestArbiter);
    }
  }

### RxJavaCallAdapterFactory
	static final class RequestArbiter<T> extends AtomicBoolean implements Subscription, Producer {
    private final Call<T> call;
    private final Subscriber<? super Response<T>> subscriber;

    RequestArbiter(Call<T> call, Subscriber<? super Response<T>> subscriber) {
      this.call = call;
      this.subscriber = subscriber;
    }

    @Override public void request(long n) {
      if (n < 0) throw new IllegalArgumentException("n < 0: " + n);
      if (n == 0) return; // Nothing to do when requesting 0.
      if (!compareAndSet(false, true)) return; // Request was already triggered.

      try {
		// 重要到这里了 去执行请求的call,这个call 等待外部传入call
        //  Retrofit.create{serviceMethod.callAdapter.adapt(okHttpCall)}
        Response<T> response = call.execute();
        if (!subscriber.isUnsubscribed()) {
		  // okhttp请求返回的结果 通过Rx被观察这拿到
          subscriber.onNext(response);
        }
      } catch (Throwable t) {
        Exceptions.throwIfFatal(t);
        if (!subscriber.isUnsubscribed()) {
          subscriber.onError(t);
        }
        return;
      }

      if (!subscriber.isUnsubscribed()) {
        subscriber.onCompleted();
      }
    }
#### 2.2 构建ResponseConverter

### ServiceMothod

    private Converter<ResponseBody, T> createResponseConverter() {
      Annotation[] annotations = method.getAnnotations();
      try {
        // 又从retrofit获取
        return retrofit.responseBodyConverter(responseType, annotations);
      } catch (RuntimeException e) { // Wide exception range because factories are user code.
        throw methodError(e, "Unable to create converter for %s", responseType);
      }
    }
### Retrofit

 	public <T> Converter<ResponseBody, T> responseBodyConverter(Type type, Annotation[] annotations) {
    return nextResponseBodyConverter(null, type, annotations);
  	}

### Retrofit

	public <T> Converter<ResponseBody, T> nextResponseBodyConverter(Converter.Factory skipPast,
      Type type, Annotation[] annotations) {
    checkNotNull(type, "type == null");
    checkNotNull(annotations, "annotations == null");

    int start = converterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = converterFactories.size(); i < count; i++) {
	  // 从 converterFactories 里面去取对应type的
      Converter<ResponseBody, ?> converter =
          converterFactories.get(i).responseBodyConverter(type, annotations, this);
      if (converter != null) {
        //noinspection unchecked
        return (Converter<ResponseBody, T>) converter;
      }
    }

    //省略代码......
    throw new IllegalArgumentException(builder.toString());
  }

### JacksonConverterFactory

   	@Override
   	public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
      Retrofit retrofit) {
    JavaType javaType = mapper.getTypeFactory().constructType(type);
    ObjectReader reader = mapper.reader(javaType);
    return new JacksonResponseBodyConverter<>(reader);
  	}

### 2、构造OkHttpCall

### OkHttpCall
		
  	@Override 
	public Response<T> execute() throws IOException {
    okhttp3.Call call;

    synchronized (this) {
      if (executed) throw new IllegalStateException("Already executed.");
      executed = true;

      if (creationFailure != null) {
        if (creationFailure instanceof IOException) {
          throw (IOException) creationFailure;
        } else {
          throw (RuntimeException) creationFailure;
        }
      }

      call = rawCall;
      if (call == null) {
        try {
          //创建真正的 okhttp 的 call
          call = rawCall = createRawCall();
        } catch (IOException | RuntimeException e) {
          creationFailure = e;
          throw e;
        }
      }
    }

    if (canceled) {
      call.cancel();
    }
	// 解析okhttp执行的结果
    return parseResponse(call.execute());
  }

### OkHttpCall

  	private okhttp3.Call createRawCall() throws IOException {
	// 获取到 request
    Request request = serviceMethod.toRequest(args);
    okhttp3.Call call = serviceMethod.callFactory.newCall(request);
    if (call == null) {
      throw new NullPointerException("Call.Factory returned null.");
    }
    return call;
  }

### OkHttpCall

	Response<T> parseResponse(okhttp3.Response rawResponse) throws IOException {
    ResponseBody rawBody = rawResponse.body();

    // Remove the body's source (the only stateful object) so we can pass the response along.
    rawResponse = rawResponse.newBuilder()
        .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
        .build();

    int code = rawResponse.code();
    if (code < 200 || code >= 300) {
      try {
        // Buffer the entire body to avoid future I/O.
        ResponseBody bufferedBody = Utils.buffer(rawBody);
        return Response.error(bufferedBody, rawResponse);
      } finally {
        rawBody.close();
      }
    }

    if (code == 204 || code == 205) {
      return Response.success(null, rawResponse);
    }

    ExceptionCatchingRequestBody catchingBody = new ExceptionCatchingRequestBody(rawBody);
    try {
	  // 解析结果 用事先准备好的 ResponseConverter
      T body = serviceMethod.toResponse(catchingBody);
      return Response.success(body, rawResponse);
    } catch (RuntimeException e) {
      // If the underlying source threw an exception, propagate that rather than indicating it was
      // a runtime exception.
      catchingBody.throwIfCaught();
      throw e;
    }
  }

### ServiceMethod
	 /** Builds a method return value from an HTTP response body. */
 	T toResponse(ResponseBody body) throws IOException {
    	return responseConverter.convert(body);
  	}


# 三、流程梳理

+ 初始化Retrofit：通过Retrofit.build 初始化一个Retrofit对象,这个是对外提供服务的对象；
+ 使用Retrofit: 通过动态代理技术构建出接口的实现类
	+ 第一步：构建出 ServiceMothed 对象
		+ 构建对应的CallAdapter
			+ DefaultCallAdapterFactory
			+ ExecutorCallAdapterFactory
			+ RxJavaCallAdapterFactory（RxJava提供的）,有下面三种策略
				+ ResponseCallAdapter
				+ SimpleCallAdapter
				+ ResultCallAdapter
		+ 构建对应的：ResponseConverter
	+ 第二步：构建OkHttpCall，这个实际是封装了OkHttp,执行最终的请求
	+ 第三步：执行serviceMethod.callAdapter.adapt(okHttpCall)。
		+ 把okHttpCall传入开始构建好的CallAdapter
		+ callAdapter.adapt方法最终会执行okHttpCall.excute方法
			+ 用开始构建好的ServiceMothed去获取Request
			+ 用okhttp请求，并得到响应
			+ 通过ServiceMothed中构建好的ResponseConverter解析数据
			+ 最终获得响应，该怎么回调出去就怎么出去

# 四、设计模式应用
### 1、Retrofit
使用了外观模式，对外提供接口方法，隐藏了内部所有的实现。
### 2、DefaultCallAdapterFactory


### 3、Service

