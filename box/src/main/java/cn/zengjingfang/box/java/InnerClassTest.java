package cn.zengjingfang.box.java;

/**
 * Created by ZengJingFang on 2018/2/27
 */
public class InnerClassTest {

    private static String c;
    public String b;

    public void test() {
        new A().test1();

    }

    public static void test0() {

    }
    class A {
        void test1() {
            c = "";
            b ="";
        }

        /**
         * 非静态内部内不能有静态方法
         */
        /*static void test11() {
            c = "";
            b ="";
        }*/

    }
    static class B {
        private A a;
        private InnerClassTest innerClassTest;

        public B() {
        }

        public B(A a, InnerClassTest innerClassTest) {
            this.a = a;
            this.innerClassTest = innerClassTest;
        }

        void test2() {
            c = "";

            innerClassTest.b = "";
        }

        static void test22() {
            c = "";

        }
    }
}
class OutClassTest{

    InnerClassTest.A a = new InnerClassTest().new A();// 外围类的实例来New对象
    InnerClassTest.B b = new InnerClassTest.B();// 直接New对象
}