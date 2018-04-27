package cn.zengjingfang.box.java;

/**
 *
 * Created by ZengJingFang on 2018/4/18.
 */

public class MathTest {

    private static final int MODE_SHIFT = 30;
    private static final int MODE_MASK  = 0x3 << MODE_SHIFT;
    /**
     * Measure specification mode: The parent has not imposed any constraint
     * on the child. It can be whatever size it wants.
     */
    public static final int UNSPECIFIED = 0 << MODE_SHIFT;

    /**
     * Measure specification mode: The parent has determined an exact size
     * for the child. The child is going to be given those bounds regardless
     * of how big it wants to be.
     */
    public static final int EXACTLY     = 1 << MODE_SHIFT;

    /**
     * Measure specification mode: The child can be as large as it wants up
     * to the specified size.
     */
    public static final int AT_MOST     = 2 << MODE_SHIFT;

    public static void main(String[] args) {
        test2();
    }

    private static void test1() {
        // 30 >>>  0000 0000 0001 1110
        //

        System.out.print("\n MODE_SHIFT: " + Integer.toBinaryString(MODE_SHIFT));//2+4+8+16
        System.out.print("\n 0x3: " + Integer.parseInt("3", 16));
        System.out.print("\n MODE_MASK: " + Integer.toBinaryString(MODE_MASK));
        System.out.print("\n UNSPECIFIED: " + Integer.toBinaryString(UNSPECIFIED));
        System.out.print("\n EXACTLY: " + Integer.toBinaryString(EXACTLY));
        System.out.print("\n AT_MOST: " + Integer.toBinaryString(AT_MOST));

        System.out.print("\n mode: " + getMode(10));


    }
    public static int getMode(int measureSpec) {
        //noinspection ResourceType
        return (measureSpec & MODE_MASK);
    }

    public static void test2() {
        int a = 60; /* 60 = 0011 1100 */
        int b = 13; /* 13 = 0000 1101 */
        int c = 0;
        c = a & b;       /* 12 = 0000 1100 */
        System.out.println("a & b = " + c );

        c = a | b;       /* 61 = 0011 1101 */
        System.out.println("a | b = " + c );

        c = a ^ b;       /* 49 = 0011 0001 */
        System.out.println("a ^ b = " + c );

        c = ~a;          /*-61 = 1100 0011 */
        System.out.println("~a = " + c );

        c = a << 2;     /* 240 = 1111 0000 */
        System.out.println("a << 2 = " + c );

        c = a >> 2;     /* 15 = 1111 */
        System.out.println("a >> 2  = " + c );

        c = a >>> 2;     /* 15 = 0000 1111 */
        System.out.println("a >>> 2 = " + c );
    }
}
