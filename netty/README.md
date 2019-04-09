## Netty之Channel创建
[SS](https://github.com/dqqzj/tutorial/blob/master/netty/src/main/resources/pictures/channel/build.png)

```java
package com.github.dqqzj.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;

/**
 * @author qinzhongjian
 * @date created in 2019-04-09 09:44
 * @description: TODD
 * @since 1.0.0
 */
public class Server {
    public static void main(String[] args) {
        EventLoopGroup boss = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        EventLoopGroup work = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss,work)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childAttr(AttributeKey.newInstance("childAttr"), "childAttrValue")
                .handler(new ServerHandler())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //socketChannel.pipeline()
                    }
                });
        try {
            ChannelFuture future = serverBootstrap.bind(8080).sync();
            future.channel().close().sync();
        } catch (InterruptedException e) {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        } finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }
    static class ServerHandler extends SimpleChannelInboundHandler {
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
            System.out.println(o);
        }
    }
}
```



#### 创建channel步骤

- 端口绑定`bind()`
- 初始化并注册`initAndRegister()`
- 创建服务端channel`newChannel()`

端口绑定是抽象类`AbstractBootstrap`的逻辑实现

```java
private ChannelFuture doBind(final SocketAddress localAddress) {
    final ChannelFuture regFuture = this.initAndRegister();
    //省略无关代码......
}
```

```java
final ChannelFuture initAndRegister() {
    Channel channel = null;

    try {
        channel = this.channelFactory.newChannel();
        this.init(channel);
    } catch (Throwable var3) {
        if (channel != null) {
            channel.unsafe().closeForcibly();
            return (new DefaultChannelPromise(channel, GlobalEventExecutor.INSTANCE)).setFailure(var3);
        }
    //省略无关代码......
}
```

分析了源代码可以知道`Netty`的`channel`的创建表面上是在`AbstractBootstrap`中实现的。

进一步分析`newChannel()`这个方法，会发现该方法是由`ReflectiveChannelFactory`类实现的，并且是**反射机制**进行的初始化对象。

```java
public T newChannel() {
    try {
        return (Channel)this.constructor.newInstance();
    } catch (Throwable var2) {
        throw new ChannelException("Unable to create Channel from class " + this.constructor.getDeclaringClass(), var2);
    }
}
```

既然是反射，那么这个`constructor`到底是哪一个类的呢？首先来看一下`ReflectiveChannelFactory`的定义。

```java
public class ReflectiveChannelFactory<T extends Channel> implements ChannelFactory<T> {
    private final Constructor<? extends T> constructor;

    public ReflectiveChannelFactory(Class<? extends T> clazz) {
        ObjectUtil.checkNotNull(clazz, "clazz");

        try {
            this.constructor = clazz.getConstructor();
        } catch (NoSuchMethodException var3) {
            throw new IllegalArgumentException("Class " + StringUtil.simpleClassName(clazz) + " does not have a public non-arg constructor", var3);
        }
    }
   //省略无关代码......
} 
```

到这里就明白了，是`ReflectiveChannelFactory`的构造器进行的初始化`constructor`的值。并且这个值必须是`Channel`的子类。问题转变为了`ReflectiveChannelFactory`的初始化如何发生的。

**回顾代码**

```java
serverBootstrap.group(boss,work)
        .channel(NioServerSocketChannel.class)
        //省略无关代码......
```

进入源代码分析

```java
public B channel(Class<? extends C> channelClass) {
    if (channelClass == null) {
        throw new NullPointerException("channelClass");
    } else {
        return this.channelFactory((io.netty.channel.ChannelFactory)(new ReflectiveChannelFactory(channelClass)));
    }
}
```

发现该方法实际是`AbstractBootstrap`的实现，并且进行了初始化`ReflectiveChannelFactory`，所以`Channel`的初始化就完成了.

### NioServerSocketChannel的初始化

- `newSocket()`通过jdk来创建底层`channel`
- `NioSserverSocketChannelConfig()`进行` tcp`参数配置
- `AbstractNioChannel#configureBlocking(false)`是否阻塞模式
- `AbstractNioChannel#AbstractChannel()`创建`channel`的`id`，`unsafe`，`pipeline`属性

源码分析

```java
public class NioServerSocketChannel extends AbstractNioMessageChannel implements ServerSocketChannel {
    private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
    //省略无关代码......
    private static java.nio.channels.ServerSocketChannel newSocket(SelectorProvider provider) {
        try {
            return provider.openServerSocketChannel();
        } catch (IOException var2) {
            throw new ChannelException("Failed to open a server socket.", var2);
        }
    }

    public NioServerSocketChannel() {
        this(newSocket(DEFAULT_SELECTOR_PROVIDER));
    }

    public NioServerSocketChannel(java.nio.channels.ServerSocketChannel channel) {
        super((Channel)null, channel, 16);
        this.config = new NioServerSocketChannel.NioServerSocketChannelConfig(this, this.javaChannel().socket());
    }
   //省略无关代码......
}
```

### SelectorProvider

跟进源码

```java
public static SelectorProvider provider() {
    synchronized (lock) {
        if (provider != null)
            return provider;
        return AccessController.doPrivileged(
            new PrivilegedAction<SelectorProvider>() {
                public SelectorProvider run() {
                        if (loadProviderFromProperty())
                            return provider;
                        if (loadProviderAsService())
                            return provider;
                        provider = sun.nio.ch.DefaultSelectorProvider.create();
                        return provider;
                    }
                });
    }
}
```

关键的语句就是`provider = sun.nio.ch.DefaultSelectorProvider.create();`

```java
public class DefaultSelectorProvider {
    public static SelectorProvider create() {
        return new KQueueSelectorProvider();
    }
}
```

上面是创建**SelectorProvider**的过程。可以看到SelectorProvider**是`new KQueueSelectorProvider`生成的.

```java
public ServerSocketChannel openServerSocketChannel() throws IOException {
        return new ServerSocketChannelImpl(this);
    }
```

分析`super((Channel)null, channel, 16);`

跟进源码会发现实际是调用了父类的构造器设置了非阻塞模式

```java
protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    super(parent);
    this.ch = ch;
    this.readInterestOp = readInterestOp;

    try {
        ch.configureBlocking(false);
    } catch (IOException var7) {
        try {
            ch.close();
        } catch (IOException var6) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to close a partially initialized socket.", var6);
            }
        }

        throw new ChannelException("Failed to enter non-blocking mode.", var7);
    }
}
```

继续跟进源码的`super(parent);`

```java
protected AbstractChannel(Channel parent) {
    this.parent = parent;
    this.id = this.newId();
    this.unsafe = this.newUnsafe();
    this.pipeline = this.newChannelPipeline();
}
```

**NioServerSocketChannel**的初始化就完成了一大半了，可能还有很多对`SelectorProvider`的疑惑，后续会慢慢分析。