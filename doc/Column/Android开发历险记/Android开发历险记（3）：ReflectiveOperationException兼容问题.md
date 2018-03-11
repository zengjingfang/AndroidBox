### 一、问题背景

在崩溃平台出现一个这样的ReflectiveOperationException崩溃，拿4.0版本的系统发现应用直接挂了，可见这个异常问题严重。崩溃发生的代码如下:

> 反射获取getTag接口

	private static String getWakeLockTag(PowerManager.WakeLock wakeLock) {
        String tag = "Default-WakeLock-Tag";
        if (wakeLock == null) {
            return tag;
        }
        try {
            Method getTagMethod = wakeLock.getClass().getDeclaredMethod("getTag");
            getTagMethod.setAccessible(true);
            tag = (String) getTagMethod.invoke(wakeLock, (Object[]) null);
        } catch (ClassNotFoundExceptione) {
            LogUtil.w(TAG,e);
        } 
        return tag;
    }

### 二、分析过程


#### 1、这里为什么要用反射

查看手机系统为Android4.2.2.r1源码：

[4.2.2_r1  PowerManager](https://android.googlesource.com/platform/frameworks/base/+/android-4.2.2_r1/core/java/android/os/PowerManager.java)

有wakeLock 这个内部类，但是没有getTag这个方法。

且来看看高版本的方法：

   /** @hide */
   public void setTag(String tag) {
         mTag = tag;
   }
   
   /** @hide */
   public String getTag() {
        return mTag;
   }

  可以看出加了  @hide 对外不公开，但是系统内部可见的，所以这里要用反射。


#### 2、修改catch块里面的异常为父类ReflectiveOperationException


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getWakeLockTag(PowerManager.WakeLock wakeLock) {
        String tag = "Default-WakeLock-Tag";
        if (wakeLock == null) {
            return tag;
        }
        try {
            Method getTagMethod = wakeLock.getClass().getDeclaredMethod("getTag");
            getTagMethod.setAccessible(true);
            tag = (String) getTagMethod.invoke(wakeLock, (Object[]) null);
        } catch (ReflectiveOperationException e) {
            LogUtil.w(TAG,e);
        } catch (Exception e) {
            LogUtil.w(TAG,e);
        }
        return tag;
    }

#### 3、以为万事大吉，谁知道报了VerifyError

	java.lang.VerifyError: com/xxx/xxx/xxx/xxx/manager/WakeLockManager

getWakeLockTag 是上述类WakeLockManager中的一个方法

查资料[java.lang.VerifyError 原理及常见错误](https://www.jianshu.com/p/07873b237b86)

JVM加载类的时候，“校验器”检查文件格式虽然正确，但是内部的存在不一致性和安全性的问题，所以抛出了该错误。

不同的虚拟编译器不一样，所以抛出的错误信息叶铿不一样。

异常名称	异常栈中的段落信息	可能原因
+ java.lang.VerifyError	Call to wrong initialization method	可能是在调用构造函数即<init>的时候传进了错误的owner

+ java.lang.VerifyError	Incompatible object argument for function call	同样是方法调用的时候出现的错误。看时候有参数设置错误了
+ java.lang.VerifyError	Stack size too large	设置的最大栈空间大小不够
+ java.lang.VerifyError	Illegal local variable number	可能是设置的最大局部变量大小不够，也可能是访问的局部变量的index不对
+ java.lang.VerifyError	Must call initializers using invokespecial	在你调用 <init>方法的时候使用了非INVOKESPECIAL的其他操纵码了。
+ java.lang.VerifyError	Expecting to find integer on stack	可能是在赋值的时候类型不匹配，典型的就是将int类型直接赋值到Integer这之类的。固然在写java代码的时候可以直接赋值，但是在字节码的时候先要调用Integer的valueOf方法创建一个Integer对象再赋值
+ java.lang.ClassFormatError	Arguments can't fit into locals in class file	可能是设置的最大局部变量大小不够

Android 虚拟机注意
ART 模式下面，可能不会报告错误
但是在 Davlik 虚拟机下，会在运行时编译，检测器就会工作
导致在5.0及其以上的设备工作正常，但在操作系统5.0以下（部分4.4开启了ART不会出现）以下报告java.lang.VerifyError` 错误

导致这个错误的原因有2个

三方jar包本身有错误
反编译smali代码修改继承或者申请寄存器操作错误


#### 4、进一步验证VerifyError


	AndroidRuntime: FATAL EXCEPTION: Thread-21891
      java.lang.VerifyError: com/xxx/xxx/xxx/xxx/xxx/WakeLockManager
             at com.xxx.xxx.xxx.xxx.b.b.b(Unknown Source)
             at com.xxx.xxx.xxx.xxx.b.b.a(Unknown Source)
             at com.xxx.xxx.xxx.xxx.b.b$1.run(Unknown Source)

+ 验证结果：如果在5.0.0的机器上跑，则不存在该问题。
+ 分析结论：
	+ Davlik 虚拟机下，会在运行时编译，检测器就会工作，报了该异常；
	+ 5.0以上的机器开启了ART,没有报告该错误。

#### 5、尝试别的姿势，修改代码如下

	 private static String getWakeLockTag(PowerManager.WakeLock wakeLock,String wakeLockName) {
        String tag = "Default-WakeLock-Tag";
        if (wakeLock == null) {
            return tag;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                Method getTagMethod = wakeLock.getClass().getDeclaredMethod("getTag");
                getTagMethod.setAccessible(true);
                new ReflectiveOperationException("test");// 直接New一个，而不是在catch的代码块里面
                tag = (String) getTagMethod.invoke(wakeLock, (Object[]) null);
            }catch (Exception e) {
                LogUtil.w(TAG,e);
            }
        }else {
            LogUtil.d(TAG, "api < 19 ,so wakeLockName: " + wakeLockName);
            return wakeLockName;
        }
        return tag;
    }
+ 验证结果：没有报出VerifyError

#### 6、那么catch里面和直接在代码里面直接去new有什么区别呢？

+ catch代码块在类加载时就会进行校验
+ 而代码中需要运行时才会检测

所以，出现了上述的现象。在代码中 `new ReflectiveOperationException("test");并没有真正的运行，但是在catch代码块中直接就爆出了 `java.lang.VerifyError`。	


###  三、分析结论

+ 问题原因：
	+ ClassNotFoundExceptione是ReflectiveOperationException的子类，无法catch到父类。
	+ Davlik 虚拟机下，会在运行时编译，检测器就会工作。报告VerifyError，5.0以上版本开启了ART,所以没有报出。
+ 解决方案：
	+ 将ClassNotFoundExceptione改为ReflectiveOperationException在低版本报VerifyError，不可取。
	+ 所以更改为Exception。

### 四、扩展知识

+ 源码中的@hide注解方法是系统对外隐藏的方法，无法直接调用。

### 五、参考资料

+ [java.lang.VerifyError 原理及常见错误](https://www.jianshu.com/p/07873b237b86)