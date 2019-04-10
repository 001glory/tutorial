# Netty源码分析篇

## 文档资料

- [netty官网](https://netty.io/)
- [netty开源中国](https://www.oschina.net/question/tag/netty)

## 概述

Netty是一个NIO client-server框架，可以快速和简单的开发网络应用程序，比如协议服务器服务器和客户端。Netty向你提供了一种新的方式开发你的网络应用程序，使得它简单和可扩展。它通过这样的方式实现：抽象出所涉及的复杂性和通过提供一种简单易用的API，这个将业务逻辑从网络处理代码中解耦出来。因为它是为NIO构建,所有的Netty API都是异步的。

## 说明

>API使用简单,开发门槛低;
 功能强大,预置了多种编解码功能,支持多种主流协议;
 定制能力强,可以通过ChannelHandler对通信框架进行灵活地扩展;
 性能高,通过与其他业界主流的NIO框架对比,Netty的综合性能最优;
 成熟、稳定,Netty修复了已经发现的所有JDK NIO BUG,业务开发人员不需要再为NIO的BUG而烦恼;
 社区活跃,版本迭代周期短,发现的BUG可以被及时修复,同时,更多的新功能会加入;
 经历了大规模的商业应用考验,质量得到验证。在互联网、大数据、网络游戏、企业应用、电信软件等众多行业得到成功商用,证明了它已经完全能够满足不同行业的商业应用了。
 

## 目录

**发现任何分析问题或格式问题欢迎提PR帮忙完善。**

- [说明](README.md)
- [Netty之Channel](md/channel/channel.md)
	- [创建](md/channel/build.md)
	- [初始化](md/channel/init.md)

------


