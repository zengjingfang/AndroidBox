package cn.zengjingfang.box.android.rxhandler.rx.Schedulers;

import cn.zengjingfang.box.android.rxhandler.rx.Scheduler;

/**
 * IO 任务 线程
 * Created by ZengJingFang on 2018/4/27.
 */

public class IOScheduler extends Scheduler {

    private static final Scheduler IO_SCHEDULER = new IOScheduler();

    public static Scheduler getIoScheduler() {
        return IO_SCHEDULER;
    }

    @Override
    public Worker createWorker() {
        return null;
    }
}
