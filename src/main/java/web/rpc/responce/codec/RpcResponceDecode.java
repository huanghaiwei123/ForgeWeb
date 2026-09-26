package web.rpc.responce.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import web.rpc.responce.RpcResponce;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class RpcResponceDecode extends ByteToMessageDecoder {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        boolean success = Boolean.parseBoolean(readString(in));
        String type = readString(in);
        int dataLength = in.readInt();
        byte[] dataBytes = new byte[dataLength];
        in.readBytes(dataBytes);

        Object data = null;
        if (!type.isEmpty() && dataLength > 0) {
            data = objectMapper.readValue(dataBytes, resolveClass(type));
        }
        String errorMessage = readString(in);
        out.add(new RpcResponce(success, data, errorMessage));
    }

    private String readString(ByteBuf in) {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Class<?> resolveClass(String type) throws ClassNotFoundException {
        return switch (type) {
            case "boolean" -> Boolean.class;
            case "byte" -> Byte.class;
            case "short" -> Short.class;
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "float" -> Float.class;
            case "double" -> Double.class;
            case "char" -> Character.class;
            default -> Class.forName(type);
        };
    }
}
