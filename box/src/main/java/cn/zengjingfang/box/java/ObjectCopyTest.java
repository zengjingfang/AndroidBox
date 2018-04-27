package cn.zengjingfang.box.java;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 深拷贝：先new出目标对象，再把原始对象的参数一个一个的copy到新的对象中
 * 浅拷贝：直接“=”的方式吧原始对象的引用，交给目标对象，此时，这两个对象的引用同时指向同一个对象
 *
 * Created by ZengJingFang on 2018/4/17.
 */

public class ObjectCopyTest {

    public static void main(String[] args) {

        shallowCopy();
        deepCopy();
        unModifiableList();

    }

    private static void unModifiableList() {
        List<Integer> list1 = new ArrayList<>();
        list1.add(0);
        list1.add(1);

        List<Integer> list2 = new ArrayList<>();
        list2 = Collections.unmodifiableList(list1);

        list1.add(2);
//        list2.add(3);// 执行这里抛出 UnsupportedOperationException  原因是 list不可变

        System.out.print("\n>>> " + Arrays.toString(list1.toArray()));

        System.out.print("\n>>> " + Arrays.toString(list2.toArray()));

        // >>> [0, 1, 2]
        // >>> [0, 1, 2]
        // 说明：list2在unmodifiableList之后并没有进行深拷贝，只是不能对list2进行任何的“修改类型的”操作
    }

    private static void deepCopy() {
        ArrayList<Integer> list1 = new ArrayList<>();
        list1.add(0);
        list1.add(1);

        ArrayList<Integer> list2 = new ArrayList<>(list1);
        list1.add(2);
        list2.add(3);

        System.out.print("\n>>> " + Arrays.toString(list1.toArray()));

        System.out.print("\n>>> " + Arrays.toString(list2.toArray()));

        //>>> [0, 1, 2]
        //>>> [0, 1, 3]

    }

    private static void shallowCopy() {
        ArrayList<Integer> list1 = new ArrayList<>();
        list1.add(0);
        list1.add(1);

        ArrayList<Integer> list2 = new ArrayList<>();
        list2 = list1;

        list1.add(2);
        list2.add(3);

        System.out.print(">>> " + Arrays.toString(list1.toArray()));

        System.out.print("\n>>> " + Arrays.toString(list2.toArray()));
        // >>> [0, 1, 2, 3]
        // >>> [0, 1, 2, 3]
    }

    /**
     * ArrayList深拷贝的源码解析
     * @param original 原始数据
     * @param newLength 原始数据长度
     * @param newType  新的数据类型
     * @param <T>  新的数据类型
     * @param <U>  原始数据类型
     * @return
     */
    public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {

        // 如果是对象类型：  new Object[newLength]
        // 如果是基本类型：   Array.newInstance
        T[] copy = ((Object)newType == (Object)Object[].class)
                ? (T[]) new Object[newLength]
                : (T[]) Array.newInstance(newType.getComponentType(), newLength);

        // 把原数据copy到新的对象中
        System.arraycopy(original, 0, copy, 0,
                Math.min(original.length, newLength));

        return copy;
    }
}
