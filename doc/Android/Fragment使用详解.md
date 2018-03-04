# Fragment简述 #
Fragment（碎片），必须要依赖一个Activity（活动），一个Framgent可以被多个Activity关联，并且互不干扰。一个Fragment有自己的Id,以及Tag,来作为自身的身份识别。在一个Activity或者一个Fragment中，可以通过***findFragmentById()***,或者***findFragmentByTag()***来获得。如何去理解呢？举个列子。很久以前，Activity只是个摆摊的小贩，一个个干活，挣的钱也是一个人分。但是，activity积累了点资本之后就雇(new了几个Fragmet的实例，并关联添加到自己的View中）了几个小弟（Fragment),并把不同的工作分给了不同个小弟（Fragment）来完成。这样通过不同的小弟（Fragment）分工合作，出色的完成了现有的工作，节约了资源（内存），提高了效率。当然，如果老板（Activity)跑路了（onPause \ onStop \ onDestory),小弟们（依赖于该Activity的所有Fragment）得到消息也会做出相应的反应。

# Fragment生命周期 #
	  activity   |     fragment
    -------------------------------------------------
	  Created    |     onAttach()  //fragment关联到activity，此方法中可以进行一些数据的初始化工作
	          	 |	   onCreate() 
			  	 |     onCreateView()//绘制视图，返回该Fragment对应的Layout,初始化UI
			  	 |     onAcitivtyCreated()//当宿主Activity成功创建（onCreate)后回调的方法，可以对Activity进行一些判断
	-------------------------------------------------
	  Started	 | 	   onStart()
	-------------------------------------------------
	  Resumed	 | 	   onResume()
	-------------------------------------------------
	  Paused	 | 	   onPause()
	-------------------------------------------------
	  Stoped	 | 	   onStop()
	-------------------------------------------------
	  Destroyed	 | 	   onDestoryView()	//销毁视图
				 |     onDestory()	
				 |     onDetach() //分离 与宿主Activity分离

>宿主Activity管理了依赖的Fragment,当依赖该Activity的所有Fragment**依次**创建完毕（onCreateView)之后，宿主Activity完成创建（onCreate),若退出，则依赖该Activity的所有Fragment**依次**销毁分离（onDetach)之后，宿主activity才会被销毁（onDestroy)。总之，Fragment的生命周期，依赖于Activity,所以都会在Activity之前调用相应方法。

# Fragment实战 #

## Fragment静态添加 ##

>给FragmentMainActivity的布局文件，里边直接添加了两个Fragment组件，并通过 android:name="",直接指定了该Fragment。

    
		<LinearLayout
	    xmlns:android="http://schemas.android.com/apk/res/android"
	    xmlns:tools="http://schemas.android.com/tools"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"
	    android:orientation="horizontal">
	
	    <fragment
	        android:id="@+id/main_left_fragment"
	        android:name="com.zjf.studydemo.fragment.LeftFragment"
	        android:layout_width="0dp"
	        android:layout_height="match_parent"
	        android:layout_weight="2"
	        tools:layout="@layout/left_fragment">
	    </fragment>
	
	
	    <fragment
	        android:id="@+id/main_right_fragment"
	        android:name="com.zjf.studydemo.fragment.RightFragment"
	        android:layout_width="0dp"
	        android:layout_height="match_parent"
	        android:layout_weight="5"
	        tools:layout="@layout/right_fragment"/>

	

## Fragment动态添加 ##

>给FragmentMainActivity的布局文件添加一个FragmentLayout

	    <FrameLayout
        android:id="@+id/main_right_Fragment_layout"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="5">

        <fragment
            android:id="@+id/main_right_fragment"
            android:name="com.zjf.studydemo.fragment.RightFragment"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            tools:layout="@layout/right_fragment"/>
  		 </FrameLayout>


>在FragmentMainActivity中动态添加Fragment

      			Fragment anotherRightFragment = new AnotherRightFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                /*开启一个事务*/
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                /*向容器内添加碎片*/
                fragmentTransaction.add(R.id.main_right_Fragment_layout, anotherRightFragment, "anotherRight");
                /*添加一个反回栈*/
                fragmentTransaction.addToBackStack(null);
                /*提交事务*/
                fragmentTransaction.commit();



## Fragment与Activity之间的通信 ##

> 给Fragment注册监听

	 public interface OnNewsTitleFragmentClick {
        void onNewsListItemClick(NewsInfo newsInfo);
     }
	 
	 public void setOnNewsTitleFragmentClick(OnNewsTitleFragmentClick onNewsTitleFragmentClick) {
        mOnNewsTitleFragmentClick = onNewsTitleFragmentClick;
    }
 	 @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NewsInfo newsInfo = mNewsInfos.get(position);

        if (mOnNewsTitleFragmentClick != null) {
            mOnNewsTitleFragmentClick.onNewsListItemClick(newsInfo);
        }
    }
> 在Activiity中获取到Fragment实例，设置监听事件

	
	NewsTitleFragment newsTitleFragment = (NewsTitleFragment) getSupportFragmentManager().findFragmentById(R.id.news_main_title_fragment);
        newsTitleFragment.setOnNewsTitleFragmentClick(this);


	 @Override
    public void onNewsListItemClick(NewsInfo newsInfo) {

        NewsContentFragment newsContentFragment = (NewsContentFragment) getSupportFragmentManager().findFragmentById(R.id.news_main_content_fragment);
        newsContentFragment.refresh(newsInfo.getTitle(), newsInfo.getContent());
    }

>Fragment之间的通信通过宿主Activity作为中间人，使用上述方法实现了通信，这样便于Activity对依赖其的Fragment进行统一管理。


***获取源码：***[http://github.com/zengjingfang/StudyDemo](http://github.com/zengjingfang/StudyDemo "StudyDemo")