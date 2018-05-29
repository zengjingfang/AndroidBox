package cn.zengjingfang.box.android.rxhandler;

import android.os.Build;

import cn.zengjingfang.box.android.rxhandler.rx.Observable;
import cn.zengjingfang.box.android.rxhandler.rx.Scheduler;
import cn.zengjingfang.box.android.rxhandler.rx.schedulers.Schedulers;
import cn.zengjingfang.box.android.rxhandler.rx.Subscriber;


/**
 * Created by ZengJingFang on 2017/12/26
 */
public class RxJava1 {


    private static final String TAG = "RxJava1";

    public static void main(String[] args) {
        testCreate();
    }


    public static void testCreate() {
       d(TAG, "xxx " + Build.SERIAL);
        Observable<String> observable1 = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {

               d(TAG, "===>>>> " + Thread.currentThread());

                subscriber.onStart();
                subscriber.onNext("--test-create--next" + Thread.currentThread());

                subscriber.onCompleted();

            }
        });

        Subscriber<String> stringSubscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
               d(TAG, "===test=create=completed");

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
               d(TAG, ">>> " + s);
               d(TAG, "===>>>> " + Thread.currentThread());
            }
        };

        observable1.subscribe(stringSubscriber);


        Scheduler ioScheduler = Schedulers.io();
        Observable<String> observer2 = observable1.subscribeOn(ioScheduler);
//
//        Scheduler mainScheduler = AndroidSchedulers.mainThread();
//        Observable<String> observer3 = observer2.observeOn(mainScheduler);
//
        observer2.subscribe(stringSubscriber);

    }

    public static void d(String tag,String s) {
        System.out.print("\n" + tag + s);
    }



//    public static void testDefer() {
//        Observable<String> defer = Observable.defer(new Func0<Observable<String>>() {
//            @Override
//            public Observable<String> call() {
//                final Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
//                    @Override
//                    public void call(Subscriber<? super String> subscriber) {
//                        subscriber.onNext("defer--2--");
//                        // onError后不会响应onCompleted ?
////                        subscriber.onError(new Throwable("defer-error"));
//                        subscriber.onCompleted();
//                    }
//                });
//                return observable;
//            }
//        });
//        defer.subscribe(new Action1<String>() {
//            @Override
//            public void call(String s) {
//                Log.d(TAG, ">>> " + s);
//            }
//        }, new Action1<Throwable>() {
//            @Override
//            public void call(Throwable throwable) {
//                Log.d(TAG, "=== defer" + throwable);
//            }
//        }, new Action0() {
//            @Override
//            public void call() {
//                Log.d(TAG, "===test=defer=completed");
//                testFrom();
//            }
//        });
//
//    }
//
//    public static void testFrom() {
//
//        Integer[] items = { 0, 1, 2, 3, 4, 5 };
//
//
//        Observable<Integer> observable = Observable.from(items);
//
//        observable.subscribe(new Action1<Integer>() {
//            @Override
//            public void call(Integer item) {
//                Log.d(TAG, "-- form --" + item);
//            }
//        }, new Action1<Throwable>() {
//            @Override
//            public void call(Throwable throwable) {
//
//            }
//        }, new Action0() {
//            @Override
//            public void call() {
//                Log.d(TAG, "===complete==form===");
//                testInterval();
//                testBuffer();
//            }
//        });
//
//        Future<String> future = new Future<String>() {
//            @Override
//            public boolean cancel(boolean mayInterruptIfRunning) {
//                return false;
//            }
//
//            @Override
//            public boolean isCancelled() {
//                return false;
//            }
//
//            @Override
//            public boolean isDone() {
//                return false;
//            }
//
//            @Override
//            public String get() throws InterruptedException, ExecutionException {
//                return "future-1";
//            }
//
//            @Override
//            public String get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
//                return "future-2";
//            }
//        };
////        Observable<String> observable1 = Observable.from(future);
//        Observable<String> observable1 = Observable.from(future,10,TimeUnit.SECONDS);
//        observable1.subscribe(new Subscriber<String>() {
//            @Override
//            public void onCompleted() {
//                Log.d(TAG, "from-future-completed");
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.e(TAG, "from-future-e" + e);
//            }
//
//            @Override
//            public void onNext(String s) {
//                Log.i(TAG, "from-future-next: " + s);
//            }
//        });
//
//
//    }
//
//    public static void testInterval() {
////        Observable.interval(10L, 3, TimeUnit.SECONDS, Schedulers.newThread()).limit(10).subscribe(new Action1<Long>() {
////            @Override
////            public void call(Long aLong) {
////                Log.d(TAG, "---test-interval--" + aLong);
////            }
////        }, new Action1<Throwable>() {
////            @Override
////            public void call(Throwable throwable) {
////
////            }
////        }, new Action0() {
////            @Override
////            public void call() {
////                Log.d(TAG, "===complete==interval===");
////            }
////        });
//        Log.d(TAG, "-------------------------------");
//        // period:3 initialDelay5L  由于要5L后才init 所以 第一个3s取到的observable为null
//        Observable.interval(5L, 3, TimeUnit.SECONDS, Schedulers.newThread()).limit(5).buffer(3, TimeUnit.SECONDS).subscribe(new Subscriber<List<Long>>() {
//
//            @Override
//            public void onCompleted() {
//                Log.d(TAG, "interval-completed");
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onNext(List<Long> longs) {
//                Log.d(TAG, "interval-next-longs: " + longs);
//            }
//        });
//    }
//
//    public static void testBuffer() {
//        Integer[] items = { 0, 1, 2, 3, 4, 5 ,6};
//        Observable.from(items).buffer(2).subscribe(new Action1<List<Integer>>() {
//            @Override
//            public void call(List<Integer> integers) {
//                Log.d(TAG, "from-buffer-2 >>> integers" + integers);
//            }
//        });
//        Observable.from(items).buffer(2,3).subscribe(new Action1<List<Integer>>() {
//            @Override
//            public void call(List<Integer> integers) {
//                Log.d(TAG, "from-buffer-2-skip-3 >>> integers" + integers);
//            }
//        });
//        Log.d(TAG, "=============================================");
//
//        Observable.from(items).buffer(3,TimeUnit.SECONDS).subscribe(new Subscriber<List<Integer>>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onNext(List<Integer> lists) {
//                Log.d(TAG, "----lists" + lists);
//            }
//        });
//
//    }

}
