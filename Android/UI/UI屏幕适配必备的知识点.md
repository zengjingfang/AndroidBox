
# 为什么要适配 #
---
Android手机的屏幕尺寸大小、分辨率大小各有不同，如果只是一套布局在不同的设备上所显示的效果会有不同。可以根据产品用户的对象进行有针对性的适配。

# 重要概念 #
---
- 屏幕尺寸

手机平板等设备对角线的长度，单位为inch（英寸）

- 屏幕分辨率

横纵方向的像素点数

- 屏幕像素密度

每英寸上的像素点数，单位dpi (dot per inch)

- 举例

尺寸：4.95 inch    屏幕分辨率：1920x1080   DPI=445

计算：(√(1920^2+1080^2))/4.95=445dpi

- dp 当屏幕像素密度为160dpi 的时候 1dp=1px

480x320 160dpi   1dp=1px

800x480 240dpi   1dp=1.5px


也就是说 一个dp实际铺满屏幕的长度随着分辨率的增大成比例增大

- sp 字体大小

根据谷歌的设计，最好使用的字体大小为：12sp、14sp、18sp、22sp

字体sp不要使用奇数或者小数，会引起精度的丢失



- mdpi res资源文件


mdpi    120dpi ~ 160dpi    1x

hdpi    160dpi ~ 240dpi    1.5x

xhdpi   240dpi ~ 320dpi    2x

xxhdpi  320dpi ~ 480dpi    3x

xxxdpi  480dpi ~ 640dpi    4x


# 屏幕适配方案 #

- weight详解

组件宽度=原来宽度+剩余空间所占百分比宽度

屏幕宽度：L

btn_1的宽度为：2/3L =L+(L-2L)*1/3

       <Button 
        android:id="@+id/btn_1"
        android:layout_width="match_content"
        android:layout_height="wrap_content"
        android:weight="1"
        />

        <Button 
        android:id="@+id/btn_2"
        android:layout_width="match_content"
        android:layout_height="wrap_content"
        android:weight="1"
        />

		——————————————————————————————
	    |       btn_1      |  btn_2   |
		——————————————————————————————
       
- 使用限定符
 
1、使用尺寸限定符 larger ,适用于3.2之前的版本

res/layout/main.xml 手机

res/layout-large/main.xml 平板


2、最小宽度限定符 sw-xxx (Small Width),适用于3.2之后的版本

res/layout/main.xml 手机使用

res/layout-sw600dp/main.xml   最小宽度 如果大于600dp (横屏或者竖屏) 则使用这个layout 

3、使用布局别名
setContentView（R.layout.main)

res/values/layout.xml:

	<resources>
    <item name="main" type="layout">@layout/main</item>
	</resources>

Android3.2之后的平板布局

res/values-sw600dp/layout.xml:
	<resources>
    <item name="main" type="layout">@layout/main_twopanes</item>
</resources>

4、方向限定符

res/values-sw600-land 横屏（land)
res/values-sw600-port 竖屏  (port)

1.res/values/layouts.xml:

	<resources>
    <item name="main_layout" type="layout">@layout/onepane_with_bar</item>
    <bool name="has_two_panes">false</bool>
	</resources>

2.res/values-sw600dp-land/layouts.xml:

	<resources>
    <item name="main_layout" type="layout">@layout/twopanes</item>
    <bool name="has_two_panes">true</bool>
	</resources>

**注意：程序可以根据标志位的Boolean值来获取到当期的适配layout的状态，进一步判断并作出相应的响应**



- 解决屏幕宽度不一致的问题

 
原因：即使使用dp为单位，不同分辨率尺寸的设备的值也不一定相同


方案：
将屏幕进行等分,定义自己的“单位”

默认：res/vaules/lay_x.xml 


		<dimen name="x1">1dp</dimen>
		                  ...
		<dimen name="x320">320dp</dimen>

480x320分辨率：res/vaules-480x320/lay_x.xml
		
		<dimen name="x1">1px</dimen>
						...
		<dimen name="x320">320px</dimen>

800x480分辨率：res/vaules-800x480/lay_x.xml
		
		<dimen name="x1">1.5px</dimen>
		   					...
		<dimen name="x320">480px</dimen>


- 不同资源文件夹下图片的占用内容的问题

测试机器：为xxhdpi

drawble        74.97M  71.1M

drawble-mdpi   74.95M  71.1M

drawble-hdpi   35.38M

drawble-xxhdpi 11.65M  7.8M

解析：若放在drawble-mdpi，而当前机器为xxhdpi，图片需要拉升，长度为3倍关系，平面就是9倍关系，11.65x9=70.2,根据测试结果，成立。drawble与drawble-mdpi的大约相同的原因，是因为我们已drawble-mdpi为基准来进行确定。

# 自适应用户界面 #
---
- 确定当前布局

方法：判断当前界面是否存在该布局（是否显示,View.VISABLE),根据某布局的标志位的Boolean值

举例

布局1：res/values/layouts.xml:

		<resources>
		    <item name="main_layout" type="layout">@layout/onepane_with_bar</item>
		    <bool name="has_two_panes">false</bool>
		</resources>

布局2：res/values-sw600dp-land/layouts.xml:

		<resources>
		    <item name="main_layout" type="layout">@layout/twopanes</item>
		    <bool name="has_two_panes">true</bool>
		<resources>

分析：根据R.layouts.has_two_panes的Boolean值来进行确定，若为false则当前是布局1，若为true则当前布局为布局2。

- 根据当前布局做出响应
- 重复使用不同布局中的相同片段
  
      使用Fragment

- 处理屏幕配置变化


- 动态设置

  在代码中进行布局的设计，非常灵活，可以通过计算当前设备的屏幕宽度来设计尺寸。
