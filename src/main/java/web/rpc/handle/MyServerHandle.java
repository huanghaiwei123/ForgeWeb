package web.rpc.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import web.rpc.RpcInvoke;
import web.rpc.request.RpcRequest;
import web.rpc.responce.RpcResponce;

public class MyServerHandle extends ChannelInboundHandlerAdapter {
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest res = (RpcRequest) msg;
        RpcResponce responce = new RpcInvoke().invoke(res);
        ctx.writeAndFlush(responce).addListener(io.netty.channel.ChannelFutureListener.CLOSE);
    }
}
