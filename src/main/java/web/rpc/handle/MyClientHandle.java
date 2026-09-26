package web.rpc.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import web.rpc.request.RpcRequest;
import web.rpc.responce.RpcResponce;

import java.util.concurrent.CompletableFuture;

public class MyClientHandle extends ChannelInboundHandlerAdapter {
    private final CompletableFuture<RpcResponce> future;
    private final RpcRequest request;
    public MyClientHandle(RpcRequest request, CompletableFuture<RpcResponce> future) {
        this.request = request;
        this.future = future;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel is connected");
        ctx.writeAndFlush(request);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
       future.complete((RpcResponce) msg);
       ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        future.completeExceptionally(cause);
        ctx.close();
    }
}
