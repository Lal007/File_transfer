package com.lka.netty.work.server;

import com.lka.netty.work.common.AuthAnswer;
import com.lka.netty.work.common.AuthorizationReq;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    boolean authOK = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof AuthorizationReq){
            // TODO add DB
            if (((AuthorizationReq) msg).getLogin().equals("user1") && ((AuthorizationReq) msg).getPass().equals("123")){
                authOK = true;
                ctx.writeAndFlush(new AuthAnswer(true));
                System.out.println("auth ok");
                return;
            }
        }else {
            if (authOK) {
                ctx.fireChannelRead(msg);
                return;
            }
        }

        ctx.writeAndFlush(new AuthAnswer(false));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
