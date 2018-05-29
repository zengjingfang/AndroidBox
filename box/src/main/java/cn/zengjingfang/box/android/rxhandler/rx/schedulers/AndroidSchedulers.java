package cn.zengjingfang.box.android.rxhandler.rx.schedulers;

import cn.zengjingfang.box.android.rxhandler.rx.Scheduler;

/**
 *
 * Created by ZengJingFang on 2018/4/27.
 */

public class AndroidSchedulers {
    public static Scheduler mainThread() {
        return MainScheduler.getMainScheduler();
    }
}
