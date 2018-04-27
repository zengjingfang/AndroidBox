package cn.zengjingfang.box.android.rxhandler.rx;

import java.util.concurrent.TimeUnit;

import cn.zengjingfang.box.android.rxhandler.rx.functions.Action0;

/**
 *
 * Created by ZengJingFang on 2018/4/26.
 */

public abstract class Scheduler {

    public abstract Worker createWorker();


    public abstract static class Worker implements Subscription {

        public abstract Subscription schedule(Action0 action);

        public abstract Subscription schedule(final Action0 action, final long delayTime, final TimeUnit unit);

        public long now() {
            return System.currentTimeMillis();
        }
    }
}
