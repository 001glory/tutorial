### Netty之Channel初始化
![](https://github.com/dqqzj/tutorial/blob/master/netty/src/main/resources/pictures/channel/init.png)

初始化`Channel`步骤

- 初始化入口`init()`

- 设置`options`，`attrs`

- 设置`childOptions`，`childAttrs`

- 配置`handler`，`childHandler`

- 添加连接器`ServerBootstrapAcceptor`  

  初始化入口在`AbstractBootstrap#initAndRegister`方法：

  ```java
  final ChannelFuture initAndRegister() {
          Channel channel = null;
          try {
              channel = this.channelFactory.newChannel();
              this.init(channel);
          } catch (Throwable var3) {
             //省略无关代码......
          }
          //省略无关代码......
      }
  ```

  源码分析初始化逻辑

  ```java
  void init(Channel channel) throws Exception {
      Map<ChannelOption<?>, Object> options = this.options0();
      synchronized(options) {
          setChannelOptions(channel, options, logger);
      }
  
      Map<AttributeKey<?>, Object> attrs = this.attrs0();
      synchronized(attrs) {
          Iterator var5 = attrs.entrySet().iterator();
  
          while(true) {
              if (!var5.hasNext()) {
                  break;
              }
  
              Entry<AttributeKey<?>, Object> e = (Entry)var5.next();
              AttributeKey<Object> key = (AttributeKey)e.getKey();
              channel.attr(key).set(e.getValue());
          }
      }
  
      ChannelPipeline p = channel.pipeline();
      final EventLoopGroup currentChildGroup = this.childGroup;
      final ChannelHandler currentChildHandler = this.childHandler;
      final Entry[] currentChildOptions;
      synchronized(this.childOptions) {
          currentChildOptions = (Entry[])this.childOptions.entrySet().toArray(newOptionArray(0));
      }
  
      final Entry[] currentChildAttrs;
      synchronized(this.childAttrs) {
          currentChildAttrs = (Entry[])this.childAttrs.entrySet().toArray(newAttrArray(0));
      }
  
      p.addLast(new ChannelHandler[]{new ChannelInitializer<Channel>() {
          public void initChannel(final Channel ch) throws Exception {
              final ChannelPipeline pipeline = ch.pipeline();
              ChannelHandler handler = ServerBootstrap.this.config.handler();
              if (handler != null) {
                  pipeline.addLast(new ChannelHandler[]{handler});
              }
  
              ch.eventLoop().execute(new Runnable() {
                  public void run() {
                      pipeline.addLast(new ChannelHandler[]{new ServerBootstrap.ServerBootstrapAcceptor(ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs)});
                  }
              });
          }
      }});
  }
  ```

  

  上述的代码中可以看见设置属性`options`，`attrs`，`childOptions`，`childAttrs`都是保存在一个局部变量`LinkedHashMap`中的，逻辑很简单没有什么可以讲的。

  配置`handler`也是直接从服务端的配置中直接添加到`pipeline`

  `ServerBootstrapAcceptor`是配置了`childHandler`和`childOptions`，`childAttrs`以及用户自定义的`workGroup`的连接器。