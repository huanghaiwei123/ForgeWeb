package web.rpc.request.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import web.rpc.request.RpcRequest;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class RpcRequestDecoder extends ByteToMessageDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        String interfaceName = readString(in);
        String methodName = readString(in);
        int parameterCount = in.readInt();
        Class<?>[] parameterTypes = new Class<?>[parameterCount];
        Object[] arguments = new Object[parameterCount];

        for (int i = 0; i < parameterCount; i++) {
            parameterTypes[i] = resolveClass(readString(in));
            int argumentLength = in.readInt();
            byte[] argumentBytes = new byte[argumentLength];
            in.readBytes(argumentBytes);
            arguments[i] = objectMapper.readValue(argumentBytes, parameterTypes[i]);
        }
        out.add(new RpcRequest(interfaceName, methodName, parameterTypes, arguments));
    }

    private String readString(ByteBuf in) {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Class<?> resolveClass(String name) throws ClassNotFoundException {
        return switch (name) {
            case "boolean" -> boolean.class;
            case "byte" -> byte.class;
            case "short" -> short.class;
            case "int" -> int.class;
            case "long" -> long.class;
            case "float" -> float.class;
            case "double" -> double.class;
            case "char" -> char.class;
            default -> Class.forName(name);
        };
    }
}
