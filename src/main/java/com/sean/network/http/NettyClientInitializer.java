package com.sean.network.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.ssl.SslContext;

import java.util.concurrent.CountDownLatch;

/**
 * Created by guozhenbin on 2017/7/13.
 */
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private SslContext _sslCtx;
    private NettyHttpClient.BaseCallback _callback;

    public NettyClientInitializer(SslContext sslContext, NettyHttpClient.BaseCallback callback, CountDownLatch latch) {
        this._sslCtx = sslContext;
        this._callback = callback;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        if (null != _sslCtx) {
            socketChannel.pipeline().addLast(_sslCtx.newHandler(socketChannel.alloc()));
        }
        socketChannel.pipeline().addLast(new HttpClientCodec());
    }

}
