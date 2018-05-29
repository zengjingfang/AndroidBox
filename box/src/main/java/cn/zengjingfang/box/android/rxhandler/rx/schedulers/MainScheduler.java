package cn.zengjingfang.box.android.rxhandler.rx.schedulers;

import cn.zengjingfang.box.android.rxhandler.rx.Scheduler;

/**
 * Android主线程
 *
 * Created by ZengJingFang on 2018/4/27.
 */

public class MainScheduler extends Scheduler{

    private static final Scheduler MAIN_SCHEDULER = new IOScheduler();

    public static Scheduler getMainScheduler() {
        return MAIN_SCHEDULER;
    }

    @Override
    public Worker createWorker() {
        return null;
    }
}
