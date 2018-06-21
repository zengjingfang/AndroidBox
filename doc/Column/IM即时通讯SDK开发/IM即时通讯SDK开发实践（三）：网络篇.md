### 1、前言

对于网络有太深的理论知识，我曾尝试看了些，但是还是一知半解。对于网络知识了解甚少，但是不懂这些并不影响我们在应用层进行IM的开发。底层所有的技术都已经帮我们封装好了，不过有些基础理论是我们需要了解的。本篇就网络相关知识简单进行一些介绍。


### 2、网络


### 3、TCP


#### 3.1、关于NAT

IPV4数量有限，可分配的IP不够用。需要通过运营商中转下，运营商维护一个公网ip和内网ip的对应表，机器连接运营商的网络实际是连接了运营商的内网ip,当访问外网时，运营商会替我们转换ip地址。如果，长时间没有数据交换，则会淘汰这个机器的ip对应记录，造成链路中断。

![](https://docs.google.com/drawings/d/e/2PACX-1vRLphF5qkI8iNcXzT7zF8Z47BaN1R-Wd1_7HrcqWnkGNU1mj-machfORk2KLQOzp4nzW1EBzyCQGip6/pub?w=289&h=150)

+ 中国移动:NAT=5min
+ 中国联通：NAT=5min
+ 中国电信：NAT=28min


#### 3.2、关于DHCP(Dynamic Host Configuration Protocol)租期过期

设备接入网络需要给自己设置ip地址，子网掩码，为了让ip地址唯一，可以通过管理员进行统一分配，以免冲突。为了减少管理员的工作量，让DHCP服务器代替了这份工作。但是这个有一定的期限，超过了时间需要自己重新续约。Android系统内部有这个bug，不能主动续约，也没有网络变化事件发出。等到下一次心跳失败触发重连，重新分配ip。

![](https://docs.google.com/drawings/d/e/2PACX-1vSHfECMunaJk2Db8_nbt1dIl6s7U7W7fPsfyIpd_8x_LQYRLCT1Dr4LEBFnH4yzPZwy5HZWKPEb7O6M/pub?w=394&h=215)

#### 3.3、粘包、拆包问题

TCP是一个“流”的协议，虽然我们看来交互的是数据块，实则TCP传输的是一连串无结构的字节流，没有边界。

+ 拆包：

	+ 发送的数据大于TCP发送缓存区的大小；
	+ 发送的数据大于MSS(最大报文长度）。
+ 粘包：
	+ 当发送的数据小于缓存区的大小，TCP就将写入缓存区的数据一次发送出去；
	+ 或者接收端没有及时读取缓存区的数据，就会发生粘包。

+ 解决办法：

	+ 给每个数据包添加首部，首部包含了这个数据包的实际长度；
	+ 数据包为固定长度；
	+ 数据包之间设置边界，比如添加个特殊符号；
	+ 接收端进行循环读取。

### 参考文章

+ [TCP粘包，拆包及解决方法](https://blog.insanecoder.top/tcp-packet-splice-and-split-issue/)