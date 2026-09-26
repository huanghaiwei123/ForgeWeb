package web.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import web.rpc.handle.MyClientHandle;
import web.rpc.request.RpcRequest;
import web.rpc.request.codec.RpcRequestEncoder;
import web.rpc.responce.RpcResponce;
import web.rpc.responce.codec.RpcResponceDecode;

import java.util.concurrent.CompletableFuture;

/**
 * 远程控制客户端
 */
public class RpcClient {
    private final int port;
    private final String host;
    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public CompletableFuture<RpcResponce> run(RpcRequest request) throws InterruptedException {
        CompletableFuture<RpcResponce> future = new CompletableFuture<>();
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            sc.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                                    0, 4, 0, 4))
                                    .addLast(new RpcResponceDecode())
                                    .addLast(new LengthFieldPrepender(4))
                                    .addLast(new RpcRequestEncoder())
                                    .addLast(new MyClientHandle(request,future));
                        }
                    });
        ChannelFuture cf = bootstrap.connect(host, port).sync();
        cf.channel().closeFuture().addListener(ignored -> workGroup.shutdownGracefully());
        return future;
    }
}

