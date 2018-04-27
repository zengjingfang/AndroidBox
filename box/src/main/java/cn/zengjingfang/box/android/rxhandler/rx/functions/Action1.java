package cn.zengjingfang.box.android.rxhandler.rx.functions;

/**
 * Created by ZengJingFang on 2018/4/27.
 */

public interface Action1<T> extends Action {

    void call(T t);
}
