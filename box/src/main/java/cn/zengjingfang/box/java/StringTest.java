package cn.zengjingfang.box.java;

/**
 * Created by ZengJingFang on 2018/4/12.
 */

public class StringTest {
    public static void main(String[] args) {
        test();

    }

    private static void test() {
        String s1 = "hello word";
        String s2 = "hello word";
        String s3 = new String("hello word");
        String s4 = new String("word");
        String s5 = "hello " + "word";
        String s6 = "hello " + s4;

        System.out.print("\n >>> s1: " + s1.hashCode());
        System.out.print("\n >>> s2: " + s2.hashCode());
        System.out.print("\n >>> s3: " + s3.hashCode());
        System.out.print("\n >>> s4: " + s4.hashCode());
        System.out.print("\n >>> s5: " + s5.hashCode());
        System.out.print("\n >>> s6: " + s6.hashCode());

        System.out.print("\n s1==s2 >>> "+ (s1==s2));
        System.out.print("\n s1==s3 >>> "+ (s1==s3));
        System.out.print("\n s5==s1 >>> "+ (s5==s1));
        System.out.print("\n s5==s6 >>> "+ (s5==s6));

    }
}
