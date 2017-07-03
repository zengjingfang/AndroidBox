package cn.zengjingfang.box.java;

/**
 * Desc:   并发学习
 * Author: ZengJingFang
 * Time:   2017/6/23 17:54
 * Email:  zengjingfang@foxmail.com
 */
public class ConcurrentTest {

    public static void main(String[] args) {

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                addA();
                addB();
            }
        });

        thread.start();


    }

    private static int a = 1;
    private static int b = 1;
    public static int addA() {

        a++;
        return a;
    }

    public  synchronized static int addB() {
        b++;
        return b;
    }

    private void print(String s) {
        System.out.print(" >>>  "+s);
    }

}
