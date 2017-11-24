# 前言

这几天看到一个搞笑的图片表情，一个夸张的表情，底下配上了文字：在我的机器上是没有问题的呀！我们通常会碰到这种问题，尤其是Android这种严重碎片化，被各大厂商自定义的的机器上，总有些让人无法理解的坑。但是，为了更加广大人民群众的利益，我们还是老老实实的做挖坑、找坑、填坑等系统、机器设备兼容性问题。

# 一、Android原生API变化

	WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	String macAddress = wifiManager.getConnectionInfo().getMacAddress();

我们有时候通过设备的一些信息来标识这台设备，比如出厂序列号、MAC地址、SIM号码等，但是在Android7.0的接口上就出现了返回一直是“02:00:00:00:00:00”，导致标识机器出错。

# 二、API使用不当造成的问题
	// process-core
	private void scanAllApp(){
		List<PackageInfo> appList= packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo packageInfo : appList) {
            Bundle metaData = packageManager.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA).metaData;
        }
    }
    // process-main
    private void aidlGetAccount(){
		//aidl跨进程调用返回Account信息
		return account;
    }

这个问题的现象是应用启动的时候报了TransactionTooLargeException，然而很明显根据exception的栈来看，我启动时调用了aidlGetAccount这个接口，但是这个接口真的只是return一个account对象，不会出现数据过大的情况。追了下源码有个200k的限制，也不存在问题。最后查了博客发现Android的Binder机制限制单应用的总大小为1M，后面发现启动core进程时候循环调用了系统的getApplicationInfo，这个接口包含的数据量很大。当设备上的APP比较多的时候，存在超出的风险。


# 三、厂商定制引发的问题

	if (Build.VERSION.SDK_INT >= 21){
	 	// 调用高版本系统API接口
	}else{
		// 调用低版本系统API接口
	}

发现在某手机上调用接口奔溃，最后发现奔溃的手机API Level 是27，Android版本4.2.2。ROM编译版本写错了，也是坑到家了。

# 四、Android生命周期不对的问题


# 参考资料
[大家遇到过什么 Android 兼容性问题？](https://www.zhihu.com/question/40300713)



