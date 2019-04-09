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
