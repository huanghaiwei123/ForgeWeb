package web.rpc.request.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import web.rpc.request.RpcRequest;

import java.nio.charset.StandardCharsets;

public class RpcRequestEncoder extends MessageToByteEncoder<RpcRequest> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequest request, ByteBuf out) throws Exception {
        Object[] arguments = request.getArguments() == null ? new Object[0] : request.getArguments();
        Class<?>[] parameterTypes = request.getParameterTypes() == null
                ? new Class<?>[0] : request.getParameterTypes();

        writeString(out, request.getInterfaceName());
        writeString(out, request.getMethodName());
        out.writeInt(parameterTypes.length);
        for (int i = 0; i < parameterTypes.length; i++) {
            writeString(out, parameterTypes[i].getName());
            byte[] argument = objectMapper.writeValueAsBytes(arguments[i]);
            out.writeInt(argument.length);
            out.writeBytes(argument);
        }
    }

    private void writeString(ByteBuf out, String value) {
        byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
