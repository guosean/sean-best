package com.sean.network.http;

import com.google.common.util.concurrent.SettableFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by guozhenbin on 2017/7/13.
 */
public class HttpResponseHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger("netty.do");

    private static AtomicInteger count = new AtomicInteger(0);

    private SettableFuture<ResponseWrapper> future;

    private int status;
    private ResponseWrapper _wrapper = new ResponseWrapper();

    private final int handlerCount;

    public HttpResponseHandler(SettableFuture future) {
        this.future = future;
        handlerCount = count.addAndGet(1);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        LOG.info(handlerCount+":channelRegistered");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOG.info(handlerCount+":channelUnregistered");
    }

    public void channelActive(ChannelHandlerContext ctx){
        LOG.info(handlerCount+":channelActive");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info(handlerCount+":channelInactive");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        LOG.info(handlerCount+":channelReadComplete");
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        LOG.info(handlerCount+":userEventTriggered:" + evt);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        LOG.info(handlerCount+":channelWritabilityChanged");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead:"+msg);
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            status = response.status().code();
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            LOG.info(String.format(handlerCount+":httcontent:%s", content.getClass()));
            if (content instanceof LastHttpContent) {
                LOG.info(handlerCount+":closing connection");
//                ctx.close();
            } else {

                String responseContent = content.content().toString(CharsetUtil.UTF_8);
                _wrapper.responseCode = status;
                _wrapper.responseContent = responseContent;
                if (null != future) {
                    future.set(_wrapper);
                    LOG.info(String.format(handlerCount+":future-%d:%s", handlerCount,future));
                }

            }
        }
        LOG.info(String.format(handlerCount+":read %s",msg.getClass()));
    }

    public ResponseWrapper getResponse() {
        return _wrapper;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ResponseWrapper.ErrorEntity errorEntity = new ResponseWrapper.ErrorEntity();
        errorEntity.message = cause.getMessage();
        ResponseWrapper.ErrorObject errorObject = new ResponseWrapper.ErrorObject();
        errorObject.error = errorEntity;
        _wrapper.error = errorObject;
        future.set(_wrapper);
        LOG.error(handlerCount+":exceptionCaught:" + cause,cause);
    }


}
