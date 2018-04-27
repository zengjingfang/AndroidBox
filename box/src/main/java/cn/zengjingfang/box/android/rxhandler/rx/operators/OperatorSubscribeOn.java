package cn.zengjingfang.box.android.rxhandler.rx.operators;

import cn.zengjingfang.box.android.rxhandler.rx.Observable;
import cn.zengjingfang.box.android.rxhandler.rx.Producer;
import cn.zengjingfang.box.android.rxhandler.rx.Scheduler;
import cn.zengjingfang.box.android.rxhandler.rx.Subscriber;
import cn.zengjingfang.box.android.rxhandler.rx.functions.Action0;

/**
 *
 * Created by ZengJingFang on 2018/4/27.
 */

public class OperatorSubscribeOn<T> implements Observable.OnSubscribe<T> {

    final Scheduler scheduler;
    final Observable<T> source;

    public OperatorSubscribeOn(Observable<T> source, Scheduler scheduler) {
        this.scheduler = scheduler;
        this.source = source;
    }


    @Override
    public void call(final Subscriber<? super T> subscriber) {
        final Scheduler.Worker inner = scheduler.createWorker();
        subscriber.add(inner);

        inner.schedule(new Action0() {
            @Override
            public void call() {
                final Thread t = Thread.currentThread();

                Subscriber<T> s = new Subscriber<T>(subscriber) {
                    @Override
                    public void onNext(T t) {
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

                source.unsafeSubscribe(s);
            }
        });
    }
}
