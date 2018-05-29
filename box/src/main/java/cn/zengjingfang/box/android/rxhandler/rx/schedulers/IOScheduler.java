package cn.zengjingfang.box.android.rxhandler.rx.schedulers;

import android.os.Handler;
import android.os.HandlerThread;

import java.util.concurrent.TimeUnit;

import cn.zengjingfang.box.android.rxhandler.rx.Scheduler;
import cn.zengjingfang.box.android.rxhandler.rx.Subscription;
import cn.zengjingfang.box.android.rxhandler.rx.functions.Action0;

/**
 * IO 任务 线程
 * Created by ZengJingFang on 2018/4/27.
 */

public class IOScheduler extends Scheduler {

    private static final Scheduler IO_SCHEDULER = new IOScheduler();
    private static Handler ioHandler = null;

    public IOScheduler() {
        HandlerThread handlerThread = new HandlerThread("io-handler-thread");
        handlerThread.start();
        ioHandler = new Handler(handlerThread.getLooper());
    }

    public static Scheduler getIoScheduler() {
        return IO_SCHEDULER;
    }

    @Override
    public Worker createWorker() {
        return new IOWork();
    }

    static final class IOWork extends Scheduler.Worker implements Action0 {
        @Override
        public void call() {

        }

        @Override
        public Subscription schedule(Action0 action) {
            return null;
        }

        @Override
        public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
            return null;
        }

        @Override
        public void unsubscribe() {

        }

        @Override
        public boolean isUnsubscribed() {
            return false;
        }
    }

}
