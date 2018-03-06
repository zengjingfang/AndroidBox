package cn.zengjingfang.box.java;

/**
 * Created by Administrator on 2018/3/6.
 */

public class PoPoTest {

    private static final int[] arr = {13, 1, 15, 3, 6, 7, 6, 4, 11};

    public static void main(String[] args) {

        for (int i = 0; i < arr.length-1; i++) {

            for (int j=0;j<arr.length-1; j++) {
                int a = arr[j];
                int b = arr[j + 1];
                if (a > b) {
                    arr[j] = b;
                    arr[j + 1] = a;
                }
            }

        }
        System.out.print("result: " + arr);

    }
}
