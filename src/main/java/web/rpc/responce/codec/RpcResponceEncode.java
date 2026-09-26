package web.rpc.responce.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import web.rpc.responce.RpcResponce;

import java.nio.charset.StandardCharsets;

public class RpcResponceEncode extends MessageToByteEncoder<RpcResponce> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcResponce response, ByteBuf out) throws Exception {
        writeString(out, String.valueOf(response.getSuccess()));
        Object data = response.getData();
        writeString(out, data == null ? "" : data.getClass().getName());

        byte[] dataBytes = data == null ? new byte[0] : objectMapper.writeValueAsBytes(data);
        out.writeInt(dataBytes.length);
        out.writeBytes(dataBytes);
        writeString(out, response.getErrorMessage());
    }

    private void writeString(ByteBuf out, String value) {
        byte[] bytes = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
