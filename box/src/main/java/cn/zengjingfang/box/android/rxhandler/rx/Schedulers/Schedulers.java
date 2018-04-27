package cn.zengjingfang.box.android.rxhandler.rx.Schedulers;

import cn.zengjingfang.box.android.rxhandler.rx.Scheduler;

/**
 *
 * Created by ZengJingFang on 2018/4/26.
 */

public class Schedulers {

    public static Scheduler io() {
        return IOScheduler.getIoScheduler();

    }


}
