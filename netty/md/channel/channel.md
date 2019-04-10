### Netty之Channel

- ### Channel简述

   Netty网络通信的组件，能够用于执行网络I/O操作。 Channel为用户提供：

  - 当前网络连接的通道的状态（例如是否打开？是否已连接？）
  - 网络连接的配置参数 （例如接收缓冲区大小）
  - 提供异步的网络I/O操作(如建立连接，读写，绑定端口)，异步调用意味着任何I / O调用都将立即返回，并且不保证在调用结束时所请求的I / O操作已完成。调用立即返回一个ChannelFuture实例，通过注册监听器到ChannelFuture上，可以I / O操作成功、失败或取消时回调通知调用方。
  - 支持关联I/O操作与对应的处理程序

  

- ### Channel功能说明

  -  io.netty.channel.channel是Netty网络操作抽象类，它聚合了一组功能，包括但不限于网络的读、写，客户端发起连接、主动关闭连接，链路关闭，获得通信双方的网络地址等。它也包含了Netty框架相关的一些功能，包括获取该Channel的EventLoop,获取缓冲分配器ByteBufAllocator和pipeline等。

- ### Netty另起Channel的原因

  **Channel是Netty抽象出来的网络IO读写相关的接口，为什么不使用JDK NIO原生的Channel而要另起炉灶呢，主要原因如下：**

  1. JDK的SocketChannel和ServerSocketChannel没有提供统一的Channel接口供业务开发者使用，对于用户而言，没有统一的操作视图，使用起来并方便。
  2. JDK的SocketChannel和ServerSocketChannel的主要职责就是网络操作，由于它们是SPI类接口，由具体的虚拟机厂家来提供，所以通过继承SPI功能类来扩展其功能的难度很大。直接实现ServerSocketChannle和SocketChannel抽象类，其工作量和重新开发一个新的Channeld的功能类差不多的。
  3. Netty的channel需要能跟Netty整体框架融合在一起，例如IO模型、基于ChannelPipie的定制模型，以及基于元数据描述配置化的TCP参数等，这些JDK的SocketChannel和ServerSocketChannel都没有提供，需要重新封装。
  4. 自定义的Channel，功能实现更加灵活。

  **基于以上的4个原因，Netty重新设计了Channel接口，并且给予了很多不同的实现，它的设计原理很简单，但是功能却比较复杂，主要的设计理念如下：**

  1. 在Channel接口层，采用Facade模式进行统一封装，将网络IO操作，及相关联的的其它操作封装起来，统一对外提供。
  2. Channel接口的定义尽量大而全，为SocketChannel和ServerSocketChannel提供统一的视图，由不同的子类实现不同的功能，公共功能在抽象父类实现，最大限度上实现功能和接口的重用。
  3. 具体实现采用聚合而非包含的方式，将相关的功能类聚合在Channel中，由Channel统一负责分配和调度，功能实现更加灵活。